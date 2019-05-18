package scraper.core;

import org.springframework.plugin.metadata.AbstractMetadataBasedPlugin;
import org.springframework.plugin.metadata.PluginMetadata;
import scraper.api.exceptions.ValidationException;
import scraper.api.node.Node;

import java.lang.reflect.InvocationTargetException;

/**
 * This class holds the metadata needed for node instantiation and node versioning.
 * During initialization, AbstractMetadata is generated for each node found in the class/module path.
 *
 * @since 1.0.0
 */
public abstract class AbstractMetadata extends AbstractMetadataBasedPlugin {

    /** The category of the node */
    private final String category;
    public String getCategory() { return category; }

    /** Indicates if the node is deprecated */
    private final boolean deprecated;
    public boolean isDeprecated() { return deprecated; }

    /** Creates a new instance of {@code AbstractMetadata} */
    protected AbstractMetadata(final String name, final String version, final String category, boolean deprecated) {
        super(name, version);
        this.deprecated = deprecated;
        this.category = category;
    }

    /** Creates a new non-deprecated instance of {@code AbstractMetadata} */
    protected AbstractMetadata(final String name, final String version, final String category) {
        this(name, version, category, false);
    }

    /** Implements basic semantic versioning of metadata */
    @Override
    public boolean supports(final PluginMetadata delimiter) {
        String name = getMetadata().getName();

        if(!name.equalsIgnoreCase(delimiter.getName())) return false;

        // unknown version
        if(delimiter.getVersion().equalsIgnoreCase("0.0.0")) return true;

        int otherApiVersion = Integer.parseInt(delimiter.getVersion().split("\\.")[0]);
        int otherMajorVersion = Integer.parseInt(delimiter.getVersion().split("\\.")[1]);
        int otherMinorVersion = Integer.parseInt(delimiter.getVersion().split("\\.")[2]);

        return backwardsCompatible(otherApiVersion, otherMajorVersion, otherMinorVersion);
    }

    /** Each actual implementation for a node should be able to instantiate the node implementation */
    public abstract Node getNode() throws IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException, ValidationException;

    /**
     * Checks if this node is backwards compatible with another node
     *
     * @param other Other node metadata to check against
     * @return true, if this node implementation is backwards compatible with the other node implementation
     */
    public boolean backwardsCompatible(final AbstractMetadata other) {
        String oversion = other.getMetadata().getVersion();
        int oapi = Integer.valueOf(oversion.split("\\.")[0]);
        int omajor = Integer.valueOf(oversion.split("\\.")[1]);
        int ominor = Integer.valueOf(oversion.split("\\.")[2]);

        return backwardsCompatible(oapi, omajor, ominor);
    }

    /** Implementation of backwards compatibility check with api, major, and minor version */
    private boolean backwardsCompatible(int oapi, int omajor, int ominor) {
        String version = getMetadata().getVersion();
        int api = Integer.valueOf(version.split("\\.")[0]);
        int major = Integer.valueOf(version.split("\\.")[1]);
        int minor = Integer.valueOf(version.split("\\.")[2]);

        if(api < oapi) return false;
        if(api > oapi) return false;
        // api matches

        if(major < omajor) return false;
        if(major > omajor) return true;
        // major version matches

        if(minor < ominor) return false;
        if(minor > ominor) return true;

        // all match
        return true;
    }
}
