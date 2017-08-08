package org.openstreetmap.atlas.utilities.collections;

import java.util.AbstractQueue;
import java.util.Comparator;
import java.util.Iterator;
import java.util.PriorityQueue;

/**
 * A fixed size priority queue (binary heap) for use case like getting top n, implemented based on
 * {@link PriorityQueue}.
 * <p>
 * After reaching the given maximum size, any additional element offered to the queue will be added
 * into the queue first and then remove the new head, so the size of the queue will remain the same
 *
 * @param <E>
 *            The type of element
 * @author tony
 */
public class FixedSizePriorityQueue<E> extends AbstractQueue<E>
{
    private final int maximumSize;
    private final PriorityQueue<E> priorityQueue;

    public FixedSizePriorityQueue(final int maximumSize)
    {
        this.maximumSize = maximumSize;
        this.priorityQueue = new PriorityQueue<>(maximumSize + 1);
    }

    public FixedSizePriorityQueue(final int maximumSize, final Comparator<? super E> comparator)
    {
        this.maximumSize = maximumSize;
        this.priorityQueue = new PriorityQueue<>(maximumSize + 1, comparator);
    }

    @Override
    public Iterator<E> iterator()
    {
        return this.priorityQueue.iterator();
    }

    @Override
    public boolean offer(final E e)
    {
        final boolean flag = this.priorityQueue.offer(e);
        if (this.priorityQueue.size() > this.maximumSize)
        {
            poll();
        }
        return flag;
    }

    @Override
    public E peek()
    {
        return this.priorityQueue.peek();
    }

    @Override
    public E poll()
    {
        return this.priorityQueue.poll();
    }

    @Override
    public int size()
    {
        return this.priorityQueue.size();
    }

}
