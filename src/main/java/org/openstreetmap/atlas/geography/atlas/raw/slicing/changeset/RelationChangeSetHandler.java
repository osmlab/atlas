package org.openstreetmap.atlas.geography.atlas.raw.slicing.changeset;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.builder.RelationBean;
import org.openstreetmap.atlas.geography.atlas.items.ItemType;
import org.openstreetmap.atlas.geography.atlas.items.Line;
import org.openstreetmap.atlas.geography.atlas.items.Point;
import org.openstreetmap.atlas.geography.atlas.items.Relation;
import org.openstreetmap.atlas.geography.atlas.items.RelationMember;
import org.openstreetmap.atlas.geography.atlas.pbf.slicing.identifier.ReverseIdentifierFactory;
import org.openstreetmap.atlas.geography.atlas.raw.slicing.temporary.TemporaryRelationMember;
import org.openstreetmap.atlas.tags.ISOCountryTag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link RelationChangeSetHandler} is responsible for applying a {@link RelationChangeSet} to a
 * given {@link Atlas}.
 *
 * @author mgostintsev
 */
public class RelationChangeSetHandler extends ChangeSetHandler
{
    private static final Logger logger = LoggerFactory.getLogger(RelationChangeSetHandler.class);

    private final RelationChangeSet changeSet;

    /**
     * Default constructor.
     *
     * @param atlas
     *            The {@link Atlas} to apply changes to
     * @param changeSet
     *            The {@link RelationChangeSet} to apply
     */
    public RelationChangeSetHandler(final Atlas atlas, final RelationChangeSet changeSet)
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
        // Log original Atlas statistics
        logger.info(atlasStatistics(super.getAtlas()));

        // Prepare the builder
        setAtlasSizeEstimateAndMetadata();

        // Add any points created by Relation slicing
        addNewPoints();

        // Add all Points and Lines
        addExistingPointsAndLines();

        // Add any Lines created by Relation slicing
        addNewLines();

        // Process the change set
        addUpdatedRelations();
        addNewRelations();

        // Build and log
        final Atlas atlasWithUpdates = this.getBuilder().get();
        logger.info(atlasStatistics(atlasWithUpdates));

        return atlasWithUpdates;
    }

    private void addExistingPointsAndLines()
    {
        this.getAtlas().points().forEach(point -> this.getBuilder().addPoint(point.getIdentifier(),
                point.getLocation(), point.getTags()));
        this.getAtlas().lines().forEach(line ->
        {
            // Add the line, if it hasn't been removed
            if (!this.changeSet.getDeletedLines().contains(line.getIdentifier()))
            {
                this.getBuilder().addLine(line.getIdentifier(), line.asPolyLine(), line.getTags());
            }
        });
    }

    private void addNewLines()
    {
        this.changeSet.getCreatedLines().values().forEach(line -> this.getBuilder()
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
     * Add any new {@link Relation}s from the change set.
     */
    private void addNewRelations()
    {
        this.changeSet.getCreatedRelations()
                .forEach(relation -> this.getBuilder().addRelation(relation.getIdentifier(),
                        getOsmIdentifier(relation.getIdentifier()), relation.getRelationBean(),
                        relation.getTags()));
    }

    /**
     * Updates the tags for all existing {@link Relation}s in the original Atlas, unless the
     * {@link Relation} was deleted by the change set, and adds it to the updated Atlas.
     */
    private void addUpdatedRelations()
    {
        this.getAtlas().relationsLowerOrderFirst().forEach(relation ->
        {
            final long identifier = relation.getIdentifier();
            // Only add if we've not deleted this relation
            if (!this.changeSet.getDeletedRelations().contains(identifier))
            {
                // Add the Relation with the updated tag value
                if (this.changeSet.getUpdatedRelationTags().containsKey(identifier))
                {
                    final Relation originalRelation = this.getAtlas().relation(identifier);

                    // Update the tags
                    final Map<String, String> updatedTags = originalRelation.getTags();
                    updatedTags.putAll(this.changeSet.getUpdatedRelationTags().get(identifier));

                    // Add the modified Relation
                    this.getBuilder().addRelation(identifier, getOsmIdentifier(identifier),
                            constructRelationBean(relation), updatedTags);
                }
                else
                {
                    // All Relations should have at least a country code tag addition after
                    // country-slicing takes place. If it doesn't, it means we couldn't slice it
                    // properly. Add this feature into the Atlas with a missing country code.
                    logger.error(
                            "Adding Relation {} with missing country code to maintain Atlas integrity.",
                            identifier);

                    final Map<String, String> updatedTags = relation.getTags();
                    updatedTags.put(ISOCountryTag.KEY, ISOCountryTag.COUNTRY_MISSING);
                    this.getBuilder().addRelation(identifier, getOsmIdentifier(identifier),
                            constructRelationBean(relation), updatedTags);
                }
            }
        });
    }

    private RelationBean constructRelationBean(final Relation relation)
    {
        final RelationBean bean = new RelationBean();
        if (!relation.members().isEmpty())
        {
            final Set<TemporaryRelationMember> membersToRemove = Optional.ofNullable(
                    this.changeSet.getDeletedRelationMembers().get(relation.getIdentifier()))
                    .orElse(new HashSet<>());
            relation.members().forEach(member ->
            {
                // Only add the member if it wasn't on the list to be removed
                if (shouldAddRelationMember(membersToRemove, member))
                {
                    bean.addItem(member.getEntity().getIdentifier(), member.getRole(),
                            ItemType.forEntity(member.getEntity()));
                }
            });
        }

        // Add any new members for this relation
        final Set<TemporaryRelationMember> membersToAdd = Optional
                .ofNullable(this.changeSet.getAddedRelationMembers().get(relation.getIdentifier()))
                .orElse(new HashSet<>());
        for (final TemporaryRelationMember memberToAdd : membersToAdd)
        {
            bean.addItem(memberToAdd.getIdentifier(), memberToAdd.getRole(), memberToAdd.getType());
        }

        if (bean.isEmpty())
        {
            logger.error("Constructed an empty bean for relation {}", relation.getIdentifier());
        }
        return bean;
    }

    private long getOsmIdentifier(final long identifier)
    {
        return new ReverseIdentifierFactory().getOsmIdentifier(identifier);
    }

    private boolean shouldAddRelationMember(final Set<TemporaryRelationMember> membersToRemove,
            final RelationMember member)
    {
        final TemporaryRelationMember temporary = new TemporaryRelationMember(
                member.getEntity().getIdentifier(), member.getRole(), member.getEntity().getType());
        if (membersToRemove.contains(temporary))
        {
            // Optimization to shorten our removal list
            membersToRemove.remove(temporary);
            return false;
        }

        // If we get here, this member isn't on the list of removed members
        return true;
    }
}
