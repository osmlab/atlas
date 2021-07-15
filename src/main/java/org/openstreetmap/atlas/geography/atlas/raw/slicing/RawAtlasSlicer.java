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
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.TopologyException;
import org.locationtech.jts.geom.prep.PreparedGeometry;
import org.locationtech.jts.geom.prep.PreparedGeometryFactory;
import org.locationtech.jts.geom.prep.PreparedPolygon;
import org.locationtech.jts.operation.linemerge.LineMerger;
import org.locationtech.jts.operation.overlayng.OverlayNG;
import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.MultiPolygon;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.Polygon;
import org.openstreetmap.atlas.geography.Rectangle;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.AtlasMetaData;
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
import org.openstreetmap.atlas.geography.atlas.items.ItemType;
import org.openstreetmap.atlas.geography.atlas.items.Line;
import org.openstreetmap.atlas.geography.atlas.items.Point;
import org.openstreetmap.atlas.geography.atlas.items.Relation;
import org.openstreetmap.atlas.geography.atlas.items.RelationMember;
import org.openstreetmap.atlas.geography.atlas.items.RelationMemberList;
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
import org.openstreetmap.atlas.tags.SyntheticGeometrySlicedTag;
import org.openstreetmap.atlas.tags.SyntheticInvalidGeometryTag;
import org.openstreetmap.atlas.tags.SyntheticInvalidMultiPolygonRelationMembersRemovedTag;
import org.openstreetmap.atlas.tags.SyntheticRelationMemberAdded;
import org.openstreetmap.atlas.tags.SyntheticSyntheticRelationMemberTag;
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
    // Buffer values for slicing operation. If the remaining piece turns to be smaller than
    // buffer, we'll just ignore them.
    public static final double LINE_BUFFER = 0.000001;
    public static final double AREA_BUFFER = 0.000000005;
    public static final double BUFFER_PERCENTAGE = 0.005;
    public static final double PERCENTAGE = 100;

    // JTS converters
    private static final JtsPolygonConverter JTS_POLYGON_CONVERTER = new JtsPolygonConverter();
    private static final JtsPolyLineConverter JTS_POLYLINE_CONVERTER = new JtsPolyLineConverter();
    private static final JtsMultiPolygonToMultiPolygonConverter JTS_MULTIPOLYGON_CONVERTER = new JtsMultiPolygonToMultiPolygonConverter();
    private static final RelationOrAreaToMultiPolygonConverter RELATION_TO_MULTIPOLYGON_CONVERTER = new RelationOrAreaToMultiPolygonConverter(
            true);
    private static final Logger logger = LoggerFactory.getLogger(RawAtlasSlicer.class);
    private static final double MINIMUM_CONSOLIDATE_THRESHOLD = 0.8;
    private static final long SLICING_DURATION_WARN = 10;

    // Logging constants
    private static final String MULTIPOLYGON_RELATION_EXCEPTION_CREATING_POLYGON = "Relation {} for Atlas {} could not be constructed into valid multipolygon!";
    private static final String MULTIPOLYGON_RELATION_INVALID_GEOMETRY = "Relation {} for Atlas {} had invalid multipolygon geometry {}!";
    private static final String MULTIPOLYGON_RELATION_HAD_NO_SLICED_GEOMETRY = "Relation {} for Atlas {} had no valid sliced geometry!";
    private static final String MULTIPOLYGON_RELATION_HAD_EQUIVALENT_SLICED_GEOMETRY = "Relation {} for Atlas {} had sliced geometry equal to original geometry, not slicing!";
    private static final String MULTIPOLYGON_RELATION_ENTIRELY_SYNTHETIC_GEOMETRY = "Relation {} for Atlas {} created using entirely synthetic members!";
    private static final String MULTIPOLYGON_RELATION_SLICING_DURATION_EXCEEDED = "Relation {} for Atlas {} took {} to slice!";
    private static final String MULTIPOLYGON_RELATION_INVALID_MEMBER_REMOVED = "Purging invalid member {} from relation {}";
    private static final String MULTIPOLYGON_RELATION_INVALID_SLICED_GEOMETRY = "Relation {} sliced for country {} produced invalid geometry {}!";
    private static final String MULTIPOLYGON_RELATION_OVERLAPPING_INNERS = "Relation {} for Atlas {} had overlapping inners, but slicing will continue!";

    private static final String LINE_HAD_MULTIPOLYGON_SLICE = "Line {} for Atlas {} had multipolygon slicing result when sliced as polygon, will slice as line instead!";
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

    private final Atlas inputAtlas;
    private final Shard initialShard;
    private final Predicate<AtlasEntity> relationPredicate;
    private final Predicate<AtlasEntity> consolidatePredicate;
    private final Predicate<AtlasEntity> isInCountry;
    private final Predicate<AtlasEntity> isAtlasEdge;
    private final CountryBoundaryMap boundary;
    private final String shardOrAtlasName;
    private final String country;
    /** See {@link AtlasLoadingOption#isKeepAll} */
    private final boolean keepAll;
    private final Map<Long, CompleteArea> stagedAreas = new ConcurrentHashMap<>();
    private final Map<Long, CompleteRelation> stagedRelations = new ConcurrentHashMap<>();
    private final Map<Long, CompleteLine> stagedLines = new ConcurrentHashMap<>();
    private final Map<Long, CompletePoint> stagedPoints = new ConcurrentHashMap<>();

    private final Map<Long, Map<String, CompleteRelation>> splitRelations = new ConcurrentHashMap<>();
    private final Set<FeatureChange> changes = Collections
            .newSetFromMap(new ConcurrentHashMap<FeatureChange, Boolean>());
    private final Set<Long> pointsBelongingToEdge = Collections
            .newSetFromMap(new ConcurrentHashMap<Long, Boolean>());
    private final PreparedGeometryFactory preparer = new PreparedGeometryFactory();

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
        this.inputAtlas = startingAtlas;
        this.initialShard = null;
        this.country = loadingOption.getCountryCode();
        this.relationPredicate = entity -> entity.getType().equals(ItemType.RELATION)
                && loadingOption.getRelationSlicingFilter().test(entity);
        this.consolidatePredicate = entity -> entity.getType().equals(ItemType.RELATION)
                && loadingOption.getRelationSlicingConsolidateFilter().test(entity);
        if (loadingOption.getCountryCode() == null || loadingOption.getCountryCode().isEmpty())
        {
            this.isInCountry = entity -> true;
        }
        else
        {
            this.isInCountry = entity -> ISOCountryTag.isIn(loadingOption.getCountryCode())
                    .test(entity);
        }
        this.isAtlasEdge = entity -> loadingOption.getEdgeFilter().test(entity);
        this.boundary = loadingOption.getCountryBoundaryMap();
        this.shardOrAtlasName = startingAtlas.metaData().getShardName()
                .orElse(startingAtlas.getName());
        this.inputAtlas.areas().forEach(
                area -> this.stagedAreas.put(area.getIdentifier(), CompleteArea.from(area)));
        this.inputAtlas.points().forEach(
                point -> this.stagedPoints.put(point.getIdentifier(), CompletePoint.from(point)));
        this.inputAtlas.lines().forEach(
                line -> this.stagedLines.put(line.getIdentifier(), CompleteLine.from(line)));
        this.inputAtlas.relations().forEach(relation -> this.stagedRelations
                .put(relation.getIdentifier(), CompleteRelation.from(relation)));

        this.keepAll = loadingOption.isKeepAll();
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
        this.initialShard = initialShard;
        this.country = loadingOption.getCountryCode();
        this.relationPredicate = entity -> entity.getType().equals(ItemType.RELATION)
                && loadingOption.getRelationSlicingFilter().test(entity);
        this.consolidatePredicate = entity -> entity.getType().equals(ItemType.RELATION)
                && loadingOption.getRelationSlicingConsolidateFilter().test(entity);
        if (loadingOption.getCountryCode() == null || loadingOption.getCountryCode().isEmpty())
        {
            this.isInCountry = entity -> true;
        }
        else
        {
            this.isInCountry = entity -> ISOCountryTag.isIn(loadingOption.getCountryCode())
                    .test(entity);
        }
        this.boundary = loadingOption.getCountryBoundaryMap();
        this.isAtlasEdge = entity -> loadingOption.getEdgeFilter().test(entity);

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
            this.inputAtlas = subAtlasOptional.get();
        }
        else
        {
            throw new CoreException(
                    "No data after sub-atlasing to remove new relations from partially expanded Atlas {}!",
                    expandedAtlas.getName());
        }

        this.shardOrAtlasName = this.inputAtlas.metaData().getShardName()
                .orElse(this.inputAtlas.getName());
        this.inputAtlas.areas().forEach(
                area -> this.stagedAreas.put(area.getIdentifier(), CompleteArea.from(area)));
        this.inputAtlas.points().forEach(
                point -> this.stagedPoints.put(point.getIdentifier(), CompletePoint.from(point)));
        this.inputAtlas.lines().forEach(
                line -> this.stagedLines.put(line.getIdentifier(), CompleteLine.from(line)));
        this.inputAtlas.relations().forEach(relation -> this.stagedRelations
                .put(relation.getIdentifier(), CompleteRelation.from(relation)));

        this.keepAll = loadingOption.isKeepAll();
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
        this.inputAtlas.areas().forEach(this::sliceArea);

        final Set<CompleteLine> linesToSlice = new HashSet<>();
        linesToSlice.addAll(this.stagedLines.values());
        linesToSlice.forEach(this::sliceLine);

        logger.info(FINISHED_LINE_SLICING, this.shardOrAtlasName,
                time.elapsedSince().asMilliseconds());

        time = Time.now();
        logger.info(STARTED_RELATION_SLICING, this.shardOrAtlasName);
        this.inputAtlas.relationsLowerOrderFirst().forEach(relation ->
        {
            // the first part of this predicate checks to see if the relation qualifies for slicing
            // based on tagging, the second checks its members since a relation with no sliced
            // members shouldn't need slicing
            if (relation.isGeometric() && this.relationPredicate.test(relation)
                    && !this.stagedRelations.get(relation.getIdentifier()).membersMatching(
                            member -> member.getEntity().getType().equals(ItemType.LINE)
                                    && this.inputAtlas
                                            .line(member.getEntity().getIdentifier()) == null)
                            .isEmpty())
            {
                sliceRelation(this.stagedRelations.get(relation.getIdentifier()));
            }
        });
        logger.info(FINISHED_RELATION_SLICING, this.shardOrAtlasName,
                time.elapsedSince().asMilliseconds());

        time = Time.now();
        logger.info(STARTED_POINT_SLICING, this.shardOrAtlasName);
        this.inputAtlas.points().forEach(this::slicePoint);
        logger.info(FINISHED_POINT_SLICING, this.shardOrAtlasName,
                time.elapsedSince().asMilliseconds());

        logger.info(STARTED_RELATION_FILTERING, this.shardOrAtlasName);
        this.inputAtlas.relationsLowerOrderFirst().forEach(relation ->
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
                .forEach(line -> this.changes.add(FeatureChange.add(line, this.inputAtlas)));
        this.stagedPoints.values()
                .forEach(point -> this.changes.add(FeatureChange.add(point, this.inputAtlas)));
        this.stagedRelations.values().forEach(
                relation -> this.changes.add(FeatureChange.add(relation, this.inputAtlas)));
        this.stagedAreas.values()
                .forEach(area -> this.changes.add(FeatureChange.add(area, this.inputAtlas)));

        logger.info(FINISHED_SLICING, this.shardOrAtlasName,
                overallTime.elapsedSince().asMilliseconds());

        final ChangeAtlas slicedAtlas = new ChangeAtlas(this.inputAtlas,
                new ChangeBuilder().addAll(this.changes).get())
        {
            private static final long serialVersionUID = -1379576156041355921L;

            @Override
            public synchronized AtlasMetaData metaData()
            {
                // Override meta-data here so the country code is properly included.
                final AtlasMetaData metaData = super.metaData();
                return new AtlasMetaData(metaData.getSize(), false,
                        metaData.getCodeVersion().orElse(null),
                        metaData.getDataVersion().orElse(null), RawAtlasSlicer.this.country,
                        RawAtlasSlicer.this.shardOrAtlasName, new HashMap<>());
            }
        };
        return cutSubAtlasForOriginalShard(slicedAtlas);
    }

    /**
     * Given a new split Relation, filter its original Relation's members by the country code for
     * the new split Relation and add only them
     *
     * @param newRelation
     *            A new Relation containing a subset of members based on country code
     * @param oldRelation
     *            The original Relation for the new split Relation
     * @param countryMultiPolygon
     */
    private void addCountryMembersToSplitRelation(final CompleteRelation newRelation,
            final CompleteRelation oldRelation, final PreparedPolygon countryMultiPolygon)
    {
        oldRelation.membersMatching(member -> member.getEntity().getType().equals(ItemType.LINE)
                && Validators.isOfSameType(this.stagedLines.get(member.getEntity().getIdentifier()),
                        newRelation, ISOCountryTag.class))
                .forEach(member ->
                {
                    final CompleteLine lineForMember = this.stagedLines
                            .get(member.getEntity().getIdentifier());
                    // quirk of line slicing-- check to see that either we never altered the
                    // geometry, in which case it definitely belongs in the relation, or that it was
                    // sliced but intersects the output multipolygon
                    if (lineForMember.getTag(SyntheticGeometrySlicedTag.KEY).isEmpty()
                            || countryMultiPolygon.intersects(
                                    JTS_POLYLINE_CONVERTER.convert(lineForMember.asPolyLine())))
                    {
                        newRelation.withAddedMember(lineForMember, member.getRole());
                        lineForMember.withAddedRelationIdentifier(newRelation.getIdentifier());
                    }
                    lineForMember.withRemovedRelationIdentifier(oldRelation.getIdentifier());
                });
        oldRelation.membersMatching(member -> member.getEntity().getType().equals(ItemType.AREA)
                && Validators.isOfSameType(this.stagedAreas.get(member.getEntity().getIdentifier()),
                        newRelation, ISOCountryTag.class))
                .forEach(member ->
                {
                    // areas in a multipolygon definitionally were never sliced and must be inside
                    // the output multipolygon-- if they were sliced, we would have converted them
                    // to lines
                    final CompleteArea areaForMember = this.stagedAreas
                            .get(member.getEntity().getIdentifier());
                    newRelation.withAddedMember(areaForMember, member.getRole());
                    areaForMember.withAddedRelationIdentifier(newRelation.getIdentifier());
                    areaForMember.withRemovedRelationIdentifier(oldRelation.getIdentifier());
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
            final Iterable<Point> pointsAtFirstLocation = this.inputAtlas.pointsAt(slice.first());
            if (Iterables.isEmpty(pointsAtFirstLocation))
            {
                final EntityIdentifierGenerator pointIdentifierGenerator = new EntityIdentifierGenerator();
                final SortedSet<String> countries = new TreeSet<>();
                final Map<String, String> tags = new HashMap<>();
                countries.addAll(Arrays.asList(this.boundary.getCountryCodeISO3(slice.first())
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
            final Iterable<Point> pointsAtLastLocation = this.inputAtlas.pointsAt(slice.last());
            if (Iterables.isEmpty(pointsAtLastLocation))
            {
                final EntityIdentifierGenerator pointIdentifierGenerator = new EntityIdentifierGenerator();
                final SortedSet<String> countries = new TreeSet<>();
                final Map<String, String> tags = new HashMap<>();
                countries.addAll(Arrays.asList(this.boundary.getCountryCodeISO3(slice.last())
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
            newLineTags.put(SyntheticSyntheticRelationMemberTag.KEY,
                    SyntheticSyntheticRelationMemberTag.YES.toString());
            final EntityIdentifierGenerator lineIdentifierGenerator = new EntityIdentifierGenerator();
            final CompleteLine newLine = new CompleteLine(0L,
                    JTS_POLYLINE_CONVERTER.backwardConvert(remainderLine), newLineTags,
                    new HashSet<>());
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
                        .anyMatch(relation -> relation.isGeometric()
                                && this.relationPredicate.test(relation)
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
     * Checks the slices of a Line to see if we can process them succesfully. If there's just one
     * country, go ahead and tag the line, and if it's too many slices tag the line as well.
     *
     * @param slicesKeySet
     *            The set of country codes for all of the slices
     * @param totalSlicesCount
     *            The total number of slices for the line
     * @return false if the line shouldn't be sliced with these slices, true otherwise
     */
    private boolean checkSlices(final Set<String> slicesCountryCodes, final long totalSlicesCount,
            final AtlasEntity entity)
    {
        if (slicesCountryCodes.size() == 1)
        {
            final String countryCode = slicesCountryCodes.iterator().next();
            if (entity instanceof Line)
            {
                this.stagedLines.get(entity.getIdentifier()).withAddedTag(ISOCountryTag.KEY,
                        countryCode);
                if (!this.isInCountry.test(this.stagedLines.get(entity.getIdentifier())))
                {
                    removeLine((Line) entity);
                }
            }
            else if (entity instanceof Area)
            {
                this.stagedAreas.get(entity.getIdentifier()).withAddedTag(ISOCountryTag.KEY,
                        countryCode);
                if (!this.isInCountry.test(this.stagedAreas.get(entity.getIdentifier())))
                {
                    removeArea((Area) entity);
                }
            }
            return false;
        }
        else if (totalSlicesCount >= AbstractIdentifierFactory.IDENTIFIER_SCALE_DEFAULT)
        {
            // this should be rare, but we can't slice the line if we don't have the identifier
            // space for the slices
            logger.error(LINE_EXCEEDED_SLICING_IDENTIFIER_SPACE,
                    AbstractIdentifierFactory.IDENTIFIER_SCALE_DEFAULT, entity.getOsmIdentifier(),
                    this.shardOrAtlasName);
            final String countryString = String.join(",", slicesCountryCodes);
            if (entity instanceof Line)
            {
                this.stagedLines.get(entity.getIdentifier()).withTags(entity.getTags())
                        .withAddedTag(ISOCountryTag.KEY, countryString);
            }
            else if (entity instanceof Area)
            {
                this.stagedAreas.get(entity.getIdentifier()).withTags(entity.getTags())
                        .withAddedTag(ISOCountryTag.KEY, countryString);
            }
            return false;
        }
        return true;
    }

    /**
     * Given an Area and its slices, create the new Area entities and put them in the stagedAreas
     * map
     *
     * @param area
     *            The Area being sliced
     * @param slices
     *            The map representing the portions of its geometry in each country, mapped to
     *            country code
     */
    private void createNewSlicedAreas(final Area area,
            final SortedMap<String, Set<org.locationtech.jts.geom.Polygon>> slices)
    {
        final CountrySlicingIdentifierFactory areaIdentifierFactory = new CountrySlicingIdentifierFactory(
                area.getIdentifier());
        slices.keySet().forEach(countryCode ->
        {
            for (final org.locationtech.jts.geom.Polygon slice : slices.get(countryCode))
            {
                final CompleteArea newAreaSlice = CompleteArea.from(area)
                        .withIdentifier(areaIdentifierFactory.nextIdentifier())
                        .withAddedTag(SyntheticGeometrySlicedTag.KEY,
                                SyntheticGeometrySlicedTag.YES.toString())
                        .withAddedTag(ISOCountryTag.KEY, countryCode);
                if (this.isInCountry.test(newAreaSlice))
                {
                    final Polygon newAreaGeometry = new Polygon(processSlice(slice, area));
                    newAreaSlice.withGeometry(newAreaGeometry);
                    area.relations().forEach(relation -> newAreaSlice
                            .withAddedRelationIdentifier(relation.getIdentifier()));
                    this.stagedAreas.put(newAreaSlice.getIdentifier(), newAreaSlice);
                    for (final Relation relation : area.relations())
                    {
                        if (this.stagedRelations.containsKey(relation.getIdentifier()))
                        {
                            this.stagedRelations.get(relation.getIdentifier())
                                    .withAddedMember(newAreaSlice, area);
                        }
                    }
                }
            }
        });
        removeArea(area);
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
    private void createNewSlicedLines(final Line line, // NOSONAR
            final SortedMap<String, Set<LineString>> slices)
    {
        final CountrySlicingIdentifierFactory lineIdentifierFactory = new CountrySlicingIdentifierFactory(
                line.getIdentifier());
        slices.keySet().forEach(countryCode ->
        {
            for (final LineString slice : slices.get(countryCode))
            {
                final CompleteLine newLineSlice = CompleteLine.from(line)
                        .withIdentifier(lineIdentifierFactory.nextIdentifier())
                        .withAddedTag(SyntheticGeometrySlicedTag.KEY,
                                SyntheticGeometrySlicedTag.YES.toString())
                        .withAddedTag(ISOCountryTag.KEY, countryCode);
                if (this.isInCountry.test(newLineSlice))
                {
                    newLineSlice.withGeometry(processSlice(slice, line));
                    if (this.isAtlasEdge.test(line))
                    {
                        addSyntheticBoundaryNodesForSlice(line, newLineSlice.asPolyLine());
                    }
                    this.stagedLines.put(newLineSlice.getIdentifier(), newLineSlice);
                    for (final Relation relation : line.relations())
                    {
                        if (this.stagedRelations.containsKey(relation.getIdentifier()))
                        {
                            final String role = this.stagedRelations.get(relation.getIdentifier())
                                    .membersMatching(member -> member.getEntity().getType()
                                            .equals(ItemType.LINE)
                                            && member.getEntity().getIdentifier() == line
                                                    .getIdentifier())
                                    .iterator().next().getRole();
                            this.stagedRelations.get(relation.getIdentifier())
                                    .withAddedMember(newLineSlice, role);
                        }
                    }
                }
            }
        });
        removeLine(line);
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
            final PreparedPolygon newMultiPolygon)
    {
        final SortedSet<String> syntheticIds = new TreeSet<>();
        // add in remaining synthetic segments
        for (int polygonIndex = 0; polygonIndex < newMultiPolygon.getGeometry()
                .getNumGeometries(); polygonIndex++)
        {
            final org.locationtech.jts.geom.Polygon geometry = (org.locationtech.jts.geom.Polygon) newMultiPolygon
                    .getGeometry().getGeometryN(polygonIndex);
            Geometry remainderExterior = geometry.getExteriorRing().norm();
            if (newRelation.members() != null)
            {
                remainderExterior = cutOutExistingMembers(newRelation,
                        this.preparer.create(remainderExterior), true);
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
                final Geometry remainderInterior = cutOutExistingMembers(newRelation,
                        this.preparer.create(geometry.getInteriorRingN(i)), false);
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
            final PreparedGeometry slicedPolygonOuter, final boolean isOuter)
    {
        Geometry remainder = slicedPolygonOuter.getGeometry();
        final RelationMemberList relationMembersToCheck = isOuter ? newRelation.members()
                : newRelation.membersMatching(
                        member -> member.getRole().equals(RelationTypeTag.MULTIPOLYGON_ROLE_INNER));
        for (final RelationMember member : relationMembersToCheck)
        {
            final LineString outerLineString;
            if (member.getEntity().getType().equals(ItemType.LINE))
            {
                outerLineString = JTS_POLYLINE_CONVERTER.convert(
                        this.stagedLines.get(member.getEntity().getIdentifier()).asPolyLine());
            }
            else
            {
                outerLineString = JTS_POLYLINE_CONVERTER.convert(
                        this.stagedAreas.get(member.getEntity().getIdentifier()).asPolygon());
            }
            if (slicedPolygonOuter.intersects(outerLineString))
            {
                remainder = remainder.difference(outerLineString);
                if (isOuter && member.getRole().equals(RelationTypeTag.MULTIPOLYGON_ROLE_INNER))
                {
                    newRelation.changeMemberRole(member.getEntity(),
                            RelationTypeTag.MULTIPOLYGON_ROLE_OUTER);
                }
            }
        }
        final LineMerger merger = new LineMerger();
        merger.add(remainder);
        final Geometry[] mergedGeometryCollection = (Geometry[]) merger.getMergedLineStrings()
                .toArray(new Geometry[merger.getMergedLineStrings().size()]);
        for (final Geometry geom : mergedGeometryCollection)
        {
            geom.normalize();
        }
        return new GeometryCollection(mergedGeometryCollection,
                JtsPrecisionManager.getGeometryFactory());
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
                                final CompleteRelation splitRelation = this.splitRelations
                                        .get(member.getEntity().getIdentifier()).get(countryCode);
                                if (this.isInCountry.test(splitRelation))
                                {
                                    relation.withAddedMember(splitRelation, member.getRole());
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
     * Given a JTS geometry, return all Polygons from the country boundary map that intersect its
     * internal envelope
     *
     * @param targetGeometry
     *            The geometry being queried
     * @return All Polygons from the country boundary map that intersect its internal envelope
     */
    private Set<PreparedPolygon> getIntersectingBoundaryPolygons(final Geometry targetGeometry)
    {
        return this.boundary.query(targetGeometry.getEnvelopeInternal()).stream().distinct()
                .filter(preparedPolygon -> preparedPolygon.intersects(targetGeometry))
                .collect(Collectors.toSet());
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
     * Checks two sets of geometries to see if one contains any geometries that are covered by or
     * equals to any geometries in the second set
     *
     * @param geometries
     *            A Set of Geometries to check
     * @param geometriesComparison
     *            A second Set of Geometries to compare to
     * @return True if any geometry in geometries is equal to or is covered by a geometry in
     *         geometryComparison, false otherwise
     */
    private boolean isCoveredBy(final Set<Geometry> geometries,
            final Set<PreparedGeometry> geometriesComparison)
    {
        for (final PreparedGeometry comparisonGeometry : geometriesComparison)
        {
            for (final Geometry geometry : geometries)
            {
                if (geometry.equals(comparisonGeometry.getGeometry())
                        || comparisonGeometry.coveredBy(geometry)
                        || comparisonGeometry.intersects(geometry) && geometry
                                .intersection(comparisonGeometry.getGeometry()).getDimension() > 0)
                {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Check to see if a Area is a member of a Relation that meets the Relation predicate-- if so,
     * we want to slice it as a linear feature even if it's closed
     *
     * @param area
     *            The Area to check
     * @return True if any Relations it belongs to meet the Relation predicate
     */
    private boolean isMultipolygonMember(final Area area)
    {
        return area.relations().stream().anyMatch(this.relationPredicate::test);
    }

    /**
     * A filter to ensure trivial pieces of geometry aren't preserved from the slicing operation
     *
     * @param geometry
     *            The geometry to check
     * @return True if the geometry is valid and larger than either the
     *         CountryBoundaryMap.LINE_BUFFER or CountryBoundaryMap.AREA_BUFFER
     */
    private boolean isSignificantGeometry(final Geometry original, final Geometry clipped)
    {
        if (!clipped.isValid() && logger.isWarnEnabled())
        {
            logger.warn("Found invalid geometry {} during slicing Atlas {}", clipped.toText(),
                    this.shardOrAtlasName);
            return false;
        }
        if (clipped.getDimension() == 1)
        {
            return clipped.getLength() > LINE_BUFFER
                    || clipped.getLength() / original.getLength() > BUFFER_PERCENTAGE;
        }
        else if (clipped.getDimension() == 2)
        {
            return clipped.getArea() > AREA_BUFFER
                    || clipped.getArea() / original.getArea() > BUFFER_PERCENTAGE;
        }
        return false;
    }

    /**
     * Given a slice for a Line entity, construct an appropriate PolyLine for the new sliced Line
     * member. In the case of a Polygon (i.e. Area), the PolyLine will be constructed such that the
     * winding represents the winding of the original entity
     *
     * @param slice
     *            The slice for a Line entity
     * @param entity
     *            The Line being sliced
     * @return The best PolyLine to represent that slice for the Line
     */
    private PolyLine processSlice(final Geometry slice, final AtlasEntity entity)
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
                    entity.getOsmIdentifier(), this.shardOrAtlasName);
        }

        // JTS frequently reverses the winding during slicing-- this checks the winding of the
        // original geometry, and reverse the winding of the slice if needed
        if (entity instanceof Area)
        {
            final boolean originalClockwise = ((Area) entity).asPolygon().isClockwise();
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
     * Given a geometric Relation, purge any members that don't meet the criteria in the OSM
     * specification
     *
     * @param relation
     *            The Relation to check
     */
    private void purgeInvalidGeometricRelationMembers(final CompleteRelation relation)
    {
        final Set<String> memberIdentifiersRemoved = new HashSet<>();
        relation.membersMatching(member -> member.getEntity().getType() != ItemType.LINE
                || !(member.getRole().equals(RelationTypeTag.MULTIPOLYGON_ROLE_OUTER)
                        || member.getRole().equals(RelationTypeTag.MULTIPOLYGON_ROLE_INNER)))
                .forEach(invalidMember ->
                {
                    final long identifier = invalidMember.getEntity().getIdentifier();
                    logger.warn(MULTIPOLYGON_RELATION_INVALID_MEMBER_REMOVED, invalidMember,
                            relation.getOsmIdentifier());
                    if (invalidMember.getEntity().getType().equals(ItemType.LINE))
                    {
                        relation.withRemovedMember(invalidMember.getEntity());
                        this.stagedLines.get(identifier)
                                .withRemovedRelationIdentifier(relation.getIdentifier());
                        memberIdentifiersRemoved.add(Long.toString(identifier));
                    }
                    else if (invalidMember.getEntity().getType().equals(ItemType.AREA)
                            && this.stagedAreas.containsKey(identifier)
                            && this.stagedAreas.get(identifier)
                                    .getTag(SyntheticGeometrySlicedTag.KEY).isPresent())
                    {
                        relation.withRemovedMember(invalidMember.getEntity());
                        this.stagedAreas.get(identifier)
                                .withRemovedRelationIdentifier(relation.getIdentifier());
                    }
                    else if (invalidMember.getEntity().getType().equals(ItemType.POINT))
                    {
                        if (Validators.isOfType(relation, RelationTypeTag.class,
                                RelationTypeTag.BOUNDARY)
                                && (invalidMember.getRole().equals("admin_centre")
                                        || invalidMember.getRole().equals("label")))
                        {
                            // keep admin centre or lable nodes
                        }
                        else
                        {
                            relation.withRemovedMember(invalidMember.getEntity());
                            this.stagedPoints.get(identifier)
                                    .withRemovedRelationIdentifier(relation.getIdentifier());
                            memberIdentifiersRemoved.add(Long.toString(identifier));
                        }
                    }
                    else
                    {
                        if (this.stagedRelations.containsKey(identifier))
                        {
                            relation.withRemovedMember(invalidMember.getEntity());
                            this.stagedRelations.get(identifier)
                                    .withRemovedRelationIdentifier(relation.getIdentifier());
                            memberIdentifiersRemoved.add(Long.toString(identifier));
                        }
                        else if (this.splitRelations.containsKey(identifier))
                        {
                            relation.withRemovedMember(invalidMember.getEntity());
                            this.splitRelations.get(identifier).values().forEach(
                                    childRelation -> childRelation.withRemovedRelationIdentifier(
                                            relation.getIdentifier()));
                            memberIdentifiersRemoved.add(Long.toString(identifier));
                        }
                    }
                });
        if (!memberIdentifiersRemoved.isEmpty())
        {
            relation.withAddedTag(SyntheticInvalidMultiPolygonRelationMembersRemovedTag.KEY,
                    String.join(
                            SyntheticInvalidMultiPolygonRelationMembersRemovedTag.MEMBER_DELIMITER,
                            memberIdentifiersRemoved));
        }
    }

    /**
     * Remove an Area from the Atlas, and remove it from any Relations that may contain it
     *
     * @param area
     *            The Line to remove
     */
    private void removeArea(final Area area)
    {
        final CompleteArea removedArea = this.stagedAreas.remove(area.getIdentifier());
        this.changes.add(FeatureChange.remove(removedArea, this.inputAtlas));
        removedArea.relationIdentifiers().forEach(relationIdentifier ->
        {
            if (this.stagedRelations.containsKey(relationIdentifier))
            {
                this.stagedRelations.get(relationIdentifier).withRemovedMember(removedArea);
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
        if (this.inputAtlas.line(line.getIdentifier()) == null)
        {
            // in rare cases, we're slicing a line that technically never existed in the original
            // atlas, e.g. multipolygon areas now being sliced as lines, or areas that failed
            // polygonal slicing. in these cases, simple remove the staged line, don't make a
            // FeatureChange to remove the line from the Atlas
            removedLine.relationIdentifiers().forEach(relationIdentifier ->
            {
                if (this.stagedRelations.containsKey(relationIdentifier))
                {
                    this.stagedRelations.get(relationIdentifier).withRemovedMember(removedLine);
                }
            });
            return;
        }
        this.changes.add(FeatureChange.remove(removedLine, this.inputAtlas));
        removedLine.relationIdentifiers().forEach(relationIdentifier ->
        {
            if (this.stagedRelations.containsKey(relationIdentifier))
            {
                this.stagedRelations.get(relationIdentifier).withRemovedMember(removedLine);
            }
        });
    }

    private org.locationtech.jts.geom.MultiPolygon removeOsmValidOverlappingInners(
            final Relation relation, final org.locationtech.jts.geom.MultiPolygon multipolygon)
    {
        final Set<PreparedGeometry> slicedInnerLines = new HashSet<>();
        relation.membersMatching(
                member -> member.getRole().equals(RelationTypeTag.MULTIPOLYGON_ROLE_INNER)
                        && member.getEntity().getType().equals(ItemType.LINE))
                .forEach(member ->
                {
                    final Line innerLine = this.stagedLines.get(member.getEntity().getIdentifier());
                    if (innerLine.getTag(SyntheticGeometrySlicedTag.KEY).isPresent())
                    {
                        slicedInnerLines.add(this.preparer
                                .create(JTS_POLYLINE_CONVERTER.convert(innerLine.asPolyLine())));
                    }
                });

        final org.locationtech.jts.geom.Polygon[] modifiedPolygons = new org.locationtech.jts.geom.Polygon[multipolygon
                .getNumGeometries()];
        for (int i = 0; i < multipolygon.getNumGeometries(); i++)
        {
            final org.locationtech.jts.geom.Polygon currentPolygon = (org.locationtech.jts.geom.Polygon) multipolygon
                    .getGeometryN(i);
            final List<LinearRing> holes = new ArrayList<>();

            for (int j = 0; j < currentPolygon.getNumInteriorRing(); j++)
            {
                final PreparedGeometry currentInner = this.preparer
                        .create(currentPolygon.getInteriorRingN(j));
                boolean remove = false;
                for (int k = j + 1; k < currentPolygon.getNumInteriorRing(); k++)
                {
                    final Geometry comparisonInner = currentPolygon.getInteriorRingN(k);
                    if (currentInner.intersects(comparisonInner))
                    {
                        final Set<Geometry> inners = new HashSet<>();
                        inners.add(currentInner.getGeometry());
                        inners.add(comparisonInner);
                        if (isCoveredBy(inners, slicedInnerLines))
                        {
                            remove = false;
                            break;
                        }
                        remove = true;
                    }
                }
                if (!remove)
                {
                    holes.add((LinearRing) currentInner.getGeometry());
                }
            }
            modifiedPolygons[i] = new org.locationtech.jts.geom.Polygon(
                    currentPolygon.getExteriorRing(), holes.toArray(new LinearRing[holes.size()]),
                    JtsPrecisionManager.getGeometryFactory());
        }
        return new org.locationtech.jts.geom.MultiPolygon(modifiedPolygons,
                JtsPrecisionManager.getGeometryFactory());
    }

    /**
     * Slice a Line that qualifies as an Area by converting it to 2d geometry, calculating its
     * slices, and creating the new sliced Lines. If it belongs to just one country or cannot be
     * sliced, update the ISOCountryTag appropriately instead
     *
     * @param line
     *            The Line to slice as an Area
     */
    private void sliceArea(final Area area)
    {
        final Time time = Time.now();
        final org.locationtech.jts.geom.Polygon jtsPolygon = JTS_POLYGON_CONVERTER
                .convert(area.asPolygon());
        final Set<PreparedPolygon> intersectingBoundaryPolygons = getIntersectingBoundaryPolygons(
                jtsPolygon);
        if (intersectingBoundaryPolygons.size() == 1
                || CountryBoundaryMap.isSameCountry(intersectingBoundaryPolygons))
        {
            final String countryCode = CountryBoundaryMap.getGeometryProperty(
                    intersectingBoundaryPolygons.iterator().next().getGeometry(),
                    ISOCountryTag.KEY);
            this.stagedAreas.get(area.getIdentifier()).withAddedTag(ISOCountryTag.KEY, countryCode);
            if (!this.isInCountry.test(this.stagedAreas.get(area.getIdentifier())))
            {
                removeArea(area);
            }
            return;
        }

        // only check this once we know it's likely to be sliced
        if (jtsPolygon.isEmpty() || !jtsPolygon.isValid())
        {
            if (logger.isErrorEnabled())
            {
                logger.error(LINE_HAD_INVALID_GEOMETRY, area.getOsmIdentifier(),
                        this.shardOrAtlasName, jtsPolygon.toText());
            }
            final SortedSet<String> countries = new TreeSet<>();
            intersectingBoundaryPolygons.forEach(polygon -> countries.add(CountryBoundaryMap
                    .getGeometryProperty(polygon.getGeometry(), ISOCountryTag.KEY)));
            final String countryCodes = String.join(",", countries);
            this.stagedAreas.get(area.getIdentifier()).withAddedTag(ISOCountryTag.KEY,
                    countryCodes);
            this.stagedAreas.get(area.getIdentifier()).withAddedTag(SyntheticInvalidGeometryTag.KEY,
                    SyntheticInvalidGeometryTag.YES.toString());
            return;
        }
        final SortedMap<String, Set<org.locationtech.jts.geom.Polygon>> slices;
        try
        {
            slices = slicePolygonGeometry(area.getOsmIdentifier(), jtsPolygon,
                    intersectingBoundaryPolygons);
        }
        catch (final CoreException exception)
        {
            logger.error(LINE_HAD_MULTIPOLYGON_SLICE, area.getOsmIdentifier(),
                    this.shardOrAtlasName);
            final Set<Long> relationIds = new HashSet<>();
            area.relations().forEach(relation -> relationIds.add(relation.getIdentifier()));
            final CompleteLine lineFromArea = new CompleteLine(area.getIdentifier(),
                    JTS_POLYLINE_CONVERTER.backwardConvert(
                            JTS_POLYGON_CONVERTER.convert(area.asPolygon()).getExteriorRing()),
                    area.getTags(), relationIds);
            area.relations().forEach(relation -> this.stagedRelations.get(relation.getIdentifier())
                    .withAddedMember(lineFromArea, area));
            removeArea(area);
            this.stagedLines.put(lineFromArea.getIdentifier(), lineFromArea);
            return;
        }

        long numSlices = 0;
        for (final Set<org.locationtech.jts.geom.Polygon> sliceSet : slices.values())
        {
            numSlices += sliceSet.size();
        }
        logger.info("Way {} was sliced into {} slices", area.getOsmIdentifier(), numSlices);

        if (!checkSlices(slices.keySet(), numSlices, area))
        {
            return;
        }

        createNewSlicedAreas(area, slices);

        if (time.elapsedSince().isMoreThan(Duration.minutes(SLICING_DURATION_WARN)))
        {
            logger.warn(LINE_SLICING_DURATION_EXCEEDED, area.getOsmIdentifier(),
                    this.shardOrAtlasName, time.elapsedSince().asMilliseconds());
        }

        // HACK ALERT UGH
        if (isMultipolygonMember(area))
        {
            // read line to staged lines since we know we just removed it to slice it-- we only get
            // to here if we had to slice the line
            final Set<Long> relationIds = new HashSet<>();
            area.relations().forEach(relation -> relationIds.add(relation.getIdentifier()));
            final CompleteLine lineFromArea = new CompleteLine(area.getIdentifier(),
                    area.asPolygon(), area.getTags(), relationIds);
            relationIds.forEach(relationId ->
            {
                if (this.relationPredicate.test(this.stagedRelations.get(relationId)))
                {
                    this.stagedRelations.get(relationId).withAddedMember(lineFromArea, area);
                }
                else
                {
                    // only add this line to multipolygon relations
                    lineFromArea.withRemovedRelationIdentifier(relationId);
                }
            });
            this.stagedLines.put(lineFromArea.getIdentifier(), lineFromArea);
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
            final Geometry geometry, final Set<PreparedPolygon> countryBoundaryPolygons,
            final long identifier)
    {
        final Set<Geometry> filteredPieces = new HashSet<>();
        final Map<String, Set<org.locationtech.jts.geom.Geometry>> results = new HashMap<>();
        for (final PreparedPolygon boundaryPolygon : countryBoundaryPolygons)
        {
            final String countryCode = CountryBoundaryMap
                    .getGeometryProperty(boundaryPolygon.getGeometry(), ISOCountryTag.KEY);
            // occasionally, the geometry's internal envelope will intersect multiple boundary
            // polygons but the geometry doesn't. in this case, just tag the geometry entirely
            if (boundaryPolygon.contains(geometry))
            {
                CountryBoundaryMap.setGeometryProperty(geometry, ISOCountryTag.KEY, countryCode);
                results.clear();
                results.put(countryCode, new HashSet<>());
                results.get(countryCode).add(geometry);
                return results;
            }
            else if (boundaryPolygon.intersects(geometry))
            {
                if (!results.containsKey(countryCode))
                {
                    results.put(countryCode, new HashSet<>());
                }
                Geometry clipped;
                try
                {
                    clipped = geometry.intersection(boundaryPolygon.getGeometry());
                }
                catch (final TopologyException exc)
                {
                    logger.warn(
                            "Topology exception using regular intersection, falling back to snap overlay");
                    clipped = OverlayNG.overlay(geometry, boundaryPolygon.getGeometry(),
                            OverlayNG.INTERSECTION, JtsPrecisionManager.getPrecisionModel());
                }

                if (clipped instanceof GeometryCollection)
                {
                    CountryBoundaryMap.geometries((GeometryCollection) clipped)
                            .filter(result -> isSignificantGeometry(geometry, result))
                            .forEach(result ->
                            {
                                CountryBoundaryMap.setGeometryProperty(result, ISOCountryTag.KEY,
                                        countryCode);
                                results.get(countryCode).add(result);
                            });
                    filteredPieces
                            .addAll(CountryBoundaryMap.geometries((GeometryCollection) clipped)
                                    .filter(result -> !isSignificantGeometry(geometry, result))
                                    .collect(Collectors.toSet()));
                }
                else if (isSignificantGeometry(geometry, clipped))
                {
                    CountryBoundaryMap.setGeometryProperty(clipped, ISOCountryTag.KEY, countryCode);
                    results.get(countryCode).add(clipped);
                }
                else
                {
                    filteredPieces.add(clipped);
                }
            }
        }
        if (!filteredPieces.isEmpty())
        {
            if (geometry.getDimension() == 1)
            {
                long length = 0;
                for (final Geometry filtered : filteredPieces)
                {
                    length += filtered.getLength();
                }
                logger.warn("Removed {} slices from way {} for being trivial, summing to {} length",
                        filteredPieces.size(), identifier, length);
            }
            else if (geometry.getDimension() == 2)
            {
                long area = 0;
                for (final Geometry filtered : filteredPieces)
                {
                    area += filtered.getArea();
                }
                logger.warn(
                        "Removed {} slices from OSM entity {} for being trivial, summing to {} area",
                        filteredPieces.size(), identifier, area);
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

        if (this.isAtlasEdge.test(line))
        {
            line.forEach(location -> this.inputAtlas.pointsAt(location)
                    .forEach(point -> this.pointsBelongingToEdge.add(point.getIdentifier())));
        }

        final Set<PreparedPolygon> intersectingBoundaryPolygons = getIntersectingBoundaryPolygons(
                jtsLine);
        if (CountryBoundaryMap.isSameCountry(intersectingBoundaryPolygons))
        {
            final String countryCode = CountryBoundaryMap.getGeometryProperty(
                    intersectingBoundaryPolygons.iterator().next().getGeometry(),
                    ISOCountryTag.KEY);
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
            intersectingBoundaryPolygons.forEach(polygon -> countries.add(CountryBoundaryMap
                    .getGeometryProperty(polygon.getGeometry(), ISOCountryTag.KEY)));
            final String countryCodes = String.join(",", countries);
            this.stagedLines.get(line.getIdentifier()).withAddedTag(ISOCountryTag.KEY,
                    countryCodes);
            this.stagedLines.get(line.getIdentifier()).withAddedTag(SyntheticInvalidGeometryTag.KEY,
                    SyntheticInvalidGeometryTag.YES.toString());
            return;
        }
        final SortedMap<String, Set<LineString>> slices = sliceLineStringGeometry(jtsLine,
                intersectingBoundaryPolygons, line.getOsmIdentifier());

        long numSlices = 0;
        for (final Set<LineString> sliceSet : slices.values())
        {
            numSlices += sliceSet.size();
        }
        if (this.isAtlasEdge.test(line))
        {
            logger.info("Edge {} was sliced into {} slices", line.getOsmIdentifier(), numSlices);
        }
        else
        {
            logger.info("Way {} was sliced into {} slices", line.getOsmIdentifier(), numSlices);
        }

        if (!checkSlices(slices.keySet(), numSlices, line))
        {
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
            final Set<PreparedPolygon> intersectingBoundaryPolygons, final long identifier)
    {
        final Map<String, Set<Geometry>> currentResults = sliceGeometry(line,
                intersectingBoundaryPolygons, identifier);
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
            final Set<PreparedPolygon> intersectingBoundaryPolygons)
    {
        final Map<String, Set<org.locationtech.jts.geom.Geometry>> currentResults = sliceGeometry(
                geometry, intersectingBoundaryPolygons, identifier);
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
     * Given a point, either remove it from the Atlas if it has no pre-existing tags and doesn't
     * belong to an Edge, OR update its ISOCountryTag value
     *
     * @param point
     *            The point to slice
     */
    private void slicePoint(final Point point)
    {
        if (point.getOsmTags().isEmpty()
                && !this.pointsBelongingToEdge.contains(point.getIdentifier()) && !this.stagedPoints
                        .get(point.getIdentifier()).getTag(SyntheticBoundaryNodeTag.KEY).isPresent()
                && !this.keepAll)
        {
            // we care about a point if and only if it has pre-existing OSM tags OR it belongs
            // to a future edge OR we are keeping all points for QC
            this.stagedPoints.remove(point.getIdentifier());
            this.changes.add(FeatureChange.remove(CompletePoint.shallowFrom(point)));
        }
        else
        {
            final CompletePoint updatedPoint = this.stagedPoints.get(point.getIdentifier());
            final SortedSet<String> countries = new TreeSet<>();
            countries.addAll(Arrays.asList(this.boundary.getCountryCodeISO3(point.getLocation())
                    .getIso3CountryCode().split(ISOCountryTag.COUNTRY_DELIMITER)));
            updatedPoint.withAddedTag(ISOCountryTag.KEY,
                    String.join(ISOCountryTag.COUNTRY_DELIMITER, countries));
            if (countries.size() > 1)
            {
                updatedPoint.withAddedTag(SyntheticBoundaryNodeTag.KEY,
                        SyntheticBoundaryNodeTag.EXISTING.toString());
            }
            if (!this.isInCountry.test(updatedPoint) && !this.keepAll)
            {
                this.stagedPoints.remove(point.getIdentifier());
                this.changes.add(FeatureChange.remove(updatedPoint, this.inputAtlas));
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
            final Set<PreparedPolygon> intersectingBoundaryPolygon)
    {
        final Map<String, Set<Geometry>> currentResults = sliceGeometry(polygon,
                intersectingBoundaryPolygon, identifier);

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
                        throw new CoreException(
                                "Line {} for Atlas {} had multipolygon geometry {}!", identifier,
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

    /**
     * Slice a geometric Relation by constructing its geometry out of the valid raw Atlas members,
     * calculating its slices, and creating the new sliced Relations with valid geometry. If it
     * belongs to just one country or cannot be sliced, update the ISOCountryTag appropriately
     * instead
     *
     * @param Relation
     *            The Relation to slice geometrically
     */
    private void sliceRelation(final CompleteRelation relation)
    {
        final Time time = Time.now();
        if (relation.isGeometric())
        {
            purgeInvalidGeometricRelationMembers(relation);
        }
        org.locationtech.jts.geom.MultiPolygon jtsMp;
        try
        {
            final MultiPolygon multipolygon = RELATION_TO_MULTIPOLYGON_CONVERTER
                    .convert(this.inputAtlas.relation(relation.getIdentifier()));
            jtsMp = JTS_MULTIPOLYGON_CONVERTER.backwardConvert(multipolygon);
        }
        catch (final CoreException exception)
        {
            logger.error(MULTIPOLYGON_RELATION_EXCEPTION_CREATING_POLYGON,
                    relation.getOsmIdentifier(), this.shardOrAtlasName, exception);
            relation.withAddedTag(SyntheticInvalidGeometryTag.KEY,
                    SyntheticInvalidGeometryTag.YES.toString());
            return;
        }

        // Check to see if the relation is one country only, short circuit if it is
        final Set<PreparedPolygon> polygons = this.boundary.query(jtsMp.getEnvelopeInternal())
                .stream().distinct().collect(Collectors.toSet());

        if (CountryBoundaryMap.isSameCountry(polygons))
        {
            final String country = CountryBoundaryMap.getGeometryProperty(
                    polygons.iterator().next().getGeometry(), ISOCountryTag.KEY);
            // just tag with the country code and move on, no slicing needed
            relation.withAddedTag(ISOCountryTag.KEY, country);
            return;
        }

        if (!jtsMp.isValid())
        {
            jtsMp = removeOsmValidOverlappingInners(relation, jtsMp);
            if (!jtsMp.isValid())
            {
                if (logger.isErrorEnabled())
                {
                    logger.error(MULTIPOLYGON_RELATION_INVALID_GEOMETRY,
                            relation.getOsmIdentifier(), this.shardOrAtlasName, jtsMp.toText());
                }
                relation.withAddedTag(SyntheticInvalidGeometryTag.KEY,
                        SyntheticInvalidGeometryTag.YES.toString());
                return;
            }
            else
            {
                logger.warn(MULTIPOLYGON_RELATION_OVERLAPPING_INNERS, relation.getOsmIdentifier(),
                        this.shardOrAtlasName);
            }
        }

        final SortedMap<String, org.locationtech.jts.geom.MultiPolygon> clippedMultiPolygons = sliceMultiPolygonGeometry(
                relation.getIdentifier(), jtsMp, polygons);

        if (clippedMultiPolygons.isEmpty())
        {
            logger.error(MULTIPOLYGON_RELATION_HAD_NO_SLICED_GEOMETRY, relation.getOsmIdentifier(),
                    this.shardOrAtlasName);
            return;
        }

        final SortedMap<String, PreparedPolygon> preparedClippedPolygons = new TreeMap<>();
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
            preparedClippedPolygons.put(country,
                    (PreparedPolygon) this.preparer.create(countryMultipolygon));
        }

        if (this.consolidatePredicate.test(relation))
        {
            final Set<String> removedCountries = new HashSet<>();
            double size = 0;
            double percentSize = 0;
            org.locationtech.jts.geom.MultiPolygon largest = null;
            for (final PreparedPolygon polygon : preparedClippedPolygons.values())
            {
                if (largest == null || polygon.getGeometry().getArea() > largest.getArea())
                {
                    largest = (org.locationtech.jts.geom.MultiPolygon) polygon.getGeometry();
                }
            }

            if (largest.getArea() / jtsMp.getArea() > MINIMUM_CONSOLIDATE_THRESHOLD)
            {
                final Set<String> countrySlices = new HashSet<>();
                countrySlices.addAll(preparedClippedPolygons.keySet());
                for (final String country : countrySlices)
                {
                    if (!preparedClippedPolygons.get(country).getGeometry().equals(largest))
                    {
                        size += preparedClippedPolygons.get(country).getGeometry().getArea();
                        percentSize += preparedClippedPolygons.get(country).getGeometry().getArea()
                                / jtsMp.getArea() * PERCENTAGE;
                        logger.info(
                                "Removing sliced relation {} with size {} and percentage size {}",
                                relation.getOsmIdentifier(),
                                preparedClippedPolygons.get(country).getGeometry().getArea(),
                                preparedClippedPolygons.get(country).getGeometry().getArea()
                                        / jtsMp.getArea());
                        preparedClippedPolygons.remove(country);
                        removedCountries.add(country);
                    }
                }
                logger.info(
                        "Consolidated relation {} to country {}, ignoring countries {} with sliced size {} and percent size {}",
                        relation.getOsmIdentifier(),
                        preparedClippedPolygons.keySet().iterator().next(),
                        String.join(ISOCountryTag.COUNTRY_DELIMITER, removedCountries), size,
                        percentSize);
            }
            else
            {
                logger.info(
                        "Relation {} met tagging criteria for consolidation, but largest piece with size {} did not meet percentage threshold {}",
                        relation.getOsmIdentifier(), largest.getArea() / jtsMp.getArea(),
                        MINIMUM_CONSOLIDATE_THRESHOLD);
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
        preparedClippedPolygons.keySet().forEach(countryCode ->
        {
            final CompleteRelation newRelation = CompleteRelation.shallowFrom(relation)
                    .withIdentifier(relationIdentifierFactory.nextIdentifier())
                    .withTags(relation.getTags())
                    .withAddedTag(SyntheticGeometrySlicedTag.KEY,
                            SyntheticGeometrySlicedTag.YES.toString())
                    .withAddedTag(ISOCountryTag.KEY, countryCode)
                    .withRelationIdentifiers(relation.relationIdentifiers())
                    .withOsmRelationIdentifier(relation.getOsmIdentifier());
            newRelationIds.add(newRelation.getIdentifier());
            if (this.isInCountry.test(newRelation))
            {
                addCountryMembersToSplitRelation(newRelation, relation,
                        preparedClippedPolygons.get(countryCode));
                createSyntheticRelationMembers(newRelation,
                        preparedClippedPolygons.get(countryCode));
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
        this.changes.add(FeatureChange.remove(relation, this.inputAtlas));
        this.stagedRelations.remove(relation.getIdentifier());
        this.splitRelations.put(relation.getIdentifier(), relationByCountry);
        if (time.elapsedSince().isMoreThan(Duration.minutes(SLICING_DURATION_WARN)))
        {
            logger.warn(MULTIPOLYGON_RELATION_SLICING_DURATION_EXCEEDED,
                    relation.getOsmIdentifier(), this.shardOrAtlasName, time.elapsedSince());
        }
    }
}
