package org.openstreetmap.atlas.geography.geojson.parser.domain.geometry.coordinate;

import java.io.Serializable;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.openstreetmap.atlas.geography.Latitude;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.Longitude;

/**
 * @author Yazad Khambata
 */
public class Position implements Serializable
{
    private Double coordinate1;
    private Double coordinate2;
    
    public Position(final Double coordinate1, final Double coordinate2)
    {
        this.coordinate1 = coordinate1;
        this.coordinate2 = coordinate2;
    }
    
    @Override
    public boolean equals(final Object that)
    {
        return EqualsBuilder.reflectionEquals(this, that);
    }
    
    public Double getCoordinate1()
    {
        return this.coordinate1;
    }
    
    public Double getCoordinate2()
    {
        return this.coordinate2;
    }
    
    @Override
    public int hashCode()
    {
        return HashCodeBuilder.reflectionHashCode(this);
    }
    
    /**
     * The order of longitude and latitude in GeoJson as per the RFC is [lon, lat, alt].
     * <p>
     * However the order longitude and latitude is not shared in various atlas constructors, hence
     * the flip in the order.
     *
     * @return - the {@link Location} represented by the {@link Position}.
     */
    public Location toLocation()
    {
        return new Location(Latitude.degrees(coordinate2), Longitude.degrees(coordinate1));
    }
    
    @Override
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this);
    }
}
