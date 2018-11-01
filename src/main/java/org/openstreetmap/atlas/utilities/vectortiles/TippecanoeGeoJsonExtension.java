package org.openstreetmap.atlas.utilities.vectortiles;

import com.google.gson.JsonObject;

/**
 * TippecanoeGeoJsonExtension fills in a JsonObject called "tippecanoe" to your GeoJSON feature.
 * Here you can specify things like minzoom and layer name for the given feature.
 * https://github.com/mapbox/tippecanoe/blob/master/README.md#geojson-extension
 *
 * @author hallahan
 */
class TippecanoeGeoJsonExtension
{
    private static final int DEFAULT_MINIMUM_ZOOM = 14;

    private final JsonObject json = new JsonObject();

    TippecanoeGeoJsonExtension()
    {
        minimumZoom(DEFAULT_MINIMUM_ZOOM);
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

    void minimumZoom(final int minimumZoom)
    {
        json.addProperty("minzoom", minimumZoom);
    }

}
