package scraper.nodes.core.time;

import scraper.annotations.NotNull;
import scraper.annotations.node.FlowKey;
import scraper.annotations.node.NodePlugin;
import scraper.api.flow.FlowMap;
import scraper.api.node.Address;
import scraper.api.node.container.FunctionalNodeContainer;
import scraper.api.node.container.NodeContainer;
import scraper.api.node.type.FunctionalNode;
import scraper.api.node.type.Node;
import scraper.api.reflect.T;
import scraper.api.specification.ScrapeInstance;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

import static scraper.api.node.container.NodeLogLevel.DEBUG;
import static scraper.util.NodeUtil.flowOf;

/**
 * Executes a given node periodically.
 * Periodic execution can start at initialization or on first accept call, controlled via the flag.
 */
@NodePlugin(value = "1.0.0")
public final class PeriodicNode implements FunctionalNode {

    /** Period time in ms */
    @FlowKey(mandatory = true)
    private Long period;

    /** Target node label which is called on period */
    @FlowKey(mandatory = true)
    private Address onPeriod;

    /** If true, enables dispatch of the periodic task. If false, stops dispatch of the periodic task */
    @FlowKey(defaultValue = "true")
    private T<Boolean> flag = new T<>(){};

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
                    n.log(DEBUG,"Dispatching {}", onPeriod);
                    final FlowMap oCopy = o.copy();
                    n.forkDispatch(oCopy, onPeriod);
                }
            }
        };

        timer = new Timer(false);
    }

    @Override
    public void modify(@NotNull FunctionalNodeContainer n, @NotNull final FlowMap o) {
        boolean flag = o.eval(this.flag);
        dispatch.set(flag);
        this.o = flowOf(o);

        if (!started.getAndSet(true)) {
            timer.scheduleAtFixedRate(timerTask, 0 , period);
        }
    }

}
