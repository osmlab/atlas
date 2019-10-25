package org.openstreetmap.atlas.geography.geojson.parser.domain.base;

import java.io.Serializable;
import java.util.Set;

import org.openstreetmap.atlas.geography.geojson.parser.domain.base.type.Type;
import org.openstreetmap.atlas.geography.geojson.parser.domain.bbox.Bbox;
import org.openstreetmap.atlas.geography.geojson.parser.domain.foreign.SupportsForeigners;
import org.openstreetmap.atlas.geography.geojson.parser.domain.properties.Properties;

/**
 * @author Yazad Khambata
 */
public interface GeoJsonItem extends SupportsForeigners, Serializable
{
    Type getType();

    Bbox getBbox();

    Properties getProperties();
}
