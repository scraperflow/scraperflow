package scraper.nodes.core.functional;

import junitparams.JUnitParamsRunner;
import org.junit.runner.RunWith;
import scraper.nodes.core.test.annotations.Functional;
import scraper.nodes.core.test.helper.FunctionalTest;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;


@RunWith(JUnitParamsRunner.class)
public class DateNodeTest extends FunctionalTest {

    @Functional(DateNode.class)
    public Object[] emptyGroupsCapture0() {
        String min = "yyyy-MM-dd'T'HH:mm";
        SimpleDateFormat parsed = new SimpleDateFormat(min);

        String date = parsed.format(new Date());

        Map nodeInput = Map.of("dateFormat", min);
        Map flowInput = Map.of();
        Map flowOutput = Map.of("date", date);
        return new Object[]{ nodeInput, flowInput, flowOutput };
    }
}
