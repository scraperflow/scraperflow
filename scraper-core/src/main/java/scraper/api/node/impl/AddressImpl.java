package scraper.api.node.impl;

import scraper.annotations.NotNull;
import scraper.annotations.Nullable;
import scraper.api.node.Address;

import java.util.Objects;

public class AddressImpl implements Address {
    /** Unique label */
    private @NotNull String label;

    public AddressImpl(@NotNull String label) {
        this.label = label;
    }

    @NotNull public String getLabel() {
        return label;
    }
    public void setLabel(@NotNull String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return "<"+label+">";
    }

    @NotNull
    @Override
    public String getRepresentation() {
        return toString().substring(1,toString().length()-1);
    }

    @Override
    public @Nullable Address resolve(@NotNull Address toResolve) {
        // cant resolve
        if(!getRepresentation().contains(toResolve.getRepresentation())) return null;

        // no instance address
        if(!getRepresentation().contains(".")) return null;

        String resolved = getRepresentation().substring(0, getRepresentation().indexOf("."));
        if(resolved.equalsIgnoreCase(toResolve.getRepresentation())) {
            return new AddressImpl(getRepresentation().substring(resolved.length()+1));
        }

        // bad instance address
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AddressImpl address = (AddressImpl) o;
        return label.equals(address.label);
    }

    @Override
    public int hashCode() {
        return Objects.hash(label);
    }
}
