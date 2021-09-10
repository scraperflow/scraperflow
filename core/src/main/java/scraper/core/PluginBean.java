package scraper.core;

import scraper.annotations.NodePlugin;
import scraper.annotations.NotNull;
import scraper.api.Node;
import scraper.api.ValidationException;
import scraper.utils.ClassUtil;

import java.util.LinkedList;
import java.util.List;
import java.util.ServiceLoader.Provider;

import static java.lang.System.Logger.Level.DEBUG;
import static java.util.ServiceLoader.load;

public class PluginBean {
    private @NotNull static final System.Logger log = System.getLogger("NodeDiscovery");
    private @NotNull final List<AbstractMetadata> plugins = new LinkedList<>();

    public PluginBean() {
        String pkg = "scraper";

        load(Node.class).stream()
                .map(Provider::get)
                .filter(n -> n.getClass().getName().startsWith(pkg))
                .forEach(node -> {

                    NodePlugin nodeInfo = node.getClass().getAnnotation(NodePlugin.class);

                    String metadataName = node.getClass().getSimpleName();
                    String metadataVersion = nodeInfo.value();
                    Boolean metadataDeprecated = nodeInfo.deprecated();
                    String metadataCategory = ClassUtil.extractCategoryOfNode(node.getClass().getName());
                    String className = node.getClass().getName();

                    AbstractMetadata metadata = new AbstractMetadata(metadataName, metadataVersion, metadataCategory, metadataDeprecated) {
                        @NotNull @Override
                        public Node getNode() throws ValidationException {
                            try {
                                return (Node) Class.forName(className).getDeclaredConstructor().newInstance();
                            } catch (Exception e) {
                                throw new ValidationException(e, "Could not instantiate node: " + e.getMessage());
                            }
                        }
                    };

                    plugins.add(metadata);
                });

        String nodes = nodeDiscovery();
        log.log(DEBUG, "Discovered {0} nodes, {1}", plugins.size(), nodes);
    }

    public String nodeDiscovery() {
        // construct human readable plugins string
        StringBuilder sb = new StringBuilder("[");
        for (AbstractMetadata metadata : getPlugins()) {
            if(metadata.isDeprecated()) continue;
            if(sb.length() != 1) sb.append(", ");
            sb.append(metadata.getName()).append(" [").append(metadata.getVersion()).append("]");
        }
        return sb.append("]").toString();
    }

    public @NotNull List<AbstractMetadata> getPlugins() {
        return this.plugins;
    }
}
