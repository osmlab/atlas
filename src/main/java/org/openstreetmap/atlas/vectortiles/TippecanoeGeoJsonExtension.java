package org.openstreetmap.atlas.vectortiles;

import com.google.gson.JsonObject;

/**
 * TippecanoeGeoJsonExtension fills in a JsonObject called "tippecanoe" to your GeoJSON feature.
 * Here you can specify things like minzoom and layer name for the given feature.
 *
 * @author hallahan
 */
class TippecanoeGeoJsonExtension
{
    private static final int DEFAULT_MIN_ZOOM = 14;

    private final JsonObject json = new JsonObject();

    TippecanoeGeoJsonExtension()
    {
        minZoom(DEFAULT_MIN_ZOOM);
    }

    TippecanoeGeoJsonExtension addTo(final JsonObject feature)
    {
        feature.add("tippecanoe", json);
        return this;
    }

    void layer(final String layer)
    {
        json.addProperty("layer", layer);
    }

    void minZoom(final int minZoom)
    {
        json.addProperty("minzoom", minZoom);
    }

}
