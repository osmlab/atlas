package org.openstreetmap.atlas.geography.atlas.raw.creation;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.GeometricSurface;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.MultiPolygon;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.AtlasMetaData;
import org.openstreetmap.atlas.geography.atlas.builder.AtlasSize;
import org.openstreetmap.atlas.geography.atlas.builder.RelationBean;
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.geography.atlas.items.ItemType;
import org.openstreetmap.atlas.geography.atlas.items.Line;
import org.openstreetmap.atlas.geography.atlas.items.Point;
import org.openstreetmap.atlas.geography.atlas.items.Relation;
import org.openstreetmap.atlas.geography.atlas.packed.PackedAtlas;
import org.openstreetmap.atlas.geography.atlas.packed.PackedAtlasBuilder;
import org.openstreetmap.atlas.geography.atlas.pbf.AtlasLoadingOption;
import org.openstreetmap.atlas.geography.atlas.pbf.CloseableOsmosisReader;
import org.openstreetmap.atlas.streaming.resource.Resource;
import org.openstreetmap.atlas.streaming.resource.WritableResource;
import org.openstreetmap.atlas.tags.AtlasTag;
import org.openstreetmap.atlas.tags.LayerTag;
import org.openstreetmap.atlas.tags.SyntheticDuplicateOsmNodeTag;
import org.openstreetmap.atlas.utilities.collections.Iterables;
import org.openstreetmap.atlas.utilities.time.Time;
import org.openstreetmap.osmosis.core.task.v0_6.Sink;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link RawAtlasGenerator} loads an OSM protobuf file and constructs a raw {@link Atlas} from
 * it. A raw {@link Atlas} will only contains Atlas {@link Point}s, {@link Line}s and
 * {@link Relation}s. The protobuf file is structured in a specific way - there is a distinct order:
 * the file first contains Nodes, then Ways and lastly Relations. Each Way only references the
 * identifier, it doesn't contain any location or tag properties of the Nodes used to construct
 * itself. In order to process them - we can either create a Node map or read the file twice. We
 * also want to identify all features that will be part of the Atlas before building it so we can
 * create an accurate {@link AtlasSize}. It is a lot faster to read the file twice than resize the
 * underlying {@link PackedAtlas} arrays during build time. In our implementation, the
 * {@link OsmPbfCounter} is responsible for identifying and counting what to pull in by going
 * through the PBF file once. The {@link OsmPbfReader} will go through it a second time and build
 * the raw Atlas using the information obtained from the counter.
 *
 * @author mgostintsev
 */
public class RawAtlasGenerator
{
    private static final Logger logger = LoggerFactory.getLogger(RawAtlasGenerator.class);

    // Used to identify and count all entities pulled into the Atlas
    private final OsmPbfCounter pbfCounter;

    // Used to build the raw Atlas given the information from the OsmPbfCounter
    private final OsmPbfReader pbfReader;

    // The target bounding box. Anything outside of this will be discarded.
    private final GeometricSurface boundingBox;

    // Builder to build raw Atlas
    private final PackedAtlasBuilder builder;

    // Any configurations needed
    private final AtlasLoadingOption atlasLoadingOption;

    // Osmosis supplier
    private final Supplier<CloseableOsmosisReader> osmosisReaderSupplier;

    // Raw atlas metadata
    private AtlasMetaData metaData = new AtlasMetaData();

    /**
     * Constructor that supplies the maximum bounds possible as the bounding box.
     *
     * @param resource
     *            The OSM PBF {@link Resource} to use
     */
    public RawAtlasGenerator(final Resource resource)
    {
        this(resource, AtlasLoadingOption.createOptionWithOnlySectioning(), MultiPolygon.MAXIMUM);
    }

    /**
     * Default constructor.
     *
     * @param resource
     *            The OSM PBF {@link Resource} to use
     * @param loadingOption
     *            The {@link AtlasLoadingOption} to use
     * @param boundingBox
     *            The bounding box to consider when including features in the raw atlas
     */
    public RawAtlasGenerator(final Resource resource, final AtlasLoadingOption loadingOption,
            final MultiPolygon boundingBox)
    {
        this(() -> new CloseableOsmosisReader(resource.read()), loadingOption, boundingBox);
    }

    /**
     * Constructor that uses the default configuration with a given bounding box.
     *
     * @param resource
     *            The OSM PBF {@link Resource} to use
     * @param boundingBox
     *            The bounding box to consider when including features in the raw atlas
     */
    public RawAtlasGenerator(final Resource resource, final MultiPolygon boundingBox)
    {
        this(resource, AtlasLoadingOption.createOptionWithNoSlicing(), boundingBox);
    }

    public RawAtlasGenerator(final Supplier<CloseableOsmosisReader> osmosisReaderSupplier,
            final AtlasLoadingOption atlasLoadingOption, final GeometricSurface boundingBox)
    {
        this.osmosisReaderSupplier = osmosisReaderSupplier;
        this.atlasLoadingOption = atlasLoadingOption;
        this.boundingBox = boundingBox;
        this.builder = new PackedAtlasBuilder();
        this.pbfReader = new OsmPbfReader(atlasLoadingOption, this.builder);
        this.pbfCounter = new OsmPbfCounter(atlasLoadingOption, this.boundingBox);
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
        prepareBuild();

        // Second pass -- loop through the PBF file again. This time, read the entities and
        // construct a raw Atlas.
        return buildRawAtlas();
    }

    /**
     * Works the same way as build() above, but doesn't trim duplicate and extraneous points from
     * the atlas. This is used as a faster way to build the atlas when verifying the validity of PBF
     * files.
     *
     * @return the raw {@link Atlas}, can be {@code null} or filled with duplicate points
     */
    public Atlas buildNoTrim()
    {
        prepareBuild();

        // Second pass -- loop through the PBF file again. This time, read the entities and
        // construct a raw Atlas.
        return buildRawAtlasNoTrim();
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
        final String shardName = this.metaData.getShardName().orElse("unknown");
        final Atlas atlas = buildRawAtlasNoTrim();

        if (atlas == null)
        {
            logger.info("Generated empty raw Atlas for PBF Shard {}", shardName);
            return atlas;
        }
        else
        {
            final Time trimTime = Time.now();
            final Atlas trimmedAtlas;
            if (this.atlasLoadingOption.isKeepAll())
            {
                trimmedAtlas = atlas;
            }
            else
            {
                trimmedAtlas = removeDuplicateAndExtraneousPointsFromAtlas(atlas);
            }
            logger.info("Trimmed Raw Atlas for {} in {}", shardName, trimTime.elapsedSince());

            if (trimmedAtlas == null)
            {
                logger.info("Empty raw Atlas after filtering for PBF Shard {}", shardName);
            }
            return trimmedAtlas;
        }
    }

    private Atlas buildRawAtlasNoTrim()
    {
        final String shardName = this.metaData.getShardName().orElse("unknown");
        final Time parseTime = Time.now();
        try (CloseableOsmosisReader reader = connectOsmPbfToPbfConsumer(this.pbfReader))
        {
            reader.run();
        }
        catch (final Exception e)
        {
            throw new CoreException("Atlas creation error for PBF shard {}", shardName, e);
        }
        logger.info("Read PBF for {} in {}", shardName, parseTime.elapsedSince());

        final Time buildTime = Time.now();
        final Atlas atlas = this.builder.get();
        logger.info("Built Raw Atlas for {} in {}", shardName, buildTime.elapsedSince());
        return atlas;
    }

    /**
     * Connects the given {@link Sink} implementation to the PBF File.
     */
    private CloseableOsmosisReader connectOsmPbfToPbfConsumer(final Sink consumer)
    {
        final CloseableOsmosisReader reader = this.osmosisReaderSupplier.get();
        reader.setSink(consumer);
        return reader;
    }

    /**
     * Loops through the given OSM PBF file and count the all the {@link Point}s, {@link Line}s and
     * {@link Relation}s. These will be used to initialize the {@link AtlasSize} to efficiently
     * build the raw {@link Atlas}.
     */
    private void countOsmPbfEntities()
    {
        final Time countTime = Time.now();
        try (CloseableOsmosisReader counter = connectOsmPbfToPbfConsumer(this.pbfCounter))
        {
            counter.run();
        }
        catch (final Exception e)
        {
            throw new CoreException("Error counting PBF entities", e);
        }
        logger.info("Counted PBF Entities in {}", countTime.elapsedSince());
    }

    private long getLayerTagValueForPoint(final Atlas atlas, final long identifier)
    {
        return LayerTag.getTaggedOrImpliedValue(atlas.point(identifier), 0L);
    }

    /**
     * Check if the {@link Point} with the given identifier is a {@link Relation} member in the
     * given {@link Atlas}.
     *
     * @param atlas
     *            The {@link Atlas} to check
     * @param pointIdentifier
     *            The {@link Point} identifier to use
     * @return {@code true} if the given {@link Point} identifier is a {@link Relation} member in
     *         the given {@link Atlas}
     */
    private boolean isRelationMember(final Atlas atlas, final long pointIdentifier)
    {
        return !atlas.point(pointIdentifier).relations().isEmpty();
    }

    /**
     * Check if the {@link Point} with the given identifier is a shape point for some {@link Line}
     * in the given {@link Atlas}.
     *
     * @param atlas
     *            The {@link Atlas} to check
     * @param pointIdentifier
     *            The {@link Point} identifier to use
     * @return {@code true} if the given {@link Point} identifier is a shape point for some
     *         {@link Line} in the given {@link Atlas}
     */
    private boolean isShapePoint(final Atlas atlas, final long pointIdentifier)
    {
        return Iterables
                .size(atlas.linesContaining(atlas.point(pointIdentifier).getLocation())) > 0;
    }

    /**
     * A simple point is one that only has the mandatory entity tags. See
     * {@link AtlasTag#TAGS_FROM_OSM} for the 5 tags. Examples of non-simple points include stop
     * lights, barriers, etc.
     *
     * @param atlas
     *            The {@link Atlas} to check
     * @param pointIdentifier
     *            The {@link Point} identifier to use
     * @return {@code true} if the given identifier represents a simple {@link Point}
     */
    private boolean isSimplePoint(final Atlas atlas, final long pointIdentifier)
    {
        return atlas.point(pointIdentifier).getTags().size() == AtlasTag.TAGS_FROM_OSM.size();
    }

    private boolean locationPartOfMultipleWaysWithDifferentLayerTags(final Atlas atlas,
            final Location location)
    {
        final long distinctLayerTagValues = StreamSupport
                .stream(atlas.linesContaining(location).spliterator(), false)
                .map(line -> LayerTag.getTaggedOrImpliedValue(atlas.line(line.getIdentifier()), 0L))
                .distinct().count();

        return distinctLayerTagValues > 1;
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
     * Get the set of {@link Point}s that make up all the filtered PBF {@link Way}s and see if we
     * can remove them from the generated raw Atlas. Criteria for removal are:
     * <ul>
     * <li>The {@link Point} has to be simple. This avoids removing non-shape point features.
     * <li>The {@link Point} cannot be a {@link Relation} member.
     * <li>The {@link Point} cannot be a shape point for an existing {@link Line}.
     * </ul>
     *
     * @param atlas
     *            The {@link Atlas} being filtered from
     * @return the {@link Set} of {@link Point} identifiers that are safe to filter out
     */
    private Set<Long> preFilterPointsToRemove(final Atlas atlas)
    {
        return this.pbfReader.getPointIdentifiersFromFilteredLines().stream()
                .filter(identifier -> atlas.point(identifier) != null)
                .filter(identifier -> isSimplePoint(atlas, identifier))
                .filter(identifier -> !isRelationMember(atlas, identifier))
                .filter(identifier -> !isShapePoint(atlas, identifier)).collect(Collectors.toSet());
    }

    private void prepareBuild()
    {
        countOsmPbfEntities();

        // Update the metadata to reflect any configuration that was used and use count results to
        // set the AtlasSize estimate.
        populateAtlasMetadata();
        setAtlasSizeEstimate();

        // Update the reader to be aware of any included nodes/ways to avoid repeated calculations
        this.pbfReader.setIncludedNodes(this.pbfCounter.getIncludedNodeIdentifiers());
        this.pbfReader.setIncludedWays(this.pbfCounter.getIncludedWayIdentifiers());
    }

    private Atlas rebuildAtlas(final Atlas atlas, final Set<Long> pointsToRemove,
            final Set<Long> pointsNeedingSyntheticTag)
    {
        final PackedAtlasBuilder rebuilder = new PackedAtlasBuilder();

        // Set the metadata and size. Use existing Atlas as estimate.
        rebuilder.setMetaData(this.metaData);
        final AtlasSize size = new AtlasSize(0, 0, atlas.numberOfAreas(), atlas.numberOfLines(),
                atlas.numberOfPoints(), atlas.numberOfRelations());
        rebuilder.setSizeEstimates(size);

        // Add Points
        atlas.points().forEach(point ->
        {
            final long identifier = point.getIdentifier();
            // Only add if this point isn't being removed
            if (!pointsToRemove.contains(identifier))
            {
                final Map<String, String> tags = point.getTags();
                if (pointsNeedingSyntheticTag.contains(identifier))
                {
                    // Add the synthetic tag
                    tags.put(SyntheticDuplicateOsmNodeTag.KEY,
                            SyntheticDuplicateOsmNodeTag.YES.toString());
                }

                // Add the Point
                rebuilder.addPoint(identifier, point.getLocation(), tags);
            }
        });

        // Add Lines
        atlas.lines().forEach(
                line -> rebuilder.addLine(line.getIdentifier(), line.asPolyLine(), line.getTags()));

        // Add Lines
        atlas.areas().forEach(
                area -> rebuilder.addArea(area.getIdentifier(), area.asPolygon(), area.getTags()));

        // Add Relations
        // Keep a set of all relations that have members that have been removed, so if that member
        // is the only member, we do not add the parent relation either.
        final Set<Long> relationsToCheckForRemoval = new HashSet<>();
        atlas.relationsLowerOrderFirst().forEach(relation ->
        {
            final RelationBean bean = new RelationBean();
            relation.members().forEach(member ->
            {
                final AtlasEntity entity = member.getEntity();
                final long memberIdentifier = entity.getIdentifier();
                if (entity.getType() == ItemType.POINT && pointsToRemove.contains(memberIdentifier))
                {
                    // Make sure not to add any removed points
                    logger.debug(
                            "Excluding point {} from relation {} since point was removed from Atlas",
                            memberIdentifier, relation.getIdentifier());
                }
                else if (entity.getType() == ItemType.RELATION
                        && relationsToCheckForRemoval.contains(memberIdentifier))
                {
                    // Make sure not to add any removed relations
                    logger.debug(
                            "Excluding relation member {} from parent relation {} since that relation member became empty",
                            memberIdentifier, relation.getIdentifier());
                }
                else
                {
                    bean.addItem(memberIdentifier, member.getRole(), entity.getType());
                }
            });

            if (!bean.isEmpty())
            {
                rebuilder.addRelation(relation.getIdentifier(), relation.getOsmIdentifier(), bean,
                        relation.getTags());
            }
            else
            {
                final long relationIdentifier = relation.getIdentifier();
                logger.debug("Relation {} bean is empty, dropping from Atlas", relationIdentifier);
                relationsToCheckForRemoval.add(relationIdentifier);
            }
        });

        // Build and return the new Atlas
        return rebuilder.get();
    }

    /**
     * We may need to remove {@link Point}s from the built raw Atlas. There are two scenarios for
     * removal:
     * <p>
     * <ul>
     * <li>1. A {@link Point} was a shape point for an OSM {@link Way} that was removed. This point
     * doesn't have any tags, isn't a part of a {@link Relation} and doesn't intersect with any
     * other features in the Atlas.
     * <li>2. There are multiple {@link Point}s at a {@link Location}. In this case, we sort all the
     * points, keep the one with the smallest identifier, add a {@link SyntheticDuplicateOsmNodeTag}
     * and remove the rest of the duplicate points. Two notes: 1. We keep Nodes if they have
     * different layer tagging. This way, we aren't creating a false connection between an overpass
     * and a road beneath it, which happened to have a way node at the identical location. 2. We are
     * potentially tossing out OSM Nodes with non-empty tags. However, this is the most
     * deterministic and simple way to handle this. The presence of the synthetic tag will make it
     * easy to write an Atlas Check to resolve the data error.
     * </ul>
     *
     * @param atlas
     *            The {@link Atlas} to remove the Points from
     * @return a new {@link Atlas} without the extra points or the given Atlas if no removal is
     *         needed
     */
    private Atlas removeDuplicateAndExtraneousPointsFromAtlas(final Atlas atlas)
    {
        final Set<Long> pointsToRemove = new HashSet<>();
        final Set<Long> duplicatePointsToKeep = new HashSet<>();

        for (final Point point : atlas.points())
        {
            // Don't try to de-duplicate points we've already handled
            if (!pointsToRemove.contains(point.getIdentifier())
                    && !duplicatePointsToKeep.contains(point.getIdentifier()))
            {
                final Set<Long> duplicatePoints = Iterables
                        .stream(atlas.pointsAt(point.getLocation())).map(Point::getIdentifier)
                        .collectToSet();
                if (!duplicatePoints.isEmpty() && duplicatePoints.size() > 1)
                {
                    // Factor in ways that pass through these points. If these points are part of
                    // ways that have a different layer tag value, then
                    // keep all of them to avoid merging ways that shouldn't be merged.
                    if (locationPartOfMultipleWaysWithDifferentLayerTags(atlas,
                            point.getLocation()))
                    {
                        duplicatePointsToKeep.addAll(duplicatePoints);
                        continue;
                    }

                    // Sort the points
                    final Set<Long> sortedDuplicates = Iterables.asSortedSet(duplicatePoints);
                    final Set<Long> uniqueLayerValues = new HashSet<>();

                    // Keep the point with the smallest identifier (deterministic) for each layer
                    final Iterator<Long> duplicateIterator = sortedDuplicates.iterator();
                    final long duplicatePointToKeep = duplicateIterator.next();
                    final long layerValue = getLayerTagValueForPoint(atlas, duplicatePointToKeep);

                    duplicatePointsToKeep.add(duplicatePointToKeep);
                    duplicateIterator.remove();
                    uniqueLayerValues.add(layerValue);

                    while (duplicateIterator.hasNext())
                    {
                        final long candidateToKeep = duplicateIterator.next();
                        final long candidateLayerValue = getLayerTagValueForPoint(atlas,
                                candidateToKeep);

                        if (!uniqueLayerValues.contains(candidateLayerValue))
                        {
                            // Keep the point if it has a unique layer value
                            duplicatePointsToKeep.add(candidateToKeep);
                            duplicateIterator.remove();
                        }
                    }

                    // Remove all remaining (non-kept) points
                    pointsToRemove.addAll(sortedDuplicates);
                }
            }
        }

        // Remove any non-used shape points from filtered lines
        if (!this.pbfReader.getPointIdentifiersFromFilteredLines().isEmpty())
        {
            pointsToRemove.addAll(preFilterPointsToRemove(atlas));
        }

        // Remove points or return the original atlas
        if (pointsToRemove.isEmpty())
        {
            return atlas;
        }
        else
        {
            // Rebuild the Atlas to add the synthetic tags and get rid of the removed points
            return rebuildAtlas(atlas, pointsToRemove, duplicatePointsToKeep);
        }
    }

    /**
     * Sets the {@link AtlasSize} to efficiently build the raw {@link Atlas}, using the values
     * obtained from the {@link OsmPbfCounter}.
     */
    private void setAtlasSizeEstimate()
    {
        final AtlasSize size = new AtlasSize(0, 0, this.pbfCounter.lineCount(),
                this.pbfCounter.lineCount(), this.pbfCounter.pointCount(),
                this.pbfCounter.relationCount());
        this.builder.setSizeEstimates(size);
    }
}
