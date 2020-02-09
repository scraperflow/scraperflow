package scraper.util;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ClassInfoList;
import io.github.classgraph.ScanResult;
import org.slf4j.Logger;
import scraper.api.di.DIContainer;
import scraper.api.di.impl.DIContainerImpl;
import scraper.api.node.NodeHook;
import scraper.api.plugin.Addon;
import scraper.api.plugin.Hook;
import scraper.api.plugin.PreHook;
import scraper.api.service.impl.*;
import scraper.core.JobFactory;
import scraper.core.PluginBean;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class DependencyInjectionUtil {
    private static final Logger log = org.slf4j.LoggerFactory.getLogger("DependencyInjection");

    public static DIContainer getDIContainer() {
        DIContainer diContainer = new DIContainerImpl();

        diContainer.addComponent(PluginBean.class);
        diContainer.addComponent(ExecutorsServiceImpl.class);
        diContainer.addComponent(HttpServiceImpl.class);
        diContainer.addComponent(ProxyReservationImpl.class);
        diContainer.addComponent(FileServiceImpl.class);

        diContainer.addComponent(JobFactory.class);

        try (ScanResult scanResult = new ClassGraph().enableClassInfo()
                             .whitelistPackages("scraper.plugins","scraper.addons", "scraper.hooks")
                             .scan()) {
            List.of(
                    scanResult.getClassesImplementing(Addon.class.getName()),
                    scanResult.getClassesImplementing(Hook.class.getName()),
                    scanResult.getClassesImplementing(PreHook.class.getName()),
                    scanResult.getClassesImplementing(NodeHook.class.getName())
            ).stream()
                    .filter(o -> !o.isEmpty())
                    .flatMap(Collection::stream)
                    .distinct()
                    .forEach(plugin -> {
                        try {
                            Class<?> addon = Class.forName(plugin.getName());
                            diContainer.addComponent(addon, true);
                            log.debug("Found plugin '{}'", addon.getSimpleName());
                        } catch (ClassNotFoundException e) {
                            log.error("Scan found plugin, but class not found", e);
                        }
                    });
        }

        return diContainer;
    }
}
