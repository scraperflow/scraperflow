package scraper.plugins.core.typechecker.visitors;

import scraper.api.*;

import java.util.LinkedList;
import java.util.List;

public class GatherConsumes extends DefaultVisitor<Void> {

    public List<Term<?>> consumes = new LinkedList<>();

    @Override
    public Void visitFlowKeyLookup(FlowKeyLookup<?> mapKey) {
        if(mapKey.isConsume()){
            consumes.add(mapKey);
        }

        return null;
    }
}
