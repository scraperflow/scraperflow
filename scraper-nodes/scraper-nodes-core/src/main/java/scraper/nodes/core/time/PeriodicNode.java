package scraper.nodes.core.time;

import scraper.annotations.node.FlowKey;
import scraper.annotations.node.NodePlugin;
import scraper.api.exceptions.NodeException;
import scraper.api.exceptions.ValidationException;
import scraper.api.flow.FlowMap;
import scraper.api.specification.ScrapeInstance;
import scraper.core.AbstractNode;
import scraper.core.Template;
import scraper.util.NodeUtil;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

import static scraper.core.NodeLogLevel.DEBUG;
import static scraper.util.NodeUtil.flowOf;

/**
 * Executes a given node periodically. Exceptions are swallowed. Periodic execution can start at initialization or on first
 * accept call, controlled via the flag.
 *
 * <p>Example .scrape definition:
 *
 * <pre>
 * {
 *   "type": "PeriodicNode",
 *   "label" : "initPings",
 *   "period": "{status-ping-period}",
 *   "onPeriod": "ping",
 *   "forward" : false
 * }</pre>
 *
 * @see AbstractNode
 * @apiNote thread safe
 * @since 0.1
 * @author Albert Schimpf
 */
@NodePlugin(value = "0.1.0", stateful = true)
public final class PeriodicNode extends AbstractNode {

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
    public void init(final ScrapeInstance job) throws ValidationException {
        super.init(job);

        o = flowOf(job.getInitialArguments());

        timerTask = new TimerTask() {
            @Override
            public void run() {
                if(started.get() && dispatch.get()) {
                    log(DEBUG,"Dispatching {}", onPeriod);
                    final FlowMap oCopy = o;
                    dispatch(() -> {
                        try {
                            eval(oCopy, NodeUtil.addressOf(onPeriod));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        return null;
                    });

                }
            }
        };

        timer = new Timer(false);
    }

    @Override
    public FlowMap process(final FlowMap o) throws NodeException {
        start(o);

        boolean flag = this.flag.eval(o);
        dispatch.set(flag);
        this.o = flowOf(o);

        synchronized (started) {
            if(!started.get()) {
                started.set(true);
                timer.scheduleAtFixedRate(timerTask, 0 , period);
            }
        }


        return forward(o);
    }

}
