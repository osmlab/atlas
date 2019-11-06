package org.openstreetmap.atlas.geography.atlas.raw.slicing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.IntersectionMatrix;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.TopologyException;
import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.Polygon;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.change.ChangeAtlas;
import org.openstreetmap.atlas.geography.atlas.change.ChangeBuilder;
import org.openstreetmap.atlas.geography.atlas.change.FeatureChange;
import org.openstreetmap.atlas.geography.atlas.complete.CompleteArea;
import org.openstreetmap.atlas.geography.atlas.complete.CompleteEdge;
import org.openstreetmap.atlas.geography.atlas.complete.CompleteEntity;
import org.openstreetmap.atlas.geography.atlas.complete.CompleteLine;
import org.openstreetmap.atlas.geography.atlas.complete.CompleteNode;
import org.openstreetmap.atlas.geography.atlas.complete.CompletePoint;
import org.openstreetmap.atlas.geography.atlas.complete.CompleteRelation;
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.geography.atlas.items.ItemType;
import org.openstreetmap.atlas.geography.atlas.items.Line;
import org.openstreetmap.atlas.geography.atlas.items.Point;
import org.openstreetmap.atlas.geography.atlas.items.Relation;
import org.openstreetmap.atlas.geography.atlas.items.RelationMember;
import org.openstreetmap.atlas.geography.atlas.items.RelationMemberList;
import org.openstreetmap.atlas.geography.atlas.pbf.AtlasLoadingOption;
import org.openstreetmap.atlas.geography.atlas.pbf.slicing.identifier.AbstractIdentifierFactory;
import org.openstreetmap.atlas.geography.atlas.pbf.slicing.identifier.CountrySlicingIdentifierFactory;
import org.openstreetmap.atlas.geography.atlas.pbf.slicing.identifier.PointIdentifierFactory;
import org.openstreetmap.atlas.geography.atlas.pbf.slicing.identifier.ReverseIdentifierFactory;
import org.openstreetmap.atlas.geography.atlas.raw.temporary.TemporaryRelation;
import org.openstreetmap.atlas.geography.atlas.sub.AtlasCutType;
import org.openstreetmap.atlas.geography.boundary.CountryBoundaryMap;
import org.openstreetmap.atlas.geography.converters.jts.JtsUtility;
import org.openstreetmap.atlas.geography.sharding.Shard;
import org.openstreetmap.atlas.tags.ISOCountryTag;
import org.openstreetmap.atlas.tags.RelationTypeTag;
import org.openstreetmap.atlas.tags.SyntheticBoundaryNodeTag;
import org.openstreetmap.atlas.tags.SyntheticRelationMemberAdded;
import org.openstreetmap.atlas.tags.annotations.validation.Validators;
import org.openstreetmap.atlas.utilities.collections.Iterables;
import org.openstreetmap.atlas.utilities.collections.MultiIterable;
import org.openstreetmap.atlas.utilities.identifiers.EntityIdentifierGenerator;
import org.openstreetmap.atlas.utilities.maps.MultiMap;
import org.openstreetmap.atlas.utilities.time.Time;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

/**
 * The {@link RawAtlasRelationSlicer} consumes a partially sliced (only points and lines are sliced)
 * Atlas and produces a fully country-sliced Atlas as output.
 *
 * @author mgostintsev
 * @author samg
 */
public class RawAtlasRelationSlicer extends RawAtlasSlicer
{
    private static final Logger logger = LoggerFactory.getLogger(RawAtlasRelationSlicer.class);

    // The initial Shard being sliced
    private final Shard initialShard;

    // Keep track of any points that may have to be removed after relation merging
    private final Set<Long> pointCandidatesForRemoval;

    private final Map<Long, Map<String, CompleteRelation>> splitRelations = new HashMap<>();

    private final Set<FeatureChange> changes = new HashSet<>();

    private final Map<Long, CompleteLine> newLineCountries = new HashMap<>();

    private final Set<Long> deletedRelations = new HashSet<>();

    private final Predicate<AtlasEntity> isInCountry;

    /**
     * Standard constructor
     *
     * @param atlas
     *            The Atlas file to sliced-- should be line sliced
     * @param initialShard
     *            The initial shard being sliced-- should be the same as the line sliced Atlas
     *            passed in
     * @param loadingOption
     *            The {@link AtlasLoadingOption} to use
     */
    public RawAtlasRelationSlicer(final Atlas atlas, final Shard initialShard,
            final AtlasLoadingOption loadingOption)
    {
        super(loadingOption, new CoordinateToNewPointMapping(), atlas);
        this.initialShard = initialShard;
        this.pointCandidatesForRemoval = new HashSet<>();
        this.isInCountry = entity -> ISOCountryTag.isIn(this.getIsoCountries()).test(entity);
    }

    /**
     * Country-slice the relations for the given Atlas.
     *
     * @return a country-sliced {@link Atlas}
     */
    @Override
    public Atlas slice()
    {
        final Time time = Time.now();
        logger.info("Starting relation slicing for Atlas {}", getShardOrAtlasName());

        // Slice all relations
        sliceRelations();
        logger.info("Finished slicing relations for Atlas {}", getShardOrAtlasName());
        // Remove any shape points from deleted lines
        removeDeletedPoints();
        logger.info("Finished relation slicing for Atlas {} in {}", getShardOrAtlasName(),
                time.elapsedSince());
        if (this.changes.isEmpty())
        {
            return cutSubAtlasForOriginalShard(getStartingAtlas());
        }

        return cutSubAtlasForOriginalShard(new ChangeAtlas(getStartingAtlas(),
                new ChangeBuilder().addAll(this.changes).get()));
    }

    /**
     * Tries to build valid {@link LinearRing}s given a {@link Relation}'s members.
     *
     * @param members
     *            The members to build rings with
     * @param relationIdentifier
     *            The relation identifier whose members we're building with
     * @return a list of built {@link LinearRing}s
     */
    private List<LinearRing> buildRings(final List<RelationMember> members,
            final long relationIdentifier)
    {
        final List<LinearRing> results = new ArrayList<>();
        final List<PolyLine> linePieces = new ArrayList<>(members.size());

        for (final RelationMember member : members)
        {
            // We can assume all members are lines since we've already filtered out non-line members
            final Line line = getStartingAtlas().line(member.getEntity().getIdentifier());
            if (line == null)
            {
                throw new CoreException(
                        "Relation {} has a member {} that's not in the original Atlas {}",
                        relationIdentifier, member.getEntity().getIdentifier(),
                        getShardOrAtlasName());
            }
            linePieces.add(line.asPolyLine());
        }

        try
        {
            final Iterable<Polygon> polygons = MULTIPLE_POLY_LINE_TO_POLYGON_CONVERTER
                    .convert(linePieces);
            polygons.forEach(polygon -> results.add(JTS_LINEAR_RING_CONVERTER.convert(polygon)));
        }
        catch (final Exception e)
        {
            // Could not form closed rings for some of the members. Keep them in the Atlas.
            logger.error(
                    "One of the members for relation {} for Atlas {} is invalid and does not form a closed ring!",
                    relationIdentifier, getShardOrAtlasName(), e);
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
    private List<LineString> clipBoundary(final long identifier,
            final org.locationtech.jts.geom.Polygon geometry, final CountryBoundaryMap boundary)
    {
        final List<LineString> results = new ArrayList<>();
        if (Objects.isNull(geometry))
        {
            return results;
        }

        final Geometry target = geometry;

        final List<org.locationtech.jts.geom.Polygon> polygons = boundary
                .query(target.getEnvelopeInternal());

        if (CountryBoundaryMap.isSameCountry(polygons))
        {
            return results;
        }

        for (final org.locationtech.jts.geom.Polygon polygon : polygons)
        {
            final IntersectionMatrix matrix;
            try
            {
                matrix = target.relate(polygon);
            }
            catch (final Exception e)
            {
                logger.warn("Error slicing feature: {}, {}", identifier, e.getMessage());
                continue;
            }

            if (matrix.isWithin())
            {
                return results;
            }
            else if (matrix.isIntersects())
            {
                final Geometry clipped = target.intersection(polygon.getExteriorRing());
                final String containedCountryCode = CountryBoundaryMap.getGeometryProperty(polygon,
                        ISOCountryTag.KEY);

                if (clipped instanceof GeometryCollection)
                {
                    final GeometryCollection collection = (GeometryCollection) clipped;
                    CountryBoundaryMap.geometries(collection).forEach(point ->
                    {
                        CountryBoundaryMap.setGeometryProperty(point, ISOCountryTag.KEY,
                                containedCountryCode);
                        results.add((LineString) point);
                    });
                }
                else if (clipped instanceof LineString)
                {
                    CountryBoundaryMap.setGeometryProperty(clipped, ISOCountryTag.KEY,
                            containedCountryCode);
                    results.add((LineString) clipped);
                }
                else
                {
                    throw new CoreException(
                            "Unexpected geometry {} encountered during country slicing.", clipped);
                }
            }
        }
        return results;
    }

    /**
     * Determines whether any of the given members was sliced.
     *
     * @param members
     *            The members to look at
     * @return {@code true} if any of the members was sliced
     */
    private boolean containsSlicedMember(final Iterable<RelationMember> members)
    {
        for (final RelationMember member : members)
        {
            // A member was sliced if the country code was incremented during line slicing
            if (new ReverseIdentifierFactory()
                    .getCountryCode(member.getEntity().getIdentifier()) != 0)
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Given a {@link LineString} and corresponding {@link CountrySlicingIdentifierFactory}s for
     * point and line identifier generation, creates a new {@link Line} and any new {@link Point}s,
     * if necessary. The new {@link Line} is then added as a member for the given {@link Relation}
     * {@link Identifier}. Corresponding synthetic tags are added to the relation to signify an
     * added relation member.
     *
     * @param lineString
     *            The JTS {@link LineString} that describes the {@link Line} to create
     * @param relationIdentifier
     *            The {@link Relation} identifier to which to add the new {@link Line} as a member
     *            for
     * @param pointIdentifierGenerator
     *            The {@link CountrySlicingIdentifierFactory} for new {@link Point}s
     * @param lineIdentifierGenerator
     *            The {@link CountrySlicingIdentifierFactory} for new {@link Line}s
     */
    private CompleteLine createNewLineMemberForRelation(final LineString lineString,
            final PointIdentifierFactory pointIdentifierGenerator)
    {
        final List<Location> newLineLocations = new ArrayList<>();

        // Keep track of identifiers that form the geometry of the new line
        final Coordinate[] lineCoordinates = PRECISION_REDUCER.edit(lineString.getCoordinates(),
                lineString);

        for (final Coordinate pointCoordinate : lineCoordinates)
        {
            final Location pointLocation = JTS_LOCATION_CONVERTER.backwardConvert(pointCoordinate);

            // If there aren't any points at this precision in the raw Atlas, need to
            // examine cache and possibly scale the coordinate to 6 digits of precision
            if (Iterables.isEmpty(getStartingAtlas().pointsAt(pointLocation)))
            {
                // Add the scaled location to the line
                final Location scaledLocation = JTS_LOCATION_CONVERTER.backwardConvert(
                        getCoordinateToPointMapping().getScaledCoordinate(pointCoordinate));
                newLineLocations.add(scaledLocation);

                // If the location doesn't have a point in the original Atlas or in the cache,
                // make a new Point and add it both
                if (Iterables.isEmpty(getStartingAtlas().pointsAt(scaledLocation))
                        && !getCoordinateToPointMapping().containsCoordinate(pointCoordinate))
                {
                    final Map<String, String> newPointTags = createPointTags(pointLocation, false);
                    newPointTags.put(SyntheticBoundaryNodeTag.KEY,
                            SyntheticBoundaryNodeTag.YES.toString());
                    final long newPointIdentifier = createNewPointIdentifier(
                            pointIdentifierGenerator);
                    final CompletePoint newPoint = new CompletePoint(newPointIdentifier,
                            scaledLocation, newPointTags, new HashSet<Long>());
                    // Store coordinate to avoid creating duplicate points
                    getCoordinateToPointMapping().storeMapping(pointCoordinate, newPointIdentifier);
                    // Save the point to add to the rebuilt atlas
                    this.changes.add(FeatureChange.add(newPoint, getStartingAtlas()));
                }
            }
            else
            {
                newLineLocations.add(pointLocation);
            }
        }

        // Create the patched line
        final Map<String, String> newLineTags = createLineTags(lineString, new HashMap<>());
        final EntityIdentifierGenerator lineIdentifierGenerator = new EntityIdentifierGenerator();
        final CompleteLine newLine = new CompleteLine(0L, new PolyLine(newLineLocations),
                newLineTags, new HashSet<Long>());
        newLine.withIdentifier(lineIdentifierGenerator.generateIdentifier(newLine));
        this.newLineCountries.put(newLine.getIdentifier(), newLine);
        return newLine;
    }

    /**
     * A method to handle creating new point identifiers. It's important to use this method as it
     * will test the generated identifier to ensure no collisions with other entities and
     * re-generate if needed.
     *
     * @param pointIdentifierFactory
     *            A factory for generating point identifiers
     * @param coordinate
     *            The coordinate that needs an identifier
     * @return A valid point identifier
     */
    private long createNewPointIdentifier(final PointIdentifierFactory pointIdentifierFactory)
    {
        while (pointIdentifierFactory.hasMore())
        {
            final long identifier = pointIdentifierFactory.nextIdentifier();
            if (getStartingAtlas().point(identifier) == null)
            {
                return identifier;
            }
        }
        throw new CoreException("Exceeded maximum number {} of supported new Points for Atlas {}",
                pointIdentifierFactory.getIdentifierScale(), getShardOrAtlasName());
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
        try
        {
            if (this.initialShard != null)
            {
                // filter out all AtlasEntities that are not in the list of countries being sliced
                // for this shard
                final Optional<Atlas> countrySubAtlas = atlas.subAtlas(this.isInCountry,
                        AtlasCutType.SILK_CUT);
                if (countrySubAtlas.isPresent())
                {
                    // then, filter out all remaining entities based on the shard bounds
                    final Optional<Atlas> finalSubAtlas = countrySubAtlas.get()
                            .subAtlas(this.initialShard.bounds(), AtlasCutType.SILK_CUT);
                    if (finalSubAtlas.isPresent())
                    {
                        return finalSubAtlas.get();
                    }
                    else
                    {
                        return null;
                    }
                }
                else
                {
                    return null;
                }
            }
            else
            {
                return atlas;
            }
        }
        catch (final Exception e)
        {
            throw new CoreException("Error creating sub-atlas for original shard bounds", e);
        }
    }

    /**
     * Loops through all the outer and inner members and creates a mapping between an outer and all
     * the inners that happen to intersect it.
     *
     * @param closedOuterLines
     *            The list of closed outer {@link Line}s to use
     * @param closedInnerLines
     *            The list of closed inner {@link Line}s to use
     * @return a mapping of intersecting outer to inner relation members
     */
    private MultiMap<Integer, Integer> findIntersectingMembers(final List<Line> closedOuterLines,
            final List<Line> closedInnerLines)
    {
        final MultiMap<Integer, Integer> outerToInnerIntersectionMap = new MultiMap<>();
        if (!closedOuterLines.isEmpty() && !closedInnerLines.isEmpty())
        {
            for (int innerIndex = 0; innerIndex < closedInnerLines.size(); innerIndex++)
            {
                final Line inner = closedInnerLines.get(innerIndex);
                for (int outerIndex = 0; outerIndex < closedOuterLines.size(); outerIndex++)
                {
                    final Line outer = closedOuterLines.get(outerIndex);

                    // Make sure we're looking at the same country, not just intersection
                    if (fromSameCountry(outer, inner) && outer.intersects(new Polygon(inner)))
                    {
                        outerToInnerIntersectionMap.add(outerIndex, innerIndex);
                    }
                }
            }
        }

        return outerToInnerIntersectionMap;
    }

    /**
     * Given a list of relation members, will generate a new list containing all existing, closed or
     * non-closed (depending on given parameter) members.
     *
     * @param relationIdentifier
     *            The {@link Relation} identifier whose members we're processing
     * @param members
     *            The list of {@link RelationMember} we want to group
     * @param closed
     *            {@code true} if we want to find closed member lines, false otherwise
     * @return a subset of the given {@link RelationMember}s, either closed or non-closed
     */
    private List<RelationMember> generateMemberList(final long relationIdentifier,
            final List<RelationMember> members, final boolean closed)
    {
        if (members != null && !members.isEmpty())
        {
            // Check if all outer ways are closed. If there are any that aren't closed, we need to
            // build LineRings and fix the gaps
            return members.stream().filter(member ->
            {
                final Line line = getStartingAtlas().line(member.getEntity().getIdentifier());
                if (line == null)
                {
                    logger.error("Line member {} for Relation {} is not the in Atlas {}",
                            member.getEntity().getIdentifier(), relationIdentifier,
                            getShardOrAtlasName());
                    return false;
                }

                final boolean isClosed = line.isClosed();
                return closed ? isClosed : !isClosed;
            }).collect(Collectors.toList());
        }
        else
        {
            return Collections.emptyList();
        }
    }

    private AtlasEntity getCompleteEntity(final AtlasEntity entity)
    {
        if (entity.getType().equals(ItemType.AREA))
        {
            return CompleteArea.from(getStartingAtlas().area(entity.getIdentifier()));
        }
        if (entity.getType().equals(ItemType.EDGE))
        {
            return CompleteEdge.from(getStartingAtlas().edge(entity.getIdentifier()));
        }
        if (entity.getType().equals(ItemType.LINE))
        {
            if (entity instanceof CompleteEntity)
            {
                return this.newLineCountries.get(entity.getIdentifier());
            }
            return CompleteLine.from(getStartingAtlas().line(entity.getIdentifier()));
        }
        if (entity.getType().equals(ItemType.POINT))
        {
            return CompletePoint.from(getStartingAtlas().point(entity.getIdentifier()));
        }
        if (entity.getType().equals(ItemType.NODE))
        {
            return CompleteNode.from(getStartingAtlas().node(entity.getIdentifier()));
        }
        return CompleteRelation.from(getStartingAtlas().relation(entity.getIdentifier()));

    }

    private Map<Integer, List<LinearRing>> getOuterToInnerMap(final Relation relation,
            final List<LinearRing> outerRings, final List<RelationMember> inners)
    {
        final Map<Integer, List<LinearRing>> outerToInnerMap = new HashMap<>();
        for (int i = 0; i < outerRings.size(); i++)
        {
            outerToInnerMap.put(i, new ArrayList<LinearRing>());
        }

        if (inners == null || inners.isEmpty())
        {
            return outerToInnerMap;
        }

        // Grab all non-closed inner ways
        final List<LinearRing> innerRings = new ArrayList<>();
        final List<RelationMember> nonClosedInners = new ArrayList<>();
        nonClosedInners.addAll(generateMemberList(relation.getIdentifier(), inners, false));
        innerRings.addAll(buildRings(nonClosedInners, relation.getIdentifier()));
        if (innerRings.isEmpty())
        {
            return outerToInnerMap;
        }

        for (int innerIndex = 0; innerIndex < innerRings.size(); innerIndex++)
        {
            final LinearRing inner = innerRings.get(innerIndex);
            boolean foundEnclosingOuter = false;
            for (int outerIndex = 0; outerIndex < outerRings.size(); outerIndex++)
            {
                final LinearRing outer = outerRings.get(outerIndex);
                final org.locationtech.jts.geom.Polygon outerPolygon = new org.locationtech.jts.geom.Polygon(
                        outer, null, JtsUtility.GEOMETRY_FACTORY);
                if (outerPolygon.contains(inner))
                {
                    foundEnclosingOuter = true;
                    outerToInnerMap.get(outerIndex).add(innerRings.get(innerIndex));
                }
            }

            if (!foundEnclosingOuter)
            {
                logger.error(
                        "Found isolated inner member for Multipolygon Relation geometry while slicing Atlas {}: {}",
                        getShardOrAtlasName(), inner);
            }
        }
        return outerToInnerMap;
    }

    /**
     * Takes in a list of relation members and groups them by country. The output is a map of
     * country to list of members.
     *
     * @param members
     *            The members we wish to group
     */
    private Map<String, List<RelationMember>> groupRelationMembersByCountry(
            final long relationIdentifier, final RelationMemberList members)
    {
        final MultiMap<String, RelationMember> countryEntityMap = new MultiMap<>();
        Iterables.stream(members).forEach(member ->
        {
            switch (member.getEntity().getType())
            {
                case RELATION:
                    if (this.splitRelations.containsKey(member.getEntity().getIdentifier()))
                    {
                        this.splitRelations.get(member.getEntity().getIdentifier()).keySet()
                                .forEach(country ->
                                {
                                    final Relation splitRelation = this.splitRelations
                                            .get(member.getEntity().getIdentifier()).get(country);
                                    final RelationMember splitRelationMember = new RelationMember(
                                            member.getRole(), splitRelation, relationIdentifier);
                                    countryEntityMap.add(country, splitRelationMember);
                                });
                    }
                    break;
                case LINE:
                    if (this.newLineCountries.containsKey(member.getEntity().getIdentifier()))
                    {
                        final Line line = this.newLineCountries
                                .get(member.getEntity().getIdentifier());
                        final RelationMember lineMember = new RelationMember(member.getRole(), line,
                                relationIdentifier);
                        ISOCountryTag.allCountryStrings(line)
                                .forEach(country -> countryEntityMap.add(country, lineMember));
                    }
                    else
                    {
                        final Line line = CompleteLine
                                .from(getStartingAtlas().line(member.getEntity().getIdentifier()));
                        final RelationMember lineMember = new RelationMember(member.getRole(), line,
                                relationIdentifier);
                        ISOCountryTag.allCountryStrings(line)
                                .forEach(country -> countryEntityMap.add(country, lineMember));
                    }
                    break;
                default:
                    final Point point = CompletePoint
                            .from(getStartingAtlas().point(member.getEntity().getIdentifier()));
                    final RelationMember pointMember = new RelationMember(member.getRole(), point,
                            relationIdentifier);
                    ISOCountryTag.allCountryStrings(point)
                            .forEach(country -> countryEntityMap.add(country, pointMember));
            }
        });
        return countryEntityMap;
    }

    /**
     * Marks a line for deletion, if that line is being removed as a relation member. This is safe
     * to do when the line member was merged with another member to maintain a valid relation and
     * the merged line is not part of any other relation.
     *
     * @param line
     *            The relation member {@link Line}
     * @param relationIdentifier
     *            The parent {@link Relation} identifier
     */
    private void markRemovedMemberLineForDeletion(final Line line, final Relation relation)
    {
        // Only mark this line for deletion if this line isn't part of any other relations
        final boolean isPartOfOtherRelations = line.relations().stream()
                .anyMatch(partOf -> partOf.getIdentifier() != relation.getIdentifier());

        logger.info("Removing line {} from relation {}", line, relation);
        if (!isPartOfOtherRelations)
        {
            // Delete Line and all of its points
            this.changes
                    .add(FeatureChange.remove(CompleteLine.shallowFrom(line), getStartingAtlas()));
            getStartingAtlas().line(line.getIdentifier()).asPolyLine()
                    .forEach(location -> getStartingAtlas().pointsAt(location).forEach(
                            point -> this.pointCandidatesForRemoval.add(point.getIdentifier())));
        }
        else
        {
            final CompleteLine updatedLine = CompleteLine.shallowFrom(line);
            final Set<Long> updatedParentRelations = new HashSet<>();
            line.relations().forEach(parentRelation ->
            {
                if (parentRelation.getIdentifier() != relation.getIdentifier())
                {
                    updatedParentRelations.add(parentRelation.getIdentifier());
                }
            });
            updatedLine.withRelationIdentifiers(updatedParentRelations);
            this.changes.add(FeatureChange.add(updatedLine, getStartingAtlas()));
        }
    }

    /**
     * Try to merge any closed inner and outer members into a single member. The flow is:
     * <ul>
     * <li>1. Create inner and outer lists of closed {@link Line} members for the given
     * {@link Relation}
     * <li>2. Find any outer to inner intersections
     * <li>3. For all such intersections, take the JTS difference of the outer and inner
     * <li>4. Create new points and lines for the new piece that was created
     * <li>5. Update the change set with any created features
     * </ul>
     *
     * @param relation
     *            The {@link Relation} whose members we're looking at
     * @param outers
     *            The list of outer {@link RelationMember}s
     * @param inners
     *            The list of inner {@link RelationMember}s
     */
    private void mergeOverlappingClosedMembers(final CompleteRelation relation,
            final List<RelationMember> outers, final List<RelationMember> inners,
            final PointIdentifierFactory pointIdentifierFactory)
    {
        final long relationIdentifier = relation.getIdentifier();
        final List<RelationMember> closedOuters = generateMemberList(relationIdentifier, outers,
                true);
        final List<RelationMember> closedInners = generateMemberList(relationIdentifier, inners,
                true);
        final List<Line> closedOuterLines = new ArrayList<>();
        closedOuters.forEach(outer -> closedOuterLines
                .add(getStartingAtlas().line(outer.getEntity().getIdentifier())));

        final List<Line> closedInnerLines = new ArrayList<>();
        closedInners.forEach(inner -> closedInnerLines
                .add(getStartingAtlas().line(inner.getEntity().getIdentifier())));

        final MultiMap<Integer, Integer> outerToInnerIntersectionMap = findIntersectingMembers(
                closedOuterLines, closedInnerLines);

        final Set<RelationMember> removedMembers = new HashSet<>();
        final Set<CompleteLine> addedMembers = new HashSet<>();
        // Try to combine the intersecting members
        for (final Entry<Integer, List<Integer>> entry : outerToInnerIntersectionMap.entrySet())
        {
            final int outerIndex = entry.getKey();
            final List<Integer> innerIndices = entry.getValue();
            Geometry mergedMembers = null;

            // Convert outer to ring
            final Line outer = closedOuterLines.get(outerIndex);
            final LinearRing outerRing = JTS_LINEAR_RING_CONVERTER
                    .convert(new Polygon(outer.getRawGeometry()));
            final org.locationtech.jts.geom.Polygon outerPolygon = new org.locationtech.jts.geom.Polygon(
                    outerRing, null, JtsUtility.GEOMETRY_FACTORY);

            for (final int innerIndex : innerIndices)
            {
                // Convert inner to ring, create polygon, take union of all
                final Line inner = closedInnerLines.get(innerIndex);
                final LinearRing innerRing = JTS_LINEAR_RING_CONVERTER
                        .convert(new Polygon(inner.getRawGeometry()));
                final org.locationtech.jts.geom.Polygon innerPolygon = new org.locationtech.jts.geom.Polygon(
                        innerRing, null, JtsUtility.GEOMETRY_FACTORY);

                try
                {
                    mergedMembers = outerPolygon.difference(innerPolygon);
                }
                catch (final Exception e)
                {
                    logger.error(
                            "Error combining intersecting outer {} and inner {} members for Relation {} for Atlas {}",
                            outer, inner, relationIdentifier, getShardOrAtlasName(), e);
                }
            }

            // Make sure the merged piece is valid. Some merges will be invalid - for example an
            // outer contained within an inner. If that's the case, we want to abort the merge and
            // leave the members as they are. We are also ignoring MultiPolygons as those have merge
            // complications.
            if (!(mergedMembers instanceof org.locationtech.jts.geom.Polygon)
                    || !((org.locationtech.jts.geom.Polygon) mergedMembers).getExteriorRing()
                            .isClosed()
                    || ((org.locationtech.jts.geom.Polygon) mergedMembers).getExteriorRing()
                            .isEmpty())
            {
                continue;
            }

            final LineString exteriorRing = ((org.locationtech.jts.geom.Polygon) mergedMembers)
                    .getExteriorRing();

            removedMembers.add(closedOuters.get(outerIndex));
            innerIndices.forEach(index -> closedOuters.add(closedInners.get(index)));

            markRemovedMemberLineForDeletion(outer, relation);
            innerIndices
                    .forEach(index -> markRemovedMemberLineForDeletion(closedInnerLines.get(index),
                            relation));

            final Optional<String> possibleCountryCode = outer.getTag(ISOCountryTag.KEY);
            if (!possibleCountryCode.isPresent())
            {
                // At this point, all members are sliced and must have a country code
                throw new CoreException(
                        "Relation {} contains member {} that is missing a country code for Atlas {}",
                        relationIdentifier, outer, getShardOrAtlasName());
            }

            CountryBoundaryMap.setGeometryProperty(exteriorRing, ISOCountryTag.KEY,
                    possibleCountryCode.get());

            // Create points, lines and update members
            addedMembers.add(createNewLineMemberForRelation(exteriorRing, pointIdentifierFactory));
        }

        final List<RelationMember> updatedRelationMembers = new ArrayList<>();
        for (final RelationMember member : relation.members())
        {
            if (!getStartingAtlas().relation(relation.getIdentifier()).members().contains(member))
            {
                updatedRelationMembers.add(member);
            }
        }

        final RelationMemberList updatedMembersList = new RelationMemberList(
                Iterables.stream(getStartingAtlas().relation(relation.getIdentifier()).members())
                        .filter(member -> !removedMembers.contains(member)));
        relation.withMembersAndSource(updatedMembersList,
                getStartingAtlas().relation(relation.getIdentifier()));
        updatedRelationMembers.forEach(member -> relation
                .withAddedMember(getCompleteEntity(member.getEntity()), member.getRole()));
        addedMembers.forEach(newLine ->
        {
            this.changes.add(FeatureChange.add(newLine, getStartingAtlas()));
            relation.withAddedMember(newLine, RelationTypeTag.MULTIPOLYGON_ROLE_OUTER);
        });
    }

    /**
     * Try to patch any holes for multipolygon relations comprised of non-closed members. The flow
     * is:
     * <ul>
     * <li>1. Create inner and outer lists of non-closed {@link Line} members for the given
     * {@link Relation}
     * <li>2. Build valid outer and inner rings
     * <li>3. Construct a JTS Polygon with the outer and inner rings
     * <li>4. Clip the Polygon along the country boundary
     * <li>5. For any clip, create new points, lines and relation members
     * <li>6. Update the change set with any created features
     * </ul>
     *
     * @param relation
     *            The {@link Relation} being patched
     * @param outers
     *            All the inner relation members
     * @param inners
     *            All the outer relation members
     */
    private void patchNonClosedMembers(final CompleteRelation relation,
            final List<RelationMember> outers, final List<RelationMember> inners,
            final PointIdentifierFactory pointIdentifierFactory)
    {
        final long relationIdentifier = relation.getIdentifier();

        // Grab all non-closed outer ways
        final List<RelationMember> nonClosedOuters = generateMemberList(relationIdentifier, outers,
                false);

        if (nonClosedOuters.isEmpty())
        {
            return;
        }

        final List<LinearRing> outerRings = buildRings(nonClosedOuters, relationIdentifier);
        if (outerRings.isEmpty())
        {
            return;
        }

        final Map<Integer, List<LinearRing>> outerToInnerHoleMap = getOuterToInnerMap(relation,
                outerRings, inners);

        for (int outerIndex = 0; outerIndex < outerRings.size(); outerIndex++)
        {
            final LinearRing outerRing = outerRings.get(outerIndex);
            final LinearRing[] holes = outerToInnerHoleMap.get(outerIndex)
                    .toArray(new LinearRing[0]);

            final org.locationtech.jts.geom.Polygon polygon = new org.locationtech.jts.geom.Polygon(
                    outerRing, holes, JtsUtility.GEOMETRY_FACTORY);

            // Check if the polygon is valid. Sometimes polygons from relations can be
            // invalid, such as for large boundaries.
            if (!polygon.isValid())
            {
                logger.error("Polygon created by Relation {} is invalid for Atlas {}",
                        relationIdentifier, getShardOrAtlasName());
                return;
            }

            try
            {
                // The intent is to have all the unique slices in the same order that
                // they were generated in. Because the grid index is pre-built and used for
                // slicing, we may encounter duplicate LineString in the returned list, so
                // we de-dupe using a Set.
                final Set<LineString> borderLines = Sets.newLinkedHashSet(
                        clipBoundary(relationIdentifier, polygon, getCountryBoundaryMap()));
                if (borderLines == null || borderLines.isEmpty())
                {
                    // There was no cut, we don't need to update the relation
                    return;
                }

                if (borderLines.size() >= AbstractIdentifierFactory.IDENTIFIER_SCALE_DEFAULT)
                {
                    logger.error(
                            "Borderline got cut into more than 999 pieces for Relation {} for Atlas {}",
                            relationIdentifier, getShardOrAtlasName());
                    return;
                }

                final Set<CompleteLine> newBorderLines = new HashSet<>();
                // Create geometry for all new border lines
                borderLines.forEach(borderLine -> newBorderLines
                        .add(createNewLineMemberForRelation(borderLine, pointIdentifierFactory)));
                newBorderLines.forEach(newBorderLine ->
                {
                    this.changes.add(FeatureChange.add(newBorderLine, getStartingAtlas()));
                    relation.withAddedMember(newBorderLine,
                            RelationTypeTag.MULTIPOLYGON_ROLE_OUTER);
                });
            }
            catch (final Exception e)
            {
                logger.error("Error processing Relation {} for Atlas {}, message: {}, geometry: {}",
                        relationIdentifier, getShardOrAtlasName(), e.getMessage(),
                        polygon.toString(), e);
            }
        }
    }

    /**
     * Taking our pool of candidate points to delete, we check two cases before marking the point
     * for deletion: 1. No remaining lines rely on the point 2. No new lines rely on the point
     * without providing a replacement
     */
    private void removeDeletedPoints()
    {
        for (final long identifier : this.pointCandidatesForRemoval)
        {
            final Location location = getStartingAtlas().point(identifier).getLocation();
            final boolean partOfExistingNonDeletedLine = !Iterables
                    .isEmpty(getStartingAtlas().linesContaining(location));

            // All lines that contain this point have been deleted, delete the point
            if (!partOfExistingNonDeletedLine)
            {
                this.changes.add(FeatureChange
                        .remove(CompletePoint.shallowFrom(getStartingAtlas().point(identifier))));
            }
        }
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
    private void sliceMultiPolygonRelation(final Relation relation)
    {
        final Map<String, List<RelationMember>> roleMap = relation.members().stream()
                .filter(member -> member.getEntity().getType() == ItemType.LINE)
                .collect(Collectors.groupingBy(RelationMember::getRole));
        final List<RelationMember> outers = roleMap.get(RelationTypeTag.MULTIPOLYGON_ROLE_OUTER);
        final List<RelationMember> inners = roleMap.get(RelationTypeTag.MULTIPOLYGON_ROLE_INNER);
        final PointIdentifierFactory pointIdentifierFactory = new PointIdentifierFactory(
                relation.getIdentifier());
        final CompleteRelation updatedRelation = CompleteRelation.from(relation);

        if (outers != null && !outers.isEmpty())
        {
            patchNonClosedMembers(updatedRelation, outers, inners, pointIdentifierFactory);
            // We only want to merge members for modified relations. Unless we touched the relation
            // directly, we will leave the "mergeable" relation as is since it's a more true
            // representation of OSM data.
            if (inners != null && !inners.isEmpty()
                    && containsSlicedMember(new MultiIterable<>(outers, inners)))
            {
                mergeOverlappingClosedMembers(updatedRelation, outers, inners,
                        pointIdentifierFactory);
            }
        }

        splitRelation(updatedRelation);
    }

    private void sliceNonMultiPolygonRelation(final Relation relation)
    {
        final CompleteRelation updatedRelation = CompleteRelation.shallowFrom(relation);
        updatedRelation.withTags(relation.getTags());
        // Group members by country
        final Map<String, List<RelationMember>> countryEntityMap = groupRelationMembersByCountry(
                relation.getIdentifier(), relation.members());
        // Create a new Relation for each country
        final List<RelationMember> relationMembers = new ArrayList<>();
        countryEntityMap.entrySet().forEach(entry ->
        {
            // we're keeping these members, but do need to check that any child relations that have
            // been split are updated
            if (this.getCountries().contains(entry.getKey())
                    || String.join(",", this.getCountries()).equals(entry.getKey()))
            {
                relationMembers.addAll(entry.getValue());
            }
        });
        if (relationMembers.isEmpty())
        {
            this.deletedRelations.add(relation.getIdentifier());
            this.changes.add(FeatureChange.remove(updatedRelation));
        }
        else
        {
            final RelationMemberList updatedMembersList = new RelationMemberList(relationMembers);
            updatedRelation.withMembersAndSource(updatedMembersList,
                    getStartingAtlas().relation(relation.getIdentifier()));
            // compute the value of the final country tag
            final List<String> countries = new ArrayList<>();
            countryEntityMap.keySet().forEach(country ->
            {
                if (this.getCountries().contains(country))
                {
                    countries.add(country);
                }
            });
            updatedRelation.withAddedTag(ISOCountryTag.KEY, String.join(",", countries));
            this.changes.add(FeatureChange.add(updatedRelation));
            final HashMap<String, CompleteRelation> relationByCountry = new HashMap<>();
            relationByCountry.put(String.join(",", countries), updatedRelation);
            this.splitRelations.put(updatedRelation.getIdentifier(), relationByCountry);
            relation.members().stream().filter(member -> !relationMembers.contains(member))
                    .forEach(filteredMember ->
                    {
                        if (filteredMember.getEntity().getType() != ItemType.RELATION
                                || !this.deletedRelations
                                        .contains(filteredMember.getEntity().getIdentifier()))
                        {
                            final CompleteEntity<?> filteredEntity = (CompleteEntity<?>) getCompleteEntity(
                                    filteredMember.getEntity());
                            filteredEntity.withRemovedRelationIdentifier(relation.getIdentifier());
                            this.changes.add(FeatureChange.add((AtlasEntity) filteredEntity,
                                    getStartingAtlas()));
                        }
                    });
        }
    }

    private void sliceRelations()
    {
        getStartingAtlas().relationsLowerOrderFirst().forEach(relation ->
        {
            final Set<String> isoCountryCodes = new HashSet<>();
            relation.membersOfType(ItemType.LINE, ItemType.POINT).forEach(member -> isoCountryCodes
                    .add(member.getEntity().getTag(ISOCountryTag.KEY).get()));
            if (Validators.isOfType(relation, RelationTypeTag.class, RelationTypeTag.BOUNDARY,
                    RelationTypeTag.MULTIPOLYGON) && isoCountryCodes.size() > 1)
            {
                sliceMultiPolygonRelation(relation);
            }
            else
            {
                sliceNonMultiPolygonRelation(relation);
            }

        });
    }

    /**
     * Take a {@link Relation} and create one or more {@link TemporaryRelation}s to add to the
     * sliced Atlas. We first add all existing members, if they haven't been removed during slicing.
     * We then add any new members for this relation. Finally, we group by members by country and
     * see if we need to split up the existing relation into two or more separate relations.
     *
     * @param relation
     *            The {@link Relation} to update
     */
    private void splitRelation(final CompleteRelation currentRelation)
    {
        // remove the old relation
        this.changes.add(FeatureChange.remove(CompleteRelation.shallowFrom(currentRelation),
                getStartingAtlas()));
        this.deletedRelations.add(currentRelation.getIdentifier());

        // Group members by country
        final Map<String, List<RelationMember>> countryEntityMap = groupRelationMembersByCountry(
                currentRelation.getIdentifier(), currentRelation.members());
        final List<RelationMember> membersWithoutCountry;
        if (countryEntityMap.containsKey(ISOCountryTag.COUNTRY_MISSING))
        {
            membersWithoutCountry = countryEntityMap.remove(ISOCountryTag.COUNTRY_MISSING);
        }
        else
        {
            membersWithoutCountry = Collections.emptyList();
        }

        final HashMap<String, CompleteRelation> relationByCountry = new HashMap<>();
        final CountrySlicingIdentifierFactory relationIdentifierFactory = new CountrySlicingIdentifierFactory(
                currentRelation.getIdentifier());
        final List<Long> newRelationIds = new ArrayList<>();
        final Set<CompleteRelation> newRelations = new HashSet<>();

        // Create a new Relation for each country
        countryEntityMap.entrySet().forEach(entry ->
        {
            final RelationMemberList candidateMembers = new RelationMemberList(entry.getValue());
            candidateMembers.addAll(membersWithoutCountry);
            if (!candidateMembers.isEmpty())
            {
                // increment the relation identifier for determinism
                final long slicedRelationIdentifier = relationIdentifierFactory.nextIdentifier();
                newRelationIds.add(slicedRelationIdentifier);
                // but only make the new relation if we're going to keep it
                if (this.getCountries().contains(entry.getKey()))
                {
                    // Create new relation tags - add in a country code
                    final CompleteRelation newRelation = CompleteRelation.from(currentRelation)
                            .withIdentifier(slicedRelationIdentifier).withMembers(candidateMembers)
                            .withAddedTag(ISOCountryTag.KEY, entry.getKey());
                    newRelations.add(newRelation);
                }
            }
        });
        newRelations.forEach(newRelation ->
        {
            // update so each split relation knows about the other split relations
            newRelation.withAllRelationsWithSameOsmIdentifier(newRelationIds);
            relationByCountry.put(newRelation.getTag(ISOCountryTag.KEY).get(), newRelation);
            final Set<String> syntheticIds = new HashSet<>();
            newRelation.members().forEach(member ->
            {
                if (this.newLineCountries.keySet().contains(member.getEntity().getIdentifier()))
                {
                    syntheticIds.add(Long.toString(member.getEntity().getIdentifier()));
                }
            });
            newRelation.withAddedTag(SyntheticRelationMemberAdded.KEY,
                    String.join(",", syntheticIds));
            this.changes.add(FeatureChange.add(newRelation));

        });
        this.splitRelations.put(currentRelation.getIdentifier(), relationByCountry);
    }
}
