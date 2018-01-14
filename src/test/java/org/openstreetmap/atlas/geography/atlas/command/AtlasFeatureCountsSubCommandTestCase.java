package org.openstreetmap.atlas.geography.atlas.command;

import java.io.IOException;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.openstreetmap.atlas.streaming.resource.File;
import org.openstreetmap.atlas.utilities.collections.Iterables;

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

    private static File temporaryFolder;

    @AfterClass
    public static void cleanupAtlasesOnDisk() throws IOException
    {
        temporaryFolder.deleteRecursively();
    }

    @BeforeClass
    public static void prepareAtlasesOnDisk() throws IOException
    {
        final File temp = File.temporaryFolder();
        setup.getFirstAtlas().save(temp.child("first.atlas"));
        setup.getSecondAtlas().save(temp.child("second.atlas"));
        setup.getThirdAtlas().save(temp.child("third.atlas"));
        setup.getFourthAtlas().save(temp.child("fourth.atlas"));
        temporaryFolder = temp;
    }

    @Test
    public void testThreadSafety() throws IOException
    {
        final File outputPath = File.temporary();
        try
        {
            AtlasReader.main("featureCounts",
                    String.format("-input=%s",
                            AtlasFeatureCountsSubCommandTestCase.temporaryFolder),
                    "-parallel", String.format("-output=%s", outputPath));
            final boolean[] found = { false };
            Iterables.filter(outputPath.lines(), line -> line.contains("JPN-NODE")).forEach(line ->
            {
                final String[] fields = line.split(":");
                Assert.assertEquals(4, Integer.parseInt(fields[1].trim()));
                found[0] = true;
            });
            Assert.assertTrue(found[0]);
        }
        finally
        {
            outputPath.delete();
        }
    }
}
