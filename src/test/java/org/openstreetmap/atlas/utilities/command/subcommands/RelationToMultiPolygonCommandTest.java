package org.openstreetmap.atlas.utilities.command.subcommands;

import java.io.IOException;
import java.nio.file.FileSystem;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.raw.slicing.RawAtlasSlicerTestRule;
import org.openstreetmap.atlas.streaming.resource.File;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;

/**
 * @author samg
 **/
public class RelationToMultiPolygonCommandTest
{
    @Rule
    public final RawAtlasSlicerTestRule setup = new RawAtlasSlicerTestRule();

    @Test
    public void testInvalidMultiPolygonNoAtlasSave()
    {
        try (FileSystem filesystem = Jimfs.newFileSystem(Configuration.osX()))
        {
            setupFilesystem1(filesystem);
            RelationToMultiPolygonCommand command = new RelationToMultiPolygonCommand();
            command.setNewFileSystem(filesystem);

            int returnVal = command.runSubcommand("--id=1", "--wkt",
                    "/Users/foo/invalidMultiPolygonAtlas.txt");
            Assert.assertEquals(1, returnVal);
            Assert.assertFalse(new File("/work/1.wkt", filesystem).exists());
            Assert.assertFalse(new File("/work/1.atlas", filesystem).exists());

            command = new RelationToMultiPolygonCommand();
            command.setNewFileSystem(filesystem);
            returnVal = command.runSubcommand("--id=1", "--wkt",
                    "/Users/foo/invalidMultiPolygonAtlas2.txt");
            Assert.assertEquals(1, returnVal);
            Assert.assertFalse(new File("/work/1.wkt", filesystem).exists());
            Assert.assertFalse(new File("/work/1.atlas", filesystem).exists());

        }
        catch (final IOException exception)
        {
            throw new CoreException("FileSystem operation failed", exception);
        }
    }

    @Test
    public void testRelationNotFound()
    {
        try (FileSystem filesystem = Jimfs.newFileSystem(Configuration.osX()))
        {
            setupFilesystem1(filesystem);
            final RelationToMultiPolygonCommand command = new RelationToMultiPolygonCommand();
            command.setNewFileSystem(filesystem);

            final int returnVal = command.runSubcommand("--id=3", "--wkt",
                    "/Users/foo/invalidMultiPolygonAtlas.txt");
            Assert.assertEquals(1, returnVal);
            Assert.assertFalse(new File("/work/1.wkt", filesystem).exists());
            Assert.assertFalse(new File("/work/1.atlas", filesystem).exists());
        }
        catch (final IOException exception)
        {
            throw new CoreException("FileSystem operation failed", exception);
        }
    }

    @Test
    public void testValidMultiPolygon()
    {
        try (FileSystem filesystem = Jimfs.newFileSystem(Configuration.osX()))
        {
            setupFilesystem1(filesystem);
            final RelationToMultiPolygonCommand command = new RelationToMultiPolygonCommand();
            command.setNewFileSystem(filesystem);

            final int returnVal = command.runSubcommand("--id=1",
                    "/Users/foo/validMultiPolygonAtlas.txt");
            Assert.assertEquals(0, returnVal);
            Assert.assertTrue(new File("/work/1.wkt", filesystem).exists());
            Assert.assertEquals(
                    "MULTIPOLYGON (((-8.3372107 6.8921817, -8.3389237 6.8837848, -8.328271 6.883466, -8.3286993 6.8914376, -8.3372107 6.8921817, -8.3372107 6.8921817)), ((-8.3426173 6.9044046, -8.3478098 6.8973366, -8.3415467 6.8936697, -8.3346947 6.8984526, -8.3426173 6.9044046, -8.3426173 6.9044046)), ((-8.3259156 6.8970709, -8.3170295 6.8994623, -8.3154771 6.8895776, -8.3262368 6.8915439, -8.3259156 6.8970709, -8.3259156 6.8970709)))",
                    new File("/work/1.wkt", filesystem).all());
            Assert.assertTrue(new File("/work/1.atlas", filesystem).exists());
        }
        catch (final IOException exception)
        {
            throw new CoreException("FileSystem operation failed", exception);
        }
    }

    @Test
    public void testValidMultiPolygonNoAtlasSave()
    {
        try (FileSystem filesystem = Jimfs.newFileSystem(Configuration.osX()))
        {
            setupFilesystem1(filesystem);
            final RelationToMultiPolygonCommand command = new RelationToMultiPolygonCommand();
            command.setNewFileSystem(filesystem);

            final int returnVal = command.runSubcommand("--id=1", "--wkt",
                    "/Users/foo/validMultiPolygonAtlas.txt");
            Assert.assertEquals(0, returnVal);
            Assert.assertTrue(new File("/work/1.wkt", filesystem).exists());
            Assert.assertEquals(
                    "MULTIPOLYGON (((-8.3372107 6.8921817, -8.3389237 6.8837848, -8.328271 6.883466, -8.3286993 6.8914376, -8.3372107 6.8921817, -8.3372107 6.8921817)), ((-8.3426173 6.9044046, -8.3478098 6.8973366, -8.3415467 6.8936697, -8.3346947 6.8984526, -8.3426173 6.9044046, -8.3426173 6.9044046)), ((-8.3259156 6.8970709, -8.3170295 6.8994623, -8.3154771 6.8895776, -8.3262368 6.8915439, -8.3259156 6.8970709, -8.3259156 6.8970709)))",
                    new File("/work/1.wkt", filesystem).all());
            Assert.assertFalse(new File("/work/1.atlas", filesystem).exists());
        }
        catch (final IOException exception)
        {
            throw new CoreException("FileSystem operation failed", exception);
        }
    }

    private void setupFilesystem1(final FileSystem filesystem) throws IOException
    {
        final Atlas validMultiPolygonAtlas = this.setup.getSimpleMultiPolygonAtlas();
        final File validMultiPolygonAtlasFile = new File(
                filesystem.getPath("/Users/foo", "validMultiPolygonAtlas.txt"));
        validMultiPolygonAtlas.saveAsText(validMultiPolygonAtlasFile);

        final Atlas invalidMultiPolygonAtlas = this.setup.getOpenMultiPolygonInOneCountryAtlas();
        final File invalidMultiPolygonAtlasFile = new File(
                filesystem.getPath("/Users/foo", "invalidMultiPolygonAtlas.txt"));
        invalidMultiPolygonAtlas.saveAsText(invalidMultiPolygonAtlasFile);

        final Atlas invalidMultiPolygonAtlas2 = this.setup
                .getSelfIntersectingOuterMemberRelationAtlas();
        final File invalidMultiPolygonAtlasFile2 = new File(
                filesystem.getPath("/Users/foo", "invalidMultiPolygonAtlas2.txt"));
        invalidMultiPolygonAtlas2.saveAsText(invalidMultiPolygonAtlasFile2);
    }
}
