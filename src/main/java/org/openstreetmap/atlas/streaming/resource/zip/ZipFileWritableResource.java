package org.openstreetmap.atlas.streaming.resource.zip;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.streaming.resource.File;
import org.openstreetmap.atlas.streaming.resource.InputStreamResource;
import org.openstreetmap.atlas.streaming.resource.Resource;

/**
 * Zip wrapper for a {@link File} resource. This enables random lookups which are not available in
 * the case of a {@link ZipResource}
 *
 * @author matthieun
 */
public class ZipFileWritableResource extends ZipWritableResource
{
    public ZipFileWritableResource(final File source)
    {
        super(source);
    }

    @Override
    public Iterable<Resource> entries()
    {
        try
        {
            final ZipInputStream iteratorStream = getZipInputStream();
            ZipEntry zipEntry;
            final List<Resource> resources = new ArrayList<>();
            while ((zipEntry = iteratorStream.getNextEntry()) != null)
            {
                final ZipInputStream stream = getZipInputStream();
                int number = 0;
                // while loop to skip the stream ahead to start at the given ZipEntry
                while (!stream.getNextEntry().getName().equals(zipEntry.getName()))
                {
                    // meaningless statement to satisfy checkstyle
                    number++;
                }
                resources.add(new InputStreamResource(() -> stream).withName(zipEntry.getName()));
            }
            return resources;
        }
        catch (final IOException exception)
        {
            throw new CoreException("Could not read entries for {}",
                    getFileSource().getAbsolutePathString(), exception);
        }
    }

    public Resource entryForName(final String name)
    {
        try
        {
            final ZipInputStream zipStream = getZipInputStream();
            ZipEntry zipEntry;
            while ((zipEntry = zipStream.getNextEntry()) != null)
            {
                if (Objects.equals(zipEntry.getName(), name))
                {
                    return new InputStreamResource(() -> zipStream).withName(name);
                }
            }
            throw new IOException("Entry " + name + " does not exist.");
        }
        catch (final IOException exception)
        {
            throw new CoreException("Cannot get the entry {} from the Zipfile {}.", name,
                    this.getFileSource().getName(), exception);
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

    protected ZipInputStream getZipInputStream()
    {
        return new ZipInputStream(new BufferedInputStream(getFileSource().read()));
    }
}
