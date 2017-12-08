package org.openstreetmap.atlas.geography.atlas.raw.slicing;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.Polygon;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.geography.atlas.items.ItemType;
import org.openstreetmap.atlas.geography.atlas.items.Line;
import org.openstreetmap.atlas.geography.atlas.items.Point;
import org.openstreetmap.atlas.geography.atlas.items.Relation;
import org.openstreetmap.atlas.geography.atlas.items.RelationMember;
import org.openstreetmap.atlas.geography.atlas.pbf.slicing.identifier.AbstractIdentifierFactory;
import org.openstreetmap.atlas.geography.atlas.pbf.slicing.identifier.CountrySlicingIdentifierFactory;
import org.openstreetmap.atlas.geography.atlas.pbf.slicing.identifier.ReverseIdentifierFactory;
import org.openstreetmap.atlas.geography.atlas.raw.slicing.changeset.RawAtlasChangeSetBuilder;
import org.openstreetmap.atlas.geography.atlas.raw.slicing.changeset.RawAtlasRelationChangeSet;
import org.openstreetmap.atlas.geography.atlas.raw.slicing.changeset.RawAtlasRelationChangeSetBuilder;
import org.openstreetmap.atlas.geography.atlas.raw.slicing.changeset.RawAtlasSimpleChangeSet;
import org.openstreetmap.atlas.geography.atlas.raw.slicing.changeset.RawAtlasSimpleChangeSetBuilder;
import org.openstreetmap.atlas.geography.atlas.raw.slicing.temporary.PolygonPiece;
import org.openstreetmap.atlas.geography.atlas.raw.slicing.temporary.TemporaryLine;
import org.openstreetmap.atlas.geography.atlas.raw.slicing.temporary.TemporaryPoint;
import org.openstreetmap.atlas.geography.atlas.raw.slicing.temporary.TemporaryRelation;
import org.openstreetmap.atlas.geography.atlas.raw.slicing.temporary.TemporaryRelationMember;
import org.openstreetmap.atlas.geography.boundary.CountryBoundaryMap;
import org.openstreetmap.atlas.geography.converters.jts.JtsCoordinateArrayConverter;
import org.openstreetmap.atlas.geography.converters.jts.JtsLinearRingConverter;
import org.openstreetmap.atlas.geography.converters.jts.JtsLocationConverter;
import org.openstreetmap.atlas.geography.converters.jts.JtsPolyLineConverter;
import org.openstreetmap.atlas.geography.converters.jts.JtsPolygonConverter;
import org.openstreetmap.atlas.geography.converters.jts.JtsUtility;
import org.openstreetmap.atlas.locale.IsoCountry;
import org.openstreetmap.atlas.tags.ISOCountryTag;
import org.openstreetmap.atlas.tags.RelationTypeTag;
import org.openstreetmap.atlas.tags.SyntheticBoundaryNodeTag;
import org.openstreetmap.atlas.tags.SyntheticNearestNeighborCountryCodeTag;
import org.openstreetmap.atlas.tags.SyntheticRelationMemberAdded;
import org.openstreetmap.atlas.tags.annotations.validation.Validators;
import org.openstreetmap.atlas.utilities.maps.MultiMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Iterables;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.TopologyException;

/**
 * The {@link RawAtlasCountrySlicer} consumes a raw Atlas and produces a fully country-sliced Atlas
 * as output.
 *
 * @author mgostintsev
 */
public class RawAtlasCountrySlicer
{
    private static final Logger logger = LoggerFactory.getLogger(RawAtlasCountrySlicer.class);

    // JTS converters
    private static final JtsPolygonConverter JTS_POLYGON_CONVERTER = new JtsPolygonConverter();
    private static final JtsPolyLineConverter JTS_POLYLINE_CONVERTER = new JtsPolyLineConverter();
    private static final JtsLocationConverter JTS_LOCATION_CONVERTER = new JtsLocationConverter();
    private static final JtsLinearRingConverter JTS_LINEAR_RING_CONVERTER = new JtsLinearRingConverter();
    private static final JtsCoordinateArrayConverter JTS_COORDINATE_CONVERTER = new JtsCoordinateArrayConverter();

    // Constants
    private static final int JTS_MINIMUM_RING_SIZE = 4;
    private static final double COORDINATE_PRECISION_SCALE = 10_000_000;

    // The raw Atlas to slice
    private Atlas rawAtlas;

    // The countries we're interested in slicing against
    private final Set<IsoCountry> countries;

    // Contains boundary MultiPolygons
    private final CountryBoundaryMap countryBoundaryMap;

    // Keep track of changes made during Point/Line and Relation slicing
    private final RawAtlasSimpleChangeSet slicedPointAndLineChanges = new RawAtlasSimpleChangeSet();
    private final RawAtlasRelationChangeSet slicedRelationChanges = new RawAtlasRelationChangeSet();

    // Mapping between Coordinate and created Temporary Point identifiers. This is to avoid
    // duplicate points at the same locations and to allow fast lookup to construct new lines
    // requiring the temporary point as a Line shape point
    private final CoordinateToNewPointMapping newPointCoordinates = new CoordinateToNewPointMapping();

    private final RawAtlasSlicingStatistic statistics = new RawAtlasSlicingStatistic(logger);

    /**
     * JTS has trouble dealing with high-precision double values. For this reason, we round all
     * coordinates to 7 degrees of precision. See {@link Coordinate} java doc for more detailed
     * explanation of scaling.
     *
     * @param coordinate
     *            The {@link Coordinate} to round
     */
    private static void roundCoordinate(final Coordinate coordinate)
    {
        coordinate.x = Math.round(coordinate.x * COORDINATE_PRECISION_SCALE)
                / COORDINATE_PRECISION_SCALE;
        coordinate.y = Math.round(coordinate.y * COORDINATE_PRECISION_SCALE)
                / COORDINATE_PRECISION_SCALE;
    }

    public RawAtlasCountrySlicer(final Atlas rawAtlas, final Set<IsoCountry> countries,
            final CountryBoundaryMap countryBoundaryMap)
    {
        this.rawAtlas = rawAtlas;
        this.countries = countries;
        this.countryBoundaryMap = countryBoundaryMap;
    }

    /**
     * Creates a mapping of all inner rings enclosed by the outer rings.
     *
     * @param outerToInnerMap
     *            The mapping to update any time we find an outer ring that contains an inner ring
     * @param outerRings
     *            The list of outer {@link LinearRing}s to use
     * @param innerRings
     *            The list of inner {@link LinearRing}s to use
     */
    final void findEnclosedRings(final MultiMap<Integer, Integer> outerToInnerMap,
            final List<LinearRing> outerRings, final List<LinearRing> innerRings)
    {
        for (int innerIndex = 0; innerIndex < innerRings.size(); innerIndex++)
        {
            final LinearRing inner = innerRings.get(innerIndex);
            boolean foundEnclosingOuter = false;
            for (int outerIndex = 0; outerIndex < outerRings.size(); outerIndex++)
            {
                final LinearRing outer = outerRings.get(outerIndex);
                final com.vividsolutions.jts.geom.Polygon outerPolygon = new com.vividsolutions.jts.geom.Polygon(
                        outer, null, JtsUtility.GEOMETRY_FACTORY);
                if (outerPolygon.contains(inner))
                {
                    foundEnclosingOuter = true;
                    outerToInnerMap.add(outerIndex, innerIndex);
                }
            }

            if (!foundEnclosingOuter)
            {
                // Isolated inner ring, invalid multipolygon
                logger.error("Found isolated inner member for Multipolygon Relation geometry: {}",
                        inner);
            }
        }
    }

    /**
     * @param closedOuterLines
     *            The list of closed outer {@link Line}s to use
     * @param closedInnerLines
     *            The list of closed inner {@link Line}s to use
     * @return a mapping of intersecting outer to inner relation members
     */
    final MultiMap<Integer, Integer> findIntersectingMembers(final List<Line> closedOuterLines,
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

                    // Make sure we're looking at the same country
                    if (fromSameCountry(outer, inner))
                    {
                        if (outer.intersects(new Polygon(inner)))
                        {
                            outerToInnerIntersectionMap.add(outerIndex, innerIndex);
                        }
                    }
                }
            }
        }

        return outerToInnerIntersectionMap;
    }

    /**
     * @param one
     *            The first {@link Line}
     * @param two
     *            The second {@link Line}
     * @return {@code true} if both lines belong to the same country
     */
    final boolean fromSameCountry(final Line one, final Line two)
    {
        final Optional<IsoCountry> countryOne = ISOCountryTag.first(one);
        final Optional<IsoCountry> countryTwo = ISOCountryTag.first(two);
        if (countryOne.isPresent() && countryTwo.isPresent())
        {
            return countryOne.get().equals(countryTwo.get());
        }
        return false;
    }

    /**
     * Country-slice the given Atlas. The slicing flow is:
     * <p>
     * <ul>
     * <li>1. Slice all lines
     * <li>2. Slice all points (assign a country code)
     * <li>3. Re-build the Atlas
     * <li>4. Slice the relations
     * <li>5. Build the fully sliced Atlas
     * </ul>
     * <p>
     *
     * @return a country-sliced {@link Atlas}
     */
    public Atlas slice()
    {
        logger.info("Starting country slicing Atlas {}", this.rawAtlas.getName());

        // Slice lines and points
        sliceLines();
        slicePoints();

        // Apply changes and rebuild the atlas with the changes before slicing relations
        final RawAtlasChangeSetBuilder simpleChangeBuilder = new RawAtlasSimpleChangeSetBuilder(
                this.rawAtlas, this.slicedPointAndLineChanges);
        final Atlas atlasWithSlicedWaysAndPoints = simpleChangeBuilder.applyChanges();
        this.rawAtlas = atlasWithSlicedWaysAndPoints;

        // Slice all relations and rebuild the atlas
        sliceRelations();

        // Apply changes from relation slicing and rebuild the fully-sliced atlas
        final RawAtlasChangeSetBuilder relationChangeBuilder = new RawAtlasRelationChangeSetBuilder(
                this.rawAtlas, this.slicedRelationChanges);
        final Atlas fullySlicedAtlas = relationChangeBuilder.applyChanges();
        logger.info("Finished country slicing Atlas {}", this.rawAtlas.getName());

        this.statistics.summary();
        return fullySlicedAtlas;
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
        final List<PolygonPiece> pieces = new ArrayList<>(members.size());
        final Deque<PolygonPiece> stack = new ArrayDeque<>(members.size());

        for (final RelationMember member : members)
        {
            final Line line = this.rawAtlas.line(member.getEntity().getIdentifier());
            if (line == null)
            {
                logger.error("Relation {} has a member {} that's not in the Atlas",
                        relationIdentifier, member.getEntity().getIdentifier());
                return null;
            }

            final PolygonPiece piece = new PolygonPiece(line);
            pieces.add(piece);
            stack.push(piece);
        }

        while (!stack.isEmpty())
        {
            final PolygonPiece currentPiece = stack.pop();
            while (!currentPiece.isClosed())
            {
                boolean foundConnection = false;

                // Try all the members first
                for (final PolygonPiece piece : stack)
                {
                    if (currentPiece.getEndNode().equals(piece.getStartNode()))
                    {
                        foundConnection = true;
                        currentPiece.merge(piece, false, false);
                    }
                    else if (currentPiece.getEndNode().equals(piece.getEndNode()))
                    {
                        foundConnection = true;
                        currentPiece.merge(piece, false, true);
                    }
                    else if (currentPiece.getStartNode().equals(piece.getStartNode()))
                    {
                        foundConnection = true;
                        currentPiece.merge(piece, true, false);
                    }
                    else if (currentPiece.getStartNode().equals(piece.getEndNode()))
                    {
                        foundConnection = true;
                        currentPiece.merge(piece, true, true);
                    }

                    if (foundConnection)
                    {
                        stack.remove(piece);
                        break;
                    }
                }

                if (!foundConnection)
                {
                    logger.error("Relation {} is a multipolygon that has a detached line {}",
                            relationIdentifier, currentPiece.getIdentifier());
                    return null;
                }
            }

            if (currentPiece.getLocations().size() < JTS_MINIMUM_RING_SIZE)
            {
                logger.warn(
                        "Relation {} claims to be multipolygon, but doesn't have a valid closed way {}",
                        relationIdentifier, currentPiece.getIdentifier());
                return null;
            }

            // If we reach here, we have a closed way and can build a valid LineRing
            results.add(JtsUtility.buildLinearRing(
                    JTS_COORDINATE_CONVERTER.convert(currentPiece.getLocations())));
        }

        return results;
    }

    /**
     * Converts the given {@link Line} into a JTS {@link Geometry} and slices it.
     *
     * @param line
     *            The {@link Line} to convert and slice
     * @return the resulting {@link Geometry} slices
     */
    private List<Geometry> convertToJtsGeometryAndSlice(final Line line)
    {
        // Create the JTS Geometry from Line
        final Geometry geometry;
        if (line.isClosed())
        {
            // An Area
            geometry = JTS_POLYGON_CONVERTER.convert(new Polygon(line));
        }
        else
        {
            // A Line
            geometry = JTS_POLYLINE_CONVERTER.convert(line.asPolyLine());
        }

        // Slice the JTS Geometry
        try
        {
            return this.countryBoundaryMap.slice(line.getIdentifier(), geometry);
        }
        catch (final TopologyException e)
        {
            logger.error("Topology Exception when slicing Line {}", line.getIdentifier(), e);
            return Collections.emptyList();
        }
    }

    /**
     * @param relation
     *            The {@link Relation} for which to create seeds for
     * @return an array of identifiers corresponding to the first unused identifier for each
     *         relation member in the given Relation
     */
    private long[] createIdentifierSeeds(final Relation relation)
    {
        // Assume any member can be split, so create a seed for each one
        final long[] seeds = new long[relation.members().size()];
        for (int seedIndex = 0; seedIndex < seeds.length; seedIndex++)
        {
            long identifier = relation.members().get(seedIndex).getEntity().getIdentifier();
            final long originalAtlasIdentifier = new ReverseIdentifierFactory()
                    .getFirstAtlasIdentifier(identifier);

            final Set<Long> createdWaysForIdentifier = this.slicedPointAndLineChanges
                    .getDeletedToCreatedLineMapping().get(originalAtlasIdentifier);
            if (createdWaysForIdentifier != null)
            {
                // Grab the first Atlas identifier for this entity and increment at offset
                identifier = identifier + createdWaysForIdentifier.size()
                        * CountrySlicingIdentifierFactory.IDENTIFIER_SCALE;
            }
            seeds[seedIndex] = identifier;
        }

        return seeds;
    }

    /**
     * Assigns {@link ISOCountryTag} and {@link SyntheticNearestNeighborCountryCodeTag} values for a
     * given {@link Geometry}, which is a {@link Line} since we don't have other non-Point
     * geometries in the raw Atlas.
     *
     * @param geometry
     *            The {@link Geometry} to create the tags for
     * @param tags
     *            The tags to which to add to
     * @return the resulting tags
     */
    private Map<String, String> createLineTags(final Geometry geometry,
            final Map<String, String> tags)
    {
        final String countryCode = CountryBoundaryMap.getGeometryProperty(geometry,
                ISOCountryTag.KEY);
        tags.put(ISOCountryTag.KEY, countryCode);
        final String usingNearestNeighbor = CountryBoundaryMap.getGeometryProperty(geometry,
                SyntheticNearestNeighborCountryCodeTag.KEY);
        if (usingNearestNeighbor != null)
        {
            tags.put(SyntheticNearestNeighborCountryCodeTag.KEY, usingNearestNeighbor);
        }
        return tags;
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
    private void createNewLineMemberForRelation(final LineString lineString,
            final long relationIdentifier,
            final CountrySlicingIdentifierFactory pointIdentifierGenerator,
            final CountrySlicingIdentifierFactory lineIdentifierGenerator)
    {
        // Keep track of identifiers that form the geometry of the new line
        final List<Long> newLineShapePoints = new ArrayList<>(lineString.getNumPoints());

        for (int exteriorIndex = 0; exteriorIndex < lineString.getNumPoints(); exteriorIndex++)
        {
            final Coordinate pointCoordinate = lineString.getCoordinateN(exteriorIndex);
            roundCoordinate(pointCoordinate);

            if (this.newPointCoordinates.containsCoordinate(pointCoordinate))
            {
                // A new point was already created for this coordinate. Look it up
                // and use it for the line we're creating
                newLineShapePoints
                        .add(this.newPointCoordinates.getPointForCoordinate(pointCoordinate));
            }
            else
            {
                // The point is in the original Raw Atlas or need to create a new one
                final Location pointLocation = JTS_LOCATION_CONVERTER
                        .backwardConvert(pointCoordinate);
                final Iterable<Point> rawAtlasPointsAtCoordinate = this.rawAtlas
                        .pointsAt(pointLocation);
                if (Iterables.isEmpty(rawAtlasPointsAtCoordinate))
                {
                    // Point doesn't exist in the raw Atlas, create a new one
                    final Map<String, String> newPointTags = createPointTags(pointLocation, false);

                    final TemporaryPoint newPoint = createNewPoint(pointCoordinate,
                            pointIdentifierGenerator, newPointTags);

                    // Store coordinate to avoid creating duplicate Points
                    this.newPointCoordinates.storeMapping(pointCoordinate,
                            newPoint.getIdentifier());

                    // Store this point to reconstruct the Line geometry
                    newLineShapePoints.add(newPoint.getIdentifier());

                    // Save the Point to add to the rebuilt atlas
                    this.slicedRelationChanges.createPoint(newPoint);
                }
                else
                {
                    // There is at least one Point at this Location in the raw Atlas
                    // Update all existing points to have the country code.
                    for (final Point rawAtlasPoint : rawAtlasPointsAtCoordinate)
                    {
                        // Add all point identifiers to make up the new Line
                        newLineShapePoints.add(rawAtlasPoint.getIdentifier());
                    }
                }
            }
        }

        // Create the patched Line
        final Map<String, String> newLineTags = createLineTags(lineString, new HashMap<>());
        final long newLineIdentifier = lineIdentifierGenerator.nextIdentifier();
        final TemporaryLine newLine = new TemporaryLine(newLineIdentifier, newLineShapePoints,
                newLineTags);
        this.slicedRelationChanges.createLine(newLine);

        // Create a new member for this relation
        final TemporaryRelationMember newOuterMember = new TemporaryRelationMember(
                newLine.getIdentifier(), RelationTypeTag.MULTIPOLYGON_ROLE_OUTER, ItemType.LINE);
        this.slicedRelationChanges.addRelationMember(relationIdentifier, newOuterMember);

        // Update synthetic tags
        updateSyntheticRelationMemberTag(relationIdentifier, newLineIdentifier);
    }

    /**
     * Creates a new {@link TemporaryPoint} at the given {@link Coordinate}. We can assume that any
     * new Points will be created at the country boundaries, so we can add the
     * {@link SyntheticBoundaryNodeTag} tag.
     *
     * @param coordinate
     *            The {@link Coordinate} of the new point
     * @param pointIdentifierFactory
     *            The {@link CountrySlicingIdentifierFactory} to calculate new point identifier
     * @param pointTags
     *            The tags for this new point
     * @return the {@link TemporaryPoint}
     */
    private TemporaryPoint createNewPoint(final Coordinate coordinate,
            final CountrySlicingIdentifierFactory pointIdentifierFactory,
            final Map<String, String> pointTags)
    {
        if (!pointIdentifierFactory.hasMore())
        {
            throw new CoreException(
                    "Country Slicing exceeded maximum number {} of supported new points at Coordinate {}",
                    AbstractIdentifierFactory.IDENTIFIER_SCALE, coordinate);
        }
        else
        {
            // Add the synthetic boundary node tags
            pointTags.put(SyntheticBoundaryNodeTag.KEY, SyntheticBoundaryNodeTag.YES.toString());

            return new TemporaryPoint(pointIdentifierFactory.nextIdentifier(),
                    JTS_LOCATION_CONVERTER.backwardConvert(coordinate), pointTags);
        }
    }

    /**
     * Assigns {@link ISOCountryTag}, {@link SyntheticNearestNeighborCountryCodeTag} and
     * {@link SyntheticBoundaryNodeTag} values for a given {@link Location}. The logic behind
     * {@link SyntheticBoundaryNodeTag} assignment is if we're creating a new {@link Point}, it's
     * only going to happen at the boundary, since that's where slicing happens. We have two ways of
     * retrieving Points - 1. from the original un-sliced Raw Atlas 2. from the newPointCoordinates
     * mapping. We first check to see if there is already a {@link TemporaryPoint} for this
     * {@link Coordinate} in the map (to avoid creating duplicate points). If there's not, we check
     * the raw Atlas to see if there is already a {@link Point} at this {@link Location}. If the
     * atlas has a {@link Point} there, we know to assign the
     * {@link SyntheticBoundaryNodeTag#EXISTING} value, as this {@link Point} is already part of the
     * raw OSM data. If the raw Atlas doesn't contain a {@link Point} at this {@link Location}, then
     * we create a new {@link TemporaryPoint} and assign the {@link SyntheticBoundaryNodeTag#YES}
     * value.
     *
     * @param location
     *            The {@link Location} for which to assign country code tags
     * @param fromRawAtlas
     *            {@code true} if this {@link Location} is already an {@link Point} in the raw Atlas
     * @return the created tags
     */
    private Map<String, String> createPointTags(final Location location, final boolean fromRawAtlas)
    {
        final Map<String, String> tags = new HashMap<>();

        // Get the country code details
        final CountryCodeProperties countryDetails = this.countryBoundaryMap
                .getCountryCodeISO3(location);

        // Store the country code
        tags.put(ISOCountryTag.KEY, countryDetails.getIso3CountryCode());

        // If we used nearest neighbor logic to determine the country code, add a tag
        // to indicate this
        if (countryDetails.usingNearestNeighbor())
        {
            tags.put(SyntheticNearestNeighborCountryCodeTag.KEY,
                    SyntheticNearestNeighborCountryCodeTag.YES.toString());
        }

        // For any border nodes, add the existing tag
        if (fromRawAtlas && countryDetails.inMultipleCountries())
        {
            // TODO - Edge case: ferries that end just short of the country boundary should have an
            // existing synthetic boundary node tag. One approach to generate these is to snap the
            // ferry end point to the closest MultiPolygon boundary and if it's within a configured
            // distance, assign the tag. However, this is facing performance issues and will have to
            // be addressed in the future.
            tags.put(SyntheticBoundaryNodeTag.KEY, SyntheticBoundaryNodeTag.EXISTING.toString());
        }

        return tags;
    }

    /**
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
        // Check if all outer ways are closed. If there are any that aren't closed, we need to
        // build LineRings and fix the gaps
        return members.stream().filter(member ->
        {
            final Line line = this.rawAtlas.line(member.getEntity().getIdentifier());
            if (line == null)
            {
                logger.error("Line member {} for Relation {} is not the in raw Atlas.",
                        member.getEntity().getIdentifier(), relationIdentifier);
            }

            if (!closed)
            {
                return line == null || !line.isClosed();
            }
            else
            {
                return line == null || line.isClosed();
            }
        }).collect(Collectors.toList());
    }

    /**
     * Takes in a list of relation members and groups them by country. The output is a map of
     * country to list of members.
     *
     * @param members
     *            The members we wish to group
     */
    private Map<String, List<TemporaryRelationMember>> groupRelationMembersByCountry(
            final List<TemporaryRelationMember> members)
    {
        final Map<String, List<TemporaryRelationMember>> countryEntityMap = members.stream()
                .collect(Collectors.groupingBy(member ->
                {
                    final AtlasEntity entity;
                    if (member.getType() == ItemType.POINT)
                    {
                        entity = this.rawAtlas.point(member.getIdentifier());
                    }
                    else if (member.getType() == ItemType.LINE)
                    {
                        entity = this.rawAtlas.line(member.getIdentifier());
                    }
                    else if (member.getType() == ItemType.RELATION)
                    {
                        entity = this.rawAtlas.relation(member.getIdentifier());
                    }
                    else
                    {
                        throw new CoreException("Unsupported Relation Member of Type {}",
                                member.getType());
                    }

                    if (entity != null)
                    {
                        // Entity is in the Raw Atlas
                        final Optional<IsoCountry> possibleCountryCode = ISOCountryTag
                                .first(entity);
                        if (possibleCountryCode.isPresent())
                        {
                            return possibleCountryCode.get().getIso3CountryCode();
                        }
                        else
                        {
                            logger.error(
                                    "Existing Raw Atlas Relation Member {} does not have a country code!",
                                    member.getIdentifier());
                        }
                    }
                    else
                    {
                        // Entity is not in the Raw Atlas
                        final TemporaryLine newLine = this.slicedRelationChanges.getCreatedLines()
                                .get(member.getIdentifier());
                        if (newLine != null)
                        {
                            final String countryCode = newLine.getTags().get(ISOCountryTag.KEY);
                            if (countryCode != null)
                            {
                                return countryCode;
                            }
                            else
                            {
                                logger.error(
                                        "Newly added Relation Member {} does not have a country code!",
                                        member.getIdentifier());
                            }
                        }
                    }

                    // Missing Entity, log and assign missing country
                    logger.error("Could not find Relation Member {} in Atlas or Added Lines List!",
                            member.getIdentifier());
                    return ISOCountryTag.COUNTRY_MISSING;
                }));

        return countryEntityMap;
    }

    /**
     * Check if the {@link Geometry} should be filtered out based on the provided bound.
     *
     * @param geometry
     *            The {@link Geometry} to check.
     * @return {@code true} if the given geometry should be filtered out.
     */
    private boolean isOutsideWorkingBound(final Geometry geometry)
    {
        final Optional<IsoCountry> countryCode = IsoCountry.forCountryCode(
                CountryBoundaryMap.getGeometryProperty(geometry, ISOCountryTag.KEY));
        if (countryCode.isPresent())
        {
            return this.countries != null && !this.countries.isEmpty()
                    && !this.countries.contains(countryCode.get());
        }

        // Assume it's inside the bound
        return false;
    }

    /**
     * Checks if there is a single slice or if all of the slices are in the same country AND the
     * line being sliced isn't a feature that extends into the water, such as a Ferry or Pier.
     *
     * @param line
     *            The {@link Line} that was sliced
     * @param slices
     *            The resulting sliced pieces
     * @return {@code true} if the slices for this line are all part of the same country
     */
    private boolean lineBelongsToSingleCountry(final Line line, final List<Geometry> slices)
    {
        // TODO - this is an optimization that hides the corner case of not slicing any pier or
        // ferry that extends into the ocean. Because the ocean isn't viewed as another country, the
        // pier and ferries are not sliced at the country boundary and ocean. This should be fixed
        // for consistency issues.
        return slices.size() == 1 || CountryBoundaryMap.isSameCountry(slices);
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
    private void markRemovedMemberLineForDeletion(final Line line, final long relationIdentifier)
    {
        // Only mark this line for deletion if this line isn't part of any other relations
        final boolean isPartOfOtherRelations = line.relations().stream()
                .filter(partOf -> partOf.getIdentifier() != relationIdentifier).count() > 0;
        if (!isPartOfOtherRelations)
        {
            this.slicedRelationChanges.deleteLine(line.getIdentifier());
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
    private void mergeOverlappingClosedMembers(final Relation relation,
            final List<RelationMember> outers, final List<RelationMember> inners)
    {
        if (inners == null || inners.isEmpty())
        {
            return;
        }

        final long relationIdentifier = relation.getIdentifier();
        final List<RelationMember> closedOuters = generateMemberList(relationIdentifier, outers,
                true);
        final List<RelationMember> closedInners = generateMemberList(relationIdentifier, inners,
                true);
        final List<Line> closedOuterLines = new ArrayList<>();
        closedOuters.forEach(outer -> closedOuterLines
                .add(this.rawAtlas.line(outer.getEntity().getIdentifier())));

        final List<Line> closedInnerLines = new ArrayList<>();
        closedInners.forEach(inner -> closedInnerLines
                .add(this.rawAtlas.line(inner.getEntity().getIdentifier())));

        final MultiMap<Integer, Integer> outerToInnerIntersectionMap = findIntersectingMembers(
                closedOuterLines, closedInnerLines);

        final long[] identifierSeeds = createIdentifierSeeds(relation);
        final CountrySlicingIdentifierFactory lineIdentifierGenerator = new CountrySlicingIdentifierFactory(
                identifierSeeds[0]);
        final CountrySlicingIdentifierFactory pointIdentifierGenerator = new CountrySlicingIdentifierFactory(
                identifierSeeds);

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
            final com.vividsolutions.jts.geom.Polygon outerPolygon = new com.vividsolutions.jts.geom.Polygon(
                    outerRing, null, JtsUtility.GEOMETRY_FACTORY);
            boolean successfulMerge = true;

            for (final int innerIndex : innerIndices)
            {
                // Convert inner to ring, create polygon, take union of all
                final Line inner = closedInnerLines.get(innerIndex);

                final LinearRing innerRing = JTS_LINEAR_RING_CONVERTER
                        .convert(new Polygon(inner.getRawGeometry()));
                final com.vividsolutions.jts.geom.Polygon innerPolygon = new com.vividsolutions.jts.geom.Polygon(
                        innerRing, null, JtsUtility.GEOMETRY_FACTORY);

                try
                {
                    mergedMembers = outerPolygon.difference(innerPolygon);
                }
                catch (final Exception e)
                {
                    successfulMerge = false;
                    logger.error(
                            "Error combining intersecting outer {} and inner {} members for relation {}",
                            outer, inner, relationIdentifier);
                }
            }

            if (successfulMerge && mergedMembers != null)
            {
                // Remove the outer member
                final TemporaryRelationMember outerToRemove = new TemporaryRelationMember(
                        outer.getIdentifier(), RelationTypeTag.MULTIPOLYGON_ROLE_OUTER,
                        outer.getType());
                this.slicedRelationChanges.deleteRelationMember(relationIdentifier, outerToRemove);
                markRemovedMemberLineForDeletion(outer, relationIdentifier);

                // Remove the inner members
                for (final int innerIndex : innerIndices)
                {
                    final Line inner = closedInnerLines.get(innerIndex);
                    final TemporaryRelationMember innerToRemove = new TemporaryRelationMember(
                            inner.getIdentifier(), RelationTypeTag.MULTIPOLYGON_ROLE_INNER,
                            ItemType.LINE);
                    this.slicedRelationChanges.deleteRelationMember(relationIdentifier,
                            innerToRemove);
                    markRemovedMemberLineForDeletion(inner, relationIdentifier);
                }

                // Create points (if necessary) and lines for the merged member
                final com.vividsolutions.jts.geom.Polygon merged = (com.vividsolutions.jts.geom.Polygon) mergedMembers;
                final LineString exterior = merged.getExteriorRing();

                // Set the proper country code
                CountryBoundaryMap.setGeometryProperty(exterior, ISOCountryTag.KEY,
                        ISOCountryTag.first(outer).get().getIso3CountryCode());

                // Create points, lines and update members
                createNewLineMemberForRelation(exterior, relationIdentifier,
                        pointIdentifierGenerator, lineIdentifierGenerator);
            }
        }
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
    private void patchNonClosedMembers(final Relation relation, final List<RelationMember> outers,
            final List<RelationMember> inners)
    {
        if (outers == null || outers.isEmpty())
        {
            return;
        }

        final long relationIdentifier = relation.getIdentifier();

        // Grab all non-closed outer ways
        final List<RelationMember> nonClosedOuters = generateMemberList(relationIdentifier, outers,
                false);

        if (!nonClosedOuters.isEmpty())
        {
            final List<LinearRing> outerRings = buildRings(nonClosedOuters, relationIdentifier);
            if (outerRings != null && !outerRings.isEmpty())
            {
                List<LinearRing> innerRings = null;
                final MultiMap<Integer, Integer> outerToInnerMap = new MultiMap<>();
                List<RelationMember> nonClosedInners = null;
                if (inners != null && !inners.isEmpty())
                {
                    // Grab all non-closed inner ways
                    nonClosedInners = generateMemberList(relationIdentifier, inners, false);
                    innerRings = buildRings(nonClosedInners, relationIdentifier);
                    if (innerRings != null && !innerRings.isEmpty())
                    {
                        // Try to find the outer ring that contains the inner ring
                        findEnclosedRings(outerToInnerMap, outerRings, innerRings);
                    }
                }

                for (int outerIndex = 0; outerIndex < outerRings.size(); outerIndex++)
                {
                    final LinearRing outerRing = outerRings.get(outerIndex);
                    LinearRing[] holes = null;
                    if (outerToInnerMap.containsKey(outerIndex))
                    {
                        final List<Integer> innerIndexes = outerToInnerMap.get(outerIndex);
                        holes = new LinearRing[innerIndexes.size()];
                        for (int innerIndex = 0; innerIndex < innerIndexes.size(); innerIndex++)
                        {
                            holes[innerIndex] = innerRings.get(innerIndexes.get(innerIndex));
                        }
                    }

                    final com.vividsolutions.jts.geom.Polygon polygon = new com.vividsolutions.jts.geom.Polygon(
                            outerRing, holes, JtsUtility.GEOMETRY_FACTORY);

                    // Check if the polygon is valid. Sometimes polygons from relations can be
                    // invalid, such as for large boundaries.
                    if (!polygon.isValid())
                    {
                        logger.error("Polygon created by relation {} is invalid",
                                relationIdentifier);
                        return;
                    }

                    final List<LineString> borderLines;
                    try
                    {
                        borderLines = this.countryBoundaryMap.clipBoundary(relationIdentifier,
                                polygon);
                    }
                    catch (final Exception e)
                    {
                        logger.error("Error processing relation {}, message: {}, geometry: {}",
                                relationIdentifier, e.getMessage(), polygon.toString());
                        return;
                    }

                    if (borderLines == null || borderLines.size() == 0)
                    {
                        // There was no cut, we don't need to update the relation
                        return;
                    }

                    if (borderLines.size() >= AbstractIdentifierFactory.IDENTIFIER_SCALE)
                    {
                        logger.error("Borderline got cut into more than 999 pieces for relation {}",
                                relationIdentifier);
                        return;
                    }

                    final long[] identifierSeeds = createIdentifierSeeds(relation);
                    final CountrySlicingIdentifierFactory lineIdentifierGenerator = new CountrySlicingIdentifierFactory(
                            identifierSeeds[0]);
                    final CountrySlicingIdentifierFactory pointIdentifierGenerator = new CountrySlicingIdentifierFactory(
                            identifierSeeds);

                    // Create geometry for all new border lines
                    borderLines.forEach(borderLine -> createNewLineMemberForRelation(borderLine,
                            relationIdentifier, pointIdentifierGenerator, lineIdentifierGenerator));
                }
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
    private void preProcessMultiPolygonRelation(final Relation relation)
    {
        final Map<String, List<RelationMember>> roleMap = relation.members().stream()
                .filter(member -> member.getEntity().getType() == ItemType.LINE)
                .collect(Collectors.groupingBy(RelationMember::getRole));
        final List<RelationMember> outers = roleMap.get(RelationTypeTag.MULTIPOLYGON_ROLE_OUTER);
        final List<RelationMember> inners = roleMap.get(RelationTypeTag.MULTIPOLYGON_ROLE_INNER);

        patchNonClosedMembers(relation, outers, inners);
        mergeOverlappingClosedMembers(relation, outers, inners);
    }

    /**
     * Processes each slice by updating corresponding tags ({@link ISOCountryTag},
     * {@link SyntheticNearestNeighborCountryCodeTag}, {@link SyntheticBoundaryNodeTag} and creating
     * {@link RawAtlasSimpleChangeSet}s to keep track of created, updated and deleted {@link Point}s
     * and {@link Line}s.
     *
     * @param line
     *            The {@link Line} that was sliced
     * @param slices
     *            The resulting {@link Geometry} slices
     */
    private void processLineSlices(final Line line, final List<Geometry> slices)
    {
        if (slices == null || slices.isEmpty())
        {
            // No slices generated or an error in slicing, create missing country code
            final Map<String, String> tags = new HashMap<>();
            tags.put(ISOCountryTag.KEY, ISOCountryTag.COUNTRY_MISSING);
            this.slicedPointAndLineChanges.updateLineTags(line.getIdentifier(), tags);
            updateLineShapePoints(line);
        }
        else if (lineBelongsToSingleCountry(line, slices))
        {
            // This line belongs to a single country
            this.slicedPointAndLineChanges.updateLineTags(line.getIdentifier(),
                    createLineTags(slices.get(0), line.getTags()));
            updateLineShapePoints(line);
        }
        else if (slices.size() < AbstractIdentifierFactory.IDENTIFIER_SCALE)
        {
            // Used to generate identifiers for new points and lines
            final CountrySlicingIdentifierFactory lineIdentifierFactory = new CountrySlicingIdentifierFactory(
                    line.getIdentifier());
            final CountrySlicingIdentifierFactory pointIdentifierFactory = new CountrySlicingIdentifierFactory(
                    line.getIdentifier());

            final List<TemporaryLine> createdLines = new ArrayList<>();

            try
            {
                for (final Geometry slice : slices)
                {
                    // Check if the slice is within the working bound
                    if (isOutsideWorkingBound(slice))
                    {
                        continue;
                    }

                    // Keep track of identifiers that form the geometry of the new line
                    final List<Long> newLineShapePoints = new ArrayList<>(slice.getNumPoints());

                    final Coordinate[] jtsSliceCoordinates = slice.getCoordinates();
                    for (final Coordinate coordinate : jtsSliceCoordinates)
                    {
                        // Because country shapes do not share border, we are rounding coordinate
                        // first to consider very close nodes as one
                        roundCoordinate(coordinate);

                        if (this.newPointCoordinates.containsCoordinate(coordinate))
                        {
                            // A new point was already created for this coordinate. Look it up and
                            // use it for the line we're creating
                            newLineShapePoints.add(
                                    this.newPointCoordinates.getPointForCoordinate(coordinate));
                        }
                        else
                        {
                            // The point in the original Raw Atlas or we need to create a new one
                            final Location coordinateLocation = JTS_LOCATION_CONVERTER
                                    .backwardConvert(coordinate);
                            final Iterable<Point> rawAtlasPointsAtSliceVertex = this.rawAtlas
                                    .pointsAt(coordinateLocation);

                            if (Iterables.isEmpty(rawAtlasPointsAtSliceVertex))
                            {
                                // Grab the country code tags for this point
                                final Map<String, String> pointTags = createPointTags(
                                        coordinateLocation, false);

                                // Need to create a new point
                                final TemporaryPoint newPoint = createNewPoint(coordinate,
                                        pointIdentifierFactory, pointTags);

                                // Store coordinate to avoid creating duplicate Points
                                this.newPointCoordinates.storeMapping(coordinate,
                                        newPoint.getIdentifier());

                                // Store this point to reconstruct the Line geometry
                                newLineShapePoints.add(newPoint.getIdentifier());

                                // Save the Point to add to the rebuilt atlas
                                this.slicedPointAndLineChanges.createPoint(newPoint);
                            }
                            else
                            {
                                // Grab the country code tags for this point
                                final Map<String, String> pointTags = createPointTags(
                                        coordinateLocation, true);

                                // There is at least one point at this location in the raw Atlas.
                                // Update all existing points to have the country code.
                                for (final Point rawAtlasPoint : rawAtlasPointsAtSliceVertex)
                                {
                                    // Update the country codes
                                    this.slicedPointAndLineChanges.updatePointTags(
                                            rawAtlasPoint.getIdentifier(), pointTags);

                                    // Add all point identifiers to make up the new Line
                                    newLineShapePoints.add(rawAtlasPoint.getIdentifier());
                                }
                            }
                        }
                    }

                    // Extract relevant tag values for this slice
                    final Map<String, String> lineTags = createLineTags(slice, line.getTags());

                    // Create and store the new line
                    final TemporaryLine createdLine = new TemporaryLine(
                            lineIdentifierFactory.nextIdentifier(), newLineShapePoints, lineTags);
                    createdLines.add(createdLine);
                }

                // Update the change with the added and removed lines
                createdLines.forEach(
                        createdLine -> this.slicedPointAndLineChanges.createLine(createdLine));

                this.slicedPointAndLineChanges.deleteLine(line.getIdentifier());
                this.slicedPointAndLineChanges.createDeletedToCreatedMapping(line.getIdentifier(),
                        createdLines.stream().map(TemporaryLine::getIdentifier)
                                .collect(Collectors.toSet()));

                // Record a successful slice
                this.statistics.recordSlicedLine();
            }
            catch (final CoreException e)
            {
                // TODO - Consider shifting to a 4 digit name space for identifiers
                logger.error(
                        "Country slicing exceeded maximum point identifier name space of {} for Line {}",
                        AbstractIdentifierFactory.IDENTIFIER_SCALE, line.getIdentifier(), e);
                this.statistics.recordSkippedLine();
            }
        }
        else
        {
            // TODO - Consider expanding to a 4 digit name space for identifiers
            logger.error(
                    "Country slicing exceeded maximum line identifier name space of {} for Line {}",
                    AbstractIdentifierFactory.IDENTIFIER_SCALE, line.getIdentifier());
            this.statistics.recordSkippedLine();
        }
    }

    /**
     * Converts the given {@link Line} to a JTS {@link Geometry}, slices the geometry and updates
     * all corresponding {@link Point}s and {@link Line}s in the given raw Atlas.
     *
     * @param line
     *            The {@link Line} to slice
     */
    private void sliceLine(final Line line)
    {
        this.statistics.recordProcessedLine();
        final List<Geometry> slices = convertToJtsGeometryAndSlice(line);
        processLineSlices(line, slices);
    }

    // TODO come back and verify we're keeping track of all required statistics
    // TODO - create better separation of behavior. Some relation specific things can be moved out.

    /**
     * Slices all the {@link Line}s in the given raw Atlas.
     */
    private void sliceLines()
    {
        this.rawAtlas.lines().forEach(this::sliceLine);
    }

    /**
     * Updates all points that haven't been assigned a country code after line-slicing. This
     * includes any stand-alone points (e.g. trees, barriers) or points that fell outside of any
     * country boundary.
     */
    private void slicePoints()
    {
        this.rawAtlas.points().forEach(point ->
        {
            final long pointIdentifier = point.getIdentifier();

            // Only update points that haven't been assigned a country code after way slicing
            if (!this.slicedPointAndLineChanges.getUpdatedPointTags().containsKey(pointIdentifier))
            {
                this.statistics.recordProcessedPoint();
                this.slicedPointAndLineChanges.updatePointTags(pointIdentifier,
                        createPointTags(point.getLocation(), true));
            }
        });
    }

    /**
     * Slice a given {@link Relation}. In case it's a multipolygon relation, we do some
     * pre-processing to see if there are any gaps that need to be patched or members that need to
     * be merged. Once that's completed, we group the members by country and either assign a country
     * code or break the relation into two or more new relations.
     *
     * @param relation
     *            The {@link Relation} to slice
     */
    private List<TemporaryRelation> sliceRelation(final Relation relation, final List<Long> parents)
    {
        this.statistics.recordProcessedRelation();

        if (Validators.isOfType(relation, RelationTypeTag.class, RelationTypeTag.BOUNDARY,
                RelationTypeTag.MULTIPOLYGON))
        {
            preProcessMultiPolygonRelation(relation);
        }

        return updateAndSplitRelation(relation, parents);
    }

    /**
     * Slices all {@link Relation}s in the given raw Atlas.
     */
    private void sliceRelations()
    {
        this.rawAtlas.relationsLowerOrderFirst()
                .forEach(relation -> this.sliceRelation(relation, new ArrayList<>()));
    }

    /**
     * Take a {@link Relation} and create one or more {@link TemporaryRelation}s to add to the
     * sliced Atlas. We first add all existing members if they haven't been removed during slicing.
     * We then add any new members for this relation. Finally, we group by members by country and
     * see if we need to split up the existing relation into two or more separate relations. If
     * we're dealing with a member relation, then we call ourselves recursively and pass in the
     * known parent list.
     *
     * @param relation
     *            The {@link Relation} to update
     * @param parents
     *            The list of parent {@link Relation}s
     */
    private List<TemporaryRelation> updateAndSplitRelation(final Relation relation,
            final List<Long> parents)
    {
        // Work with TemporaryRelationMembers instead of RelationMembers. There is less overhead
        // this way - we don't need actual atlas entities, just their identifiers
        final List<TemporaryRelation> createdRelations = new ArrayList<>();
        final List<TemporaryRelationMember> members = new ArrayList<>();

        final List<TemporaryRelationMember> removedMembers = Optional
                .ofNullable(this.slicedRelationChanges.getDeletedRelationMembers()
                        .get(relation.getIdentifier()))
                .orElse(new ArrayList<>());

        for (final RelationMember member : relation.members())
        {
            final long memberIdentifier = member.getEntity().getIdentifier();
            final ItemType memberType = member.getEntity().getType();

            final TemporaryRelationMember temporaryMember = new TemporaryRelationMember(
                    memberIdentifier, member.getRole(), memberType);
            if (!removedMembers.contains(temporaryMember))
            {
                switch (memberType)
                {
                    case LINE:
                    case POINT:
                        members.add(temporaryMember);
                        break;
                    case RELATION:
                        parents.add(relation.getIdentifier());
                        final Relation subRelation = this.rawAtlas.relation(memberIdentifier);

                        if (subRelation == null)
                        {
                            logger.debug("Could not find relation member {} in atlas",
                                    memberIdentifier);
                            // Put the member back into the relation. Missing members will be
                            // handled on a case by case basis
                            members.add(temporaryMember);
                            break;
                        }

                        final List<TemporaryRelation> slicedMembers;
                        if (!parents.contains(subRelation.getIdentifier()))
                        {
                            slicedMembers = sliceRelation(subRelation, parents);
                        }
                        else
                        {
                            logger.error("Relation {} has a loop! Parent tree: {}",
                                    subRelation.getIdentifier(), parents);
                            slicedMembers = null;
                        }

                        if (slicedMembers != null)
                        {
                            this.statistics.recordSlicedRelation();
                            slicedMembers.forEach(
                                    slicedRelation -> members.add(new TemporaryRelationMember(
                                            memberIdentifier, member.getRole(), memberType)));
                        }
                        else
                        {
                            members.add(temporaryMember);
                        }
                        break;
                    default:
                        // Raw Atlas should not have any other types than the ones here
                        throw new CoreException("Unsupported {} Member for Relation {}", memberType,
                                relation.getIdentifier());
                }
            }
            else
            {
                // Found member to remove, remove it to shorten our list
                removedMembers.remove(temporaryMember);
            }
        }

        // Add in any new members as a result of multipolygon fixes
        final List<TemporaryRelationMember> addedMembers = Optional.ofNullable(
                this.slicedRelationChanges.getAddedRelationMembers().get(relation.getIdentifier()))
                .orElse(new ArrayList<>());
        addedMembers.forEach(newMember -> members.add(newMember));

        // Group members by country
        final Map<String, List<TemporaryRelationMember>> countryEntityMap = groupRelationMembersByCountry(
                members);

        final List<TemporaryRelationMember> membersWithoutCountry;
        if (countryEntityMap.containsKey(ISOCountryTag.COUNTRY_MISSING))
        {
            membersWithoutCountry = countryEntityMap.remove(ISOCountryTag.COUNTRY_MISSING);
        }
        else
        {
            membersWithoutCountry = Collections.emptyList();
        }

        final int countryCount = countryEntityMap.size();

        if (countryCount == 0)
        {
            // Assign a missing country
            final Map<String, String> tags = new HashMap<>();
            tags.put(ISOCountryTag.KEY, ISOCountryTag.COUNTRY_MISSING);
            this.slicedRelationChanges.updateRelationTags(relation.getIdentifier(), tags);
        }
        else if (countryCount == 1)
        {
            // Assign the single country
            final Map<String, String> tags = new HashMap<>();
            tags.put(ISOCountryTag.KEY, countryEntityMap.keySet().iterator().next());
            this.slicedRelationChanges.updateRelationTags(relation.getIdentifier(), tags);
        }
        else
        {
            // Hard Case - multiple countries found, implies relation crosses boundary. For
            // now, for features without a country code (points and lines not covered by any
            // boundary), we put a copy for every sliced piece to ensure integrity. We need to
            // remove the original relation and create a new relation for each country
            this.slicedRelationChanges.deleteRelation(relation.getIdentifier());

            final CountrySlicingIdentifierFactory relationIdentifierFactory = new CountrySlicingIdentifierFactory(
                    relation.getIdentifier());

            // Create a new Relation for each country
            countryEntityMap.entrySet().forEach(entry ->
            {
                final List<TemporaryRelationMember> candidateMembers = new ArrayList<>();
                candidateMembers.addAll(entry.getValue());
                candidateMembers.addAll(membersWithoutCountry);

                if (!candidateMembers.isEmpty())
                {
                    // Create new relation tags - add in a country code,
                    final Map<String, String> relationTags = relation.getTags();
                    relationTags.put(ISOCountryTag.KEY, entry.getKey());

                    // Add any synthetic tags for this relation that were created during
                    // multipolygon fixing
                    if (this.slicedRelationChanges.getUpdatedRelationTags()
                            .containsKey(relation.getIdentifier()))
                    {
                        relationTags.putAll(this.slicedRelationChanges.getUpdatedRelationTags()
                                .get(relation.getIdentifier()));
                    }

                    final TemporaryRelation newRelation = new TemporaryRelation(
                            relationIdentifierFactory.nextIdentifier(), relationTags);
                    candidateMembers.forEach(member -> newRelation.addMember(member));
                    createdRelations.add(newRelation);
                    this.slicedRelationChanges.createRelation(newRelation);
                }
            });
        }

        return createdRelations;
    }

    /**
     * Updates all of the given {@link Line}'s shape points' tags.
     *
     * @param line
     *            The {@link Line} whose shape points to update
     */
    private void updateLineShapePoints(final Line line)
    {
        for (final Location location : line.asPolyLine())
        {
            for (final Point point : this.rawAtlas.pointsAt(location))
            {
                this.statistics.recordProcessedPoint();
                this.slicedPointAndLineChanges.updatePointTags(point.getIdentifier(),
                        createPointTags(location, true));
            }
        }
    }

    /**
     * Updates {@link Relation} tags with the {@link SyntheticRelationMemberAdded} value to keep
     * track of any synthetic line members that were created and added to the relation.
     *
     * @param relationIdentifier
     *            The {@link Relation} identifier whose tags to update
     * @param newLineIdentifier
     *            The {@link Line} identifier that got created
     */
    private void updateSyntheticRelationMemberTag(final long relationIdentifier,
            final long newLineIdentifier)
    {
        final Map<String, String> updatedTags = this.slicedRelationChanges.getUpdatedRelationTags()
                .get(relationIdentifier);
        if (updatedTags == null)
        {
            // No updated tags exist for this relation, create new ones and insert synthetic value
            final Map<String, String> newTags = new HashMap<>();
            newTags.put(SyntheticRelationMemberAdded.KEY.toString(),
                    String.valueOf(newLineIdentifier));
            this.slicedRelationChanges.updateRelationTags(relationIdentifier, newTags);
        }
        else
        {
            // Updated tags exist
            if (updatedTags.containsKey(SyntheticRelationMemberAdded.KEY))
            {
                // Synthetic key exists, need to append another member
                final String newValue = updatedTags.get(SyntheticRelationMemberAdded.KEY)
                        + SyntheticRelationMemberAdded.MEMBER_DELIMITER
                        + String.valueOf(newLineIdentifier);
                updatedTags.put(SyntheticRelationMemberAdded.KEY.toString(), newValue);
            }
            else
            {
                // Synthetic key doesn't exist, need to insert a new value
                updatedTags.put(SyntheticRelationMemberAdded.KEY.toString(),
                        String.valueOf(newLineIdentifier));
            }
            this.slicedRelationChanges.updateRelationTags(relationIdentifier, updatedTags);
        }
    }
}
