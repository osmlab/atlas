package org.openstreetmap.atlas.geography.atlas.items.complex.boundaries.converters;

import java.util.Map;

import org.openstreetmap.atlas.geography.atlas.items.complex.boundaries.ComplexBoundary;
import org.openstreetmap.atlas.geography.geojson.GeoJsonBuilder;
import org.openstreetmap.atlas.geography.geojson.GeoJsonBuilder.LocationIterableProperties;
import org.openstreetmap.atlas.streaming.resource.WritableResource;
import org.openstreetmap.atlas.streaming.writers.JsonWriter;
import org.openstreetmap.atlas.utilities.collections.Iterables;

/**
 * Write a set of complex boundaries to a geojson resource.
 *
 * @author matthieun
 */
public final class ComplexBoundaryIterableToGeoJsonWriter
{
    public static void saveAsGeojson(final Iterable<ComplexBoundary> complexBoundaries,
            final WritableResource output)
    {
        try (JsonWriter writer = new JsonWriter(output))
        {
            final Iterable<LocationIterableProperties> geojsonObjects = Iterables
                    .stream(complexBoundaries).flatMap(boundary ->
                    {
                        final Map<String, String> tags = boundary.getSource().getTags();
                        return Iterables
                                .stream(boundary.getOutline().asLocationIterableProperties())
                                .map(locationIterableProperties ->
                                {
                                    locationIterableProperties.getProperties().putAll(tags);
                                    return locationIterableProperties;
                                });
                    });
            writer.write(new GeoJsonBuilder().create(geojsonObjects).jsonObject());
        }
    }

    private ComplexBoundaryIterableToGeoJsonWriter()
    {
    }
}
