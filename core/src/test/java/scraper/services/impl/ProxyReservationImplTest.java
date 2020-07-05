package scraper.services.impl;

import org.junit.jupiter.api.Test;
import scraper.api.service.ProxyReservation;
import scraper.api.service.impl.ProxyReservationImpl;
import scraper.api.service.proxy.GroupInfo;
import scraper.api.service.proxy.ProxyMode;
import scraper.api.service.proxy.ReservationToken;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

public class ProxyReservationImplTest {

    @Test
    public void noAvailable() throws Exception {
        ProxyReservation r = new ProxyReservationImpl();
        // first proxy
        r.addProxies(Set.of("1.1.1.1:1"), "none");

        try (ReservationToken token = r.reserveToken("none", ProxyMode.PROXY, 100, 100)) {
            // first proxy is reserved
            assertTrue(token.score() <= 201 && token.score() >= 100);

            // try to reserve a second proxy, should fail
            try (ReservationToken token2 = r.reserveToken("none", ProxyMode.PROXY,100,100)) {
                fail("Got token even though none available");
            } catch (TimeoutException ignored) {}

            // start parallel request
            AtomicReference<Boolean> result = new AtomicReference<>();
            Thread t = new Thread(() -> {
                try (ReservationToken proxy = r.reserveToken("none", ProxyMode.PROXY, 201, 100)) {
                    System.out.println("Got new proxy! " + proxy.get());
                    assertNotNull(proxy);
                    result.set(true);
                } catch (Exception e) {
                    System.err.println(e.toString());
                    result.set(false);
                }
            });

            t.start();
            Thread.sleep(50);
            r.addProxies(Set.of("2.2.2.2:2"), "none");
            t.join();
            if(!result.get()) fail();

        }
    }
    @Test
    public void simpleTest() throws Exception {
        ProxyReservation r = new ProxyReservationImpl();
        r.addProxies(Set.of("1.1.1.1:1"), "simple");

        try (ReservationToken token = r.reserveToken("simple", ProxyMode.PROXY, 100, 10)) {
            assertTrue(token.score() <= 201 && token.score() >= 100);
        }
    }

    @Test
    public void addProxiesTest() throws Exception {
        ProxyReservation r = new ProxyReservationImpl();

        r.addProxies(Set.of("1.1.1.1:1", "2.2.2.2:2","3.3.3.3:3"), "t1");

        // get token and release, check score
        for (int i = 1; i < 10; i++) {
            try (ReservationToken token = r.reserveToken("t1", ProxyMode.PROXY, 100, 10)) {
                assertTrue(token.score() <= 201 && token.score() >= 100, token.score()+"");
            }
        }
    }

    @Test
    public void localTest() throws Exception {
        ProxyReservation r = new ProxyReservationImpl();

        // reserve some local transport
        try (ReservationToken token = r.reserveToken("t1", ProxyMode.LOCAL, 10, 10)) {
            // two times the same group is prohibited
            try (ReservationToken token2 = r.reserveToken("t1", ProxyMode.LOCAL, 10, 10)) {
                fail("Reserved local transport for same group two times!");
            } catch (TimeoutException expected){}

            // another group is fine
            try (ReservationToken token2 = r.reserveToken("t2", ProxyMode.LOCAL, 10, 10)) {}
        }

        Thread.sleep(20);
        // wait for other thread
        Thread t = new Thread(() -> {
            try (ReservationToken token = r.reserveToken("t1", ProxyMode.LOCAL, 10, 10)) {
                Thread.sleep(100);
            } catch (InterruptedException | TimeoutException e) {
                e.printStackTrace();
            }
        });
        t.start();
        Thread.sleep(20);

        try (ReservationToken token = r.reserveToken("t1", ProxyMode.LOCAL, 200, 10)) {
            System.out.println("y");
            token.bad();
        }
    }

    @Test
    public void fileAddTest() throws IOException {
        File tmp = File.createTempFile("pre","suff");
        tmp.deleteOnExit();
        try (PrintWriter pw = new PrintWriter(new FileOutputStream(tmp))) {
            pw.println("1.1.1.1:1");
            pw.println("1.2.1.2:1");
            pw.println("1.2.1.2:3");
        }

        ProxyReservation r = new ProxyReservationImpl();
        r.addProxies(tmp.getAbsolutePath(),"1");
        assertTrue(tmp.delete());

        // should log error
        r.addProxies(tmp.getAbsolutePath(),"1");

        GroupInfo info = r.getInfoForGroup("1");
        assertNotNull(info);
        List<String> l = info.getAllProxiesAsString(false);
        assertTrue(l.contains("1.1.1.1:1"));
        assertTrue(l.contains("1.2.1.2:1"));
        assertTrue(l.contains("1.2.1.2:3"));
    }

    @Test
    public void reserveTokenTest() {
        assertTimeout(Duration.ofSeconds(1), () ->{
            ProxyReservation r = new ProxyReservationImpl();

            // wait for other thread
            Thread t = new Thread(() -> {
                try (ReservationToken token = r.reserveToken("t1", ProxyMode.LOCAL, 10, 10)) {
                    Thread.sleep(10);
                    System.out.println("Finished");
                } catch (InterruptedException | TimeoutException e) { e.printStackTrace(); }
            });

            t.start();
            Thread.sleep(20);

            try (ReservationToken token = r.reserveToken("t1", ProxyMode.LOCAL)) {
                System.out.println("y");
            }

                });
    }
}