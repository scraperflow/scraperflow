package scraper.test.services.impl.proxy;

import org.junit.jupiter.api.Test;
import scraper.api.service.impl.proxy.ReservationTokenImpl;

import java.net.InetSocketAddress;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;


@SuppressWarnings("ResultOfMethodCallIgnored")
public class ReservationTokenImplTest {

    @Test
    public void equalsTest() {
        ReservationTokenImpl t1 = new ReservationTokenImpl(
                UUID.randomUUID(),1L,0L,
                null, System.out::println, System.out::println);

        ReservationTokenImpl t2 = new ReservationTokenImpl(
                UUID.randomUUID(),1L,0L,
                new InetSocketAddress(1), System.out::println, System.out::println);

        assertEquals(t1,t1);
        assertNotEquals(t1,t2);
        assertNotEquals(t1,"test");

        t1.toString();
        t2.toString();

        t1.get();
        try {
            t1.get();
            fail();
        } catch (Exception ignored){}

        assertEquals((Long) 0L, t1.timesUsed());
        assertNotEquals(t1.hashCode(),t2.hashCode());
    }
}
