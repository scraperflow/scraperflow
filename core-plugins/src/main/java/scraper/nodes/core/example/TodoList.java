package scraper.nodes.core.example;

import scraper.annotations.NotNull;
import scraper.annotations.FlowKey;
import scraper.annotations.NodePlugin;
import scraper.api.FlowMap;
import scraper.api.FunctionalNodeContainer;
import scraper.api.FunctionalNode;
import scraper.api.L;
import scraper.api.T;

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
