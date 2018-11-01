package org.openstreetmap.atlas.vectortiles;

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

    public static final String[] ARGS = new String[] { "-Z6", "-z14", "--generate-ids", "--read-parallel",
            "--no-tile-size-limit", "--no-feature-limit" };

    public static final BiConsumer<AtlasEntity, JsonObject> JSON_MUTATOR = (atlasEntity, feature) ->
    {
        final TippecanoeGeoJsonExtension tippecanoe = new TippecanoeGeoJsonExtension()
                .addTo(feature);

        final String atlasType = atlasEntity.getType().name();
        tippecanoe.layer(atlasType);

        atlasEntity.getTag("amenity").ifPresent(tag -> tippecanoe.minimumZoom(MinimumZoom.amenity(tag)));
        atlasEntity.getTag("building").ifPresent(tag -> tippecanoe.minimumZoom(MinimumZoom.building(tag)));
        atlasEntity.getTag("highway").ifPresent(tag -> tippecanoe.minimumZoom(MinimumZoom.highway(tag)));
        atlasEntity.getTag("railway").ifPresent(tag -> tippecanoe.minimumZoom(MinimumZoom.railway(tag)));
        atlasEntity.getTag("route").ifPresent(tag -> tippecanoe.minimumZoom(MinimumZoom.route(tag)));
        atlasEntity.getTag("place").ifPresent(tag -> tippecanoe.minimumZoom(MinimumZoom.place(tag)));
        atlasEntity.getTag("natural").ifPresent(tag -> tippecanoe.minimumZoom(MinimumZoom.natural(tag)));
        atlasEntity.getTag("leisure").ifPresent(tag -> tippecanoe.minimumZoom(MinimumZoom.leisure(tag)));
        atlasEntity.getTag("landuse").ifPresent(tag -> tippecanoe.minimumZoom(MinimumZoom.landuse(tag)));
        atlasEntity.getTag("waterway").ifPresent(tag -> tippecanoe.minimumZoom(MinimumZoom.waterway(tag)));

    };

}
