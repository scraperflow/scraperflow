package scraper.services.impl;

import org.junit.Assert;
import org.junit.Test;
import scraper.api.service.ProxyReservation;
import scraper.api.service.impl.ProxyReservationImpl;

import java.util.Set;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

public class ProxyReservationImplTest {

    @Test
    public void noAvailable() throws Exception {
        ProxyReservation r = new ProxyReservationImpl();
        // first proxy
        r.addProxies(Set.of("1.1.1.1:1"), "none");

        try (ProxyReservation.ReservationToken token = r.reserveToken("none", ProxyReservation.ProxyMode.PROXY, 100, 100)) {
            // first proxy is reserved
            Assert.assertTrue(token.score() <= 201 && token.score() >= 100);

            // try to reserve a second proxy, should fail
            try (ProxyReservation.ReservationToken token2 = r.reserveToken("none", ProxyReservation.ProxyMode.PROXY,100,100)) {
                Assert.fail("Got token even though none available");
            } catch (TimeoutException ignored) {}

            // start parallel request
            AtomicReference<Boolean> result = new AtomicReference<>();
            Thread t = new Thread(() -> {
                try (ProxyReservation.ReservationToken proxy = r.reserveToken("none", ProxyReservation.ProxyMode.PROXY, 201, 100)) {
                    System.out.println("Got new proxy! " + proxy.get());
                    Assert.assertNotNull(proxy);
                    result.set(true);
                } catch (Exception e) {
                    System.err.println(e);
                    result.set(false);
                }
            });

            t.start();
            Thread.sleep(50);
            r.addProxies(Set.of("2.2.2.2:2"), "none");
            t.join();
            if(!result.get()) Assert.fail();

        }
    }
    @Test
    public void simpleTest() throws Exception {
        ProxyReservation r = new ProxyReservationImpl();
        r.addProxies(Set.of("1.1.1.1:1"), "simple");

        try (ProxyReservation.ReservationToken token = r.reserveToken("simple", ProxyReservation.ProxyMode.PROXY, 100, 10)) {
            Assert.assertTrue(token.score() <= 201 && token.score() >= 100);
        }
    }

    @Test
    public void addProxiesTest() throws Exception {
        ProxyReservation r = new ProxyReservationImpl();

        r.addProxies(Set.of("1.1.1.1:1", "2.2.2.2:2","3.3.3.3:3"), "t1");

        // get token and release, check score
        for (int i = 1; i < 10; i++) {
            try (ProxyReservation.ReservationToken token = r.reserveToken("t1", ProxyReservation.ProxyMode.PROXY, 100, 10)) {
                Assert.assertTrue(""+token.score(), token.score() <= 201 && token.score() >= 100);
            }
        }
    }
}