package scraper.core;

import com.google.common.reflect.TypeToken;
import org.slf4j.Logger;
import scraper.api.flow.FlowMap;
import scraper.api.node.TypesafeObject;
import scraper.api.exceptions.NodeException;

import java.util.function.Supplier;


public abstract class MapKey<T> {
    private static final Logger log = org.slf4j.LoggerFactory.getLogger(MapKey.class);
    public final TypeToken<T> type = new TypeToken<>(getClass()){};

    public String key;
    private Supplier<T> base;
    private boolean raw = false;
    private boolean failOnMissing = false;

    public T eval(FlowMap o) throws NodeException {
        Object value = o.get(key);
        if(value == null && base != null) value = base.get();
        if(value == null && failOnMissing) {
            log.error("Missing flow value at key {}", key);
            throw new NodeException("Missing flow value at key '"+key+"'");
        }
        if(value == null) return null;


        if(raw || !type.toString().contains("<")) { // check raw type only if enabled or if type is raw type itself
            if(type.getRawType().isAssignableFrom(value.getClass())) {
                // explicit raw type checked
                @SuppressWarnings("unchecked") T castVal = (T) value;
                return castVal;
            } else {
                log.error("Unexpected raw type at map key '{}'. Expected {}, got {}", key, type.getRawType(), value.getClass());
                throw new NodeException("Unexpected raw type at map key '"+key+"'. Expected "+type.getRawType()
                        +", got "+value.getClass()+ ". Check scrape definition");
            }
        }


        try { // try type safe check first
            TypesafeObject object = (TypesafeObject) value;
            if(object.getType().isSubtypeOf(type) && type.isSubtypeOf(object.getType())) {
                // type safe by checking type tokens
                @SuppressWarnings("unchecked") T castVal = (T) value;
                return castVal;
            } else {
                log.error("Unexpected type at map key '{}'. Expected {}, got {} ({})", key, type, object.getType(), value.getClass());
                throw new NodeException("Unexpected type at map key '"+key+"'. Expected "+type+", got "
                        +object.getType()+" ("+value.getClass()+"). Check scrape definition");
            }
        } catch (ClassCastException e) {
            // if not a TypesafeObject, fail if not specified to continue
            String fail = System.getProperty("scraper.failOnNonSafeMapKey", null);
            if(fail == null) {
                log.error("Object not a type-safe map key value, got {} ({})", value.getClass(), key);
                throw new NodeException("Object not a type-safe map key value, got " +
                        value.getClass()+"! ("+key+"). Check scrape definition");
            }

            log.warn("Not using type-safe objects for MapKey! (key: '{}')", key);
            // warning for unsafe usage
            @SuppressWarnings("unchecked") T castVal = (T) value;
            return castVal;
        }
    }

    public MapKey<T> base(Supplier<T> base) {
        this.base = base;
        return this;
    }

    public MapKey<T> raw(Supplier<T> rawBase) {
        this.base = rawBase;
        raw = true;
        return this;
    }

    public MapKey<T> failOnMissing() {
        failOnMissing = true;
        return this;
    }
}