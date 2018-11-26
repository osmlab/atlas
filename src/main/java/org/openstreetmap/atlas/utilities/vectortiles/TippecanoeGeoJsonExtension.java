package org.openstreetmap.atlas.utilities.vectortiles;

import com.google.gson.JsonObject;

/**
 * TippecanoeGeoJsonExtension fills in a JsonObject called "tippecanoe" to your GeoJSON feature.
 * Here you can specify things like minzoom and layer name for the given feature.
 * https://github.com/mapbox/tippecanoe/blob/739445cb6cd9654bdbc95046ee5e4a6b55cd2ff9/README.md#geojson-extension
 *
 * @author hallahan
 */
public class TippecanoeGeoJsonExtension
{
    private static final int DEFAULT_MINIMUM_ZOOM = 14;

    private final JsonObject json = new JsonObject();

    public TippecanoeGeoJsonExtension()
    {
        minimumZoom(DEFAULT_MINIMUM_ZOOM);
    }

    public TippecanoeGeoJsonExtension addTo(final JsonObject feature)
    {
        feature.add("tippecanoe", json);
        return this;
    }

    public void layer(final String layer)
    {
        json.addProperty("layer", layer);
    }

    public void minimumZoom(final int minimumZoom)
    {
        json.addProperty("minzoom", minimumZoom);
    }
}
