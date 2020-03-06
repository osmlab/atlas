package org.openstreetmap.atlas.geography.geojson;

import com.google.gson.JsonObject;

/**
 * For all classes with a GeoJson representation. From the spec
 * https://tools.ietf.org/html/rfc7946#section-3
 * 
 * <pre>
 *      A GeoJSON object represents a Geometry, Feature, or collection of
 *    Features.
 *
 *    o  A GeoJSON object is a JSON object.
 *
 *    o  A GeoJSON object has a member with the name "type".  The value of
 *       the member MUST be one of the GeoJSON types.
 *
 *    o  A GeoJSON object MAY have a "bbox" member, the value of which MUST
 *       be a bounding box array (see Section 5).
 *
 *    o  A GeoJSON object MAY have other members (see Section 6).
 * </pre>
 * 
 * This interface is for all classes with a geojson object representation.
 *
 * @author jklamer
 */
public interface GeoJson
{
    JsonObject asGeoJson();

    GeoJsonType getGeoJsonType();
}
