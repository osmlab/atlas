package org.openstreetmap.atlas.streaming.resource;

import org.junit.Assert;
import org.junit.Test;

/**
 * Files that don't exist shouldn't be returned through a recursive descent
 *
 * @author cstaylor
 */
public class RecursiveListWithMissingFileTestCase
{
    @Test
    public void shouldBeEmpty()
    {
        Assert.assertTrue(new File("/tmp/not/a/file").listFilesRecursively().isEmpty());
    }
}
