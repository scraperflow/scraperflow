package scraper.api.flow;

/**
 * Manages the current state of the flo
 *
 * @since 1.0.0
 */
public interface FlowState {
    /** Index of the last node that accepted this flow map */
    int getStageIndex();

    /** Label of the last node that accepted this flow map */
    String getLabel();

    /** Name of the job this flow state belongs to */
    String getJobName();
}
