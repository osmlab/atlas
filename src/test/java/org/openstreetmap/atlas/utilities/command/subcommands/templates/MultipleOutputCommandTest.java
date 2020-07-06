package org.openstreetmap.atlas.utilities.command.subcommands.templates;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.FileSystem;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.streaming.resource.File;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;

/**
 * @author lcram
 */
public class MultipleOutputCommandTest
{
    /**
     * @author lcram
     */
    private static class TestImplementationCommand extends MultipleOutputCommand
    {
        @Override
        public int execute()
        {
            // set up the output path from the parent class
            final int code = super.execute();
            if (code != 0)
            {
                return code;
            }

            // Write two files to the output path
            final File file1 = new File(this.getFileSystem()
                    .getPath(this.getOutputPath().toAbsolutePath().toString(), "file1.txt"));
            file1.writeAndClose("file1");
            final File file2 = new File(this.getFileSystem()
                    .getPath(this.getOutputPath().toAbsolutePath().toString(), "file2.txt"));
            file2.writeAndClose("file2");

            return 0;
        }

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
    }

    @Test
    public void testCommand()
    {
        try (FileSystem filesystem = Jimfs.newFileSystem(Configuration.osX()))
        {
            setupFilesystem(filesystem);
            final TestImplementationCommand command = new TestImplementationCommand();
            command.setNewFileSystem(filesystem);
            command.runSubcommand("--output=/Users/foo/output");

            final File file1 = new File(
                    command.getFileSystem().getPath("/Users/foo/output", "file1.txt"));
            Assert.assertEquals("file1", file1.all());

            final File file2 = new File(
                    command.getFileSystem().getPath("/Users/foo/output", "file2.txt"));
            Assert.assertEquals("file2", file2.all());
        }
        catch (final IOException exception)
        {
            throw new CoreException("FileSystem operation failed", exception);
        }
    }

    @Test
    public void testFailures()
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
            command.runSubcommand("--output=/Users/foo/bar");
            final StringBuilder errExpected = new StringBuilder();
            errExpected.append("test: error: /Users/foo/bar already exists and is a file\n")
                    .append("test: error: invalid output path\n");
            Assert.assertEquals(errExpected.toString(), errContent.toString());
        }
        catch (final IOException exception)
        {
            throw new CoreException("FileSystem operation failed", exception);
        }
    }

    private void setupFilesystem(final FileSystem filesystem)
    {
        new File(filesystem.getPath("/Users/foo")).mkdirs();
        new File(filesystem.getPath("/Users/foo/bar")).writeAndClose("bar");
    }
}
