package org.openstreetmap.atlas.geography.geojson.concrete.feature;

import org.openstreetmap.atlas.geography.geojson.concrete.geometry.GeometryWithCoordinateSupport;

import java.util.Map;

/**
 * @author Yazad Khambata
 */
public class Feature extends AbstractFeature {
    private GeometryWithCoordinateSupport geometry;
    private Map<String, Object> properties; //Foreign Members

}
