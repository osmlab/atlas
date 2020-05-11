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
import org.locationtech.jts.geom.TopologyException;
import org.locationtech.jts.operation.linemerge.LineMerger;
import org.locationtech.jts.operation.overlay.snap.SnapOverlayOp;
import org.locationtech.jts.precision.GeometryPrecisionReducer;
import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.MultiPolygon;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.Polygon;
import org.openstreetmap.atlas.geography.Rectangle;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.change.ChangeAtlas;
import org.openstreetmap.atlas.geography.atlas.change.ChangeBuilder;
import org.openstreetmap.atlas.geography.atlas.change.FeatureChange;
import org.openstreetmap.atlas.geography.atlas.complete.CompleteArea;
import org.openstreetmap.atlas.geography.atlas.complete.CompleteLine;
import org.openstreetmap.atlas.geography.atlas.complete.CompletePoint;
import org.openstreetmap.atlas.geography.atlas.complete.CompleteRelation;
import org.openstreetmap.atlas.geography.atlas.dynamic.DynamicAtlas;
import org.openstreetmap.atlas.geography.atlas.dynamic.policy.DynamicAtlasPolicy;
import org.openstreetmap.atlas.geography.atlas.items.Area;
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
import org.openstreetmap.atlas.tags.SyntheticRelationMemberAdded;
import org.openstreetmap.atlas.tags.annotations.validation.Validators;
import org.openstreetmap.atlas.utilities.collections.Iterables;
import org.openstreetmap.atlas.utilities.identifiers.EntityIdentifierGenerator;
import org.openstreetmap.atlas.utilities.scalars.Duration;
import org.openstreetmap.atlas.utilities.time.Time;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Takes a raw Atlas (i.e. only Points, Lines, and Relations, all lacking country code tags) and
 * "slices" it
 *
 * @author samg
 */
public class RawAtlasSlicer
{
    // JTS converters
    private static final JtsPolygonConverter JTS_POLYGON_CONVERTER = new JtsPolygonConverter();
    private static final JtsPolyLineConverter JTS_POLYLINE_CONVERTER = new JtsPolyLineConverter();
    private static final JtsMultiPolygonToMultiPolygonConverter JTS_MULTIPOLYGON_CONVERTER = new JtsMultiPolygonToMultiPolygonConverter();
    private static final RelationOrAreaToMultiPolygonConverter RELATION_TO_MULTIPOLYGON_CONVERTER = new RelationOrAreaToMultiPolygonConverter();
    private static final Logger logger = LoggerFactory.getLogger(RawAtlasSlicer.class);

    private static final long SLICING_DURATION_WARN = 10;

    // Logging constants
    private static final String MULTIPOLYGON_RELATION_SLICING_NOT_NEEDED = "Relation {} for Atlas {} had no sliced members and therefore does not need to be sliced!";
    private static final String MULTIPOLYGON_RELATION_EXCEPTION_CREATING_POLYGON = "Relation {} for Atlas {} could not be constructed into valid multipolygon!";
    private static final String MULTIPOLYGON_RELATION_INVALID_GEOMETRY = "Relation {} for Atlas {} had invalid multipolygon geometry {}!";
    private static final String MULTIPOLYGON_RELATION_HAD_NO_SLICED_GEOMETRY = "Relation {} for Atlas {} had no valid sliced geometry!";
    private static final String MULTIPOLYGON_RELATION_HAD_EQUIVALENT_SLICED_GEOMETRY = "Relation {} for Atlas {} had sliced geometry equal to original geometry, not slicing!";
    private static final String MULTIPOLYGON_RELATION_ENTIRELY_SYNTHETIC_GEOMETRY = "Relation {} for Atlas {} created using entirely synthetic members!";
    private static final String MULTIPOLYGON_RELATION_SLICING_DURATION_EXCEEDED = "Relation {} for Atlas {} took {} to slice!";
    private static final String MULTIPOLYGON_RELATION_TOPOLOGY_EXCEPTION = "Topology exception while using snap overlay operation on relation {} for Atlas {}, falling back to regular intersection";
    private static final String MULTIPOLYGON_RELATION_INVALID_MEMBER_REMOVED = "Purging invalid member {} from relation {}";
    private static final String MULTIPOLYGON_RELATION_INVALID_SLICED_GEOMETRY = "Relation {} sliced for country {} produced invalid geometry {}!";

    private static final String LINE_HAD_MULTIPOLYGON_SLICE = "Line {} for Atlas {} had multipolygon slicing result {}, discarding as cannot be represented in an Area";
    private static final String LINE_SLICING_DURATION_EXCEEDED = "Line {} for Atlas {} took {} to slice!";
    private static final String LINE_HAD_INVALID_GEOMETRY = "Line {} for Atlas {} had invalid geometry {}, removing instead of slicing!";
    private static final String LINE_EXCEEDED_SLICING_IDENTIFIER_SPACE = "Country slicing exceeded maximum line identifier name space of {} for line {} for Atlas {}. It will be added as is, with two or more country codes";

    private static final String STARTED_SLICING = "Starting slicing for Atlas {}";
    private static final String FINISHED_SLICING = "Finished slicing for Atlas {} in {}";
    private static final String STARTED_LINE_SLICING = "Starting line slicing for Atlas {}";
    private static final String FINISHED_LINE_SLICING = "Finished line slicing for Atlas {} in {}";
    private static final String STARTED_RELATION_SLICING = "Starting relation slicing for Atlas {}";
    private static final String FINISHED_RELATION_SLICING = "Finished relation slicing for Atlas {} in {}";
    private static final String STARTED_POINT_SLICING = "Starting point slicing for Atlas {}";
    private static final String FINISHED_POINT_SLICING = "Finished point slicing for Atlas {} in {}";
    private static final String STARTED_RELATION_FILTERING = "Starting relation filtering for Atlas {}";
    private static final String FINISHED_RELATION_FILTERING = "Finished relation filtering for Atlas {} in {}";

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

    private final Map<Long, CompleteArea> stagedAreas = new HashMap<>();

    private final Predicate<AtlasEntity> isInCountry;

    /**
     * This constructor will build a RawAtlasSlicer for use on a single Atlas with no dynamic
     * expansion on Relations. Primarily for tests, please consider using the alternate constructor
     * for most use cases!
     *
     * @param loadingOption
     *            An AtlasLoadingOption with a minimum of a CountryBoundaryMap included
     * @param startingAtlas
     *            The raw Atlas to slice
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
     * This constructor will build a RawAtlasSlicer for the initial Shard given, using the provided
     * Atlas fetcher function to dynamically expand on any Relations matching the
     * relationSlicingFilter in the provided AtlasLoadingOption
     *
     * @param loadingOption
     *            An AtlasLoadingOption with a minimum of a CountryBoundaryMap included
     * @param initialShard
     *            The initial Shard for the output Atlas to slice
     * @param sharding
     *            The relevant Sharding tree
     * @param atlasFetcher
     *            A function to get an Atlas object for a given Shard
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
            throw new CoreException("Could not get data for initial shard {} during slicing!",
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

    /**
     * Calculates the changes needed to slice the Atlas, then builds a ChangeAtlas out of that and
     * cuts it down to match the boundaries of the initial shard
     *
     * @return An Atlas with only entities that lay inside the original Shard bounds and matching
     *         the country code set provided in the AtlasLoadingOption, all with ISOCountryTags
     *         properly set
     */
    public Atlas slice()
    {
        final Time overallTime = Time.now();
        Time time = Time.now();
        logger.info(STARTED_SLICING, this.shardOrAtlasName);
        logger.info(STARTED_LINE_SLICING, this.shardOrAtlasName);
        this.startingAtlas.lines().forEach(line ->
        {
            if (line.isClosed() && !isAtlasEdge(line))
            {
                sliceArea(line);
            }
            else
            {
                sliceLine(line);
            }
        });
        logger.info(FINISHED_LINE_SLICING, this.shardOrAtlasName,
                time.elapsedSince().asMilliseconds());

        time = Time.now();
        logger.info(STARTED_RELATION_SLICING, this.shardOrAtlasName);
        this.startingAtlas.relationsLowerOrderFirst().forEach(relation ->
        {
            if (this.relationPredicate.test(relation))
            {
                sliceMultiPolygonRelation(this.stagedRelations.get(relation.getIdentifier()));
            }
        });
        logger.info(FINISHED_RELATION_SLICING, this.shardOrAtlasName,
                time.elapsedSince().asMilliseconds());

        time = Time.now();
        logger.info(STARTED_POINT_SLICING, this.shardOrAtlasName);
        this.startingAtlas.points().forEach(this::slicePoint);
        logger.info(FINISHED_POINT_SLICING, this.shardOrAtlasName,
                time.elapsedSince().asMilliseconds());

        logger.info(STARTED_RELATION_FILTERING, this.shardOrAtlasName);
        this.startingAtlas.relationsLowerOrderFirst().forEach(relation ->
        {
            if (this.stagedRelations.containsKey(relation.getIdentifier())
                    && !Validators.hasValuesFor(this.stagedRelations.get(relation.getIdentifier()),
                            ISOCountryTag.class))
            {
                filterRelation(this.stagedRelations.get(relation.getIdentifier()));
            }
        });
        logger.info(FINISHED_RELATION_FILTERING, this.shardOrAtlasName,
                time.elapsedSince().asMilliseconds());

        this.stagedLines.values()
                .forEach(line -> this.changes.add(FeatureChange.add(line, this.startingAtlas)));
        this.stagedPoints.values()
                .forEach(point -> this.changes.add(FeatureChange.add(point, this.startingAtlas)));
        this.stagedRelations.values().forEach(
                relation -> this.changes.add(FeatureChange.add(relation, this.startingAtlas)));
        this.stagedAreas.values()
                .forEach(area -> this.changes.add(FeatureChange.add(area, this.startingAtlas)));

        logger.info(FINISHED_SLICING, this.shardOrAtlasName,
                overallTime.elapsedSince().asMilliseconds());

        return cutSubAtlasForOriginalShard(new ChangeAtlas(this.startingAtlas,
                new ChangeBuilder().addAll(this.changes).get()));
    }

    /**
     * Given a new split Relation, filter its original Relation's members by the country code for
     * the new split Relation and add only them
     *
     * @param newRelation
     *            A new Relation containing a subset of members based on country code
     * @param oldRelation
     *            The original Relation for the new split Relation
     */
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

    /**
     * Given a Line and its slice segment, ensure that any new coordinates where the Line has been
     * split either have an existing Point that has an updated SyntheticBoundaryNodeTag.EXISTING
     * value, or a new Point is created with the SyntheticBoundaryNodeTag.NEW value
     *
     * @param line
     *            A Line being sliced that meets the criteria for an Edge (see isAtlasEdge method)
     * @param slice
     *            A slice for that Line
     */
    private void addSyntheticBoundaryNodesForSlice(final Line line, final PolyLine slice)
    {
        if (!slice.first().equals(line.asPolyLine().first()))
        {
            final Iterable<Point> pointsAtFirstLocation = this.startingAtlas
                    .pointsAt(slice.first());
            if (Iterables.isEmpty(pointsAtFirstLocation))
            {
                final EntityIdentifierGenerator pointIdentifierGenerator = new EntityIdentifierGenerator();
                final SortedSet<String> countries = new TreeSet<>();
                final Map<String, String> tags = new HashMap<>();
                countries.addAll(
                        Arrays.asList(getCountryBoundaryMap().getCountryCodeISO3(slice.first())
                                .getIso3CountryCode().split(ISOCountryTag.COUNTRY_DELIMITER)));
                tags.put(ISOCountryTag.KEY,
                        String.join(ISOCountryTag.COUNTRY_DELIMITER, countries));
                tags.put(SyntheticBoundaryNodeTag.KEY, SyntheticBoundaryNodeTag.YES.toString());
                final CompletePoint syntheticBoundaryNode = new CompletePoint(1L, slice.first(),
                        tags, new HashSet<>());
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
                final SortedSet<String> countries = new TreeSet<>();
                final Map<String, String> tags = new HashMap<>();
                countries.addAll(
                        Arrays.asList(getCountryBoundaryMap().getCountryCodeISO3(slice.last())
                                .getIso3CountryCode().split(ISOCountryTag.COUNTRY_DELIMITER)));
                tags.put(ISOCountryTag.KEY,
                        String.join(ISOCountryTag.COUNTRY_DELIMITER, countries));
                tags.put(SyntheticBoundaryNodeTag.KEY, SyntheticBoundaryNodeTag.YES.toString());
                final CompletePoint syntheticBoundaryNode = new CompletePoint(1L, slice.last(),
                        tags, new HashSet<>());
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

    /**
     * Given a remainder geometry for a sliced multipolygon Relation, add in new Lines to the Atlas
     *
     * @param newRelation
     *            A new sliced Relation
     * @param remainder
     *            Geometry not covered by this new Relation's existing sliced Line members
     * @param role
     *            The role for this geometry in the Relation
     * @return A Set of ids for the newly added Lines, used to updated the
     *         SyntheticRelationMemberAdded tag in the new Relation
     */
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
        final DynamicAtlasPolicy policy = new DynamicAtlasPolicy(atlasFetcher, sharding,
                initialShard, Rectangle.MAXIMUM).withDeferredLoading(true)
                        .withExtendIndefinitely(true)
                        .withAtlasEntitiesToConsiderForExpansion(expandPredicate);

        final DynamicAtlas atlas = new DynamicAtlas(policy);
        atlas.preemptiveLoad();
        return atlas;
    }

    /**
     * Given a Line that will be a future Area and its slices, create the new Line entities and put
     * them in the stagedLines map
     *
     * @param line
     *            The Line being sliced as an Area
     * @param slices
     *            The map representing the portions of its geometry in each country, mapped to
     *            country code
     */
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
                final Polygon newLineGeometry = new Polygon(processSlice(slice, line));
                final Map<String, String> lineTags = line.getTags();
                lineTags.put(ISOCountryTag.KEY, countryCode);
                final Set<Long> relationIds = new HashSet<>();
                line.relations().forEach(relation -> relationIds.add(relation.getIdentifier()));
                final CompleteArea newAreaSlice = new CompleteArea(lineSliceIdentifier,
                        newLineGeometry, lineTags, relationIds);
                if (isInsideWorkingBound(newAreaSlice))
                {
                    this.stagedAreas.put(newAreaSlice.getIdentifier(), newAreaSlice);
                    for (final Relation relation : line.relations())
                    {
                        if (this.stagedRelations.containsKey(relation.getIdentifier()))
                        {
                            this.stagedRelations.get(relation.getIdentifier())
                                    .withAddedMember(newAreaSlice, line);
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

    /**
     * Given a non-Area Line and its slices, create the new Line entities and put them in the
     * stagedLines map
     *
     * @param line
     *            The Line being sliced
     * @param slices
     *            The map representing the portions of its geometry in each country, mapped to
     *            country code
     */
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

    /**
     * Takes a split multipolygon child Relation and its sliced geometry, subtracts out the existing
     * Line members, then adds in the remainder as new Lines
     *
     * @param newRelation
     *            The split multipolygon Relation
     * @param newMultiPolygon
     *            The sliced MultiPolygon geometry for that Relation
     */
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
                logger.warn(MULTIPOLYGON_RELATION_ENTIRELY_SYNTHETIC_GEOMETRY,
                        newRelation.getIdentifier(), this.shardOrAtlasName);
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

    /**
     * Takes a new split multipolygon Relation and an outer geometry and cuts out any overlapping
     * sliced Line members
     *
     * @param newRelation
     *            The split multipolygon Relation
     * @param slicedPolygonOuter
     *            One of its outer ring geometries
     * @return The remainder after all overlapping members have been "cut" out from the
     *         slicedPolygonOuter geometry
     */
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
                    logger.error(MULTIPOLYGON_RELATION_TOPOLOGY_EXCEPTION,
                            newRelation.getIdentifier(), this.shardOrAtlasName);
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

    /**
     * For any Relation that we can't slice (non-multipolygon, didn't meet the Relation predicate
     * criteria, or bad geometry), instead filter out any members that aren't in the country code
     * set, and update the ISOCountryTag for the Relation
     *
     * @param relation
     */
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
                throw new CoreException(
                        "Untagged country value for entity {} for relation {} for Atlas {}",
                        stagedRelationMember, relation.getIdentifier(), this.shardOrAtlasName);

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

    /**
     * Returns the country code set being sliced
     *
     * @return The set of country codes to slice
     */
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

    /**
     * Returns the country boundary map from the AtlasLoadingOption
     *
     * @return The country boundary map from the AtlasLoadingOption
     */
    private CountryBoundaryMap getCountryBoundaryMap()
    {
        return this.loadingOption.getCountryBoundaryMap();
    }

    /**
     * Given a JTS geometry, return all Polygons from the country boundary map that intersect its
     * internal envelope
     *
     * @param targetGeometry
     *            The geometry being queried
     * @return All Polygons from the country boundary map that intersect its internal envelope
     */
    private Set<org.locationtech.jts.geom.Polygon> getIntersectingBoundaryPolygons(
            final Geometry targetGeometry)
    {
        return this.getCountryBoundaryMap().query(targetGeometry.getEnvelopeInternal()).stream()
                .distinct().collect(Collectors.toSet());
    }

    /**
     * Given a RelationMember, find its staged CompleteEntity
     *
     * @param member
     *            A RelationMember to find
     * @return Its staged CompleteEntity
     */
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
        else if (member.getEntity() instanceof Area)
        {
            if (this.stagedAreas.containsKey(identifier))
            {
                return this.stagedAreas.get(identifier);
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

    /**
     * Checks to see if an entity is inside the working set of country codes
     *
     * @param entity
     *            The entity to check
     * @return True if that entity is inside a country included in the set of country codes being
     *         sliced, false otherwise
     */
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

    /**
     * Check to see if a Line is a member of a Relation that meets the Relation predicate-- if so,
     * we want to slice it as a linear feature even if it's closed
     *
     * @param line
     *            The Line to check
     * @return True if any Relations it belongs to meet the Relation predicate
     */
    private boolean isMultipolygonMember(final Line line)
    {
        return line.relations().stream().anyMatch(this.relationPredicate::test);
    }

    /**
     * A filter to ensure trivial pieces of geometry aren't preserved from the slicing operation
     *
     * @param geometry
     *            The geometry to check
     * @return True if the geometry is valid and larger than either the
     *         CountryBoundaryMap.LINE_BUFFER or CountryBoundaryMap.AREA_BUFFER
     */
    private boolean isSignificantGeometry(final Geometry geometry)
    {
        if (!geometry.isValid() && logger.isWarnEnabled())
        {
            logger.warn("Found invalid geometry {} during slicing Atlas {}", geometry.toText(),
                    this.shardOrAtlasName);
        }
        return geometry.isValid() && (geometry.getDimension() == 1
                && geometry.getLength() > CountryBoundaryMap.LINE_BUFFER
                || geometry.getDimension() == 2
                        && geometry.getArea() > CountryBoundaryMap.AREA_BUFFER);
    }

    /**
     * Given a slice for a Line entity, construct an appropriate PolyLine for the new sliced Line
     * member. In the case of a Polygon (i.e. Area), the PolyLine will be constructed such that the
     * winding represents the winding of the original entity
     *
     * @param slice
     *            The slice for a Line entity
     * @param line
     *            The Line being sliced
     * @return The best PolyLine to represent that slice for the Line
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
            throw new CoreException("Unexpected geometry when slicing line {} for Atlas {}",
                    line.getOsmIdentifier(), this.shardOrAtlasName);
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

    /**
     * Given a multipolygon Relation, purge any members that don't meet the criteria in the OSM
     * specification
     *
     * @param relation
     *            The Relation to check
     */
    private void purgeInvalidMultiPolygonMembers(final CompleteRelation relation)
    {
        relation.membersMatching(member -> member.getEntity().getType() != ItemType.LINE
                || !(member.getRole().equals(RelationTypeTag.MULTIPOLYGON_ROLE_OUTER)
                        || member.getRole().equals(RelationTypeTag.MULTIPOLYGON_ROLE_INNER)))
                .forEach(invalidMember ->
                {
                    final long identifier = invalidMember.getEntity().getIdentifier();
                    logger.warn(MULTIPOLYGON_RELATION_INVALID_MEMBER_REMOVED, invalidMember,
                            relation.getOsmIdentifier());
                    relation.withRemovedMember(invalidMember.getEntity());
                    if (invalidMember.getEntity().getType().equals(ItemType.LINE))
                    {
                        this.stagedLines.get(identifier)
                                .withRemovedRelationIdentifier(relation.getIdentifier());
                    }
                    else if (invalidMember.getEntity().getType().equals(ItemType.AREA))
                    {
                        this.stagedAreas.get(identifier)
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

    /**
     * Remove a Line from the Atlas, and remove it from any Relations that may contain it
     *
     * @param line
     *            The Line to remove
     */
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

    /**
     * Slice a Line that qualifies as an Area by converting it to 2d geometry, calculating its
     * slices, and creating the new sliced Lines. If it belongs to just one country or cannot be
     * sliced, update the ISOCountryTag appropriately instead
     *
     * @param line
     *            The Line to slice as an Area
     */
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
            if (logger.isErrorEnabled())
            {
                logger.error(LINE_HAD_INVALID_GEOMETRY, line.getOsmIdentifier(),
                        this.shardOrAtlasName, jtsPolygon.toText());
            }
            final SortedSet<String> countries = new TreeSet<>();
            intersectingBoundaryPolygons.forEach(polygon -> countries
                    .add(CountryBoundaryMap.getGeometryProperty(polygon, ISOCountryTag.KEY)));
            final String countryCodes = String.join(",", countries);
            this.stagedLines.get(line.getIdentifier()).withAddedTag(ISOCountryTag.KEY,
                    countryCodes);
            return;
        }
        final SortedMap<String, Set<org.locationtech.jts.geom.Polygon>> slices;
        try
        {
            slices = slicePolygonGeometry(line.getOsmIdentifier(), jtsPolygon,
                    intersectingBoundaryPolygons);
        }
        catch (final CoreException exception)
        {
            logger.error(
                    "Line {} for Atlas {} had multipolygon slicing result when sliced as polygon, will slice as line instead!",
                    line.getOsmIdentifier(), this.shardOrAtlasName);
            sliceLine(line);
            return;
        }
        if (slices.keySet().size() == 1)
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
            // this should be rare, but we can't slice the line if we don't have the identifier
            // space for the slices
            logger.error(LINE_EXCEEDED_SLICING_IDENTIFIER_SPACE,
                    AbstractIdentifierFactory.IDENTIFIER_SCALE_DEFAULT, line.getOsmIdentifier(),
                    this.shardOrAtlasName);
            final String countryString = String.join(",", slices.keySet());
            this.stagedLines.get(line.getIdentifier()).withTags(line.getTags())
                    .withAddedTag(ISOCountryTag.KEY, countryString);
            return;
        }

        createNewSlicedAreas(line, slices);

        if (time.elapsedSince().isMoreThan(Duration.minutes(SLICING_DURATION_WARN)))
        {
            logger.warn(LINE_SLICING_DURATION_EXCEEDED, line.getOsmIdentifier(),
                    this.shardOrAtlasName, time.elapsedSince().asMilliseconds());
        }

        // HACK ALERT UGH
        if (isMultipolygonMember(line))
        {
            // readd line to staged lines since we know we just removed it to slice it-- we only get
            // to here if we had to slice the line
            this.stagedLines.put(line.getIdentifier(), CompleteLine.from(line));
            sliceLine(line);
        }
    }

    /**
     * Given a geometry and a set of intersecting country boundary Polygons, calculate the portion
     * of that geometry contained by each boundary polygon and return those mapped to country code
     *
     * @param geometry
     *            The JTS geometry to slice
     * @param countryBoundaryPolygons
     *            All country boundary polygons intersecting it
     * @return A map of country codes to the portions of JTS geometry inside those country boundary
     *         polygons
     */
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
            // occasionally, the geometry's internal envelope will intersect multiple boundary
            // polygons but the geometry doesn't. in this case, just tag the geometry entirely
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
     * Slice a Line by converting it to 1d geometry, calculating its slices, and creating the new
     * sliced Lines. If it belongs to just one country or cannot be sliced, update the ISOCountryTag
     * appropriately instead
     *
     * @param line
     *            The Line to slice as an Line
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
            if (logger.isErrorEnabled())
            {
                logger.error(LINE_HAD_INVALID_GEOMETRY, line.getOsmIdentifier(),
                        this.shardOrAtlasName, jtsLine.toText());
            }
            final SortedSet<String> countries = new TreeSet<>();
            intersectingBoundaryPolygons.forEach(polygon -> countries
                    .add(CountryBoundaryMap.getGeometryProperty(polygon, ISOCountryTag.KEY)));
            final String countryCodes = String.join(",", countries);
            this.stagedLines.get(line.getIdentifier()).withAddedTag(ISOCountryTag.KEY,
                    countryCodes);
            return;
        }
        final SortedMap<String, Set<LineString>> slices = sliceLineStringGeometry(jtsLine,
                intersectingBoundaryPolygons);
        if (slices.keySet().size() == 1)
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
            // this should be rare, but we can't slice the line if we don't have the identifier
            // space for the slices
            logger.error(LINE_EXCEEDED_SLICING_IDENTIFIER_SPACE,
                    AbstractIdentifierFactory.IDENTIFIER_SCALE_DEFAULT, line.getOsmIdentifier(),
                    this.shardOrAtlasName);
            final String countryString = String.join(",", slices.keySet());
            this.stagedLines.get(line.getIdentifier()).withTags(line.getTags())
                    .withAddedTag(ISOCountryTag.KEY, countryString);
            return;
        }

        createNewSlicedLines(line, slices);

        if (time.elapsedSince().isMoreThan(Duration.minutes(SLICING_DURATION_WARN)))
        {
            logger.warn(LINE_SLICING_DURATION_EXCEEDED, line.getOsmIdentifier(),
                    this.shardOrAtlasName, time.elapsedSince().asMilliseconds());
        }
    }

    /**
     * Take a LineString and find its slices, also attempt to merge all LineSlices for a country
     * together to reduce artifacting
     *
     * @param line
     *            The Line being sliced
     * @param intersectingBoundaryPolygons
     *            All intersecting country boundary polygons
     * @return A SortedMap of country codes to the portions of the Line inside those country
     *         boundary polygons
     */
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
     * Take a MultiPolygon and find its slices, while also checking for validity and geometric
     * consistency (i.e. only Polygonal results)
     *
     * @param identifier
     *            The identifier for the Relation being sliced
     * @param geometry
     *            The multipolygon being sliced
     * @param intersectingBoundaryPolygons
     *            All intersecting country boundary polygons
     * @return A SortedMap of country codes to the portions of the MultiPolygon inside those country
     *         boundary polygons
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
                if (logger.isErrorEnabled())
                {
                    logger.warn(MULTIPOLYGON_RELATION_INVALID_SLICED_GEOMETRY, identifier,
                            countryCode, multipolygon.toText());
                }
            }
            else
            {
                results.put(countryCode, multipolygon);
            }
        }

        return results;
    }

    /**
     * Slice a multipolygon Relation by constructing its geometry out of the valid raw Atlas
     * members, calculating its slices, and creating the new sliced Relations with valid
     * MultiPolygon geometry. If it belongs to just one country or cannot be sliced, update the
     * ISOCountryTag appropriately instead
     *
     * @param Relation
     *            The Relation to slice as a multipolygon
     */
    private void sliceMultiPolygonRelation(final CompleteRelation relation)
    {
        final Time time = Time.now();
        purgeInvalidMultiPolygonMembers(relation);
        if (relation
                .membersMatching(member -> member.getEntity().getType().equals(ItemType.LINE)
                        && this.startingAtlas.line(member.getEntity().getIdentifier()) == null)
                .isEmpty())
        {
            logger.info(MULTIPOLYGON_RELATION_SLICING_NOT_NEEDED, relation.getOsmIdentifier(),
                    this.shardOrAtlasName);
            return;
        }
        final org.locationtech.jts.geom.MultiPolygon jtsMp;
        try
        {
            final MultiPolygon multipolygon = RELATION_TO_MULTIPOLYGON_CONVERTER
                    .convert(this.startingAtlas.relation(relation.getIdentifier()));
            jtsMp = JTS_MULTIPOLYGON_CONVERTER.backwardConvert(multipolygon);
        }
        catch (final CoreException exception)
        {
            logger.error(MULTIPOLYGON_RELATION_EXCEPTION_CREATING_POLYGON,
                    relation.getOsmIdentifier(), this.shardOrAtlasName, exception);
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
            if (logger.isErrorEnabled())
            {
                logger.error(MULTIPOLYGON_RELATION_INVALID_GEOMETRY, relation.getOsmIdentifier(),
                        this.shardOrAtlasName, jtsMp.toText());
            }
            return;
        }

        final SortedMap<String, org.locationtech.jts.geom.MultiPolygon> clippedMultiPolygons = sliceMultiPolygonGeometry(
                relation.getIdentifier(), jtsMp, polygons);

        if (clippedMultiPolygons.isEmpty())
        {
            logger.error(MULTIPOLYGON_RELATION_HAD_NO_SLICED_GEOMETRY, relation.getOsmIdentifier(),
                    this.shardOrAtlasName);
            return;
        }

        for (final Map.Entry<String, org.locationtech.jts.geom.MultiPolygon> entry : clippedMultiPolygons
                .entrySet())
        {
            final String country = entry.getKey();
            final org.locationtech.jts.geom.MultiPolygon countryMultipolygon = entry.getValue();
            if (countryMultipolygon.equals(jtsMp))
            {
                logger.info(MULTIPOLYGON_RELATION_HAD_EQUIVALENT_SLICED_GEOMETRY,
                        relation.getOsmIdentifier(), this.shardOrAtlasName);
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
        if (time.elapsedSince().isMoreThan(Duration.minutes(SLICING_DURATION_WARN)))
        {
            logger.warn(MULTIPOLYGON_RELATION_SLICING_DURATION_EXCEEDED,
                    relation.getOsmIdentifier(), this.shardOrAtlasName, time.elapsedSince());
        }
    }

    /**
     * Given a point, either remove it from the Atlas if it has no pre-existing tags and doesn't
     * belong to an Edge, OR update its ISOCountryTag value
     *
     * @param point
     *            The point to slice
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
            final SortedSet<String> countries = new TreeSet<>();
            countries.addAll(
                    Arrays.asList(getCountryBoundaryMap().getCountryCodeISO3(point.getLocation())
                            .getIso3CountryCode().split(ISOCountryTag.COUNTRY_DELIMITER)));
            updatedPoint.withAddedTag(ISOCountryTag.KEY,
                    String.join(ISOCountryTag.COUNTRY_DELIMITER, countries));
            if (countries.size() > 1)
            {
                updatedPoint.withAddedTag(SyntheticBoundaryNodeTag.KEY,
                        SyntheticBoundaryNodeTag.EXISTING.toString());
            }
            if (!this.isInCountry.test(updatedPoint))
            {
                this.stagedPoints.remove(point.getIdentifier());
                this.changes.add(FeatureChange.remove(updatedPoint, this.startingAtlas));
            }
        }
    }

    /**
     * Take a polygon and find its slices, while also checking for validity and geometric
     * consistency (i.e. only Polygonal results)
     *
     * @param identifier
     *            the OSM identifier for the Line being sliced
     * @param polygon
     *            The Polygon being sliced
     * @param intersectingBoundaryPolygons
     *            All intersecting country boundary polygons
     * @return A SortedMap of country codes to the portions of the Polygon inside those country
     *         boundary polygons
     */
    private SortedMap<String, Set<org.locationtech.jts.geom.Polygon>> slicePolygonGeometry(
            final long identifier, final org.locationtech.jts.geom.Polygon polygon,
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
                    // this can happen when a country boundary introduces a hole into a polygon that
                    // was previously simple. in this case, we *must* discard the result. we surface
                    // the log as an error so it should be easy to find
                    if (((org.locationtech.jts.geom.Polygon) geometry).getNumInteriorRing() > 0)
                    {
                        throw new CoreException(LINE_HAD_MULTIPOLYGON_SLICE, identifier,
                                this.shardOrAtlasName, geometry.toText());
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
