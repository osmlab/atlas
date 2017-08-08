package org.openstreetmap.atlas.streaming.readers.json.serializers;

import org.openstreetmap.atlas.geography.Polygon;

import com.google.gson.JsonSerializer;

/**
 * {@link JsonSerializer} for a {@link Polygon}
 *
 * @author matthieun
 */
public class PolygonSerializer extends MultiLocationSerializer<Polygon>
{
    @Override
    protected String getType()
    {
        return "Polygon";
    }
}
