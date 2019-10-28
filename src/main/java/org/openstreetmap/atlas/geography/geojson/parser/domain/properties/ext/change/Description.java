package org.openstreetmap.atlas.geography.geojson.parser.domain.properties.ext.change;

import java.io.Serializable;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.openstreetmap.atlas.geography.geojson.parser.domain.annotation.Foreign;

/**
 * @author Yazad Khambata
 */
@Foreign public class Description implements Serializable
{
    private String type;
    private Descriptor[] descriptors;
    
    public Description()
    {
    }
    
    @Override
    public boolean equals(Object that)
    {
        return EqualsBuilder.reflectionEquals(this, that);
    }
    
    public Descriptor[] getDescriptors()
    {
        return descriptors;
    }
    
    public String getType()
    {
        return type;
    }
    
    @Override
    public int hashCode()
    {
        return HashCodeBuilder.reflectionHashCode(this);
    }
    
    public void setDescriptors(Descriptor[] descriptors)
    {
        this.descriptors = descriptors;
    }
    
    public void setType(String type)
    {
        this.type = type;
    }
    
    @Override
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this);
    }
}
