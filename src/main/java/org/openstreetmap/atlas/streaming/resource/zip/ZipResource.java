package org.openstreetmap.atlas.streaming.resource.zip;

import java.io.BufferedInputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.streaming.Streams;
import org.openstreetmap.atlas.streaming.resource.AbstractResource;
import org.openstreetmap.atlas.streaming.resource.Resource;

/**
 * Zipped {@link Resource} flavored wrapper using {@link ZipInputStream}
 *
 * @author matthieun
 */
public class ZipResource
{
    /**
     * @author matthieun
     */
    public static class ZipIterator implements Iterator<Resource>, Closeable
    {
        private final Resource source;
        private final ZipInputStream input;
        private ZipEntry nextEntry = null;
        private boolean doneReading = true;

        public ZipIterator(final Resource source)
        {
            this.source = source;
            this.input = new ZipInputStream(new BufferedInputStream(this.source.read()));
        }

        @Override
        public void close()
        {
            Streams.close(this.input);
        }

        @Override
        public boolean hasNext()
        {
            try
            {
                if (this.nextEntry == null)
                {
                    this.nextEntry = this.input.getNextEntry();
                }
                if (this.nextEntry == null)
                {
                    close();
                }
                return this.nextEntry != null;
            }
            catch (final IOException e)
            {
                throw new CoreException("Unable to go to next Zip Entry!", e);
            }
        }

        @Override
        public Resource next()
        {
            if (!this.doneReading)
            {
                throw new CoreException(PREMATURE_READ_ERROR_MESSAGE);
            }
            if (hasNext())
            {
                this.doneReading = false;
                final Resource result = new AbstractResource()
                {
                    private final String name = ZipIterator.this.nextEntry.getName();

                    @Override
                    public String getName()
                    {
                        return this.name;
                    }

                    @Override
                    protected InputStream onRead()
                    {
                        return new InputStream()
                        {
                            @Override
                            public void close()
                            {
                                // Trick to make sure the resource is read fully before moving
                                // to the next one.
                                ZipIterator.this.doneReading = true;
                            }

                            @Override
                            public int read() throws IOException
                            {
                                return ZipIterator.this.input.read();
                            }

                            @Override
                            public int read(final byte[] buffer, final int offset, final int length)
                                    throws IOException
                            {
                                return ZipIterator.this.input.read(buffer, offset, length);
                            }
                        };
                    }
                };
                this.nextEntry = null;
                return result;
            }
            else
            {
                throw new NoSuchElementException();
            }
        }
    }

    public static final String PREMATURE_READ_ERROR_MESSAGE = "Cannot go to the next ZipEntry before the previous one has been fully read.";

    private final Resource source;

    public ZipResource(final Resource source)
    {
        this.source = source;
    }

    /**
     * @return The entries of the file as an {@link Iterable} of {@link Resource}s. This works only
     *         if each resource is sequentially read.
     */
    public Iterable<Resource> entries()
    {
        return () -> new ZipIterator(getSource());
    }

    public String getName()
    {
        return this.source.getName();
    }

    @Override
    public String toString()
    {
        return this.getName();
    }

    protected Resource getSource()
    {
        return this.source;
    }
}
