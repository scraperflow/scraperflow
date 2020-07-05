package scraper.nodes.core.functional;

import org.junit.jupiter.api.Test;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static scraper.test.FunctionalTest.runWith;


public class DateNodeTest {

    @Test
    public void emptyGroupsCapture0() {
        String min = "yyyy-MM-dd'T'HH:mm";
        SimpleDateFormat parsed = new SimpleDateFormat(min);

        String date = parsed.format(new Date());

        Map<?,?> nodeInput = Map.of("dateFormat", min);
        Map<?,?> flowInput = Map.of();
        Map<?,?> flowOutput = Map.of("date", date);
        runWith(DateNode.class, List.of(nodeInput, flowInput, flowOutput));
    }
}
