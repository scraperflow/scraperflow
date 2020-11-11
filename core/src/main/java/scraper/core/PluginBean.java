package scraper.core;

import io.github.classgraph.AnnotationInfo;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ScanResult;
import scraper.annotations.NotNull;
import scraper.annotations.node.NodePlugin;
import scraper.api.exceptions.ValidationException;
import scraper.api.node.type.Node;
import scraper.utils.ClassUtil;

import java.util.LinkedList;
import java.util.List;

import static java.lang.System.Logger.Level.DEBUG;
import static java.lang.System.Logger.Level.INFO;

public class PluginBean {
    private @NotNull static final System.Logger log = System.getLogger("NodeDiscovery");
    private @NotNull final List<AbstractMetadata> plugins = new LinkedList<>();

    public PluginBean() {
        String pkg = "scraper";
        String routeAnnotation = NodePlugin.class.getName();
        try (ScanResult scanResult =
                     new ClassGraph()
                             .enableAllInfo()
                             .acceptPackages(pkg)
                             .scan()) {
            for (ClassInfo routeClassInfo : scanResult.getClassesWithAnnotation(routeAnnotation)) {
                if (routeClassInfo.isAbstract()) continue;

                AnnotationInfo routeAnnotationInfo = routeClassInfo.getAnnotationInfo(routeAnnotation);

                String metadataName = routeClassInfo.getSimpleName();
                String metadataVersion = (String) routeAnnotationInfo.getParameterValues().getValue("value");
                Boolean metadataDeprecated = (Boolean) routeAnnotationInfo.getParameterValues().getValue("deprecated");
                String metadataCategory = ClassUtil.extractCategoryOfNode(routeClassInfo.getName());
                String className = routeClassInfo.getName();

                AbstractMetadata metadata = new AbstractMetadata(metadataName, metadataVersion, metadataCategory, metadataDeprecated) {
                    @NotNull @Override
                    public Node getNode() throws ValidationException {
                        try {
                            return (Node) Class.forName(className).getDeclaredConstructor().newInstance();
                        } catch (Exception e) {
                            throw new ValidationException(e, "Could not instantiate node");
                        }
                    }
                };

                plugins.add(metadata);
            }
        }

        printDiscovery();
    }

    private void printDiscovery() {
        // construct human readable plugins string
        StringBuilder sb = new StringBuilder("[");
        for (AbstractMetadata metadata : getPlugins()) {
            if(metadata.isDeprecated()) continue;
            if(sb.length() != 1) sb.append(", ");
            sb.append(metadata.getName()).append(" [").append(metadata.getVersion()).append("]");
        }
        log.log(DEBUG, "Discovered {0} nodes, {1}", plugins.size(), sb.append("]").toString());
    }

    public @NotNull List<AbstractMetadata> getPlugins() {
        return this.plugins;
    }
}
