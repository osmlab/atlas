package org.openstreetmap.atlas.streaming.resource;

import java.util.Iterator;

import org.openstreetmap.atlas.streaming.readers.GeoJsonReader;
import org.openstreetmap.atlas.streaming.readers.json.serializers.PropertiesLocated;

/**
 * File that contains GeoJson data.
 *
 * @author matthieun
 */
public class GeoJsonFile extends File implements Iterable<PropertiesLocated>
{
    public GeoJsonFile(final String path)
    {
        super(path);
    }

    @Override
    public Iterator<PropertiesLocated> iterator()
    {
        return new GeoJsonReader(this);
    }
}
