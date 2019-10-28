package org.openstreetmap.atlas.geography.geojson.parser.testdomain;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Yazad Khambata
 */
public class BeanB
{
    private String name;
    private BeanA beanA;
    private BeanA[] beanAs;
    
    public BeanB()
    {
    }
    
    @Override
    public boolean equals(Object that)
    {
        return EqualsBuilder.reflectionEquals(this, that);
    }
    
    public BeanA getBeanA()
    {
        return beanA;
    }
    
    public BeanA[] getBeanAs()
    {
        return beanAs;
    }
    
    public String getName()
    {
        return name;
    }
    
    @Override
    public int hashCode()
    {
        return HashCodeBuilder.reflectionHashCode(this);
    }
    
    public void setBeanA(BeanA beanA)
    {
        this.beanA = beanA;
    }
    
    public void setBeanAs(BeanA[] beanAs)
    {
        this.beanAs = beanAs;
    }
    
    public void setName(String name)
    {
        this.name = name;
    }
    
    @Override
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this);
    }
}
