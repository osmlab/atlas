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
 * This interface is for all objects with a geojson geometry representation. <b>This breaks from the
 * spec in that this will not include Geometry collections even though that is technically a
 * geometry object. This is because the spec also calls for no nested GeoJsonGeometryCollections. We
 * can enforce that rule and design better by excluding GeometryCollection here.</b>
 *
 * @author jklamer
 */
public interface GeoJsonGeometry extends GeoJson
{
    JsonObject asGeoJsonGeometry();
}
