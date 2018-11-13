package org.openstreetmap.atlas.utilities.vectortiles;

import java.util.function.BiConsumer;

import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.geography.atlas.items.ItemType;
import org.openstreetmap.atlas.tags.RelationTypeTag;
import org.openstreetmap.atlas.tags.annotations.validation.Validators;

import com.google.gson.JsonObject;

/**
 * This is where you configure the tippecanoe properties that will be in the generated
 * line-delimited GeoJSON. You also set the arguments to drive tippecanoe here.
 *
 * @author hallahan
 */
final class TippecanoeSettings
{
    private TippecanoeSettings()
    {
        // Utility Class
    }

    public static final DefaultArtifactVersion MIN_VERSION = new DefaultArtifactVersion("1.32.1");
    public static final String GEOJSON = "EVERYTHING.geojson";

    public static final String[] ARGS = new String[] { "-Z6", "-z14", "--generate-ids",
            "--read-parallel", "--no-tile-size-limit", "--no-feature-limit" };

    public static final BiConsumer<AtlasEntity, JsonObject> JSON_MUTATOR = (atlasEntity, feature) ->
    {
        final TippecanoeGeoJsonExtension tippecanoe = new TippecanoeGeoJsonExtension()
                .addTo(feature);

        final ItemType type = atlasEntity.getType();
        final String typeName = type.name();

        // It's good to have multipolygon relations in their own layer, because all other relations
        // are just bounding boxes. Multipolygons are multipolygons. So, we'll probably want to
        // render the multipolygons, but we can turn off rendering other relations, that really are
        // represented as a bunch of bounding boxes...
        if (ItemType.RELATION.equals(type) && Validators.isOfType(atlasEntity,
                RelationTypeTag.class, RelationTypeTag.MULTIPOLYGON))
        {
            tippecanoe.layer("MULTIPOLYGON");
        }
        else
        {
            tippecanoe.layer(typeName);
        }

        tippecanoe.minimumZoom(MinimumZoom.INSTANCE.get(atlasEntity.getTags()));
    };

}
