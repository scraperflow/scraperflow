package scraper.nodes.core.example;

import scraper.annotations.NotNull;
import scraper.annotations.FlowKey;
import scraper.annotations.NodePlugin;
import scraper.api.FlowMap;
import scraper.api.FunctionalNodeContainer;
import scraper.api.FunctionalNode;
import scraper.api.L;
import scraper.api.T;

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
