package org.openstreetmap.atlas.streaming.resource;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;

/**
 * The whole purpose of this class is to throw when closed (which ByteArrayOutputStream does not).
 * This should never be used in production, and should be used only in tests.
 * 
 * @author Taylor Smock
 */
public class ByteArrayOutputStreamExceptional extends ByteArrayOutputStream
{
    private boolean isClosed;
    private boolean throwOnClose;

    @Override
    public void close() throws IOException
    {
        if (this.throwOnClose)
        {
            throw new IOException("This stream is closed");
        }
        this.isClosed = true;
    }

    /**
     * Check if this resource is closed
     * 
     * @return {@code true} if this resource has been closed
     */
    public boolean isClosed()
    {
        return this.isClosed;
    }

    /**
     * Use to force this OutputStream to throw an exception on close
     * 
     * @param throwOnClose
     *            {@code true} to force a throw on close.
     */
    public void setThrowOnClose(final boolean throwOnClose)
    {
        this.throwOnClose = throwOnClose;
    }

    @Override
    public byte[] toByteArray() throws UncheckedIOException
    {
        this.checkClosed();
        return super.toByteArray();
    }

    @Override
    public void write(final int byteWrite)
    {
        this.checkClosed();
        super.write(byteWrite);
    }

    @Override
    public synchronized void write(final byte[] bytes, final int off, final int len)
    {
        this.checkClosed();
        super.write(bytes, off, len);
    }

    @Override
    public void writeBytes(final byte[] bytes) throws UncheckedIOException
    {
        this.checkClosed();
        super.writeBytes(bytes);
    }

    private void checkClosed()
    {
        if (this.isClosed)
        {
            throw new UncheckedIOException(new IOException("This stream is closed"));
        }
    }
}
