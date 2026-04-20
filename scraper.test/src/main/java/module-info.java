import scraper.api.Addon;
import scraper.api.Node;

import scraper.nodes.test.*;

open module scraper.test {
    requires scraper.api;
    requires scraper.core;
    requires scraper.core.parsers;
    exports scraper.test;
    exports scraper.nodes.test;

    provides Addon with TestAddon;
    provides Node with SimpleIO
            , scraper.nodes.test.Exception
            , Simplest
            , IntConsumer
            , SimpleLog
            , Put
            , scraper.nodes.test.Simple
            , scraper.nodes.test.v2.Simple
            , scraper.nodes.test.v3.Simple
            , Assert
            , Echo
            , SimpleListGoTo
            , ReadFileDummy
            , BadJsonDefaults
            , SimpleFunctional
            , File
            , Io
            , Sleep
            , SimpleFlowTest
            , ComplexFlow
            , TwoInputs
            , Goto;
}