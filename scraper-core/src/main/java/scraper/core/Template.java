package scraper.core;

import com.google.common.reflect.TypeToken;
import org.slf4j.Logger;
import scraper.api.exceptions.NodeException;
import scraper.api.flow.FlowMap;

import java.util.*;

public abstract class Template<T> {

    private static final Logger log = org.slf4j.LoggerFactory.getLogger(Template.class);
    public final TypeToken<T> type = new TypeToken<>(getClass()){};

    // parsed JSON object
    private Object template;


    public T eval(final FlowMap o) throws NodeException {
        Object templ = template;

        return eval(templ, type, o);
    }

    protected <C> C eval(Object currentObject, TypeToken<C> currentType, FlowMap o) throws NodeException {
        // if java null, return null regardless of currentType
        if(currentObject == null) return null;

        // if JSON list, currentType is also a List<?>
        if (List.class.isAssignableFrom(currentObject.getClass())) {
            // resolve ? type from List<?>
            TypeToken<?> elementType = currentType.resolveType(List.class.getTypeParameters()[0]);

            // create raw list
            List<Object> rawList = new ArrayList<>();

            // populate with target element type
            for (Object element : ((List) currentObject)) {
                Object retElement = eval(element, elementType, o);
                rawList.add(retElement);
            }

            @SuppressWarnings("unchecked") // object types are checked if a raw type is encountered
            C castList =  (C) rawList;
            return castList;
        } // if JSON map, currentType is also a Map<String, ?>
        else if(Map.class.isAssignableFrom(currentObject.getClass())) {
            // resolve ? type from Map<String, ?>
            TypeToken<?> elementType = currentType.resolveType(Map.class.getTypeParameters()[1]);

            // create raw Map
            Map<Object, Object> rawMap = new LinkedHashMap<>();

            // populate with target element type
            for (Object key : ((Map) currentObject).keySet()) {
                Object retElement = eval(((Map) currentObject).get(key), elementType, o);
                rawMap.put(key, retElement);
            }

            @SuppressWarnings("unchecked") // object types are checked if a raw type is encountered
            C castMap = (C) rawMap;
            return castMap;
        } // JSON primitive
        else {
            // case 1: current object is the same type as the current JSON object, use JSON object
            if (currentType.getRawType().isAssignableFrom(currentObject.getClass())) {
                Object val = currentObject;

                // case 1.2: If object type is unknown (i.e. type object), try to evaluate rawJson object, if present
                if(currentType.getRawType().equals(Object.class) && currentObject instanceof TemplateString)
                    val = ((TemplateString<?>) currentObject).eval(o);

                @SuppressWarnings("unchecked") // Checked with currentType argument
                C castVal = (C) val;
                return castVal;
            } // case 2: current object is a rawJson class
            else if (currentObject instanceof TemplateString) {
                TemplateString<?> t = (TemplateString<?>) currentObject;
                Object evalObj = t.eval(o);
                if(evalObj == null) {
                    log.error("Could not evaluate rawJson: {}", t);
                    throw new NodeException("Template evaluated to null: '"+t+"'. Check reflection implementation");
                }
                if(currentType.getRawType().isAssignableFrom(evalObj.getClass())) {
                    @SuppressWarnings("unchecked") // Checked with currentType argument
                    C castVal = (C) evalObj;
                    return castVal;
                } else {
                    log.error("Argument evaluated to wrong target type. Expected '{}', got '{}'", currentType, evalObj.getClass());
                    throw new NodeException("Argument evaluated to wrong target type. Expected '"
                            +currentType+"', got '"+evalObj.getClass()+"'. Check reflection implementation");
                }
            } // case 3: current object has unknown class. this can be the case if new rawJson classes are implemented
            else {
                log.error("Unknown rawJson object type found, missing Implementation? {}", currentObject.getClass());
                throw new NodeException("Unknown rawJson object: "+currentObject.getClass()+ ". Fix reflection implementation");
            }
        }
    }

    public void setTemplate(Object convertedTemplateObject) {
        this.template = convertedTemplateObject;
    }

    private Collection<String> getKeysDescend(Object toDescend) {
        Collection<String> allKeys = new HashSet<>();
        if(toDescend instanceof TemplateString) {
            return ((TemplateString<?>) toDescend).getKeysInTemplate();
        } else if (toDescend instanceof Collection) {
            ((Collection<?>) toDescend).forEach(element -> allKeys.addAll(getKeysDescend(element)));
        } else if (toDescend instanceof Map) {
            ((Map<?, ?>) toDescend).forEach((key, element) -> allKeys.addAll(getKeysDescend(element)));
        }
        // else ignore other (raw) data types
        return allKeys;
    }

    public Collection<String> getKeysInTemplate() {
        return getKeysDescend(template);
    }

    @Override
    public String toString() {
        return template.toString();
    }

}