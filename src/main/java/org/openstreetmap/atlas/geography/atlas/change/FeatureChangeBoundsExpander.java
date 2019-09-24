package org.openstreetmap.atlas.geography.atlas.change;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.Rectangle;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.complete.CompleteArea;
import org.openstreetmap.atlas.geography.atlas.complete.CompleteEdge;
import org.openstreetmap.atlas.geography.atlas.complete.CompleteLine;
import org.openstreetmap.atlas.geography.atlas.complete.CompleteNode;
import org.openstreetmap.atlas.geography.atlas.complete.CompletePoint;
import org.openstreetmap.atlas.geography.atlas.complete.CompleteRelation;
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.ItemType;
import org.openstreetmap.atlas.geography.atlas.items.Node;
import org.openstreetmap.atlas.geography.atlas.items.Relation;
import org.openstreetmap.atlas.geography.atlas.items.RelationMemberList;
import org.openstreetmap.atlas.utilities.maps.MultiMapWithSet;

/**
 * Expand the size of bounds for features that belong to a relation, or for nodes that are connected
 * to edges, thus expanding the full geographical impact of a FeatureChange
 * 
 * @author matthieun
 */
public class FeatureChangeBoundsExpander
{
    private final Set<FeatureChange> featureChanges;
    private Atlas atlas;
    private final Predicate<FeatureChange> needsUpdate = featureChange ->
    {
        if (featureChange.getItemType() == ItemType.NODE)
        {
            return true;
        }
        if (featureChange.getChangeType() == ChangeType.ADD)
        {
            // For relation members, only look at removes
            return false;
        }
        final Set<Relation> relations = featureChange.getAfterView().relations();
        if (relations != null && !relations.isEmpty())
        {
            return true;
        }
        final AtlasEntity entity = this.atlas.entity(featureChange.getIdentifier(),
                featureChange.getItemType());
        return entity != null && !entity.relations().isEmpty();
    };
    private final Set<FeatureChange> result = new HashSet<>();
    private final Set<FeatureChange> featureChangesToUpdate = new HashSet<>();
    private final MultiMapWithSet<AtlasEntityKey, Rectangle> typeIdentifierToExtensionBounds = new MultiMapWithSet<>();
    private final Map<AtlasEntityKey, FeatureChange> typeIdentifierToFeatureChange = new HashMap<>();

    public FeatureChangeBoundsExpander(final Set<FeatureChange> featureChanges, final Atlas atlas)
    {
        this.featureChanges = featureChanges;
        this.atlas = atlas;
    }

    public Set<FeatureChange> apply()
    {
        if (!this.result.isEmpty())
        {
            throw new CoreException("Cannot apply the same bounds expander twice!");
        }
        this.featureChanges.forEach(featureChange -> this.typeIdentifierToFeatureChange.put(
                AtlasEntityKey.from(featureChange.getItemType(), featureChange.getIdentifier()),
                featureChange));
        findBounds();
        for (final FeatureChange featureChange : this.featureChangesToUpdate)
        {
            final Set<Rectangle> expansionRectangles = this.typeIdentifierToExtensionBounds
                    .get(AtlasEntityKey.from(featureChange.getItemType(),
                            featureChange.getIdentifier()));
            FeatureChange newFeatureChange = featureChange;
            if (expansionRectangles != null)
            {
                newFeatureChange = new FeatureChange(featureChange.getChangeType(),
                        expanded(featureChange.getAfterView(), expansionRectangles),
                        featureChange.getBeforeView());
            }

            this.result.add(newFeatureChange);
        }
        return this.result;
    }

    private AtlasEntity expanded(final AtlasEntity other, final Set<Rectangle> expansionRectangles)
    {
        final Rectangle newBounds = Rectangle.forLocated(expansionRectangles);
        if (other instanceof CompleteNode)
        {
            return ((CompleteNode) other).withBoundsExtendedBy(newBounds);
        }
        if (other instanceof CompleteEdge)
        {
            return ((CompleteEdge) other).withBoundsExtendedBy(newBounds);
        }
        if (other instanceof CompleteArea)
        {
            return ((CompleteArea) other).withBoundsExtendedBy(newBounds);
        }
        if (other instanceof CompleteLine)
        {
            return ((CompleteLine) other).withBoundsExtendedBy(newBounds);
        }
        if (other instanceof CompletePoint)
        {
            return ((CompletePoint) other).withBoundsExtendedBy(newBounds);
        }
        if (other instanceof CompleteRelation)
        {
            return ((CompleteRelation) other).withBoundsExtendedBy(newBounds);
        }
        throw new CoreException("AtlasEntity is of a non-workable type: {}",
                other.getClass().getName());
    }

    private void findBounds() // NOSONAR
    {
        for (final FeatureChange featureChange : this.featureChanges)
        {
            final ItemType itemType = featureChange.getItemType();
            if (this.needsUpdate.test(featureChange))
            {
                this.featureChangesToUpdate.add(featureChange);
            }
            else
            {
                this.result.add(featureChange);
            }
            if (itemType == ItemType.RELATION)
            {
                findBoundsFromRelation((Relation) featureChange.getAfterView());
                final Relation relationFromAtlas = this.atlas
                        .relation(featureChange.getIdentifier());
                if (relationFromAtlas != null)
                {
                    findBoundsFromRelation(relationFromAtlas);
                }
            }
            if (itemType == ItemType.EDGE)
            {
                findBoundsFromEdge((Edge) featureChange.getAfterView());
                final Edge edgeFromAtlas = this.atlas.edge(featureChange.getIdentifier());
                if (edgeFromAtlas != null)
                {
                    findBoundsFromEdge(edgeFromAtlas);
                }
            }
        }
        for (final Edge edge : this.atlas.edges())
        {
            final AtlasEntityKey startKey = AtlasEntityKey.from(ItemType.NODE,
                    edge.start().getIdentifier());
            final AtlasEntityKey endKey = AtlasEntityKey.from(ItemType.NODE,
                    edge.end().getIdentifier());
            if (this.typeIdentifierToFeatureChange.containsKey(startKey)
                    || this.typeIdentifierToFeatureChange.containsKey(endKey))
            {
                findBoundsFromEdge(edge);
            }
        }
        for (final Relation relation : this.atlas.relations())
        {
            final Set<AtlasEntityKey> memberKeys = new HashSet<>();
            relation.members().forEach(member -> memberKeys.add(AtlasEntityKey
                    .from(member.getEntity().getType(), member.getEntity().getIdentifier())));
            if (memberKeys.stream().anyMatch(this.typeIdentifierToFeatureChange::containsKey))
            {
                findBoundsFromRelation(relation);
            }
        }
    }

    private void findBoundsFromEdge(final Edge edge)
    {
        final Node start = edge.start();
        final Node end = edge.end();
        final Rectangle bounds = edge.bounds();
        if (start != null)
        {
            this.typeIdentifierToExtensionBounds
                    .add(AtlasEntityKey.from(ItemType.NODE, start.getIdentifier()), bounds);
        }
        if (end != null)
        {
            this.typeIdentifierToExtensionBounds
                    .add(AtlasEntityKey.from(ItemType.NODE, end.getIdentifier()), bounds);
        }
    }

    private void findBoundsFromRelation(final Relation relation)
    {
        final RelationMemberList members = relation.members();
        if (members != null && !members.isEmpty())
        {
            final Rectangle bounds = relation.bounds();
            members.forEach(relationMember ->
            {
                final AtlasEntity entity = relationMember.getEntity();
                this.typeIdentifierToExtensionBounds
                        .add(AtlasEntityKey.from(entity.getType(), entity.getIdentifier()), bounds);
            });
        }
    }
}
