package scraper.nodes.core.example;

import scraper.annotations.NotNull;
import scraper.annotations.node.FlowKey;
import scraper.annotations.node.NodePlugin;
import scraper.api.flow.FlowMap;
import scraper.api.node.container.FunctionalNodeContainer;
import scraper.api.node.type.FunctionalNode;
import scraper.api.template.L;
import scraper.api.template.T;

/**
 * Create a user record
 */
@NodePlugin(value = "0.0.1", deprecated = true)
public class User implements FunctionalNode {

    /** Location of output user */
    @FlowKey(defaultValue = "\"_\"")
    private final L<RUser> put = new L<>(){};

    @FlowKey(mandatory = true)
    private final T<String> name = new T<>(){};
    @FlowKey(mandatory = true)
    private final T<Integer> id = new T<>(){};
    @FlowKey(defaultValue = "true")
    private final T<Boolean> verified = new T<>(){};

    @Override
    public void modify(@NotNull FunctionalNodeContainer n, @NotNull final FlowMap o) {
        RUser u = new RUser();
        u.id = o.eval(id);
        u.name = o.eval(name);
        u.verified = o.eval(verified);
        o.output(put, u);
    }

    public static class RUser {
        Integer id;
        String name;
        Boolean verified;

        public String toString() {
            return "{"+id+ ": " +name+"}";
        }
    }
}
