package scraper.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.slf4j.Logger;
import scraper.api.specification.ScrapeSpecification;
import scraper.api.specification.impl.ScrapeSpecificationImpl;
import scraper.utils.FileUtil;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Class providing parsing of the Scraper YML V1 format.
 *
 * @since 1.0.0
 * @version 1
 */
final class YmlParse {

    private static final Logger log = org.slf4j.LoggerFactory.getLogger(YmlParse.class);
    private static ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

    @SuppressWarnings("unchecked") // exception catches type errors
    static List<ScrapeSpecification> parseYmlFile(String ym, Set<String> canditatePaths) throws IOException {
        return null;
//        log.info("Processing yml {}", ym);
//        List<ScrapeSpecification> jobDefinitions = new LinkedList<>();
//
//        // get first specified yml file among candidate paths
//        File ymlPath = FileUtil.getFirstExisting(ym, canditatePaths);
//        Map args = mapper.readValue(ymlPath, Map.class);
//
//        // ???
//        List use = (args.get("use") == null ? new ArrayList<>() : (List) args.get("use"));
//        use.forEach(p -> log.debug("Classpath: {}", p));
//
//        // parse each specified job
//        Map jobs = (Map) args.get("jobs");
//
//        jobs.forEach((job, spec) -> {
//            ScrapeSpecificationImpl.JobDefinitionBuilder b = ScrapeSpecificationImpl.builder();
//
//            // ???
//            use.forEach(p -> b.path((String) p));
//
//            // set base path as the parent of the .yml file
//            b.basePath((ymlPath.getParent() == null
//                    ? System.getProperty("user.dir")
//                    : ymlPath.getParent()));
//
//            String scrapeArg = (String) ((Map) spec).get("scrape-file");
//            List argumentArgs = (List) ((Map) spec).getOrDefault("arguments", new ArrayList());
//            List fragmentArgs = (List) ((Map) spec).getOrDefault("fragments", new ArrayList());
//
//            if(scrapeArg != null) {
//                b.scrapeFile(scrapeArg);
//            } else {
//                throw new IllegalStateException("Missing scrape file definition for job "+ job);
//            }
//
//            if (argumentArgs != null) {
//                argumentArgs.forEach(arg -> b.argumentFile((String) arg));
//            }
//
//            if (fragmentArgs != null) {
//                fragmentArgs.forEach(arg -> b.fragmentFolder((String) arg));
//            }
//
//            b.name((String) job);
//
//            b.packagedFile((String) ((Map) spec).get("packaged-file"));
//
//            canditatePaths.forEach(b::path);
//
//            jobDefinitions.add(b.build());
//        });
//
//        return jobDefinitions;
    }

}
