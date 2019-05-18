package scraper.api.exceptions;

import scraper.api.flow.FlowState;
import scraper.api.flow.FlowMap;

/**
 * Exception thrown during bad template evaluation with additional information where the template evaluation failed
 * in the flow.
 *
 * @since 1.0.0
 */
public class TemplateException extends NodeException {
    public TemplateException(FlowMap args, String message) { super(generatePrefix(args)+message); }
    public TemplateException(FlowMap args, Exception cause, String message) { super(cause, generatePrefix(args)+message); }

    private static String generatePrefix(FlowMap args) {
        FlowState info = args.getFlowState();
        try {
            return "["+info.getJobName()+": "+(info.getLabel()==null?"":info.getLabel())+"@"+info.getStageIndex()+"] ";
        } catch (Exception e) {
            return "[?] ";
        }
    }
}