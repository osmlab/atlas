package org.openstreetmap.atlas.geography.geojson;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * Test cases for ConcatenateGeoJsonFiles.
 * 
 * @author rmegraw
 */
public class ConcatenateGeoJsonCommandTest
{
    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Test
    public void testConcatenateGeoJsonFiles() throws IOException
    {
        final String inputPath = new File(this.getClass().getResource("test1.geojson").getPath())
                .getParent();
        final String outputPath = this.tempFolder.newFile().getAbsolutePath();

        new ConcatenateGeoJsonCommand().runWithoutQuitting("-path=" + inputPath, "-mode=FILE",
                "-output=" + outputPath);

        final File outputFile = new File(outputPath);
        Assert.assertTrue(outputFile.exists());

        Assert.assertTrue(FileUtils
                .readFileToString(new File(this.getClass()
                        .getResource("concatenated_geojson_files_expected").getPath()),
                        Charset.defaultCharset())
                .equals(FileUtils.readFileToString(outputFile, Charset.defaultCharset())));

    }

    @Test
    public void testConcatenateGeoJsonLines() throws IOException
    {
        final String inputPath = new File(this.getClass().getResource("test1.geojson").getPath())
                .getParent();
        final String outputPath = this.tempFolder.newFile().getAbsolutePath();

        new ConcatenateGeoJsonCommand().runWithoutQuitting("-path=" + inputPath, "-mode=LINE",
                "-output=" + outputPath, "-filePrefix=" + "test");

        final File outputFile = new File(outputPath);
        Assert.assertTrue(outputFile.exists());

        Assert.assertTrue(FileUtils
                .readFileToString(new File(this.getClass()
                        .getResource("concatenated_geojson_files_expected").getPath()),
                        Charset.defaultCharset())
                .equals(FileUtils.readFileToString(outputFile, Charset.defaultCharset())));

    }

}
