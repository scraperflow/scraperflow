package scraper.api.exceptions;

import scraper.api.flow.FlowState;
import scraper.api.flow.FlowMap;

/**
 * Exception thrown during bad template evaluation with additional information where the template evaluation failed
 * in the flow.
 *
 * @since 1.0.0
 */
public class TemplateException extends RuntimeException {
    public TemplateException(String message) { super(message); }
    public TemplateException(FlowMap args, String message) { super(generatePrefix(args)+message); }
    public TemplateException(FlowMap args, Exception cause, String message) { super(generatePrefix(args)+message, cause); }

    private static String generatePrefix(FlowMap args) {
        try {
            FlowState info = args.getFlowState();
            return "["+info.getJobName()+": "+(info.getLabel()==null?"":info.getLabel())+"@"+info.getStageIndex()+"] ";
        } catch (Exception e) {
            return "[?] ";
        }
    }
}