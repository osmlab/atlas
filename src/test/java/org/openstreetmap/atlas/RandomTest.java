package org.openstreetmap.atlas;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.Test;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;

/**
 * @author lcram
 */
public class RandomTest
{
    @Test
    public void test() throws IOException
    {
        final FileSystem fileSystem = Jimfs.newFileSystem(Configuration.unix());
        final Path foo = fileSystem.getPath("/foo");
        Files.createDirectory(foo);

        final Path hello = foo.resolve("hello.txt");

        System.out.println(hello.toAbsolutePath().toString());
    }
}
