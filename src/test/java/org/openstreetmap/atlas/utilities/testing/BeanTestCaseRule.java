package org.openstreetmap.atlas.utilities.testing;

/**
 * Example of how to use the Bean annotation in a test case
 *
 * @author cstaylor
 */
public class BeanTestCaseRule extends CoreTestRule
{
    @Bean({ "name=Christopher Taylor", "street=123 Main Street" })
    private SomeTestBean bean;

    SomeTestBean bean()
    {
        return this.bean;
    }
}
