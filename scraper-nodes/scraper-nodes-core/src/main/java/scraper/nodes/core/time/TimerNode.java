package scraper.nodes.core.time;


import scraper.annotations.NotNull;
import scraper.annotations.node.FlowKey;
import scraper.annotations.node.NodePlugin;
import scraper.api.flow.FlowMap;
import scraper.core.AbstractFunctionalNode;
import scraper.core.Template;
import scraper.util.NodeUtil;

import java.util.concurrent.atomic.AtomicBoolean;

import static scraper.core.NodeLogLevel.*;
import static scraper.util.NodeUtil.flowOf;

/**
 * Execute a node after a timeout. TimerNode is initially dormant and has to be started with a start Action.
 * Timer can be stopped and status can be queried with other Actions (e.g. with prior
 * EchoNode invocations).
 *
 * <p>Example .scrape definition:
 *
 * <pre>
 * {
 *    "type"     : "TimerNode",
 *    "__comment": "Provides a timer for the error timeout.",
 *    "label"    : "errorTimer",
 *    "name"     : "error timer",
 *    "timeout"  : "{error-timeout}",
 *    "put"      : "result",
 *    "onTimeout": "fridgeOn",
 *    "action"   : "{error-action}",
 *    "forward"  : false
 * }</pre>
 *
 */
@NodePlugin(value = "0.1.1", stateful = true)
public final class TimerNode extends AbstractFunctionalNode {

    /** timeout in ms for this node */
    @FlowKey(mandatory = true)
    private final Template<Integer> timeout = new Template<>(){};

    /** go to node if timeout occurs */
    @FlowKey(mandatory = true)
    private String onTimeout;

    /** descriptive name of this timer */
    @FlowKey(mandatory = true)
    private String name;

    /** action to be taken when this node accepts a call */
    @FlowKey(defaultValue = "\"NoOP\"")
    private final Template<Action> action = new Template<>(){};

    /** time location */
    @FlowKey(mandatory = true)
    private String put;


    private Long elapsedTimestamp = null;
    private Long timeoutTimestamp = null;
    private Integer lastTimeout = null;

    private Thread timeoutThread;
    private AtomicBoolean forceWait = new AtomicBoolean(false);

    private Action lastReceivedAction = null;

    @Override
    public void modify(@NotNull final FlowMap o) {
        Action action = this.action.eval(o);
        processAction(action, o);
    }

    private synchronized void processAction(Action action, FlowMap o) {
        switch (action) {
            case TIME_ELAPSED: {
                checkState(o);
                break;
            }
            case TIME_LEFT: {
                checkTimeout(o);
                break;
            }
            case START: {
                log(DEBUG, "[{}] Starting timer action received", name);
                startElapsed();
                startTimeout(o);
                startThread(o, false);
                break;
            }
            case FORCE_START: {
                log(DEBUG, "[{}] Force starting timer action received", name);
                startElapsed();
                startTimeout(o);
                startThread(o, true);
                break;
            }
            case STOP: {
                if(lastReceivedAction != Action.STOP) log(DEBUG, "[{}] Stop timer action received", name);
                stopElapsed();
                stopTimeout();
                stopThread();
                break;
            }
            case NoOP: {
                log(TRACE,"[{}] No op timer action", name);
            }
        }

        lastReceivedAction = action;
    }

    private void stopTimeout() {
        timeoutTimestamp = null;
        lastTimeout = null;
    }

    private void startTimeout(FlowMap o) {
        if(timeoutTimestamp == null) {
            timeoutTimestamp = System.nanoTime()/1000000;
            lastTimeout = timeout.eval(o);
        }
    }

    private void stopElapsed() {
        elapsedTimestamp = null;
    }

    private void startElapsed() {
        if(elapsedTimestamp == null) elapsedTimestamp = System.nanoTime()/1000000;
    }

    private void stopThread() {
        if(timeoutThread != null && !forceWait.get()) {
            //TO action does not emit
            timeoutThread.interrupt();
            timeoutThread = null;
        }
    }

    private void startThread(final FlowMap o, boolean force) {
        forceWait.set(force);
        Integer timeout = this.timeout.eval(o);

        if(timeoutThread == null) {
            log(INFO,"Starting alarm, {} ms!", timeout);

            // create new timer and start it
            timeoutThread = getThread(o, timeout);
            timeoutThread.start();
        } else {
            if(force) {
                // force interrupt and recreate + start timer
                log(INFO,"Force starting alarm, {} ms!", timeout);
                timeoutThread.interrupt();
                timeoutThread = getThread(o, timeout);
                timeoutThread.start();
            } else {
                // do nothing if timer already started, let it finish
                log(DEBUG,"[{}] Timer already running, {} ms left", name, getTimeLeft());
            }
        }
    }

    private void checkState(final FlowMap o) {
        if(elapsedTimestamp != null) {
            long time = (System.nanoTime()/1000000 - elapsedTimestamp);
            o.put(put, time);
        } else {
            o.put(put, 0);
        }
    }

    private long getTimeLeft() {
        if(timeoutTimestamp != null) {
            return lastTimeout - (System.nanoTime()/1000000 - timeoutTimestamp);
        } else {
            return 0;
        }
    }

    private void checkTimeout(FlowMap o) {
        if(timeoutTimestamp != null) {
            long time = lastTimeout - (System.nanoTime()/1000000 - timeoutTimestamp);
            o.put(put, time);
        } else {
            o.put(put, 0);
        }
    }

    private Thread getThread(final FlowMap o, Integer timeout) {
        return new Thread(() -> {
            log(TRACE,"Make a local copy of the current input map");
            final FlowMap oCopy = flowOf(o);

            while(!Thread.interrupted()) {
                log(INFO,"'{}' alarm in {}", name, timeout);

                try {
                    Thread.sleep(timeout);
                    log(INFO,"Executing alarm: {}", toString());
                    forkDispatch(oCopy, NodeUtil.addressOf(onTimeout));
                } catch (InterruptedException e) {
                    log(INFO,"'{}' stopped", name);
                } finally {
                    Thread.currentThread().interrupt();
                    stopTimeout();
                    timeoutThread = null;
                }
            }
        });
    }


    enum Action {
        TIME_LEFT, TIME_ELAPSED, START, FORCE_START, STOP, NoOP
    }
}
