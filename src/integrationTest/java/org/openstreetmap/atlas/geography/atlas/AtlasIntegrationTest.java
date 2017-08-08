package org.openstreetmap.atlas.geography.atlas;

import java.io.InputStream;
import java.util.function.Supplier;

import org.openstreetmap.atlas.geography.MultiPolygon;
import org.openstreetmap.atlas.geography.Polygon;
import org.openstreetmap.atlas.geography.atlas.builder.text.TextAtlasBuilder;
import org.openstreetmap.atlas.geography.atlas.pbf.AtlasLoadingOption;
import org.openstreetmap.atlas.geography.atlas.pbf.OsmPbfLoader;
import org.openstreetmap.atlas.geography.atlas.pbf.OsmPbfLoaderIntegrationTest;
import org.openstreetmap.atlas.streaming.compression.Decompressor;
import org.openstreetmap.atlas.streaming.resource.File;
import org.openstreetmap.atlas.streaming.resource.InputStreamResource;
import org.openstreetmap.atlas.utilities.time.Time;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author tony
 */
public class AtlasIntegrationTest
{
    private static final Logger logger = LoggerFactory.getLogger(AtlasIntegrationTest.class);

    public static Atlas loadCuba()
    {
        final Time start = Time.now();
        final Supplier<InputStream> supplier = () -> AtlasIntegrationTest.class
                .getResourceAsStream("CUB_7-37-56.txt.gz");
        final InputStreamResource resource = new InputStreamResource(supplier);
        resource.setDecompressor(Decompressor.GZIP);
        final Atlas result = new TextAtlasBuilder().read(resource);
        logger.info("Loaded a Cuba slice in {}", start.elapsedSince());
        return result;
    }

    protected Atlas loadBahamas(final Polygon polygon)
    {
        final String path = OsmPbfLoaderIntegrationTest.class.getResource("BHS_07082015.pbf")
                .getPath();
        final OsmPbfLoader loader = new OsmPbfLoader(new File(path),
                MultiPolygon.forPolygon(polygon), AtlasLoadingOption.createOptionWithNoSlicing()
                        .setLoadWaysSpanningCountryBoundaries(false));
        return loader.read();
    }

    protected Atlas loadBelizeRaw(final Polygon polygon,
            final AtlasLoadingOption atlasLoadingOption)
    {
        final String path = OsmPbfLoaderIntegrationTest.class
                .getResource("BLZ_raw_08242015.osm.pbf").getPath();
        final OsmPbfLoader loader = new OsmPbfLoader(new File(path),
                MultiPolygon.forPolygon(polygon), atlasLoadingOption);
        return loader.read();
    }
}
