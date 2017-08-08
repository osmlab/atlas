package org.openstreetmap.atlas.utilities.runtime;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * An unbounded {@link PipeBuffer} (still limited at Integer.MAX_VALUE) using an underlying
 * {@link LinkedBlockingQueue}
 *
 * @author matthieun
 */
public class OpenPipeBuffer extends PipeBuffer
{

    @Override
    protected BlockingQueue<Byte> createBlockingQueue()
    {
        // Unbounded, dies at Integer.MAX_VALUE
        return new LinkedBlockingQueue<>();
    }

}
