//package scraper.plugins.core.flowgraph.data;
//
//public class EchoNodeData  {
//
////    @Version("1.1.0")
////    public static Map<String, String> getOutput(Map<String, String> previous, NodeContainer target, ScrapeInstance instance) throws Exception {
////        // 1.1.0 has puts and remove
////        Template<Map<String, Object>> puts = FlowUtil.getField("puts", target);
////        List<String> remove = FlowUtil.getField("remove", target);
////
////        // this assumes that no template are used in puts keys
////        for (String toPut : puts.evalWithIdentity().keySet()) {
////            // TODO more than just 'Object' can be inferred about the type here
////            previous.put(toPut, "scraper.core.Template<java.lang.Object>");
////        }
////
////        // remove entries
////        for (String toRemove : remove) {
////            previous.remove(toRemove);
////        }
////
////        return previous;
////    }
//}
