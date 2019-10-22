package org.openstreetmap.atlas.geography.geojson.concrete;

import org.openstreetmap.atlas.geography.geojson.concrete.bbox.Bbox;
import org.openstreetmap.atlas.geography.geojson.concrete.foreign.SupportsForeigners;
import org.openstreetmap.atlas.geography.geojson.concrete.type.Type;

import java.io.Serializable;
import java.util.Map;

/**
 * @author Yazad Khambata
 */
public interface GeoJsonItem extends SupportsForeigners, Serializable {
    Type getType();
    Bbox getBbox();
}
