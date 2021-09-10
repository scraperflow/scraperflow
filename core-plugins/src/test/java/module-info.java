import scraper.api.Node;
import scraper.test.plugins.core.typechecker.helper.TestRec;
import scraper.test.plugins.core.typechecker.helper.TestRecExtract;

open module scraper.test.core.plugins {
    requires org.junit.jupiter.api;
    requires scraper.api;
    requires scraper.core;
    requires scraper.test;
    requires scraper.core.plugins;
    requires org.junit.jupiter.params;

    provides Node with TestRec, TestRecExtract;
}