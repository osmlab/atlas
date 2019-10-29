package org.openstreetmap.atlas.geography.geojson.parser.domain.base;

import java.io.Serializable;

import org.openstreetmap.atlas.geography.geojson.parser.domain.base.type.Type;
import org.openstreetmap.atlas.geography.geojson.parser.domain.bbox.Bbox;
import org.openstreetmap.atlas.geography.geojson.parser.domain.foreign.SupportsForeigners;
import org.openstreetmap.atlas.geography.geojson.parser.domain.properties.Properties;

/**
 * @author Yazad Khambata
 */
public interface GeoJsonItem extends SupportsForeigners, Serializable
{
    Bbox getBbox();

    Properties getProperties();

    Type getType();
}
