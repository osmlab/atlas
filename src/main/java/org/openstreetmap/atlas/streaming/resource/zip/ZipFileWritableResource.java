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
            ZipEntry currentZipEntry;
            final List<Resource> resources = new ArrayList<>();
            while ((currentZipEntry = iteratorStream.getNextEntry()) != null)
            {
                final String entryName = currentZipEntry.getName();
                resources.add(new InputStreamResource(() ->
                {
                    final ZipInputStream zipStream = getZipInputStream();
                    seekStreamAheadToEntry(zipStream, entryName);
                    return zipStream;
                }).withName(entryName));
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
        return new InputStreamResource(() ->
        {
            final ZipInputStream zipStream = getZipInputStream();
            seekStreamAheadToEntry(zipStream, name);
            return zipStream;
        }).withName(name);
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

    private void seekStreamAheadToEntry(final ZipInputStream streamToSeekThrough,
            final String entryNameToSeek)
    {
        if (entryNameToSeek == null)
        {
            throw new CoreException("Cannot seek for null entry name");
        }

        String currentEntryName = null;
        while (!Objects.equals(currentEntryName, entryNameToSeek))
        {
            try
            {
                final ZipEntry currentEntry = streamToSeekThrough.getNextEntry();
                if (currentEntry == null)
                {
                    break;
                }
                currentEntryName = currentEntry.getName();
            }
            catch (final IOException exception)
            {
                throw new CoreException("IOException while getting next entry", exception);
            }
        }

        /*
         * If we make it here, then we didn't find the entry we were looking for - these aren't the
         * entries you're looking for.
         */
        if (!Objects.equals(currentEntryName, entryNameToSeek))
        {
            throw new CoreException("No such entry {} found in stream", entryNameToSeek);
        }
    }
}
