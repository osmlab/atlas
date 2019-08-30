package org.openstreetmap.atlas.geography.atlas.change.description;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.atlas.change.description.descriptors.ChangeDescriptor;
import org.openstreetmap.atlas.geography.atlas.change.description.descriptors.GeometryChangeDescriptor;
import org.openstreetmap.atlas.geography.atlas.change.description.descriptors.TagChangeDescriptor;
import org.openstreetmap.atlas.geography.atlas.complete.CompleteEntity;
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.geography.atlas.items.ItemType;

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

        if (beforeEntity.getGeometry() != null && afterEntity.getGeometry() != null)
        {
            final List<Location> beforeGeometry = new ArrayList<>();
            beforeEntity.getGeometry().forEach(beforeGeometry::add);
            final List<Location> afterGeometry = new ArrayList<>();
            afterEntity.getGeometry().forEach(afterGeometry::add);
            descriptors.add(new GeometryChangeDescriptor(ChangeDescriptorType.UPDATE,
                    beforeGeometry, afterGeometry));
        }

        return descriptors;
    }

    private List<ChangeDescriptor> generateTagDescriptors()
    {
        final List<ChangeDescriptor> descriptors = new ArrayList<>();

        final Map<String, String> beforeTags = this.beforeView.getTags();
        final Map<String, String> afterTags = this.afterView.getTags();

        /*
         * If the afterView tags were null, then we know that the tags were not updated. We can just
         * return nothing.
         */
        if (this.afterView.getTags() == null)
        {
            return descriptors;
        }

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
