package scraper.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import scraper.annotations.node.Argument;
import scraper.annotations.node.FlowKey;
import scraper.api.flow.FlowMap;
import scraper.api.flow.impl.FlowMapImpl;
import scraper.api.reflect.T;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;


/**
 * Test cases of possible annotation combinations on fields concerning .scrape definition
 */
@SuppressWarnings("unused") // reflective access by convention
public class FieldTranslationTest {

    // simple mandatory usage
    @FlowKey(mandatory = true)
    @TranslationInput( jsonValue = "\"mandatory!\"" )
    private String mandatoryString;
    private String mandatoryStringExpected = "mandatory!";


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


    // template map static eval
    @FlowKey(mandatory = true)
    @TranslationInput(jsonValue = "{\"key\":\"value\"}")
    private T<Map<String, String>> mapTemplate = new T<>(){};
    private Consumer<Map<String, String>> mapTCheck = o -> {
        if(!"value".equalsIgnoreCase(o.get("key"))) throw new IllegalStateException("Bad template evaluation");
    };


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
    @TranslationInput
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
    private Consumer<Map<String, Integer>> multiTCheck = o -> {
        Integer actual = 4;
        if(!actual.equals(o.get("actual"))) throw new IllegalStateException("Bad template evaluation");
        Integer replaced = 2;
        if(!replaced.equals(o.get("replaced"))) throw new IllegalStateException("Bad template evaluation");
    };


    // map bad complex type evaluation
    @FlowKey(defaultValue = "{\"actual\": 4, \"replaced\": \"{id}\"}")
    @TranslationInput(fail = true)
    private T<Map<String, Integer>> badMultiTemplate = new T<>(){};
    private Map<String, Object> badMultiTemplateArgs = Map.of("id", "test");



    @Test
    public void fieldTranslationTest() throws Exception {
        for (Field field : getClass().getDeclaredFields()) {
            TranslationInput testInput = field.getAnnotation(TranslationInput.class);
            FlowKey flowKey = field.getAnnotation(FlowKey.class);
            if(testInput == null) continue;

            try {
                testField(field, flowKey, testInput);
            } catch (Exception e) {
                if(!testInput.fail()) throw e;
                else continue;
            }

            if(testInput.fail()) throw new IllegalStateException("Field was expected to fail evaluation: " + field.getName());
        }
    }

    private void testField(Field field, FlowKey flowKey, TranslationInput testInput) throws Exception {
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
            //noinspection unchecked
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
                flowKey.mandatory(), flowKey.defaultValue(), flowKey.output(),
                isArgument, converter,
                args
        );

        //noinspection ConstantConditions NPE expected and catched
        if((expectedReturnValue != null || translatedValue != null) && !expectedReturnValue.equals(translatedValue))
            throw new IllegalStateException("Failed equality check");

        if(field.getType().isAssignableFrom(T.class)) {
            T<?> template = (T<?>) field.get(this);
            Optional<?> templateEvaluation = FlowMapImpl.origin(args).evalMaybe(template);

            // static convention check for simple cases
            if(expectedTemplateEval != null && (templateEvaluation.isEmpty() || !expectedTemplateEval.equals(templateEvaluation.get()))) {
                throw new IllegalStateException("Expected template evaluation does not match actual template evaluation");
            }

            // custom method check for complex cases
            Consumer<? super Object> checkMethod = null;
            try { // try to get check method by convention, if not, do not use it
                //noinspection unchecked
                checkMethod = (Consumer<? super Object>) getClass().getDeclaredField(field.getName()+"Check").get(this);
            } catch (Exception ignored) {}

            if(checkMethod != null) //noinspection OptionalGetWithoutIsPresent check method should check for null value
                checkMethod.accept(templateEvaluation.get());
        }
    }
}
