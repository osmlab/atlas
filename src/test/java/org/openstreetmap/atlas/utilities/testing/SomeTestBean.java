package org.openstreetmap.atlas.utilities.testing;

/**
 * Sample object used in the BeanTestCaseRule
 *
 * @author cstaylor
 */
class SomeTestBean
{
    private String name;
    private String street;

    public String getName()
    {
        return this.name;
    }

    public String getStreet()
    {
        return this.street;
    }

    public void setName(final String name)
    {
        this.name = name;
    }

    public void setStreet(final String street)
    {
        this.street = street;
    }

}
