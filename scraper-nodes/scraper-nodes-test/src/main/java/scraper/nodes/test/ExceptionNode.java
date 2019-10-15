package scraper.nodes.test;



import scraper.annotations.node.FlowKey;
import scraper.annotations.node.NodePlugin;
import scraper.api.flow.FlowMap;
import scraper.api.flow.impl.FlowMapImpl;
import scraper.core.AbstractNode;
import scraper.api.exceptions.NodeException;

@NodePlugin(deprecated = true)
public class ExceptionNode extends AbstractNode {

    private @FlowKey(mandatory = true) String exception;

    @Override
    public FlowMap process(FlowMap o) throws NodeException {
        switch (exception) {
            case "NODE": {
                throw new NodeException("Dummy Exception");
            }
            default:
                throw new RuntimeException("default runtime exception");
        }
    }
}
