package org.openstreetmap.atlas.utilities.archive;

import java.io.File;
import java.io.IOException;

import org.apache.commons.compress.archivers.ArchiveException;
import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.streaming.resource.InputStreamResource;
import org.openstreetmap.atlas.streaming.resource.Resource;

/**
 * @author matthieun
 */
public class ExtractorTest
{
    @Test
    public void testExtract() throws IOException, ArchiveException
    {
        File temporaryFile = null;
        File outputDirectory = null;
        try
        {
            final String name = "testExtractor";
            temporaryFile = File.createTempFile(name, ".zip");
            outputDirectory = File.createTempFile(name + "_output", "");
            outputDirectory.delete();
            outputDirectory.mkdirs();
            final Resource testData = new InputStreamResource(
                    () -> ExtractorTest.class.getResourceAsStream("testExtractor.zip"));
            testData.copyTo(new org.openstreetmap.atlas.streaming.resource.File(temporaryFile));
            final Extractor extractor = Extractor.extractZipArchive(outputDirectory);
            extractor.extract(temporaryFile);
            final String expectedOutput = "file1";
            final String actualOutput = new org.openstreetmap.atlas.streaming.resource.File(
                    new File(outputDirectory, "file1.txt")).all();
            Assert.assertEquals(expectedOutput, actualOutput);
        }
        finally
        {
            temporaryFile.delete();
            new org.openstreetmap.atlas.streaming.resource.File(outputDirectory)
                    .listFilesRecursively()
                    .forEach(org.openstreetmap.atlas.streaming.resource.File::delete);
            outputDirectory.delete();
        }
    }
}
