package scraper.core;

import com.google.common.reflect.TypeToken;
import org.slf4j.Logger;
import scraper.api.exceptions.TemplateException;
import scraper.api.flow.FlowMap;
import scraper.api.flow.impl.IdentityFlowMap;
import scraper.core.template.TemplateExpression;

import java.util.*;

public abstract class Template<T> {

    private static final Logger log = org.slf4j.LoggerFactory.getLogger(Template.class);
    public final TypeToken<T> type = new TypeToken<>(getClass()){};

    // parsed JSON object
    private Object parsedJson;


    public T eval(final FlowMap o) {
        return eval(parsedJson, type, o);
    }

    public T evalWithIdentity() {
        return eval(parsedJson, type, new IdentityFlowMap());
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

            // populate with goTo element type
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

            // populate with goTo element type
            for (Object key : ((Map) jsonObject).keySet()) {
                Object retElement = eval(((Map) jsonObject).get(key), elementType, o);
                rawMap.put(key, retElement);
            }

            @SuppressWarnings("unchecked") // object types are checked if a raw type is encountered
            C castMap = (C) rawMap;
            return castMap;
        } // JSON primitive
        else {
            // case 1: current object is the same type as the current JSON object, use JSON object
            if (currentType.getRawType().isAssignableFrom(jsonObject.getClass()) && !(jsonObject instanceof TemplateExpression)) {
                @SuppressWarnings("unchecked") // Checked with currentType argument
                C castVal = (C) jsonObject;
                return castVal;
            } // case 2: template expression
            else if (jsonObject instanceof TemplateExpression) {
                TemplateExpression<?> t = (TemplateExpression<?>) jsonObject;
                Object evalObj = t.eval(o);
                @SuppressWarnings("unchecked") // throws exception if type is not correct in eval step
                C castVal = (C) evalObj;
                return castVal;
            } // case 3: current object has unknown class. this can be the case if new rawJson classes are implemented, e.g. something different than Template
            else {
                log.error("Unknown rawJson object: {}; missing implementation?", jsonObject.getClass());
                throw new TemplateException("Unknown rawJson object: "+jsonObject.getClass()+ "; missing implementation? ");
            }
        }
    }

    public void setParsedJson(Object convertedTemplateObject) {
        this.parsedJson = convertedTemplateObject;
    }

    private Collection<String> getKeysDescend(Object toDescend, FlowMap o) {
        Collection<String> allKeys = new HashSet<>();
        if(toDescend instanceof TemplateExpression) {
            return ((TemplateExpression<?>) toDescend).getKeysInTemplate(o);
        } else if (toDescend instanceof Collection) {
            ((Collection<?>) toDescend).forEach(element -> allKeys.addAll(getKeysDescend(element, o)));
        } else if (toDescend instanceof Map) {
            ((Map<?, ?>) toDescend).forEach((key, element) -> allKeys.addAll(getKeysDescend(element, o)));
        }
        // else ignore other (raw) data types
        return allKeys;
    }

    public Collection<String> getKeysInTemplate(FlowMap o) {
        return getKeysDescend(parsedJson, o);
    }

    @Override
    public String toString() {
        return parsedJson.toString();
    }

    public String raw() { return parsedJson.toString(); }

    public T input(FlowMap o) { return this.eval(o); }

    public void output(FlowMap o, T object) {
        o.put(parsedJson.toString(), object);
    }

}