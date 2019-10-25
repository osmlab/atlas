package org.openstreetmap.atlas.geography.geojson.parser.domain.bbox;

import java.io.Serializable;
import java.util.List;

/**
 * @author Yazad Khambata
 */
public interface Bbox extends Serializable {

    Dimensions applicableDimensions();

    List<Double> toList();
}
