package org.openstreetmap.atlas.geography.atlas.command;

import java.util.Arrays;
import java.util.List;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openstreetmap.atlas.geography.atlas.builder.text.TextAtlasBuilder;
import org.openstreetmap.atlas.streaming.resource.File;

/**
 * Unit tests for {@link AtlasJoinerSubCommand}.
 *
 * @author bbreithaupt
 */
public class AtlasJoinerSubCommandTest
{
    private static final String TXT_PATH = AtlasFindByAtlasIdentifierSubCommandTest.class
            .getResource("DNK_Copenhagen").getPath();

    private static final File SHARD_PATH = File.temporaryFolder();

    @BeforeClass
    public static void createBinaryAtlases()
    {
        final List<String> shardList = Arrays.asList(TXT_PATH + "/DNK_1.txt",
                TXT_PATH + "/DNK_2.txt", TXT_PATH + "/DNK_3.txt");
        shardList.forEach(shard -> new TextAtlasBuilder().read(new File(shard))
                .save(new File(SHARD_PATH.getPath() + shard.replace("txt", "atlas"))));
    }

    @AfterClass
    public static void deleteBinaryAtlases()
    {
        SHARD_PATH.deleteRecursively();
    }

    @Test
    public void testJoinedOutput()
    {
        final File temp = File.temporary();

        // Run AtlasJoinerSubCommand
        final String[] args = { "join", String.format("-input=%1$s", SHARD_PATH),
                String.format("-output=%1$s", temp.getPath()) };
        new AtlasReader(args).runWithoutQuitting(args);

        Assert.assertTrue(temp.length() > 0);
        temp.delete();
    }
}
