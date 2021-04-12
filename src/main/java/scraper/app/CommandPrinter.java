package scraper.app;

import io.github.classgraph.*;
import scraper.annotations.ArgsCommand;
import scraper.annotations.ArgsCommands;
import scraper.annotations.NotNull;

public final class CommandPrinter {
    private CommandPrinter(){}

     static void collectAndPrintCommandLineArguments() {
        try (ScanResult scanResult = new ClassGraph().enableAllInfo()
                .acceptPackages("scraper")
                .scan()) {
            for (ClassInfo routeClassInfo : scanResult.getClassesWithAnnotation(ArgsCommand.class.getName())) {
                printArgsCommand(routeClassInfo.getAnnotationInfo(ArgsCommand.class.getName()));
            }

            for (ClassInfo routeClassInfo : scanResult.getClassesWithAnnotation(ArgsCommands.class.getName())) {
                AnnotationInfoList argsCommands = (routeClassInfo.getAnnotationInfoRepeatable(ArgsCommand.class.getName()));
                for (AnnotationInfo argsCommand : argsCommands) {
                    printArgsCommand(argsCommand);
                }
            }
        }
    }

     static void printArgsCommand(@NotNull final AnnotationInfo argsCommand) {
        String val = (String) argsCommand.getParameterValues().getValue("value");
        String doc = (String) argsCommand.getParameterValues().getValue("doc");
        String example = (String) argsCommand.getParameterValues().getValue("example");


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
