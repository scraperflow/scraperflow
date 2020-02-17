package scraper.nodes.core.io;


import scraper.annotations.NotNull;
import scraper.annotations.node.Argument;
import scraper.annotations.node.FlowKey;
import scraper.annotations.node.NodePlugin;
import scraper.api.flow.FlowMap;
import scraper.api.node.container.FunctionalNodeContainer;
import scraper.api.node.container.NodeContainer;
import scraper.api.node.type.FunctionalNode;
import scraper.api.reflect.T;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Pings a given hostname on the given port.
 */
@NodePlugin("0.1.0")
public final class PingNode implements FunctionalNode {

    /** Argument ping address */
    @FlowKey(mandatory = true) @Argument
    private String address;

    /** Argument ping port */
    @FlowKey(mandatory = true) @Argument
    private Integer port;

    /** List of targets with additional parameters */
    @FlowKey(output = true)
    private T<Boolean> result = new T<>(){};

    /** Enforced ping timeout time in ms. Default: 500 */
    @FlowKey(defaultValue = "500") @Argument
    private Integer timeout;


    @Override
    public void modify(@NotNull FunctionalNodeContainer n, @NotNull final FlowMap o) {
        boolean ping = isReachable(n, address, port, timeout);
        o.output(result, ping);
    }

    /**
     * @param addr Ipv4 address or hostname
     * @param openPort Port known to be open
     * @param timeOutMillis timeout for the ping
     * @return true if target is reachable; false otherwise
     */
    private boolean isReachable(NodeContainer n, String addr, int openPort, int timeOutMillis) {
        Future<Boolean> future = n.getService().submit(() -> {
            try {
                try (Socket soc = new Socket()) {
                    soc.setSoTimeout(timeOutMillis);
                    soc.connect(new InetSocketAddress(addr, openPort), timeOutMillis);
                }
                return true;
            } catch (IOException ex) {
                return false;
            }
        });

        try {
            return future.get(timeOutMillis, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            return false;
        }
    }
}
