package scraper.util;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;
import scraper.api.di.DIContainer;
import scraper.api.di.impl.DIContainerImpl;
import scraper.api.plugin.Addon;
import scraper.api.plugin.Hook;
import scraper.api.plugin.NodeHook;
import scraper.api.plugin.ScrapeSpecificationParser;
import scraper.api.service.impl.ExecutorsServiceImpl;
import scraper.api.service.impl.FileServiceImpl;
import scraper.api.service.impl.HttpServiceImpl;
import scraper.api.service.impl.ProxyReservationImpl;
import scraper.core.JobFactory;
import scraper.core.PluginBean;

import java.util.Collection;
import java.util.List;

import static java.lang.System.Logger.Level.DEBUG;
import static java.lang.System.Logger.Level.ERROR;


public class DependencyInjectionUtil {
    private static final System.Logger log = System.getLogger("DependencyInjection");

    public static DIContainer getDIContainer() {
        DIContainer diContainer = new DIContainerImpl();

        diContainer.addComponent(PluginBean.class);
        diContainer.addComponent(ExecutorsServiceImpl.class);
        diContainer.addComponent(HttpServiceImpl.class);
        diContainer.addComponent(ProxyReservationImpl.class);
        diContainer.addComponent(FileServiceImpl.class);

        diContainer.addComponent(JobFactory.class);

        try (ScanResult scanResult = new ClassGraph().enableClassInfo()
                             .acceptPackages("scraper.plugins","scraper.addons", "scraper.hooks")
                             .scan()) {
            List.of(
                    scanResult.getClassesImplementing(Addon.class.getName()),
                    scanResult.getClassesImplementing(Hook.class.getName()),
                    scanResult.getClassesImplementing(ScrapeSpecificationParser.class.getName()),
                    scanResult.getClassesImplementing(NodeHook.class.getName())
            ).stream()
                    .filter(o -> !o.isEmpty())
                    .flatMap(Collection::stream)
                    .distinct()
                    .forEach(plugin -> {
                        try {
                            Class<?> addon = Class.forName(plugin.getName());
                            diContainer.addComponent(addon, true);
                            log.log(DEBUG, "Found plugin {0}", addon.getSimpleName());
                        } catch (ClassNotFoundException e) {
                            log.log(ERROR,   "Scan found plugin, but class not found", e);
                        }
                    });
        }

        return diContainer;
    }
}
