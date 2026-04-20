package scraper.test.core.converter;

import org.junit.jupiter.api.Test;
import scraper.api.ValidationException;
import scraper.core.converter.StringToClassConverter;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests the default StringToClassConverter
 */
public class StringToClassConverterTest {

    @Test
    public void booleansTest() throws Exception {
        String[] input = new String[]{"true", "false","TRUE", "FALSE", "falSE","tRuE"};
        for (String bool : input) {
            Class<?> target = Boolean.class;
            Object convert = StringToClassConverter.convert(bool, target);

            assertTrue(Boolean.class.isAssignableFrom(Objects.requireNonNull(convert).getClass()));
            assertEquals(Boolean.valueOf(bool), convert);
        }
    }

    @Test
    public void intTest() throws Exception {
        String[] input = new String[]{"-1", "0","1"};
        for (String i : input) {
            Class<?> target = Integer.class;
            Object convert = StringToClassConverter.convert(i, target);

            assertTrue(Integer.class.isAssignableFrom(Objects.requireNonNull(convert).getClass()));
            assertEquals(Integer.valueOf(i), convert);
        }
    }

    @Test
    public void longTest() throws Exception {
        String[] input = new String[]{"-1", "0","1", "2147483648"};
        for (String i : input) {
            Class<?> target = Long.class;
            Object convert = StringToClassConverter.convert(i, target);

            assertTrue(Long.class.isAssignableFrom(Objects.requireNonNull(convert).getClass()));
            assertEquals(Long.valueOf(i), convert);
        }
    }

    @Test
    public void doubleTest() throws Exception {
        assertEquals(((Double) 1.0), StringToClassConverter.convert("1", Double.class));
        assertEquals(((Double) 0.0), StringToClassConverter.convert("0", Double.class));
        assertEquals(((Double) 1.5), StringToClassConverter.convert("1.5", Double.class));
        assertEquals(((Double) 1.2345), StringToClassConverter.convert("1.2345", Double.class));
    }

    @Test
    public void longOutOfBoundsTest() {
        assertThrows(ValidationException.class, () -> StringToClassConverter.convert("9223372036854775808", Long.class));
    }

    @Test
    public void intOutOfBoundsTest() {
        assertThrows(ValidationException.class, () -> StringToClassConverter.convert("2147483648", Integer.class));
    }

    @Test
    public void nullJsonTest() throws Exception {
        String nullJson = "null";
        Class<?> target = Object.class;
        Object convert = StringToClassConverter.convert(nullJson, target);
        assertNull(convert);
    }

    @SuppressWarnings("ConstantConditions") // testing the constant condition
    @Test
    public void nullStringTest() throws Exception {
        Class<?> target = Object.class;
        Object convert = StringToClassConverter.convert(null, target);
        assertNull(convert);
    }

    @Test
    public void enumTest() throws Exception {
        String enumString = "test";
        Class<?> target = HelloEnum.class;
        Object convert = StringToClassConverter.convert(enumString, target);
        assertEquals(convert, HelloEnum.test);
    }

    @Test
    public void enumFailTest() {
        assertThrows(ValidationException.class, () -> {
            String enumString = "Test";
            Class<?> target = HelloEnum.class;
            Object convert = StringToClassConverter.convert(enumString, target);
            assertEquals(convert, HelloEnum.test);
        });
    }

    @Test
    public void stringFuzzer() {
        Class<?>[] targetClasses = new Class[]{void.class, Object.class, Boolean.class, Integer.class, Double.class, Long.class, Enum.class, String.class};
        Random r = new Random();

        for (int i = 0; i < 10000; i++) {
            // get random target class
            Class<?> targetClass = targetClasses[r.nextInt(targetClasses.length)];

            // get random utf-8 string
            byte[] array = new byte[10];
            new Random().nextBytes(array);
            String generatedString = new String(array, StandardCharsets.UTF_8);

            // converting should only produce ValidationExceptions
            try {
                StringToClassConverter.convert(generatedString, targetClass);
            } catch (ValidationException ignored) {}
            catch (Exception e){
                System.err.println("Exception with generated String and target class");
                System.err.println(generatedString);
                System.err.println(targetClass);
                throw new IllegalStateException(e);
            }
        }
    }

    @Test
    public void testConstructorIsPrivate() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Constructor<StringToClassConverter> constructor = StringToClassConverter.class.getDeclaredConstructor();
        assertTrue(Modifier.isPrivate(constructor.getModifiers()));
        constructor.setAccessible(true);
        constructor.newInstance();
    }
}