// TODO #29 needs to be open to read (test) resource files until gradle decides to add first-class support for modules
// gradle/gradle-test-resources-fix.gradle is related to this
open module scraper.utils {
    requires scraper.annotations;

    exports scraper.utils;
}