import scraper.api.Command;
import scraper.api.Node;
import scraper.api.Hook;
import scraper.hooks.*;
import scraper.nodes.core.complex.*;
import scraper.nodes.core.example.AnnotationsExample;
import scraper.nodes.core.example.WordCount;
import scraper.nodes.core.flow.*;
import scraper.nodes.core.functional.*;
import scraper.nodes.core.io.*;
import scraper.nodes.core.os.*;
import scraper.nodes.core.stream.*;
import scraper.nodes.core.time.*;
import scraper.plugins.core.flowgraph.ControlFlowGraphGeneratorHook;
import scraper.plugins.core.pointerfree.PointerFreeHook;
import scraper.plugins.core.typechecker.TypeHook;

open module scraper.core.plugins {
    exports scraper.hooks;
    exports scraper.nodes.core.example;
    exports scraper.nodes.core.flow;
    exports scraper.nodes.core.functional;
    exports scraper.nodes.core.io;
    exports scraper.nodes.core.os;
    exports scraper.nodes.core.stream;
    exports scraper.nodes.core.time;
    exports scraper.nodes.core.complex;
    exports scraper.plugins.core.flowgraph.api;
    exports scraper.plugins.core.flowgraph;
    exports scraper.plugins.core.typechecker;


    requires transitive scraper.core;

    provides Command with ExitHook, NodeDependencyGeneratorHook, ControlFlowGraphGeneratorHook, TypeHook;

    provides Hook with
            ExitHook, NodeDependencyGeneratorHook, ControlFlowGraphGeneratorHook, TypeHook, PointerFreeHook;


    provides Node with
            WordCount, Sleep, Let, Join, LetIn
            , AnnotationsExample
            , FilterEmptyMap, FilterEmptyList, Fork, IfThenElse, JoinFlow, JoinSingle, Map, MapMap, Redirect, Stop
            , UnzipSingle ,Timestamp ,LongToString ,Hash ,Pad ,Date ,AggregateList ,StringContains ,StringToInt ,Sum
                ,Unzip ,JsonObject ,StringJoin ,Base64Encode ,CleanJsonObject ,StringReplace ,ObjectToJsonString
                ,ListDiff ,ListSort ,IntToString ,ListDistinct, ListReverse ,Zip ,MergeMap ,ListSlice ,ListEmpty ,BooleanOp
                ,ToLowerCase ,FlattenStringList ,FlattenList ,Log ,StringSplit ,ContainedInCollection ,Base64Decode
                ,RemoveKey ,StringEqualityCheck ,JsonStringToObject
            ,WriteLineToFile ,Ping ,FileExists ,HttpRequest ,ReadFileAsStream ,ListFiles ,PathGlobFile
                ,Output ,ReadChunkFile ,ReadFile
            ,Exec, ExecStr
            ,Regex ,SingleInput ,IntRange ,ExecStream ,Input
            ,Periodic ,TimestampDifference ,Timer
    ;
}
