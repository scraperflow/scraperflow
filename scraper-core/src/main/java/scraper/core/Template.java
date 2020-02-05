package scraper.core;

import com.google.common.reflect.TypeToken;
import org.slf4j.Logger;
import scraper.annotations.NotNull;
import scraper.annotations.Nullable;
import scraper.api.exceptions.TemplateException;
import scraper.api.flow.FlowMap;
import scraper.api.reflect.T;
import scraper.core.template.TemplateExpression;

import java.util.*;

public class Template {

    private static final Logger log = org.slf4j.LoggerFactory.getLogger(Template.class);

    @Nullable
    public static <C> C eval(@NotNull T<C> type, @NotNull FlowMap o) {
        return eval(type.getParsedJson(), TypeToken.of(type.get()), o);
    }

    private static <C> C eval(@Nullable final Object jsonObject,
                                @NotNull final TypeToken<?> currentType,
                                @NotNull final FlowMap o) {
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

    @NotNull
    private static Collection<String> getKeysDescend(@NotNull final Object toDescend,
                                                     @NotNull final FlowMap o) {
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

    @NotNull
    public static Collection<String> getKeysInTemplate(@NotNull T<?> t, @NotNull FlowMap o) {
        return getKeysDescend(t.getParsedJson(), o);
    }
}