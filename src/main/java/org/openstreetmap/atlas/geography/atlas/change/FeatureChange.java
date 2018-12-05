package org.openstreetmap.atlas.geography.atlas.change;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.Located;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.Polygon;
import org.openstreetmap.atlas.geography.Rectangle;
import org.openstreetmap.atlas.geography.atlas.bloated.BloatedArea;
import org.openstreetmap.atlas.geography.atlas.bloated.BloatedEdge;
import org.openstreetmap.atlas.geography.atlas.bloated.BloatedEntity;
import org.openstreetmap.atlas.geography.atlas.bloated.BloatedLine;
import org.openstreetmap.atlas.geography.atlas.bloated.BloatedNode;
import org.openstreetmap.atlas.geography.atlas.bloated.BloatedPoint;
import org.openstreetmap.atlas.geography.atlas.bloated.BloatedRelation;
import org.openstreetmap.atlas.geography.atlas.builder.RelationBean;
import org.openstreetmap.atlas.geography.atlas.items.Area;
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.ItemType;
import org.openstreetmap.atlas.geography.atlas.items.LineItem;
import org.openstreetmap.atlas.geography.atlas.items.LocationItem;
import org.openstreetmap.atlas.geography.atlas.items.Node;
import org.openstreetmap.atlas.geography.atlas.items.Relation;
import org.openstreetmap.atlas.tags.Taggable;
import org.openstreetmap.atlas.utilities.collections.Maps;
import org.openstreetmap.atlas.utilities.collections.Sets;

/**
 * Single feature change, does not include any consistency checks.
 * <p>
 * To add a new, non existing feature: {@link ChangeType} is ADD, and the included reference needs
 * to contain all the information related to that new feature.
 * <p>
 * To modify an existing feature: {@link ChangeType} is ADD, and the included reference needs to
 * contain the only the changed information related to that changed feature.
 * <p>
 * To remove an existing feature: {@link ChangeType} is REMOVE. The included reference's only
 * feature that needs to match the existing feature is the identifier.
 *
 * @author matthieun
 */
public class FeatureChange implements Located, Serializable
{
    private static final long serialVersionUID = 9172045162819925515L;
    private static final BinaryOperator<Map<String, String>> tagMerger = Maps::withMaps;
    private static final BinaryOperator<Set<Long>> directReferenceMerger = Sets::withSets;
    private static final BinaryOperator<Set<Long>> directReferenceMergerLoose = (left,
            right) -> Sets.withSets(false, left, right);
    private static final BinaryOperator<RelationBean> relationBeanMerger = RelationBean::merge;

    private final ChangeType changeType;
    private final AtlasEntity reference;

    public static FeatureChange add(final AtlasEntity reference)
    {
        return new FeatureChange(ChangeType.ADD, reference);
    }

    public static FeatureChange remove(final AtlasEntity reference)
    {
        return new FeatureChange(ChangeType.REMOVE, reference);
    }

    public FeatureChange(final ChangeType changeType, final AtlasEntity reference)
    {
        if (reference == null)
        {
            throw new CoreException("reference cannot be null.");
        }
        if (!(reference instanceof BloatedEntity))
        {
            throw new CoreException(
                    "FeatureChange requires BloatedEntity, found reference of type {}",
                    reference.getClass().getName());
        }
        if (changeType == null)
        {
            throw new CoreException("changeType cannot be null.");
        }
        this.changeType = changeType;
        this.reference = reference;
        this.validateUsefulFeatureChange();
    }

    @Override
    public Rectangle bounds()
    {
        return this.reference.bounds();
    }

    @Override
    public boolean equals(final Object other)
    {
        if (other instanceof FeatureChange)
        {
            final FeatureChange that = (FeatureChange) other;
            return this.getChangeType() == that.getChangeType()
                    && this.getReference().equals(that.getReference());
        }
        return false;
    }

    public ChangeType getChangeType()
    {
        return this.changeType;
    }

    public long getIdentifier()
    {
        return getReference().getIdentifier();
    }

    public ItemType getItemType()
    {
        return ItemType.forEntity(getReference());
    }

    public AtlasEntity getReference()
    {
        return this.reference;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(this.changeType, this.reference);
    }

    /**
     * Merge two feature changes together. If it cannot succeed, this method will throw a
     * {@link CoreException} explaining why.
     *
     * @param other
     *            The other to merge into this one.
     * @return The merged {@link FeatureChange}
     */
    public FeatureChange merge(final FeatureChange other)
    {
        try
        {
            if (this.getIdentifier() != other.getIdentifier()
                    || this.getItemType() != other.getItemType()
                    || this.getChangeType() != other.getChangeType())
            {
                throw new CoreException(
                        "Cannot merge two feature changes that are not of the same type.");
            }
            if (this.getChangeType() == ChangeType.REMOVE)
            {
                return this;
            }
            if (this.getChangeType() == ChangeType.ADD)
            {
                final AtlasEntity thisReference = this.getReference();
                final AtlasEntity thatReference = other.getReference();
                final Map<String, String> mergedTags = mergedMember("tags", thisReference,
                        thatReference, Taggable::getTags, Optional.of(tagMerger));
                final Set<Long> mergedParentRelations = mergedMember("parentRelations",
                        thisReference, thatReference,
                        atlasEntity -> atlasEntity.relations() == null ? null
                                : atlasEntity.relations().stream().map(Relation::getIdentifier)
                                        .collect(Collectors.toSet()),
                        Optional.of(directReferenceMergerLoose));
                if (thisReference instanceof LocationItem)
                {
                    return mergeLocationItems(other, mergedTags, mergedParentRelations);
                }
                else if (thisReference instanceof LineItem)
                {
                    return mergeLineItems(other, mergedTags, mergedParentRelations);
                }
                else if (thisReference instanceof Area)
                {
                    return mergeAreas(other, mergedTags, mergedParentRelations);
                }
                else
                {
                    // Relation
                    return mergeRelations(other, mergedTags, mergedParentRelations);
                }

            }
            throw new CoreException("Unable to merge {} and {}", this, other);
        }
        catch (final Exception e)
        {
            throw new CoreException("Cannot merge two feature changes {} and {}.", this, other, e);
        }
    }

    @Override
    public String toString()
    {
        return "FeatureChange [changeType=" + this.changeType + ", reference={"
                + this.reference.getType() + "," + this.reference.getIdentifier() + "}, bounds="
                + bounds() + "]";
    }

    private FeatureChange mergeAreas(final FeatureChange other,
            final Map<String, String> mergedTags, final Set<Long> mergedParentRelations)
    {
        final AtlasEntity thisReference = this.getReference();
        final AtlasEntity thatReference = other.getReference();
        final Polygon mergedPolygon = mergedMember("polygon", thisReference, thatReference,
                atlasEntity -> ((Area) atlasEntity).asPolygon(), Optional.empty());

        BloatedArea result = new BloatedArea(getIdentifier(), mergedPolygon, mergedTags,
                mergedParentRelations);
        if (result.bounds() == null)
        {
            if (bounds() != null)
            {
                result = result.withAggregateBoundsExtendedUsing(bounds());
            }
            else if (other.bounds() != null)
            {
                result = result.withAggregateBoundsExtendedUsing(other.bounds());
            }
        }
        return FeatureChange.add(result);
    }

    private <M> M mergedMember(final String memberName, final AtlasEntity left,
            final AtlasEntity right, final Function<AtlasEntity, M> memberExtractor,
            final Optional<BinaryOperator<M>> memberMergerOption)
    {
        final M result;
        final M leftMember = memberExtractor.apply(left);
        final M rightMember = memberExtractor.apply(right);
        if (leftMember != null && rightMember != null)
        {
            // Both are not null, merge evaluated
            if (leftMember.equals(rightMember))
            {
                // They are equal, arbitrarily pick one.
                result = leftMember;
            }
            else if (memberMergerOption.isPresent())
            {
                // They are unequal, but we can attempt a merge
                result = memberMergerOption.get().apply(leftMember, rightMember);
            }
            else
            {
                // They are unequal and we do not have a tool to merge them.
                throw new CoreException("Conflicting {}: {} and {}", memberName, leftMember,
                        rightMember);
            }
        }
        else
        {
            // One is not null, or both are.
            if (leftMember != null)
            {
                result = leftMember;
            }
            else
            {
                result = rightMember;
            }
        }
        return result;
    }

    private FeatureChange mergeLineItems(final FeatureChange other,
            final Map<String, String> mergedTags, final Set<Long> mergedParentRelations)
    {
        final AtlasEntity thisReference = this.getReference();
        final AtlasEntity thatReference = other.getReference();
        final PolyLine mergedPolyLine = mergedMember("polyLine", thisReference, thatReference,
                atlasEntity -> ((LineItem) atlasEntity).asPolyLine(), Optional.empty());
        if (thisReference instanceof Edge)
        {
            final Long mergedStartNodeIdentifier = mergedMember("startNode", thisReference,
                    thatReference, edge -> ((Edge) edge).start() == null ? null
                            : ((Edge) edge).start().getIdentifier(),
                    Optional.empty());
            final Long mergedEndNodeIdentifier = mergedMember("endNode", thisReference,
                    thatReference, edge -> ((Edge) edge).end() == null ? null
                            : ((Edge) edge).end().getIdentifier(),
                    Optional.empty());
            BloatedEdge result = new BloatedEdge(getIdentifier(), mergedPolyLine, mergedTags,
                    mergedStartNodeIdentifier, mergedEndNodeIdentifier, mergedParentRelations);
            if (result.bounds() == null)
            {
                if (bounds() != null)
                {
                    result = result.withAggregateBoundsExtendedUsing(bounds());
                }
                else if (other.bounds() != null)
                {
                    result = result.withAggregateBoundsExtendedUsing(other.bounds());
                }
            }
            return FeatureChange.add(result);
        }
        else
        {
            // Line
            BloatedLine result = new BloatedLine(getIdentifier(), mergedPolyLine, mergedTags,
                    mergedParentRelations);
            if (result.bounds() == null)
            {
                if (bounds() != null)
                {
                    result = result.withAggregateBoundsExtendedUsing(bounds());
                }
                else if (other.bounds() != null)
                {
                    result = result.withAggregateBoundsExtendedUsing(other.bounds());
                }
            }
            return FeatureChange.add(result);
        }
    }

    private FeatureChange mergeLocationItems(final FeatureChange other,
            final Map<String, String> mergedTags, final Set<Long> mergedParentRelations)
    {
        final AtlasEntity thisReference = this.getReference();
        final AtlasEntity thatReference = other.getReference();
        final Location mergedLocation = mergedMember("location", thisReference, thatReference,
                atlasEntity -> ((LocationItem) atlasEntity).getLocation(), Optional.empty());
        if (thisReference instanceof Node)
        {
            final SortedSet<Long> mergedInEdgeIdentifiers = (SortedSet<Long>) mergedMember(
                    "inEdgeIdentifiers", thisReference, thatReference,
                    atlasEntity -> ((Node) atlasEntity).inEdges() == null ? null
                            : ((Node) atlasEntity).inEdges().stream().map(Edge::getIdentifier)
                                    .collect(Collectors.toCollection(TreeSet::new)),
                    Optional.of(directReferenceMerger));
            final SortedSet<Long> mergedOutEdgeIdentifiers = (SortedSet<Long>) mergedMember(
                    "outEdgeIdentifiers", thisReference, thatReference,
                    atlasEntity -> ((Node) atlasEntity).outEdges() == null ? null
                            : ((Node) atlasEntity).outEdges().stream().map(Edge::getIdentifier)
                                    .collect(Collectors.toCollection(TreeSet::new)),
                    Optional.of(directReferenceMerger));
            BloatedNode result = new BloatedNode(getIdentifier(), mergedLocation, mergedTags,
                    mergedInEdgeIdentifiers, mergedOutEdgeIdentifiers, mergedParentRelations);
            if (result.bounds() == null)
            {
                if (bounds() != null)
                {
                    result = result.withAggregateBoundsExtendedUsing(bounds());
                }
                else if (other.bounds() != null)
                {
                    result = result.withAggregateBoundsExtendedUsing(other.bounds());
                }
            }
            return FeatureChange.add(result);
        }
        else
        {
            // Point
            BloatedPoint result = new BloatedPoint(getIdentifier(), mergedLocation, mergedTags,
                    mergedParentRelations);
            if (result.bounds() == null)
            {
                if (bounds() != null)
                {
                    result = result.withAggregateBoundsExtendedUsing(bounds());
                }
                else if (other.bounds() != null)
                {
                    result = result.withAggregateBoundsExtendedUsing(other.bounds());
                }
            }
            return FeatureChange.add(result);
        }
    }

    private FeatureChange mergeRelations(final FeatureChange other,
            final Map<String, String> mergedTags, final Set<Long> mergedParentRelations)
    {
        final AtlasEntity thisReference = this.getReference();
        final AtlasEntity thatReference = other.getReference();

        final RelationBean mergedMembers = mergedMember("relationMembers", thisReference,
                thatReference, entity -> ((Relation) entity).members() == null ? null
                        : ((Relation) entity).members().asBean(),
                Optional.of(relationBeanMerger));
        final Rectangle mergedBounds = Rectangle.forLocated(thisReference, thatReference);
        final Long mergedOsmRelationIdentifier = mergedMember("osmRelationIdentifier",
                thisReference, thatReference, entity -> ((Relation) entity).getOsmIdentifier(),
                Optional.empty());
        final Set<Long> mergedAllRelationsWithSameOsmIdentifierSet = mergedMember(
                "allRelationsWithSameOsmIdentifier", thisReference, thatReference,
                atlasEntity -> ((Relation) atlasEntity).allRelationsWithSameOsmIdentifier() == null
                        ? null
                        : ((Relation) atlasEntity).allRelationsWithSameOsmIdentifier().stream()
                                .map(Relation::getIdentifier).collect(Collectors.toSet()),
                Optional.of(directReferenceMerger));
        final List<Long> mergedAllRelationsWithSameOsmIdentifier = mergedAllRelationsWithSameOsmIdentifierSet == null
                ? null
                : mergedAllRelationsWithSameOsmIdentifierSet.stream().collect(Collectors.toList());
        final RelationBean mergedAllKnownMembers = mergedMember("allKnownOsmMembers", thisReference,
                thatReference,
                entity -> ((Relation) entity).allKnownOsmMembers() == null ? null
                        : ((Relation) entity).allKnownOsmMembers().asBean(),
                Optional.of(relationBeanMerger));

        return FeatureChange.add(new BloatedRelation(getIdentifier(), mergedTags, mergedBounds,
                mergedMembers, mergedAllRelationsWithSameOsmIdentifier, mergedAllKnownMembers,
                mergedOsmRelationIdentifier, mergedParentRelations));
    }

    private void validateUsefulFeatureChange()
    {
        if (this.changeType == ChangeType.ADD && this.reference instanceof BloatedEntity
                && ((BloatedEntity) this.reference).isSuperShallow())
        {
            throw new CoreException("{} does not contain anything useful.", this);
        }
    }
}
