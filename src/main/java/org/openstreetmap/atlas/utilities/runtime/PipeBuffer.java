package org.openstreetmap.atlas.utilities.runtime;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.BlockingQueue;
import java.util.function.Supplier;

import org.openstreetmap.atlas.utilities.scalars.Duration;

/**
 * Create an {@link InputStream} from an {@link OutputStream} and handle buffering using a
 * predefined size FIFO. Both can be read by different threads
 *
 * @author matthieun
 */
public abstract class PipeBuffer
{
    private static final Duration AVOID_DEADLOCK_DELAY = Duration.milliseconds(10);

    private BlockingQueue<Byte> queue;
    private InputStream input;
    private OutputStream output;
    private boolean outClosed = false;
    private boolean inClosed = false;
    private Supplier<Boolean> outClosedAction = () -> true;
    private Supplier<Boolean> inClosedAction = () -> true;

    /**
     * @return The {@link InputStream} to read from from a thread
     */
    public InputStream input()
    {
        if (this.input != null)
        {
            throw new IllegalAccessError("Cannot create a new pipe from the same PipeBuffer."
                    + "There needs to be a new PipeBuffer object created for this.");
        }
        if (this.output == null)
        {
            initialize();
        }
        this.input = new InputStream()
        {
            @Override
            public void close()
            {
                PipeBuffer.this.inClosed = true;
                PipeBuffer.this.inClosedAction.get();
            }

            @Override
            public int read() throws IOException
            {
                Byte result = PipeBuffer.this.queue.poll();
                while (result == null && !PipeBuffer.this.outClosed)
                {
                    // Using this instead of the blocking take() avoids deadlocks when the out is
                    // closed after the take() was entered
                    AVOID_DEADLOCK_DELAY.sleep();
                    result = PipeBuffer.this.queue.poll();
                }
                if (result == null)
                {
                    return -1;
                }
                else
                {
                    return result;
                }
            }
        };
        return this.input;
    }

    /**
     * @return The {@link OutputStream} to write to from a thread
     */
    public OutputStream out()
    {
        if (this.output != null)
        {
            throw new IllegalAccessError("Cannot create a new pipe from the same PipeBuffer."
                    + "There needs to be a new PipeBuffer object created for this.");
        }
        if (this.input == null)
        {
            initialize();
        }
        this.output = new OutputStream()
        {
            @Override
            public void close()
            {
                PipeBuffer.this.outClosed = true;
                PipeBuffer.this.outClosedAction.get();
            }

            @Override
            public void write(final int byteValue) throws IOException
            {
                final boolean added = add(byteValue);
                while (!added && !PipeBuffer.this.inClosed)
                {
                    AVOID_DEADLOCK_DELAY.sleep();
                    add(byteValue);
                }
                if (!added)
                {
                    throw new IOException("Consuming InputStream has been closed.");
                }
            }

            private boolean add(final int byteValue)
            {
                try
                {
                    PipeBuffer.this.queue.add((byte) byteValue);
                    return true;
                }
                catch (final IllegalStateException e)
                {
                    return false;
                }
            }
        };
        return this.output;
    }

    /**
     * @return the size of the pipe buffer
     */
    public int size()
    {
        return this.queue.size();
    }

    public PipeBuffer withInClosedAction(final Supplier<Boolean> inClosedAction)
    {
        this.inClosedAction = inClosedAction;
        return this;
    }

    public PipeBuffer withOutClosedAction(final Supplier<Boolean> outClosedAction)
    {
        this.outClosedAction = outClosedAction;
        return this;
    }

    /**
     * @return The queue used to cache the contents of this pipe
     */
    protected abstract BlockingQueue<Byte> createBlockingQueue();

    private void initialize()
    {
        if (this.queue != null)
        {
            throw new IllegalAccessError("Cannot create a new pipe from the same PipeBuffer."
                    + "There needs to be a new PipeBuffer object created for this.");
        }
        this.queue = createBlockingQueue();
    }
}
