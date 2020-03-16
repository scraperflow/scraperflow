package scraper.nodes.unstable.pass.dialog;

import scraper.annotations.NotNull;
import scraper.annotations.node.FlowKey;
import scraper.annotations.node.Io;
import scraper.annotations.node.NodePlugin;
import scraper.api.flow.FlowMap;
import scraper.api.node.container.FunctionalNodeContainer;
import scraper.api.node.type.FunctionalNode;
import scraper.api.template.L;

import javax.swing.*;
import java.util.Arrays;

import static scraper.api.node.container.NodeLogLevel.INFO;

/**
 * Ensures that a key is set, either pre-set or by user input.
 * User input prefers console, otherwise pops up a graphical user prompt.
 */
@NodePlugin("0.1.0")
@Io
public final class UserInputNode implements FunctionalNode {

    /** Which key to ensure */
    @FlowKey(mandatory = true)
    private L<String> key = new L<>(){};

    /** Prompt message */
    @FlowKey(mandatory = true)
    private String prompt;

    /** Can hide echo for sensitive inputs */
    @FlowKey(defaultValue = "false")
    private Boolean hidden;

    /** If the key already exists, do nothing */
    @FlowKey(defaultValue = "true")
    private Boolean noPromptIfExists;

    @Override
    public void modify(@NotNull FunctionalNodeContainer n, @NotNull FlowMap o) {
        if (noPromptIfExists) {
            // return if output is present
            String loc = o.eval(key);
            if(o.get(loc).isPresent()) {
                if(!hidden) {
                    n.log(INFO, "{}: {}", o.eval(key), o.get(loc).get());
                } else {
                    n.log(INFO, "{} is present", o.eval(key));
                }
                return;
            }
        }

        final String input;

        if(hidden) {
            if( System.console() == null ) {
                final JPasswordField pf = new JPasswordField();
                input = JOptionPane.showConfirmDialog( null, pf, prompt,
                        JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE ) == JOptionPane.OK_OPTION
                        ? new String( pf.getPassword() ) : "";
                Arrays.fill(pf.getPassword(), (char) 0);
            } else {
                input = new String( System.console().readPassword( "%s> ", prompt ) );
            }
        } else {
            if( System.console() == null ) {
                final JTextField pf = new JTextField();
                input = JOptionPane.showConfirmDialog( null, pf, prompt,
                        JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE ) == JOptionPane.OK_OPTION
                        ? pf.getText() : "";
            } else {
                input = System.console().readLine("%s> ", prompt);
            }
        }

        o.output(key, input);
    }
}
