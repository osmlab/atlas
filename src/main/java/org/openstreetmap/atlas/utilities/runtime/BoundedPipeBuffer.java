package org.openstreetmap.atlas.utilities.runtime;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Bounded {@link PipeBuffer} based on an {@link ArrayBlockingQueue}
 *
 * @author matthieun
 */
public class BoundedPipeBuffer extends PipeBuffer
{
    private final int capacity;

    public BoundedPipeBuffer(final int capacity)
    {
        this.capacity = capacity;
    }

    @Override
    protected BlockingQueue<Byte> createBlockingQueue()
    {
        return new ArrayBlockingQueue<>(this.capacity);
    }

}
