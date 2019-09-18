package org.openstreetmap.atlas.geography.atlas.change.description;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.atlas.builder.RelationBean;
import org.openstreetmap.atlas.geography.atlas.change.description.descriptors.ChangeDescriptor;
import org.openstreetmap.atlas.geography.atlas.change.description.descriptors.ChangeDescriptorComparator;
import org.openstreetmap.atlas.geography.atlas.change.description.descriptors.ChangeDescriptorName;
import org.openstreetmap.atlas.geography.atlas.change.description.descriptors.GenericElementChangeDescriptor;
import org.openstreetmap.atlas.geography.atlas.change.description.descriptors.GeometryChangeDescriptor;
import org.openstreetmap.atlas.geography.atlas.change.description.descriptors.RelationMemberChangeDescriptor;
import org.openstreetmap.atlas.geography.atlas.change.description.descriptors.TagChangeDescriptor;
import org.openstreetmap.atlas.geography.atlas.complete.CompleteEdge;
import org.openstreetmap.atlas.geography.atlas.complete.CompleteEntity;
import org.openstreetmap.atlas.geography.atlas.complete.CompleteNode;
import org.openstreetmap.atlas.geography.atlas.complete.CompleteRelation;
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.geography.atlas.items.ItemType;

/**
 * A helper class for generating a list of {@link ChangeDescriptor}s based on some
 * {@link AtlasEntity} beforeView and afterView.
 * 
 * @author lcram
 */
public final class ChangeDescriptorGenerator
{
    private static final ChangeDescriptorComparator COMPARATOR = new ChangeDescriptorComparator();
    private static final String CORRUPTED_FEATURECHANGE_MESSAGE = "Corrupted FeatureChange: afterView {} != null but beforeView {} == null";

    private final AtlasEntity beforeView;
    private final AtlasEntity afterView;
    private final ChangeDescriptorType changeDescriptorType;

    ChangeDescriptorGenerator(final AtlasEntity beforeView, final AtlasEntity afterView,
            final ChangeDescriptorType changeDescriptorType)
    {
        this.beforeView = beforeView;
        this.afterView = afterView;
        this.changeDescriptorType = changeDescriptorType;
    }

    List<ChangeDescriptor> generate()
    {
        final List<ChangeDescriptor> descriptors = new ArrayList<>();

        /*
         * For the REMOVE case, there's no point showing any details. Users can just look at the
         * FeatureChange output itself to see the beforeView and afterView.
         */
        if (this.changeDescriptorType == ChangeDescriptorType.REMOVE)
        {
            return descriptors;
        }

        descriptors.addAll(generateTagDescriptors());
        descriptors.addAll(generateGeometryDescriptors());
        descriptors.addAll(generateParentRelationDescriptors(CompleteEntity::relationIdentifiers));
        if (this.afterView.getType() == ItemType.NODE)
        {
            descriptors.addAll(generateNodeInOutDescriptors(ChangeDescriptorName.IN_EDGE,
                    CompleteNode::inEdgeIdentifiers));
            descriptors.addAll(generateNodeInOutDescriptors(ChangeDescriptorName.OUT_EDGE,
                    CompleteNode::outEdgeIdentifiers));
        }
        if (this.afterView.getType() == ItemType.EDGE)
        {
            descriptors.addAll(generateEdgeStartEndDescriptors(ChangeDescriptorName.START_NODE,
                    CompleteEdge::startNodeIdentifier));
            descriptors.addAll(generateEdgeStartEndDescriptors(ChangeDescriptorName.END_NODE,
                    CompleteEdge::endNodeIdentifier));
        }
        if (this.afterView.getType() == ItemType.RELATION)
        {
            descriptors.addAll(generateRelationMemberDescriptors());
            /*
             * Should we handle the other special relation fields here?
             * allRelationsWithSameOsmIdentifier, allKnownOsmMembers, and osmRelationIdentifier are
             * fields that may be altered.
             */
        }

        descriptors.sort(COMPARATOR);
        return descriptors;
    }

    ChangeDescriptorType getChangeDescriptorType()
    {
        return this.changeDescriptorType;
    }

    private List<GenericElementChangeDescriptor<Long>> generateEdgeStartEndDescriptors(
            final ChangeDescriptorName name, final Function<CompleteEdge, Long> memberExtractor) // NOSONAR
    {
        final CompleteEdge beforeEntity = (CompleteEdge) this.beforeView;
        final CompleteEdge afterEntity = (CompleteEdge) this.afterView;

        /*
         * If the afterView identifier was null, then we know that it was not updated. We can just
         * return nothing.
         */
        if (memberExtractor.apply(afterEntity) == null)
        {
            return new ArrayList<>();
        }

        final Long beforeIdentifier;
        if (beforeEntity != null)
        {
            if (memberExtractor.apply(beforeEntity) == null)
            {
                throw new CoreException(CORRUPTED_FEATURECHANGE_MESSAGE, name, name);
            }
            beforeIdentifier = memberExtractor.apply(beforeEntity);
        }
        else
        {
            beforeIdentifier = null;
        }
        final Long afterIdentifier = memberExtractor.apply(afterEntity);

        return generateLongValueDescriptors(name, beforeIdentifier, afterIdentifier);
    }

    private List<ChangeDescriptor> generateGeometryDescriptors()
    {
        final List<ChangeDescriptor> descriptors = new ArrayList<>();

        /*
         * Relations do not have explicit geometry, so return nothing.
         */
        if (this.afterView.getType() == ItemType.RELATION)
        {
            return descriptors;
        }

        final CompleteEntity<? extends CompleteEntity<?>> beforeEntity = (CompleteEntity<? extends CompleteEntity<?>>) this.beforeView;
        final CompleteEntity<? extends CompleteEntity<?>> afterEntity = (CompleteEntity<? extends CompleteEntity<?>>) this.afterView;

        /*
         * If the afterView geometry is null, then we know the geometry was not updated.
         */
        if (afterEntity.getGeometry() == null)
        {
            return descriptors;
        }

        final List<Location> beforeGeometry = new ArrayList<>();
        final List<Location> afterGeometry = new ArrayList<>();
        afterEntity.getGeometry().forEach(afterGeometry::add);
        if (beforeEntity != null)
        {
            if (beforeEntity.getGeometry() == null)
            {
                throw new CoreException(CORRUPTED_FEATURECHANGE_MESSAGE, "geometry", "geometry");
            }
            beforeEntity.getGeometry().forEach(beforeGeometry::add);
        }
        descriptors.addAll(
                GeometryChangeDescriptor.getDescriptorsForGeometry(beforeGeometry, afterGeometry));

        return descriptors;
    }

    private List<GenericElementChangeDescriptor<Long>> generateLongSetDescriptors(
            final ChangeDescriptorName name, final Set<Long> beforeSet, final Set<Long> afterSet)
    {
        final List<GenericElementChangeDescriptor<Long>> descriptors = new ArrayList<>();

        final Set<Long> removedFromAfterView = com.google.common.collect.Sets.difference(beforeSet,
                afterSet);
        final Set<Long> addedToAfterView = com.google.common.collect.Sets.difference(afterSet,
                beforeSet);
        for (final Long identifier : removedFromAfterView)
        {
            descriptors.add(new GenericElementChangeDescriptor<>(ChangeDescriptorType.REMOVE,
                    identifier, null, name));
        }
        for (final Long identifier : addedToAfterView)
        {
            descriptors.add(new GenericElementChangeDescriptor<>(ChangeDescriptorType.ADD,
                    identifier, name));
        }
        return descriptors;
    }

    private List<GenericElementChangeDescriptor<Long>> generateLongValueDescriptors(
            final ChangeDescriptorName name, final Long beforeIdentifier,
            final Long afterIdentifier)
    {
        final List<GenericElementChangeDescriptor<Long>> descriptors = new ArrayList<>();

        /*
         * This case occurs when an brand new Long value (e.g. startNode, endNode, etc.) is being
         * added, and so there is no beforeElement.
         */
        if (beforeIdentifier == null)
        {
            descriptors.add(new GenericElementChangeDescriptor<>(ChangeDescriptorType.ADD, null,
                    afterIdentifier, name));
        }
        else
        {
            descriptors.add(new GenericElementChangeDescriptor<>(ChangeDescriptorType.UPDATE,
                    beforeIdentifier, afterIdentifier, name));
        }

        return descriptors;
    }

    private List<GenericElementChangeDescriptor<Long>> generateNodeInOutDescriptors(
            final ChangeDescriptorName name,
            final Function<CompleteNode, Set<Long>> memberExtractor)
    {
        final CompleteNode beforeEntity = (CompleteNode) this.beforeView;
        final CompleteNode afterEntity = (CompleteNode) this.afterView;

        /*
         * If the afterView in/out edge set was null, then we know that it was not updated. We can
         * just return nothing.
         */
        if (memberExtractor.apply(afterEntity) == null)
        {
            return new ArrayList<>();
        }

        final Set<Long> beforeSet;
        if (beforeEntity != null)
        {
            if (memberExtractor.apply(beforeEntity) == null)
            {
                throw new CoreException(CORRUPTED_FEATURECHANGE_MESSAGE, name, name);
            }
            beforeSet = memberExtractor.apply(beforeEntity);
        }
        else
        {
            beforeSet = new HashSet<>();
        }
        final Set<Long> afterSet = memberExtractor.apply(afterEntity);

        return generateLongSetDescriptors(name, beforeSet, afterSet);
    }

    private List<GenericElementChangeDescriptor<Long>> generateParentRelationDescriptors(
            final Function<CompleteEntity, Set<Long>> memberExtractor)
    {
        final ChangeDescriptorName name = ChangeDescriptorName.PARENT_RELATION;

        final CompleteEntity<? extends CompleteEntity<?>> beforeEntity = (CompleteEntity<? extends CompleteEntity<?>>) this.beforeView;
        final CompleteEntity<? extends CompleteEntity<?>> afterEntity = (CompleteEntity<? extends CompleteEntity<?>>) this.afterView;

        /*
         * If the afterView parent relations were null, then we know that they were not updated. We
         * can just return nothing.
         */
        if (memberExtractor.apply(afterEntity) == null)
        {
            return new ArrayList<>();
        }

        final Set<Long> beforeSet;
        if (beforeEntity != null)
        {
            if (memberExtractor.apply(beforeEntity) == null)
            {
                throw new CoreException(CORRUPTED_FEATURECHANGE_MESSAGE, name, name);
            }
            beforeSet = memberExtractor.apply(beforeEntity);
        }
        else
        {
            beforeSet = new HashSet<>();
        }
        final Set<Long> afterSet = memberExtractor.apply(afterEntity);

        return generateLongSetDescriptors(name, beforeSet, afterSet);
    }

    private List<ChangeDescriptor> generateRelationMemberDescriptors()
    {
        final List<ChangeDescriptor> descriptors = new ArrayList<>();

        final CompleteRelation beforeEntity = (CompleteRelation) this.beforeView;
        final CompleteRelation afterEntity = (CompleteRelation) this.afterView;

        /*
         * If the afterView members were null, then we know that the members were not updated. We
         * can just return nothing.
         */
        if (afterEntity.members() == null)
        {
            return descriptors;
        }

        final Set<RelationBean.RelationBeanItem> beforeBeanSet;
        if (beforeEntity != null)
        {
            if (beforeEntity.members() == null)
            {
                throw new CoreException(CORRUPTED_FEATURECHANGE_MESSAGE, "relation members",
                        "relation members");
            }
            beforeBeanSet = beforeEntity.members().asBean().asSet();
        }
        else
        {
            beforeBeanSet = new HashSet<>();
        }
        final Set<RelationBean.RelationBeanItem> afterBeanSet = afterEntity.members().asBean()
                .asSet();

        final Set<RelationBean.RelationBeanItem> itemsRemovedFromAfterView = com.google.common.collect.Sets
                .difference(beforeBeanSet, afterBeanSet);
        final Set<RelationBean.RelationBeanItem> itemsAddedToAfterView = com.google.common.collect.Sets
                .difference(afterBeanSet, beforeBeanSet);

        for (final RelationBean.RelationBeanItem item : itemsRemovedFromAfterView)
        {
            descriptors.add(new RelationMemberChangeDescriptor(ChangeDescriptorType.REMOVE,
                    item.getIdentifier(), item.getType(), item.getRole()));
        }
        for (final RelationBean.RelationBeanItem item : itemsAddedToAfterView)
        {
            descriptors.add(new RelationMemberChangeDescriptor(ChangeDescriptorType.ADD,
                    item.getIdentifier(), item.getType(), item.getRole()));
        }

        return descriptors;
    }

    private List<ChangeDescriptor> generateTagDescriptors()
    {
        final List<ChangeDescriptor> descriptors = new ArrayList<>();

        /*
         * If the afterView tags were null, then we know that the tags were not updated. We can just
         * return nothing.
         */
        if (this.afterView.getTags() == null)
        {
            return descriptors;
        }

        final Map<String, String> beforeTags;
        if (this.beforeView != null)
        {
            if (this.beforeView.getTags() == null)
            {
                throw new CoreException(CORRUPTED_FEATURECHANGE_MESSAGE, "tags", "tags");
            }
            beforeTags = this.beforeView.getTags();
        }
        else
        {
            beforeTags = new HashMap<>();
        }
        final Map<String, String> afterTags = this.afterView.getTags();

        final Set<String> keysRemovedFromAfterView = com.google.common.collect.Sets
                .difference(beforeTags.keySet(), afterTags.keySet());
        final Set<String> keysAddedToAfterView = com.google.common.collect.Sets
                .difference(afterTags.keySet(), beforeTags.keySet());
        final Set<String> keysShared = com.google.common.collect.Sets
                .intersection(beforeTags.keySet(), afterTags.keySet());
        for (final String key : keysRemovedFromAfterView)
        {
            descriptors.add(
                    new TagChangeDescriptor(ChangeDescriptorType.REMOVE, key, beforeTags.get(key)));
        }
        for (final String key : keysAddedToAfterView)
        {
            descriptors.add(
                    new TagChangeDescriptor(ChangeDescriptorType.ADD, key, afterTags.get(key)));
        }
        for (final String key : keysShared)
        {
            if (!beforeTags.get(key).equals(afterTags.get(key)))
            {
                descriptors.add(new TagChangeDescriptor(ChangeDescriptorType.UPDATE, key,
                        afterTags.get(key), beforeTags.get(key)));
            }
        }

        return descriptors;
    }
}
