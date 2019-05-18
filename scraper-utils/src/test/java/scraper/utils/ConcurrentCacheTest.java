package scraper.utils;

import org.junit.Assert;
import org.junit.Test;

public class ConcurrentCacheTest {
    @Test
    public void simpleLimitTest() {
        ConcurrentCacheHashMap<Object, Object> map = new ConcurrentCacheHashMap<>();

        for (int i = 0; i < 1002; i++) {
            map.put(i,i);
        }

        Assert.assertEquals(1, map.size());
    }

}
