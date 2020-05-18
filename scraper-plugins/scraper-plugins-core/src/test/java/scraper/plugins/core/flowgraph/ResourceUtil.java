package scraper.plugins.core.flowgraph;

import org.junit.Assert;
import scraper.api.di.DIContainer;
import scraper.api.specification.ScrapeInstance;
import scraper.api.specification.ScrapeSpecification;
import scraper.core.JobFactory;
import scraper.plugins.core.flowgraph.helper.InstanceHelper;
import scraper.util.DependencyInjectionUtil;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public class ResourceUtil {

    public static ScrapeInstance read(String path) throws Exception {
        URL resource = ResourceUtil.class.getResource("/scraper/plugins/core/flowgraph/"+path);
        File scrapeFile = new File(resource.toURI());

        ScrapeSpecification spec = InstanceHelper.getInstance(scrapeFile);
        DIContainer dibean = DependencyInjectionUtil.getDIContainer();

        return dibean.get(JobFactory.class).convertScrapeJob(spec);
    }

    public static <T> T opt(Supplier<Optional<T>> o) {
        Optional<T> result = o.get();
        Assert.assertTrue(result.isPresent());
        return result.get();
    }
}

