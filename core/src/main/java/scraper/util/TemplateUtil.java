package scraper.util;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import scraper.annotations.NotNull;
import scraper.api.exceptions.TemplateException;
import scraper.api.exceptions.ValidationException;
import scraper.api.template.*;
import scraper.core.exp.TemplateExpressionVisitor;
import scraper.core.exp.TemplateLexer;
import scraper.core.exp.TemplateParser;
import scraper.core.template.TemplateConstant;
import scraper.core.template.TemplateExpression;
import scraper.core.template.TemplateList;
import scraper.core.template.TemplateMap;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.System.Logger.Level.*;


public final class TemplateUtil {

    private static final System.Logger log = System.getLogger("TemplateUtil");


    @SuppressWarnings({"unchecked", "rawtypes"}) // checked with types
    public static <K> Term<K> parseTemplate(@NotNull final Object term, @NotNull final T<K> targetType) throws ValidationException {
        T<K> templ = targetType;


        if (List.class.isAssignableFrom(term.getClass()) && (
                List.class.isAssignableFrom(templ.getRawType()) || templ.getRawType().equals(Object.class))) {
            if ((targetType.get() instanceof TypeVariable)) {
                Term<K> tt = (Term<K>) parseTemplateL((List) term, (T<List<?>>) targetType, true);
                log.log(DEBUG,  "Making type variable more precise: {0} => {1}", tt.getTypeString(), "List<" + ((ListTerm) tt).getElementType().getTypeString() + ">");
                return tt;
            } else {
                return (Term<K>) parseTemplateL((List) term, (T<List<?>>) targetType, false);
            }
        } // JSON map
        else if (Map.class.isAssignableFrom(term.getClass()) && (
                Map.class.isAssignableFrom(templ.getRawType()) || templ.getRawType().equals(Object.class) // descend into object
        )) {
            if ((targetType.get() instanceof TypeVariable)) {
                Term<K> tt = (Term<K>) parseTemplateM((Map<String, ?>) term, (T<Map<String, ?>>) targetType, true);
                log.log(DEBUG,  "Making type variable more precise: {0} => {1}", tt.getTypeString(), "Map<String, "+ ((MapTerm) tt).getElementType().getTypeString()+">");
                return tt;
            } else {
                return (Term<K>) parseTemplateM((Map<String, ?>) term, (T<Map<String, ?>>) targetType, false);
            }
        }
        // primitives
        else {
            // raw type
            if (templ.getRawType().isAssignableFrom(term.getClass()) && !String.class.isAssignableFrom(term.getClass())) {
                // same types, return actual object
                TemplateConstant<K> constant = new TemplateConstant<>((K) term, targetType);
//                constant.eval();
                return constant;
            } else if (String.class.isAssignableFrom(term.getClass())) {
                // string template found
                return parseTemplate(((String) term), targetType);
            } else {
                throw new ValidationException("Argument type mismatch! Expected String or " + templ + ", but found " + term.getClass());
            }
        }
    }

    public static <C> Term<C> parseTemplate(@NotNull final String term,
                                            @NotNull final T<C> targetType) {
        log.log(TRACE,"Converting term to template expression: {0}", term);

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

            log.log(TRACE, "Converted term: '{}'", convertedTerm);
            return convertedTerm;
        } catch (Exception e) {
            throw new TemplateException(e, "Bad template syntax: " + e.getMessage());
        }
    }

    public static <K> T<List<K>> listOf(Type elementType) {
        Type tt = TypeResolver.listType(elementType);
        return new T<>(tt){};
    }

    public static <K> T<List<K>> listOf(T<K> elementType) {
        return listOf(elementType.get());
    }

    public static <K, V> T<Map<K, V>> mapOf(T<K> keyType, T<V> elementType) {
        Type tt = TypeResolver.mapType(keyType.get(), elementType.get());
        return new T<>(tt){};
    }

    private static T<?> mapElement(T<Map<String, ?>> typeParameter) {
        return new T<>(((ParameterizedType) typeParameter.get()).getActualTypeArguments()[1]){};
    }

    private static T<?> listElement(T<List<?>> listType) {
        return new T<>(((ParameterizedType) listType.get()).getActualTypeArguments()[0]){};
    }

    // ==============
    // descend into lists and maps helper functions
    // ==============

    @SuppressWarnings({"unchecked", "rawtypes"}) // checked
    private static Term<Map<String, ?>> parseTemplateM(@NotNull final Map<String, ?> term,
                                                       @NotNull final T<Map<String, ?>> targetType, boolean fromTypeVariable) throws ValidationException {
        // create a map with checked values
        T<?> elementType = (((T<?>) targetType).getRawType().equals(Object.class) ? targetType : mapElement(targetType));

        Map<String, Term<?>> resultMap = new HashMap<>();

        for (String k : term.keySet()) {
            if(fromTypeVariable) {
                resultMap.put(k, parseTemplate(term.get(k), new T<>(elementType.get(), targetType.getSuffix()+"MapOf") {}));
            } else {
                resultMap.put(k, parseTemplate(term.get(k), new T<>(elementType.get()) {}));
            }
        }

        return new TemplateMap(resultMap,
                new T<>(elementType.get(), fromTypeVariable ? targetType.getSuffix() + "MapOf" : targetType.getSuffix()) {},
                targetType,
                fromTypeVariable);
    }


    @SuppressWarnings({"unchecked", "rawtypes"}) // checked
    private static ListTerm<?> parseTemplateL(@NotNull final List<?> term,
                                              @NotNull final T<List<?>> listType, boolean fromTypeVariable) throws ValidationException {
        // create a list with converted values
        T<?> elementType = (((T<?>) listType).getRawType().equals(Object.class) ? listType : listElement(listType));

        List<Term<?>> resultList = new ArrayList<>();

        for (Object o : term) {
            if(fromTypeVariable) {
                resultList.add(parseTemplate(o, new T<>(elementType.get(), listType.getSuffix() + "ListOf") {}));
            } else {
                resultList.add(parseTemplate(o, new T<>(elementType.get()) {}));
            }
        }

        return new TemplateList(resultList, listType, new T<>(elementType.get(), fromTypeVariable ? listType.getSuffix() + "ListOf" : listType.getSuffix()){}, fromTypeVariable);
    }


    public static T<?> templateOf(L<?> location) {
        T<Object> t = new T<>(){};
        t.setTerm(parseTemplate("{"+location.getLocation().getRaw()+"}", new T<>(){}));
        return t;
    }

    public static <A> T<A> templateOf(String location) {
        T<A> t = new T<>(){};
        t.setTerm(parseTemplate("{"+location+"}", new T<>(){}));
        return t;
    }

    public static <A> L<A> locationOf(String loc) {
        L<A> t = new L<>(){};
        t.setLocation(new TemplateConstant<>(loc, new T<>(){}));
        return t;

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
