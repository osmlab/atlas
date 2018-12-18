package org.openstreetmap.atlas.geography;

import com.google.gson.JsonObject;

/**
 * @author matthieun
 */
public interface GeojsonPrintable
{
    JsonObject asGeoJsonGeometry();
}
