package org.openstreetmap.atlas.geography.atlas.change.rule;

import java.io.Serializable;

import org.openstreetmap.atlas.geography.Located;
import org.openstreetmap.atlas.geography.Rectangle;
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.geography.atlas.items.ItemType;

/**
 * Single feature change, does not include any consistency checks.
 *
 * @author matthieun
 */
public class FeatureChange implements Located, Serializable
{
    private static final long serialVersionUID = 9172045162819925515L;

    private final ChangeType changeType;
    private final AtlasEntity reference;

    public FeatureChange(final ChangeType changeType, final AtlasEntity reference)
    {
        this.changeType = changeType;
        this.reference = reference;
    }

    @Override
    public Rectangle bounds()
    {
        return getReference().bounds();
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
}
