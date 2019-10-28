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
    public boolean equals(Object that)
    {
        return EqualsBuilder.reflectionEquals(this, that);
    }
    
    public String getAfterView()
    {
        return afterView;
    }
    
    public String getBeforeView()
    {
        return beforeView;
    }
    
    public String getKey()
    {
        return key;
    }
    
    public String getName()
    {
        return name;
    }
    
    public String getOriginalValue()
    {
        return originalValue;
    }
    
    public String getPosition()
    {
        return position;
    }
    
    public String getType()
    {
        return type;
    }
    
    public String getValue()
    {
        return value;
    }
    
    @Override
    public int hashCode()
    {
        return HashCodeBuilder.reflectionHashCode(this);
    }
    
    public void setAfterView(String afterView)
    {
        this.afterView = afterView;
    }
    
    public void setBeforeView(String beforeView)
    {
        this.beforeView = beforeView;
    }
    
    public void setKey(String key)
    {
        this.key = key;
    }
    
    public void setName(String name)
    {
        this.name = name;
    }
    
    public void setOriginalValue(String originalValue)
    {
        this.originalValue = originalValue;
    }
    
    public void setPosition(String position)
    {
        this.position = position;
    }
    
    public void setType(String type)
    {
        this.type = type;
    }
    
    public void setValue(String value)
    {
        this.value = value;
    }
    
    @Override
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this);
    }
}
