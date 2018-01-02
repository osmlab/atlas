package org.openstreetmap.atlas.geography.atlas.raw.slicing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.PolyLine;
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
import org.openstreetmap.atlas.geography.atlas.raw.slicing.changeset.ChangeSetHandler;
import org.openstreetmap.atlas.geography.atlas.raw.slicing.changeset.RelationChangeSet;
import org.openstreetmap.atlas.geography.atlas.raw.slicing.changeset.RelationChangeSetHandler;
import org.openstreetmap.atlas.geography.atlas.raw.slicing.changeset.SimpleChangeSet;
import org.openstreetmap.atlas.geography.atlas.raw.slicing.temporary.TemporaryLine;
import org.openstreetmap.atlas.geography.atlas.raw.slicing.temporary.TemporaryPoint;
import org.openstreetmap.atlas.geography.atlas.raw.slicing.temporary.TemporaryRelation;
import org.openstreetmap.atlas.geography.atlas.raw.slicing.temporary.TemporaryRelationMember;
import org.openstreetmap.atlas.geography.boundary.CountryBoundaryMap;
import org.openstreetmap.atlas.geography.converters.jts.JtsUtility;
import org.openstreetmap.atlas.locale.IsoCountry;
import org.openstreetmap.atlas.tags.ISOCountryTag;
import org.openstreetmap.atlas.tags.RelationTypeTag;
import org.openstreetmap.atlas.tags.SyntheticRelationMemberAdded;
import org.openstreetmap.atlas.tags.annotations.validation.Validators;
import org.openstreetmap.atlas.utilities.collections.Iterables;
import org.openstreetmap.atlas.utilities.collections.MultiIterable;
import org.openstreetmap.atlas.utilities.maps.MultiMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;

/**
 * The {@link RawAtlasRelationSlicer} consumes a partially sliced (only points and lines are sliced)
 * raw Atlas and produces a fully country-sliced Atlas as output.
 *
 * @author mgostintsev
 */
public class RawAtlasRelationSlicer extends RawAtlasSlicer
{
    private static final Logger logger = LoggerFactory.getLogger(RawAtlasRelationSlicer.class);

    // The raw Atlas to slice
    private final Atlas partiallySlicedRawAtlas;

    // Push this into Base class
    // Keep track of changes made during Point/Line and Relation slicing
    private final SimpleChangeSet slicedPointAndLineChanges;
    private final RelationChangeSet slicedRelationChanges;

    /**
     * Determines whether any of the given members was sliced.
     *
     * @param members
     *            The members we want to look at
     * @return {@code true} if any of the members was sliced
     */
    private static boolean containsSlicedMember(final Iterable<RelationMember> members)
    {
        for (final RelationMember member : members)
        {
            // We know a member was sliced if the country code was incremented during the line
            // slicing process
            if (new ReverseIdentifierFactory()
                    .getCountryCode(member.getEntity().getIdentifier()) != 0)
            {
                return true;
            }
        }
        return false;
    }

    public RawAtlasRelationSlicer(final Atlas atlas, final Set<IsoCountry> countries,
            final CountryBoundaryMap countryBoundaryMap, final SimpleChangeSet simpleChangeSet,
            final RelationChangeSet relationChangeSet,
            final CoordinateToNewPointMapping newPointCoordinates)
    {
        super(countries, countryBoundaryMap, newPointCoordinates);
        this.partiallySlicedRawAtlas = atlas;
        this.slicedPointAndLineChanges = simpleChangeSet;
        this.slicedRelationChanges = relationChangeSet;
    }

    /**
     * Country-slice the Relations for the given Atlas.
     *
     * @return a country-sliced {@link Atlas}
     */
    @Override
    public Atlas slice()
    {
        logger.info("Starting Relation slicing for Raw Atlas {}",
                this.partiallySlicedRawAtlas.getName());

        // Slice all relations
        sliceRelations();

        // Apply changes from relation slicing and rebuild the fully-sliced atlas
        final ChangeSetHandler relationChangeBuilder = new RelationChangeSetHandler(
                this.partiallySlicedRawAtlas, this.slicedRelationChanges);

        final Atlas fullySlicedAtlas = relationChangeBuilder.applyChanges();
        logger.info("Finished Relation slicing for Raw Atlas {}",
                this.partiallySlicedRawAtlas.getName());

        getStatistics().summary();
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
        final List<PolyLine> linePieces = new ArrayList<>(members.size());

        for (final RelationMember member : members)
        {
            // We can assume all members are lines since we've already filtered out non-line members
            final Line line = this.partiallySlicedRawAtlas.line(member.getEntity().getIdentifier());
            if (line == null)
            {
                throw new CoreException("Relation {} has a member {} that's not in the raw atlas",
                        relationIdentifier, member.getEntity().getIdentifier());
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
            // Could not form closed rings for some of the members. Keep them in the Atlas, but note
            // the issue.
            logger.error(
                    "One of the members for relation {} is invalid and does not form a closed ring!",
                    relationIdentifier, e);
        }

        return results;
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

            if (getCoordinateToPointMapping().containsCoordinate(pointCoordinate))
            {
                // A new point was already created for this coordinate. Look it up
                // and use it for the line we're creating
                newLineShapePoints
                        .add(getCoordinateToPointMapping().getPointForCoordinate(pointCoordinate));
            }
            else
            {
                // The point is in the original Raw Atlas or need to create a new one
                final Location pointLocation = JTS_LOCATION_CONVERTER
                        .backwardConvert(pointCoordinate);
                final Iterable<Point> rawAtlasPointsAtCoordinate = this.partiallySlicedRawAtlas
                        .pointsAt(pointLocation);
                if (Iterables.isEmpty(rawAtlasPointsAtCoordinate))
                {
                    // Point doesn't exist in the raw Atlas, create a new one
                    final Map<String, String> newPointTags = createPointTags(pointLocation, false);

                    final TemporaryPoint newPoint = createNewPoint(pointCoordinate,
                            pointIdentifierGenerator, newPointTags);

                    // Store coordinate to avoid creating duplicate Points
                    getCoordinateToPointMapping().storeMapping(pointCoordinate,
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
     * Creates a mapping of all inner rings enclosed by the outer rings.
     *
     * @param outerToInnerMap
     *            The mapping to update any time we find an outer ring that contains an inner ring
     * @param outerRings
     *            The list of outer {@link LinearRing}s to use
     * @param innerRings
     *            The list of inner {@link LinearRing}s to use
     */
    private void findEnclosedRings(final MultiMap<Integer, Integer> outerToInnerMap,
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
                final Line line = this.partiallySlicedRawAtlas
                        .line(member.getEntity().getIdentifier());
                if (line == null)
                {
                    logger.error("Line member {} for Relation {} is not the in raw Atlas.",
                            member.getEntity().getIdentifier(), relationIdentifier);
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
                    final ItemType memberType = member.getType();
                    switch (memberType)
                    {
                        case POINT:
                            entity = this.partiallySlicedRawAtlas.point(member.getIdentifier());
                            break;
                        case LINE:
                            entity = this.partiallySlicedRawAtlas.line(member.getIdentifier());
                            break;
                        case RELATION:
                            entity = this.partiallySlicedRawAtlas.relation(member.getIdentifier());
                            break;
                        default:
                            throw new CoreException(
                                    "Unsupported Relation Member of Type {} in Raw Atlas",
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
                .anyMatch(partOf -> partOf.getIdentifier() != relationIdentifier);

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
        final long relationIdentifier = relation.getIdentifier();

        if (outers == null || outers.isEmpty())
        {
            return;
        }

        if (inners == null || inners.isEmpty())
        {
            return;
        }

        // We only want to merge members for modified relations. Unless we touched the relation
        // directly, we will leave the "mergeable" relation as is since it's a more true
        // representation of OSM data.
        if (!containsSlicedMember(new MultiIterable<>(outers, inners)))
        {
            return;
        }

        final List<RelationMember> closedOuters = generateMemberList(relationIdentifier, outers,
                true);
        final List<RelationMember> closedInners = generateMemberList(relationIdentifier, inners,
                true);
        final List<Line> closedOuterLines = new ArrayList<>();
        closedOuters.forEach(outer -> closedOuterLines
                .add(this.partiallySlicedRawAtlas.line(outer.getEntity().getIdentifier())));

        final List<Line> closedInnerLines = new ArrayList<>();
        closedInners.forEach(inner -> closedInnerLines
                .add(this.partiallySlicedRawAtlas.line(inner.getEntity().getIdentifier())));

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
                            outer, inner, relationIdentifier, e);
                }
            }

            // Make sure the merged piece is valid
            final com.vividsolutions.jts.geom.Polygon merged = (com.vividsolutions.jts.geom.Polygon) mergedMembers;
            final LineString exteriorRing = merged.getExteriorRing();
            if (exteriorRing.isEmpty() || !exteriorRing.isClosed())
            {
                // There is a chance that even a successful merge can result in an invalid Polygon.
                // This can happen when we try to merge invalid members. One example is an outer
                // contained within an inner. If that's the case, we want to abort the merge and
                // leave the members as they are.
                successfulMerge = false;
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

                // Set the proper country code
                CountryBoundaryMap.setGeometryProperty(exteriorRing, ISOCountryTag.KEY,
                        ISOCountryTag.first(outer).get().getIso3CountryCode());

                // Create points, lines and update members
                createNewLineMemberForRelation(exteriorRing, relationIdentifier,
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
                        borderLines = getCountryBoundaryMap().clipBoundary(relationIdentifier,
                                polygon);
                    }
                    catch (final Exception e)
                    {
                        logger.error("Error processing relation {}, message: {}, geometry: {}",
                                relationIdentifier, e.getMessage(), polygon.toString(), e);
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

    // TODO come back and verify we're keeping track of all required statistics

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
        getStatistics().recordProcessedRelation();

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
        this.partiallySlicedRawAtlas.relationsLowerOrderFirst()
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

        final Set<TemporaryRelationMember> removedMembers = Optional
                .ofNullable(this.slicedRelationChanges.getDeletedRelationMembers()
                        .get(relation.getIdentifier()))
                .orElse(new HashSet<>());

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
                        final Relation subRelation = this.partiallySlicedRawAtlas
                                .relation(memberIdentifier);

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
                            getStatistics().recordSlicedRelation();
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
                        throw new CoreException(
                                "Unsupported {} Member for Relation {} for a Raw Atlas", memberType,
                                relation.getIdentifier());
                }
            }
        }

        // Add in any new members as a result of multipolygon fixes
        final Set<TemporaryRelationMember> addedMembers = Optional.ofNullable(
                this.slicedRelationChanges.getAddedRelationMembers().get(relation.getIdentifier()))
                .orElse(new HashSet<>());
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
