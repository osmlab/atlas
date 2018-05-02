package org.openstreetmap.atlas.streaming.resource.zip;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.compress.utils.IOUtils;
import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.streaming.resource.Resource;
import org.openstreetmap.atlas.streaming.resource.WritableResource;
import org.openstreetmap.atlas.utilities.collections.Iterables;

/**
 * Zipped {@link WritableResource} flavored wrapper using {@link ZipOutputStream}
 *
 * @author matthieun
 */
public class ZipWritableResource extends ZipResource
{
    private static final int ZIP_MAXIMUM_COMPRESSION_LEVEL = 9;

    private boolean compression = true;

    public ZipWritableResource(final WritableResource source)
    {
        super(source);
    }

    /**
     * @param compression
     *            True to compress the zip archive when writing.
     */
    public void setWriteCompression(final boolean compression)
    {
        this.compression = compression;
    }

    /**
     * @param compression
     *            True to compress the zip archive when writing.
     * @return The same object
     */
    public ZipWritableResource withWriteCompression(final boolean compression)
    {
        setWriteCompression(compression);
        return this;
    }

    /**
     * Write a set of {@link Resource}s to the zip resource and close the stream.
     *
     * @param entries
     *            The {@link Resource}s to write to the zip resource.
     */
    public void writeAndClose(final Iterable<? extends Resource> entries)
    {
        try (ZipOutputStream output = new ZipOutputStream(
                new BufferedOutputStream(getWritableSource().write())))
        {
            output.setLevel(
                    this.compression ? ZIP_MAXIMUM_COMPRESSION_LEVEL : Deflater.NO_COMPRESSION);
            int counter = 0;
            for (final Resource resource : entries)
            {
                String name = resource.getName();
                if (name == null)
                {
                    name = "Entry " + counter;
                }
                final ZipEntry entry = new ZipEntry(resource.getName());
                output.putNextEntry(entry);
                try (InputStream input = resource.read())
                {
                    IOUtils.copy(input, output);
                    counter++;
                }
                catch (final Exception e)
                {
                    throw new CoreException("Unable to read resource {}", resource, e);
                }
            }
        }
        catch (final IOException e)
        {
            throw new CoreException("Unable to write next ZipEntry!", e);
        }
    }

    /**
     * Write a set of {@link Resource}s to the zip resource and close the stream.
     *
     * @param entries
     *            The {@link Resource}s to write to the zip resource.
     */
    public void writeAndClose(final Resource... entries)
    {
        writeAndClose(Iterables.asList(entries));
    }

    protected WritableResource getWritableSource()
    {
        return (WritableResource) getSource();
    }
}
