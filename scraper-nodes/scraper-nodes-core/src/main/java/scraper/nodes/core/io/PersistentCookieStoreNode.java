package scraper.nodes.core.io;

import scraper.annotations.NotNull;
import scraper.annotations.node.NodePlugin;
import scraper.api.exceptions.NodeException;
import scraper.api.flow.FlowMap;
import scraper.api.node.container.FunctionalNodeContainer;
import scraper.api.node.type.FunctionalNode;

import java.io.IOException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.URI;
import java.util.List;
import java.util.Map;

/**
 * Sets a persistent system-wide cookie store handler
 * @author Albert Schimpf
 */
@NodePlugin("0.0.1")
public final class PersistentCookieStoreNode implements FunctionalNode {

    @Override
    public void modify(@NotNull FunctionalNodeContainer n, @NotNull FlowMap o) throws NodeException {
        System.out.println("Setting persistent cookie store");

        if (CookieManager.getDefault() != null) {
            System.out.println("Overwriting default cookie manager");
        }

//        CookieManager.setDefault(new PCM());
    }


    private static class PCM extends CookieHandler {

        private final CookieManager store;

        PCM() {
            System.out.println("Initializing store");
            CookieManager man = new CookieManager();
            this.store = man;
        }

        @Override
        public Map<String, List<String>> get(URI uri, Map<String, List<String>> requestHeaders) throws IOException {
            System.out.println("GET");
            return Map.of();
        }

        @Override
        public void put(URI uri, Map<String, List<String>> responseHeaders) throws IOException {
            System.out.println("PUT");

        }
    }
}
