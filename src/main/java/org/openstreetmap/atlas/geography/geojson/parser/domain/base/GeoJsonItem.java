package org.openstreetmap.atlas.geography.geojson.parser.domain.base;

import org.openstreetmap.atlas.geography.geojson.parser.domain.bbox.Bbox;
import org.openstreetmap.atlas.geography.geojson.parser.domain.foreign.SupportsForeigners;
import org.openstreetmap.atlas.geography.geojson.parser.domain.base.type.Type;

import java.io.Serializable;

/**
 * @author Yazad Khambata
 */
public interface GeoJsonItem extends SupportsForeigners, Serializable {
    Type getType();
    Bbox getBbox();
}
