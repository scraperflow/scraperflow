package scraper.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.slf4j.Logger;
import scraper.annotations.ArgsCommand;
import scraper.api.node.NodeAddress;
import scraper.api.specification.ScrapeSpecification;
import scraper.api.specification.impl.NodeAddressDeserializer;
import scraper.api.specification.impl.ScrapeSpecificationImpl;
import scraper.api.exceptions.ValidationException;
import scraper.utils.FileUtil;
import scraper.utils.StringUtil;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@ArgsCommand(
        value = "<path ends with .ndep>",
        doc = "Specifies a .ndep file which is used for node versioning. By default, the job file name is used implicitly to look for a node versioning file.",
        example = "scraper app.jf dependencies.ndep"
)
@ArgsCommand(
        value = "<path ends with .yml>",
        doc = "Specify a scrape job grouping .yml file.",
        example = "scraper app.yml"
)
@ArgsCommand(
        value = "<path ends with .jf>",
        doc = "Specify a scrape job specification in JSON to run.",
        example = "scraper app.jf"
)
@ArgsCommand(
        value = "<path ends with .yf>",
        doc = "Specify a scrape job specification in YML to run.",
        example = "scraper app.yf"
)
@ArgsCommand(
        value = "<path ends with .args>",
        doc = "Specify a scrape job argument file to include.",
        example = "scraper app.jf config_db.args config_runtime.args"
)
public final class JobUtil {

    // JSON and YML mapper
    private static final ObjectMapper jsonMapper = new ObjectMapper();
    private static final ObjectMapper ymlMapper = new ObjectMapper(new YAMLFactory());
    static {
        SimpleModule module = new SimpleModule();
        module.addDeserializer(NodeAddress.class, new NodeAddressDeserializer());
        ymlMapper.registerModule(module);
        jsonMapper.registerModule(module);
    }

    private static final Logger log = org.slf4j.LoggerFactory.getLogger(JobUtil.class);

    public static List<ScrapeSpecification> parseJobs(final String[] args, final Set<String> canditatePaths)
            throws IOException, ValidationException {
        List<ScrapeSpecification> jobs = new LinkedList<>();


        jobs.add(parseSingleSpecification(args, canditatePaths));
        log.info("Parsed {} scrape job definitions", jobs.size());

        return jobs;
    }


    private static ScrapeSpecification parseSingleSpecification(String[] args, Set<String> candidatePaths) throws IOException, ValidationException {
        String scrapePath;
        // TODO implement command line parameters (update file spec after creation)
        String ndepPath;
        List<String> argsPaths = new LinkedList<>();
        List<String> fragmentFolders = new LinkedList<>();

        List<String> specs = Stream.concat(
                StringUtil.getAllArguments(args,".yf").stream(),
                StringUtil.getAllArguments(args,".jf").stream())
                .collect(Collectors.toList());

        if (specs.size() > 1) {
            log.warn("More than 1 specification file! ({}) Use a group specification to start multiple jobs in one JVM.", specs.size());
            throw new ValidationException("Multiple single specifications not allowed, use a group specification instead");
        }

        if(specs.size() == 1) {
            scrapePath = specs.get(0);
        } else {
            List<String> impliedPaths = new LinkedList<>();
            Stream.concat(
                    Files.list(Paths.get(System.getProperty("user.dir")))
                            .filter(path -> path.toString().endsWith(".yf")),
                    Files.list(Paths.get(System.getProperty("user.dir")))
                            .filter(path -> path.toString().endsWith(".jf"))
            )
                    .forEach(p -> impliedPaths.add(p.toString()));

            if (impliedPaths.size() != 1) {
                log.warn("None or too many scrape files (.yf, .jf) in current working folder found. Specify the scrape file directly. ");
                throw new ValidationException("None or too many scrape file in current working folder: " + impliedPaths.size());
            }
            scrapePath = impliedPaths.get(0);
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
                log.debug("Using implied node dependency location");
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

        // search for file
        File scrapeFile = FileUtil.getFirstExisting(scrapePath, candidatePaths);
        if(!scrapeFile.exists() || !scrapeFile.isFile()) throw new IOException("scrape file is not a file or does not exist");

        // try to parse JSON and YML
        ScrapeSpecificationImpl spec;
        try {
            spec = jsonMapper.readValue(scrapeFile, ScrapeSpecificationImpl.class);
        } catch (Exception e) {
            try {
                spec = ymlMapper.readValue(scrapeFile, ScrapeSpecificationImpl.class);
            } catch (Exception e2) {
                throw new ValidationException("Could not parse specification, not a JSON and not a YML file!", e2);
            }
        }
        spec.setScrapeFile(scrapeFile.toPath());

        validate(spec);
        return spec;
    }

    private static void validate(ScrapeSpecification spec) throws ValidationException {
        if(spec.getName() == null) throw new ValidationException("Name field not specified");
        if(spec.getEntry() == null) throw new ValidationException("Entry field not specified");
        if(spec.getGraphs() == null) throw new ValidationException("Graphs field not specified");
        if(spec.getScrapeFile() == null) throw new ValidationException("Path to scrape file null");
    }
}
