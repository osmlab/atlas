package org.openstreetmap.atlas.streaming.resource;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.apache.commons.io.IOUtils;
import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.streaming.NotifyingIOUtils;
import org.openstreetmap.atlas.streaming.NotifyingIOUtils.IOProgressListener;
import org.openstreetmap.atlas.streaming.Streams;
import org.openstreetmap.atlas.utilities.collections.StringList;

/**
 * A resource that contains data and is readable by default.
 *
 * @author matthieun
 */
public interface Resource
{
    int BYTE_MASK = 0x00FF;

    /**
     * @return The full contents of the {@link Resource} as a {@link String}
     */
    default String all()
    {
        return new StringList(lines()).join("\n");
    }

    /**
     * Copy all the contents of this {@link Resource} to a {@link WritableResource}
     *
     * @param output
     *            The output {@link WritableResource}
     */
    default void copyTo(final WritableResource output)
    {
        try (InputStream inputStream = read(); OutputStream outputStream = output.write())
        {
            IOUtils.copy(inputStream, outputStream);
        }
        catch (final Exception e)
        {
            throw new CoreException("Unable to copy {} to {}.", this, output, e);
        }
    }

    /**
     * Copy all of the contents of {@link Resource} to a {@link WritableResource} while notifying a
     * progress listener
     *
     * @param output
     *            The output {@link WritableResource}
     * @param listener
     *            The notification {@link IOProgressListener} called as data is being copied
     */
    default void copyTo(final WritableResource output,
            final NotifyingIOUtils.IOProgressListener listener)
    {
        try (InputStream inputStream = read(); OutputStream outputStream = output.write())
        {
            NotifyingIOUtils.copy(inputStream, outputStream, listener);
        }
        catch (final Exception e)
        {
            throw new CoreException("Unable to copy {} to {}.", this, output, e);
        }
    }

    /**
     * @return The first line in this resource
     */
    default String firstLine()
    {
        try (BufferedReader reader = reader())
        {
            return reader.readLine();
        }
        catch (final IOException e)
        {
            throw new CoreException("Unable to read first line of {}", this, e);
        }
    }

    /**
     * @return The optional name of the resource.
     */
    default String getName()
    {
        return null;
    }

    /**
     * @return True if the name of this resource lets believe that the resource contains Gzipped
     *         contents.
     */
    default boolean isGzipped()
    {
        return FileSuffix.GZIP.matches(this);
    }

    /**
     * @return The raw length on the {@link Resource} (as stored, regardless of compression).
     *         Depending on the type of the {@link Resource}, it can be really slow if there is no
     *         direct length meta data.
     */
    long length();

    /**
     * @return A String {@link Iterable} of all the lines in this resource.
     */
    default Iterable<String> lines()
    {
        final BufferedReader reader = reader();
        return () ->
        {
            try
            {
                return new Iterator<String>()
                {
                    private String line = reader.readLine();

                    @Override
                    public boolean hasNext()
                    {
                        return this.line != null;
                    }

                    @Override
                    public String next()
                    {
                        if (!hasNext())
                        {
                            throw new NoSuchElementException();
                        }
                        final String result = this.line;
                        populateNextLine();
                        return result;
                    }

                    private void populateNextLine()
                    {
                        try
                        {
                            this.line = reader.readLine();
                        }
                        catch (final IOException e)
                        {
                            Streams.close(reader);
                            throw new CoreException("Could not read resource line", e);
                        }
                        if (this.line == null)
                        {
                            Streams.close(reader);
                        }
                    }
                };
            }
            catch (final IOException e)
            {
                Streams.close(reader);
                throw new CoreException("Could not read resource line", e);
            }
        };
    }

    /**
     * @return A {@link StringList} of all the lines in this resource.
     */
    default StringList linesList()
    {
        return new StringList(lines());
    }

    /**
     * @return An {@link InputStream} streaming the contents of the resource
     */
    InputStream read();

    /**
     * @return The contents of the resource as a String
     */
    default String readAndClose()
    {
        final StringList builder = new StringList();
        lines().forEach(builder::add);
        return builder.join(System.lineSeparator());
    }

    /**
     * @return The contents of the resource as a byte[]
     */
    default byte[] readBytesAndClose()
    {
        final List<Byte> byteContents = new ArrayList<>();
        int kyte;
        try (InputStream input = new BufferedInputStream(read()))
        {
            while ((kyte = input.read()) >= 0)
            {
                byteContents.add((byte) (kyte & BYTE_MASK));
            }
        }
        catch (final IOException e)
        {
            throw new CoreException("Unable to read the bytes from {}.", this, e);
        }
        final byte[] contents = new byte[byteContents.size()];
        for (int index = 0; index < byteContents.size(); index++)
        {
            contents[index] = byteContents.get(index);
        }
        return contents;
    }

    /**
     * @return A {@link BufferedReader} on this resource.
     */
    default BufferedReader reader()
    {
        return new BufferedReader(new InputStreamReader(this.read(), StandardCharsets.UTF_8));
    }
}
