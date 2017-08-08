package org.openstreetmap.atlas.geography.geojson;

import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.MultiPolygon;
import org.openstreetmap.atlas.geography.Polygon;
import org.openstreetmap.atlas.streaming.resource.WritableResource;
import org.openstreetmap.atlas.streaming.writers.JsonWriter;
import org.openstreetmap.atlas.utilities.collections.Iterables;
import org.openstreetmap.atlas.utilities.collections.Maps;
import org.openstreetmap.atlas.utilities.collections.MultiIterable;

/**
 * Save some geometry items to a resource, as GeoJson
 *
 * @author matthieun
 */
public final class GeoJsonSaver
{
    public static void save(final Iterable<? extends Iterable<Location>> geometries,
            final WritableResource destination)
    {
        final GeoJsonObject object = new GeoJsonBuilder().create(Iterables.translate(geometries,
                polyLine -> new GeoJsonBuilder.LocationIterableProperties(polyLine,
                        Maps.hashMap())));
        save(object, destination);
    }

    public static void saveMultipolygon(final Iterable<MultiPolygon> geometries,
            final WritableResource destination)
    {
        final Iterable<Polygon> outers = Iterables.translateMulti(geometries,
                multiPolygon -> multiPolygon.outers());
        final Iterable<Polygon> inners = Iterables.translateMulti(geometries,
                multiPolygon -> multiPolygon.inners());
        final Iterable<Polygon> multi = new MultiIterable<>(outers, inners);
        save(multi, destination);
    }

    private static void save(final GeoJsonObject object, final WritableResource destination)
    {
        final JsonWriter writer = new JsonWriter(destination);
        writer.write(object.jsonObject());
        writer.close();
    }

    private GeoJsonSaver()
    {
    }
}
