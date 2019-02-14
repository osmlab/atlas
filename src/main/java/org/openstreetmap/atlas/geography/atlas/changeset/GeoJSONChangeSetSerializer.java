package org.openstreetmap.atlas.geography.atlas.changeset;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.openstreetmap.atlas.geography.geojson.GeoJsonBuilder;
import org.openstreetmap.atlas.geography.geojson.GeoJsonObject;
import org.openstreetmap.atlas.streaming.resource.WritableResource;

/**
 * Serializes {@link ChangeSet} objects and writes them into {@link OutputStream}s in geojson
 * format.
 *
 * @author Yiqing Jin
 * @deprecated - see new API under org.openstreetmap.atlas.geography.atlas.change package.
 */
@Deprecated
public class GeoJSONChangeSetSerializer implements ChangeSetSerializer // NOSONAR
{
    private final WritableResource resource;

    public GeoJSONChangeSetSerializer(final WritableResource resourceToWriteInto)
    {
        this.resource = resourceToWriteInto;
    }

    @Override
    public void accept(final ChangeSet changeSet)
    {
        toGeoJson(changeSet).save(this.resource);
    }

    @Override
    public void close() throws Exception
    {
        // Do nothing as close is handled in GeoJsonObject.save method
    }

    public GeoJsonObject toGeoJson(final ChangeSet changeSet)
    {
        final List<GeoJsonBuilder.LocationIterableProperties> collection = new ArrayList<>();
        changeSet.iterator().forEachRemaining(changeItem ->
        {
            if (changeItem.getGeometry() == null)
            {
                return;
            }
            changeItem.getTags().put("action", changeItem.getAction().name());
            changeItem.getTags().put("id", String.valueOf(changeItem.getIdentifier()));
            collection.add(new GeoJsonBuilder.LocationIterableProperties(changeItem.getGeometry(),
                    changeItem.getTags()));
        });
        final GeoJsonBuilder builder = new GeoJsonBuilder();

        return builder.create(collection);
    }
}
