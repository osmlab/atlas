package org.openstreetmap.atlas.utilities.command.subcommands;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.FileSystem;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.streaming.compression.Compressor;
import org.openstreetmap.atlas.streaming.resource.File;
import org.openstreetmap.atlas.streaming.resource.InputStreamResource;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;

/**
 * @author matthieun
 */
public class CountryBoundaryMapPrinterCommandTest
{
    @Test
    public void testCommandWithInputFile()
    {
        try (FileSystem filesystem = Jimfs.newFileSystem(Configuration.osX()))
        {
            final File inputText = new File(filesystem.getPath("/Users/foo", "boundary.txt.gz"))
                    .withCompressor(Compressor.GZIP);
            inputText.writeAndClose(new InputStreamResource(
                    () -> CountryBoundaryMapPrinterCommandTest.class.getResourceAsStream(
                            "CountryBoundaryMapPrinterCommandTestBoundaries.txt"))
                    .all());
            final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
            final CountryBoundaryMapPrinterCommand command = new CountryBoundaryMapPrinterCommand();
            command.setNewFileSystem(filesystem);
            command.setNewErrStream(new PrintStream(outContent));
            command.runSubcommand("--" + CountryBoundaryMapPrinterCommand.BOUNDARY_OPTION_LONG
                    + "=/Users/foo/boundary.txt.gz", "--verbose");
            Assert.assertTrue(outContent.toString().contains("boundary-itemizer: Saved XYZ in"));
            final File outputFolder = new File("/Users/foo", filesystem);
            final File outputWkt = outputFolder.child("boundary-wkt");
            final File outputGeojson = outputFolder.child("boundary-geojson");
            Assert.assertEquals(3, outputGeojson.listFiles().size());
            Assert.assertEquals(3, outputWkt.listFiles().size());
            Assert.assertEquals("MULTIPOLYGON (((4.2038896 38.8273784, 4.2132564 38.8330031, "
                    + "4.2308843 38.8337631, 4.2451948 38.8205877, 4.2422676 38.811769, "
                    + "4.226396 38.8072578, 4.2102643 38.8082716, 4.2021333 38.8176483, "
                    + "4.2038896 38.8273784)), ((9.1437517 41.3421313, 9.1863237 41.3591412, "
                    + "9.2144762 41.338007, 9.19937 41.3173817, 9.1739641 41.3359448, "
                    + "9.1616045 41.3194445, 9.1437517 41.3421313)))",
                    outputWkt.child("ABC.wkt").all());
            Assert.assertEquals(
                    "{\"type\":\"MultiPolygon\",\"coordinates\":[[[[9.1437517,41.3421313],"
                            + "[9.1863237,41.3591412],[9.2144762,41.338007],[9.19937,41.3173817],"
                            + "[9.1739641,41.3359448],[9.1616045,41.3194445],[9.1437517,41.3421313]]],"
                            + "[[[4.2038896,38.8273784],[4.2132564,38.8330031],[4.2308843,38.8337631],"
                            + "[4.2451948,38.8205877],[4.2422676,38.811769],[4.226396,38.8072578],"
                            + "[4.2102643,38.8082716],[4.2021333,38.8176483],[4.2038896,38.8273784]]]]}",
                    outputGeojson.child("ABC.geojson").all());
        }
        catch (final IOException exception)
        {
            throw new CoreException("FileSystem operation failed", exception);
        }
    }
}
