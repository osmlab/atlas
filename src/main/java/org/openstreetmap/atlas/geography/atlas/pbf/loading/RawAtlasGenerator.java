package org.openstreetmap.atlas.geography.atlas.pbf.loading;

import java.util.function.Supplier;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.AtlasMetaData;
import org.openstreetmap.atlas.geography.atlas.builder.AtlasSize;
import org.openstreetmap.atlas.geography.atlas.items.Line;
import org.openstreetmap.atlas.geography.atlas.items.Point;
import org.openstreetmap.atlas.geography.atlas.items.Relation;
import org.openstreetmap.atlas.geography.atlas.packed.PackedAtlasBuilder;
import org.openstreetmap.atlas.geography.atlas.pbf.AtlasLoadingOption;
import org.openstreetmap.atlas.streaming.resource.Resource;
import org.openstreetmap.atlas.streaming.resource.WritableResource;
import org.openstreetmap.atlas.utilities.time.Time;
import org.openstreetmap.osmosis.core.task.v0_6.Sink;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import crosby.binary.osmosis.OsmosisReader;

/**
 * The {@link RawAtlasGenerator} loads an OSM protobuf file and constructs a raw {@link Atlas} from
 * it. A raw {@link Atlas} will only contains Atlas {@link Point}s, {@link Line}s and
 * {@link Relation}s.
 *
 * @author mgostintsev
 */
public class RawAtlasGenerator
{
    private static final Logger logger = LoggerFactory.getLogger(RawAtlasGenerator.class);

    private OsmosisReader reader;
    private final OsmPbfReader pbfReader;
    private final OsmPbfCounter pbfCounter;
    private final PackedAtlasBuilder builder;
    private final AtlasLoadingOption atlasLoadingOption;
    private final Supplier<OsmosisReader> osmosisReaderSupplier;
    private AtlasMetaData metaData = new AtlasMetaData();

    /**
     * Constructor.
     *
     * @param resource
     *            The OSM PBF {@link Resource} to use
     */
    public RawAtlasGenerator(final Resource resource)
    {
        // TODO : Update AtlasLoadingOption to remove country-slicing/way-sectioning configurations
        // after refactor is complete.
        this(resource, AtlasLoadingOption.createOptionWithNoSlicing());
    }

    /**
     * Constructor.
     *
     * @param resource
     *            The OSM PBF {@link Resource} to use
     * @param loadingOption
     *            The {@link AtlasLoadingOption} to use
     */
    public RawAtlasGenerator(final Resource resource, final AtlasLoadingOption loadingOption)
    {
        this(() -> new OsmosisReader(resource.read()), loadingOption);
    }

    protected RawAtlasGenerator(final Supplier<OsmosisReader> osmosisReaderSupplier,
            final AtlasLoadingOption atlasLoadingOption)
    {
        this.osmosisReaderSupplier = osmosisReaderSupplier;
        this.atlasLoadingOption = atlasLoadingOption;
        this.builder = new PackedAtlasBuilder();
        this.pbfReader = new OsmPbfReader(atlasLoadingOption, this.builder);
        this.pbfCounter = new OsmPbfCounter(atlasLoadingOption);
    }

    /**
     * Loops through the PBF file once to gather the entity counts. Updates the
     * {@link AtlasMetaData} and {@link AtlasSize}, then proceeds to loop through the PBF file a
     * second time to build the raw {@link Atlas}.
     *
     * @return the raw {@link Atlas}, can be {@code null}.
     */
    public Atlas build()
    {
        // First pass -- loop through the PBF file and count the number of Points, Lines and
        // Relations in the PBF file. The counts are used to initialize the Atlas Size Estimate when
        // building the Raw Atlas. It's much faster to loop through the PBF file and count the
        // entities rather than re-size the underlying entity arrays on the fly when building the
        // Atlas.
        countOsmPbfEntities();

        // Update the metadata to reflect any configuration that was used.
        populateAtlasMetadata();

        // Use the entity counts from above to set the size estimates.
        setAtlasSizeEstimate();

        // Second pass -- loop through the PBF file again. This time, read the entities and
        // construct a raw Atlas.
        return buildRawAtlas();
    }

    /**
     * Save raw {@link Atlas} as geoJson.
     *
     * @param resource
     *            The {@link WritableResource} to save to.
     */
    public void saveAsGeojson(final WritableResource resource)
    {
        logger.info("Saving Raw Atlas as geojson");
        build().saveAsGeoJson(resource);
    }

    /**
     * Save raw {@link Atlas} as text.
     *
     * @param resource
     *            The {@link WritableResource} to save to.
     */
    public void saveAsText(final WritableResource resource)
    {
        logger.info("Saving Raw Atlas as text");
        build().saveAsText(resource);
    }

    /**
     * Save raw {@link Atlas}.
     *
     * @param resource
     *            The {@link WritableResource} to save to.
     */
    public void saveAtlas(final WritableResource resource)
    {
        logger.info("Saving Raw Atlas file");
        build().save(resource);
    }

    /**
     * Use given {@link AtlasMetaData} object
     *
     * @param metaData
     *            {@link AtlasMetaData} to use
     * @return the updated {@link RawAtlasGenerator}
     */
    public RawAtlasGenerator withMetaData(final AtlasMetaData metaData)
    {
        this.metaData = metaData;
        return this;
    }

    /**
     * Loops through the given OSM PBF file and builds the raw {@link Atlas}.
     *
     * @return the raw {@link Atlas}, possibly {@code null} if no {@link Atlas} was built.
     */
    private Atlas buildRawAtlas()
    {
        final Time parseTime = Time.now();
        connectOsmPbfToPbfConsumer(this.pbfReader);
        this.reader.run();
        logger.info("Read PBF in {}, preparing to build Raw Atlas", parseTime.elapsedSince());

        final Time buildTime = Time.now();
        final Atlas atlas = this.builder.get();
        logger.info("Built Raw Atlas in {}", buildTime.elapsedSince());

        if (atlas != null)
        {
            logger.info("Successfully built atlas {}", atlas.getName());
        }
        else
        {
            logger.info("No Atlas generated for given PBF Shard {}",
                    this.metaData.getShardName().orElse("unknown"));
        }

        return atlas;
    }

    /**
     * Connects the given {@link Sink} implementation to the PBF File.
     */
    private void connectOsmPbfToPbfConsumer(final Sink consumer)
    {
        this.reader = this.osmosisReaderSupplier.get();
        this.reader.setSink(consumer);
    }

    /**
     * Loops through the given OSM PBF file and count the all the {@link Point}s, {@link Line}s and
     * {@link Relation}s. These will be used to initialize the {@link AtlasSize} to efficiently
     * build the raw {@link Atlas}.
     */
    private void countOsmPbfEntities()
    {
        final Time countTime = Time.now();
        connectOsmPbfToPbfConsumer(this.pbfCounter);
        this.reader.run();
        logger.info("Counted PBF entities in {}", countTime.elapsedSince());
    }

    /**
     * Populates the {@link AtlasMetaData} used to build the raw {@link Atlas}. Specifically,
     * records any {@link Node}, {@link Way} and {@link Relation} filtering that may have been used.
     */
    private void populateAtlasMetadata()
    {
        this.metaData.getTags().put(AtlasMetaData.OSM_PBF_NODE_CONFIGURATION,
                this.atlasLoadingOption.getOsmPbfNodeFilter().toString());
        this.metaData.getTags().put(AtlasMetaData.OSM_PBF_WAY_CONFIGURATION,
                this.atlasLoadingOption.getOsmPbfWayFilter().toString());
        this.metaData.getTags().put(AtlasMetaData.OSM_PBF_RELATION_CONFIGURATION,
                this.atlasLoadingOption.getOsmPbfRelationFilter().toString());
        this.builder.setMetaData(this.metaData);
    }

    /**
     * Sets the {@link AtlasSize} to efficiently build the raw {@link Atlas}, using the values
     * obtained from the {@link OsmPbfCounter}.
     */
    private void setAtlasSizeEstimate()
    {
        final AtlasSize size = new AtlasSize(0, 0, 0, this.pbfCounter.lineCount(),
                this.pbfCounter.pointCount(), this.pbfCounter.relationCount());
        this.builder.setSizeEstimates(size);
    }
}
