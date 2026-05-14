package scraper.plugins.core.typechecker.data;

import scraper.api.L;
import scraper.api.NodeContainer;
import scraper.api.ScrapeInstance;
import scraper.api.T;
import scraper.core.SubprocessNode;
import scraper.core.template.TemplateConstant;
import scraper.core.SubprocessNode.FieldSchema;
import scraper.plugins.core.flowgraph.api.ControlFlowEdge;
import scraper.plugins.core.flowgraph.api.ControlFlowGraph;
import scraper.plugins.core.typechecker.TypeChecker;
import scraper.plugins.core.typechecker.TypeEnvironment;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/** Type rule for {@link SubprocessNode}. Reads the helper binary's reported
 *  schema and registers its output locations with the type environment, so
 *  downstream nodes can see the keys the subprocess will write. Without this
 *  the type checker would treat subprocess nodes as opaque and fail to
 *  resolve templates like {@code "{full-proj-encoded}"} that reference them. */
@SuppressWarnings({"unused", "unchecked", "rawtypes"})
public final class SubprocessNodeData {

    public static void infoAfter(TypeChecker t, TypeEnvironment env, ControlFlowEdge inc,
                                 NodeContainer<?> node, ControlFlowGraph cfg,
                                 ScrapeInstance spec, List<NodeContainer<?>> visited) throws Exception {
        SubprocessNode sn = (SubprocessNode) node.getC();
        SubprocessNode.Schema schema = sn.getSchema();
        Set<String> typeVars = new LinkedHashSet<>(schema.typeVariables);
        String scope = node.getAddress().toString();

        for (FieldSchema out : schema.outputs) {
            Object locFromSpec = node.getNodeConfiguration().get(out.name);
            if (!(locFromSpec instanceof String)) continue;
            String location = (String) locFromSpec;

            L<Object> l = new L<>() {};
            l.setLocation(new TemplateConstant<>(location, new T<String>() {}));

            T token = tokenForTypeName(out.type, typeVars, scope);
            env.add(l.getLocation(), token);
        }
    }

    /** Parses the helper binary's {@code Type.getTypeName()} string back into
     *  a full {@code java.lang.reflect.Type} ({@code Class},
     *  {@code ParameterizedType}, {@code GenericArrayType}, {@code
     *  WildcardType}, or {@code TypeVariable}), preserving the generic
     *  structure so the type checker sees subprocess outputs exactly as it
     *  would an in-process node's output. Variables are alpha-renamed per
     *  node-instance via {@code scope} so two instances of nodes that both
     *  declare {@code <T>} don't collide in the captures map. A parse error
     *  is fatal — if a helper reports a type we can't reconstruct, that's a
     *  schema contract bug worth surfacing. */
    private static T<?> tokenForTypeName(String typeName, Set<String> typeVars, String scope) {
        if (typeName == null) return new T<Object>() {};
        return new T<>(TypeNameParser.parse(typeName, typeVars, scope)) {};
    }
}
