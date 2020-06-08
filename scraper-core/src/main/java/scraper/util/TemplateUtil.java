package scraper.util;

import com.google.common.reflect.TypeParameter;
import com.google.common.reflect.TypeToken;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.slf4j.Logger;
import scraper.annotations.NotNull;
import scraper.api.exceptions.TemplateException;
import scraper.api.exceptions.ValidationException;
import scraper.api.template.ListTerm;
import scraper.api.template.T;
import scraper.api.template.Term;
import scraper.core.exp.TemplateExpressionVisitor;
import scraper.core.exp.TemplateLexer;
import scraper.core.exp.TemplateParser;
import scraper.core.template.TemplateConstant;
import scraper.core.template.TemplateExpression;
import scraper.core.template.TemplateList;
import scraper.core.template.TemplateMap;

import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public final class TemplateUtil {

    private static final Logger log = org.slf4j.LoggerFactory.getLogger(TemplateUtil.class);

    @SuppressWarnings({"unchecked", "rawtypes"}) // checked with types
    public static <K> Term<K> parseTemplate(@NotNull final Object term, @NotNull final T<K> targetType) throws ValidationException {
        TypeToken templ = TypeToken.of(targetType.get());


        if(List.class.isAssignableFrom(term.getClass()) && (
                List.class.isAssignableFrom(templ.getRawType()) || templ.getRawType().equals(Object.class))) {
            if ((targetType.get() instanceof TypeVariable)) {
                log.warn("Making type variable more precise: {} => {}", targetType.get(), "List<"+targetType.get().getTypeName()+">");
                return (Term<K>) parseTemplateL((List) term, (T<List<?>>) targetType, true);
            } else {
                return (Term<K>) parseTemplateL((List) term, (T<List<?>>) targetType, false);
            }
        } // JSON map
        else if(Map.class.isAssignableFrom(term.getClass()) && (
                Map.class.isAssignableFrom(templ.getRawType()) || templ.getRawType().equals(Object.class) // descend into object
        )) {
            if ((targetType.get() instanceof TypeVariable)) {
                log.warn("Making type variable more precise: {} => {}", targetType.get(), "Map<String, " + targetType.get().getTypeName() + ">");
                return (Term<K>) parseTemplateM((Map<String, ?>) term, (T<Map<String, ?>>) targetType, true);
            } else {
                return (Term<K>) parseTemplateM((Map<String, ?>) term, (T<Map<String, ?>>) targetType, false);
            }
        }
        // primitives
        else {
            // raw type
            if(templ.getRawType().isAssignableFrom(term.getClass()) && !String.class.isAssignableFrom(term.getClass())) {
                // same types, return actual object
                TemplateConstant<K> constant = new TemplateConstant<>((K) term, targetType);
//                constant.eval();
                return constant;
            } else if (String.class.isAssignableFrom(term.getClass())) {
                // string template found
                return parseTemplate(((String) term), targetType);
            } else {
                throw new ValidationException("Argument type mismatch! Expected String or "+templ+", but found "+ term.getClass());
            }
        }
    }

    public static <C> Term<C> parseTemplate(@NotNull final String term,
                                            @NotNull final T<C> targetType) {
        log.trace("Converting term to template expression: '{}'", term);

        try {
            TemplateLexer lexer = new TemplateLexer(CharStreams.fromString(term));
            lexer.removeErrorListeners();
            lexer.addErrorListener(ThrowingErrorListener.INSTANCE);

            TemplateParser parser = new TemplateParser(new CommonTokenStream(lexer));
            parser.removeErrorListeners();
            parser.addErrorListener(ThrowingErrorListener.INSTANCE);

            TemplateExpressionVisitor<C> visitor = new TemplateExpressionVisitor<>(targetType);
            TemplateParser.RootContext topExpression = parser.root();
            TemplateExpression<C> convertedTerm = visitor.visitRoot(topExpression);

            log.trace("Converted term: '{}'", convertedTerm);
            return convertedTerm;
        } catch (Exception e) {
            throw new TemplateException(e, "Bad template syntax: " + e.getMessage());
        }
    }

    public static <K, V> TypeToken<Map<K, V>> mapOf(TypeToken<K> keyType, TypeToken<V> valueType) {
        return new TypeToken<Map<K, V>>() {}
                .where(new TypeParameter<>() {}, keyType)
                .where(new TypeParameter<>() {}, valueType);
    }

    public static <K> TypeToken<List<K>> listOf(TypeToken<K> elementType) {
        return new TypeToken<List<K>>() {}.where(new TypeParameter<>() {}, elementType);
    }

    public static <K> T<List<K>> listOf(Type t) {
        return new T<>(listOf(TypeToken.of(t)).getType()){};
    }

    public static <K> T<List<K>> listOf(T<K> elementType) {
        TypeToken<List<K>> list = listOf((TypeToken<K>) TypeToken.of(elementType.get()));
        return new T<>(list.getType()){};
    }

    public static <K, V> T<Map<K, V>> mapOf(T<K> keyType, T<V> elementType) {
        TypeToken<Map<K, V>> list = mapOf((TypeToken<K>) TypeToken.of(keyType.get()),(TypeToken<V>) TypeToken.of(elementType.get()));
        return new T<>(list.getType()){};
    }

    // ==============
    // descend into lists and maps helper functions
    // ==============

    @SuppressWarnings("unchecked") // checked
    private static Term<Map<String, ?>> parseTemplateM(@NotNull final Map<String, ?> term,
                                                       @NotNull final T<Map<String, ?>> targetType, boolean fromTypeVariable) throws ValidationException {
        TypeToken<?> templ = TypeToken.of(targetType.get());
        // create a map with checked values
        TypeToken<?> elementType = (templ.getRawType().equals(Object.class) ? templ : templ.resolveType(Map.class.getTypeParameters()[1]));

        Map<String, Term<?>> resultMap = new HashMap<>();

        for (String k : term.keySet()) {
            resultMap.put(k, parseTemplate(term.get(k), new T<>(elementType.getType()) {}));

        }

        return new TemplateMap(resultMap, new T<>(elementType.getType()){}, targetType, fromTypeVariable);
    }

    @SuppressWarnings("unchecked") // checked
    private static ListTerm<?> parseTemplateL(@NotNull final List<?> term,
                                              @NotNull final T<List<?>> listType, boolean fromTypeVariable) throws ValidationException {
        TypeToken<?> templ = TypeToken.of(listType.get());
        // create a list with converted values
        TypeToken<?> elementType = (templ.getRawType().equals(Object.class) ? templ : templ.resolveType(List.class.getTypeParameters()[0]));

        List<Term<?>> resultList = new ArrayList<>();

        for (Object o : term) {
            resultList.add(parseTemplate(o, new T<>(elementType.getType()){}));
        }

        System.out.println();


        return new TemplateList(resultList, listType, fromTypeVariable);
    }
}

class ThrowingErrorListener extends BaseErrorListener {

    static final ThrowingErrorListener INSTANCE = new ThrowingErrorListener();

    @Override
    public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e)
            throws ParseCancellationException {
        throw new ParseCancellationException("line " + line + ":" + charPositionInLine + " " + msg);
    }
}
