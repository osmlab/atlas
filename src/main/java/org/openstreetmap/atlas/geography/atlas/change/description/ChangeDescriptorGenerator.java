package org.openstreetmap.atlas.geography.atlas.change.description;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.atlas.change.description.descriptors.ChangeDescriptor;
import org.openstreetmap.atlas.geography.atlas.change.description.descriptors.TagChangeDescriptor;
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;

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

        if (this.beforeView == null)
        {
            throw new CoreException("TODO handle this");
        }

        descriptors.addAll(generateTagDescriptors());

        return descriptors;
    }

    private List<ChangeDescriptor> generateTagDescriptors()
    {
        final List<ChangeDescriptor> descriptors = new ArrayList<>();

        final Map<String, String> beforeTags = this.beforeView.getTags();
        final Map<String, String> afterTags = this.afterView.getTags();

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
