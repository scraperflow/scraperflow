package scraper.nodes.core.example;

import scraper.annotations.NotNull;
import scraper.annotations.FlowKey;
import scraper.annotations.NodePlugin;
import scraper.api.FlowMap;
import scraper.api.FunctionalNodeContainer;
import scraper.api.FunctionalNode;
import scraper.api.L;
import scraper.api.T;

/**
 * Unbox user class
 */
@NodePlugin(value = "0.0.1", deprecated = true)
public class UserExtract implements FunctionalNode {

    /** Get User Object */
    @FlowKey(mandatory = true)
    private final T<User.RUser> get = new T<>(){};

    /** Location of name output */
    @FlowKey(defaultValue = "\"_\"")
    private final L<String> name = new L<>(){};
    @FlowKey(defaultValue = "\"_\"")
    private final L<Integer> id = new L<>(){};
    @FlowKey(defaultValue = "\"_\"")
    private final L<Boolean> verified = new L<>(){};

    @Override
    public void modify(@NotNull FunctionalNodeContainer n, @NotNull final FlowMap o) {
        User.RUser user = o.eval(get);
        o.output(name, user.name);
        o.output(id, user.id);
        o.output(verified, user.verified);
    }
}
