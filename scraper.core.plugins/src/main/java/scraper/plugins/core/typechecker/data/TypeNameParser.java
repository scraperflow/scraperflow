package scraper.plugins.core.typechecker.data;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Reconstructs a {@link java.lang.reflect.Type} from the string produced by
 *  {@link Type#getTypeName()}. Handles {@code Class}, {@code ParameterizedType},
 *  {@code GenericArrayType}, {@code WildcardType}, and — when a set of known
 *  variable names is supplied — {@code TypeVariable}.
 *
 *  <p>Variables are alpha-renamed on the way in: every variable identifier is
 *  keyed as {@code "<scope>$<name>"} in {@link TypeVariable#getName()} and
 *  {@link TypeVariable#getTypeName()}, so two different helper nodes that both
 *  declare a variable called {@code T} don't collide in the host type
 *  checker's captures map.
 *
 *  Grammar (roughly):
 *  <pre>
 *    Type   = '?'' ('extends' Type | 'super' Type)?
 *           | FQCN ('&lt;' Type (',' Type)* '&gt;')? ('[' ']')*
 *    FQCN   = [a-zA-Z_$] [a-zA-Z_$0-9.]*    (primitive keywords also allowed)
 *  </pre>
 */
public final class TypeNameParser {
    private TypeNameParser(String s, Set<String> typeVariables, String scope) {
        this.s = s;
        this.typeVariables = typeVariables == null ? Collections.emptySet() : typeVariables;
        this.scope = scope;
    }

    public static Type parse(String typeName) {
        return parse(typeName, Collections.emptySet(), null);
    }

    /** Parse {@code typeName}, treating each identifier in {@code typeVariables}
     *  as a type variable rather than a class. {@code scope} disambiguates
     *  same-named variables across nodes by prefixing the variable's reported
     *  name with {@code "<scope>$"}. */
    public static Type parse(String typeName, Set<String> typeVariables, String scope) {
        TypeNameParser p = new TypeNameParser(typeName, typeVariables, scope);
        Type t = p.parseType();
        p.skipSpaces();
        if (p.i != p.s.length()) {
            throw new IllegalArgumentException(
                    "Trailing input in type name at position " + p.i + ": " + typeName);
        }
        return t;
    }

    private static final Map<String, Class<?>> PRIMITIVES = Map.of(
            "boolean", boolean.class, "byte", byte.class, "short", short.class,
            "int", int.class, "long", long.class, "float", float.class,
            "double", double.class, "char", char.class, "void", void.class);

    private final String s;
    private final Set<String> typeVariables;
    private final String scope;
    private int i = 0;

    private Type parseType() {
        skipSpaces();
        Type base;
        if (peek() == '?') {
            i++;
            skipSpaces();
            if (startsWith("extends ")) {
                i += "extends ".length();
                base = new WildcardTypeImpl(new Type[]{parseType()}, new Type[0]);
            } else if (startsWith("super ")) {
                i += "super ".length();
                base = new WildcardTypeImpl(new Type[]{Object.class}, new Type[]{parseType()});
            } else {
                base = new WildcardTypeImpl(new Type[]{Object.class}, new Type[0]);
            }
        } else {
            String name = readIdentifier();
            if (typeVariables.contains(name)) {
                base = new TypeVariableImpl(scopedName(name));
                skipSpaces();
                if (peek() == '<') {
                    throw new IllegalArgumentException(
                            "Type variable '" + name + "' cannot take type arguments at " + i + ": " + s);
                }
            } else {
                Class<?> raw = resolveClass(name);
                skipSpaces();
                if (peek() == '<') {
                    i++;
                    List<Type> args = new ArrayList<>();
                    args.add(parseType());
                    skipSpaces();
                    while (peek() == ',') {
                        i++;
                        args.add(parseType());
                        skipSpaces();
                    }
                    expect('>');
                    base = new ParameterizedTypeImpl(raw, args.toArray(new Type[0]), null);
                } else {
                    base = raw;
                }
            }
        }
        skipSpaces();
        while (startsWith("[]")) {
            i += 2;
            skipSpaces();
            base = (base instanceof Class<?>)
                    ? java.lang.reflect.Array.newInstance((Class<?>) base, 0).getClass()
                    : new GenericArrayTypeImpl(base);
        }
        return base;
    }

    private String scopedName(String name) {
        return (scope == null || scope.isEmpty()) ? name : scope + "$" + name;
    }

    private String readIdentifier() {
        skipSpaces();
        int start = i;
        if (i >= s.length() || !isIdStart(s.charAt(i))) {
            throw new IllegalArgumentException("Expected identifier at " + i + ": " + s);
        }
        i++;
        while (i < s.length() && isIdPart(s.charAt(i))) i++;
        return s.substring(start, i);
    }

    private Class<?> resolveClass(String name) {
        Class<?> primitive = PRIMITIVES.get(name);
        if (primitive != null) return primitive;
        try {
            return Class.forName(name);
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("Unknown class in type name: " + name, e);
        }
    }

    private void skipSpaces() {
        while (i < s.length() && s.charAt(i) == ' ') i++;
    }

    private char peek() { return i < s.length() ? s.charAt(i) : '\0'; }

    private boolean startsWith(String prefix) { return s.startsWith(prefix, i); }

    private void expect(char c) {
        if (peek() != c) {
            throw new IllegalArgumentException(
                    "Expected '" + c + "' at " + i + ": " + s);
        }
        i++;
    }

    private static boolean isIdStart(char c) {
        return Character.isJavaIdentifierStart(c);
    }

    private static boolean isIdPart(char c) {
        return Character.isJavaIdentifierPart(c) || c == '.';
    }

    // ---- minimal Type implementations ------------------------------------

    private static final class ParameterizedTypeImpl implements ParameterizedType {
        private final Type raw;
        private final Type[] actuals;
        private final Type owner;

        ParameterizedTypeImpl(Type raw, Type[] actuals, Type owner) {
            this.raw = raw; this.actuals = actuals; this.owner = owner;
        }

        @Override public Type[] getActualTypeArguments() { return actuals.clone(); }
        @Override public Type getRawType() { return raw; }
        @Override public Type getOwnerType() { return owner; }

        @Override public boolean equals(Object o) {
            if (!(o instanceof ParameterizedType)) return false;
            ParameterizedType that = (ParameterizedType) o;
            return java.util.Objects.equals(raw, that.getRawType())
                    && java.util.Objects.equals(owner, that.getOwnerType())
                    && Arrays.equals(actuals, that.getActualTypeArguments());
        }
        @Override public int hashCode() {
            return Arrays.hashCode(actuals) ^ java.util.Objects.hashCode(raw)
                    ^ java.util.Objects.hashCode(owner);
        }
        @Override public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(raw.getTypeName()).append('<');
            for (int k = 0; k < actuals.length; k++) {
                if (k > 0) sb.append(", ");
                sb.append(actuals[k].getTypeName());
            }
            sb.append('>');
            return sb.toString();
        }
    }

    private static final class GenericArrayTypeImpl implements GenericArrayType {
        private final Type component;
        GenericArrayTypeImpl(Type component) { this.component = component; }
        @Override public Type getGenericComponentType() { return component; }
        @Override public boolean equals(Object o) {
            return o instanceof GenericArrayType
                    && java.util.Objects.equals(component, ((GenericArrayType) o).getGenericComponentType());
        }
        @Override public int hashCode() { return java.util.Objects.hashCode(component); }
        @Override public String toString() { return component.getTypeName() + "[]"; }
    }

    /** A stand-alone {@link TypeVariable} whose identity is purely the scoped
     *  name. The unifier (both {@code TypeGeneralizer} and
     *  {@code ReplaceCapturesOrCrashVisitor}) compares type variables by
     *  {@code getName()} / {@code getTypeName()} strings, so returning the
     *  scoped name is sufficient alpha-renaming across subprocess boundaries.
     *
     *  <p>Bounds, annotations, and {@code getGenericDeclaration()} aren't
     *  consulted by scraperflow's type checker (see audit of {@code
     *  TypeGeneralizer} / {@code ReplaceCapturesOrCrashVisitor}) so they're
     *  given inert defaults. A future change that actually consults bounds
     *  would need a schema extension to carry them. */
    private static final class TypeVariableImpl implements TypeVariable<GenericDeclaration> {
        private final String name;
        TypeVariableImpl(String name) { this.name = name; }
        @Override public Type[] getBounds() { return new Type[]{Object.class}; }
        @Override public GenericDeclaration getGenericDeclaration() { return null; }
        @Override public String getName() { return name; }
        @Override public java.lang.reflect.AnnotatedType[] getAnnotatedBounds() {
            return new java.lang.reflect.AnnotatedType[0];
        }
        @Override public <T extends Annotation> T getAnnotation(Class<T> cls) { return null; }
        @Override public Annotation[] getAnnotations() { return new Annotation[0]; }
        @Override public Annotation[] getDeclaredAnnotations() { return new Annotation[0]; }
        @Override public String getTypeName() { return name; }
        @Override public boolean equals(Object o) {
            return o instanceof TypeVariable
                    && java.util.Objects.equals(name, ((TypeVariable<?>) o).getName());
        }
        @Override public int hashCode() { return java.util.Objects.hashCode(name); }
        @Override public String toString() { return name; }
    }

    private static final class WildcardTypeImpl implements WildcardType {
        private final Type[] upper;
        private final Type[] lower;
        WildcardTypeImpl(Type[] upper, Type[] lower) { this.upper = upper; this.lower = lower; }
        @Override public Type[] getUpperBounds() { return upper.clone(); }
        @Override public Type[] getLowerBounds() { return lower.clone(); }
        @Override public boolean equals(Object o) {
            if (!(o instanceof WildcardType)) return false;
            WildcardType that = (WildcardType) o;
            return Arrays.equals(upper, that.getUpperBounds())
                    && Arrays.equals(lower, that.getLowerBounds());
        }
        @Override public int hashCode() { return Arrays.hashCode(upper) ^ Arrays.hashCode(lower); }
        @Override public String toString() {
            if (lower.length > 0) return "? super " + lower[0].getTypeName();
            if (upper.length > 0 && upper[0] != Object.class) return "? extends " + upper[0].getTypeName();
            return "?";
        }
    }
}
