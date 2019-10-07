package scraper.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.slf4j.Logger;
import scraper.annotations.ArgsCommand;
import scraper.api.specification.ScrapeSpecification;
import scraper.api.specification.impl.ScrapeSpecificationImpl;
import scraper.api.exceptions.ValidationException;
import scraper.utils.FileUtil;
import scraper.utils.StringUtil;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;


@ArgsCommand(
        value = "<path ends with .ndep>",
        doc = "Specifies a .ndep file which is used for node versioning. By default, the .scrape file name is used implicitly to look for a node versioning file.",
        example = "scraper app.scrape dependencies.ndep"
)
@ArgsCommand(
        value = "<path ends with .yml>",
        doc = "Specify a scrape job grouping .yml file.",
        example = "scraper app.yml"
)
@ArgsCommand(
        value = "<path ends with .scrape>",
        doc = "Specify a scrape job specification to run.",
        example = "scraper app.scrape"
)
@ArgsCommand(
        value = "<path ends with .args>",
        doc = "Specify a scrape job argument file to include.",
        example = "scraper app.scrape config_db.args config_runtime.args"
)
public final class JobUtil {

    // JSON and YML mapper
    private static final ObjectMapper jsonMapper = new ObjectMapper();
    private static final ObjectMapper ymlMapper = new ObjectMapper(new YAMLFactory());

    private static final Logger log = org.slf4j.LoggerFactory.getLogger(JobUtil.class);

    public static List<ScrapeSpecification> parseJobs(final String[] args, final Set<String> canditatePaths)
            throws IOException, ValidationException {
        List<ScrapeSpecification> jobs = new LinkedList<>();

        List<String> ymls = StringUtil.getAllArguments(args,".yml");

        // TODO implement handle more than one yml
        if (ymls.size() > 1) {
            log.warn("More than 1 .yml file! {}", ymls);
            log.error("Processing more than 1 yml file not implemented");
            throw new ValidationException("More than 1 yml file not allowed");
        }


        // jobs defined via yml file
        if(ymls.size() == 1) {
            jobs = YmlParse.parseYmlFile(ymls.get(0), canditatePaths);
        } // single job defined via application arguments
        else {
            ScrapeSpecification jobDefinition = parseArgsJob(args,canditatePaths);
            if(jobDefinition != null) {
                jobs.add(jobDefinition);
            }
        }

        log.info("Parsed {} scrape job definitions", jobs.size());
        return jobs;
    }


    public static ScrapeSpecification parseArgsJob(String[] args, Set<String> canditatePaths) throws IOException, ValidationException {
        return parseSingleFormat(args, ".scrape", canditatePaths);
    }

    public static ScrapeSpecification parseTestJob(String[] args) throws IOException, ValidationException {
        return parseSingleFormat(args, ".stest", Collections.emptySet());
    }

    private static ScrapeSpecification parseSingleFormat(String[] args, String format, Set<String> candidatePaths) throws IOException, ValidationException {
        String scrapePath;
        String ndepPath;
        List<String> argsPaths = new LinkedList<>();
        List<String> fragmentFolders = new LinkedList<>();

        {
            // check paths for exactly one scrape file
            List<String> paths = StringUtil.getAllArguments(args, format);

            if(paths.size() == 1) { // priority over implied paths
                scrapePath = paths.get(0);
            } else if (paths.size() > 1) {
                log.error("Expected exactly one {} file! Specified: {}", format, paths);
                throw new ValidationException("More than one "+format+" file specified as arguments: "+paths);
            } else { // try implied .scrape files
                List<String> impliedPaths = new LinkedList<>();
                Files.list(Paths.get(System.getProperty("user.dir")))
                        .filter(path -> path.toString().endsWith(".scrape"))
                        .forEach(p -> impliedPaths.add(p.toString()));
                if (impliedPaths.size() != 1) {
                    log.warn("None or too many scrape files in current working folder found. Specify the scrape file directly. ");
                    return null;
                }
                scrapePath = impliedPaths.get(0);
            }
        }

        {
            // check dependency file
            Set<String> paths = new HashSet<>(StringUtil.getAllArguments(args,".ndep"));
            Files.list(Paths.get(System.getProperty("user.dir")))
                    .filter(path -> path.endsWith(".ndep"))
                    .forEach(p -> paths.add(p.toString()));
            if (paths.size() > 1) {
                log.error("Expected exactly one or no .ndep files! Found: {}", paths);
                throw new ValidationException("More than 1 .ndep files found");
            }
            if (paths.size() == 1) {
                ndepPath = paths.iterator().next();
            } else {
                log.info("Using implied node dependency location");
                ndepPath = FileUtil.replaceFileExtension(scrapePath, "ndep");
            }
        }

        {
            // check argument files
            List<String> paths = StringUtil.getAllArguments(args,".args");
            Files.list(Paths.get(System.getProperty("user.dir")))
                    .filter(path -> path.toString().endsWith(".args"))
                    .filter(path -> new File(path.toString()).isFile())
                    .forEach(p -> paths.add(p.toString()));
            argsPaths.addAll(paths);
        }

        {
            // check fragment folders
            List<String> paths = StringUtil.getAllArguments(args,"fragments:");
            paths.add(System.getProperty("user.dir"));
            fragmentFolders.addAll(paths);
        }

        // set base path as current working directory
        String base = (Paths.get("").getParent() == null
                ? System.getProperty("user.dir")
                : Paths.get("").getParent().toString());

        // search for file
        File scrapeFile = FileUtil.getFirstExisting(scrapePath, candidatePaths);
        if(!scrapeFile.exists() || !scrapeFile.isFile()) throw new IOException("scrape file is not a file or does not exist");

        // try to parse JSON and YML
        ScrapeSpecification spec;
        try {
            jsonMapper.readValue(scrapeFile, ScrapeSpecificationImpl.class);
            System.out.println("OK");
        } catch (Exception e) {
            e.printStackTrace();
            ymlMapper.readValue(scrapeFile, ScrapeSpecificationImpl.class);
            System.out.println("YML");
        }


        throw new IllegalStateException("finished");
//        return ScrapeSpecificationImpl.builder()
//                .paths(candidatePaths)
//                .basePath(base)
//                .scrapeFile(scrapePath)
//                .nodeDependencyFile(ndepPath)
//                .argumentFiles(argsPaths)
//                .fragmentFolders(fragmentFolders)
//                .build();
    }
}
