package org.openstreetmap.atlas.utilities.command.subcommands.templates;

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
import org.openstreetmap.atlas.utilities.command.abstractcommand.AbstractAtlasShellToolsCommand;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;

/**
 * @author lcram
 */
public class AtlasLoaderTemplateTest
{
    /**
     * @author lcram
     */
    private static class TestCommand extends AbstractAtlasShellToolsCommand
    {
        @Override
        public int execute()
        {
            AtlasLoaderTemplate.execute(this, this::start, this::processAtlas, this::finish);
            return 0;
        }

        @Override
        public String getCommandName()
        {
            return "test-command";
        }

        @Override
        public String getSimpleDescription()
        {
            return "unit test command";
        }

        @Override
        public void registerManualPageSections()
        {

        }

        @Override
        public void registerOptionsAndArguments()
        {
            registerOptionsAndArgumentsFromTemplate(new AtlasLoaderTemplate());
            super.registerOptionsAndArguments();
        }

        private int finish()
        {
            return 0;
        }

        private void processAtlas(final Atlas atlas, final String atlasFileName,
                final File atlasResource)
        {
            this.getCommandOutputDelegate().printlnStdout(atlasFileName);
            this.getCommandOutputDelegate().printlnStdout(atlas.metaData().getSize().toString());
        }

        private int start()
        {
            return 0;
        }
    }

    @Test
    public void test()
    {
        try (FileSystem filesystem = Jimfs.newFileSystem(Configuration.osX()))
        {
            setupFilesystem1(filesystem);
            final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
            final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
            final AtlasLoaderTemplateTest.TestCommand command = new AtlasLoaderTemplateTest.TestCommand();
            command.setNewFileSystem(filesystem);
            command.setNewOutStream(new PrintStream(outContent));
            command.setNewErrStream(new PrintStream(errContent));

            command.runSubcommand("/Users/foo/atlas1.atlas", "/Users/foo/atlas2.atlas",
                    "--verbose");

            Assert.assertEquals("atlas1.atlas\n"
                    + "[AtlasSize: edgeNumber=0, nodeNumber=0, areaNumber=0, lineNumber=0, pointNumber=3, relationNumber=0]\n"
                    + "atlas2.atlas\n"
                    + "[AtlasSize: edgeNumber=0, nodeNumber=0, areaNumber=0, lineNumber=0, pointNumber=3, relationNumber=0]\n",
                    outContent.toString());
            Assert.assertEquals(
                    "test-command: loading /Users/foo/atlas1.atlas\n"
                            + "test-command: loading /Users/foo/atlas2.atlas\n"
                            + "test-command: processing atlas /Users/foo/atlas1.atlas (1/2)\n"
                            + "test-command: processing atlas /Users/foo/atlas2.atlas (2/2)\n",
                    errContent.toString());
        }
        catch (final IOException exception)
        {
            throw new CoreException("FileSystem operation failed", exception);
        }
    }

    @Test
    public void testCombine()
    {
        try (FileSystem filesystem = Jimfs.newFileSystem(Configuration.osX()))
        {
            setupFilesystem1(filesystem);
            final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
            final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
            final AtlasLoaderTemplateTest.TestCommand command = new AtlasLoaderTemplateTest.TestCommand();
            command.setNewFileSystem(filesystem);
            command.setNewOutStream(new PrintStream(outContent));
            command.setNewErrStream(new PrintStream(errContent));

            command.runSubcommand("/Users/foo/atlas1.atlas", "/Users/foo/atlas2.atlas", "--combine",
                    "--verbose");

            Assert.assertEquals("combined.atlas\n"
                    + "[AtlasSize: edgeNumber=0, nodeNumber=0, areaNumber=0, lineNumber=0, pointNumber=3, relationNumber=0]\n",
                    outContent.toString());
            Assert.assertEquals(
                    "test-command: loading /Users/foo/atlas1.atlas\n"
                            + "test-command: loading /Users/foo/atlas2.atlas\n"
                            + "test-command: processing all atlases as one multiatlas...\n",
                    errContent.toString());
        }
        catch (final IOException exception)
        {
            throw new CoreException("FileSystem operation failed", exception);
        }
    }

    @Test
    public void testStrictFail()
    {
        try (FileSystem filesystem = Jimfs.newFileSystem(Configuration.osX()))
        {
            setupFilesystem1(filesystem);
            final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
            final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
            final AtlasLoaderTemplateTest.TestCommand command = new AtlasLoaderTemplateTest.TestCommand();
            command.setNewFileSystem(filesystem);
            command.setNewOutStream(new PrintStream(outContent));
            command.setNewErrStream(new PrintStream(errContent));

            command.runSubcommand("/Users/foo/atlas1.atlas", "/Users/foo/atlas2.atlas",
                    "/Users/foo/atlas3.atlas", "--strict", "--verbose");

            Assert.assertEquals("", outContent.toString());
            Assert.assertEquals(
                    "test-command: loading /Users/foo/atlas1.atlas\n"
                            + "test-command: loading /Users/foo/atlas2.atlas\n"
                            + "test-command: warn: file not found: /Users/foo/atlas3.atlas\n"
                            + "test-command: error: strict load is missing some atlas(es)\n"
                            + "test-command: error: no atlas files were loaded\n",
                    errContent.toString());
        }
        catch (final IOException exception)
        {
            throw new CoreException("FileSystem operation failed", exception);
        }
    }

    private void setupFilesystem1(final FileSystem filesystem)
    {
        final PackedAtlasBuilder builder = new PackedAtlasBuilder();

        builder.addPoint(1L, Location.forWkt("POINT(1 1)"), Maps.hashMap("foo", "bar"));
        builder.addPoint(2L, Location.forWkt("POINT(2 2)"), Maps.hashMap("baz", "bat"));
        builder.addPoint(3L, Location.forWkt("POINT(3 3)"),
                Maps.hashMap("foo", "bar", "baz", "bat"));

        final Atlas atlas = builder.get();
        assert atlas != null;

        final File atlasFile1 = new File("/Users/foo/atlas1.atlas", filesystem);
        final File atlasFile2 = new File("/Users/foo/atlas2.atlas", filesystem);
        atlas.save(atlasFile1);
        atlas.save(atlasFile2);
    }
}
