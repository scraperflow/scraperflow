import scraper.annotations.*;
import scraper.api.*;

import java.util.regex.Pattern;

/**
 * Extract times.
 */
@NodePlugin(value = "0.0.1")
public class ExtractTimes implements FunctionalNode {

    /** Element to output */
    @FlowKey T<String> content = new T<>(){};

    @FlowKey(defaultValue = "\"wc\"") L<String> wc = new L<>(){};
    @FlowKey(defaultValue = "\"wcp\"") L<String> wcp = new L<>(){};
    @FlowKey(defaultValue = "\"seq\"") L<String> seq = new L<>(){};
    @FlowKey(defaultValue = "\"seqgen\"") L<String> seqgen = new L<>(){};
    @FlowKey(defaultValue = "\"par\"") L<String> par = new L<>(){};
    @FlowKey(defaultValue = "\"pargen\"") L<String> pargen = new L<>(){};

    @Override
    public void modify(FunctionalNodeContainer n, FlowMap o) {
        String times = o.eval(content);

        o.output(wc, "NA");
        o.output(wcp, "NA");
        o.output(seq, "NA");
        o.output(seqgen, "NA");
        o.output(par, "NA");
        o.output(pargen, "NA");

        String[] toParse = times.split("(---)|(\n\n)");
        for (String s : toParse) {
            Pattern.compile("WC:\\sreal (.*?)\\s") .matcher(s).results().map(m -> m.group(1)).findFirst()
                    .ifPresent(c -> o.output(wc, c));

            Pattern.compile("WCP:\\sreal (.*?)\\s") .matcher(s).results().map(m -> m.group(1)).findFirst()
                    .ifPresent(c -> o.output(wcp, c));

            Pattern.compile("SEQ:\\sreal (.*?)\\s") .matcher(s).results().map(m -> m.group(1)).findFirst()
                    .ifPresent(c -> o.output(seq, c));

            Pattern.compile("SEQGEN:\\sreal (.*?)\\s") .matcher(s).results().map(m -> m.group(1)).findFirst()
                    .ifPresent(c -> o.output(seqgen, c));

            Pattern.compile("PAR:\\sreal (.*?)\\s") .matcher(s).results().map(m -> m.group(1)).findFirst()
                    .ifPresent(c -> o.output(par, c));

            Pattern.compile("PARGEN:\\sreal (.*?)\\s") .matcher(s).results().map(m -> m.group(1)).findFirst()
                    .ifPresent(c -> o.output(pargen, c));
        }
    }
}
