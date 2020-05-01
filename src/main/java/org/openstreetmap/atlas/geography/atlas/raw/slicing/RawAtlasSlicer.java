package org.openstreetmap.atlas.geography.atlas.raw.slicing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.IntersectionMatrix;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.geom.TopologyException;
import org.locationtech.jts.operation.linemerge.LineMerger;
import org.locationtech.jts.operation.overlay.snap.SnapOverlayOp;
import org.locationtech.jts.operation.valid.IsValidOp;
import org.locationtech.jts.precision.GeometryPrecisionReducer;
import org.locationtech.jts.precision.PrecisionReducerCoordinateOperation;
import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.MultiPolygon;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.Polygon;
import org.openstreetmap.atlas.geography.Rectangle;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.change.ChangeAtlas;
import org.openstreetmap.atlas.geography.atlas.change.ChangeBuilder;
import org.openstreetmap.atlas.geography.atlas.change.FeatureChange;
import org.openstreetmap.atlas.geography.atlas.complete.CompleteLine;
import org.openstreetmap.atlas.geography.atlas.complete.CompletePoint;
import org.openstreetmap.atlas.geography.atlas.complete.CompleteRelation;
import org.openstreetmap.atlas.geography.atlas.dynamic.DynamicAtlas;
import org.openstreetmap.atlas.geography.atlas.dynamic.policy.DynamicAtlasPolicy;
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.ItemType;
import org.openstreetmap.atlas.geography.atlas.items.Line;
import org.openstreetmap.atlas.geography.atlas.items.Point;
import org.openstreetmap.atlas.geography.atlas.items.Relation;
import org.openstreetmap.atlas.geography.atlas.items.RelationMember;
import org.openstreetmap.atlas.geography.atlas.items.complex.RelationOrAreaToMultiPolygonConverter;
import org.openstreetmap.atlas.geography.atlas.pbf.AtlasLoadingOption;
import org.openstreetmap.atlas.geography.atlas.pbf.slicing.identifier.AbstractIdentifierFactory;
import org.openstreetmap.atlas.geography.atlas.pbf.slicing.identifier.CountrySlicingIdentifierFactory;
import org.openstreetmap.atlas.geography.atlas.pbf.slicing.identifier.PointIdentifierFactory;
import org.openstreetmap.atlas.geography.atlas.sub.AtlasCutType;
import org.openstreetmap.atlas.geography.boundary.CountryBoundaryMap;
import org.openstreetmap.atlas.geography.converters.jts.JtsMultiPolygonToMultiPolygonConverter;
import org.openstreetmap.atlas.geography.converters.jts.JtsPolyLineConverter;
import org.openstreetmap.atlas.geography.converters.jts.JtsPolygonConverter;
import org.openstreetmap.atlas.geography.converters.jts.JtsPrecisionManager;
import org.openstreetmap.atlas.geography.sharding.Shard;
import org.openstreetmap.atlas.geography.sharding.Sharding;
import org.openstreetmap.atlas.tags.ISOCountryTag;
import org.openstreetmap.atlas.tags.RelationTypeTag;
import org.openstreetmap.atlas.tags.SyntheticBoundaryNodeTag;
import org.openstreetmap.atlas.tags.SyntheticNearestNeighborCountryCodeTag;
import org.openstreetmap.atlas.tags.SyntheticRelationMemberAdded;
import org.openstreetmap.atlas.tags.annotations.validation.Validators;
import org.openstreetmap.atlas.utilities.collections.Iterables;
import org.openstreetmap.atlas.utilities.identifiers.EntityIdentifierGenerator;
import org.openstreetmap.atlas.utilities.scalars.Duration;
import org.openstreetmap.atlas.utilities.time.Time;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

/**
 * The abstract class that contains all common raw Atlas slicing functionality.
 *
 * @author mgostintsev
 */
public class RawAtlasSlicer
{
    // JTS converters
    private static final JtsPolygonConverter JTS_POLYGON_CONVERTER = new JtsPolygonConverter();
    private static final JtsPolyLineConverter JTS_POLYLINE_CONVERTER = new JtsPolyLineConverter();
    private static final JtsMultiPolygonToMultiPolygonConverter JTS_MULTIPOLYGON_CONVERTER = new JtsMultiPolygonToMultiPolygonConverter();
    private static final RelationOrAreaToMultiPolygonConverter RELATION_TO_MULTIPOLYGON_CONVERTER = new RelationOrAreaToMultiPolygonConverter();
    private static final Logger logger = LoggerFactory.getLogger(RawAtlasSlicer.class);
    // JTS precision handling
    private static final Integer SEVEN_DIGIT_PRECISION_SCALE = 10_000_000;
    private static final PrecisionModel PRECISION_MODEL = new PrecisionModel(
            SEVEN_DIGIT_PRECISION_SCALE);
    protected static final PrecisionReducerCoordinateOperation PRECISION_REDUCER = new PrecisionReducerCoordinateOperation(
            PRECISION_MODEL, true);

    // Logging constants
    private static final String COMPLETED_TASK_MESSAGE = "Finished {} for Shard {} in {}";
    private static final String DYNAMIC_ATLAS_CREATION_TASK = "dynamic Atlas creation";
    private static final long RELATION_SLICING_DURATION_WARN = 10;

    // Bring in all Relations that are tagged according to our filter
    private final Predicate<AtlasEntity> relationPredicate;
    private final AtlasLoadingOption loadingOption;
    private final String shardOrAtlasName;
    private final Atlas startingAtlas;
    private final Shard initialShard;

    private final Map<Long, Map<String, CompleteRelation>> splitRelations = new HashMap<>();

    private final Set<FeatureChange> changes = new HashSet<>();

    private final Map<Long, CompleteRelation> stagedRelations = new HashMap<>();

    private final Map<Long, CompleteLine> stagedLines = new HashMap<>();

    private final Map<Long, CompletePoint> stagedPoints = new HashMap<>();

    private final Predicate<AtlasEntity> isInCountry;

    /**
     * @param loadingOption
     *            does
     * @param startingAtlas
     *            this
     */
    public RawAtlasSlicer(final AtlasLoadingOption loadingOption, final Atlas startingAtlas)
    {
        this.loadingOption = loadingOption;
        this.startingAtlas = startingAtlas;
        this.initialShard = null;
        this.relationPredicate = entity -> entity.getType().equals(ItemType.RELATION)
                && loadingOption.getRelationSlicingFilter().test(entity);
        this.isInCountry = entity -> ISOCountryTag.isIn(this.getCountries()).test(entity);
        this.shardOrAtlasName = startingAtlas.metaData().getShardName()
                .orElse(startingAtlas.getName());
        this.startingAtlas.points().forEach(
                point -> this.stagedPoints.put(point.getIdentifier(), CompletePoint.from(point)));
        this.startingAtlas.lines().forEach(
                line -> this.stagedLines.put(line.getIdentifier(), CompleteLine.from(line)));
        this.startingAtlas.relations().forEach(relation -> this.stagedRelations
                .put(relation.getIdentifier(), CompleteRelation.from(relation)));
    }

    /**
     * @param loadingOption
     *            does
     * @param initialShard
     *            this
     * @param sharding
     *            not
     * @param atlasFetcher
     *            count
     */
    public RawAtlasSlicer(final AtlasLoadingOption loadingOption, final Shard initialShard,
            final Sharding sharding, final Function<Shard, Optional<Atlas>> atlasFetcher)
    {
        this.loadingOption = loadingOption;
        this.initialShard = initialShard;
        this.relationPredicate = entity -> entity.getType().equals(ItemType.RELATION)
                && loadingOption.getRelationSlicingFilter().test(entity);
        this.isInCountry = entity -> ISOCountryTag.isIn(this.getCountries()).test(entity);

        // build an expanded version of the initial shard on the relations that match the predicate
        final Set<Long> relationsForInitialShard = new HashSet<>();
        final Optional<Atlas> initialShardOptional = atlasFetcher.apply(initialShard);
        if (initialShardOptional.isPresent())
        {
            initialShardOptional.get().relations()
                    .forEach(relation -> relationsForInitialShard.add(relation.getIdentifier()));
        }
        else
        {
            throw new CoreException(
                    "Could not get data for initial shard {} during relation slicing!",
                    initialShard.getName());
        }
        final Atlas expandedAtlas = buildExpandedAtlas(initialShard, initialShardOptional.get(),
                sharding, atlasFetcher);
        // expanding will bring in multipolygon relations that weren't on the original shard; here,
        // we'll filter those out so we don't unnecessarily process them
        final Predicate<AtlasEntity> filter = entity ->
        {
            if (entity.getType().equals(ItemType.RELATION))
            {
                return relationsForInitialShard.contains(entity.getIdentifier());
            }
            return true;
        };
        final Optional<Atlas> subAtlasOptional = expandedAtlas.subAtlas(filter,
                AtlasCutType.SILK_CUT);
        if (subAtlasOptional.isPresent())
        {
            this.startingAtlas = subAtlasOptional.get();
        }
        else
        {
            throw new CoreException(
                    "No data after sub-atlasing to remove new relations from partially expanded Atlas {}!",
                    expandedAtlas.getName());
        }

        this.shardOrAtlasName = this.startingAtlas.metaData().getShardName()
                .orElse(this.startingAtlas.getName());
        this.startingAtlas.points().forEach(
                point -> this.stagedPoints.put(point.getIdentifier(), CompletePoint.from(point)));
        this.startingAtlas.lines().forEach(
                line -> this.stagedLines.put(line.getIdentifier(), CompleteLine.from(line)));
        this.startingAtlas.relations().forEach(relation -> this.stagedRelations
                .put(relation.getIdentifier(), CompleteRelation.from(relation)));
    }

    public Atlas slice()
    {
        final Time overallTime = Time.now();
        Time time = Time.now();
        logger.info("Starting slicing for Atlas {}", this.shardOrAtlasName);
        logger.info("Starting line slicing for Atlas {}", this.shardOrAtlasName);
        this.startingAtlas.lines(line -> this.stagedLines.containsKey(line.getIdentifier())
                && !Validators.hasValuesFor(this.stagedLines.get(line.getIdentifier()),
                        ISOCountryTag.class))
                .forEach(line ->
                {
                    if (line.isClosed() && !isAtlasEdge(line) && !isMultipolygonMember(line))
                    {
                        sliceArea(line);
                    }
                    else
                    {
                        sliceLine(line);
                    }
                });
        logger.info("Finished line slicing for Atlas {} in {}", this.shardOrAtlasName,
                time.elapsedSince());

        time = Time.now();
        logger.info("Starting relation slicing for Atlas {}", this.shardOrAtlasName);
        this.startingAtlas.relationsLowerOrderFirst().forEach(relation ->
        {
            if (this.relationPredicate.test(relation))
            {
                sliceMultiPolygonRelation(this.stagedRelations.get(relation.getIdentifier()));
            }
        });
        logger.info("Finished relation slicing for Atlas {} in {}", this.shardOrAtlasName,
                time.elapsedSince());

        time = Time.now();
        logger.info("Starting point slicing for Atlas {}", this.shardOrAtlasName);
        this.startingAtlas.points().forEach(this::slicePoint);
        logger.info("Finished point slicing for Atlas {} in {}", this.shardOrAtlasName,
                time.elapsedSince());

        logger.info("Starting relation filtering for Atlas {}", this.shardOrAtlasName);
        this.startingAtlas.relationsLowerOrderFirst().forEach(relation ->
        {
            if (this.stagedRelations.containsKey(relation.getIdentifier())
                    && !Validators.hasValuesFor(this.stagedRelations.get(relation.getIdentifier()),
                            ISOCountryTag.class))
            {
                filterRelation(this.stagedRelations.get(relation.getIdentifier()));
            }
        });
        logger.info("Finished relation filtering for Atlas {} in {}", this.shardOrAtlasName,
                time.elapsedSince());

        this.stagedLines.values()
                .forEach(line -> this.changes.add(FeatureChange.add(line, this.startingAtlas)));
        this.stagedPoints.values()
                .forEach(line -> this.changes.add(FeatureChange.add(line, this.startingAtlas)));
        this.stagedRelations.values()
                .forEach(line -> this.changes.add(FeatureChange.add(line, this.startingAtlas)));

        logger.info("Finished slicing for Atlas {} in {}", this.shardOrAtlasName,
                overallTime.elapsedSince());
        if (this.changes.isEmpty())
        {
            return cutSubAtlasForOriginalShard(this.startingAtlas);
        }

        return cutSubAtlasForOriginalShard(new ChangeAtlas(this.startingAtlas,
                new ChangeBuilder().addAll(this.changes).get()));
    }

    private void addCountryMembersToSplitRelation(final CompleteRelation newRelation,
            final CompleteRelation oldRelation)
    {
        oldRelation.membersMatching(member -> Validators.isOfSameType(
                this.stagedLines.get(member.getEntity().getIdentifier()), newRelation,
                ISOCountryTag.class)).forEach(member ->
                {
                    newRelation.withAddedMember(
                            this.stagedLines.get(member.getEntity().getIdentifier()),
                            member.getRole());
                    this.stagedLines.get(member.getEntity().getIdentifier())
                            .withRemovedRelationIdentifier(oldRelation.getIdentifier());
                    this.stagedLines.get(member.getEntity().getIdentifier())
                            .withAddedRelationIdentifier(newRelation.getIdentifier());
                });
    }

    private void addSyntheticBoundaryNodesForSlice(final Line line, final PolyLine slice)
    {
        if (!slice.first().equals(line.asPolyLine().first()))
        {
            final Iterable<Point> pointsAtFirstLocation = this.startingAtlas
                    .pointsAt(slice.first());
            if (Iterables.isEmpty(pointsAtFirstLocation))
            {
                final EntityIdentifierGenerator pointIdentifierGenerator = new EntityIdentifierGenerator();
                final CompletePoint syntheticBoundaryNode = new CompletePoint(1L, slice.first(),
                        createPointTags(slice.first(), false), new HashSet<>());
                syntheticBoundaryNode.withAddedTag(SyntheticBoundaryNodeTag.KEY,
                        SyntheticBoundaryNodeTag.YES.toString());
                syntheticBoundaryNode.withIdentifier(
                        pointIdentifierGenerator.generateIdentifier(syntheticBoundaryNode));
                this.stagedPoints.put(syntheticBoundaryNode.getIdentifier(), syntheticBoundaryNode);
            }
            else
            {
                this.stagedPoints.get(pointsAtFirstLocation.iterator().next().getIdentifier())
                        .withAddedTag(SyntheticBoundaryNodeTag.KEY,
                                SyntheticBoundaryNodeTag.EXISTING.toString());
            }
        }

        if (!slice.last().equals(line.asPolyLine().last()))
        {
            final Iterable<Point> pointsAtLastLocation = this.startingAtlas.pointsAt(slice.last());
            if (Iterables.isEmpty(pointsAtLastLocation))
            {
                final EntityIdentifierGenerator pointIdentifierGenerator = new EntityIdentifierGenerator();
                final CompletePoint syntheticBoundaryNode = new CompletePoint(1L, slice.last(),
                        createPointTags(slice.last(), false), new HashSet<>());
                syntheticBoundaryNode.withAddedTag(SyntheticBoundaryNodeTag.KEY,
                        SyntheticBoundaryNodeTag.YES.toString());
                syntheticBoundaryNode.withIdentifier(
                        pointIdentifierGenerator.generateIdentifier(syntheticBoundaryNode));
                this.stagedPoints.put(syntheticBoundaryNode.getIdentifier(), syntheticBoundaryNode);
            }
            else
            {
                this.stagedPoints.get(pointsAtLastLocation.iterator().next().getIdentifier())
                        .withAddedTag(SyntheticBoundaryNodeTag.KEY,
                                SyntheticBoundaryNodeTag.EXISTING.toString());
            }
        }
    }

    private Set<String> addSyntheticLinesForRemainder(final CompleteRelation newRelation,
            final Geometry remainder, final String role)
    {
        final Optional<String> countryCodeTag = newRelation.getTag(ISOCountryTag.KEY);
        if (countryCodeTag.isEmpty())
        {
            throw new CoreException("Could not find country code for split relation {}",
                    newRelation.getIdentifier());
        }
        final String countryCode = countryCodeTag.get();
        final Set<String> synetheticIdsAdded = new HashSet<>();
        for (int i = 0; i < remainder.getNumGeometries(); i++)
        {
            final LineString remainderLine = (LineString) remainder.getGeometryN(i);
            final Map<String, String> newLineTags = new HashMap<>();
            newLineTags.put(ISOCountryTag.KEY, countryCode);
            final EntityIdentifierGenerator lineIdentifierGenerator = new EntityIdentifierGenerator();
            final CompleteLine newLine = new CompleteLine(0L,
                    JTS_POLYLINE_CONVERTER.backwardConvert(remainderLine), newLineTags,
                    new HashSet<Long>());
            newLine.withIdentifier(lineIdentifierGenerator.generateIdentifier(newLine));
            newLine.withAddedRelationIdentifier(newRelation.getIdentifier());
            newRelation.withAddedMember(newLine, role);
            this.stagedLines.put(newLine.getIdentifier(), newLine);
            synetheticIdsAdded.add(Long.toString(newLine.getIdentifier()));
        }
        return synetheticIdsAdded;
    }

    /**
     * Grabs the atlas for the initial shard, in its entirety. Then proceeds to expand out to
     * surrounding shards if there are any edges bleeding over the shard bounds plus
     * {@link #SHARD_EXPANSION_DISTANCE}. Finally, will return the constructed Atlas.
     *
     * @param initialShard
     *            The initial {@link Shard} being processed
     * @param sharding
     *            The {@link Sharding} used to identify which shards to fetch
     * @param partiallySlicedAtlasFetcher
     *            The fetcher policy to retrieve an Atlas file for each shard
     * @return the expanded {@link Atlas}
     */
    private Atlas buildExpandedAtlas(final Shard initialShard, final Atlas initialAtlas,
            final Sharding sharding, final Function<Shard, Optional<Atlas>> atlasFetcher)
    {
        final Predicate<AtlasEntity> expandPredicate = entity -> entity.getType()
                .equals(ItemType.LINE)
                && entity.relations().stream()
                        .anyMatch(relation -> this.relationPredicate.test(relation)
                                && initialAtlas.relation(relation.getIdentifier()) != null);
        final Time dynamicAtlasTime = Time.now();
        logger.info(DYNAMIC_ATLAS_CREATION_TASK, initialShard.getName());
        final DynamicAtlasPolicy policy = new DynamicAtlasPolicy(atlasFetcher, sharding,
                initialShard, Rectangle.MAXIMUM).withDeferredLoading(true)
                        .withExtendIndefinitely(true)
                        .withAtlasEntitiesToConsiderForExpansion(expandPredicate);

        final DynamicAtlas atlas = new DynamicAtlas(policy);
        atlas.preemptiveLoad();
        logger.info(COMPLETED_TASK_MESSAGE, DYNAMIC_ATLAS_CREATION_TASK, initialShard.getName(),
                dynamicAtlasTime.elapsedSince());
        return atlas;
    }

    private void createNewSlicedAreas(final Line line,
            final SortedMap<String, Set<org.locationtech.jts.geom.Polygon>> slices)
    {
        final CountrySlicingIdentifierFactory lineIdentifierFactory = new CountrySlicingIdentifierFactory(
                line.getIdentifier());
        slices.keySet().forEach(countryCode ->
        {
            for (final org.locationtech.jts.geom.Polygon slice : slices.get(countryCode))
            {
                final long lineSliceIdentifier = lineIdentifierFactory.nextIdentifier();
                final PolyLine newLineGeometry = processSlice(slice, line);
                final Map<String, String> lineTags = line.getTags();
                lineTags.put(ISOCountryTag.KEY, countryCode);
                final CompleteLine newLineSlice = CompleteLine.from(line)
                        .withIdentifier(lineSliceIdentifier).withTags(lineTags)
                        .withPolyLine(newLineGeometry);
                if (isInsideWorkingBound(newLineSlice))
                {
                    this.stagedLines.put(newLineSlice.getIdentifier(), newLineSlice);
                    for (final Relation relation : line.relations())
                    {
                        if (this.stagedRelations.containsKey(relation.getIdentifier()))
                        {
                            this.stagedRelations.get(relation.getIdentifier())
                                    .withAddedMember(newLineSlice, line);
                        }
                    }
                }
            }
        });
        final CompleteLine removedOldLine = this.stagedLines.remove(line.getIdentifier());
        this.changes.add(FeatureChange.remove(removedOldLine, this.startingAtlas));
        this.stagedLines.remove(line.getIdentifier());
        removedOldLine.relationIdentifiers().forEach(relationIdentifier ->
        {
            if (this.stagedRelations.containsKey(relationIdentifier))
            {
                this.stagedRelations.get(relationIdentifier).withRemovedMember(removedOldLine);
            }
        });
    }

    private void createNewSlicedLines(final Line line,
            final SortedMap<String, Set<LineString>> slices)
    {
        final CountrySlicingIdentifierFactory lineIdentifierFactory = new CountrySlicingIdentifierFactory(
                line.getIdentifier());
        slices.keySet().forEach(countryCode ->
        {
            for (final LineString slice : slices.get(countryCode))
            {
                final long lineSliceIdentifier = lineIdentifierFactory.nextIdentifier();
                if (this.getCountries().contains(countryCode))
                {
                    final PolyLine newLineGeometry = processSlice(slice, line);
                    if (isAtlasEdge(line))
                    {
                        addSyntheticBoundaryNodesForSlice(line, newLineGeometry);
                    }
                    final Map<String, String> lineTags = line.getTags();
                    lineTags.put(ISOCountryTag.KEY, countryCode);
                    final CompleteLine newLineSlice = CompleteLine.from(line)
                            .withIdentifier(lineSliceIdentifier).withTags(lineTags)
                            .withPolyLine(newLineGeometry);
                    this.stagedLines.put(newLineSlice.getIdentifier(), newLineSlice);
                    for (final Relation relation : line.relations())
                    {
                        if (this.stagedRelations.containsKey(relation.getIdentifier()))
                        {
                            this.stagedRelations.get(relation.getIdentifier())
                                    .withAddedMember(newLineSlice, line);
                        }
                    }
                }
            }
        });
        final CompleteLine removedOldLine = this.stagedLines.remove(line.getIdentifier());
        this.changes.add(FeatureChange.remove(removedOldLine, this.startingAtlas));
        this.stagedLines.remove(line.getIdentifier());
        removedOldLine.relationIdentifiers().forEach(relationIdentifier ->
        {
            if (this.stagedRelations.containsKey(relationIdentifier))
            {
                this.stagedRelations.get(relationIdentifier).withRemovedMember(removedOldLine);
            }
        });
    }

    private Map<String, String> createPointTags(final Location location, final boolean fromRawAtlas)
    {
        final Map<String, String> tags = new HashMap<>();

        // Get the country code details
        final CountryCodeProperties countryDetails = getCountryBoundaryMap()
                .getCountryCodeISO3(location);

        // Store the country code, enforce alphabetical order if there are multiple
        if (countryDetails.inMultipleCountries())
        {
            tags.put(ISOCountryTag.KEY, String.join(",", Sets.newTreeSet(Arrays.asList(
                    countryDetails.getIso3CountryCode().split(ISOCountryTag.COUNTRY_DELIMITER)))));
        }
        else
        {
            tags.put(ISOCountryTag.KEY, countryDetails.getIso3CountryCode());
        }

        // If we used nearest neighbor logic to determine the country code, add a tag
        // to indicate this
        if (countryDetails.usingNearestNeighbor())
        {
            tags.put(SyntheticNearestNeighborCountryCodeTag.KEY,
                    SyntheticNearestNeighborCountryCodeTag.YES.toString());
            tags.put(SyntheticBoundaryNodeTag.KEY, SyntheticBoundaryNodeTag.EXISTING.toString());
        }

        // For any border nodes, add the existing tag
        if (fromRawAtlas && countryDetails.inMultipleCountries())
        {
            tags.put(SyntheticBoundaryNodeTag.KEY, SyntheticBoundaryNodeTag.EXISTING.toString());
        }

        return tags;
    }

    private void createSyntheticRelationMembers(final CompleteRelation newRelation,
            final org.locationtech.jts.geom.MultiPolygon newMultiPolygon)
    {

        final SortedSet<String> syntheticIds = new TreeSet<>();
        // add in remaining synthetic segments
        for (int polygonIndex = 0; polygonIndex < newMultiPolygon
                .getNumGeometries(); polygonIndex++)
        {
            final org.locationtech.jts.geom.Polygon geometry = (org.locationtech.jts.geom.Polygon) newMultiPolygon
                    .getGeometryN(polygonIndex);
            Geometry remainderExterior = geometry.getExteriorRing();
            if (newRelation.members() != null)
            {
                remainderExterior = cutOutExistingMembers(newRelation, geometry.getExteriorRing());
            }
            else
            {
                logger.warn("Relation {} created using entirely synthetic members!",
                        newRelation.getIdentifier());
            }

            if (remainderExterior.isEmpty())
            {
                continue;
            }
            syntheticIds.addAll(addSyntheticLinesForRemainder(newRelation, remainderExterior,
                    RelationTypeTag.MULTIPOLYGON_ROLE_OUTER));

            for (int i = 0; i < geometry.getNumInteriorRing(); i++)
            {
                Geometry remainderInterior = geometry.getInteriorRingN(i);
                for (final RelationMember member : newRelation.membersMatching(
                        member -> member.getRole().equals(RelationTypeTag.MULTIPOLYGON_ROLE_INNER)))
                {
                    final LineString innerLineString = JTS_POLYLINE_CONVERTER.convert(
                            this.stagedLines.get(member.getEntity().getIdentifier()).asPolyLine());
                    if (innerLineString.intersects(remainderInterior))
                    {
                        remainderInterior = SnapOverlayOp.difference(remainderInterior,
                                innerLineString);
                    }
                }

                if (remainderInterior.isEmpty())
                {
                    continue;
                }
                syntheticIds.addAll(addSyntheticLinesForRemainder(newRelation, remainderInterior,
                        RelationTypeTag.MULTIPOLYGON_ROLE_INNER));
            }
        }
        newRelation.withAddedTag(SyntheticRelationMemberAdded.KEY, String.join(",", syntheticIds));
    }

    private Geometry cutOutExistingMembers(final CompleteRelation newRelation,
            final Geometry slicedPolygonOuter)
    {
        Geometry remainder = slicedPolygonOuter;
        for (final RelationMember member : newRelation.members())
        {
            final LineString outerLineString = JTS_POLYLINE_CONVERTER
                    .convert(this.stagedLines.get(member.getEntity().getIdentifier()).asPolyLine());
            if (outerLineString.intersects(slicedPolygonOuter))
            {
                try
                {
                    remainder = SnapOverlayOp.difference(remainder, outerLineString);
                }
                catch (final TopologyException exception)
                {
                    logger.error(
                            "Topology exception while using snap overlay operation on relation {}, falling back to regularl intersection",
                            newRelation.getIdentifier());
                    remainder = remainder.difference(outerLineString);
                }
                if (member.getRole().equals(RelationTypeTag.MULTIPOLYGON_ROLE_INNER))
                {
                    newRelation.changeMemberRole(member.getEntity(),
                            RelationTypeTag.MULTIPOLYGON_ROLE_OUTER);
                }
            }
        }
        return remainder;
    }

    /**
     * Used to cut out unnecessary data loaded in during DynamicAtlas exploration from the final
     * shard. First, a subatlas is called to remove lines loaded in from countries not being
     * sliced-- this is important because when dynamically exploring water relations, line sliced
     * data from other countries is loaded so the whole relation can be loaded. That data is not
     * necessary once the relation has been sliced, so it is removed. Then, a separate subatlas call
     * is made to trim out geometry outside the shard bounds. Note that SILK_CUT is used so that any
     * lines that expand past the borders of the shard have their underlying point geometries kept.
     * This inclusion is critical for way-sectioning, which needs all lines to have their points for
     * purposes of edge detection.
     *
     * @param atlas
     *            The {@link Atlas} file we need to trim
     * @return the {@link Atlas} for the bounds of the input shard
     */
    private Atlas cutSubAtlasForOriginalShard(final Atlas atlas)
    {
        if (this.initialShard == null)
        {
            return atlas;
        }
        // then, filter out all remaining entities based on the shard bounds
        final Optional<Atlas> finalSubAtlas = atlas.subAtlas(this.initialShard.bounds(),
                AtlasCutType.SILK_CUT);
        if (finalSubAtlas.isPresent())
        {
            return finalSubAtlas.get();
        }
        else
        {
            return null;
        }
    }

    private void filterRelation(final CompleteRelation relation)
    {
        final Set<String> countryList = new HashSet<>();
        for (final RelationMember member : relation.members())
        {
            final AtlasEntity stagedRelationMember = getStagedEntityForMember(member);
            if (stagedRelationMember == null)
            {
                relation.withRemovedMember(member.getEntity());
                if (member.getEntity().getType().equals(ItemType.RELATION)
                        && this.splitRelations.containsKey(member.getEntity().getIdentifier()))
                {
                    this.splitRelations.get(member.getEntity().getIdentifier()).keySet()
                            .forEach(countryCode ->
                            {
                                if (this.getCountries().contains(countryCode))
                                {
                                    relation.withAddedMember(this.splitRelations
                                            .get(member.getEntity().getIdentifier())
                                            .get(countryCode), member.getRole());
                                    countryList.add(countryCode);
                                }
                            });
                }
                continue;
            }
            final Optional<String> countryCodeTag = stagedRelationMember.getTag(ISOCountryTag.KEY);
            if (countryCodeTag.isEmpty())
            {
                throw new CoreException("Untagged country value for entity {} for relation {}",
                        stagedRelationMember, relation.getIdentifier());

            }
            Collections.addAll(countryList, countryCodeTag.get().split(","));
        }
        if (countryList.isEmpty())
        {
            this.stagedRelations.remove(relation.getIdentifier());
            this.changes.add(FeatureChange.remove(relation));
            return;
        }

        // compute the value of the final country tag
        relation.withAddedTag(ISOCountryTag.KEY, ISOCountryTag.join(countryList));
    }

    private Set<String> getCountries()
    {
        if (this.loadingOption.getCountryCodes().isEmpty())
        {
            final HashSet<String> allCountryCodes = new HashSet<>();
            allCountryCodes.addAll(this.loadingOption.getCountryBoundaryMap().allCountryNames());
            this.loadingOption.setAdditionalCountryCodes(allCountryCodes);
        }
        return this.loadingOption.getCountryCodes();
    }

    private CountryBoundaryMap getCountryBoundaryMap()
    {
        return this.loadingOption.getCountryBoundaryMap();
    }

    private Set<org.locationtech.jts.geom.Polygon> getIntersectingBoundaryPolygons(
            final Geometry targetGeometry)
    {
        return this.getCountryBoundaryMap().query(targetGeometry.getEnvelopeInternal()).stream()
                .distinct().collect(Collectors.toSet());
    }

    private AtlasEntity getStagedEntityForMember(final RelationMember member)
    {
        final long identifier = member.getEntity().getIdentifier();
        if (member.getEntity() instanceof Point)
        {
            if (this.stagedPoints.containsKey(identifier))
            {
                return this.stagedPoints.get(identifier);
            }
            return null;
        }
        else if (member.getEntity() instanceof Line)
        {
            if (this.stagedLines.containsKey(identifier))
            {
                return this.stagedLines.get(identifier);
            }
            return null;
        }
        else
        {
            if (this.stagedRelations.containsKey(identifier))
            {
                return this.stagedRelations.get(identifier);
            }
            return null;
        }
    }

    /**
     * Determines if the given raw atlas {@link Line} qualifies to be an {@link Edge} in the final
     * atlas. Relies on the underlying {@link AtlasLoadingOption} configuration to make the
     * decision.
     *
     * @param line
     *            The {@link Line} to check
     * @return {@code true} if the given raw atlas {@link Line} qualifies to be an {@link Edge} in
     *         the final atlas.
     */
    private boolean isAtlasEdge(final Line line)
    {
        return this.loadingOption.getEdgeFilter().test(line);
    }

    private boolean isInsideWorkingBound(final AtlasEntity entity)
    {
        final Optional<String> countryCodes = entity.getTag(ISOCountryTag.KEY);
        if (countryCodes.isPresent() && this.getCountries() != null
                && !this.getCountries().isEmpty())
        {
            for (final String countryCode : countryCodes.get()
                    .split(ISOCountryTag.COUNTRY_DELIMITER))
            {
                if (this.getCountries().contains(countryCode))
                {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isMultipolygonMember(final Line line)
    {
        return line.relations().stream().anyMatch(this.relationPredicate::test);
    }

    private boolean isSignificantGeometry(final Geometry geometry)
    {
        return geometry.isValid() && (geometry.getDimension() == 1
                && geometry.getLength() > CountryBoundaryMap.LINE_BUFFER
                || geometry.getDimension() == 2
                        && geometry.getArea() > CountryBoundaryMap.AREA_BUFFER);
    }

    /**
     * Given a slice {@link Geometry}, create a {@link PolyLine} out of it, ensuring that all
     * {@link Location}s along the {@link PolyLine} have either {@link Point}s in the original
     * {@link Atlas}, {@link Point}s made by other slices, or new {@link Point}s using
     * {@link FeatureChange}s. Additionally, JTS frequently reverses geometry winding during the
     * slice operation, so the returned {@link PolyLine} will be checked to ensure its winding
     * matches the source entity's winding.
     *
     * @param slice
     *            The slice {@link Geometry} to make a new {@link Line} for
     * @param pointIdentifierFactory
     *            A {@link PointIdentifierFactory} for generating new {@link Point} identifiers if
     *            needed
     * @param line
     *            The {@link Line} being sliced
     * @param atlas
     *            The {@link Atlas} being sliced
     * @param lineChanges
     *            The {@link ChangeBuilder} tracking {@link Line} slicing changes
     * @return A {@link PolyLine} representing the slice
     */

    private PolyLine processSlice(final Geometry slice, final Line line)
    {
        PolyLine polylineForSlice;
        if (slice instanceof LineString)
        {
            polylineForSlice = JTS_POLYLINE_CONVERTER.backwardConvert((LineString) slice);
        }
        else if (slice instanceof org.locationtech.jts.geom.Polygon)
        {
            polylineForSlice = JTS_POLYLINE_CONVERTER
                    .backwardConvert(((org.locationtech.jts.geom.Polygon) slice).getExteriorRing());
        }
        else
        {
            throw new CoreException("Unexpected geometry when slicing line {}",
                    line.getIdentifier());
        }

        // JTS frequently reverses the winding during slicing-- this checks the winding of the
        // original geometry, and reverse the winding of the slice if needed
        if (line.isClosed() && slice instanceof org.locationtech.jts.geom.Polygon)
        {
            final boolean originalClockwise = new Polygon(line.asPolyLine().truncate(0, 1))
                    .isClockwise();
            final boolean sliceClockwise = new Polygon(polylineForSlice.truncate(0, 1))
                    .isClockwise();
            if (originalClockwise != sliceClockwise)
            {
                polylineForSlice = polylineForSlice.reversed();
            }
        }
        return polylineForSlice;
    }

    private void purgeInvalidMultiPolygonMembers(final CompleteRelation relation)
    {
        relation.membersMatching(member -> member.getEntity().getType() != ItemType.LINE
                || !(member.getRole().equals(RelationTypeTag.MULTIPOLYGON_ROLE_OUTER)
                        || member.getRole().equals(RelationTypeTag.MULTIPOLYGON_ROLE_INNER)))
                .forEach(invalidMember ->
                {
                    final long identifier = invalidMember.getEntity().getIdentifier();
                    logger.warn("Purging invalid member {} from relation {}", invalidMember,
                            relation.getIdentifier());
                    relation.withRemovedMember(invalidMember.getEntity());
                    if (invalidMember.getEntity().getType().equals(ItemType.LINE))
                    {
                        this.stagedLines.get(identifier)
                                .withRemovedRelationIdentifier(relation.getIdentifier());
                    }
                    else if (invalidMember.getEntity().getType().equals(ItemType.POINT))
                    {
                        this.stagedPoints.get(identifier)
                                .withRemovedRelationIdentifier(relation.getIdentifier());
                    }
                    else
                    {
                        if (this.stagedRelations.containsKey(identifier))
                        {
                            this.stagedRelations.get(identifier)
                                    .withRemovedRelationIdentifier(relation.getIdentifier());
                        }
                        else if (this.splitRelations.containsKey(identifier))
                        {
                            this.splitRelations.get(identifier).values().forEach(
                                    childRelation -> childRelation.withRemovedRelationIdentifier(
                                            relation.getIdentifier()));
                        }
                    }
                });
    }

    private void removeLine(final Line line)
    {
        final CompleteLine removedLine = this.stagedLines.remove(line.getIdentifier());
        this.changes.add(FeatureChange.remove(removedLine, this.startingAtlas));
        removedLine.relationIdentifiers().forEach(relationIdentifier ->
        {
            if (this.stagedRelations.containsKey(relationIdentifier))
            {
                this.stagedRelations.get(relationIdentifier).withRemovedMember(removedLine);
            }
        });
    }

    private void sliceArea(final Line line)
    {
        final Time time = Time.now();
        final org.locationtech.jts.geom.Polygon jtsPolygon = JTS_POLYGON_CONVERTER
                .convert(new Polygon(line.asPolyLine()));
        final Set<org.locationtech.jts.geom.Polygon> intersectingBoundaryPolygons = getIntersectingBoundaryPolygons(
                jtsPolygon);
        if (intersectingBoundaryPolygons.size() == 1
                || CountryBoundaryMap.isSameCountry(intersectingBoundaryPolygons))
        {
            final String countryCode = CountryBoundaryMap.getGeometryProperty(
                    intersectingBoundaryPolygons.iterator().next(), ISOCountryTag.KEY);
            this.stagedLines.get(line.getIdentifier()).withAddedTag(ISOCountryTag.KEY, countryCode);
            if (!this.isInCountry.test(this.stagedLines.get(line.getIdentifier())))
            {
                removeLine(line);
            }
            return;
        }

        // only check this once we know it's likely to be sliced
        if (jtsPolygon.isEmpty() || !jtsPolygon.isValid())
        {
            logger.error("Way {} had invalid geometry, removing from Atlas {}!",
                    line.getIdentifier(), this.shardOrAtlasName);
            removeLine(line);
            return;
        }
        final SortedMap<String, Set<org.locationtech.jts.geom.Polygon>> slices = slicePolygonGeometry(
                jtsPolygon, intersectingBoundaryPolygons);
        if (slices.isEmpty())
        {
            logger.warn("Way {} had empty sliced geometry!", line.getIdentifier());
            this.stagedLines.get(line.getIdentifier()).withTags(line.getTags())
                    .withAddedTag(ISOCountryTag.KEY, ISOCountryTag.COUNTRY_MISSING);
            return;
        }
        else if (slices.keySet().size() == 1)
        {
            final String countryCode = slices.keySet().iterator().next();
            this.stagedLines.get(line.getIdentifier()).withAddedTag(ISOCountryTag.KEY, countryCode);
            if (!this.isInCountry.test(this.stagedLines.get(line.getIdentifier())))
            {
                removeLine(line);
            }
            return;
        }
        else if (slices.values().size() > AbstractIdentifierFactory.IDENTIFIER_SCALE_DEFAULT)
        {
            logger.error(
                    "Country slicing exceeded maximum line identifier name space of {} for Line {} for Atlas {}. It will be added as is, with two or more country codes.",
                    AbstractIdentifierFactory.IDENTIFIER_SCALE_DEFAULT, line.getIdentifier(),
                    this.shardOrAtlasName);
            final String countryString = String.join(",", slices.keySet());
            this.stagedLines.get(line.getIdentifier()).withTags(line.getTags())
                    .withAddedTag(ISOCountryTag.KEY, countryString);
            return;
        }

        createNewSlicedAreas(line, slices);

        if (time.elapsedSince().isMoreThan(Duration.minutes(RELATION_SLICING_DURATION_WARN)))
        {
            logger.warn("Line {} for Atlas {} took {} to slice!", line.getIdentifier(),
                    this.shardOrAtlasName, time.elapsedSince());
        }
    }

    private Map<String, Set<org.locationtech.jts.geom.Geometry>> sliceGeometry(
            final Geometry geometry,
            final Set<org.locationtech.jts.geom.Polygon> countryBoundaryPolygons)
    {
        final Map<String, Set<org.locationtech.jts.geom.Geometry>> results = new HashMap<>();
        for (final org.locationtech.jts.geom.Polygon boundaryPolygon : countryBoundaryPolygons)
        {
            final String countryCode = CountryBoundaryMap.getGeometryProperty(boundaryPolygon,
                    ISOCountryTag.KEY);
            final IntersectionMatrix matrix = geometry.relate(boundaryPolygon);
            if (matrix.isWithin())
            {
                CountryBoundaryMap.setGeometryProperty(geometry, ISOCountryTag.KEY, countryCode);
                results.clear();
                results.put(countryCode, new HashSet<>());
                results.get(countryCode).add(geometry);
                return results;
            }
            else if (matrix.isIntersects())
            {
                if (!results.containsKey(countryCode))
                {
                    results.put(countryCode, new HashSet<>());
                }
                final Geometry clipped = GeometryPrecisionReducer.reduce(
                        geometry.intersection(boundaryPolygon),
                        JtsPrecisionManager.getPrecisionModel());
                if (clipped instanceof GeometryCollection)
                {
                    CountryBoundaryMap.geometries((GeometryCollection) clipped)
                            .filter(this::isSignificantGeometry).forEach(result ->
                            {
                                CountryBoundaryMap.setGeometryProperty(result, ISOCountryTag.KEY,
                                        countryCode);
                                results.get(countryCode).add(result);
                            });
                }
                else if (isSignificantGeometry(clipped))
                {
                    CountryBoundaryMap.setGeometryProperty(clipped, ISOCountryTag.KEY, countryCode);
                    results.get(countryCode).add(clipped);
                }
            }
        }
        return results;
    }

    /**
     * Converts the given {@link Line} to a JTS {@link Geometry} and slices it. If the results of
     * slicing are empty, or are from the same country, simply updates the original geometry with
     * the country tag. Otherwise, calls a helper function to replace this {@link Line} with the
     * slices.
     *
     * @param line
     *            The {@link Line} to slice
     * @param atlas
     *            The {@link Atlas} being sliced
     * @param lineChanges
     *            The {@link ChangeBuilder} used to keep track of the {@link Line} slicing changes
     */
    private void sliceLine(final Line line)
    {
        final Time time = Time.now();
        final LineString jtsLine = JTS_POLYLINE_CONVERTER.convert(line.asPolyLine());

        final Set<org.locationtech.jts.geom.Polygon> intersectingBoundaryPolygons = getIntersectingBoundaryPolygons(
                jtsLine);
        if (CountryBoundaryMap.isSameCountry(intersectingBoundaryPolygons))
        {
            final String countryCode = CountryBoundaryMap.getGeometryProperty(
                    intersectingBoundaryPolygons.iterator().next(), ISOCountryTag.KEY);
            this.stagedLines.get(line.getIdentifier()).withAddedTag(ISOCountryTag.KEY, countryCode);
            if (!this.isInCountry.test(this.stagedLines.get(line.getIdentifier())))
            {
                removeLine(line);
            }
            return;
        }

        // we only want to do this validation if we're going to slice it
        if (jtsLine.isEmpty() || !jtsLine.isValid())
        {
            logger.error("Way {} had invalid geometry, removing from Atlas {}!",
                    line.getIdentifier(), this.shardOrAtlasName);
            removeLine(line);
            return;
        }
        final SortedMap<String, Set<LineString>> slices = sliceLineStringGeometry(jtsLine,
                intersectingBoundaryPolygons);
        if (slices.isEmpty())
        {
            logger.warn("Way {} had empty sliced geometry!", line.getIdentifier());
            this.stagedLines.get(line.getIdentifier()).withTags(line.getTags())
                    .withAddedTag(ISOCountryTag.KEY, ISOCountryTag.COUNTRY_MISSING);
            return;
        }
        else if (slices.keySet().size() == 1)
        {
            final String countryCode = slices.keySet().iterator().next();
            this.stagedLines.get(line.getIdentifier()).withAddedTag(ISOCountryTag.KEY, countryCode);
            if (!this.isInCountry.test(this.stagedLines.get(line.getIdentifier())))
            {
                removeLine(line);
            }
            return;
        }
        else if (slices.values().size() > AbstractIdentifierFactory.IDENTIFIER_SCALE_DEFAULT)
        {
            logger.error(
                    "Country slicing exceeded maximum line identifier name space of {} for Line {} for Atlas {}. It will be added as is, with two or more country codes.",
                    AbstractIdentifierFactory.IDENTIFIER_SCALE_DEFAULT, line.getIdentifier(),
                    this.shardOrAtlasName);
            final String countryString = String.join(",", slices.keySet());
            this.stagedLines.get(line.getIdentifier()).withTags(line.getTags())
                    .withAddedTag(ISOCountryTag.KEY, countryString);
            return;
        }

        createNewSlicedLines(line, slices);

        if (time.elapsedSince().isMoreThan(Duration.minutes(RELATION_SLICING_DURATION_WARN)))
        {
            logger.warn("Line {} for Atlas {} took {} to slice!", line.getIdentifier(),
                    this.shardOrAtlasName, time.elapsedSince());
        }
    }

    @SuppressWarnings("unchecked")
    private SortedMap<String, Set<LineString>> sliceLineStringGeometry(final LineString line,
            final Set<org.locationtech.jts.geom.Polygon> intersectingBoundaryPolygons)
    {
        final Map<String, Set<Geometry>> currentResults = sliceGeometry(line,
                intersectingBoundaryPolygons);
        final SortedMap<String, Set<org.locationtech.jts.geom.LineString>> results = new TreeMap<>();
        for (final Map.Entry<String, Set<org.locationtech.jts.geom.Geometry>> entry : currentResults
                .entrySet())
        {
            final Set<org.locationtech.jts.geom.LineString> lineSlices = new HashSet<>();
            final LineMerger lineMerger = new LineMerger();
            entry.getValue().stream()
                    .filter(polygon -> polygon instanceof org.locationtech.jts.geom.LineString)
                    .forEach(lineMerger::add);
            final String countryCode = entry.getKey();

            lineMerger.add(lineSlices);

            lineMerger.getMergedLineStrings().forEach(mergedLineSlice ->
            {
                if (mergedLineSlice instanceof LineString)
                {
                    lineSlices.add((LineString) mergedLineSlice);
                }
            });

            results.put(countryCode, lineSlices);
        }
        return results;
    }

    /**
     * Clips the the given geometry along the boundary.
     *
     * @param identifier
     *            The identifier of the feature we are clipping
     * @param geometry
     *            The {@link Polygon} we are clipping
     * @return The {@link LineString}s making up the clipped geometry
     * @throws TopologyException
     *             Indicating a slicing error
     */
    private SortedMap<String, org.locationtech.jts.geom.MultiPolygon> sliceMultiPolygonGeometry(
            final long identifier, final org.locationtech.jts.geom.MultiPolygon geometry,
            final Set<org.locationtech.jts.geom.Polygon> intersectingBoundaryPolygons)
    {

        final Map<String, Set<org.locationtech.jts.geom.Geometry>> currentResults = sliceGeometry(
                geometry, intersectingBoundaryPolygons);
        final SortedMap<String, org.locationtech.jts.geom.MultiPolygon> results = new TreeMap<>();
        for (final Map.Entry<String, Set<org.locationtech.jts.geom.Geometry>> entry : currentResults
                .entrySet())
        {
            final Set<org.locationtech.jts.geom.Polygon> polygonClippings = new HashSet<>();
            entry.getValue().stream()
                    .filter(polygon -> polygon instanceof org.locationtech.jts.geom.Polygon)
                    .forEach(polygon -> polygonClippings
                            .add((org.locationtech.jts.geom.Polygon) polygon));
            final String countryCode = entry.getKey();
            final org.locationtech.jts.geom.MultiPolygon multipolygon = new org.locationtech.jts.geom.MultiPolygon(
                    polygonClippings.toArray(
                            new org.locationtech.jts.geom.Polygon[polygonClippings.size()]),
                    JtsPrecisionManager.getGeometryFactory());
            if (multipolygon.isEmpty() || !multipolygon.isValid())
            {
                logger.warn("Relation {} sliced for country {} produced invalid geometry!",
                        identifier, countryCode);
            }
            else
            {
                results.put(countryCode, multipolygon);
            }
        }

        return results;
    }

    /**
     * When this is called, all lines (closed and non-closed) crossing country boundaries will have
     * been sliced. In the pre-process step, we want to handle a couple of scenarios. The first one
     * is when a multipolygon relation is made up of distinct, non-closed lines tied together with a
     * Relation. It's possible that after line slicing, we may need to close some of the gaps that
     * formed along the border. Another scenario we need to handle is to look at all closed members
     * and identify any that need to be merged.
     *
     * @param relation
     *            The {@link Relation} to pre-process
     */
    private void sliceMultiPolygonRelation(final CompleteRelation relation)
    {
        final Time time = Time.now();

        purgeInvalidMultiPolygonMembers(relation);
        final org.locationtech.jts.geom.MultiPolygon jtsMp;
        try
        {
            final MultiPolygon multipolygon = RELATION_TO_MULTIPOLYGON_CONVERTER
                    .convert(this.startingAtlas.relation(relation.getIdentifier()));
            jtsMp = JTS_MULTIPOLYGON_CONVERTER.backwardConvert(multipolygon);
        }
        catch (final CoreException exception)
        {
            logger.error(
                    "Malformed polygon for relation {}! Falling back to line slicing for relation",
                    relation.getIdentifier());
            return;
        }

        // Check to see if the relation is one country only, short circuit if it is
        final Set<org.locationtech.jts.geom.Polygon> polygons = this.getCountryBoundaryMap()
                .query(jtsMp.getEnvelopeInternal()).stream().distinct().collect(Collectors.toSet());

        if (CountryBoundaryMap.isSameCountry(polygons))
        {
            final String country = CountryBoundaryMap
                    .getGeometryProperty(polygons.iterator().next(), ISOCountryTag.KEY);
            // just tag with the country code and move on, no slicing needed
            relation.withAddedTag(ISOCountryTag.KEY, country);
            return;
        }

        if (!jtsMp.isValid())
        {
            logger.error(
                    "Malformed polygon for relation {}! Falling back to line slicing for relation",
                    relation.getIdentifier());
            return;
        }

        final SortedMap<String, org.locationtech.jts.geom.MultiPolygon> clippedMultiPolygons = sliceMultiPolygonGeometry(
                relation.getIdentifier(), jtsMp, polygons);

        if (clippedMultiPolygons.isEmpty())
        {
            logger.error("Falling back to line slicing for relation {} with bad geometry",
                    relation.getIdentifier());
            return;
        }

        for (final Map.Entry<String, org.locationtech.jts.geom.MultiPolygon> entry : clippedMultiPolygons
                .entrySet())
        {
            final String country = entry.getKey();
            final org.locationtech.jts.geom.MultiPolygon countryMultipolygon = entry.getValue();
            final IsValidOp countryValid = new IsValidOp(countryMultipolygon);
            countryValid.setSelfTouchingRingFormingHoleValid(false);
            if (!countryValid.isValid())
            {
                logger.error("Falling back to line slicing for relation {} with bad geometry",
                        relation.getIdentifier());
                return;
            }
            else if (countryMultipolygon.equals(jtsMp))
            {
                // just tag with the country code and move on, no slicing needed
                relation.withAddedTag(ISOCountryTag.KEY, country);
                return;
            }
        }

        // because this is a SortedSet, iterating over the keys guarantees that we will split our
        // relation into a deterministic identifier each time. note that this is why we put all
        // countries that this relation spans into the key set for the map, even if it's associated
        // with empty polygons. that way, if a relation spans country A and country B, country A
        // will get 001000 and country B will get 002000, no matter what the country parameter for
        // slicing is
        final CountrySlicingIdentifierFactory relationIdentifierFactory = new CountrySlicingIdentifierFactory(
                relation.getIdentifier());
        final List<Long> newRelationIds = new ArrayList<>();
        final Map<String, CompleteRelation> newRelations = new HashMap<>();
        clippedMultiPolygons.keySet().forEach(countryCode ->
        {
            final long newRelationId = relationIdentifierFactory.nextIdentifier();
            newRelationIds.add(newRelationId);
            if (this.getCountries().contains(countryCode))
            {
                final CompleteRelation newRelation = CompleteRelation.shallowFrom(relation);
                newRelation.withTags(relation.getTags());
                newRelation.withAddedTag(ISOCountryTag.KEY, countryCode);
                newRelation.withIdentifier(newRelationId);
                newRelation.withRelationIdentifiers(relation.relationIdentifiers());
                addCountryMembersToSplitRelation(newRelation, relation);
                createSyntheticRelationMembers(newRelation, clippedMultiPolygons.get(countryCode));
                final SortedSet<String> syntheticIds = new TreeSet<>();
                if (!syntheticIds.isEmpty())
                {
                    newRelation.withAddedTag(SyntheticRelationMemberAdded.KEY,
                            SyntheticRelationMemberAdded.join(syntheticIds));
                }
                newRelations.put(countryCode, newRelation);
            }
        });

        final HashMap<String, CompleteRelation> relationByCountry = new HashMap<>();
        newRelations.values().forEach(newRelation ->
        {
            // update so each split relation knows about the other split relations
            newRelation.withAllRelationsWithSameOsmIdentifier(newRelationIds);
            relationByCountry.put(newRelation.getTag(ISOCountryTag.KEY).get(), newRelation);
            this.stagedRelations.put(newRelation.getIdentifier(), newRelation);
        });

        // remove the old relation
        this.changes.add(FeatureChange.remove(relation, this.startingAtlas));
        this.stagedRelations.remove(relation.getIdentifier());
        this.splitRelations.put(relation.getIdentifier(), relationByCountry);
        if (time.elapsedSince().isMoreThan(Duration.minutes(RELATION_SLICING_DURATION_WARN)))
        {
            logger.warn("Relation {} for Atlas {} took {} to slice!", relation.getIdentifier(),
                    this.shardOrAtlasName, time.elapsedSince());
        }
    }

    /**
     * Tag all {@link Point}s in the {@link Atlas} with country codes by making
     * {@link FeatureChange}s, then rebuilding using {@link ChangeAtlas}. Should no {@link Point}s
     * need tagging, return the original {@link Atlas}.
     *
     * @param rawAtlas
     *            A raw {@link Atlas} to be point-tagged
     */
    private void slicePoint(final Point point)
    {
        if (point.getOsmTags().isEmpty()
                && !Iterables.stream(this.startingAtlas.linesContaining(point.getLocation()))
                        .anyMatch(this::isAtlasEdge))
        {
            // we care about a point if and only if it has pre-existing OSM tags OR it belongs
            // to a future edge
            this.stagedPoints.remove(point.getIdentifier());
            this.changes.add(FeatureChange.remove(CompletePoint.shallowFrom(point)));
        }
        else
        {
            final CompletePoint updatedPoint = this.stagedPoints.get(point.getIdentifier());
            createPointTags(point.getLocation(), true).forEach(updatedPoint::withAddedTag);
            if (!this.isInCountry.test(updatedPoint))
            {
                this.stagedPoints.remove(point.getIdentifier());
                this.changes.add(FeatureChange.remove(updatedPoint, this.startingAtlas));
            }
        }
    }

    private SortedMap<String, Set<org.locationtech.jts.geom.Polygon>> slicePolygonGeometry(
            final org.locationtech.jts.geom.Polygon polygon,
            final Set<org.locationtech.jts.geom.Polygon> intersectingBoundaryPolygon)
    {
        final Map<String, Set<Geometry>> currentResults = sliceGeometry(polygon,
                intersectingBoundaryPolygon);

        final SortedMap<String, Set<org.locationtech.jts.geom.Polygon>> results = new TreeMap<>();
        for (final Map.Entry<String, Set<Geometry>> entry : currentResults.entrySet())
        {
            final String countryCode = entry.getKey();
            final Set<org.locationtech.jts.geom.Polygon> slicedPolygons = new HashSet<>();
            entry.getValue().forEach(geometry ->
            {
                if (geometry instanceof org.locationtech.jts.geom.Polygon)
                {
                    if (((org.locationtech.jts.geom.Polygon) geometry).getNumInteriorRing() > 0)
                    {
                        logger.error("Discarding multipolygon slicing result");
                    }
                    else
                    {
                        slicedPolygons.add((org.locationtech.jts.geom.Polygon) geometry);
                    }
                }
            });
            results.put(countryCode, slicedPolygons);
        }
        return results;
    }
}
