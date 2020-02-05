package scraper.util;

import com.google.common.reflect.TypeToken;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.slf4j.Logger;
import scraper.annotations.NotNull;
import scraper.api.exceptions.TemplateException;
import scraper.api.flow.impl.IdentityFlowMap;
import scraper.api.reflect.T;
import scraper.core.exp.TemplateExpressionVisitor;
import scraper.core.exp.TemplateLexer;
import scraper.core.exp.TemplateParser;
import scraper.core.template.TemplateExpression;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;


public final class TemplateUtil {

    private static final Logger log = org.slf4j.LoggerFactory.getLogger(TemplateUtil.class);


    public static <C> TemplateExpression<C> parseTemplate(@NotNull final String term,
                                                          @NotNull final TypeToken<C> targetType) {
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
            throw new TemplateException(e, "Bad template syntax");
        }
    }

    public static <C> TemplateExpression<C> parseTemplate(@NotNull final String term) {
        log.trace("Converting term to template expression: '{}'", term);
        TypeToken<C> targetType = new TypeToken<>(){};
        return parseTemplate(term, targetType);
    }

    public static <Target> void accept(T<?> t, Class<Target> addressClass, Consumer<Target> o) {
        Object parsedJson = t.getParsedJson();
        if(parsedJson != null) descend(parsedJson, addressClass, o);
    }

    @SuppressWarnings("unchecked")
    private static <Target> void descend(Object parsedJson, Class<Target> addressClass, Consumer<Target> o) {
        if(parsedJson instanceof List) {
            ((List) parsedJson).forEach(e -> descend(e, addressClass, o));
        } else if(parsedJson instanceof Map) {
            ((Map) parsedJson).forEach((k,v) -> descend(v, addressClass, o));
        } else {
            if(parsedJson instanceof TemplateExpression) {
                if (((TemplateExpression) parsedJson).getType() == addressClass) {
                    Target t = (Target) ((TemplateExpression) parsedJson).eval(new IdentityFlowMap());
                    o.accept(t);
                }
            }
            if(parsedJson.getClass() == addressClass) {
                    Target t = (Target) parsedJson;
                    o.accept(t);
            }
        }
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
