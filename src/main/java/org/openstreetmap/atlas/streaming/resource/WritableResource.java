package org.openstreetmap.atlas.streaming.resource;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.streaming.NotifyingIOUtils.IOProgressListener;
import org.openstreetmap.atlas.streaming.writers.SafeBufferedWriter;

/**
 * A resource that can be written to
 *
 * @author matthieun
 */
public interface WritableResource extends Resource
{
    /**
     * Copy all the contents of another {@link Resource} to this {@link WritableResource}
     *
     * @param input
     *            The input {@link Resource}
     */
    default void copyFrom(final Resource input)
    {
        input.copyTo(this);
    }

    /**
     * Copy all the contents of another {@link Resource} to this {@link WritableResource} while
     * notifying a progress listener
     *
     * @param input
     *            The input {@link Resource}
     * @param listener
     *            The notification {@link IOProgressListener} called as data is being copied
     */
    default void copyFrom(final Resource input, final IOProgressListener listener)
    {
        input.copyTo(this, listener);
    }

    /**
     * @return An {@link OutputStream} that streams data to this resource
     */
    OutputStream write();

    /**
     * Write to this resource and close it.
     *
     * @param value
     *            The value to write.
     */
    default void writeAndClose(final byte[] value)
    {
        try (BufferedOutputStream output = new BufferedOutputStream(write()))
        {
            output.write(value);
        }
        catch (final Exception e)
        {
            throw new CoreException("Could not write to {}", this, e);
        }
    }

    /**
     * Write to this resource and close it.
     *
     * @param value
     *            The value to write.
     */
    default void writeAndClose(final String value)
    {
        try (BufferedWriter writer = writer())
        {
            writer.write(value);
        }
        catch (final Exception e)
        {
            throw new CoreException("Could not write to {}", this, e);
        }
    }

    /**
     * @return A {@link BufferedWriter} on this resource.
     */
    default SafeBufferedWriter writer()
    {
        return new SafeBufferedWriter(new OutputStreamWriter(write(), StandardCharsets.UTF_8));
    }
}
