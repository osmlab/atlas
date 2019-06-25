package org.openstreetmap.atlas.utilities.checkstyle;

import org.junit.Test;

import com.puppycrawl.tools.checkstyle.AbstractModuleTestSupport;
import com.puppycrawl.tools.checkstyle.DefaultConfiguration;

/**
 * @author matthieun
 */
public class ArrangementCheckTest extends AbstractModuleTestSupport
{
    @Test
    public void testRight() throws Exception
    {
        verify(configuration(), getPath("ArrangementCheckRight.java"));
    }

    @Test
    public void testWrongField0() throws Exception
    {
        verify(configuration(), getPath("ArrangementCheckWrongField0.java"),
                "12: Invalid order, method, field");
    }

    @Test
    public void testWrongField1() throws Exception
    {
        verify(configuration(), getPath("ArrangementCheckWrongField1.java"),
                "9: Invalid order, field, FIELD");
    }

    @Test
    public void testWrongField2() throws Exception
    {
        verify(configuration(), getPath("ArrangementCheckWrongField2.java"),
                "9: Invalid order, fieldA, fieldB");
    }

    @Test
    public void testWrongInitializerBlock() throws Exception
    {
        verify(configuration(), getPath("ArrangementCheckWrongInitializerBlock.java"),
                "12: Invalid order, method, initializer_block");
    }

    @Test
    public void testWrongInitializerStaticBlock() throws Exception
    {
        verify(configuration(), getPath("ArrangementCheckWrongInitializerStaticBlock.java"),
                "12: Invalid order, method, static_initializer_block");
    }

    @Test
    public void testWrongMethodModifier() throws Exception
    {
        verify(configuration(), getPath("ArrangementCheckWrongMethodModifier.java"),
                "12: Invalid order, methodA, methodB");
    }

    @Test
    public void testWrongMethodName() throws Exception
    {
        verify(configuration(), getPath("ArrangementCheckWrongMethodName.java"),
                "12: Invalid order, methodB, methodA");
    }

    @Test
    public void testWrongMethodVisibility1() throws Exception
    {
        verify(configuration(), getPath("ArrangementCheckWrongMethodVisibility1.java"),
                "12: Invalid order, methodA, methodB");
    }

    @Test
    public void testWrongMethodVisibility2() throws Exception
    {
        verify(configuration(), getPath("ArrangementCheckWrongMethodVisibility2.java"),
                "12: Invalid order, methodA, methodB");
    }

    @Override
    protected String getPackageLocation()
    {
        return "org/openstreetmap/atlas/utilities/checkstyle";
    }

    private DefaultConfiguration configuration()
    {
        final DefaultConfiguration result = createModuleConfig(ArrangementCheck.class);
        // Make sure to test with the configurable path!
        result.addAttribute("arrangementDefinition",
                ArrangementCheck.class.getResource("arrangement.txt").getPath());
        return result;
    }
}
