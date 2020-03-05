package scraper.nodes.core.functional;

import junitparams.JUnitParamsRunner;
import org.junit.runner.RunWith;
import scraper.api.exceptions.TemplateException;
import scraper.nodes.core.test.annotations.Functional;
import scraper.nodes.core.test.helper.FunctionalTest;

import java.util.List;
import java.util.Map;

@RunWith(JUnitParamsRunner.class)
public class FlattenListNodeTest extends FunctionalTest {

    @Functional(FlattenListNode.class)
    public Object[] emptyLists() {
        return new Object[]{
                Map.of("flatten", List.of(List.of(), List.of())),
                Map.of(),
                Map.of("output",List.of())
        };
    }

    @Functional(FlattenListNode.class)
    public Object[] singletonList() {
        return new Object[]{
                Map.of("flatten", List.of(List.of(), List.of(50))),
                Map.of(),
                Map.of("output",List.of(50))
        };
    }

    @Functional(FlattenListNode.class)
    public Object[] simpleLists() {
        return new Object[]{
                Map.of("flatten", List.of(List.of("123120"), List.of("qweq"))),
                Map.of(),
                Map.of("output",List.of("123120","qweq"))
        };
    }

    @Functional(value = FlattenListNode.class, expectException = TemplateException.class)
    public Object[] badTypes() {
        return new Object[]{
                Map.of("flatten", List.of(List.of("wontwork"), List.of(50))),
                Map.of(),
                Map.of()
        };
    }
}

