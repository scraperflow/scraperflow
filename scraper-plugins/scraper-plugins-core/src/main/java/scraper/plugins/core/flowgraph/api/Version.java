package scraper.plugins.core.flowgraph.api;


import scraper.annotations.NotNull;

/**
 * @since 1.0.0
 */
public @interface Version {
    /** Node version the control/data flow implementation is meant for */
    @NotNull String value();
}

