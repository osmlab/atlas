package org.openstreetmap.atlas.streaming.resource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.exception.CoreException;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;

/**
 * @author lcram
 */
public class FileTest
{
    @Test
    public void readLinesFromFileTest()
    {
        try (FileSystem filesystem = Jimfs.newFileSystem(Configuration.osX()))
        {
            final Path directoryPath = filesystem.getPath("/tmp");
            Files.createDirectory(directoryPath);

            final Path filePath = filesystem.getPath("/tmp/foo");
            Files.createFile(filePath);
            final List<String> lines = Arrays.asList("one", "two", "three");
            Files.write(filePath, lines, StandardCharsets.UTF_8);

            final File file = new File(filePath);
            Assert.assertEquals("one\ntwo\nthree", file.all());
        }
        catch (final IOException exception)
        {
            throw new CoreException("FileSystem operation failed", exception);
        }
    }

    @Test
    public void testCreateParentDirectories()
    {
        try (FileSystem filesystem = Jimfs.newFileSystem(Configuration.osX()))
        {
            final Path homeDirectoryPath = filesystem.getPath("/Users/foobar");
            Files.createDirectories(homeDirectoryPath);

            final Path desktopPath = homeDirectoryPath.resolve("Desktop");
            Files.createDirectory(desktopPath);

            final Path directoryOnDesktopPath = desktopPath.resolve("folder_on_desktop");
            Files.createDirectory(directoryOnDesktopPath);

            final Path fileOnDesktopPath = desktopPath.resolve("file_on_desktop");
            Files.createFile(fileOnDesktopPath);
            final List<String> lines = Arrays.asList("foo", "bar");
            Files.write(fileOnDesktopPath, lines, StandardCharsets.UTF_8);

            final Path tmpDirectoryPath = filesystem.getPath("/tmp");
            Files.createDirectory(tmpDirectoryPath);

            final Path symlinkToDesktopPath = tmpDirectoryPath.resolve("symlink_to_desktop");
            Files.createSymbolicLink(symlinkToDesktopPath, desktopPath);

        }
        catch (final IOException exception)
        {
            throw new CoreException("FileSystem operation failed", exception);
        }
        /*
         * TODO we need to also see what happens when we perform file operations with a symlink that
         * points to a file
         */
    }
}
