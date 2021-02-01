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
public class TodoList implements FunctionalNode {

    /** Location of output user */
    @FlowKey(defaultValue = "\"_\"")
    private final L<TodoListR> put = new L<>(){};

    @FlowKey(mandatory = true)
    private final T<String> name = new T<>(){};
    @FlowKey(mandatory = true)
    private final T<List<String>> list = new T<>(){};

    @Override
    public void modify(@NotNull FunctionalNodeContainer n, @NotNull final FlowMap o) {
        TodoListR u = new TodoListR();
        u.name = o.eval(name);
        u.list = o.eval(list);
        o.output(put, u);
    }

    public static class TodoListR implements Serializable {
        public String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public List<String> getList() {
            return list;
        }

        public void setList(List<String> list) {
            this.list = list;
        }

        public List<String> list;

        public String toString() {
            return "{"+name+ ": " +list+"}";
        }
    }
}
