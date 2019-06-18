package org.openstreetmap.atlas.geography.atlas.change.exception;

import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.exception.CoreException;

/**
 * @author Yazad Khambata
 */
public class ChangeExceptionTest
{

    @Test
    public void test()
    {
        final String message = "test message {}";
        final String arg1 = "hello";

        final ChangeException changeException1 = new ChangeException(message, arg1);
        final ChangeException changeException2 = new ChangeException(message,
                new RuntimeException(UUID.randomUUID().toString()), arg1);

        Assert.assertEquals(changeException1.getMessage(), changeException2.getMessage());
        Assert.assertTrue(changeException1.getMessage().startsWith(CoreException.TOKEN));
        Assert.assertTrue(changeException1.getMessage().endsWith(arg1));
    }
}
