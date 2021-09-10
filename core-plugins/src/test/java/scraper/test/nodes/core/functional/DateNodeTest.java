package scraper.test.nodes.core.functional;

import org.junit.jupiter.api.Test;
import scraper.nodes.core.functional.Date;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

import static scraper.test.FunctionalTest.runWith;


public class DateNodeTest {

    @Test
    public void emptyGroupsCapture0() {
        String min = "yyyy-MM-dd'T'HH:mm";
        SimpleDateFormat parsed = new SimpleDateFormat(min);

        String date = parsed.format(new java.util.Date());

        Map<?,?> nodeInput = Map.of("dateFormat", min, "put", "date");
        Map<?,?> flowInput = Map.of();
        Map<?,?> flowOutput = Map.of("date", date);
        runWith(Date.class, List.of(nodeInput, flowInput, flowOutput));
    }
}
