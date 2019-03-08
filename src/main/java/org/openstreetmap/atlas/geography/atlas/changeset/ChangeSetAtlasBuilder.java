package org.openstreetmap.atlas.geography.atlas.changeset;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.StreamSupport;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.Polygon;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.AtlasMetaData;
import org.openstreetmap.atlas.geography.atlas.builder.RelationBean;
import org.openstreetmap.atlas.geography.atlas.items.AtlasItem;
import org.openstreetmap.atlas.geography.atlas.items.ItemType;
import org.openstreetmap.atlas.geography.atlas.packed.PackedAtlasBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ChangeSetAtlasBuilder takes an original atlas and accept a ChangeSet to build a new atlas with
 * all changes applied. Changes follows CUD(Create, Update, Delete) pattern with partial support of
 * edges and relations.
 * <p>
 * <b>this class is not thread safe</b>
 * </p>
 *
 * @author Yiqing Jin
 * @deprecated - see new API under org.openstreetmap.atlas.geography.atlas.change package.
 */
@Deprecated
public class ChangeSetAtlasBuilder
{
    private static final Logger logger = LoggerFactory.getLogger(ChangeSetAtlasBuilder.class);
    private static final int MAXIMUM_RELATION_LOOPS = 500;
    private final PackedAtlasBuilder builder;
    private final Atlas originalAtlas;
    private final ChangeSet changeSet;
    private Atlas conflatedAtlas;

    public ChangeSetAtlasBuilder(final Atlas atlas, final ChangeSet changeSet)
    {
        this.originalAtlas = atlas;
        this.changeSet = changeSet;
        this.builder = new PackedAtlasBuilder().withMetaData(new AtlasMetaData())
                .withSizeEstimates(atlas.size());
    }

    /**
     * build and return atlas object
     *
     * @return atlas been built
     */
    public Atlas get()
    {
        if (this.conflatedAtlas != null)
        {
            return this.conflatedAtlas;
        }
        handleSimple(this.originalAtlas.nodes());
        handleSimple(this.originalAtlas.points());
        handleSimple(this.originalAtlas.lines());
        handleSimple(this.originalAtlas.edges());
        handleSimple(this.originalAtlas.areas());
        handleRelations();
        this.conflatedAtlas = this.builder.get();
        return this.conflatedAtlas;
    }

    private void addItem(final long identifier, final ItemType type,
            final Iterable<Location> geometry, final Map<String, String> tags)
    {
        switch (type)
        {
            case POINT:
                this.builder.addPoint(identifier, (Location) geometry, tags);
                break;
            case NODE:
                this.builder.addNode(identifier, (Location) geometry, tags);
                break;
            case LINE:
                this.builder.addLine(identifier, (PolyLine) geometry, tags);
                break;
            case EDGE:
                this.builder.addEdge(identifier, (PolyLine) geometry, tags);
                break;
            case AREA:
                this.builder.addArea(identifier, (Polygon) geometry, tags);
                break;
            case RELATION:
            default:
                throw new IllegalArgumentException("addItem can't take relation type");
        }
    }

    private void handleRelations()
    {
        this.originalAtlas.relations().forEach(relation ->
        {
            if (this.changeSet.contains(relation.getIdentifier(), ItemType.RELATION,
                    ChangeAction.DELETE))
            {
                return;
            }
            final Optional<ChangeItem> optionalItem = this.changeSet.get(relation.getIdentifier(),
                    ItemType.RELATION, ChangeAction.UPDATE);
            if (optionalItem.isPresent())
            {
                final ChangeItem changeItem = optionalItem.get();
                this.builder.addRelation(relation.getIdentifier(), relation.getOsmIdentifier(),
                        changeItem.getRelationBean().get(), changeItem.getTags());
            }
            else
            {
                final RelationBean bean = new RelationBean();
                relation.members()
                        .forEach(member -> bean.addItem(member.getEntity().getIdentifier(),
                                member.getRole(), member.getEntity().getType()));
                this.builder.addRelation(relation.getIdentifier(), relation.osmRelationIdentifier(),
                        bean, relation.getTags());
            }
        });
        // Add all the relations that do not have members that are relations.
        Set<Long> stagedRelationIdentifiers = new HashSet<>();
        final Iterator<ChangeItem> iterator = this.changeSet.iterator(ItemType.RELATION,
                ChangeAction.CREATE);
        while (iterator.hasNext())
        {
            final ChangeItem relationMatchResult = iterator.next();
            if (StreamSupport.stream(relationMatchResult.getMembers().spliterator(), false)
                    .anyMatch(member -> member.getType() == ItemType.RELATION))
            {
                stagedRelationIdentifiers.add(relationMatchResult.getIdentifier());
            }
            else
            {
                this.builder.addRelation(relationMatchResult.getIdentifier(),
                        relationMatchResult.getIdentifier(),
                        relationMatchResult.getRelationBean().get(), relationMatchResult.getTags());
            }
        }
        // Add all the other relations
        int iterations = 0;
        while (++iterations < MAXIMUM_RELATION_LOOPS && !stagedRelationIdentifiers.isEmpty())
        {
            logger.trace("Copying relations level {} deep.", iterations);
            final Set<Long> stagedRelationIdentifiersCopy = new HashSet<>();
            for (final Long relationIdentifier : stagedRelationIdentifiers)
            {
                final Optional<ChangeItem> optionalItem = this.changeSet.get(relationIdentifier,
                        ItemType.RELATION, ChangeAction.CREATE);
                if (!optionalItem.isPresent())
                {
                    logger.error("can't find relation with id {}", relationIdentifier);
                    continue;
                }
                final ChangeItem relationItem = optionalItem.get();
                final boolean skip = StreamSupport
                        .stream(relationItem.getMembers().spliterator(), false)
                        .allMatch(member -> member.getType() == ItemType.RELATION
                                && this.builder.peek().relation(member.getIdentifier()) == null);
                if (!skip)
                {
                    this.builder.addRelation(relationItem.getIdentifier(),
                            relationItem.getIdentifier(), relationItem.getRelationBean().get(),
                            relationItem.getTags());
                }
                else
                {
                    stagedRelationIdentifiersCopy.add(relationIdentifier);
                }
            }
            stagedRelationIdentifiers = stagedRelationIdentifiersCopy;
        }
        if (iterations >= MAXIMUM_RELATION_LOOPS)
        {
            throw new CoreException(
                    "There might be a loop in relations! It took more than {} loops to copy the relation.",
                    MAXIMUM_RELATION_LOOPS);
        }
    }

    private void handleSimple(final Iterable<? extends AtlasItem> items)
    {
        items.forEach(item ->
        {
            if (this.changeSet.contains(item.getIdentifier(), item.getType(), ChangeAction.DELETE))
            {
                return;
            }
            final Optional<ChangeItem> optionalItem = this.changeSet.get(item.getIdentifier(),
                    item.getType(), ChangeAction.UPDATE);
            if (optionalItem.isPresent())
            {
                final ChangeItem changeItem = optionalItem.get();
                addItem(changeItem.getIdentifier(), item.getType(), changeItem.getGeometry(),
                        changeItem.getTags());
            }
            else
            {
                addItem(item.getIdentifier(), item.getType(), item.getRawGeometry(),
                        item.getTags());
            }
        });
        final Iterator<? extends AtlasItem> iterator = items.iterator();
        if (!iterator.hasNext())
        {
            return;
        }
        final ItemType type = iterator.next().getType();
        this.changeSet.iterator(type, ChangeAction.CREATE).forEachRemaining(changeItem ->
        {
            addItem(changeItem.getIdentifier(), type, changeItem.getGeometry(),
                    changeItem.getTags());
        });
    }
}
