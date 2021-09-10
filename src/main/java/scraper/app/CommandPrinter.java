package scraper.app;

import scraper.annotations.ArgsCommand;
import scraper.annotations.ArgsCommands;
import scraper.annotations.NotNull;
import scraper.api.Command;

import java.util.Arrays;
import java.util.List;
import java.util.ServiceLoader;
import java.util.stream.Collectors;

import static java.util.ServiceLoader.load;

public final class CommandPrinter {
    private CommandPrinter(){}

     static void collectAndPrintCommandLineArguments() {
         List<Command> parsers = load(Command.class).stream().map(ServiceLoader.Provider::get).collect(Collectors.toList());
         System.out.println(parsers.size());
         parsers.forEach(command -> {
             Arrays.stream(command.getClass().getAnnotations())
                     .filter(a -> a instanceof ArgsCommand)
                     .forEach(a -> printArgsCommand((ArgsCommand) a));
             Arrays.stream(command.getClass().getAnnotations())
                     .filter(a -> a instanceof ArgsCommands)
                     .forEach(as -> Arrays.stream(((ArgsCommands) as).value()).forEach(CommandPrinter::printArgsCommand));
         });
    }

     static void printArgsCommand(@NotNull final ArgsCommand argsCommand) {
        String val = argsCommand.value();
        String doc = argsCommand.doc();
        String example = argsCommand.example();


        System.out.println("-------------------");
        System.out.println(val);
        System.out.println();
        System.out.println("  "+ doc);
        System.out.println();
        System.out.println(" example usage");
        System.out.println();
        System.out.println("  "+ example);
        System.out.println();
        System.out.println();
    }
}
