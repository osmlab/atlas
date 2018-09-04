package org.openstreetmap.atlas.geography.atlas.command;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openstreetmap.atlas.geography.atlas.builder.text.TextAtlasBuilder;
import org.openstreetmap.atlas.streaming.resource.File;

/**
 * Unit test for {@link AtlasFindByAtlasIdentifierSubCommand}.
 *
 * @author bbreithaupt
 */
public class AtlasFindByAtlasIdentifierSubCommandTest
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
    public void testConsoleOutput()
    {
        // Redirect System.out to capture AtlasFindByFeatureIdentifierLocatorSubCommand output
        final PrintStream originalOut = System.out;
        final CaptureOutputStream captureStream = new CaptureOutputStream(originalOut);
        System.setOut(captureStream);

        try
        {
            // Run AtlasFindByAtlasIdentifierSubCommand
            final String[] args = { "find-atlas-id",
                    String.format("-input=%1$s", SHARD_PATH.getPath()),
                    "-id=546649246000001,575954012000000" };
            new AtlasReader(args).runWithoutQuitting(args);
        }
        finally
        {
            // Reset System.out
            System.setOut(originalOut);
        }

        Arrays.stream(captureStream.getLog().split("\n")).forEach(line -> Assert
                .assertTrue(line.contains("DNK_2.atlas") || line.contains("DNK_3.atlas")));
    }

    @Test
    public void testJoinedOutput()
    {
        final File temp = File.temporary();

        // Run AtlasFindByAtlasIdentifierSubCommand
        final String[] args = { "find-atlas-id", String.format("-input=%1$s", SHARD_PATH.getPath()),
                "-id=546649246000001,575954012000000",
                String.format("-joinedOutput=%1$s", temp.getPath()) };
        new AtlasReader(args).runWithoutQuitting(args);

        Assert.assertTrue(temp.length() > 0);
        temp.delete();
    }
}
