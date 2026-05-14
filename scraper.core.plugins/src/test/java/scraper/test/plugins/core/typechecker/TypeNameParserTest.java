package scraper.test.plugins.core.typechecker;

import org.junit.jupiter.api.Test;
import scraper.api.T;
import scraper.api.TypeGeneralizer;
import scraper.plugins.core.typechecker.data.TypeNameParser;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/** Exercises {@link TypeNameParser}'s variable-handling path — the piece that
 *  the existing unifier tests don't cover, because until now TypeVariables
 *  never crossed the subprocess boundary. */
public class TypeNameParserTest {

    @Test
    public void parsesPlainClass() {
        assertEquals(String.class, TypeNameParser.parse("java.lang.String"));
    }

    @Test
    public void parsesParameterizedWithoutVars() {
        Type t = TypeNameParser.parse("java.util.List<java.lang.Integer>");
        assertTrue(t instanceof ParameterizedType);
        assertEquals(List.class, ((ParameterizedType) t).getRawType());
        assertArrayEquals(new Type[]{Integer.class},
                ((ParameterizedType) t).getActualTypeArguments());
    }

    @Test
    public void unknownIdentifierIsFatalWithoutVarSet() {
        assertThrows(IllegalArgumentException.class,
                () -> TypeNameParser.parse("X"));
    }

    @Test
    public void parsesTypeVariable() {
        Type t = TypeNameParser.parse("X", Set.of("X"), "n1");
        assertTrue(t instanceof TypeVariable);
        assertEquals("n1$X", ((TypeVariable<?>) t).getName());
        assertEquals("n1$X", t.getTypeName());
    }

    @Test
    public void parsesParameterizedWithVariable() {
        Type t = TypeNameParser.parse("java.util.List<X>", Set.of("X"), "n1");
        assertTrue(t instanceof ParameterizedType);
        Type arg = ((ParameterizedType) t).getActualTypeArguments()[0];
        assertTrue(arg instanceof TypeVariable);
        assertEquals("n1$X", ((TypeVariable<?>) arg).getName());
    }

    @Test
    public void variablesFromDifferentScopesDontCollide() {
        Type a = TypeNameParser.parse("X", Set.of("X"), "nodeA");
        Type b = TypeNameParser.parse("X", Set.of("X"), "nodeB");
        assertNotEquals(a, b);
        assertEquals("nodeA$X", ((TypeVariable<?>) a).getName());
        assertEquals("nodeB$X", ((TypeVariable<?>) b).getName());
    }

    @Test
    public void variablesFromSameScopeAreEqual() {
        Type a = TypeNameParser.parse("X", Set.of("X"), "n1");
        Type b = TypeNameParser.parse("X", Set.of("X"), "n1");
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    /** The whole point: after parsing a variable with a given scope, the
     *  unifier's name-based capture map must find it under the scoped key. */
    @Test
    public void capturedByScopedNameInTypeGeneralizer() {
        Type scopedX = TypeNameParser.parse("X", Set.of("X"), "n1");

        Map<String, T<?>> captures = new HashMap<>();
        captures.put("n1$X", new T<String>() {});

        Type resolved = new TypeGeneralizer(captures) {}.visit(scopedX, Object.class);
        assertEquals(String.class, resolved);
    }

    @Test
    public void sameNameDifferentScopesResolveIndependently() {
        Type nodeAX = TypeNameParser.parse("X", Set.of("X"), "nodeA");
        Type nodeBX = TypeNameParser.parse("X", Set.of("X"), "nodeB");

        Map<String, T<?>> captures = new HashMap<>();
        captures.put("nodeA$X", new T<String>() {});
        captures.put("nodeB$X", new T<Integer>() {});

        assertEquals(String.class,
                new TypeGeneralizer(captures) {}.visit(nodeAX, Object.class));
        assertEquals(Integer.class,
                new TypeGeneralizer(captures) {}.visit(nodeBX, Object.class));
    }

    @Test
    public void parameterizedVariableRejectsArguments() {
        assertThrows(IllegalArgumentException.class,
                () -> TypeNameParser.parse("X<java.lang.String>", Set.of("X"), "n1"));
    }
}
