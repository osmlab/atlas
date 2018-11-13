package org.openstreetmap.atlas.geography.atlas.change.rule;

import java.io.Serializable;

import org.openstreetmap.atlas.geography.Located;
import org.openstreetmap.atlas.geography.Rectangle;
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.geography.atlas.items.ItemType;

/**
 * Single feature change, does not include any consistency checks.
 * <p>
 * To add a new, non existing feature: {@link ChangeType} is ADD, and the included reference needs
 * to contain all the information related to that new feature.
 * <p>
 * To modify an existing feature: {@link ChangeType} is ADD, and the included reference needs to
 * contain all the information related to that new feature, including the unchanged attributes as
 * well as the modified attributes. (This can make change objects very large, and might be updated
 * in the future if performance is problematic. In the mean time, it makes the code much simpler!).
 * <p>
 * To remove an existing feature: {@link ChangeType} is REMOVE. The included reference's only
 * feature that needs to match the existing feature is the identifier.
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
