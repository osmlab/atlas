package org.openstreetmap.atlas.geography.atlas.change;

import org.openstreetmap.atlas.geography.atlas.items.ItemType;

/**
 * Defines the guard-rails that guides the groups of {@link FeatureChange}s that can be merged
 * together in a {@link Change}.
 *
 * @author Yazad Khambata
 */
class FeatureChangeMergeGroup
{
    private final ItemType itemType;
    private final Long identifier;
    private final ChangeType changeType;

    FeatureChangeMergeGroup(final ItemType itemType, final Long identifier,
            final ChangeType changeType)
    {
        super();
        this.itemType = itemType;
        this.identifier = identifier;
        this.changeType = changeType;
    }

    static FeatureChangeMergeGroup from(final FeatureChange featureChange)
    {
        return new FeatureChangeMergeGroup(featureChange.getItemType(),
                featureChange.getIdentifier(), featureChange.getChangeType());
    }

    public ItemType getItemType()
    {
        return itemType;
    }

    public Long getIdentifier()
    {
        return identifier;
    }

    public ChangeType getChangeType()
    {
        return changeType;
    }
}
