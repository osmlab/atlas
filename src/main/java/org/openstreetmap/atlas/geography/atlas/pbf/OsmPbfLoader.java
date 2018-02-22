package org.openstreetmap.atlas.geography.atlas.pbf;

import java.util.function.Supplier;

import org.openstreetmap.atlas.geography.MultiPolygon;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.AtlasMetaData;
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.geography.atlas.packed.PackedAtlasBuilder;
import org.openstreetmap.atlas.streaming.Streams;
import org.openstreetmap.atlas.streaming.resource.Resource;
import org.openstreetmap.atlas.streaming.resource.WritableResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An {@link OsmPbfLoader} loads from one OSM protobuf file with a polygon or bounding box and
 * creates an Atlas. You can choose to do country slicing and way section or not with a boolean
 * switch 'slice'. The slicing operation will create new identifier for every {@link AtlasEntity}.
 *
 * @author tony
 * @author matthieun
 */
public class OsmPbfLoader
{
    private static final Logger logger = LoggerFactory.getLogger(OsmPbfLoader.class);
    private final PackedAtlasBuilder builder = new PackedAtlasBuilder();
    private final OsmPbfProcessor processor;
    private final Supplier<CloseableOsmosisReader> osmosisReaderSupplier;
    private CloseableOsmosisReader reader;
    private Atlas atlas;
    private AtlasMetaData metaData = new AtlasMetaData();
    private final AtlasLoadingOption atlasLoadingOption;

    public OsmPbfLoader(final Resource resource)
    {
        this(resource, MultiPolygon.MAXIMUM, AtlasLoadingOption.createOptionWithNoSlicing());
    }

    public OsmPbfLoader(final Resource resource, final AtlasLoadingOption option)
    {
        this(resource, MultiPolygon.MAXIMUM, option);
    }

    public OsmPbfLoader(final Resource resource, final MultiPolygon polygon)
    {
        this(resource, polygon, AtlasLoadingOption.createOptionWithNoSlicing());
    }

    public OsmPbfLoader(final Resource resource, final MultiPolygon polygon,
            final AtlasLoadingOption loadingOption)
    {
        this(() -> new CloseableOsmosisReader(resource.read()), polygon, loadingOption);
    }

    protected OsmPbfLoader(final Supplier<CloseableOsmosisReader> osmosisReaderSupplier,
            final MultiPolygon polygon, final AtlasLoadingOption loadingOption)
    {
        this.osmosisReaderSupplier = osmosisReaderSupplier;
        this.processor = new OsmPbfProcessor(this.builder, loadingOption, polygon);
        this.atlasLoadingOption = loadingOption;
    }

    public Atlas read()
    {
        if (this.atlas == null)
        {
            // First pass, store location for node in memory and get Atlas item number
            makeNewReader();
            this.reader.run();

            // Second pass, create a new reader with same processor to build atlas
            makeNewReader();
            this.reader.run();

            // Build atlas
            logger.info("Finished reading PBF, now retrieve atlas");
            this.metaData.getTags().put(AtlasMetaData.EDGE_CONFIGURATION,
                    this.atlasLoadingOption.getEdgeFilter().toString());
            this.metaData.getTags().put(AtlasMetaData.WAY_SECTIONING_CONFIGURATION,
                    this.atlasLoadingOption.getWaySectionFilter().toString());
            this.metaData.getTags().put(AtlasMetaData.OSM_PBF_NODE_CONFIGURATION,
                    this.atlasLoadingOption.getOsmPbfNodeFilter().toString());
            this.metaData.getTags().put(AtlasMetaData.OSM_PBF_WAY_CONFIGURATION,
                    this.atlasLoadingOption.getOsmPbfWayFilter().toString());
            this.metaData.getTags().put(AtlasMetaData.OSM_PBF_RELATION_CONFIGURATION,
                    this.atlasLoadingOption.getOsmPbfRelationFilter().toString());
            this.builder.setMetaData(this.metaData);
            this.atlas = this.builder.get();
            if (this.atlas != null)
            {
                logger.info("Finished building atlas {}", this.atlas.getName());
            }
            else
            {
                logger.info("No Atlas (empty) for shard {}",
                        this.metaData.getShardName().orElse("unknown"));
            }
        }
        this.closeReader();
        return this.atlas;
    }

    public void saveAsGeojson(final WritableResource resource)
    {
        logger.info("Saving geojson file");
        read().saveAsGeoJson(resource);
    }

    public void saveAtlas(final WritableResource resource)
    {
        logger.info("Saving atlas file");
        read().save(resource);
    }

    public OsmPbfLoader withMetaData(final AtlasMetaData metaData)
    {
        this.metaData = metaData;
        return this;
    }

    private void closeReader()
    {
        if (this.reader != null)
        {
            Streams.close(this.reader);
        }
    }

    private void makeNewReader()
    {
        closeReader();
        this.reader = this.osmosisReaderSupplier.get();
        // set processor which define how to process OSM entities
        this.reader.setSink(this.processor);
    }
}
