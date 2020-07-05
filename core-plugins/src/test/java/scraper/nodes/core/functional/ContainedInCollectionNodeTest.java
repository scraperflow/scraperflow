package scraper.nodes.core.functional;

import org.junit.jupiter.api.Test;
import scraper.api.exceptions.TemplateException;

import java.util.List;
import java.util.Map;

import static scraper.test.FunctionalTest.runWith;


public class ContainedInCollectionNodeTest {

    @Test
    public void emptyContains() {
        Map<?,?> nIn = Map.of();
        Map<?,?> fIn = Map.of("object", "important");
        Map<?,?> fOut = Map.of("flag", false);

        runWith(ContainedInCollectionNode.class, List.of(nIn, fIn, fOut), TemplateException.class);
    }

    @Test
    public void simpleContains() {
        Map<?,?> nIn = Map.of();
        Map<?,?> fIn = Map.of(
                "collection", List.of("important", "notImportant"),
                "object", "important"
        );
        Map<?,?> fOut = Map.of("flag", true);

        runWith(ContainedInCollectionNode.class, List.of(nIn, fIn, fOut));
    }

    @Test
    public void complexObjectContains() {
        Map<?,?> nIn = Map.of();
        Map<?,?> fIn = Map.of(
                "collection", List.of("important", List.of("hello", "world")),
                "object", List.of("hello", "world")
        );
        Map<?,?> fOut = Map.of("flag", true);

        runWith(ContainedInCollectionNode.class, List.of(nIn, fIn, fOut));
    }

    @Test
    public void complexObjectNotContains() {
        Map<?,?> nIn = Map.of();
        Map<?,?> fIn = Map.of(
                "collection", List.of("important", List.of("hello", "world")),
                "object", List.of("hello")
        );
        Map<?,?> fOut = Map.of("flag", false);

        runWith(ContainedInCollectionNode.class, List.of(nIn, fIn, fOut));
    }
}
