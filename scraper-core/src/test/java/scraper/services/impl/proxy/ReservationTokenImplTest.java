package scraper.services.impl.proxy;

import org.junit.Assert;
import org.junit.Test;
import scraper.api.service.impl.proxy.ReservationTokenImpl;

import java.net.InetSocketAddress;
import java.util.UUID;

import static org.junit.Assert.fail;

public class ReservationTokenImplTest {

    @Test
    public void equalsTest() {
        ReservationTokenImpl t1 = new ReservationTokenImpl(
                UUID.randomUUID(),1L,0L,
                null, System.out::println, System.out::println);

        ReservationTokenImpl t2 = new ReservationTokenImpl(
                UUID.randomUUID(),1L,0L,
                new InetSocketAddress(1), System.out::println, System.out::println);

        Assert.assertEquals(t1,t1);
        Assert.assertNotEquals(t1,t2);
        Assert.assertNotEquals(t1,"test");

        //noinspection ResultOfMethodCallIgnored
        t1.toString();
        //noinspection ResultOfMethodCallIgnored
        t2.toString();

        t1.get();
        try {
            t1.get();
            fail();
        } catch (Exception ignored){}

        Assert.assertEquals((Long) 0L, t1.timesUsed());
        Assert.assertNotEquals(t1.hashCode(),t2.hashCode());
    }
}
