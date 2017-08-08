package org.openstreetmap.atlas.streaming.readers.json.serializers;

import org.openstreetmap.atlas.geography.PolyLine;

import com.google.gson.JsonSerializer;

/**
 * {@link JsonSerializer} for a {@link PolyLine}
 *
 * @author matthieun
 */
public class PolyLineSerializer extends MultiLocationSerializer<PolyLine>
{
    @Override
    protected String getType()
    {
        return "LineString";
    }
}
