package scraper.util;

import com.google.common.reflect.TypeToken;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.slf4j.Logger;
import scraper.api.exceptions.TemplateException;
import scraper.core.exp.TemplateExpressionVisitor;
import scraper.core.exp.TemplateLexer;
import scraper.core.exp.TemplateParser;
import scraper.core.template.TemplateExpression;


public final class TemplateUtil {

    private static final Logger log = org.slf4j.LoggerFactory.getLogger(TemplateUtil.class);


    public static <C> TemplateExpression<C> parseTemplate(String term, TypeToken<C> targetType) {
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
            throw new TemplateException("Bad template syntax", e);
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
