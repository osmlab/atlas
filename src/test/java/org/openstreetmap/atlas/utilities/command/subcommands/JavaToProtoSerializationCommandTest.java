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
import org.openstreetmap.atlas.geography.atlas.AtlasResourceLoader;
import org.openstreetmap.atlas.geography.atlas.packed.PackedAtlas;
import org.openstreetmap.atlas.geography.atlas.packed.PackedAtlasBuilder;
import org.openstreetmap.atlas.streaming.resource.File;
import org.openstreetmap.atlas.utilities.collections.Maps;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;

/**
 * @author lcram
 */
public class JavaToProtoSerializationCommandTest
{
    @Test
    public void test()
    {
        try (FileSystem filesystem = Jimfs.newFileSystem(Configuration.osX()))
        {
            setupFilesystem1(filesystem);
            final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
            final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
            final JavaToProtoSerializationCommand command = new JavaToProtoSerializationCommand();
            command.setNewFileSystem(filesystem);
            command.setNewOutStream(new PrintStream(outContent));
            command.setNewErrStream(new PrintStream(errContent));

            command.runSubcommand("/Users/foo/java-atlas.atlas", "--verbose");

            Assert.assertEquals("Saved to /work/java-atlas.atlas\n", outContent.toString());
            Assert.assertEquals(
                    "java2proto: loading /Users/foo/java-atlas.atlas\n"
                            + "java2proto: processing atlas /Users/foo/java-atlas.atlas (1/1)\n",
                    errContent.toString());
            Assert.assertEquals(PackedAtlas.AtlasSerializationFormat.PROTOBUF,
                    ((PackedAtlas) new AtlasResourceLoader()
                            .load(new File("/work/java-atlas.atlas", filesystem)))
                                    .getSerializationFormat());

        }
        catch (final IOException exception)
        {
            throw new CoreException("FileSystem operation failed", exception);
        }
    }

    @Test
    public void testCheck()
    {
        try (FileSystem filesystem = Jimfs.newFileSystem(Configuration.osX()))
        {
            setupFilesystem1(filesystem);
            final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
            final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
            final JavaToProtoSerializationCommand command = new JavaToProtoSerializationCommand();
            command.setNewFileSystem(filesystem);
            command.setNewOutStream(new PrintStream(outContent));
            command.setNewErrStream(new PrintStream(errContent));

            command.runSubcommand("/Users/foo/proto-atlas.atlas", "/Users/foo/java-atlas.atlas",
                    "--check", "--verbose");

            Assert.assertEquals(
                    "atlas /Users/foo/proto-atlas.atlas format: PROTOBUF\n"
                            + "atlas /Users/foo/java-atlas.atlas format: JAVA\n",
                    outContent.toString());
            Assert.assertEquals(
                    "java2proto: loading /Users/foo/proto-atlas.atlas\n"
                            + "java2proto: loading /Users/foo/java-atlas.atlas\n"
                            + "java2proto: processing atlas /Users/foo/proto-atlas.atlas (1/2)\n"
                            + "java2proto: processing atlas /Users/foo/java-atlas.atlas (2/2)\n",
                    errContent.toString());
        }
        catch (final IOException exception)
        {
            throw new CoreException("FileSystem operation failed", exception);
        }
    }

    @Test
    public void testReverse()
    {
        try (FileSystem filesystem = Jimfs.newFileSystem(Configuration.osX()))
        {
            setupFilesystem1(filesystem);
            final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
            final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
            final JavaToProtoSerializationCommand command = new JavaToProtoSerializationCommand();
            command.setNewFileSystem(filesystem);
            command.setNewOutStream(new PrintStream(outContent));
            command.setNewErrStream(new PrintStream(errContent));

            command.runSubcommand("/Users/foo/proto-atlas.atlas", "--reverse", "--verbose");

            Assert.assertEquals("Saved to /work/proto-atlas.atlas\n", outContent.toString());
            Assert.assertEquals(
                    "java2proto: loading /Users/foo/proto-atlas.atlas\n"
                            + "java2proto: processing atlas /Users/foo/proto-atlas.atlas (1/1)\n",
                    errContent.toString());
            Assert.assertEquals(PackedAtlas.AtlasSerializationFormat.JAVA,
                    ((PackedAtlas) new AtlasResourceLoader()
                            .load(new File("/work/proto-atlas.atlas", filesystem)))
                                    .getSerializationFormat());
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

        final File protoAtlasFile = new File("/Users/foo/proto-atlas.atlas", filesystem);
        final File javaAtlasFile = new File("/Users/foo/java-atlas.atlas", filesystem);

        ((PackedAtlas) atlas)
                .setSaveSerializationFormat(PackedAtlas.AtlasSerializationFormat.PROTOBUF);
        atlas.save(protoAtlasFile);

        ((PackedAtlas) atlas).setSaveSerializationFormat(PackedAtlas.AtlasSerializationFormat.JAVA);
        atlas.save(javaAtlasFile);
    }
}
