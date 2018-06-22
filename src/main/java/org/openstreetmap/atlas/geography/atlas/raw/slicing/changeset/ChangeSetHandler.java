package org.openstreetmap.atlas.geography.atlas.raw.slicing.changeset;

import java.util.ArrayList;
import java.util.List;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.builder.AtlasSize;
import org.openstreetmap.atlas.geography.atlas.items.Point;
import org.openstreetmap.atlas.geography.atlas.packed.PackedAtlasBuilder;
import org.openstreetmap.atlas.geography.atlas.raw.slicing.temporary.TemporaryLine;

/**
 * Base class that contains common functionality for applying a given slicing change set to a raw
 * Atlas.
 *
 * @author mgostintsev
 */
public abstract class ChangeSetHandler
{
    // The scaling factor for calculating approximate atlas size
    private static final double ENTITY_SCALING_FACTOR = 1.2;

    private final Atlas atlas;
    private final PackedAtlasBuilder builder = new PackedAtlasBuilder();

    protected ChangeSetHandler(final Atlas atlas)
    {
        this.atlas = atlas;
    }

    /**
     * @return the {@link Atlas} with the applied change set.
     */
    public abstract Atlas applyChanges();

    /**
     * Logs statistics for given {@link Atlas}.
     *
     * @param atlas
     *            The {@link Atlas} whose statistics to log
     * @return the statistics string
     */
    protected String atlasStatistics(final Atlas atlas)
    {
        final StringBuilder builder = new StringBuilder();
        builder.append("Points: ");
        builder.append(atlas.numberOfPoints());
        builder.append(" Lines: ");
        builder.append(atlas.numberOfLines());
        builder.append(" Relations: ");
        builder.append(atlas.numberOfRelations());
        return builder.toString();
    }

    protected Atlas getAtlas()
    {
        return this.atlas;
    }

    protected PackedAtlasBuilder getBuilder()
    {
        return this.builder;
    }

    protected String getShardOrAtlasName()
    {
        return this.atlas.metaData().getShardName().orElse(this.atlas.getName());
    }

    /**
     * Constructs a {@link PolyLine} for the given {@link TemporaryLine}.
     *
     * @param line
     *            The {@link TemporaryLine} to use
     * @return the constructed {@link PolyLine}
     */
    protected PolyLine reconstructGeometryForLine(final TemporaryLine line)
    {
        final List<Location> locations = new ArrayList<>();
        line.getShapePointIdentifiers().forEach(identifier ->
        {
            final Point point = this.getBuilder().peek().point(identifier);
            if (point != null)
            {
                locations.add(point.getLocation());
            }
            else
            {
                throw new CoreException(
                        "Corrupt Data: Line {} is referencing a shape point {} which doesn't exist!",
                        line.getIdentifier(), identifier);
            }
        });
        return new PolyLine(locations);
    }

    /**
     * Sets new Atlas size estimate and metadata. For size estimate, we're using the original size
     * as an estimate, scaled by {@value #ENTITY_SCALING_FACTOR}. This is done to avoid re-sizing,
     * instead electing to trim afterwards if the estimates were too large.
     */
    protected void setAtlasSizeEstimateAndMetadata()
    {
        final AtlasSize size = new AtlasSize(0, 0, 0,
                Math.round(this.getAtlas().numberOfLines() * ENTITY_SCALING_FACTOR),
                Math.round(this.getAtlas().numberOfPoints() * ENTITY_SCALING_FACTOR),
                this.getAtlas().numberOfRelations());
        this.getBuilder().setSizeEstimates(size);
        this.getBuilder().setMetaData(this.atlas.metaData());
    }

}
