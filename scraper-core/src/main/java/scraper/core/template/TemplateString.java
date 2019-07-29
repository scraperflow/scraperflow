package scraper.core.template;

import com.google.common.reflect.TypeToken;
import org.slf4j.Logger;
import scraper.api.exceptions.TemplateException;
import scraper.api.exceptions.ValidationException;
import scraper.api.flow.FlowMap;

import java.util.Collection;
import java.util.Set;

import static scraper.api.converter.StringToClassConverter.convert;

public class TemplateString<T> extends TemplateExpression<T>{
    private static final Logger log = org.slf4j.LoggerFactory.getLogger(TemplateString.class);

    private StringBuilder stringContent = new StringBuilder();

    public TemplateString(TypeToken<T> targetType) {
        super(targetType);
    }

    public T eval(final FlowMap o) {
        try {
            return (T) convert(stringContent.toString(), targetType.getRawType());
        } catch (ValidationException e) {
            throw new TemplateException("Could not convert string content to " + targetType);
        }
    }

    public String toString() {
        return stringContent.toString();
    }

    public Collection<String> getKeysInTemplate(FlowMap o ) {
        return Set.of();
    }

    public void addContent(Object content) {
        stringContent.append(content.toString());
    }
}
