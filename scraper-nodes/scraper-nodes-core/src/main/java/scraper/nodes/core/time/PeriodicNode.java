package scraper.nodes.core.time;

import scraper.annotations.NotNull;
import scraper.annotations.node.FlowKey;
import scraper.annotations.node.NodePlugin;
import scraper.api.exceptions.ValidationException;
import scraper.api.flow.FlowMap;
import scraper.api.specification.ScrapeInstance;
import scraper.core.AbstractFunctionalNode;
import scraper.core.Template;
import scraper.util.NodeUtil;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

import static scraper.core.NodeLogLevel.DEBUG;
import static scraper.util.NodeUtil.flowOf;

/**
 * Executes a given node periodically.
 * Periodic execution can start at initialization or on first accept call, controlled via the flag.
 */
@NodePlugin(value = "1.0.0", stateful = true)
public final class PeriodicNode extends AbstractFunctionalNode {

    /** Period time in ms */
    @FlowKey(mandatory = true)
    private Long period;

    /** Target node label which is called on period */
    @FlowKey(mandatory = true)
    private String onPeriod;

    /** If true, enables dispatch of the periodic task. If false, stops dispatch of the periodic task */
    @FlowKey(defaultValue = "true")
    private Template<Boolean> flag = new Template<>(){};

    private final AtomicBoolean started = new AtomicBoolean(false);
    private final AtomicBoolean dispatch = new AtomicBoolean(false);
    private FlowMap o;
    private Timer timer;
    private TimerTask timerTask;

    @Override
    public void init(@NotNull final ScrapeInstance job) throws ValidationException {
        super.init(job);

        o = flowOf(job.getInitialArguments());

        timerTask = new TimerTask() {
            @Override
            public void run() {
                if(started.get() && dispatch.get()) {
                    log(DEBUG,"Dispatching {}", onPeriod);
                    final FlowMap oCopy = o;
                    forkDispatch(oCopy, NodeUtil.addressOf(onPeriod));
                }
            }
        };

        timer = new Timer(false);
    }

    @Override
    public void modify(@NotNull final FlowMap o) {
        boolean flag = this.flag.eval(o);
        dispatch.set(flag);
        this.o = flowOf(o);

        if (!started.getAndSet(true)) {
            timer.scheduleAtFixedRate(timerTask, 0 , period);
        }
    }

}
