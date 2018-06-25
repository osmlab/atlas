package org.openstreetmap.atlas.streaming.resource.zip;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Iterator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.streaming.Streams;
import org.openstreetmap.atlas.streaming.compression.Compressor;
import org.openstreetmap.atlas.streaming.resource.ByteArrayResource;
import org.openstreetmap.atlas.streaming.resource.File;
import org.openstreetmap.atlas.streaming.resource.InputStreamResource;
import org.openstreetmap.atlas.streaming.resource.Resource;
import org.openstreetmap.atlas.streaming.resource.StringResource;
import org.openstreetmap.atlas.utilities.random.RandomTextGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author matthieun
 */
public class ZipResourceTest
{
    /**
     * @author matthieun
     */
    private static class TwoStringObject implements Serializable
    {
        private static final long serialVersionUID = 2404840862177380282L;
        private final String one;
        private final String two;

        TwoStringObject(final long size)
        {
            this.one = new RandomTextGenerator().generate(size);
            this.two = new RandomTextGenerator().generate(size);
        }

        public String getOne()
        {
            return this.one;
        }

        public String getTwo()
        {
            return this.two;
        }
    }

    private static final Logger logger = LoggerFactory.getLogger(ZipResourceTest.class);

    public static final String NAME_1 = "entry1.txt";
    public static final String CONTENTS_1 = "I am entry 1.";
    public static final String NAME_2 = "entry2.txt";
    public static final String CONTENTS_2 = "I am entry 2.";

    public static void main(final String[] args)
    {
        try
        {
            new ZipResourceTest().testSizes();
        }
        catch (final Exception e)
        {
            e.printStackTrace();
        }
        // This is used to re-create the "test.zip" in the resources package.
        // final ZipWritableResource writable = new ZipWritableResource(new File(args[0]));
        // writable.writeAndClose(new StringResource(CONTENTS_1).withName(NAME_1),
        // new StringResource(CONTENTS_2).withName(NAME_2));
    }

    @Test
    public void testHighCompressionLevel() throws IOException
    {
        final File source = File.temporary();
        logger.info("testCompressionLevel using {}", source);
        try
        {
            final ZipFileWritableResource zipFile = new ZipFileWritableResource(source);
            zipFile.writeAndClose(
                    new StringResource("HereIsSomeTextThatRepeatsHereIsSomeTextThatRepeats")
                            .withName(NAME_1),
                    new StringResource("HereIsSomeTextThatDoesn'tRepeat").withName(NAME_2));
            final ZipFile file = new ZipFile(source.getFile());
            final ZipEntry name1 = file.getEntry(NAME_1);
            Assert.assertNotEquals(-1, name1.getCompressedSize());
            Assert.assertTrue(name1.getCompressedSize() < name1.getSize());
            final ZipEntry name2 = file.getEntry(NAME_2);
            Assert.assertNotEquals(-1, name2.getCompressedSize());
            Assert.assertTrue(name2.getCompressedSize() >= name2.getSize());
            file.close();
        }
        finally
        {
            source.delete();
            logger.info("testZipFile deleted {}", source);
        }
    }

    @Test
    public void testNoCompressionLevel() throws IOException
    {
        final File source = File.temporary();
        logger.info("testCompressionLevel using {}", source);
        try
        {
            final ZipFileWritableResource zipFile = new ZipFileWritableResource(source);
            zipFile.setWriteCompression(false);
            zipFile.writeAndClose(
                    new StringResource("HereIsSomeTextThatRepeatsHereIsSomeTextThatRepeats")
                            .withName(NAME_1),
                    new StringResource("HereIsSomeTextThatDoesn'tRepeat").withName(NAME_2));
            final ZipFile file = new ZipFile(source.getFile());
            final ZipEntry name1 = file.getEntry(NAME_1);
            Assert.assertNotEquals(-1, name1.getCompressedSize());
            Assert.assertTrue(name1.getCompressedSize() >= name1.getSize());
            final ZipEntry name2 = file.getEntry(NAME_2);
            Assert.assertNotEquals(-1, name2.getCompressedSize());
            Assert.assertTrue(name2.getCompressedSize() >= name2.getSize());
            file.close();
        }
        finally
        {
            source.delete();
            logger.info("testZipFile deleted {}", source);
        }
    }

    public void testSizes() throws Exception
    {
        final long size = 100000;
        final ByteArrayResource javaOutput = new ByteArrayResource().withName("Java");
        final ByteArrayResource zippedOutput = new ByteArrayResource().withName("Zipped");
        javaOutput.setCompressor(Compressor.GZIP);
        zippedOutput.setCompressor(Compressor.GZIP);
        final TwoStringObject object = new TwoStringObject(size);
        final Resource oneResource = new StringResource(object.getOne()).withName("one");
        final Resource twoResource = new StringResource(object.getTwo()).withName("two");

        final ObjectOutputStream out = new ObjectOutputStream(javaOutput.write());
        out.writeObject(object);
        Streams.close(out);

        new ZipWritableResource(zippedOutput).writeAndClose(oneResource, twoResource);

        System.out.println(javaOutput.getName() + " " + javaOutput.length());
        System.out.println(zippedOutput.getName() + " " + zippedOutput.length());
    }

    @Test
    public void testZipFile()
    {
        final File source = File.temporary();
        logger.info("testZipFile using {}", source);
        try
        {
            final ZipFileWritableResource zipFile = new ZipFileWritableResource(source);
            zipFile.writeAndClose(new StringResource(CONTENTS_1).withName(NAME_1),
                    new StringResource(CONTENTS_2).withName(NAME_2));
            // Try to access entries randomly
            final Resource entry2 = zipFile.entryForName(NAME_2);
            final String name2 = entry2.getName();
            final String contents2 = entry2.all();
            logger.info(name2 + " -> " + contents2);
            Assert.assertEquals(NAME_2, name2);
            Assert.assertEquals(CONTENTS_2, contents2);
            final Resource entry1 = zipFile.entryForName(NAME_1);
            final String name1 = entry1.getName();
            final String contents1 = entry1.all();
            logger.info(name1 + " -> " + contents1);
            Assert.assertEquals(NAME_1, name1);
            Assert.assertEquals(CONTENTS_1, contents1);
        }
        finally
        {
            source.delete();
            logger.info("testZipFile deleted {}", source);
        }
    }

    @Test
    public void testZipFileEntries()
    {
        final File source = File.temporary();
        logger.info("testZipFileEntries using {}", source);
        try
        {
            final ZipFileWritableResource zipFile = new ZipFileWritableResource(source);
            zipFile.writeAndClose(new StringResource(CONTENTS_1).withName(NAME_1),
                    new StringResource(CONTENTS_2).withName(NAME_2));
            int counter = 0;
            for (final Resource entry : zipFile.entries())
            {
                final String name = entry.getName();
                final String contents = entry.all();
                logger.info(name + " -> " + contents);
                if (counter == 0)
                {
                    Assert.assertEquals(NAME_1, name);
                    Assert.assertEquals(CONTENTS_1, contents);
                }
                else
                {
                    Assert.assertEquals(NAME_2, name);
                    Assert.assertEquals(CONTENTS_2, contents);
                }
                counter++;
            }
        }
        finally
        {
            source.delete();
            logger.info("testZipFileEntries deleted {}", source);
        }
    }

    @Test
    public void testZipResource()
    {
        final ZipResource resource = new ZipResource(
                new InputStreamResource(ZipResourceTest.class.getResourceAsStream("test.zip")));
        int counter = 0;
        for (final Resource entry : resource.entries())
        {
            final String name = entry.getName();
            final String contents = entry.all();
            logger.info(name + " -> " + contents);
            if (counter == 0)
            {
                Assert.assertEquals(NAME_1, name);
                Assert.assertEquals(CONTENTS_1, contents);
            }
            else
            {
                Assert.assertEquals(NAME_2, name);
                Assert.assertEquals(CONTENTS_2, contents);
            }
            counter++;
        }
    }

    @Test
    public void testZipResourceStopBefore()
    {
        final ZipResource resource = new ZipResource(
                new InputStreamResource(ZipResourceTest.class.getResourceAsStream("test.zip")));
        final Iterator<Resource> entryIterator = resource.entries().iterator();
        entryIterator.next();
        final String failMessage = "Should not have been able to print the contents of "
                + "the second entry after not reading the first.";
        try
        {
            System.out.println(entryIterator.next());
            Assert.fail(failMessage);
        }
        catch (final CoreException e)
        {
            if (!ZipResource.PREMATURE_READ_ERROR_MESSAGE.equals(e.getMessage()))
            {
                throw e;
            }
        }
    }

    @Test
    public void testZipWritableResource()
    {
        final ByteArrayResource source = new ByteArrayResource();
        final ZipWritableResource writable = new ZipWritableResource(source);
        writable.writeAndClose(new StringResource("byte! " + CONTENTS_1).withName(NAME_1),
                new StringResource("byte! " + CONTENTS_2).withName(NAME_2));
        final ZipResource resource = new ZipResource(source);
        int counter = 0;
        for (final Resource entry : resource.entries())
        {
            final String name = entry.getName();
            final String contents = entry.all();
            logger.info(name + " -> " + contents);
            if (counter == 0)
            {
                Assert.assertEquals(NAME_1, name);
                Assert.assertEquals("byte! " + CONTENTS_1, contents);
            }
            else
            {
                Assert.assertEquals(NAME_2, name);
                Assert.assertEquals("byte! " + CONTENTS_2, contents);
            }
            counter++;
        }
    }
}
