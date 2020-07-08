package scraper.nodes.core.time;

import scraper.annotations.NotNull;
import scraper.annotations.node.FlowKey;
import scraper.annotations.node.NodePlugin;
import scraper.annotations.node.Stateful;
import scraper.api.flow.FlowMap;
import scraper.api.node.Address;
import scraper.annotations.node.Flow;
import scraper.api.node.container.FunctionalNodeContainer;
import scraper.api.node.container.NodeContainer;
import scraper.api.node.type.FunctionalNode;
import scraper.api.node.type.Node;
import scraper.api.specification.ScrapeInstance;
import scraper.api.template.T;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

import static scraper.api.node.container.NodeLogLevel.DEBUG;


/**
 * Executes a given node periodically.
 * Periodic execution can start at initialization or on first accept call, controlled via the flag.
 */
@NodePlugin(value = "1.1.0")
@Stateful
public final class PeriodicNode implements FunctionalNode {

    /** Period time in ms */
    @FlowKey(mandatory = true)
    private Long period;

    /** Target node label which is called on period */
    @FlowKey(mandatory = true)
    @Flow(dependent = false, crossed = true, label = "periodic")
    private Address onPeriod;

    /** If true, enables dispatch of the periodic task. If false, stops dispatch of the periodic task */
    @FlowKey(defaultValue = "true")
    private final T<Boolean> flag = new T<>(){};

    private final AtomicBoolean started = new AtomicBoolean(false);
    private final AtomicBoolean dispatch = new AtomicBoolean(false);
    private FlowMap o;
    private Timer timer;
    private TimerTask timerTask;

    @Override
    public void init(@NotNull NodeContainer<? extends Node> n, @NotNull final ScrapeInstance job) {
        timerTask = new TimerTask() {
            @Override
            public void run() {
                if(started.get() && dispatch.get()) {
                    n.log(DEBUG,"Dispatching {0}", onPeriod);
                    final FlowMap oCopy = o.copy();
                    n.forkDispatch(oCopy, onPeriod);
                }
            }
        };
    }

    @Override
    public void modify(@NotNull FunctionalNodeContainer n, @NotNull final FlowMap o) {
        boolean flag = o.eval(this.flag);
        dispatch.set(flag);
        this.o = o.copy();

        if (!started.getAndSet(true)) {
            timer = new Timer(false);
            timer.scheduleAtFixedRate(timerTask, 0 , period);
        }
    }

}
