package org.openstreetmap.atlas.geography.geojson.parser.domain.bbox;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Yazad Khambata
 */
public abstract class AbstractBbox implements Bbox {
    private Dimensions dimensions;

    public AbstractBbox(final Dimensions dimensions) {
        this.dimensions = dimensions;
    }

    @Override
    public Dimensions applicableDimensions() {
        return dimensions;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public boolean equals(final Object that) {
        return EqualsBuilder.reflectionEquals(this, that);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }
}
