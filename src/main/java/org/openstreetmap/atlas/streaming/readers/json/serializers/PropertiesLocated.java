package org.openstreetmap.atlas.streaming.readers.json.serializers;

import org.openstreetmap.atlas.geography.Located;

import com.google.gson.JsonObject;

/**
 * Java bean containing a Located object and its properties from a GeoJson file
 *
 * @author matthieun
 */
public class PropertiesLocated
{
    private final Located item;
    private final JsonObject properties;

    public PropertiesLocated(final Located item, final JsonObject properties)
    {
        this.item = item;
        this.properties = properties;
    }

    public Located getItem()
    {
        return this.item;
    }

    public JsonObject getProperties()
    {
        return this.properties;
    }

    @Override
    public String toString()
    {
        final StringBuilder result = new StringBuilder();
        result.append(this.item.getClass().getSimpleName());
        result.append(": ");
        result.append(this.item.toString());
        result.append(" -- Properties: ");
        result.append(this.properties);
        return result.toString();
    }
}
