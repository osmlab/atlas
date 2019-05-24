package org.openstreetmap.atlas.geography.atlas.change;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.complete.CompleteItemType;
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.geography.atlas.items.ItemType;
import org.openstreetmap.atlas.utilities.tuples.Tuple;

/**
 * Caters to use cases where {@link AtlasEntity}-ies need to be grouped by the
 * {@link AtlasEntity#getIdentifier()}. {@link AtlasEntity#getIdentifier()} could repeat across
 * different entity types, and hence combined with {@link ItemType}. This class extends
 * {@link Tuple} and adds some functionality to reduce code verbosity.
 *
 * @author Yazad Khambata
 */
public class AtlasEntityKey extends Tuple<ItemType, Long>
{
    protected AtlasEntityKey(final ItemType itemType, final Long identifier)
    {
        super(itemType, identifier);
    }

    public static AtlasEntityKey from(final ItemType itemType, final Long identifier)
    {
        return new AtlasEntityKey(itemType, identifier);
    }

    public static AtlasEntityKey from(final FeatureChange featureChange)
    {
        return from(featureChange.getItemType(), featureChange.getIdentifier());
    }

    public ItemType getItemType()
    {
        return getFirst();
    }

    public Long getIdentifier()
    {
        return getSecond();
    }

    public CompleteItemType getCompleteItemType()
    {
        return CompleteItemType.from(getItemType());
    }

    public AtlasEntity getAtlasEntity(final Atlas atlas)
    {
        return getItemType().entityForIdentifier(atlas, getIdentifier());
    }
}
