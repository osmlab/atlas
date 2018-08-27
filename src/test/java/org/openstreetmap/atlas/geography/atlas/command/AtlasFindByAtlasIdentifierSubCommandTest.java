package org.openstreetmap.atlas.geography.atlas.command;

import java.io.PrintStream;
import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.streaming.resource.File;

/**
 * Unit test for {@link AtlasFindByAtlasIdentifierSubCommand}.
 *
 * @author bbreithaupt
 */
public class AtlasFindByAtlasIdentifierSubCommandTest
{
    private final String shardPath = AtlasFindByAtlasIdentifierSubCommandTest.class
            .getResource("DNK_Copenhagen").getPath();

    @Test
    public void testConsoleOutput()
    {
        // Redirect System.out to capture AtlasFindByFeatureIdentifierLocatorSubCommand output
        final PrintStream originalOut = System.out;
        final CaptureOutputStream captureStream = new CaptureOutputStream(originalOut);
        System.setOut(captureStream);

        // Run AtlasFindByAtlasIdentifierSubCommand
        final String[] args = { "find-atlas-id", String.format("-input=%1$s", shardPath),
                "-id=546649246000001,575954012000000" };
        new AtlasReader(args).runWithoutQuitting(args);

        // Reset System.out
        System.setOut(originalOut);

        Arrays.stream(captureStream.getLog().split("\n")).forEach(line -> Assert
                .assertTrue(line.contains("DNK_2.atlas") || line.contains("DNK_3.atlas")));
    }

    @Test
    public void testJoinedOutput()
    {
        final File temp = File.temporary();

        // Run AtlasFindByAtlasIdentifierSubCommand
        final String[] args = { "find-atlas-id", String.format("-input=%1$s", shardPath),
                "-id=546649246000001,575954012000000",
                String.format("-joinedOutput=%1$s", temp.getPath()) };
        new AtlasReader(args).runWithoutQuitting(args);

        Assert.assertTrue(temp.length() > 0);
        temp.delete();
    }
}
