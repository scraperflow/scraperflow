package scraper.nodes.test;

import scraper.annotations.node.Argument;

import scraper.annotations.node.FlowKey;
import scraper.annotations.node.NodePlugin;
import scraper.api.flow.FlowMap;
import scraper.core.AbstractNode;
import scraper.core.NodeLogLevel;
import scraper.core.Template;
import scraper.api.exceptions.NodeException;
import org.eclipse.jetty.server.Server;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static scraper.core.NodeLogLevel.ERROR;
import static scraper.core.NodeLogLevel.INFO;

@NodePlugin(deprecated = true)
public class SocketSimulationNode extends AbstractNode {

    /** Start or shutdown server */
    @FlowKey(mandatory = true)
    private final Template<Action> action = new Template<>(){};
    private enum Action { ON, OFF, FREEZE, UNFREEZE }

    @FlowKey(mandatory = true) @Argument
    private Integer port;

    private AtomicBoolean freeze = new AtomicBoolean(false);
    private AtomicReference<Server> server = new AtomicReference<>(null);

    @Override
    public FlowMap process(FlowMap o) throws NodeException {
        Action val = action.eval(o);

        switch (val) {
            case ON:
                start();
                break;
            case OFF:
                log(INFO, "Stopping server at {}", port);
                shutdown();
                break;
            case FREEZE:
                log(INFO, "Freezing server");
                freeze();
                break;
            case UNFREEZE:
                log(INFO, "Unfreeze server");
                unfreeze();
                break;
        }

        return forward(o);
    }

    private void unfreeze() {
        freeze.set(false);
    }

    private void freeze() {
        freeze.set(true);
    }

    private void shutdown() throws NodeException {
        if(freeze.get()) return;

        try {
            Server val = server.getAndSet(null);
            if(val != null) val.stop();
        } catch (Exception e) {
            throw new NodeException("Could not stop jetty server: "+e.getMessage());
        }
    }

    public void start() throws NodeException {
        if(freeze.get()) return;

        if(server.get() != null) {
            log(NodeLogLevel.WARN,"Server already started");
            return;
        }
        log(INFO, "Starting server at {}", port);

        Server server = new Server(port);
        server.setStopAtShutdown(true);

        try {
            server.start();
        } catch (Exception e) {
            log(ERROR,"Jetty server failed to start: {}", e.getMessage());
            throw new NodeException(e.getMessage());
        }
        this.server.set(server);
    }
}
