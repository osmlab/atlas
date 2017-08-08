package org.openstreetmap.atlas.geography.atlas.pbf.converters;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.atlas.items.ItemType;
import org.openstreetmap.atlas.utilities.conversion.Converter;
import org.openstreetmap.osmosis.core.domain.v0_6.EntityType;

/**
 * @author matthieun
 */
public class ItemTypeToEntityTypeConverter implements Converter<ItemType, EntityType>
{
    @Override
    public EntityType convert(final ItemType object)
    {
        switch (object)
        {
            case NODE:
                return EntityType.Node;
            case EDGE:
                return EntityType.Way;
            case AREA:
                return EntityType.Way;
            case LINE:
                return EntityType.Way;
            case POINT:
                return EntityType.Node;
            case RELATION:
                return EntityType.Relation;
            default:
                throw new CoreException("Invalid ItemType: {}", object);
        }
    }
}
