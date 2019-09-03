package org.openstreetmap.atlas.geography.atlas.change.description;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.atlas.change.description.descriptors.ChangeDescriptor;
import org.openstreetmap.atlas.geography.atlas.change.description.descriptors.GeometryChangeDescriptor;
import org.openstreetmap.atlas.geography.atlas.change.description.descriptors.ParentRelationChangeDescriptor;
import org.openstreetmap.atlas.geography.atlas.change.description.descriptors.TagChangeDescriptor;
import org.openstreetmap.atlas.geography.atlas.complete.CompleteEntity;
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.geography.atlas.items.ItemType;
import org.openstreetmap.atlas.geography.atlas.items.Relation;

/**
 * @author lcram
 */
public class ChangeDescriptorGenerator
{
    private final AtlasEntity beforeView;
    private final AtlasEntity afterView;

    public ChangeDescriptorGenerator(final AtlasEntity beforeView, final AtlasEntity afterView)
    {
        this.beforeView = beforeView;
        this.afterView = afterView;
    }

    public List<ChangeDescriptor> generate()
    {
        final List<ChangeDescriptor> descriptors = new ArrayList<>();

        descriptors.addAll(generateTagDescriptors());
        descriptors.addAll(generateGeometryDescriptors());
        descriptors.addAll(generateParentRelationDescriptors());

        /*
         * TODO need to generate relationMembers, in/out edges, start/end nodes, and other special
         * relation fields.
         */

        return descriptors;
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

    private List<ChangeDescriptor> generateParentRelationDescriptors()
    {
        final List<ChangeDescriptor> descriptors = new ArrayList<>();

        /*
         * If the afterView parent relations were null, then we know that they were not updated. We
         * can just return nothing.
         */
        if (this.afterView.relations() == null)
        {
            return descriptors;
        }

        final Set<Long> beforeRelations;
        if (this.beforeView != null)
        {
            if (this.beforeView.relations() == null)
            {
                throw new CoreException(
                        "Corrupted FeatureChange: afterView parent relations were non-null but beforeView parent relations were null");
            }
            beforeRelations = this.beforeView.relations().stream().map(Relation::getIdentifier)
                    .collect(Collectors.toSet());
        }
        else
        {
            beforeRelations = new HashSet<>();
        }
        final Set<Long> afterRelations = this.afterView.relations().stream()
                .map(Relation::getIdentifier).collect(Collectors.toSet());

        final Set<Long> relationsRemovedFromAfterView = com.google.common.collect.Sets
                .difference(beforeRelations, afterRelations);
        final Set<Long> relationsAddedToAfterView = com.google.common.collect.Sets
                .difference(afterRelations, beforeRelations);
        for (final Long identifier : relationsRemovedFromAfterView)
        {
            descriptors.add(
                    new ParentRelationChangeDescriptor(ChangeDescriptorType.REMOVE, identifier));
        }
        for (final Long identifier : relationsAddedToAfterView)
        {
            descriptors
                    .add(new ParentRelationChangeDescriptor(ChangeDescriptorType.ADD, identifier));
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
