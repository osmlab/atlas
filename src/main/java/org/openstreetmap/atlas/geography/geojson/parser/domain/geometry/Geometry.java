package org.openstreetmap.atlas.geography.geojson.parser.domain.geometry;

import org.openstreetmap.atlas.geography.geojson.parser.domain.base.GeoJsonItem;
import org.openstreetmap.atlas.geography.geojson.parser.domain.base.type.GeometryType;

/**
 * @author Yazad Khambata
 */
public interface Geometry extends GeoJsonItem
{
    GeometryType getGeometryType();
    
    String getTypeValue();
}
