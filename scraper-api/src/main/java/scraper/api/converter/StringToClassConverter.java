package scraper.api.converter;

import scraper.api.exceptions.ValidationException;

/**
 * Converts string to target (primitive) type. Precedence is as follows:
 * <li> null (if null given)
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
    /**
     * Converts a json string to a target class
     *
     * @param s json string
     * @param target which target class is expected
     * @return string converted to target class
     * @throws ValidationException if string cannot be converted to target class
     */
    public static Object convert(final String s, final Class<?> target) throws ValidationException {
        if(target == null) throw new ValidationException("Target class should not be null");

        // argument 'null'
        if(s == null) return null;
        // json 'null'
        if(s.equalsIgnoreCase("null")) return null;
        // TODO #18
        if(target.equals(Object.class))
            return s;

        if(Double.class.isAssignableFrom(target))
            try {
                return Double.valueOf(s);
            } catch (NumberFormatException e) {
                throw new ValidationException("Could not convert string to Double", e);
            }

        if(Long.class.isAssignableFrom(target))
            try {
                return Long.valueOf(s);
            } catch (NumberFormatException e) {
                throw new ValidationException("Could not convert string to Long", e);
            }

        if(Integer.class.isAssignableFrom(target))
            try {
                return Integer.valueOf(s);
            } catch (NumberFormatException e) {
                throw new ValidationException("Could not convert string to Integer", e);
            }

        if(Boolean.class.isAssignableFrom(target)) {
            if(String.valueOf(s).equalsIgnoreCase("false")) return false;
            else if(String.valueOf(s).equalsIgnoreCase("true")) return true;
        }

        if (Enum.class.isAssignableFrom(target)) {
            Class<? extends Enum> e = target.asSubclass(Enum.class);

            // class cast should be thrown before, if enum cant be converted. TODO think about this a bit more
            try {
                @SuppressWarnings("unchecked")
                Enum<?> t = Enum.valueOf(e, String.valueOf(s));
                return t;
            } catch (IllegalArgumentException | NullPointerException ex) {
                throw new ValidationException("Could not convert string to Enum", ex);
            }
        }

        if (String.class.isAssignableFrom(target)) {
            return s;
        }

        throw new ValidationException("Could not convert string to target class. String: '"+s+"'; target class: '"+target+"'");
    }
}
