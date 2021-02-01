package scraper.nodes.core.example;

import scraper.annotations.NotNull;
import scraper.annotations.node.FlowKey;
import scraper.annotations.node.NodePlugin;
import scraper.api.flow.FlowMap;
import scraper.api.node.container.FunctionalNodeContainer;
import scraper.api.node.type.FunctionalNode;
import scraper.api.template.L;
import scraper.api.template.T;

import java.io.Serializable;
import java.util.List;

/**
 * Create a user record
 */
@NodePlugin(value = "0.0.1", deprecated = true)
public class TodoListExtract implements FunctionalNode {

    /** Location of output user */
    @FlowKey(mandatory = true)
    private final T<TodoList.TodoListR> todoList = new T<>(){};

    @FlowKey(defaultValue = "\"_\"")
    private final L<String> name = new L<>(){};

    @FlowKey(defaultValue = "\"_\"")
    private final L<List<String>> list = new L<>(){};

    @Override
    public void modify(@NotNull FunctionalNodeContainer n, @NotNull final FlowMap o) {
        TodoList.TodoListR u = o.eval(todoList);
        o.output(name, u.name);
        o.output(list, u.list);
    }
}
