package org.openstreetmap.atlas.geography.geojson.parser.domain.feature;

import java.util.Map;

import org.openstreetmap.atlas.geography.geojson.parser.domain.base.AbstractGeoJsonItem;
import org.openstreetmap.atlas.geography.geojson.parser.domain.foreign.ForeignFields;

/**
 * @author Yazad Khambata
 */
public abstract class AbstractFeature extends AbstractGeoJsonItem
{
    AbstractFeature(final Map<String, Object> map, final ForeignFields foreignFields)
    {
        super(map, foreignFields);
    }
}
