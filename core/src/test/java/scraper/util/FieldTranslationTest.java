package scraper.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import scraper.annotations.node.Argument;
import scraper.annotations.node.FlowKey;
import scraper.api.flow.impl.FlowMapImpl;
import scraper.api.node.Address;
import scraper.api.template.T;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;


/**
 * Test cases of possible annotation combinations on fields concerning scrape definition
 */
@SuppressWarnings("unused") // reflective access by convention
public class FieldTranslationTest {

    // simple mandatory usage
    @FlowKey(mandatory = true)
    @TranslationInput( jsonValue = "\"mandatory!\"" )
    private String mandatoryString;
    private String mandatoryStringExpected = "mandatory!";

    // newline expected
    @FlowKey(mandatory = true)
    @TranslationInput( jsonValue = "\"\\n\"" )
    private String newlineString;
    private String newlineStringExpected = "\n";


    // mandatory json missing
    @FlowKey(mandatory = true)
    @TranslationInput( fail = true )
    private String mandatoryStringMissing;


    // enums are supported, too
    @FlowKey(mandatory = true)
    @TranslationInput(jsonValue = "\"VALUE_A\"")
    private SomeEnum mandatoryEnum;
    private SomeEnum mandatoryEnumExpected = SomeEnum.VALUE_A;


    // bad enum values cause exception
    @FlowKey(mandatory = true)
    @TranslationInput(jsonValue = "\"VALUE_AB\"", fail = true)
    private SomeEnum mandatoryBadJsonEnum;


    // enums are case sensitive
    @FlowKey(mandatory = true)
    @TranslationInput(jsonValue = "\"VALUE_a\"", fail = true)
    private SomeEnum mandatoryBadCaseEnum;


    // enum arg evaluation
    @FlowKey(mandatory = true) @Argument
    @TranslationInput(jsonValue = "\"{enumArg}\"")
    private SomeEnum argEnum;
    private Map<String, Object> argEnumArgs = Map.of("enumArg", "VALUE_A");
    private SomeEnum argEnumExpected = SomeEnum.VALUE_A;


    // argument usage, no evaluation
    @FlowKey(mandatory = true) @Argument
    @TranslationInput(jsonValue = "100")
    private Integer argNoInteger;
    private Integer argNoIntegerExpected = 100;


    // argument usage, string evaluation no args
    @FlowKey(mandatory = true) @Argument
    @TranslationInput(jsonValue = "\"100\"")
    private Integer IntegerString;
    private Integer IntegerStringExpected = 100;


    // argument usage, string evaluation via args
    @FlowKey(mandatory = true) @Argument
    @TranslationInput(jsonValue = "\"{intArg}\"")
    private Integer argInteger;
    private Map<String, Object> argIntegerArgs = Map.of("intArg", 100);
    private Integer argIntegerExpected = 100;


    // Integer value passes as a Long type
    @FlowKey(mandatory = true) @Argument
    @TranslationInput(jsonValue = "100")
    private Long integerAsLong;
    private Long integerAsLongExpected = 100L;


    // argument usage, evaluation via args, int overflow
    @FlowKey(mandatory = true) @Argument
    @TranslationInput(jsonValue = "\"{intArg}\"", fail = true)
    private Integer argIntegerOverflow;
    private Map<String, Object> argIntegerOverflowArgs = Map.of("intArg", (long) Integer.MAX_VALUE +1L);

    // optional argument
    @FlowKey(defaultValue = "\"\\\\R\"") @Argument
    @TranslationInput
    private String optionalStringArgs;
    private String optionalStringArgsExpected = "\\R";


    // optional enum
    @FlowKey(defaultValue = "\"VALUE_A\"")
    @TranslationInput
    private SomeEnum optionalEnum;
    private SomeEnum optionalEnumExpected = SomeEnum.VALUE_A;


    // Empty string replacement is treated as null value in JSON
    @FlowKey(mandatory = true) @Argument
    @TranslationInput(jsonValue = "\"{empty}\"")
    private String emptyReplace;
    private Map<String, Object> emptyReplaceArgs = Map.of("empty", "");
    private String emptyReplaceExpected = null;


    // ====================
    // Template tests
    // ====================

    // simple string template
    @FlowKey(mandatory = true)
    @TranslationInput(jsonValue = "\"static\"")
    private T<String> simpleTemplate = new T<>(){};
    private String simpleTemplateEval = "static";


    // expect to fail string template
    @FlowKey(mandatory = true)
    @TranslationInput(jsonValue = "\"fail\"", fail = true)
    private T<String> failTemplate = new T<>(){};
    private String failTemplateEval = "static";

    // template list static eval
    @FlowKey(mandatory = true)
    @TranslationInput(jsonValue = "[1,2,3]")
    private T<List<Integer>> listTemplate = new T<>(){};
    private Consumer<List<Integer>> listTemplateCheck = o -> {
        if(o.size() != 3) throw new IllegalStateException("Bad");
        if(o.get(2) != 3) throw new IllegalStateException("Bad");
        if(o.get(1) != 2) throw new IllegalStateException("Bad");
        if(o.get(0) != 1) throw new IllegalStateException("Bad");
    };

    // template list address eval
    @FlowKey(mandatory = true)
    @TranslationInput(jsonValue = "[\"adr-1\", \"adr-2\"]")
    private T<List<Address>> listAddressTemplate = new T<>(){};
    @SuppressWarnings("ConstantConditions") // check for correct class, not constant
    private Consumer<List<Address>> listAddressTemplateCheck = o -> {
        if(o.size() != 2) throw new IllegalStateException("Bad");
        if(!(o.get(0) instanceof Address)) throw new IllegalStateException("Not an address");
    };

    // template map static eval
    @FlowKey(mandatory = true)
    @TranslationInput(jsonValue = "{\"key\":\"value\"}")
    private T<Map<String, String>> mapTemplate = new T<>(){};
    private Consumer<Map<String, String>> mapTemplateCheck = o -> {
        if(!"value".equalsIgnoreCase(o.get("key"))) throw new IllegalStateException("Bad template evaluation");
    };

    // only strings as keys are allowed, currently not checked
    // TODO warn if anything other than Strings are used
//    @FlowKey(mandatory = true)
//    @TranslationInput(jsonValue = "{\"address\":\"?\"}")
//    private T<Map<Address, String>> mapAddressTemplate = new T<>(){};
//    @SuppressWarnings("ConstantConditions") // check if key is address and not a string
//    private Consumer<Map<Address, String>> mapAddressTemplateCheck = o -> {
//        if (!(o.keySet().iterator().next() instanceof Address)) throw new IllegalStateException("Not an address");
//    };


    // template map args eval
    @FlowKey(mandatory = true)
    @TranslationInput(jsonValue = "{\"key\":\"{valFromArgs}\"}")
    private T<Map<String, String>> mapArgsTemplate = new T<>(){};
    private Map<String, Object> mapArgsTemplateArgs = Map.of("valFromArgs", "evalKey");
    private Consumer<Map<String, String>> mapArgsTemplateCheck = o -> {
        if(!"evalKey".equalsIgnoreCase(o.get("key"))) throw new IllegalStateException("Bad template evaluation");
    };

    // template eval object with objects
    @FlowKey
    @TranslationInput(jsonValue = "{\"aList\":\"{evalToListObject}\"}")
    private T<Object> evalToList = new T<>(){};
    private Map<String, Object> evalToListArgs = Map.of("evalToListObject", List.of("e1","e1"));
    private Consumer<Map<String, Object>> evalToListCheck = o -> {
        if(!(o.get("aList") instanceof List)) throw new IllegalStateException("Evaluated object is not a list anymore: " + o.get("aList").getClass());
    };

    // optional
    @FlowKey
    @TranslationInput(jsonValue = "null")
    private String optionalNull;
    private String optionalNullExpected = null;


    // optional fail
    @FlowKey
    @TranslationInput(jsonValue = "null", fail = true)
    private String optionalNullFail;
    private String optionalNullFailExpected = "null";


    // optional default value
    @FlowKey(defaultValue = "\"defVal\"")
    @TranslationInput
    private String optionalDefault;
    private String optionalDefaultExpected = "defVal";


    // optional string values should be json strings
    @FlowKey(defaultValue = "defVal")
    @TranslationInput(fail = true)
    private String optionalDefaultFail;


    // given value overwriting optional value
    @FlowKey(defaultValue = "\"default\"")
    @TranslationInput(jsonValue = "\"given\"")
    private String optionalDefined;
    private String optionalDefinedExpected = "given";


    // optional template evaluation
    @FlowKey(defaultValue = "\"{optTemplate}\"")
    @TranslationInput
    private T<String> optionalBaseTemplateEval = new T<>(){};
    private Map<String, Object> optionalBaseTemplateEvalArgs = Map.of("optTemplate", "output");
    private String optionalBaseTemplateEvalEval = "output";


    // optional null template evaluation
    @FlowKey
    @TranslationInput(fail = true)
    private T<String> nullTemplate = new T<>(){};
    private String nullTemplateEval = null;


    // optional missing template args
    @FlowKey(defaultValue = "\"{missingTemplate}\"")
    @TranslationInput(fail = true)
    private T<String> missingTemplate = new T<>(){};


    // map with template and actual type are mixed
    @FlowKey(defaultValue = "{\"actual\": 4, \"replaced\": \"{id}\"}")
    @TranslationInput
    private T<Map<String, Integer>> multiTemplate = new T<>(){};
    private Map<String, Object> multiTemplateArgs = Map.of("id", 2);
    private Consumer<Map<String, Integer>> multiTemplateCheck = o -> {
        Integer actual = 4;
        if(!actual.equals(o.get("actual"))) throw new IllegalStateException("Bad template evaluation");
        Integer replaced = 2;
        if(!replaced.equals(o.get("replaced"))) throw new IllegalStateException("Bad template evaluation");
    };


    // map bad complex type evaluation
    // this is will actually not throw an Exception, there is no runtime type checking
//    @FlowKey(defaultValue = "{\"actual\": 4, \"replaced\": \"{id}\"}")
//    @TranslationInput(fail = true)
//    private T<Map<String, Integer>> badMultiTemplate = new T<>(){};
//    private Map<String, Object> badMultiTemplateArgs = Map.of("id", "test");

    static class TestInput {
        private final Field field; private final FlowKey flowKey; private final TranslationInput input;
        public TestInput(Field field, FlowKey flowKey, TranslationInput input) {
            this.field = field; this.flowKey = flowKey; this.input = input;
        }
    }

    public static Stream<TestInput> fieldTranslationProvider() throws Exception {
        return Arrays.stream(FieldTranslationTest.class.getDeclaredFields())
                .filter(f -> f.getAnnotation(TranslationInput.class) != null)
                .map(f -> {
                    TranslationInput testInput = f.getAnnotation(TranslationInput.class);
                    FlowKey flowKey = f.getAnnotation(FlowKey.class);
                    return new TestInput(f, flowKey, testInput);
                });
    }

    @SuppressWarnings("unchecked")// try to get argument map by convention, if not, use empty instead
    @ParameterizedTest
    @MethodSource("fieldTranslationProvider")
    public void testField(TestInput arg) throws Exception {
        Field field = arg.field;
        FlowKey flowKey = arg.flowKey;
        TranslationInput testInput = arg.input;

        Runnable run = () -> {
            try {
                System.out.println("Testing field '" + field.getName() + "'");

                ObjectMapper mapper = new ObjectMapper();

                Object jsonValue =
                        !testInput.jsonValue().equalsIgnoreCase("") ?
                                mapper.readValue(testInput.jsonValue(), Object.class) :
                                null;

                Object globalValue =
                        !testInput.globalValue().equalsIgnoreCase("") ?
                                mapper.readValue(testInput.globalValue(), Object.class) :
                                null;


                Map<String, Object> args = Map.of();
                try { // try to get argument map by convention, if not, use empty instead
                    args = (Map<String, Object>) getClass().getDeclaredField(field.getName()+"Args").get(this);
                } catch (Exception ignored) {}


                Object expectedReturnValue = null;
                try { // try to get expected return value by convention. default is null
                    expectedReturnValue = getClass().getDeclaredField(field.getName()+"Expected").get(this);
                } catch (Exception ignored) {}

                Object expectedTemplateEval = null;
                try { // try to get expected eval value by convention. default is null
                    expectedTemplateEval = getClass().getDeclaredField(field.getName()+"Eval").get(this);
                } catch (Exception ignored) {}

                Argument ann = field.getAnnotation(Argument.class);
                boolean isArgument = ann != null;
                Class<?> converter = (ann != null ? ann.converter() : null);

                Object translatedValue = NodeUtil.getValueForField(this, field, jsonValue, globalValue,
                        flowKey.mandatory(), flowKey.defaultValue(),
                        isArgument, converter,
                        args
                );

                if((expectedReturnValue != null || translatedValue != null)) {
                    if (expectedReturnValue != null) {
                        if(!expectedReturnValue.equals(translatedValue)){
                            throw new IllegalStateException("Failed equality check: " +expectedReturnValue + "  !=  " +translatedValue);
                        }
                    }
                }

                if(field.getType().isAssignableFrom(T.class)) {
                    T<?> template = (T<?>) field.get(this);
                    Object templateEvaluation = FlowMapImpl.origin(args).eval(template);

                    // static convention check for simple cases
                    if(expectedTemplateEval != null && (!expectedTemplateEval.equals(templateEvaluation))) {
                        throw new IllegalStateException("Expected template evaluation does not match actual template evaluation");
                    }

                    // custom method check for complex cases
                    Consumer<? super Object> checkMethod = null;
                    try { // try to get check method by convention, if not, do not use it
                        checkMethod = (Consumer<? super Object>) getClass().getDeclaredField(field.getName()+"Check").get(this);
                    } catch (Exception ignored) {}

                    if(checkMethod != null) //noinspection OptionalGetWithoutIsPresent check method should check for null value
                        checkMethod.accept(templateEvaluation);
                }
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        };

        if(testInput.fail()) {
            Assertions.assertThrows(Exception.class, run::run);
        } else {
            run.run();
        }
    }
}
