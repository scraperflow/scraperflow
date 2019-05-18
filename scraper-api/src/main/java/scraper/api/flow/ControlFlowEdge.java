package scraper.api.flow;


/**
 * Describes a single edge form one node to another
 *
 * @since 1.0.0
 */
public interface ControlFlowEdge {

    // TODO what is the difference?
    /** Get label of target node */
    String getTarget();
    /** Get label of target node*/
    String getTargetLabel();

    /** Get label of the edge */
    String getLabel();

    /** Indicates if the edge is used multiple times from the origin node */
    boolean isMultiple();
    /** Setter for multiple edges */
    void setMultiple(boolean multiple);
    /** Indicates if the edge dispatches new Flow to the next nodes */
    boolean isDispatched() ;
    /** Setter for dispatched flows */
    void setDispatched(boolean dispatched);
}

