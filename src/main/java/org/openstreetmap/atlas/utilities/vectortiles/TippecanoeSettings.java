package org.openstreetmap.atlas.utilities.vectortiles;

import java.util.function.BiConsumer;

import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;

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

        final String atlasType = atlasEntity.getType().name();
        tippecanoe.layer(atlasType);

        tippecanoe.minimumZoom(MinimumZoom.INSTANCE.get(atlasEntity.getTags()));
    };

}
