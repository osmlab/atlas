package org.openstreetmap.atlas.geography.atlas.command;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.streaming.resource.File;

/**
 * Unit tests for {@link AtlasJoinerSubCommand}.
 *
 * @author bbreithaupt
 */
public class AtlasJoinerSubCommandTest
{
    private final String shardPath = AtlasFindByAtlasIdentifierSubCommandTest.class
            .getResource("DNK_Copenhagen").getPath();

    @Test
    public void testJoinedOutput()
    {
        final File temp = File.temporary();

        // Run AtlasJoinerSubCommand
        final String[] args = { "join", String.format("-input=%1$s", shardPath),
                String.format("-output=%1$s", temp.getPath()) };
        new AtlasReader(args).runWithoutQuitting(args);

        Assert.assertTrue(temp.length() > 0);
        temp.delete();
    }
}
