package org.openstreetmap.atlas.geography.geojson.parser.domain.properties.ext.change;

import java.io.Serializable;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.openstreetmap.atlas.geography.geojson.parser.domain.annotation.Foreign;

/**
 * @author Yazad Khambata
 */
@Foreign public class Descriptor implements Serializable
{
    private String name;
    private String type;
    private String key;
    private String value;
    private String originalValue;
    private String position;
    private String beforeView;
    private String afterView;
    
    public Descriptor()
    {
    }
    
    @Override
    public boolean equals(final Object that)
    {
        return EqualsBuilder.reflectionEquals(this, that);
    }
    
    public String getAfterView()
    {
        return this.afterView;
    }
    
    public String getBeforeView()
    {
        return this.beforeView;
    }
    
    public String getKey()
    {
        return this.key;
    }
    
    public String getName()
    {
        return this.name;
    }
    
    public String getOriginalValue()
    {
        return this.originalValue;
    }
    
    public String getPosition()
    {
        return this.position;
    }
    
    public String getType()
    {
        return this.type;
    }
    
    public String getValue()
    {
        return this.value;
    }
    
    @Override
    public int hashCode()
    {
        return HashCodeBuilder.reflectionHashCode(this);
    }
    
    public void setAfterView(final String afterView)
    {
        this.afterView = afterView;
    }
    
    public void setBeforeView(final String beforeView)
    {
        this.beforeView = beforeView;
    }
    
    public void setKey(final String key)
    {
        this.key = key;
    }
    
    public void setName(final String name)
    {
        this.name = name;
    }
    
    public void setOriginalValue(final String originalValue)
    {
        this.originalValue = originalValue;
    }
    
    public void setPosition(final String position)
    {
        this.position = position;
    }
    
    public void setType(final String type)
    {
        this.type = type;
    }
    
    public void setValue(final String value)
    {
        this.value = value;
    }
    
    @Override
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this);
    }
}
