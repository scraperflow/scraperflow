package scraper.api.converter;

import scraper.annotations.NotNull;
import scraper.annotations.Nullable;
import scraper.api.exceptions.ValidationException;

/**
 * Converts string to target (primitive) type. Precedence is as follows:
 * <li> null (if null given) or String 'null'
 * <li> Double
 * <li> Long
 * <li> Integer
 * <li> Boolean
 * <li> Enum
 * <li> String
 *
 * If the given string cannot be converted to given target class, a {@link ValidationException} is thrown.
 *
 * @since 1.0.0
 */
public final class StringToClassConverter {
    private StringToClassConverter(){}
    /**
     * Converts an object to a target class
     *
     * @param o object
     * @param target which target class is expected
     * @return string converted to target class, null if o is null or String 'null'
     * @throws ValidationException if string cannot be converted to target class
     */
    public static @Nullable Object convert(@Nullable final Object o, @NotNull final Class<?> target) throws ValidationException {

        // argument 'null'
        if(o == null) return null;
        // json 'null'
        if(o instanceof String && ((String) o).equalsIgnoreCase("null")) return null;

        if(target.isAssignableFrom(o.getClass())) return o;

        if(
                !(o instanceof String)
                        && !(o instanceof Integer)
                        && !(o instanceof Boolean)
                        && !(o instanceof Double)
        )
            throw new ValidationException("Could not convert object to target class; origin class: '"+ o.getClass()+"'; target class: '"+target+"'");

        String s = String.valueOf(o);

        if(Double.class.isAssignableFrom(target))
            try {
                return Double.valueOf(s);
            } catch (NumberFormatException e) {
                throw new ValidationException(e, "Could not convert string to Double");
            }

        if(Long.class.isAssignableFrom(target))
            try {
                return Long.valueOf(s);
            } catch (NumberFormatException e) {
                throw new ValidationException(e, "Could not convert string to Long");
            }

        if(Integer.class.isAssignableFrom(target))
            try {
                return Integer.valueOf(s);
            } catch (NumberFormatException e) {
                throw new ValidationException(e, "Could not convert string to Integer");
            }

        if(Boolean.class.isAssignableFrom(target)) {
            if(s.equalsIgnoreCase("false")) return false;
            else if(s.equalsIgnoreCase("true")) return true;
        }

        if (Enum.class.isAssignableFrom(target)) {
            Class<? extends Enum> e = target.asSubclass(Enum.class);

            // class cast should be thrown before, if enum cant be converted. TODO think about this a bit more
            try {
                @SuppressWarnings("unchecked")
                Enum<?> t = Enum.valueOf(e, s);
                return t;
            } catch (IllegalArgumentException | NullPointerException ex) {
                throw new ValidationException(ex, "Could not convert string to Enum");
            }
        }

        if (String.class.isAssignableFrom(target)) {
            return s;
        }

        throw new ValidationException("Could not convert string to target class. String: '"+s+"'; target class: '"+target+"'");
    }
}
