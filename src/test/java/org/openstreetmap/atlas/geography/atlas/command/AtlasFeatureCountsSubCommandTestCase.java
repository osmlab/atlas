package org.openstreetmap.atlas.geography.atlas.command;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.openstreetmap.atlas.streaming.resource.File;

/**
 * Test case verifying that protecting against multiple concurrent threads in the AtlasReader report
 * the right output AtlasFeatureCountsSubCommand class
 * 
 * @author cstaylor
 */
public class AtlasFeatureCountsSubCommandTestCase
{
    @ClassRule
    public static AtlasFeatureCountsSubCommandTestCaseRule setup = new AtlasFeatureCountsSubCommandTestCaseRule();

    private static Path temporaryPath;

    @AfterClass
    public static void cleanupAtlasesOnDisk() throws IOException
    {
        Files.walk(temporaryPath).filter(Files::isRegularFile).forEach(path ->
        {
            try
            {
                Files.deleteIfExists(path);
            }
            catch (final IOException oops)
            {
                throw new RuntimeException("Error when cleaning up", oops);
            }
        });
        Files.deleteIfExists(temporaryPath);
    }

    @BeforeClass
    public static void prepareAtlasesOnDisk() throws IOException
    {
        final Path path = Files.createTempDirectory("atlastest");
        setup.getFirstAtlas().save(new File(path.resolve("first.atlas").toFile()));
        setup.getSecondAtlas().save(new File(path.resolve("second.atlas").toFile()));
        setup.getThirdAtlas().save(new File(path.resolve("third.atlas").toFile()));
        setup.getFourthAtlas().save(new File(path.resolve("fourth.atlas").toFile()));
        temporaryPath = path;
    }

    @Test
    public void testThreadSafety() throws IOException
    {
        final Path outputPath = Files.createTempFile("atlas", ".stats");
        try
        {
            AtlasReader.main("featureCounts", String.format("-input=%s", this.temporaryPath),
                    "-parallel", String.format("-output=%s", outputPath));
            Files.lines(outputPath).filter(line -> line.contains("JP-NODE")).forEach(line ->
            {
                final String[] fields = line.split(":");
                Assert.assertEquals(4, Integer.parseInt(fields[1].trim()));
            });
        }
        finally
        {
            Files.deleteIfExists(outputPath);
        }
    }
}
