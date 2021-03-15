package org.openstreetmap.atlas.utilities.command.subcommands.templates;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.util.Optional;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.streaming.resource.File;
import org.openstreetmap.atlas.utilities.command.abstractcommand.AbstractAtlasShellToolsCommand;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;

/**
 * @author lcram
 */
public class OutputDirectoryTemplateTest
{
    private static class TestCommand extends AbstractAtlasShellToolsCommand
    {
        @Override
        public int execute()
        {
            final Optional<Path> output = OutputDirectoryTemplate.getOutputPath(this);
            if (output.isEmpty())
            {
                this.getCommandOutputDelegate().printlnErrorMessage("could not get output path");
                return 1;
            }
            this.getCommandOutputDelegate().printlnCommandMessage(
                    "output path is " + output.get().toAbsolutePath().toString());
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
            registerOptionsAndArgumentsFromTemplate(new OutputDirectoryTemplate());
            super.registerOptionsAndArguments();
        }
    }

    @Test
    public void testNoOption()
    {
        try (FileSystem filesystem = Jimfs.newFileSystem(Configuration.osX()))
        {
            setupFilesystem1(filesystem);
            final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
            final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
            final TestCommand command = new TestCommand();
            command.setNewFileSystem(filesystem);
            command.setNewOutStream(new PrintStream(outContent));
            command.setNewErrStream(new PrintStream(errContent));

            command.runSubcommand("--verbose");

            Assert.assertEquals("", outContent.toString());
            Assert.assertEquals("test-command: output path is /work\n", errContent.toString());
        }
        catch (final IOException exception)
        {
            throw new CoreException("FileSystem operation failed", exception);
        }
    }

    @Test
    public void testOption()
    {
        try (FileSystem filesystem = Jimfs.newFileSystem(Configuration.osX()))
        {
            setupFilesystem1(filesystem);
            final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
            final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
            final TestCommand command = new TestCommand();
            command.setNewFileSystem(filesystem);
            command.setNewOutStream(new PrintStream(outContent));
            command.setNewErrStream(new PrintStream(errContent));

            command.runSubcommand("--verbose", "--output-directory=/Users/foo/bar");

            Assert.assertEquals("", outContent.toString());
            Assert.assertEquals("test-command: output path is /Users/foo/bar\n",
                    errContent.toString());
        }
        catch (final IOException exception)
        {
            throw new CoreException("FileSystem operation failed", exception);
        }
    }

    private void setupFilesystem1(final FileSystem filesystem)
    {
        final File helloText = new File("/Users/foo/hello.txt", filesystem);
        helloText.writeAndClose("Hello world!");
        final File helloText2 = new File("/Users/foo/bar/hello2.txt", filesystem);
        helloText2.writeAndClose("Hello world again!");
    }
}
