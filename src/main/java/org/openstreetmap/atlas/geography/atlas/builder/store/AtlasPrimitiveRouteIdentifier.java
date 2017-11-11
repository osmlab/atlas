package org.openstreetmap.atlas.geography.atlas.builder.store;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.openstreetmap.atlas.exception.CoreException;

import com.google.common.collect.ImmutableList;

/**
 * @author Sid
 */
public class AtlasPrimitiveRouteIdentifier
        implements Iterable<AtlasPrimitiveEdgeIdentifier>, Serializable
{
    private static final long serialVersionUID = 2321636844479248974L;

    private List<AtlasPrimitiveEdgeIdentifier> primitiveRouteIdentifier;

    public static AtlasPrimitiveRouteIdentifier from(final AtlasPrimitiveRoute atlasPrimitiveRoute)
    {
        final List<AtlasPrimitiveEdgeIdentifier> atlasPrimitiveEdgeIds = new ArrayList<>();
        atlasPrimitiveRoute.forEach(
                edge -> atlasPrimitiveEdgeIds.add(AtlasPrimitiveEdgeIdentifier.from(edge)));
        return new AtlasPrimitiveRouteIdentifier(atlasPrimitiveEdgeIds);
    }

    public AtlasPrimitiveRouteIdentifier()
    {
    }

    public AtlasPrimitiveRouteIdentifier(final AtlasPrimitiveEdgeIdentifier... primitiveEdgeIds)
    {
        this.primitiveRouteIdentifier = ImmutableList.copyOf(primitiveEdgeIds);
    }

    public AtlasPrimitiveRouteIdentifier(
            final Iterable<AtlasPrimitiveEdgeIdentifier> primitiveEdgeIds)
    {
        this.primitiveRouteIdentifier = ImmutableList.copyOf(primitiveEdgeIds);
    }

    public AtlasPrimitiveEdgeIdentifier end()
    {
        if (this.primitiveRouteIdentifier.size() > 0)
        {
            return this.primitiveRouteIdentifier.get(this.primitiveRouteIdentifier.size() - 1);
        }
        throw new CoreException("Illegal State : Empty route");
    }

    @Override
    public boolean equals(final Object other)
    {
        if (other instanceof AtlasPrimitiveRouteIdentifier)
        {
            final AtlasPrimitiveRouteIdentifier that = (AtlasPrimitiveRouteIdentifier) other;
            if (this.primitiveRouteIdentifier.size() == that.primitiveRouteIdentifier.size())
            {
                return new EqualsBuilder()
                        .append(this.primitiveRouteIdentifier, that.primitiveRouteIdentifier)
                        .isEquals();
            }
        }
        return false;
    }

    @Override
    public int hashCode()
    {
        return new HashCodeBuilder().append(this.primitiveRouteIdentifier).hashCode();
    }

    /*
     * Similar to {@link Route#overlapIndex}
     */
    public boolean isOverlappedBy(final AtlasPrimitiveRouteIdentifier primitiveRouteIdentifier)
    {
        if (primitiveRouteIdentifier == null)
        {
            return false;
        }
        // Keep track of the last index at which the last Edge was overlapping this route, to avoid
        // returning false positives in case of routes making a loop.
        int lastOverlapIndex = -1;
        for (final AtlasPrimitiveEdgeIdentifier primitiveEdgeIdentifier : primitiveRouteIdentifier)
        {
            final int index = this.primitiveRouteIdentifier.indexOf(primitiveEdgeIdentifier);
            if (index <= lastOverlapIndex)
            {
                // The edge does not overlap, or it does but at a smaller index which would indicate
                // a loop.
                return false;
            }
            lastOverlapIndex = index;
        }
        return true;
    }

    @Override
    public Iterator<AtlasPrimitiveEdgeIdentifier> iterator()
    {
        return this.primitiveRouteIdentifier.iterator();
    }

    /**
     * Counts the number of times a subRoute overlaps a route.
     *
     * @param subRouteIdentifier
     *            - Smaller subsequence of the route that can overlap with route
     * @return overlapCount
     */
    public int overlapCount(final AtlasPrimitiveRouteIdentifier subRouteIdentifier)
    {
        int overlapCount = 0;
        if (this.primitiveRouteIdentifier == null || subRouteIdentifier == null)
        {
            return overlapCount;
        }
        Iterator<AtlasPrimitiveEdgeIdentifier> subRouteIdentifierIterator = subRouteIdentifier
                .iterator();
        AtlasPrimitiveEdgeIdentifier subRouteEdgeIdentifier = subRouteIdentifierIterator.hasNext()
                ? subRouteIdentifierIterator.next() : null;
        for (final AtlasPrimitiveEdgeIdentifier edge : this.primitiveRouteIdentifier)
        {
            if (subRouteEdgeIdentifier == null)
            {
                break;
            }
            if (edge.equals(subRouteEdgeIdentifier))
            {
                if (!subRouteIdentifierIterator.hasNext())
                {
                    overlapCount++;
                    subRouteIdentifierIterator = subRouteIdentifier.iterator();
                }
            }
            else
            {
                subRouteIdentifierIterator = subRouteIdentifier.iterator();
                if (edge.equals(subRouteIdentifier.start()))
                {
                    subRouteEdgeIdentifier = subRouteIdentifierIterator.hasNext()
                            ? subRouteIdentifierIterator.next() : null;
                }
            }
            subRouteEdgeIdentifier = subRouteIdentifierIterator.hasNext()
                    ? subRouteIdentifierIterator.next() : null;
        }
        return overlapCount;
    }

    public void setPrimitiveRouteIdentifier(
            final List<AtlasPrimitiveEdgeIdentifier> primitiveRouteIdentifier)
    {
        this.primitiveRouteIdentifier = primitiveRouteIdentifier;
    }

    public int size()
    {
        return this.primitiveRouteIdentifier.size();
    }

    public AtlasPrimitiveEdgeIdentifier start()
    {
        if (this.primitiveRouteIdentifier.size() > 0)
        {
            return this.primitiveRouteIdentifier.get(0);
        }
        throw new CoreException("Illegal State : Empty route");
    }

    public List<AtlasPrimitiveEdgeIdentifier> subRoute(final int fromIndex, final int toIndex)
    {
        return this.primitiveRouteIdentifier.subList(fromIndex, toIndex);
    }

    @Override
    public String toString()
    {
        return "AtlasPrimitiveRouteIdentifier [identifier=" + this.primitiveRouteIdentifier + "]";
    }
}
