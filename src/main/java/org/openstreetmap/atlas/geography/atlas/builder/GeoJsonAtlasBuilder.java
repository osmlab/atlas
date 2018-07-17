package org.openstreetmap.atlas.geography.atlas.builder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.Polygon;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.packed.PackedAtlasBuilder;
import org.openstreetmap.atlas.streaming.readers.GeoJsonReader;
import org.openstreetmap.atlas.streaming.resource.Resource;
import org.openstreetmap.atlas.tags.OneWayTag;
import org.openstreetmap.atlas.utilities.collections.StringList;

import com.google.gson.JsonElement;

/**
 * Create an Atlas from an non-way-sectioned overpass-turbo GeoJson resource.
 *
 * @author matthieun
 */
public class GeoJsonAtlasBuilder
{
    /**
     * @author matthieun
     */
    private static class GeoJsonEdge
    {
        private final long identifier;
        private final Map<String, String> tags;
        private final PolyLine polyLine;

        GeoJsonEdge(final long identifier, final Map<String, String> tags, final PolyLine polyLine)
        {
            this.identifier = identifier;
            this.tags = tags;
            this.polyLine = polyLine;
        }

        public long getIdentifier()
        {
            return this.identifier;
        }

        public PolyLine getPolyLine()
        {
            return this.polyLine;
        }

        public Map<String, String> getTags()
        {
            return this.tags;
        }
    }

    public Atlas create(final Resource geoJson)
    {
        GeoJsonReader reader = new GeoJsonReader(geoJson);
        final AtlasBuilder builder = new PackedAtlasBuilder();
        final List<GeoJsonEdge> edges = new ArrayList<>();
        long nodeIdentifier = 0L;
        reader.forEachRemaining(item ->
        {
            if (item.getItem() instanceof PolyLine && !(item.getItem() instanceof Polygon))
            {
                // We have an edge
                Long identifier = null;
                final Set<Map.Entry<String, JsonElement>> jsonTags = item.getProperties()
                        .entrySet();
                final Map<String, String> tags = new HashMap<>();
                for (final Map.Entry<String, JsonElement> entry : jsonTags)
                {
                    final String key = entry.getKey();
                    final String value = entry.getValue().getAsString();
                    if (key.contains("@id"))
                    {
                        identifier = Long.valueOf(StringList.split(value, "/").get(1));
                    }
                    else
                    {
                        tags.put(key, value);
                    }
                }
                if (!tags.containsKey("highway"))
                {
                    // it was not an edge after all
                    return;
                }
                edges.add(new GeoJsonEdge(identifier, tags, (PolyLine) item.getItem()));
            }
        });
        final Set<Location> locations = new HashSet<>();
        for (final GeoJsonEdge edge : edges)
        {
            locations.add(edge.getPolyLine().first());
            locations.add(edge.getPolyLine().last());
        }
        for (final Location location : locations)
        {
            // Node
            builder.addNode(nodeIdentifier++, location, new HashMap<String, String>());
        }
        for (final GeoJsonEdge edge : edges)
        {
            // Edge

            if (edge.getTags().containsKey(OneWayTag.KEY)
                    && !OneWayTag.NO.name().equalsIgnoreCase(edge.getTags().get(OneWayTag.KEY)))
            {
                final String onewayTag = edge.getTags().get(OneWayTag.KEY);
                if (OneWayTag.YES.name().equalsIgnoreCase(onewayTag) || "1".equals(onewayTag))
                {
                    builder.addEdge(edge.getIdentifier(), edge.getPolyLine(), edge.getTags());
                }
                else if ("-1".equals(onewayTag))
                {
                    builder.addEdge(edge.getIdentifier(), edge.getPolyLine().reversed(),
                            edge.getTags());
                }
            }
            else
            {
                builder.addEdge(edge.getIdentifier(), edge.getPolyLine(), edge.getTags());
                builder.addEdge(-edge.getIdentifier(), edge.getPolyLine().reversed(),
                        edge.getTags());
            }
        }
        reader = new GeoJsonReader(geoJson);
        reader.forEachRemaining(item ->
        {
            if (item.getItem() instanceof Polygon)
            {
                // Area
                Long identifier = null;
                final Set<Map.Entry<String, JsonElement>> jsonTags = item.getProperties()
                        .entrySet();
                final Map<String, String> tags = new HashMap<>();
                for (final Map.Entry<String, JsonElement> entry : jsonTags)
                {
                    final String key = entry.getKey();
                    final String value = entry.getValue().getAsString();
                    if (key.contains("@id"))
                    {
                        identifier = Long.valueOf(StringList.split(value, "/").get(1));
                    }
                    else
                    {
                        tags.put(key, value);
                    }
                }
                builder.addArea(identifier, (Polygon) item.getItem(), tags);
            }
            if (item.getItem() instanceof PolyLine && !(item.getItem() instanceof Polygon))
            {
                // Line
                Long identifier = null;
                final Set<Map.Entry<String, JsonElement>> jsonTags = item.getProperties()
                        .entrySet();
                final Map<String, String> tags = new HashMap<>();
                for (final Map.Entry<String, JsonElement> entry : jsonTags)
                {
                    final String key = entry.getKey();
                    final String value = entry.getValue().getAsString();
                    if (key.contains("@id"))
                    {
                        identifier = Long.valueOf(StringList.split(value, "/").get(1));
                    }
                    else
                    {
                        tags.put(key, value);
                    }
                }
                if (tags.containsKey("highway"))
                {
                    // it was an edge after all
                    return;
                }
                builder.addLine(identifier, (PolyLine) item.getItem(), tags);
            }
            if (item.getItem() instanceof Location)
            {
                // Area
                Long identifier = null;
                final Set<Map.Entry<String, JsonElement>> jsonTags = item.getProperties()
                        .entrySet();
                final Map<String, String> tags = new HashMap<>();
                for (final Map.Entry<String, JsonElement> entry : jsonTags)
                {
                    final String key = entry.getKey();
                    final String value = entry.getValue().getAsString();
                    if (key.contains("@id"))
                    {
                        identifier = Long.valueOf(StringList.split(value, "/").get(1));
                    }
                    else
                    {
                        tags.put(key, value);
                    }
                }
                try
                {
                    builder.addPoint(identifier, (Location) item.getItem(), tags);
                }
                catch (final CoreException e)
                {
                    if (!tags.isEmpty())
                    {
                        throw e;
                    }
                    // ignore. It is a duplicated node in GeoJson without any tags
                }
            }
        });
        return builder.get();
    }
}
