package org.openstreetmap.atlas.streaming.resource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.streaming.compression.Compressor;
import org.openstreetmap.atlas.streaming.compression.Decompressor;
import org.openstreetmap.atlas.utilities.collections.Sets;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;

/**
 * @author lcram
 */
public class FileTest
{
    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Test
    public void testChild()
    {
        try (FileSystem filesystem = Jimfs.newFileSystem(Configuration.osX()))
        {
            final Path homePath = filesystem.getPath("/Users/foobar");
            final File home = new File(homePath);

            final File directChild = home.child("child");
            directChild.writeAndClose("foo");

            final File testDirectChildContents = new File(
                    filesystem.getPath("/Users/foobar/child"));
            Assert.assertEquals("foo", testDirectChildContents.all());

            // Now create a symlink to foobar's home folder
            Files.createDirectory(filesystem.getPath("/tmp"));
            final Path linkPath = filesystem.getPath("/tmp/link");
            Files.createSymbolicLink(linkPath, homePath);
            final File linkChild = home.child("child");
            linkChild.writeAndClose("foo");

            final File testLinkChildContents = new File(filesystem.getPath("/tmp/link/child"));
            Assert.assertEquals("foo", testLinkChildContents.all());
        }
        catch (final IOException exception)
        {
            throw new CoreException("FileSystem operation failed", exception);
        }
    }

    @Test
    public void testChildFailDueToParentIsAFile()
    {
        try (FileSystem filesystem = Jimfs.newFileSystem(Configuration.osX()))
        {
            final Path filePath = filesystem.getPath("/Users/bazbat/file");
            final File file = new File(filePath, true);
            file.writeAndClose("foobar");

            this.expectedException.expect(CoreException.class);
            this.expectedException
                    .expectMessage("Could not create directories for path /Users/bazbat/file");
            file.child("subdir");
        }
        catch (final IOException exception)
        {
            throw new CoreException("FileSystem operation failed", exception);
        }
    }

    @Test
    public void testChildFailDueToParentIsSymlinkToFile()
    {
        try (FileSystem filesystem = Jimfs.newFileSystem(Configuration.osX()))
        {
            final Path filePath = filesystem.getPath("/Users/bazbat/file");
            final File file = new File(filePath, true);
            file.writeAndClose("foobar");
            final Path linkPath = filesystem.getPath("/Users/bazbat/link");
            Files.createSymbolicLink(linkPath, filePath);
            final File linkFile = new File(linkPath);

            this.expectedException.expect(CoreException.class);
            this.expectedException.expectMessage(
                    "Cannot create the child of file /Users/bazbat/link since it did not resolve to a directory");
            linkFile.child("subdir");
        }
        catch (final IOException exception)
        {
            throw new CoreException("FileSystem operation failed", exception);
        }
    }

    @Test
    public void testConstructors()
    {
        try (FileSystem filesystem = Jimfs.newFileSystem(Configuration.osX()))
        {
            final Path filePath = filesystem.getPath("/Users/foobar/file.gz");
            final File file = new File(filePath, true);
            file.writeAndClose("foobar");
            Assert.assertEquals("foobar", file.all());

            final Path file2Path = filesystem.getPath("/Users/bazbat/file");
            final File file2 = new File(file2Path, false);
            Assert.assertFalse(file2.exists());
            final File dir = new File("/Users/bazbat", filesystem, false);
            Assert.assertFalse(dir.exists());
        }
        catch (final IOException exception)
        {
            throw new CoreException("FileSystem operation failed", exception);
        }
    }

    @Test
    public void testDelete()
    {
        try (FileSystem filesystem = Jimfs.newFileSystem(Configuration.osX()))
        {
            final Path filePath = filesystem.getPath("/Users/foobar/file");
            final File file = new File(filePath);
            file.writeAndClose("foobar");
            Assert.assertTrue(file.exists());
            Assert.assertEquals("foobar", file.all());
            file.delete();
            Assert.assertFalse(file.exists());
        }
        catch (final IOException exception)
        {
            throw new CoreException("FileSystem operation failed", exception);
        }
    }

    @Test
    public void testDeleteFailDueToNonExistentFile()
    {
        try (FileSystem filesystem = Jimfs.newFileSystem(Configuration.osX()))
        {
            final Path filePath = filesystem.getPath("/Users/foobar/file");
            final File file = new File(filePath);
            Assert.assertFalse(file.exists());

            this.expectedException.expect(CoreException.class);
            this.expectedException.expectMessage("Cannot delete file /Users/foobar/file");
            file.delete();
        }
        catch (final IOException exception)
        {
            throw new CoreException("FileSystem operation failed", exception);
        }
    }

    @Test
    public void testDeleteRecursive()
    {
        try (FileSystem filesystem = Jimfs.newFileSystem(Configuration.osX()))
        {
            final Path homePath = filesystem.getPath("/Users/foobar");
            final File home = new File(homePath);
            final File child1 = home.child("child1");
            child1.writeAndClose("foobar");
            final File child2 = home.child("child2");
            final File child3 = child2.child("child3");
            child3.writeAndClose("foobar");

            Assert.assertTrue(home.exists());
            Assert.assertTrue(home.isDirectory());
            home.deleteRecursively();
            Assert.assertFalse(home.exists());
            Assert.assertTrue(new File(filesystem.getPath("/Users")).exists());
        }
        catch (final IOException exception)
        {
            throw new CoreException("FileSystem operation failed", exception);
        }
    }

    @Test
    public void testDeleteRecursiveFailDueToNonExistentFile()
    {
        try (FileSystem filesystem = Jimfs.newFileSystem(Configuration.osX()))
        {
            final Path filePath = filesystem.getPath("/Users/foobar/file");
            final File file = new File(filePath);
            Assert.assertFalse(file.exists());

            this.expectedException.expect(CoreException.class);
            this.expectedException
                    .expectMessage("Cannot delete folder /Users/foobar/file recursively");
            file.deleteRecursively();
        }
        catch (final IOException exception)
        {
            throw new CoreException("FileSystem operation failed", exception);
        }
    }

    @Test
    public void testFileComparison()
    {
        try (FileSystem filesystem = Jimfs.newFileSystem(Configuration.osX()))
        {
            final Path file1Path = filesystem.getPath("/Users/foobar/file1");
            final File file1 = new File(file1Path);
            final Path file2Path = filesystem.getPath("/Users/foobar/file2");
            final File file2 = new File(file2Path);
            file1.writeAndClose("foobar\n");
            file2.writeAndClose("foobar\n");

            Assert.assertTrue(file1.compareTo(file2) < 0);
        }
        catch (final IOException exception)
        {
            throw new CoreException("FileSystem operation failed", exception);
        }
    }

    @Test
    public void testFileEqualityAndHashcodeContracts()
    {
        try (FileSystem filesystem = Jimfs.newFileSystem(Configuration.osX()))
        {
            final Path file1Path = filesystem.getPath("/Users/foobar/file1");
            final File file1 = new File(file1Path);
            final Path file2Path = filesystem.getPath("/Users/foobar/file2");
            final File file2 = new File(file2Path);

            final Path file1APath = filesystem.getPath("/Users/foobar/file1");
            final File file1A = new File(file1APath);
            final Path file2APath = filesystem.getPath("/Users/foobar/file2");
            final File file2A = new File(file2APath);

            Assert.assertEquals(file1, file1);
            Assert.assertEquals(file1, file1A);
            Assert.assertEquals(file1A, file1A);
            Assert.assertEquals(file1.hashCode(), file1.hashCode());
            Assert.assertEquals(file1.hashCode(), file1A.hashCode());
            Assert.assertEquals(file1A.hashCode(), file1A.hashCode());

            Assert.assertEquals(file2, file2);
            Assert.assertEquals(file2, file2A);
            Assert.assertEquals(file2A, file2A);
            Assert.assertEquals(file2.hashCode(), file2.hashCode());
            Assert.assertEquals(file2.hashCode(), file2A.hashCode());
            Assert.assertEquals(file2A.hashCode(), file2A.hashCode());

            Assert.assertNotEquals(file1, file2);
            Assert.assertNotEquals(file1, file2A);
            Assert.assertNotEquals(file1A, file2);
            Assert.assertNotEquals(file1A, file2A);
            Assert.assertNotEquals(file1.hashCode(), file2.hashCode());
            Assert.assertNotEquals(file1.hashCode(), file2A.hashCode());
            Assert.assertNotEquals(file1A.hashCode(), file2.hashCode());
            Assert.assertNotEquals(file1A.hashCode(), file2A.hashCode());

            Assert.assertNotEquals(file1, "somerandomthing");
        }
        catch (final IOException exception)
        {
            throw new CoreException("FileSystem operation failed", exception);
        }
    }

    @Test
    public void testFileReadWrite()
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

            // Now we read by following a symlink
            final Path symlinkPath = filesystem.getPath("/tmp/bar");
            Files.createSymbolicLink(symlinkPath, filePath);
            final File symlink = new File(symlinkPath);
            Assert.assertEquals("one\ntwo\nthree", symlink.all());

            // Now let's try writing/reading with compression
            final Path compressedPath = filesystem.getPath("/tmp/compressed.gz");
            final File compressedFile = new File(compressedPath).withCompressor(Compressor.GZIP)
                    .withDecompressor(Decompressor.GZIP);
            compressedFile.writeAndClose("one\ntwo\nthree");
            Assert.assertTrue(compressedFile.isGzipped());
            Assert.assertEquals("one\ntwo\nthree", compressedFile.all());
        }
        catch (final IOException exception)
        {
            throw new CoreException("FileSystem operation failed", exception);
        }
    }

    @Test
    public void testLength()
    {
        try (FileSystem filesystem = Jimfs.newFileSystem(Configuration.osX()))
        {
            final Path filePath = filesystem.getPath("/Users/foobar/file");
            final File file = new File(filePath);
            file.writeAndClose("foobar\n");

            Assert.assertEquals(7L, file.length());
        }
        catch (final IOException exception)
        {
            throw new CoreException("FileSystem operation failed", exception);
        }
    }

    @Test
    public void testLengthFailDueToNonexistentFile()
    {
        try (FileSystem filesystem = Jimfs.newFileSystem(Configuration.osX()))
        {
            final Path filePath = filesystem.getPath("/Users/foobar/file");
            final File file = new File(filePath);

            this.expectedException.expect(CoreException.class);
            this.expectedException.expectMessage("Could not get length of file /Users/foobar/file");
            file.length();
        }
        catch (final IOException exception)
        {
            throw new CoreException("FileSystem operation failed", exception);
        }
    }

    @Test
    public void testListFiles()
    {
        try (FileSystem filesystem = Jimfs.newFileSystem(Configuration.osX()))
        {
            final File home = new File(filesystem.getPath("/Users/foobar"));
            final File file1 = home.child("file1");
            file1.writeAndClose("foobar\n");
            final File file2 = home.child("file2");
            file2.writeAndClose("bazbat\n");
            final File dir1 = home.child("dir1");
            final File file3 = dir1.child("file3");
            file3.writeAndClose("fred\n");

            Assert.assertEquals(Sets.hashSet(file1, file2, dir1),
                    new HashSet<>(home.listFiles(true)));
            Assert.assertEquals(Sets.hashSet(file1, file2), new HashSet<>(home.listFiles()));
            Assert.assertEquals(Sets.hashSet(file1), new HashSet<>(file1.listFiles()));

            Assert.assertTrue(
                    new HashSet<>(new File(filesystem.getPath("/foo/bar")).listFiles()).isEmpty());
        }
        catch (final IOException exception)
        {
            throw new CoreException("FileSystem operation failed", exception);
        }
    }

    @Test
    public void testListFilesRecursively()
    {
        try (FileSystem filesystem = Jimfs.newFileSystem(Configuration.osX()))
        {
            final File home = new File(filesystem.getPath("/Users/foobar"));
            final File file1 = home.child("file1");
            file1.writeAndClose("foobar\n");
            final File file2 = home.child("file2");
            file2.writeAndClose("bazbat\n");
            final File dir1 = home.child("dir1");
            final File file3 = dir1.child("file3");
            file3.writeAndClose("fred\n");
            final File dir2 = dir1.child("dir2");
            final File file4 = dir2.child("file4");
            file4.writeAndClose("ned\n");

            Assert.assertEquals(Sets.hashSet(file1, file2, file3, file4, dir1, dir2),
                    new HashSet<>(home.listFilesRecursively(true)));
            Assert.assertEquals(Sets.hashSet(file1, file2, file3, file4),
                    new HashSet<>(home.listFilesRecursively()));
            Assert.assertEquals(Sets.hashSet(file1), new HashSet<>(file1.listFilesRecursively()));
            Assert.assertTrue(
                    new HashSet<>(new File(filesystem.getPath("/foo/bar")).listFilesRecursively())
                            .isEmpty());
        }
        catch (final IOException exception)
        {
            throw new CoreException("FileSystem operation failed", exception);
        }
    }

    @Test
    public void testMkdirs()
    {
        try (FileSystem filesystem = Jimfs.newFileSystem(Configuration.osX()))
        {
            final Path homeFolderPath = filesystem.getPath("/Users/foobar");
            final File homeFolder = new File(homeFolderPath, false);
            Assert.assertFalse(Files.exists(filesystem.getPath("/Users")));
            Assert.assertFalse(Files.exists(homeFolder.toPath()));
            homeFolder.mkdirs();
            Assert.assertTrue(Files.exists(filesystem.getPath("/Users")));
            Assert.assertTrue(Files.exists(homeFolder.toPath()));
            Assert.assertTrue(Files.isDirectory(homeFolder.toPath()));

            final File homeChildFolder = homeFolder.child("subdir");
            Assert.assertFalse(Files.exists(homeChildFolder.toPath()));
            homeChildFolder.mkdirs();
            Assert.assertTrue(Files.exists(homeChildFolder.toPath()));
            Assert.assertTrue(Files.isDirectory(homeChildFolder.toPath()));
        }
        catch (final IOException exception)
        {
            throw new CoreException("FileSystem operation failed", exception);
        }
    }

    @Test
    public void testParent()
    {
        try (FileSystem filesystem = Jimfs.newFileSystem(Configuration.osX()))
        {
            final Path filePath = filesystem.getPath("/Users/foobar/file");
            final File file = new File(filePath);
            file.writeAndClose("foobar\n");

            Assert.assertEquals("/Users/foobar", file.parent().getAbsolutePathString());
        }
        catch (final IOException exception)
        {
            throw new CoreException("FileSystem operation failed", exception);
        }
    }

    @Test
    public void testProperSymlinkHandling()
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

            final File desktopFolder = new File(desktopPath);

            Assert.assertTrue(Files.exists(desktopFolder.toPath()));
            Assert.assertTrue(Files.isDirectory(desktopFolder.toPath()));

            final File symlinkToDesktop = new File(symlinkToDesktopPath);
            final File fileOnDesktopThruSymlink = symlinkToDesktop.child("file_on_desktop");
            final File nonexistentFileOnDesktopThruSymlink = symlinkToDesktop
                    .child("nonexistant_file_on_desktop");

            Assert.assertTrue(fileOnDesktopThruSymlink.exists());
            Assert.assertFalse(nonexistentFileOnDesktopThruSymlink.exists());
            nonexistentFileOnDesktopThruSymlink.writeAndClose("foobar");
            Assert.assertTrue(nonexistentFileOnDesktopThruSymlink.exists());
        }
        catch (final IOException exception)
        {
            throw new CoreException("FileSystem operation failed", exception);
        }
    }

    @Test
    public void testReadFailDueToNonexistentFile()
    {
        try (FileSystem filesystem = Jimfs.newFileSystem(Configuration.osX()))
        {
            final Path filePath = filesystem.getPath("/Users/foobar/file");
            final File file = new File(filePath);
            Assert.assertFalse(file.exists());

            this.expectedException.expect(CoreException.class);
            this.expectedException.expectMessage("Cannot read file /Users/foobar/file");
            file.all();
        }
        catch (final IOException exception)
        {
            throw new CoreException("FileSystem operation failed", exception);
        }
    }

    @Test
    public void testStringMethods()
    {
        try (FileSystem filesystem = Jimfs.newFileSystem(Configuration.osX()))
        {
            final Path filePath = filesystem.getPath("/Users/foobar/file");
            final File file = new File(filePath);
            file.writeAndClose("foobar");

            Assert.assertEquals("file", file.basename());
            Assert.assertEquals("file", file.getName());
            file.withName("myFile");
            Assert.assertEquals("myFile", file.getName());
            Assert.assertEquals("/Users/foobar/file", file.getAbsolutePathString());
            Assert.assertEquals("/Users/foobar/file", file.toString());
            Assert.assertEquals("/Users/foobar/file", file.getPathString());
            Assert.assertEquals("/Users/foobar", file.getParentPathString());
        }
        catch (final IOException exception)
        {
            throw new CoreException("FileSystem operation failed", exception);
        }
    }

    @Test
    public void testTemporaryFile()
    {
        try (FileSystem filesystem = Jimfs.newFileSystem(Configuration.osX()))
        {
            final TemporaryFile file = File.temporary(filesystem);
            try (file)
            {
                file.writeAndClose("foobar");
                Assert.assertEquals("foobar", file.all());
            }
            Assert.assertFalse(file.exists());
        }
        catch (final IOException exception)
        {
            throw new CoreException("FileSystem operation failed", exception);
        }
    }

    @Test
    public void testTemporaryFolder()
    {
        try (FileSystem filesystem = Jimfs.newFileSystem(Configuration.osX()))
        {
            final File file;
            final TemporaryFile folder = File.temporaryFolder(filesystem);
            try (folder)
            {
                file = folder.child("child");
                file.writeAndClose("foobar");
                Assert.assertEquals("foobar", file.all());
            }
            Assert.assertFalse(file.exists());
            Assert.assertFalse(folder.exists());
        }
        catch (final IOException exception)
        {
            throw new CoreException("FileSystem operation failed", exception);
        }
    }

    @Test
    public void testWriteFailDueToFileIsDirectory()
    {
        try (FileSystem filesystem = Jimfs.newFileSystem(Configuration.osX()))
        {
            final Path filePath = filesystem.getPath("/Users/foobar/dir");
            final File file = new File(filePath);
            file.mkdirs();
            Assert.assertTrue(file.isDirectory());

            this.expectedException.expect(CoreException.class);
            this.expectedException.expectMessage("Could not write to /Users/foobar/dir");
            file.writeAndClose("foobar");
        }
        catch (final IOException exception)
        {
            throw new CoreException("FileSystem operation failed", exception);
        }
    }
}
