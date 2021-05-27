package scraper.nodes.core.functional;

import scraper.annotations.NotNull;
import scraper.annotations.node.FlowKey;
import scraper.annotations.node.NodePlugin;
import scraper.api.flow.FlowMap;
import scraper.api.node.container.FunctionalNodeContainer;
import scraper.api.node.type.FunctionalNode;
import scraper.api.template.L;
import scraper.api.template.T;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Sorts a list.
 */
@NodePlugin("0.1.0")
public final class ListSort<K> implements FunctionalNode {

    /** List to sort */
    @FlowKey(mandatory = true)
    private final T<List<K>> list = new T<>(){};

    /**
     * Type of sort: DEFAULT, NUMERIC
     */
    @FlowKey(defaultValue = "\"DEFAULT\"")
    private ListSort.SortType sortType;

    /** Sorted list */
    @FlowKey(mandatory = true)
    private final L<List<K>> output = new L<>(){};

    @Override
    public void modify(@NotNull FunctionalNodeContainer n, @NotNull final FlowMap o) {
        List<K> l = o.eval(this.list);
        switch (sortType) {
            case DEFAULT -> o.output(output, l.stream().sorted().collect(Collectors.toList()));
            case NUMERIC -> o.output(output, l.stream()
                    .sorted((o1, o2) -> {
                        StringBuilder sb1 = new StringBuilder();
                        String.valueOf(o1).codePoints().filter(Character::isDigit).forEachOrdered(sb1::append);

                        StringBuilder sb2 = new StringBuilder();
                        String.valueOf(o2).codePoints().filter(Character::isDigit).forEachOrdered(sb2::append);

                        return Integer.valueOf(sb1.toString()).compareTo(Integer.valueOf(sb2.toString()));
                    })
                    .collect(Collectors.toList()));
        }
        System.out.println();
    }

    public enum SortType {
        DEFAULT, NUMERIC
    }
}
