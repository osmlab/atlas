package org.openstreetmap.atlas.geography.atlas.raw.slicing;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.AtlasMetaData;
import org.openstreetmap.atlas.geography.atlas.builder.AtlasSize;
import org.openstreetmap.atlas.geography.atlas.builder.RelationBean;
import org.openstreetmap.atlas.geography.atlas.items.ItemType;
import org.openstreetmap.atlas.geography.atlas.items.Line;
import org.openstreetmap.atlas.geography.atlas.items.Point;
import org.openstreetmap.atlas.geography.atlas.items.Relation;
import org.openstreetmap.atlas.geography.atlas.items.RelationMember;
import org.openstreetmap.atlas.geography.atlas.packed.PackedAtlasBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link RawAtlasChangeSetBuilder} is responsible for applying {@link RawAtlasChangeSet}s to a
 * given Atlas.
 *
 * @author mgostintsev
 */
public class RawAtlasChangeSetBuilder
{
    private static final Logger logger = LoggerFactory.getLogger(RawAtlasChangeSetBuilder.class);

    // The scaling factor for calculating approximate atlas size
    private static final double ENTITY_SCALING_FACTOR = 1.2;

    private final Atlas original;
    private final RawAtlasChangeSet changeSet;
    private final PackedAtlasBuilder builder = new PackedAtlasBuilder();

    /**
     * Default constructor.
     *
     * @param original
     *            The {@link Atlas} to apply changes to
     * @param changeSet
     *            The {@link RawAtlasChangeSet} to apply
     */
    public RawAtlasChangeSetBuilder(final Atlas original, final RawAtlasChangeSet changeSet)
    {
        this.original = original;
        this.changeSet = changeSet;
    }

    /**
     * Apply the changes. First, update all {@link Point}s, then {@link Line}s and lastly
     * {@link Relation}s.
     *
     * @return the {@link Atlas} with changes
     */
    public Atlas applyChanges()
    {
        // Log original Atlas statistics
        logAtlasStatistics(this.original);

        // Prepare the builder
        setAtlasSizeEstimateAndMetadata();

        // Process Points
        addNewPoints();
        addUpdatedPoints();

        // Process Lines
        addNewLines();
        addUpdatedLines();

        // Process Relations
        updateAndAddRelations();

        // Build and log
        final Atlas atlasWithUpdates = this.builder.get();
        logAtlasStatistics(atlasWithUpdates);

        return atlasWithUpdates;
    }

    /**
     * Add any new {@link Line}s from the change set.
     */
    private void addNewLines()
    {
        this.changeSet.getCreatedLines().forEach(line -> this.builder.addLine(line.getIdentifier(),
                reconstructGeometryForLine(line), line.getTags()));
    }

    /**
     * Add any new {@link Point}s from the change set.
     */
    private void addNewPoints()
    {
        this.changeSet.getCreatedPoints().forEach(point -> this.builder
                .addPoint(point.getIdentifier(), point.getLocation(), point.getTags()));
    }

    /**
     * Updates the {@link Line} tags for all existing {@link Line}s in the original Atlas, unless
     * the {@link Line} was deleted by the change set, and adds it to the updated Atlas.
     */
    private void addUpdatedLines()
    {
        this.original.lines().forEach(line ->
        {
            final long lineIdentifier = line.getIdentifier();
            // Only add if we've not deleted this line
            if (!this.changeSet.getDeletedLines().contains(lineIdentifier))
            {
                // Add the Line with the updated tag value
                if (this.changeSet.getUpdatedLineTags().containsKey(lineIdentifier))
                {
                    final Line originalLine = this.original.line(lineIdentifier);
                    final Map<String, String> updatedTags = originalLine.getTags();
                    updatedTags.putAll(this.changeSet.getUpdatedLineTags().get(lineIdentifier));
                    this.builder.addLine(lineIdentifier, originalLine.asPolyLine(), updatedTags);
                }
                else
                {
                    logger.error(
                            "Line {} doesn't have any tag modifications. Adding for Atlas integrity.",
                            lineIdentifier);
                    this.builder.addLine(lineIdentifier, line.asPolyLine(), line.getTags());
                }
            }
        });
    }

    /**
     * Updates the {@link Point} tags for all existing {@link Point}s in the original Atlas, and
     * adds it to the updated Atlas.
     */
    private void addUpdatedPoints()
    {
        this.original.points().forEach(point ->
        {
            final long pointIdentifier = point.getIdentifier();
            if (this.changeSet.getUpdatedPointTags().containsKey(pointIdentifier))
            {
                final Point originalPoint = this.original.point(pointIdentifier);
                final Map<String, String> updatedTags = originalPoint.getTags();
                updatedTags.putAll(this.changeSet.getUpdatedPointTags().get(pointIdentifier));
                this.builder.addPoint(pointIdentifier, originalPoint.getLocation(), updatedTags);
            }
            else
            {
                logger.error(
                        "Point {} doesn't have any tag modifications. Adding for Atlas integrity.",
                        pointIdentifier);
                this.builder.addPoint(pointIdentifier, point.getLocation(), point.getTags());
            }
        });
    }

    /**
     * Logs statistics for given {@link Atlas}.
     *
     * @param atlas
     *            The {@link Atlas} whose statistics to log
     */
    private void logAtlasStatistics(final Atlas atlas)
    {
        final StringBuilder builder = new StringBuilder();
        builder.append("Points: ");
        builder.append(atlas.numberOfPoints());
        builder.append(" Lines: ");
        builder.append(atlas.numberOfLines());
        builder.append(" Relations: ");
        builder.append(atlas.numberOfRelations());
        logger.info(builder.toString());
    }

    /**
     * Constructs a {@link PolyLine} for the given {@link TemporaryLine}.
     *
     * @param line
     *            The {@link TemporaryLine} to use
     * @return the constructed {@link PolyLine}
     */
    private PolyLine reconstructGeometryForLine(final TemporaryLine line)
    {
        final List<Location> locations = new ArrayList<>();
        line.getShapePointIdentifiers().forEach(identifier ->
        {
            final Point point = this.builder.peek().point(identifier);
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
    private void setAtlasSizeEstimateAndMetadata()
    {
        final AtlasSize size = new AtlasSize(0, 0, 0,
                Math.round(this.original.numberOfLines() * ENTITY_SCALING_FACTOR),
                Math.round(this.original.numberOfPoints() * ENTITY_SCALING_FACTOR),
                this.original.numberOfRelations());
        this.builder.setSizeEstimates(size);
        this.builder.setMetaData(new AtlasMetaData());
    }

    /**
     * TODO Might require updates after relation slicing
     */
    private void updateAndAddRelations()
    {
        for (final Relation relation : this.original.relationsLowerOrderFirst())
        {
            final RelationBean bean = new RelationBean();
            for (final RelationMember member : relation.members())
            {
                final long memberIdentifier = member.getEntity().getIdentifier();
                final ItemType memberType = member.getEntity().getType();

                if (this.changeSet.getDeletedToCreatedLineMapping().keySet()
                        .contains(memberIdentifier) && memberType == ItemType.LINE)
                {
                    // We found a deleted Line, replace it with the newly created Line(s)
                    for (final long addedLineIdentifier : this.changeSet
                            .getDeletedToCreatedLineMapping().get(memberIdentifier))
                    {
                        // Do one last sanity check to make sure the member we're adding
                        // actually exists
                        if (this.builder.peek().line(addedLineIdentifier) != null)
                        {
                            bean.addItem(addedLineIdentifier, member.getRole(),
                                    member.getEntity().getType());
                        }
                        else
                        {
                            logger.error(
                                    "Corrupt Data! Trying to replace deleted Line member {} with created Line member {} "
                                            + "for Relation {} , but new Line doesn't exist in Atlas.",
                                    memberIdentifier, addedLineIdentifier,
                                    relation.getIdentifier());
                        }
                    }
                }
                else
                {
                    // Non-deleted member, simply add it
                    bean.addItem(member.getEntity().getIdentifier(), member.getRole(),
                            member.getEntity().getType());
                }
            }
            this.builder.addRelation(relation.getIdentifier(), relation.getOsmIdentifier(), bean,
                    relation.getTags());
        }
    }

    // TODO - statistics
}
