package scraper.plugins.core.parser;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleAbstractTypeResolver;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import scraper.annotations.ArgsCommand;
import scraper.api.DIContainer;
import scraper.api.ValidationException;
import scraper.api.Address;
import scraper.api.ScrapeSpecificationParser;
import scraper.api.ScrapeImportSpecification;
import scraper.api.ScrapeSpecification;
import scraper.api.specification.deserial.AddressDeserializer;
import scraper.api.specification.impl.ScrapeSpecificationImpl;
import scraper.api.specification.impl.ScraperImportSpecificationImpl;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Optional.empty;
import static java.util.Optional.of;


@ArgsCommand(
        value = "<path ends with .yf>",
        doc = "Specify a scrape job specification in YML to run.",
        example = "scraper app.yf"
)
public final class YamlParser implements ScrapeSpecificationParser {

    // JSON mapper
    private static final ObjectMapper ymlMapper = new ObjectMapper(new YAMLFactory());
    static {
        SimpleModule module = new SimpleModule();

        module.addDeserializer(Address.class, new AddressDeserializer());
        SimpleAbstractTypeResolver resolver = new SimpleAbstractTypeResolver();
        resolver.addMapping(ScrapeImportSpecification.class, ScraperImportSpecificationImpl.class);
        module.setAbstractTypes(resolver);

        ymlMapper.registerModule(module);
    }

    private static final System.Logger log = System.getLogger("YamlParser");

    @Override
    public List<String> acceptedFileEndings() {
        return List.of("yml", "yaml", "yf");
    }

    @Override
    public List<ScrapeSpecification> parse(DIContainer loadedDependencies, String[] args) {
        List<File> jsonSpecs = filterFiles(args);
        return jsonSpecs.stream().map(this::parseSingle).flatMap(Optional::stream).collect(Collectors.toList());
    }

    public ScrapeSpecification parseFile(File file) throws IOException, ValidationException {
        ScrapeSpecificationImpl spec = ymlMapper.readValue(file, ScrapeSpecificationImpl.class);
        spec.setScrapeFile(file.toPath());

        spec.setImports(validateAndImport(spec, ScraperImportSpecificationImpl::new));

        return spec;
    }

    public Optional<ScrapeSpecification> parseSingle(File file) {
        try {
            return of(parseFile(file));
        } catch (IOException | ValidationException e) {
            log.log(System.Logger.Level.DEBUG,"Could not parse {0}: {1}", file, e.getMessage());
        } catch (NoSuchElementException e) {
            throw new IllegalArgumentException( "Could not find import specification", e);
        }

        return empty();
    }

    @Override
    public String toString() {
        return "yaml+yml+yf";
    }
}
