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
    public boolean equals(final Object that)
    {
        return EqualsBuilder.reflectionEquals(this, that);
    }

    public BeanA getBeanA()
    {
        return this.beanA;
    }

    public BeanA[] getBeanAs()
    {
        return this.beanAs;
    }

    public String getName()
    {
        return this.name;
    }

    @Override
    public int hashCode()
    {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    public void setBeanA(final BeanA beanA)
    {
        this.beanA = beanA;
    }

    public void setBeanAs(final BeanA[] beanAs)
    {
        this.beanAs = beanAs;
    }

    public void setName(final String name)
    {
        this.name = name;
    }

    @Override
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this);
    }
}
