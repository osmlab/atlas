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
import org.openstreetmap.atlas.geography.atlas.change.ChangeType;
import org.openstreetmap.atlas.geography.atlas.change.description.descriptors.ChangeDescriptor;
import org.openstreetmap.atlas.geography.atlas.change.description.descriptors.GenericElementChangeDescriptor;
import org.openstreetmap.atlas.geography.atlas.change.description.descriptors.GeometryChangeDescriptor;
import org.openstreetmap.atlas.geography.atlas.change.description.descriptors.TagChangeDescriptor;
import org.openstreetmap.atlas.geography.atlas.complete.CompleteEdge;
import org.openstreetmap.atlas.geography.atlas.complete.CompleteEntity;
import org.openstreetmap.atlas.geography.atlas.complete.CompleteNode;
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.geography.atlas.items.ItemType;

/**
 * @author lcram
 */
public class ChangeDescriptorGenerator
{
    private final AtlasEntity beforeView;
    private final AtlasEntity afterView;
    private final ChangeDescriptorType changeDescriptorType;

    public ChangeDescriptorGenerator(final AtlasEntity beforeView, final AtlasEntity afterView,
            final ChangeType sourceFeatureChangeType)
    {
        this.beforeView = beforeView;
        this.afterView = afterView;

        if (sourceFeatureChangeType == ChangeType.ADD)
        {
            if (this.beforeView != null)
            {
                this.changeDescriptorType = ChangeDescriptorType.UPDATE;
            }
            else
            {
                this.changeDescriptorType = ChangeDescriptorType.ADD;
            }
        }
        else
        {
            this.changeDescriptorType = ChangeDescriptorType.REMOVE;
        }
    }

    public List<ChangeDescriptor> generate()
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
            descriptors.addAll(
                    generateNodeInOutDescriptors("IN_EDGE", CompleteNode::inEdgeIdentifiers));
            descriptors.addAll(
                    generateNodeInOutDescriptors("OUT_EDGE", CompleteNode::outEdgeIdentifiers));
        }
        if (this.afterView.getType() == ItemType.EDGE)
        {
            descriptors.addAll(generateEdgeStartEndDescriptors("START_NODE",
                    CompleteEdge::startNodeIdentifier));
            descriptors.addAll(
                    generateEdgeStartEndDescriptors("END_NODE", CompleteEdge::endNodeIdentifier));
        }

        /*
         * TODO need to generate relationMembers, and other special relation fields.
         */

        return descriptors;
    }

    public ChangeDescriptorType getChangeDescriptorType()
    {
        return this.changeDescriptorType;
    }

    private List<GenericElementChangeDescriptor<Long>> generateEdgeStartEndDescriptors(
            final String name, final Function<CompleteEdge, Long> memberExtractor)
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
                throw new CoreException(
                        "Corrupted FeatureChange: afterView {} were non-null but beforeView {} were null",
                        name);
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
                throw new CoreException(
                        "Corrupted FeatureChange: afterView geometry was non-null but beforeView geometry was null");
            }
            beforeEntity.getGeometry().forEach(beforeGeometry::add);
        }
        descriptors.addAll(
                GeometryChangeDescriptor.getDescriptorsForGeometry(beforeGeometry, afterGeometry));

        return descriptors;
    }

    private List<GenericElementChangeDescriptor<Long>> generateLongSetDescriptors(final String name,
            final Set<Long> beforeSet, final Set<Long> afterSet)
    {
        final List<GenericElementChangeDescriptor<Long>> descriptors = new ArrayList<>();

        final Set<Long> removedFromAfterView = com.google.common.collect.Sets.difference(beforeSet,
                afterSet);
        final Set<Long> addedToAfterView = com.google.common.collect.Sets.difference(afterSet,
                beforeSet);
        for (final Long identifier : removedFromAfterView)
        {
            descriptors.add(new GenericElementChangeDescriptor<>(ChangeDescriptorType.REMOVE,
                    identifier, name));
        }
        for (final Long identifier : addedToAfterView)
        {
            descriptors.add(new GenericElementChangeDescriptor<>(ChangeDescriptorType.ADD,
                    identifier, name));
        }
        return descriptors;
    }

    private List<GenericElementChangeDescriptor<Long>> generateLongValueDescriptors(
            final String name, final Long beforeIdentifier, final Long afterIdentifier)
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
            final String name, final Function<CompleteNode, Set<Long>> memberExtractor)
    {
        final CompleteNode beforeEntity = (CompleteNode) this.beforeView;
        final CompleteNode afterEntity = (CompleteNode) this.afterView;

        /*
         * If the afterView set was null, then we know that they were not updated. We can just
         * return nothing.
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
                throw new CoreException(
                        "Corrupted FeatureChange: afterView {} were non-null but beforeView {} were null",
                        name);
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
        final String name = "PARENT_RELATION";

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
                throw new CoreException(
                        "Corrupted FeatureChange: afterView {} were non-null but beforeView {} were null",
                        name);
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
                throw new CoreException(
                        "Corrupted FeatureChange: afterView tags were non-null but beforeView tags were null");
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
