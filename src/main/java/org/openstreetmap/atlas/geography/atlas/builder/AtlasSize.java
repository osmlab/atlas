package org.openstreetmap.atlas.geography.atlas.builder;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Objects;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.atlas.items.Area;
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.ItemType;
import org.openstreetmap.atlas.geography.atlas.items.Line;
import org.openstreetmap.atlas.geography.atlas.items.Node;
import org.openstreetmap.atlas.geography.atlas.items.Point;
import org.openstreetmap.atlas.geography.atlas.items.Relation;

/**
 * Size estimates for an {@link AtlasBuilder}
 *
 * @author matthieun
 */
public class AtlasSize implements Serializable
{
    /**
     * A simple builder class for creating {@link AtlasSize} objects with custom sizes.
     *
     * @author lcram
     */
    public static class AtlasSizeBuilder
    {
        private long edgeEstimate;
        private long nodeEstimate;
        private long areaEstimate;
        private long lineEstimate;
        private long pointEstimate;
        private long relationEstimate;

        public AtlasSizeBuilder()
        {
            this.edgeEstimate = DEFAULT_ESTIMATE;
            this.nodeEstimate = DEFAULT_ESTIMATE;
            this.areaEstimate = DEFAULT_ESTIMATE;
            this.lineEstimate = DEFAULT_ESTIMATE;
            this.pointEstimate = DEFAULT_ESTIMATE;
            this.relationEstimate = DEFAULT_ESTIMATE;
        }

        /**
         * Builds an {@link AtlasSize}. By default it uses {@link AtlasSize#DEFAULT_ESTIMATE} for
         * the size estimates.
         *
         * @return A new {@link AtlasSize}
         */
        public AtlasSize build()
        {
            return new AtlasSize(this.edgeEstimate, this.nodeEstimate, this.areaEstimate,
                    this.lineEstimate, this.pointEstimate, this.relationEstimate);
        }

        public AtlasSizeBuilder withAreaEstimate(final long areaNumber)
        {
            this.areaEstimate = areaNumber;
            return this;
        }

        public AtlasSizeBuilder withEdgeEstimate(final long edgeNumber)
        {
            this.edgeEstimate = edgeNumber;
            return this;
        }

        public AtlasSizeBuilder withLineEstimate(final long lineNumber)
        {
            this.lineEstimate = lineNumber;
            return this;
        }

        public AtlasSizeBuilder withNodeEstimate(final long nodeNumber)
        {
            this.nodeEstimate = nodeNumber;
            return this;
        }

        public AtlasSizeBuilder withPointEstimate(final long pointNumber)
        {
            this.pointEstimate = pointNumber;
            return this;
        }

        public AtlasSizeBuilder withRelationEstimate(final long relationNumber)
        {
            this.relationEstimate = relationNumber;
            return this;
        }
    }

    private static final long serialVersionUID = -4365680097735345765L;
    private static final long DEFAULT_ESTIMATE = 1024L;

    public static final AtlasSize DEFAULT = new AtlasSize(DEFAULT_ESTIMATE, DEFAULT_ESTIMATE,
            DEFAULT_ESTIMATE, DEFAULT_ESTIMATE, DEFAULT_ESTIMATE, DEFAULT_ESTIMATE);

    private final long edgeNumber;
    private final long nodeNumber;
    private final long areaNumber;
    private final long lineNumber;
    private final long pointNumber;
    private final long relationNumber;

    /**
     * Constructor that calculates the number of occurrences for each {@link AtlasEntity}.
     *
     * @param entities
     *            The {@link AtlasEntity}s to use for generating an {@link AtlasSize}
     */
    public AtlasSize(final Iterable<AtlasEntity> entities)
    {
        long nodeNumber = 0L;
        long edgeNumber = 0L;
        long areaNumber = 0L;
        long lineNumber = 0L;
        long pointNumber = 0L;
        long relationNumber = 0L;

        final Iterator<AtlasEntity> entityIterator = entities.iterator();
        while (entityIterator.hasNext())
        {
            final AtlasEntity entity = entityIterator.next();
            final ItemType type = entity.getType();
            switch (type)
            {
                case NODE:
                    nodeNumber++;
                    break;
                case EDGE:
                    edgeNumber++;
                    break;
                case AREA:
                    areaNumber++;
                    break;
                case LINE:
                    lineNumber++;
                    break;
                case POINT:
                    pointNumber++;
                    break;
                case RELATION:
                    relationNumber++;
                    break;
                default:
                    throw new CoreException("Invalid Item Type {}", type);
            }
        }

        this.edgeNumber = edgeNumber;
        this.nodeNumber = nodeNumber;
        this.areaNumber = areaNumber;
        this.lineNumber = lineNumber;
        this.pointNumber = pointNumber;
        this.relationNumber = relationNumber;
    }

    /**
     * Default constructor that takes explicit number of occurrences of each {@link AtlasEntity}.
     *
     * @param edgeNumber
     *            Number of {@link Edge}s
     * @param nodeNumber
     *            Number of {@link Node}s
     * @param areaNumber
     *            Number of {@link Area}s
     * @param lineNumber
     *            Number of {@link Line}s
     * @param pointNumber
     *            Number of {@link Point}s
     * @param relationNumber
     *            Number of {@link Relation}s
     */
    public AtlasSize(final long edgeNumber, final long nodeNumber, final long areaNumber,
            final long lineNumber, final long pointNumber, final long relationNumber)
    {
        this.edgeNumber = edgeNumber;
        this.nodeNumber = nodeNumber;
        this.areaNumber = areaNumber;
        this.lineNumber = lineNumber;
        this.pointNumber = pointNumber;
        this.relationNumber = relationNumber;
    }

    @Override
    public boolean equals(final Object other)
    {
        if (other instanceof AtlasSize)
        {
            if (this == other)
            {
                return true;
            }
            final AtlasSize that = (AtlasSize) other;
            if (this.getEdgeNumber() != that.getEdgeNumber())
            {
                return false;
            }
            if (this.getNodeNumber() != that.getNodeNumber())
            {
                return false;
            }
            if (this.getAreaNumber() != that.getAreaNumber())
            {
                return false;
            }
            if (this.getLineNumber() != that.getLineNumber())
            {
                return false;
            }
            if (this.getPointNumber() != that.getPointNumber())
            {
                return false;
            }
            if (this.getRelationNumber() != that.getRelationNumber())
            {
                return false;
            }
            return true;
        }
        return false;
    }

    public long getAreaNumber()
    {
        return this.areaNumber;
    }

    public long getEdgeNumber()
    {
        return this.edgeNumber;
    }

    public long getLineNumber()
    {
        return this.lineNumber;
    }

    public long getNodeNumber()
    {
        return this.nodeNumber;
    }

    public long getPointNumber()
    {
        return this.pointNumber;
    }

    public long getRelationNumber()
    {
        return this.relationNumber;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(Long.valueOf(this.edgeNumber), Long.valueOf(this.nodeNumber),
                Long.valueOf(this.areaNumber), Long.valueOf(this.lineNumber),
                Long.valueOf(this.pointNumber), Long.valueOf(this.relationNumber));
    }

    @Override
    public String toString()
    {
        return "[AtlasSize: edgeNumber=" + this.edgeNumber + ", nodeNumber=" + this.nodeNumber
                + ", areaNumber=" + this.areaNumber + ", lineNumber=" + this.lineNumber
                + ", pointNumber=" + this.pointNumber + ", relationNumber=" + this.relationNumber
                + "]";
    }
}
