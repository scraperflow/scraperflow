package scraper.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.reflect.TypeToken;
import org.junit.Test;
import scraper.annotations.node.Argument;
import scraper.annotations.node.FlowKey;
import scraper.api.exceptions.NodeException;
import scraper.api.flow.FlowMap;
import scraper.api.node.TypesafeObject;
import scraper.core.MapKey;
import scraper.core.Template;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
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
    private Template<String> simpleTemplate = new Template<>(){};
    private String simpleTemplateEval = "static";


    // expect to fail string template
    @FlowKey(mandatory = true)
    @TranslationInput(jsonValue = "\"fail\"", fail = true)
    private Template<String> failTemplate = new Template<>(){};
    private String failTemplateEval = "static";


    // template map static eval
    @FlowKey(mandatory = true)
    @TranslationInput(jsonValue = "{\"key\":\"value\"}")
    private Template<Map<String, String>> mapTemplate = new Template<>(){};
    private Consumer<Map<String, String>> mapTemplateCheck = o -> {
        if(!"value".equalsIgnoreCase(o.get("key"))) throw new IllegalStateException("Bad template evaluation");
    };


    // template map args eval
    @FlowKey(mandatory = true)
    @TranslationInput(jsonValue = "{\"key\":\"{valFromArgs}\"}")
    private Template<Map<String, String>> mapArgsTemplate = new Template<>(){};
    private Map<String, Object> mapArgsTemplateArgs = Map.of("valFromArgs", "evalKey");
    private Consumer<Map<String, String>> mapArgsTemplateCheck = o -> {
        if(!"evalKey".equalsIgnoreCase(o.get("key"))) throw new IllegalStateException("Bad template evaluation");
    };

    // template eval object with objects
    @FlowKey
    @TranslationInput(jsonValue = "{\"aList\":\"{evalToListObject}\"}")
    private Template<Object> evalToList = new Template<>(){};
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
    private Template<String> optionalBaseTemplateEval = new Template<>(){};
    private Map<String, Object> optionalBaseTemplateEvalArgs = Map.of("optTemplate", "output");
    private String optionalBaseTemplateEvalEval = "output";


    // optional null template evaluation
    @FlowKey
    @TranslationInput
    private Template<String> nullTemplate = new Template<>(){};
    private String nullTemplateEval = null;


    // optional missing template args
    @FlowKey(defaultValue = "\"{missingTemplate}\"")
    @TranslationInput(fail = true)
    private Template<String> missingTemplate = new Template<>(){};


    // map with template and actual type are mixed
    @FlowKey(defaultValue = "{\"actual\": 4, \"replaced\": \"{id}\"}")
    @TranslationInput
    private Template<Map<String, Integer>> multiTemplate = new Template<>(){};
    private Map<String, Object> multiTemplateArgs = Map.of("id", 2);
    private Consumer<Map<String, Integer>> multiTemplateCheck = o -> {
        Integer actual = 4;
        if(!actual.equals(o.get("actual"))) throw new IllegalStateException("Bad template evaluation");
        Integer replaced = 2;
        if(!replaced.equals(o.get("replaced"))) throw new IllegalStateException("Bad template evaluation");
    };


    // map bad complex type evaluation
    @FlowKey(defaultValue = "{\"actual\": 4, \"replaced\": \"{id}\"}")
    @TranslationInput(fail = true)
    private Template<Map<String, Integer>> badMultiTemplate = new Template<>(){};
    private Map<String, Object> badMultiTemplateArgs = Map.of("id", "test");


    // ====================
    // MapKey tests
    // ====================

    // simple MapKey usage
    @FlowKey(mandatory = true)
    @TranslationInput(jsonValue = "\"@thiskey\"")
    private MapKey<String> mapKeyString = new MapKey<>(){};
    private Map<String, Object> mapKeyStringArgs = Map.of("@thiskey", "hello");
    private String mapKeyStringEval = "hello";


    // missing key in args
    @FlowKey(mandatory = true)
    @TranslationInput(jsonValue = "\"@thiskey\"", fail = true)
    private MapKey<String> mapKeyStringMissing = new MapKey<>(){};
    private String mapKeyStringMissingEval = "hello";


    // base value for missing value @key
    @FlowKey(mandatory = true, defaultValue = "\"@nokey\"")
    private MapKey<Integer> baseMapKey = new MapKey<Integer>(){}.base(() -> 42);
    private Integer baseMapKeyEval = 42;


    // generic type safe mapKey usage with base
    @FlowKey(defaultValue = "\"@runtimeList\"")
    @TranslationInput
    private MapKey<List<Integer>> complexMapKey = new MapKey<List<Integer>>(){}.base(TypesafeAggregateList::new);
    private BiConsumer<MapKey<List<Integer>>, FlowMap> complexMapKeyCheck = (o, flow) -> {
        try {
            List<Integer> list = o.eval(flow);
            list.add(100);
            // ok
        } catch (NodeException e) {
            throw new IllegalStateException(e);
        }
    };


    // mapKey expects a type-safe implementation at key
    @FlowKey(defaultValue = "\"@runtimeList\"")
    @TranslationInput(fail = true)
    private MapKey<List<Integer>> noTypeMapKey = new MapKey<List<Integer>>(){}.base(TypesafeAggregateList::new);
    private BiConsumer<MapKey<List<Integer>>, FlowMap> noTypeMapKeyCheck = (o, flow) -> {
        // non-type-safe list
        List<Object> list = new ArrayList<>();
        flow.put("@runtimeList", list);
        try {
            List<Integer> typesafeList = o.eval(flow);
        } catch (NodeException e) {
            throw new IllegalStateException(e);
        }
    };


    // mapKey got a bad type-safe implementation at key
    @FlowKey(defaultValue = "\"@runtimeList\"")
    @TranslationInput(fail = true)
    private MapKey<List<Integer>> badTypeMapKey = new MapKey<List<Integer>>(){}.base(TypesafeAggregateList::new);
    private BiConsumer<MapKey<List<Integer>>, FlowMap> badTypeMapKeyCheck = (o, flow) -> {
        // bad generic type from another node
        List<String> list = new TypesafeAggregateStringList();
        list.add("a string");
        flow.put("@runtimeList", list);

        // evaluate the list with a bad type in it
        try {
            List<Integer> typesafeList = o.eval(flow);
        } catch (NodeException e) {
            throw new IllegalStateException(e);
        }
    };

    // raw type is important
    @FlowKey(defaultValue = "\"@rawList\"")
    @TranslationInput
    private final MapKey<List> mapKeyRaw = new MapKey<List>(){}.raw(ArrayList::new);
    private BiConsumer<MapKey<List>, FlowMap> mapKeyRawCheck = (o, flow) -> {
        // evaluate the list
        try {
            List rawList = o.eval(flow);
            //noinspection unchecked raw list
            rawList.add(120);
            flow.put(o.key, rawList);
        } catch (NodeException e) {
            throw new IllegalStateException(e);
        }

        if (!((List) flow.get("@rawList")).get(0).equals(120)) throw new IllegalStateException("Bad map key modification");
    };


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

        Object translatedValue = NodeUtil.getValueForField(field.getType(), field.get(this), jsonValue, globalValue,
                flowKey.mandatory(), flowKey.defaultValue(),
                isArgument, converter,
                args
        );

        //noinspection ConstantConditions NPE expected and catched
        if((expectedReturnValue != null || translatedValue != null) && !expectedReturnValue.equals(translatedValue))
            throw new IllegalStateException("Failed equality check");

        if(field.getType().isAssignableFrom(Template.class)) {
            Template<?> template = (Template<?>) field.get(this);
            Object templateEvaluation = template.eval(NodeUtil.flowOf(args));

            // static convention check for simple cases
            if(expectedTemplateEval != null && !expectedTemplateEval.equals(templateEvaluation)) {
                throw new IllegalStateException("Expected template evaluation does not match actual template evaluation");
            }

            // custom method check for complex cases
            Consumer<? super Object> checkMethod = null;
            try { // try to get check method by convention, if not, do not use it
                //noinspection unchecked
                checkMethod = (Consumer<? super Object>) getClass().getDeclaredField(field.getName()+"Check").get(this);
            } catch (Exception ignored) {}

            if(checkMethod != null) checkMethod.accept(templateEvaluation);
        }


        if(field.getType().isAssignableFrom(MapKey.class)) {
            MapKey<?> mapKey = (MapKey<?>) field.get(this);
            Object mapKeyEval = mapKey.eval(NodeUtil.flowOf(args));

            // static convention check for simple cases
            if(expectedTemplateEval != null && !expectedTemplateEval.equals(mapKeyEval)) {
                throw new IllegalStateException("Expected mapKey evaluation does not match actual mapKey evaluation");
            }

            // custom method check for complex cases
            BiConsumer<? super Object, ? super Object> checkMethod = null;
            try { // try to get check method by convention, if not, do not use it
                //noinspection unchecked
                checkMethod = (BiConsumer<? super Object, ? super Object>) getClass().getDeclaredField(field.getName()+"Check").get(this);
            } catch (Exception ignored) {}

            if(checkMethod != null) checkMethod.accept(mapKey, NodeUtil.flowOf(args));
        }
    }



    // type token has to be saved explicitly
    private class TypesafeAggregateList extends ArrayList<Integer> implements TypesafeObject {
        @Override public TypeToken<?> getType() { return new TypeToken<List<Integer>>(){}; }
    }

    private class TypesafeAggregateStringList extends ArrayList<String> implements TypesafeObject {
        @Override public TypeToken<?> getType() { return new TypeToken<List<String>>(){}; }
    }
}
