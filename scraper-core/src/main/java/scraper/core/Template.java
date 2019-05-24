package scraper.core;

import com.google.common.reflect.TypeToken;
import org.slf4j.Logger;
import scraper.api.exceptions.TemplateException;
import scraper.api.flow.FlowMap;

import java.util.*;

public abstract class Template<T> {

    private static final Logger log = org.slf4j.LoggerFactory.getLogger(Template.class);
    public final TypeToken<T> type = new TypeToken<>(getClass()){};

    // parsed JSON object
    private Object parsedJson;


    public T eval(final FlowMap o) {
        return eval(parsedJson, type, o);
    }

    protected <C> C eval(Object jsonObject, TypeToken<C> currentType, FlowMap o) {
        // if json null, return null regardless of currentType
        if(jsonObject == null) return null;

        // jsonObject can be: List<C>, Map<String, C>, String, Number, true, false


        // if JSON list, currentType is also a List<C>
        // descend and evaluate list elements with type C
        if (List.class.isAssignableFrom(jsonObject.getClass())) {
            // resolve ? type from List<?>
            TypeToken<?> elementType = currentType.resolveType(List.class.getTypeParameters()[0]);

            // create raw list
            List<Object> rawList = new ArrayList<>();

            // populate with target element type
            for (Object element : ((List) jsonObject)) {
                Object retElement = eval(element, elementType, o);
                rawList.add(retElement);
            }

            @SuppressWarnings("unchecked") // object types are checked if a raw type is encountered
            C castList =  (C) rawList;
            return castList;
        } // if JSON map, currentType is also a Map<String, ?>
        else if(Map.class.isAssignableFrom(jsonObject.getClass())) {
            // resolve ? type from Map<String, ?>
            TypeToken<?> elementType = currentType.resolveType(Map.class.getTypeParameters()[1]);

            // create raw Map
            Map<Object, Object> rawMap = new LinkedHashMap<>();

            // populate with target element type
            for (Object key : ((Map) jsonObject).keySet()) {
                Object retElement = eval(((Map) jsonObject).get(key), elementType, o);
                rawMap.put(key, retElement);
            }

            @SuppressWarnings("unchecked") // object types are checked if a raw type is encountered
            C castMap = (C) rawMap;
            return castMap;
        } // JSON primitive
        else {
            System.out.println(jsonObject);
            System.out.println(currentType);
            // case 1: current object is the same type as the current JSON object, use JSON object
            if (currentType.getRawType().isAssignableFrom(jsonObject.getClass())) {
                Object val = jsonObject;

                // case 1.2: If object type is unknown (i.e. type object), try to evaluate rawJson object, if present
                if(currentType.getRawType().equals(Object.class) && jsonObject instanceof TemplateString)
                    val = ((TemplateString<?>) jsonObject).eval(o);

                @SuppressWarnings("unchecked") // Checked with currentType argument
                C castVal = (C) val;
                return castVal;
            } // case 2: current object is a rawJson class
            else if (jsonObject instanceof TemplateString) {
                TemplateString<?> t = (TemplateString<?>) jsonObject;
                Object evalObj = t.eval(o);
                @SuppressWarnings("unchecked") // throws exception if type is not correct in eval step
                C castVal = (C) evalObj;
                return castVal;
            } // case 3: current object has unknown class. this can be the case if new rawJson classes are implemented
            else {
                log.error("Unknown rawJson object type found, missing Implementation? {}", jsonObject.getClass());
                throw new TemplateException("Unknown rawJson object: "+jsonObject.getClass()+ " ");
            }
        }
    }

    public void setParsedJson(Object convertedTemplateObject) {
        this.parsedJson = convertedTemplateObject;
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
        return getKeysDescend(parsedJson);
    }

    @Override
    public String toString() {
        return parsedJson.toString();
    }

}