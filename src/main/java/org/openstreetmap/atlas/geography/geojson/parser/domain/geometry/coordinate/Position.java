package org.openstreetmap.atlas.geography.geojson.parser.domain.geometry.coordinate;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.Serializable;

/**
 * @author Yazad Khambata
 */
public class Position implements Serializable {
    private Double coordinate1;
    private Double coordinate2;

    public Position(final Double coordinate1, final Double coordinate2) {
        this.coordinate1 = coordinate1;
        this.coordinate2 = coordinate2;
    }

    public Double getCoordinate1() {
        return coordinate1;
    }

    public Double getCoordinate2() {
        return coordinate2;
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
