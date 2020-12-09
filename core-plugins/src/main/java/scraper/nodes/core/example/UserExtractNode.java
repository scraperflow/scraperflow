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
 * Unbox user class
 */
@NodePlugin(value = "0.0.1", deprecated = true)
public class UserExtractNode implements FunctionalNode {

    /** Get User Object */
    @FlowKey(mandatory = true)
    private final T<UserNode.User> get = new T<>(){};

    /** Location of name output */
    @FlowKey(defaultValue = "\"_\"")
    private final L<String> name = new L<>(){};
    @FlowKey(defaultValue = "\"_\"")
    private final L<Integer> id = new L<>(){};
    @FlowKey(defaultValue = "\"_\"")
    private final L<Boolean> verified = new L<>(){};

    @Override
    public void modify(@NotNull FunctionalNodeContainer n, @NotNull final FlowMap o) {
        UserNode.User user = o.eval(get);
        o.output(name, user.name);
        o.output(id, user.id);
        o.output(verified, user.verified);
    }
}
