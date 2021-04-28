package org.openstreetmap.atlas.utilities.command.subcommands;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.FileSystem;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.packed.PackedAtlasBuilder;
import org.openstreetmap.atlas.streaming.resource.File;
import org.openstreetmap.atlas.utilities.collections.Maps;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;

/**
 * @author lcram
 */
public class AnyToGeoJsonCommandTest
{
    @Test
    public void testAtlasConversion()
    {
        try (FileSystem filesystem = Jimfs.newFileSystem(Configuration.osX()))
        {
            setupFilesystem1(filesystem);
            final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
            final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
            final AnyToGeoJsonCommand command = new AnyToGeoJsonCommand();
            command.setNewFileSystem(filesystem);
            command.setNewOutStream(new PrintStream(outContent));
            command.setNewErrStream(new PrintStream(errContent));

            command.runSubcommand("--atlas=/Users/foo/test.atlas", "--verbose",
                    "--output=/Users/foo");

            Assert.assertTrue(outContent.toString().isEmpty());
            Assert.assertEquals(
                    "any2geojson: writing the atlas geojson file to /Users/foo/output-atlas.geojson\n",
                    errContent.toString());
            Assert.assertTrue(new File("/Users/foo/output-atlas.geojson", filesystem).exists());
            Assert.assertEquals(
                    "{\"type\":\"Feature\",\"geometry\":{\"type\":\"Point\",\"coordinates\":[1.0,1.0]},\"properties\":{\"foo\":\"bar\",\"identifier\":1000000,\"osmIdentifier\":1,\"itemType\":\"POINT\"}}",
                    new File("/Users/foo/output-atlas.geojson", filesystem).readAndClose());
        }
        catch (final IOException exception)
        {
            throw new CoreException("FileSystem operation failed", exception);
        }
    }

    @Test
    public void testBoundaryConversion()
    {
        try (FileSystem filesystem = Jimfs.newFileSystem(Configuration.osX()))
        {
            setupFilesystem1(filesystem);
            final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
            final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
            AnyToGeoJsonCommand command = new AnyToGeoJsonCommand();
            command.setNewFileSystem(filesystem);
            command.setNewOutStream(new PrintStream(outContent));
            command.setNewErrStream(new PrintStream(errContent));

            command.runSubcommand("--country-boundary=/Users/foo/boundary.txt", "--verbose",
                    "--output=/Users/foo", "--countries=AIA", "--countries-deny=MAF");

            Assert.assertTrue(outContent.toString().isEmpty());
            Assert.assertEquals("any2geojson: loading country boundary map...\n"
                    + "any2geojson: loaded boundary map\n"
                    + "any2geojson: converting boundary file to GeoJSON...\n"
                    + "any2geojson: writing the boundary geojson file to /Users/foo/output-country-boundary.geojson\n",
                    errContent.toString());
            Assert.assertTrue(
                    new File("/Users/foo/output-country-boundary.geojson", filesystem).exists());
            Assert.assertEquals(
                    new String(AnyToGeoJsonCommandTest.class
                            .getResourceAsStream("test_boundary_expected.geojson").readAllBytes()),
                    new File("/Users/foo/output-country-boundary.geojson", filesystem)
                            .readAndClose() + "\n");

            final ByteArrayOutputStream outContent2 = new ByteArrayOutputStream();
            final ByteArrayOutputStream errContent2 = new ByteArrayOutputStream();
            command = new AnyToGeoJsonCommand();
            command.setNewFileSystem(filesystem);
            command.setNewOutStream(new PrintStream(outContent2));
            command.setNewErrStream(new PrintStream(errContent2));

            command.runSubcommand("--country-boundary=/Users/foo/boundary.txt", "--verbose",
                    "--output=/Users/foo", "--countries-deny=MAF");

            Assert.assertTrue(outContent2.toString().isEmpty());
            Assert.assertEquals("any2geojson: loading country boundary map...\n"
                    + "any2geojson: loaded boundary map\n"
                    + "any2geojson: converting boundary file to GeoJSON...\n"
                    + "any2geojson: writing the boundary geojson file to /Users/foo/output-country-boundary.geojson\n",
                    errContent2.toString());
            Assert.assertTrue(
                    new File("/Users/foo/output-country-boundary.geojson", filesystem).exists());
            Assert.assertEquals(
                    new String(AnyToGeoJsonCommandTest.class
                            .getResourceAsStream("test_boundary_expected.geojson").readAllBytes()),
                    new File("/Users/foo/output-country-boundary.geojson", filesystem)
                            .readAndClose() + "\n");
        }
        catch (final IOException exception)
        {
            throw new CoreException("FileSystem operation failed", exception);
        }
    }

    @Test
    public void testShardingConversion()
    {
        try (FileSystem filesystem = Jimfs.newFileSystem(Configuration.osX()))
        {
            setupFilesystem1(filesystem);
            final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
            final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
            final AnyToGeoJsonCommand command = new AnyToGeoJsonCommand();
            command.setNewFileSystem(filesystem);
            command.setNewOutStream(new PrintStream(outContent));
            command.setNewErrStream(new PrintStream(errContent));

            command.runSubcommand("--sharding=dynamic@/Users/foo/sharding.txt", "--verbose",
                    "--output=/Users/foo");

            Assert.assertTrue(outContent.toString().isEmpty());
            Assert.assertEquals(
                    "any2geojson: writing the sharding geojson file to /Users/foo/output-sharding.geojson\n",
                    errContent.toString());
            Assert.assertTrue(new File("/Users/foo/output-sharding.geojson", filesystem).exists());
            Assert.assertEquals(new String(AnyToGeoJsonCommandTest.class
                    .getResourceAsStream("sharding-tree-1-expected.geojson").readAllBytes()),
                    new File("/Users/foo/output-sharding.geojson", filesystem).readAndClose()
                            + "\n");
        }
        catch (final IOException exception)
        {
            throw new CoreException("FileSystem operation failed", exception);
        }
    }

    private void setupFilesystem1(final FileSystem filesystem) throws IOException
    {
        final PackedAtlasBuilder builder = new PackedAtlasBuilder();

        builder.addPoint(1000000L, Location.forWkt("POINT(1 1)"), Maps.hashMap("foo", "bar"));

        final Atlas atlas = builder.get();
        final File atlasFile = new File("/Users/foo/test.atlas", filesystem);
        assert atlas != null;
        atlas.save(atlasFile);

        final File boundaryFile = new File(filesystem.getPath("/Users/foo", "boundary.txt"));
        boundaryFile.writeAndClose(AnyToGeoJsonCommandTest.class
                .getResourceAsStream("test_boundary.txt").readAllBytes());

        final File shardingFile = new File(filesystem.getPath("/Users/foo", "sharding.txt"));
        shardingFile.writeAndClose(AnyToGeoJsonCommandTest.class
                .getResourceAsStream("sharding-tree-1.txt").readAllBytes());
    }
}
