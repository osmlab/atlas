package org.openstreetmap.atlas.geography.atlas.change.exception;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.exception.CoreException;

/**
 * @author Yazad Khambata
 */
public class EmptyChangeExceptionTest
{

    @Test
    public void testMessage()
    {
        final String message = new EmptyChangeException().getMessage();

        Assert.assertTrue(message.startsWith(CoreException.TOKEN));

        System.out.println(message);

        Assert.assertEquals(new EmptyChangeException().getMessage(),
                new EmptyChangeException(new RuntimeException()).getMessage());
    }
}
