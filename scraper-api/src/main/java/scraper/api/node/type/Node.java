package scraper.api.node.type;

import scraper.annotations.NotNull;
import scraper.api.exceptions.NodeException;
import scraper.api.exceptions.TemplateException;
import scraper.api.exceptions.ValidationException;
import scraper.api.flow.FlowMap;
import scraper.api.node.container.NodeContainer;
import scraper.api.node.NodeHook;
import scraper.api.node.container.NodeLogLevel;
import scraper.api.specification.ScrapeInstance;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * Objects which implement this interface can consume and modify {@link FlowMap}s.
 *
 * @since 1.0.0
 */
@FunctionalInterface
public interface Node {
    /**
     * Accept a {@link FlowMap}.
     * The FlowMap can be modified in the process.
     * Side-effects are possible.
     *
     * The default implementation has hooks before and hooks after processing the incoming flow map.
     *
     * @param o The FlowMap to modifiy/use in the function
     * @throws NodeException if there is a processing error during the function call
     */
    default @NotNull FlowMap accept(@NotNull final NodeContainer<? extends Node> n, @NotNull final FlowMap o) throws NodeException {
        try {
            for (NodeHook hook : n.beforeHooks()) { hook.accept(o); }
            this.sendDebugData(n, o);
            FlowMap fm = process(n, o);
            for (NodeHook hook : n.afterHooks()) { hook.accept(o); }
            return fm;
        } catch (TemplateException e) {
            n.log(NodeLogLevel.ERROR, "Template type error for {}: {}", n.getAddress(), e.getMessage());
            throw new IllegalStateException(e);
        }
    }

    default void sendDebugData(NodeContainer<? extends Node> n, FlowMap o) {
        DebugData d = new DebugData(n, o);

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8500"))
                .POST(HttpRequest.BodyPublishers.ofString(d.toString()))
                .build();

        try {
            HttpResponse<String> response = client.send(request,
                    HttpResponse.BodyHandlers.ofString());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    /** The process function which encapsulates the business logic of a node */
    @NotNull FlowMap process(@NotNull NodeContainer<? extends Node> n, @NotNull FlowMap o) throws NodeException;

    /** Initialization of a node, by default no-op */
    default void init(@NotNull NodeContainer<? extends Node> n, @NotNull ScrapeInstance instance) throws ValidationException {}
}

class DebugData {
    private NodeContainer<? extends Node> n;
    private FlowMap o;

    public DebugData(NodeContainer<? extends Node> n, FlowMap o) {
        this.n = n;
        this.o = o;
    }

    @Override
    public String toString() {
//        return super.toString();
        ObjectMapper mapper = new ObjectMapper();

        // Java object to JSON string
        String jsonString = mapper.writeValueAsString(object);

        return jsonString;
    }
}