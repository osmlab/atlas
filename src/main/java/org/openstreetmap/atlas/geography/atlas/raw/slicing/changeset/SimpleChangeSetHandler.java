package org.openstreetmap.atlas.geography.atlas.raw.slicing.changeset;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.builder.RelationBean;
import org.openstreetmap.atlas.geography.atlas.items.ItemType;
import org.openstreetmap.atlas.geography.atlas.items.Line;
import org.openstreetmap.atlas.geography.atlas.items.Point;
import org.openstreetmap.atlas.geography.atlas.items.Relation;
import org.openstreetmap.atlas.geography.atlas.items.RelationMember;
import org.openstreetmap.atlas.tags.ISOCountryTag;
import org.openstreetmap.atlas.utilities.time.Time;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SimpleChangeSetHandler} is responsible for applying a {@link SimpleChangeSet} to a
 * given {@link Atlas}.
 *
 * @author mgostintsev
 */
public class SimpleChangeSetHandler extends ChangeSetHandler
{
    private static final Logger logger = LoggerFactory.getLogger(SimpleChangeSetHandler.class);

    private final SimpleChangeSet changeSet;

    // Keep track of empty relations that have been filtered to avoid adding them to their parents
    private final Set<Long> filteredEmptyRelations = new HashSet<>();

    /**
     * Default constructor.
     *
     * @param atlas
     *            The {@link Atlas} to apply changes to
     * @param changeSet
     *            The {@link SimpleChangeSet} to apply
     */
    public SimpleChangeSetHandler(final Atlas atlas, final SimpleChangeSet changeSet)
    {
        super(atlas);
        this.changeSet = changeSet;
    }

    /**
     * Apply the changes. First, update all {@link Point}s, then {@link Line}s and lastly
     * {@link Relation}s.
     *
     * @return the {@link Atlas} with changes
     */
    @Override
    public Atlas applyChanges()
    {
        final Time time = Time.now();
        logger.info("Started Applying Point and Line Changes for {}", getShardOrAtlasName());

        // Log original Atlas statistics
        logger.info(atlasStatistics(this.getAtlas()));

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
        final Atlas atlasWithUpdates = this.getBuilder().get();
        logger.info(atlasStatistics(atlasWithUpdates));

        logger.info("Finished Applying Point and Line Changes for {} in {}", getShardOrAtlasName(),
                time.untilNow());

        return atlasWithUpdates;
    }

    /**
     * Add any new {@link Line}s from the change set.
     */
    private void addNewLines()
    {
        this.changeSet.getCreatedLines().forEach(line -> this.getBuilder()
                .addLine(line.getIdentifier(), reconstructGeometryForLine(line), line.getTags()));
    }

    /**
     * Add any new {@link Point}s from the change set.
     */
    private void addNewPoints()
    {
        this.changeSet.getCreatedPoints().forEach(point -> this.getBuilder()
                .addPoint(point.getIdentifier(), point.getLocation(), point.getTags()));
    }

    /**
     * Updates the {@link Line} tags for all existing {@link Line}s in the original Atlas, unless
     * the {@link Line} was deleted by the change set, and adds it to the updated Atlas.
     */
    private void addUpdatedLines()
    {
        this.getAtlas().lines().forEach(line ->
        {
            final long lineIdentifier = line.getIdentifier();
            // Only add if we've not deleted this line
            if (!this.changeSet.getDeletedToCreatedLineMapping().keySet().contains(lineIdentifier))
            {
                // Add the Line with the updated tag value
                if (this.changeSet.getUpdatedLineTags().containsKey(lineIdentifier))
                {
                    final Line originalLine = this.getAtlas().line(lineIdentifier);
                    final Map<String, String> updatedTags = originalLine.getTags();
                    updatedTags.putAll(this.changeSet.getUpdatedLineTags().get(lineIdentifier));
                    this.getBuilder().addLine(lineIdentifier, originalLine.asPolyLine(),
                            updatedTags);
                }
                else
                {
                    // All lines should have at least a country code tag addition after
                    // country-slicing takes place. If it doesn't, it means we couldn't slice it
                    // properly. Add this feature into the Atlas with a missing country code.
                    logger.error(
                            "Adding Line {} with missing country code to maintain Atlas integrity.",
                            lineIdentifier);
                    final Map<String, String> updatedTags = line.getTags();
                    updatedTags.put(ISOCountryTag.KEY, ISOCountryTag.COUNTRY_MISSING);
                    this.getBuilder().addLine(lineIdentifier, line.asPolyLine(), updatedTags);
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
        this.getAtlas().points().forEach(point ->
        {
            final long pointIdentifier = point.getIdentifier();
            if (this.changeSet.getUpdatedPointTags().containsKey(pointIdentifier))
            {
                final Point originalPoint = this.getAtlas().point(pointIdentifier);
                final Map<String, String> updatedTags = originalPoint.getTags();
                updatedTags.putAll(this.changeSet.getUpdatedPointTags().get(pointIdentifier));
                this.getBuilder().addPoint(pointIdentifier, originalPoint.getLocation(),
                        updatedTags);
            }
        });
    }

    /**
     * Updates relations to replace or remove any members that were modified during slicing.
     */
    private void updateAndAddRelations()
    {
        for (final Relation relation : this.getAtlas().relationsLowerOrderFirst())
        {
            final RelationBean bean = new RelationBean();
            for (final RelationMember member : relation.members())
            {
                final long memberIdentifier = member.getEntity().getIdentifier();
                final ItemType memberType = member.getEntity().getType();

                if (memberType == ItemType.LINE && this.changeSet.getDeletedToCreatedLineMapping()
                        .keySet().contains(memberIdentifier))
                {
                    // Deleted line, try to replace it with the newly created line(s)
                    for (final long addedLineIdentifier : this.changeSet
                            .getDeletedToCreatedLineMapping().get(memberIdentifier))
                    {
                        // Check that the new line being added exists
                        if (this.getBuilder().peek().line(addedLineIdentifier) != null)
                        {
                            bean.addItem(addedLineIdentifier, member.getRole(),
                                    member.getEntity().getType());
                        }
                        else
                        {
                            throw new CoreException(
                                    "Corrupt Data! Trying to replace deleted Line member {} with created Line member {} "
                                            + "for Relation {} , but new Line doesn't exist in Atlas.",
                                    memberIdentifier, addedLineIdentifier,
                                    relation.getIdentifier());
                        }
                    }
                }
                else if (memberType == ItemType.POINT
                        && this.changeSet.getDeletedPoints().contains(memberIdentifier))
                {
                    // A deleted point, don't add it to the relation
                    logger.trace(
                            "Point {} wasn't in the working country set and is being filtered out of Relation {}",
                            memberIdentifier, relation.getIdentifier());
                }
                else if (memberType == ItemType.RELATION
                        && this.filteredEmptyRelations.contains(memberIdentifier))
                {
                    // A deleted relation, don't add it to the relation
                    logger.trace(
                            "Relation {} is empty as a result of slicing and is being filtered out of parent Relation {}",
                            memberIdentifier, relation.getIdentifier());
                }
                else
                {
                    // Non-deleted member, add it
                    bean.addItem(member.getEntity().getIdentifier(), member.getRole(),
                            member.getEntity().getType());
                }
            }

            // Guard against empty relations - we might have filtered out all members if they
            // weren't in the working country set
            if (!bean.isEmpty())
            {
                this.getBuilder().addRelation(relation.getIdentifier(), relation.getOsmIdentifier(),
                        bean, relation.getTags());
            }
            else
            {
                this.filteredEmptyRelations.add(relation.getIdentifier());
                logger.trace("Excluding Relation {} from Atlas due to empty member list",
                        relation.getIdentifier());
            }
        }
    }

    // TODO - statistics
}
