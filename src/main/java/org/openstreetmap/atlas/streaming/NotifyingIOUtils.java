package org.openstreetmap.atlas.streaming;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.EventListener;
import java.util.Optional;

import org.openstreetmap.atlas.exception.CoreException;

/**
 * Helpful class for notifying a caller on status of copying data from one stream to anonther
 *
 * @author cstaylor
 */
public class NotifyingIOUtils
{
    /**
     * Notified on IO lifecycle events:
     * <ul>
     * <li>[1] started</li>
     * <li>[N] statusUpdate</li>
     * <li>[1] completed | failed</li>
     * </ul>
     *
     * @author cstaylor
     */
    public interface IOProgressListener extends EventListener
    {
        void completed();

        void failed(IOException oops);

        void started();

        void statusUpdate(long read);
    }

    private static final int EOF = -1;

    private static final int DEFAULT_BUFFER_SIZE = 4096;

    private Optional<IOProgressListener> progessListener;

    private int bufferSize;

    public static long copy(final InputStream input, final OutputStream output,
            final int bufferSize, final IOProgressListener listener) throws IOException
    {
        return new NotifyingIOUtils().withListener(listener).withBufferSize(bufferSize).copy(input,
                output);
    }

    public static long copy(final InputStream input, final OutputStream output,
            final IOProgressListener listener) throws IOException
    {
        return new NotifyingIOUtils().withListener(listener).copy(input, output);
    }

    public NotifyingIOUtils()
    {
        this.progessListener = Optional.empty();
        this.bufferSize = DEFAULT_BUFFER_SIZE;
    }

    public long copy(final InputStream input, final OutputStream output) throws IOException
    {
        final byte[] buffer = new byte[this.bufferSize];
        long count = 0;
        int bufferReadCount = 0;
        try
        {
            this.progessListener.ifPresent(IOProgressListener::started);
            while (EOF != (bufferReadCount = input.read(buffer)))
            {
                output.write(buffer, 0, bufferReadCount);
                count += bufferReadCount;
                final long temporaryCount = count;
                this.progessListener.ifPresent(listener -> listener.statusUpdate(temporaryCount));
            }
            this.progessListener.ifPresent(IOProgressListener::completed);
        }
        catch (final IOException oops)
        {
            this.progessListener.ifPresent(listener -> listener.failed(oops));
        }
        return count;
    }

    public NotifyingIOUtils withBufferSize(final int bufferSize)
    {
        if (bufferSize <= 0)
        {
            throw new CoreException("Buffer size must be larger than zero: {}", bufferSize);
        }
        this.bufferSize = bufferSize;
        return this;
    }

    public NotifyingIOUtils withListener(final IOProgressListener listener)
    {
        this.progessListener = Optional.ofNullable(listener);
        return this;
    }
}
