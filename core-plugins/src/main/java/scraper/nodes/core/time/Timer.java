package scraper.nodes.core.time;


import scraper.annotations.NotNull;
import scraper.annotations.FlowKey;
import scraper.annotations.NodePlugin;
import scraper.annotations.Stateful;
import scraper.api.FlowMap;
import scraper.api.Address;
import scraper.annotations.Flow;
import scraper.api.FunctionalNodeContainer;
import scraper.api.NodeContainer;
import scraper.api.FunctionalNode;
import scraper.api.Node;
import scraper.api.L;
import scraper.api.T;

import java.util.concurrent.atomic.AtomicBoolean;

import static scraper.api.NodeLogLevel.*;


/**
 * Execute a node after a timeout.
 * TimerNode is initially dormant and has to be started with a start Action.
 * Timer can be stopped and status can be queried with other Actions (e.g. with prior
 * Echo invocations).
 *
 * <p>Example:
 *
 * <pre>
 *type     : Timer
 *name     : error timer
 *timeout  : "{error-timeout}"
 *put      : result
 *onTimeout: fridgeOn
 *action   : "{error-action}"
 * </pre>
 */
@NodePlugin(value = "0.2.0")
@Stateful
public final class Timer implements FunctionalNode {

    /** Timeout in ms for this node */
    @FlowKey(mandatory = true)
    private final T<Integer> timeout = new T<>(){};

    /** Go to node if timeout occurs */
    @FlowKey(mandatory = true)
    @Flow(label = "timeout")
    private Address onTimeout;

    /** Descriptive name of this timer. Used for logging. */
    @FlowKey(mandatory = true)
    private String name;

    /** Action to be taken when this node accepts a call.
     * <ul>
     *     <li>TIME_LEFT: request timeout time left and save it to <var>put</var></li>
     *     <li>TIME_ELAPSED: request timeout time elapsed and save it to <var>put</var></li>
     *     <li>START: starts the timer if it is not started yet with given timeout </li>
     *     <li>FORCE_START: starts and possibly resets the timer even if it is started already with given timeout </li>
     *     <li>STOP: stops the timer if it is running, does nothing if it is stopped already </li>
     *     <li>NoOP: does nothing </li>
     * </ul>
     */
    @FlowKey(mandatory = true)
    private final T<Action> action = new T<>(){};

    /** Where to put the remaining timeout time on request */
    @FlowKey(mandatory = true)
    private final L<Long> put = new L<>(){};


    private Long elapsedTimestamp = null;
    private Long timeoutTimestamp = null;
    private Integer lastTimeout = null;

    private Thread timeoutThread;
    private AtomicBoolean forceWait = new AtomicBoolean(false);

    private Action lastReceivedAction = null;

    @Override
    public void modify(@NotNull FunctionalNodeContainer n, @NotNull final FlowMap o) {
        Action action = o.eval(this.action);
        processAction(n, action, o);
    }

    private synchronized void processAction(NodeContainer<? extends Node> n, Action action, FlowMap o) {
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
                n.log(DEBUG, "[{0}] Starting timer action received", name);
                startElapsed();
                startTimeout(o);
                startThread(n, o, false);
                break;
            }
            case FORCE_START: {
                n.log(DEBUG, "[{0}] Force starting timer action received", name);
                startElapsed();
                startTimeout(o);
                startThread(n, o, true);
                break;
            }
            case STOP: {
                if(lastReceivedAction != Action.STOP) n.log(DEBUG, "[{0}] Stop timer action received", name);
                stopElapsed();
                stopTimeout();
                stopThread();
                break;
            }
            case NoOP: {
                n.log(TRACE,"[{0}] No op timer action", name);
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
            lastTimeout = o.eval(timeout);
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

    private void startThread(NodeContainer<? extends Node> n, final FlowMap o, boolean force) {
        forceWait.set(force);
        Integer timeout = o.eval(this.timeout);

        if(timeoutThread == null) {
            n.log(INFO,"Starting alarm, {0} ms!", timeout);

            // create new timer and start it
            timeoutThread = getThread(n, o, timeout);
            timeoutThread.start();
        } else {
            if(force) {
                // force interrupt and recreate + start timer
                n.log(INFO,"Force starting alarm, {0} ms!", timeout);
                timeoutThread.interrupt();
                timeoutThread = getThread(n, o, timeout);
                timeoutThread.start();
            } else {
                // do nothing if timer already started, let it finish
                n.log(DEBUG,"[{0}] Timer already running, {1} ms left", name, getTimeLeft());
            }
        }
    }

    private void checkState(final FlowMap o) {
        if(elapsedTimestamp != null) {
            Long time = (System.nanoTime()/1000000 - elapsedTimestamp);
            o.output(put, time);
        } else {
            o.output(put, 0L);
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
            Long time = lastTimeout - (System.nanoTime()/1000000 - timeoutTimestamp);
            o.output(put, time);
        } else {
            o.output(put, 0L);
        }
    }

    private Thread getThread(NodeContainer<? extends Node> n, final FlowMap o, Integer timeout) {
        return new Thread(() -> {
            n.log(TRACE,"Make a local copy of the current input map");
            final FlowMap oCopy = o.copy();

            while(!Thread.interrupted()) {
                n.log(INFO,"{0} alarm in {1}", name, timeout);

                try {
                    Thread.sleep(timeout);
                    n.log(INFO,"Executing alarm: {0}", toString());
                    n.forward(oCopy, onTimeout);
                } catch (InterruptedException e) {
                    n.log(INFO,"{0} stopped", name);
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
