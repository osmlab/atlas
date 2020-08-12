package org.openstreetmap.atlas.utilities.command.subcommands.templates;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.FileSystem;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.streaming.resource.File;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;

/**
 * @author lcram
 */
public class AtlasLoaderCommandTest
{
    /**
     * @author lcram
     */
    private static class TestImplementationCommand extends AtlasLoaderCommand
    {
        @Override
        public String getCommandName()
        {
            return "test";
        }

        @Override
        public String getSimpleDescription()
        {
            return "test";
        }

        @Override
        public void processAtlas(final Atlas atlas, final String atlasFileName,
                final File atlasResource)
        {
            this.getCommandOutputDelegate().printlnCommandMessage(atlas.point(1L).toWkt());
            this.getCommandOutputDelegate().printlnCommandMessage(atlas.point(2L).toWkt());
        }
    }

    @Test
    public void testCombine()
    {
        try (FileSystem filesystem = Jimfs.newFileSystem(Configuration.osX()))
        {
            setupFilesystem(filesystem);
            final TestImplementationCommand command = new TestImplementationCommand();
            final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
            final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
            command.setNewFileSystem(filesystem);
            command.setNewOutStream(new PrintStream(outContent));
            command.setNewErrStream(new PrintStream(errContent));

            command.runSubcommand("/Users/foo/atlas1.atlas.txt", "/Users/foo/atlas2.atlas.txt",
                    "--combine");

            final StringBuilder errExpected = new StringBuilder();
            errExpected.append("test: POINT (1 1)\n").append("test: POINT (2 2)\n");
            Assert.assertEquals(errExpected.toString(), errContent.toString());
        }
        catch (final IOException exception)
        {
            throw new CoreException("FileSystem operation failed", exception);
        }
    }

    @Test
    public void testCommand()
    {
        try (FileSystem filesystem = Jimfs.newFileSystem(Configuration.osX()))
        {
            setupFilesystem(filesystem);
            final TestImplementationCommand command = new TestImplementationCommand();
            final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
            final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
            command.setNewFileSystem(filesystem);
            command.setNewOutStream(new PrintStream(outContent));
            command.setNewErrStream(new PrintStream(errContent));

            command.runSubcommand("/Users/foo/atlas1.atlas.txt", "/Users/foo/atlas2.atlas.txt",
                    "/Users/foo", "--verbose");

            final StringBuilder errExpected = new StringBuilder();
            errExpected.append("test: loading /Users/foo/atlas1.atlas.txt\n")
                    .append("test: loading /Users/foo/atlas2.atlas.txt\n")
                    .append("test: warn: skipping directory: /Users/foo\n")
                    .append("test: processing atlas /Users/foo/atlas1.atlas.txt (1/2)\n")
                    .append("test: POINT (1 1)\n").append("test: POINT (2 2)\n")
                    .append("test: processing atlas /Users/foo/atlas2.atlas.txt (2/2)\n")
                    .append("test: POINT (1 1)\n").append("test: POINT (2 2)\n");
            Assert.assertEquals(errExpected.toString(), errContent.toString());
        }
        catch (final IOException exception)
        {
            throw new CoreException("FileSystem operation failed", exception);
        }
    }

    @Test
    public void testMalformedAtlas()
    {
        try (FileSystem filesystem = Jimfs.newFileSystem(Configuration.osX()))
        {
            setupFilesystem(filesystem);

            final TestImplementationCommand command = new TestImplementationCommand();
            final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
            final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
            command.setNewFileSystem(filesystem);
            command.setNewOutStream(new PrintStream(outContent));
            command.setNewErrStream(new PrintStream(errContent));

            command.runSubcommand("/Users/foo/atlas_malformed.atlas.txt");

            final StringBuilder errExpected = new StringBuilder();
            errExpected.append("test: warn: could not load: /Users/foo/atlas_malformed.atlas.txt\n")
                    .append("test: error: no atlas files were loaded\n");
            Assert.assertEquals(errExpected.toString(), errContent.toString());
        }
        catch (final IOException exception)
        {
            throw new CoreException("FileSystem operation failed", exception);
        }
    }

    @Test
    public void testStrict()
    {
        try (FileSystem filesystem = Jimfs.newFileSystem(Configuration.osX()))
        {
            setupFilesystem(filesystem);
            final TestImplementationCommand command = new TestImplementationCommand();
            final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
            final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
            command.setNewFileSystem(filesystem);
            command.setNewOutStream(new PrintStream(outContent));
            command.setNewErrStream(new PrintStream(errContent));

            command.runSubcommand("/Users/foo/atlas1.atlas.txt", "/Users/foo/atlas2.atlas.txt",
                    "/Users/foo/atlas3.atlas.txt", "--strict");

            final StringBuilder errExpected = new StringBuilder();
            errExpected.append("test: warn: file not found: /Users/foo/atlas3.atlas.txt\n")
                    .append("test: error: strict load is missing some atlas(es)\n")
                    .append("test: error: no atlas files were loaded\n");
            Assert.assertEquals(errExpected.toString(), errContent.toString());
        }
        catch (final IOException exception)
        {
            throw new CoreException("FileSystem operation failed", exception);
        }
    }

    private void setupFilesystem(final FileSystem filesystem) throws IOException
    {
        final File atlas1File = new File(filesystem.getPath("/Users/foo", "atlas1.atlas.txt"));
        atlas1File.writeAndClose(AtlasLoaderCommandTest.class
                .getResourceAsStream("atlas1.atlas.txt").readAllBytes());
        final File atlas2File = new File(filesystem.getPath("/Users/foo", "atlas2.atlas.txt"));
        atlas2File.writeAndClose(AtlasLoaderCommandTest.class
                .getResourceAsStream("atlas1.atlas.txt").readAllBytes());
        final File atlasMalformedFile = new File(
                filesystem.getPath("/Users/foo", "atlas_malformed.atlas.txt"));
        atlasMalformedFile.writeAndClose(AtlasLoaderCommandTest.class
                .getResourceAsStream("atlas_malformed.atlas.txt").readAllBytes());
    }
}
