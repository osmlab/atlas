package org.openstreetmap.atlas.geography.atlas.change;

import java.io.Serializable;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.Located;
import org.openstreetmap.atlas.geography.Rectangle;
import org.openstreetmap.atlas.geography.atlas.bloated.BloatedEntity;
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.geography.atlas.items.ItemType;

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

    private final ChangeType changeType;
    private final AtlasEntity reference;

    public FeatureChange(final ChangeType changeType, final AtlasEntity reference)
    {
        if (changeType == null)
        {
            throw new CoreException("changeType cannot be null.");
        }
        if (reference == null)
        {
            throw new CoreException("reference cannot be null.");
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
    public String toString()
    {
        return "FeatureChange [changeType=" + this.changeType + ", reference={"
                + this.reference.getType() + "," + this.reference.getIdentifier() + "}, bounds="
                + bounds() + "]";
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
