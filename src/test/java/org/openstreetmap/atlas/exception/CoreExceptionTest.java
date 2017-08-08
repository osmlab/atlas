package org.openstreetmap.atlas.exception;

import java.util.function.BooleanSupplier;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.utilities.random.RandomTextGenerator;

/**
 * @author matthieun
 */
public class CoreExceptionTest
{
    @Test
    public void testTransitive()
    {
        final BooleanSupplier nested = () ->
        {
            throw new CoreException("Nested Message");
        };
        final BooleanSupplier wrapping = () ->
        {
            try
            {
                nested.getAsBoolean();
                return false;
            }
            catch (final CoreException e)
            {
                throw new CoreException("Wrapping Message: {}", new RandomTextGenerator().newWord(),
                        e);
            }
        };
        try
        {
            wrapping.getAsBoolean();
        }
        catch (final CoreException e)
        {
            Assert.assertNotNull(e.getCause());
        }
    }
}
