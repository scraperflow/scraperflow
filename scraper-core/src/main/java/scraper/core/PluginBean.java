package scraper.core;

import io.github.classgraph.AnnotationInfo;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ScanResult;
import org.slf4j.Logger;
import org.springframework.plugin.core.PluginRegistry;
import org.springframework.plugin.metadata.PluginMetadata;
import scraper.annotations.NotNull;
import scraper.annotations.node.NodePlugin;
import scraper.api.exceptions.ValidationException;
import scraper.api.node.type.Node;
import scraper.utils.ClassUtil;

import java.util.ArrayList;
import java.util.List;

public class PluginBean {
    private @NotNull static final Logger log = org.slf4j.LoggerFactory.getLogger("NodeDiscovery");
    private @NotNull final PluginRegistry<? extends AbstractMetadata, PluginMetadata> plugins;

    public PluginBean() {
        List<AbstractMetadata> nodePlugins = new ArrayList<>();

        String pkg = "scraper";
        String routeAnnotation = NodePlugin.class.getName();
        try (ScanResult scanResult =
                     new ClassGraph()
                             .enableAllInfo()
                             .whitelistPackages(pkg)
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
                    @NotNull
                    @Override
                    public Node getNode() throws ValidationException {
                        try {
                            return (Node) Class.forName(className).getDeclaredConstructor().newInstance();
                        } catch (Exception e) {
                            throw new ValidationException(e, "Could not instantiate node");
                        }
                    }
                };

                nodePlugins.add(metadata);
            }
        }

        plugins = PluginRegistry.of(nodePlugins);

        printDiscovery();
    }

    private void printDiscovery() {
        // construct human readable plugins string
        StringBuilder sb = new StringBuilder("[");
        for (AbstractMetadata metadata : getPlugins()) {
            if(metadata.isDeprecated()) continue;
            if(sb.length() != 1) sb.append(", ");
            sb.append(metadata.getMetadata().getName()).append(" [").append(metadata.getMetadata().getVersion()).append("]");
        }
        log.info("Discovered {} nodes, {}", plugins.getPlugins().size(), sb.append("]").toString());
    }

    @NotNull PluginRegistry<? extends AbstractMetadata, PluginMetadata> getPlugins() {
        return this.plugins;
    }
}
