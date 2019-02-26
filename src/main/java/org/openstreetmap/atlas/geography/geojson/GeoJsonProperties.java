package org.openstreetmap.atlas.geography.geojson;

import com.google.gson.JsonObject;

/**
 * Interface for all interfaces that have geojson properties. Namely {@link GeoJsonFeature} and
 * {@link GeoJsonFeatureCollection}
 *
 * @author jklamer
 */
public interface GeoJsonProperties
{
    JsonObject getGeoJsonProperties();
}
