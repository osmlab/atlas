package org.openstreetmap.atlas.streaming.resource.zip;

import java.io.IOException;
import java.io.InputStream;
import java.util.function.Supplier;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.streaming.resource.File;
import org.openstreetmap.atlas.streaming.resource.InputStreamResource;
import org.openstreetmap.atlas.streaming.resource.Resource;
import org.openstreetmap.atlas.utilities.collections.Iterables;

/**
 * Zip wrapper for a {@link File} resource. This enables random lookups which are not available in
 * the case of a {@link ZipResource}
 *
 * @author matthieun
 */
public class ZipFileWritableResource extends ZipWritableResource
{
    private static Supplier<InputStream> inputStreamSupplier(final ZipFile file,
            final ZipEntry entry)
    {
        return () ->
        {
            try
            {
                return file.getInputStream(entry);
            }
            catch (final IOException e)
            {
                throw new CoreException("Cannot get the entry {}", entry.getName(), e);
            }
        };
    }

    public ZipFileWritableResource(final File source)
    {
        super(source);
    }

    @Override
    public Iterable<Resource> entries()
    {
        try (ZipFile file = new ZipFile(getFileSource().getFile())
        {
            @Override
            public void close()
            {
                // Do nothing to close the file here, to avoid cutting the legs off the just created
                // ZipEntry-based resource.
            }
        })
        {
            return Iterables.translate(Iterables.from(file.entries()), entry ->
            {
                return new InputStreamResource(inputStreamSupplier(file, entry))
                        .withName(entry.getName());
            });
        }
        catch (final IOException e)
        {
            throw new CoreException("Cannot get entries from the Zipfile {}.",
                    this.getFileSource().getName(), e);
        }
    }

    /**
     * Jump to a specific entry.
     *
     * @param name
     *            The name of the entry
     * @return The entry as a {@link Resource}. Throws a {@link CoreException} if it cannot find the
     *         entry.
     */
    public Resource entryForName(final String name)
    {
        try (ZipFile file = new ZipFile(getFileSource().getFile())
        {
            @Override
            public void close()
            {
                // Do nothing to close the file here, to avoid cutting the legs off the just created
                // ZipEntry-based resource.
            }
        })
        {
            final ZipEntry entry = file.getEntry(name);
            if (entry != null)
            {
                return new InputStreamResource(inputStreamSupplier(file, entry)).withName(name);
            }
            else
            {
                throw new IOException("Entry " + name + " does not exist.");
            }
        }
        catch (final IOException e)
        {
            throw new CoreException("Cannot get the entry {} from the Zipfile {}.", name,
                    this.getFileSource().getName(), e);
        }
    }

    @Override
    public ZipFileWritableResource withWriteCompression(final boolean compression)
    {
        setWriteCompression(compression);
        return this;
    }

    protected File getFileSource()
    {
        return (File) getWritableSource();
    }
}
