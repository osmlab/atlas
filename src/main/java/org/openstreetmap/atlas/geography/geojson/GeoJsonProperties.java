package org.openstreetmap.atlas.geography.geojson;

import com.google.gson.JsonObject;

/**
 * Interface for all interfaces that have geojson properties. Namely {@link GeoJsonFeature} and
 * {@link GeoJsonFeatureCollection} From the spec : https://tools.ietf.org/html/rfc7946#section-3.2
 * 
 * <pre>
 *      o  A Feature object has a member with the name "properties".  The
 *       value of the properties member is an object (any JSON object or a
 *       JSON null value).
 * </pre>
 *
 * @author jklamer
 */
public interface GeoJsonProperties
{
    /**
     * This returns the geojson properties associate with the
     * 
     * @return a JsonObject for the "properties" field
     */
    JsonObject getGeoJsonProperties();
}
