package org.openstreetmap.atlas.geography.geojson;

import com.google.gson.JsonObject;

/**
 * From the spec https://tools.ietf.org/html/rfc7946#section-3.1
 * 
 * <pre>
 *      A Geometry object represents points, curves, and surfaces in
 *    coordinate space.  Every Geometry object is a GeoJSON object no
 *    matter where it occurs in a GeoJSON text.
 * </pre>
 * 
 * This interface is for all objects with a geojson Geometry object representation. This encompasses
 * all the Geojson Geometry types in {@link GeoJsonType#isGeometryType(GeoJsonType)}.
 *
 * @author jklamer
 */
public interface GeoJsonGeometry extends GeoJson
{
    @Override
    default JsonObject asGeoJson()
    {
        return this.asGeoJsonGeometry();
    }

    JsonObject asGeoJsonGeometry();
}
