package org.openstreetmap.atlas.streaming;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.openstreetmap.atlas.exception.CoreException;

/**
 * From <a href=
 * "http://stackoverflow.com/questions/5034311/multiple-readers-for-inputstream-in-java/30262036#30262036"
 * ></a>
 * <p>
 * IMPORTANT! Make sure to read from the original stream as well, and not just the split ones,
 * otherwise the buffer will blow up.
 * <p>
 * Additionally, this class has been made thread safe.
 *
 * @author matthieun
 */
public class SplittableInputStream extends InputStream
{
    /**
     * Almost an input stream: The read-method takes an id.
     *
     * @author matthieun
     */
    public static class MultiplexedSource
    {
        public static final int MINIMUM_BUFFER = 512;
        public static final int MAXIMUM_BUFFER = 10 * 512;

        // Underlying source
        private final InputStream source;

        // Read positions of each SplittableInputStream
        private final List<Integer> readPositions = new ArrayList<>();

        // Data to be read by the SplittableInputStreams
        private int[] buffer = new int[MINIMUM_BUFFER];

        // Last valid position in buffer
        private int writePosition = 0;

        public MultiplexedSource(final InputStream source)
        {
            this.source = source;
        }

        /**
         * Read and advance position for given reader
         *
         * @param readerIdentifier
         *            The reader identifier
         * @return The byte read
         * @throws IOException
         *             In case the source read failed.
         */
        public synchronized int read(final int readerIdentifier) throws IOException
        {

            // Enough data in buffer?
            if (this.readPositions.get(readerIdentifier) >= this.writePosition)
            {
                readJustBuffer();
                this.buffer[this.writePosition++] = this.source.read();
            }

            final int position = this.readPositions.get(readerIdentifier);
            final int byteValue = this.buffer[position];
            if (byteValue != -1)
            {
                this.readPositions.set(readerIdentifier, position + 1);
            }
            return byteValue;
        }

        /**
         * Add a multiplexed reader
         *
         * @param splitIdentifier
         *            The split identifier
         * @return The new reader identifier.
         */
        protected int addSource(final int splitIdentifier)
        {
            this.readPositions
                    .add(splitIdentifier == -1 ? 0 : this.readPositions.get(splitIdentifier));
            return this.readPositions.size() - 1;
        }

        /**
         * Make room for more data (and drop data that has been read by all readers)
         */
        private void readJustBuffer()
        {
            final int from = Collections.min(this.readPositions);
            final int whereTo = Collections.max(this.readPositions);
            final int newLength = Math.max((whereTo - from) * 2, MINIMUM_BUFFER);
            // System.out.println("New Length: " + newLength);
            if (newLength > MAXIMUM_BUFFER)
            {
                throw new CoreException("The SplittableInputStream buffer is blowing up. "
                        + "Make sure all the split streams (including the original one "
                        + "from which the splits originate!) are read at a similar pace.");
            }
            final int[] newBuf = new int[newLength];
            System.arraycopy(this.buffer, from, newBuf, 0, whereTo - from);
            for (int i = 0; i < this.readPositions.size(); i++)
            {
                this.readPositions.set(i, this.readPositions.get(i) - from);
            }
            this.writePosition -= from;
            this.buffer = newBuf;
        }
    }

    // Non-root fields
    private final MultiplexedSource multiSource;
    private final int myId;

    /**
     * Public constructor: Used for first SplittableInputStream
     *
     * @param source
     *            the source {@link InputStream}
     */
    public SplittableInputStream(final InputStream source)
    {
        this.multiSource = new MultiplexedSource(source);
        this.myId = this.multiSource.addSource(-1);
    }

    /**
     * Private constructor: Used in split()
     *
     * @param multiSource
     *            The multiplexed source
     * @param splitId
     *            The split identifier
     */
    private SplittableInputStream(final MultiplexedSource multiSource, final int splitId)
    {
        this.multiSource = multiSource;
        this.myId = multiSource.addSource(splitId);
    }

    @Override
    public int read() throws IOException
    {
        return this.multiSource.read(this.myId);
    }

    /**
     * @return a new InputStream that will read bytes from this position onwards.
     */
    public SplittableInputStream split()
    {
        return new SplittableInputStream(this.multiSource, this.myId);
    }
}
