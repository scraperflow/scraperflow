package scraper.api.specification.simple;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import scraper.api.exceptions.ValidationException;
import scraper.api.specification.ScrapeSpecification;
import scraper.api.specification.impl.ScrapeSpecificationImpl;
import scraper.plugins.core.parser.YamlParser;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SimpleScrapeSpecificationImpl {

    private static final YamlParser p = new YamlParser();
    private static final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

    public static ScrapeSpecification parse(File spec) throws ValidationException {
        try {
            p.parseFile(spec);
            throw new ValidationException("Not a simple yaml specification");
        } catch (Exception ignored) {}

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> rawSpec = ((Map<String, Object>) mapper.readValue(spec, Map.class));

            // pre-process each block
            // f -> type
            // name -> nameNode
            Function<List<Map<String, Object>>, List<Map<String, Object>>> transform =
                    blocks -> blocks.stream()
                            .map(SimpleScrapeSpecificationImpl::transformBlock)
                            .collect(Collectors.toList());

            @SuppressWarnings("unchecked") // cast is caught
            Map<String, List<Map<String, Object>>> transformedNodes = rawSpec.entrySet()
                    .stream()
                    .map(e -> Map.entry(e.getKey(), transform.apply(((List<Map<String, Object>>) e.getValue()))))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

            ScrapeSpecificationImpl validSpec = new ScrapeSpecificationImpl();
            validSpec.setGraphs(transformedNodes);
            validSpec.setName("flow");
            validSpec.setScrapeFile(spec.toPath());
            validSpec.setEntry("start");

            return validSpec;
        } catch (Exception e) {
            throw new ValidationException("Not a valid simple yaml specification");
        }
    }

    private static Map<String, Object> transformBlock(Map<String, Object> block) {
        // f -> type transform
        if(block.containsKey("f")) {
            Object type = block.remove("f");
            block.put("type", type);
        }

        // name -> nameNode transform
        block.compute("type", (s, o) -> {
            String type = ((String) o);
            assert type != null;
            return type.endsWith("Node") ? type : type + "Node";
        });

        return block;
    }
}
