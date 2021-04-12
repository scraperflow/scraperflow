package scraper.api.exceptions;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

public class ExceptionTest {
    @Test
    public void simpleUsageTest() {
        List.of(
                new TemplateException("test"),
                new TemplateException(new Exception(), "test"),
                new ValidationException("test"),
                new ValidationException(new Exception(), "test")
        ).forEach(Assertions::assertNotNull);
    }
}
