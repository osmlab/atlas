package org.openstreetmap.atlas.geography.atlas.pbf.slicing;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.openstreetmap.atlas.geography.atlas.pbf.converters.TagMapToTagCollectionConverter;
import org.openstreetmap.atlas.geography.atlas.pbf.slicing.identifier.AbstractIdentifierFactory;
import org.openstreetmap.atlas.geography.atlas.pbf.slicing.identifier.CountrySlicingIdentifierFactory;
import org.openstreetmap.atlas.geography.atlas.pbf.store.PbfMemoryStore;
import org.openstreetmap.atlas.geography.atlas.pbf.store.TagMap;
import org.openstreetmap.atlas.geography.boundary.CountryBoundaryMap;
import org.openstreetmap.atlas.geography.converters.jts.JtsUtility;
import org.openstreetmap.atlas.tags.ISOCountryTag;
import org.openstreetmap.atlas.tags.RelationTypeTag;
import org.openstreetmap.atlas.tags.SyntheticBoundaryNodeTag;
import org.openstreetmap.atlas.tags.SyntheticNearestNeighborCountryCodeTag;
import org.openstreetmap.atlas.utilities.collections.Maps;
import org.openstreetmap.atlas.utilities.maps.MultiMap;
import org.openstreetmap.osmosis.core.domain.v0_6.CommonEntityData;
import org.openstreetmap.osmosis.core.domain.v0_6.Entity;
import org.openstreetmap.osmosis.core.domain.v0_6.EntityType;
import org.openstreetmap.osmosis.core.domain.v0_6.Node;
import org.openstreetmap.osmosis.core.domain.v0_6.Relation;
import org.openstreetmap.osmosis.core.domain.v0_6.RelationMember;
import org.openstreetmap.osmosis.core.domain.v0_6.Tag;
import org.openstreetmap.osmosis.core.domain.v0_6.Way;
import org.openstreetmap.osmosis.core.domain.v0_6.WayNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.TopologyException;

/**
 * @author Yiqing Jin
 */
@SuppressWarnings("unused")
public class CountrySlicingProcessor
{
    /**
     * Object that represents piece of polygon cut.
     */
    public class PolygonPiece
    {
        private final List<Coordinate> coordinates;
        private final long identifier;

        public PolygonPiece(final Way way)
        {
            this.identifier = way.getId();
            final Coordinate[] coordinates = toGeometry(way).getCoordinates();

            this.coordinates = new ArrayList<>();
            for (final Coordinate coordinate : coordinates)
            {
                this.coordinates.add(coordinate);
            }
        }

        @Override
        public boolean equals(final Object other)
        {
            return other instanceof PolygonPiece
                    && this.identifier == ((PolygonPiece) other).identifier;
        }

        public List<Coordinate> getCoordinates()
        {
            return this.coordinates;
        }

        public Coordinate getEndNode()
        {
            return this.coordinates.get(this.coordinates.size() - 1);
        }

        public long getIdentifier()
        {
            return this.identifier;
        }

        public Coordinate getStartNode()
        {
            return this.coordinates.get(0);
        }

        @Override
        public int hashCode()
        {
            return (int) this.identifier;
        }

        public void merge(final PolygonPiece otherPiece, final boolean isReverseSelf,
                final boolean isReverseOther)
        {
            if (isReverseSelf)
            {
                Collections.reverse(this.coordinates);
            }
            if (isReverseOther)
            {
                Collections.reverse(otherPiece.getCoordinates());
            }
            this.coordinates.addAll(otherPiece.getCoordinates());
        }

        /**
         * @return {@code true} if coordinates represent a closed ring
         */
        private boolean isClosed()
        {
            return this.coordinates.get(0)
                    .equals(this.coordinates.get(this.coordinates.size() - 1));
        }
    }

    private static final Logger logger = LoggerFactory.getLogger(CountrySlicingProcessor.class);
    private static final int JTS_MINIMUM_RING_SIZE = 4;
    private static final TagMapToTagCollectionConverter TAG_MAP_TO_TAG_COLLECTION_CONVERTER = new TagMapToTagCollectionConverter();

    private final CountryBoundaryMap boundaryMap;
    private final Set<String> countryCodeISO3;
    private final PbfMemoryStore store;
    private final ChangeSet changeSet;

    private static void roundCoordinate(final Coordinate coordinate)
    {
        // Degree of magnitude 7 precision
        final double factor = 10000000;
        coordinate.x = Math.round(coordinate.x * factor) / factor;
        coordinate.y = Math.round(coordinate.y * factor) / factor;
    }

    private static void tagCountry(final Entity entity, final String countryCode,
            final String usingNearestNeighbor)
    {
        if (Objects.isNull(entity))
        {
            return;
        }

        final Collection<Tag> tags = new ArrayList<>();
        boolean hasCountryCode = false;

        for (final Tag tag : entity.getTags())
        {
            if (tag.getKey().equals(ISOCountryTag.KEY))
            {
                hasCountryCode = true;
                if (!tag.getValue().contains(countryCode))
                {
                    tags.add(new Tag(ISOCountryTag.KEY,
                            String.join(",", tag.getValue(), countryCode)));
                }
                else
                {
                    // It has a country tag with given country code already.
                    return;
                }
                // Node has already been tagged with country code, we are done.
            }
            else
            {
                tags.add(tag);
            }
        }

        if (hasCountryCode)
        {
            if (usingNearestNeighbor != null)
            {
                tags.add(new Tag(SyntheticNearestNeighborCountryCodeTag.KEY, usingNearestNeighbor));
            }
            entity.getTags().clear();
            entity.getTags().addAll(tags);
        }
        else
        {
            entity.getTags().add(new Tag(ISOCountryTag.KEY, countryCode));
            if (usingNearestNeighbor != null)
            {
                entity.getTags().add(
                        new Tag(SyntheticNearestNeighborCountryCodeTag.KEY, usingNearestNeighbor));
            }
        }
    }

    /**
     * @param store
     *            The {@link PbfMemoryStore} to use.
     * @param countryBoundaryMap
     *            The {@link CountryBoundaryMap} to use.
     */
    public CountrySlicingProcessor(final PbfMemoryStore store,
            final CountryBoundaryMap countryBoundaryMap)
    {
        this(store, countryBoundaryMap, null);
    }

    /**
     * @param store
     *            The {@link PbfMemoryStore} to use.
     * @param countryBoundaryMap
     *            The {@link CountryBoundaryMap} to use.
     * @param countryCodeISO3
     *            Restrict processing to this set of countries.
     */
    public CountrySlicingProcessor(final PbfMemoryStore store,
            final CountryBoundaryMap countryBoundaryMap, final Set<String> countryCodeISO3)
    {
        this.store = store;
        this.countryCodeISO3 = countryCodeISO3;
        this.boundaryMap = countryBoundaryMap;
        this.changeSet = new ChangeSet();
    }

    public void run()
    {
        logger.info("Starting country slicing process....");

        // Slice all ways first, then update relations
        processWays();

        // Add new ways to store so they could be used by relation slicing.
        this.store.apply(this.changeSet, false);
        processRelations();

        this.store.apply(this.changeSet);
        logger.info(RuntimeCounter.print());
    }

    /**
     * Build rings for different {@link Relation} types.
     *
     * @param relation
     *            The {@link Relation} to use.
     * @param members
     *            The list of {@link RelationMember}s for this relation.
     * @return the produced {@link LinearRing}s
     */
    private List<LinearRing> buildRings(final Relation relation, final List<RelationMember> members)
    {
        final List<LinearRing> results = new ArrayList<>();
        final List<PolygonPiece> pieces = new ArrayList<>(members.size());
        final Deque<PolygonPiece> stack = new ArrayDeque<>(members.size());

        for (final RelationMember member : members)
        {
            final Way way = this.store.getWay(member.getMemberId());
            if (way == null)
            {
                logger.warn("Relation {} has an incomplete member {}", relation.getId(),
                        member.getMemberId());
                return null;
            }

            final PolygonPiece piece = new PolygonPiece(way);
            pieces.add(piece);
            stack.push(piece);
        }

        while (!stack.isEmpty())
        {
            final PolygonPiece piece1 = stack.pop();
            while (!piece1.isClosed())
            {
                boolean foundConnection = false;
                for (final PolygonPiece piece2 : stack)
                {
                    if (piece1.getEndNode().equals(piece2.getStartNode()))
                    {
                        foundConnection = true;
                        piece1.merge(piece2, false, false);
                    }
                    else if (piece1.getEndNode().equals(piece2.getEndNode()))
                    {
                        foundConnection = true;
                        piece1.merge(piece2, false, true);
                    }
                    else if (piece1.getStartNode().equals(piece2.getStartNode()))
                    {
                        foundConnection = true;
                        piece1.merge(piece2, true, false);
                    }
                    else if (piece1.getStartNode().equals(piece2.getEndNode()))
                    {
                        foundConnection = true;
                        piece1.merge(piece2, true, true);
                    }
                    if (foundConnection)
                    {
                        stack.remove(piece2);
                        break;
                    }
                }

                if (!foundConnection)
                {
                    logger.warn("Relation {} claims to be multipolygon, but has a detached way {}",
                            relation.getId(), piece1.getIdentifier());
                    return null;
                }
            }

            if (piece1.getCoordinates().size() < JTS_MINIMUM_RING_SIZE)
            {
                logger.warn(
                        "Relation {} claims to be multipolygon, but doesn't have a valid closed way",
                        relation.getId(), piece1.getIdentifier());
                return null;
            }

            // If we reach here, we have a closed way and can build a valid LineRing
            results.add(JtsUtility.buildLinearRing(piece1.getCoordinates()));
        }

        return results;
    }

    private void generatePatchedWays(final Relation relation, final List<RelationMember> outers,
            final List<RelationMember> inners, final Map<Coordinate, Node> cachedNodes)
    {
        if (outers == null || outers.isEmpty())
        {
            return;
        }

        // Check if all outer ways are closed. If not, we need to try build LineRings and fix the
        // open gaps.
        final List<RelationMember> outerInCompleteList = outers.stream().filter(relationMember ->
        {
            final Way way = this.store.getWay(relationMember.getMemberId());
            return way == null || !way.isClosed();
        }).collect(Collectors.toList());

        if (outerInCompleteList.isEmpty())
        {
            return;
        }

        final List<LinearRing> outerRings = buildRings(relation, outerInCompleteList);
        if (outerRings == null || outerRings.isEmpty())
        {
            return;
        }

        List<LinearRing> innerRings = null;
        final MultiMap<Integer, Integer> outToInMap = new MultiMap<>();
        List<RelationMember> innerIncompleteList = null;

        if (inners != null && !inners.isEmpty())
        {
            innerIncompleteList = inners.stream().filter(relationMember ->
            {
                final Way way = this.store.getWay(relationMember.getMemberId());
                return way == null || !way.isClosed();
            }).collect(Collectors.toList());

            innerRings = buildRings(relation, innerIncompleteList);

            if (innerRings == null)
            {
                // This multipolygon is invalid
                return;
            }

            // Try to find the outer ring that contains the inner ring
            for (int innerIndex = 0; innerIndex < innerRings.size(); innerIndex++)
            {
                final LinearRing inner = innerRings.get(innerIndex);
                boolean isMatched = false;
                for (int outerIndex = 0; outerIndex < outerRings.size(); outerIndex++)
                {
                    final LinearRing outer = outerRings.get(outerIndex);
                    final Polygon polygon = new Polygon(outer, null, JtsUtility.GEOMETRY_FACTORY);
                    if (polygon.contains(inner))
                    {
                        isMatched = true;
                        outToInMap.add(outerIndex, innerIndex);
                    }
                }

                if (!isMatched)
                {
                    // Isolated inner ring - invalid multipolygon
                    return;
                }
            }
        }

        // Create seeds for new identifiers
        final long[] seeds = new long[relation.getMembers().size()];
        for (int seedIndex = 0; seedIndex < seeds.length; seedIndex++)
        {
            long seed = relation.getMembers().get(seedIndex).getMemberId();
            if (this.changeSet.hasWay(seed))
            {
                seed = seed + this.changeSet.getWayMap().get(seed).size()
                        * CountrySlicingIdentifierFactory.IDENTIFIER_SCALE;
            }
            seeds[seedIndex] = seed;
        }

        final CountrySlicingIdentifierFactory wayIdentifierGenerator = new CountrySlicingIdentifierFactory(
                seeds[0]);
        final CountrySlicingIdentifierFactory nodeIdentifierGenerator = new CountrySlicingIdentifierFactory(
                seeds);

        for (int outerIndex = 0; outerIndex < outerRings.size(); outerIndex++)
        {
            final LinearRing outerRing = outerRings.get(outerIndex);
            LinearRing[] holes = null;
            if (outToInMap.containsKey(outerIndex))
            {
                final List<Integer> innerIndexes = outToInMap.get(outerIndex);
                holes = new LinearRing[innerIndexes.size()];
                for (int innerIndex = 0; innerIndex < innerIndexes.size(); innerIndex++)
                {
                    holes[innerIndex] = innerRings.get(innerIndexes.get(innerIndex));
                }
            }

            final Polygon polygon = new Polygon(outerRing, holes, JtsUtility.GEOMETRY_FACTORY);

            // Check if the polygon is valid. Sometimes polygons from relations are invalid,
            // especially for large boundaries.
            if (!polygon.isValid())
            {
                logger.warn("Polygon created by relation {} is invalid", relation.getId());
                return;
            }

            final List<LineString> borderLines;
            try
            {
                borderLines = this.boundaryMap.clipBoundary(relation.getId(), polygon);
            }
            catch (final Exception e)
            {
                logger.error("Error processing relation {}, message: {}, geometry: {}",
                        relation.getId(), e.getMessage(), polygon.toString());
                return;
            }

            if (borderLines == null || borderLines.size() == 0)
            {
                // There was no cut
                return;
            }

            if (borderLines.size() >= AbstractIdentifierFactory.IDENTIFIER_SCALE)
            {
                logger.warn("Borderline got cut into more than 999 pieces for relation {}",
                        relation.getId());
                return;
            }

            if (innerIncompleteList != null && borderLines.size() > 0)
            {
                // Since we cut the inner lines open, update inner lines to be outers.
                innerIncompleteList.forEach(relationMember ->
                {
                    relation.getMembers().remove(relationMember);
                    relation.getMembers()
                            .add(new RelationMember(relationMember.getMemberId(),
                                    relationMember.getMemberType(),
                                    RelationTypeTag.MULTIPOLYGON_ROLE_OUTER));
                });
            }
            for (final LineString borderLine : borderLines)
            {
                final List<WayNode> wayNodes = new ArrayList<>(borderLine.getNumPoints());
                for (int borderLineIndex = 0; borderLineIndex < borderLine
                        .getNumPoints(); borderLineIndex++)
                {
                    final Coordinate nodeCoordinate = borderLine.getCoordinateN(borderLineIndex);
                    roundCoordinate(nodeCoordinate);
                    Node node = cachedNodes.get(nodeCoordinate);

                    if (node == null)
                    {
                        // Node doesn't exist yet, try to create a new one
                        final long newNodeIdentifier;
                        try
                        {
                            newNodeIdentifier = nodeIdentifierGenerator.nextIdentifier();
                        }
                        catch (final Exception e)
                        {
                            logger.warn(
                                    "New way generated for relation {} is too big, ran out of node identfiers to use.",
                                    relation.getId(), e);
                            return;
                        }

                        node = this.store.createNode(newNodeIdentifier, nodeCoordinate.y,
                                nodeCoordinate.x);
                        this.store.addNode(node);
                        cachedNodes.put(nodeCoordinate, node);
                    }

                    wayNodes.add(new WayNode(node.getId()));
                }

                // Create the patched way
                final Way way = new Way(
                        new CommonEntityData(wayIdentifierGenerator.nextIdentifier(),
                                relation.getVersion(), relation.getTimestampContainer(),
                                relation.getUser(), relation.getChangesetId()),
                        wayNodes);

                // Set the country code
                final String countryCodeValue = CountryBoundaryMap.getGeometryProperty(borderLine,
                        ISOCountryTag.KEY);
                way.getTags().add(new Tag(ISOCountryTag.KEY, countryCodeValue));

                // Set the nearest neighbor flag
                final String usingNearestNeighbor = CountryBoundaryMap.getGeometryProperty(
                        borderLine, SyntheticNearestNeighborCountryCodeTag.KEY);
                if (usingNearestNeighbor != null)
                {
                    way.getTags().add(new Tag(SyntheticNearestNeighborCountryCodeTag.KEY,
                            usingNearestNeighbor));
                }

                this.changeSet.addCreatedWay(way);
                relation.getMembers().add(new RelationMember(way.getId(), way.getType(),
                        RelationTypeTag.MULTIPOLYGON_ROLE_OUTER));
                this.changeSet.addModifiedRelation(relation);
            }
        }

        return;
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
        final String countryCode = CountryBoundaryMap.getGeometryProperty(geometry,
                ISOCountryTag.KEY);
        return this.countryCodeISO3 != null && !this.countryCodeISO3.isEmpty()
                && !this.countryCodeISO3.contains(countryCode);
    }

    /**
     * Assign country codes for {@link Relation}s. This is assuming all {@link Way}s crossing
     * borders got cut and new {@link Way}s were generated to fill the polygon. All we need to do
     * here is group them by country and split the members.
     *
     * @param relation
     *            The {@link Relation} to process.
     * @return All {@link Relation}s produced after splitting the given {@link Relation}.
     */
    private Optional<List<Relation>> processDefaultRelation(final Relation relation,
            final List<Long> parents)
    {
        final List<Relation> createdRelations = new ArrayList<>();
        final List<RelationMember> members = new ArrayList<>();
        boolean isModified = false;

        for (final RelationMember member : relation.getMembers())
        {
            switch (member.getMemberType())
            {
                case Way:
                    final List<Way> slicedWays = this.changeSet
                            .getCreatedWays(member.getMemberId());
                    if (slicedWays != null && !slicedWays.isEmpty())
                    {
                        isModified = true;
                        slicedWays.forEach(way -> members
                                .add(this.store.createRelationMember(member, way.getId())));
                    }
                    else
                    {
                        // Either the way is not sliced or way is outside of the supplied bound
                        members.add(member);
                    }
                    break;
                case Relation:
                    parents.add(relation.getId());
                    final Relation subRelation = this.store.getRelation(member.getMemberId());

                    // Check if we have the sub-relation in the store
                    if (subRelation == null)
                    {
                        // Put the member back into the relation. Missing members will be handled
                        // on a case by case basis
                        members.add(member);
                        break;
                    }

                    final Optional<List<Relation>> slicedMembers;
                    if (!parents.contains(subRelation.getId()))
                    {
                        slicedMembers = sliceRelation(subRelation, parents);
                    }
                    else
                    {
                        logger.error("Relation {} has a loop! Parent tree: {}", subRelation.getId(),
                                parents);
                        slicedMembers = Optional.empty();
                    }
                    if (slicedMembers.isPresent())
                    {
                        isModified = true;
                        slicedMembers.get().forEach(slicedRelation ->
                        {
                            members.add(this.store.createRelationMember(member,
                                    slicedRelation.getId()));
                        });
                    }
                    else
                    {
                        members.add(member);
                    }
                    break;
                default:
                    // Here we are not checking the bound, because we are assuming all data
                    // outside of bound has already been filtered out and doesn't exist in the
                    // PbfMemoryStore
                    members.add(member);
                    break;
            }
        }

        if (isModified)
        {
            // Modified by way slicing or relation slicing
            this.changeSet.addModifiedRelation(relation);
        }

        // Group entities by country
        final Map<String, List<RelationMember>> countryEntityMap = members.stream()
                .collect(Collectors.groupingBy(member ->
                {
                    final Entity entity = this.store.getEntity(member);
                    if (Objects.isNull(entity))
                    {
                        return ISOCountryTag.COUNTRY_MISSING;
                    }
                    else
                    {
                        final Optional<Tag> countryCodeTag = entity.getTags().stream()
                                .filter(tag -> tag.getKey().equals(ISOCountryTag.KEY)).findFirst();
                        if (countryCodeTag.isPresent())
                        {
                            return countryCodeTag.get().getValue();
                        }
                        else
                        {
                            return ISOCountryTag.COUNTRY_MISSING;
                        }
                    }
                }));

        final List<RelationMember> memberWithoutCountry;
        if (countryEntityMap.containsKey(ISOCountryTag.COUNTRY_MISSING))
        {
            memberWithoutCountry = countryEntityMap.remove(ISOCountryTag.COUNTRY_MISSING);
        }
        else
        {
            memberWithoutCountry = Collections.emptyList();
        }

        final int countryCount = countryEntityMap.size();

        if (countryCount == 0)
        {
            relation.getTags().add(new Tag(ISOCountryTag.KEY, ISOCountryTag.COUNTRY_MISSING));
            return Optional.empty();
        }
        else if (countryCount == 1)
        {
            // One country code found, assign it
            relation.getTags()
                    .add(new Tag(ISOCountryTag.KEY, countryEntityMap.keySet().iterator().next()));
            return Optional.empty();
        }
        else
        {
            // Muliple country codes found, implies relation crosses countries. As a 2 dimensional
            // feature, relation sliced should not have more than one piece for each country. For
            // now, for features without a country code (nodes and feature not covered by any
            // boundary), we put a copy for every sliced piece to ensure integrity.
            RuntimeCounter.relationSliced();
            this.changeSet.addDeletedRelation(relation);
            final CountrySlicingIdentifierFactory relationIdFactory = new CountrySlicingIdentifierFactory(
                    relation.getId());

            countryEntityMap.entrySet().forEach(entry ->
            {
                final List<RelationMember> candidateMembers = new ArrayList<>();
                candidateMembers.addAll(entry.getValue());
                candidateMembers.addAll(memberWithoutCountry);

                if (!candidateMembers.isEmpty())
                {
                    final Relation relationToAdd = this.store.createRelation(relation,
                            relationIdFactory.nextIdentifier(), candidateMembers);
                    // TODO Consider sharing CountryCode Tag objects
                    relationToAdd.getTags().add(new Tag(ISOCountryTag.KEY, entry.getKey()));
                    createdRelations.add(relationToAdd);
                    this.changeSet.addCreatedRelation(relationToAdd);
                }
            });
        }
        return Optional.of(createdRelations);
    }

    /**
     * @param relation
     *            The {@link Relation} to process.
     */
    private void processMultiPolygonRelation(final Relation relation)
    {
        // Build MultiPolygon from relation
        final Map<String, List<RelationMember>> roleMap = relation.getMembers().stream()
                .filter(member -> member.getMemberType() == EntityType.Way)
                .collect(Collectors.groupingBy(RelationMember::getMemberRole));
        final List<RelationMember> outer = roleMap.get(RelationTypeTag.MULTIPOLYGON_ROLE_OUTER);
        final List<RelationMember> inner = roleMap.get(RelationTypeTag.MULTIPOLYGON_ROLE_INNER);
        final Map<Coordinate, Node> cachedNodes = new HashMap<>();

        // Generate new way to patch the wound, if necessary
        generatePatchedWays(relation, outer, inner, cachedNodes);
    }

    private void processRelations()
    {
        this.store.getRelations().values().stream().forEach(this::sliceRelation);
    }

    private void processWays()
    {
        this.store.getWays().values().stream().forEach(this::sliceWay);
    }

    /**
     * @param relation
     *            The {@link Relation} to slice.
     * @return an {@link Optional} containing sliced {@link Relation} objects, if sliced.
     */
    private Optional<List<Relation>> sliceRelation(final Relation relation)
    {
        return sliceRelation(relation, new ArrayList<>());
    }

    /**
     * @param relation
     *            The {@link Relation} to slice.
     * @param parents
     *            The parent tree of this relation, to detect looping relations
     * @return an {@link Optional} containing sliced {@link Relation} objects, if sliced.
     */
    private Optional<List<Relation>> sliceRelation(final Relation relation,
            final List<Long> parents)
    {
        RuntimeCounter.relationProcessed();

        // Build a tag map - in case of duplicated keys, only pick first one.
        final Map<String, String> tagMap = new TagMap(relation.getTags()).getTags();
        final String typeValue = tagMap.get(RelationTypeTag.KEY);
        final RelationType type = RelationType.forValue(typeValue);

        // Special cases
        switch (type)
        {
            case MULTIPOLYGON:
            case BOUNDARY:
                processMultiPolygonRelation(relation);
                break;
            default:
                break;
        }

        // Common cases
        return processDefaultRelation(relation, parents);
    }

    /**
     * Slice a {@link Way} with current country boundary.
     *
     * @param way
     *            The {@link Way} to be sliced
     * @return an {@link Optional} list of {@link Way}s created by the slicing. The {@link Optional}
     *         could still contain an empty list because of the shard bound.
     */
    private Optional<List<Way>> sliceWay(final Way way)
    {
        RuntimeCounter.wayProcessed();

        if (Objects.isNull(way))
        {
            return Optional.empty();
        }

        final List<WayNode> wayNodes = way.getWayNodes();

        // First, check for invalid ways
        if (wayNodes.size() < 2 || wayNodes.size() == 2
                && wayNodes.get(0).getNodeId() == wayNodes.get(1).getNodeId())
        {
            return Optional.empty();
        }

        final List<Coordinate> coordinates = new ArrayList<>();

        // A lookup table to find existing nodes. Right now, it only builds for each individual way,
        // may consider to use global cache if we want force no overlapping nodes
        final Map<Coordinate, Node> coordinateToNode = new HashMap<>(wayNodes.size() + 2);
        final Map<Long, Node> identifierToNode = new HashMap<>(wayNodes.size() + 2);

        wayNodes.forEach(wayNode ->
        {
            final Node node = this.store.getNode(wayNode.getNodeId());
            if (node == null)
            {
                logger.error("Node {} in Way {} is not in the store.", wayNode.getNodeId(),
                        way.getId());
            }
            final Coordinate coordinate = new Coordinate(node.getLongitude(), node.getLatitude());

            // Sometimes JTS has trouble dealing with high precision double values.
            roundCoordinate(coordinate);
            coordinates.add(coordinate);
            coordinateToNode.put(coordinate, node);
            identifierToNode.put(node.getId(), node);
        });

        final Geometry geometry;
        if (!way.isClosed())
        {
            // Not an area
            geometry = JtsUtility.buildLineString(coordinates.toArray(new Coordinate[0]));
        }
        else
        {
            // An area
            geometry = JtsUtility.toPolygon(coordinates);
        }

        final List<Geometry> slices;
        try
        {
            slices = this.boundaryMap.slice(way.getId(), geometry);
        }
        catch (final TopologyException e)
        {
            logger.warn("Way slicing for {} threw topology exception.", way.getId(), e);
            return Optional.empty();
        }

        if (slices == null)
        {
            way.getTags().add(new Tag(ISOCountryTag.KEY, ISOCountryTag.COUNTRY_MISSING));
            return Optional.empty();
        }
        else if (slices.size() == 1 || CountryBoundaryMap.isSameCountry(slices))
        {
            // If a geometry goes slightly over the border, the tiny slice could be dropped and
            // number of slices is still 1. We should create a new geometry for this case, but
            // sometimes the adjusted geometry requires a lot of nodes to be created. If later we
            // decide not to keep osm object for processing then we should change this code to use
            // proper geometry.
            final String countryCodeValue = CountryBoundaryMap.getGeometryProperty(slices.get(0),
                    ISOCountryTag.KEY);
            way.getTags().add(new Tag(ISOCountryTag.KEY, countryCodeValue));

            // If we're using the nearest neighbor to assign the country code, make sure to
            // propagate this tag.
            final String usingNearestNeighbor = CountryBoundaryMap
                    .getGeometryProperty(slices.get(0), SyntheticNearestNeighborCountryCodeTag.KEY);
            if (usingNearestNeighbor != null)
            {
                way.getTags().add(
                        new Tag(SyntheticNearestNeighborCountryCodeTag.KEY, usingNearestNeighbor));
            }

            return Optional.empty();
        }
        else if (slices.size() < AbstractIdentifierFactory.IDENTIFIER_SCALE)
        {
            RuntimeCounter.waySliced();

            final CountrySlicingIdentifierFactory wayIdentifierFactory = new CountrySlicingIdentifierFactory(
                    way.getId());
            final CountrySlicingIdentifierFactory nodeIdentifierFactory = new CountrySlicingIdentifierFactory(
                    way.getId());
            final List<Way> createdWays = new ArrayList<>();

            for (final Geometry slice : slices)
            {
                // First, check if slice is within the working bound
                if (isOutsideWorkingBound(slice))
                {
                    continue;
                }

                final List<WayNode> newWayNodes = new ArrayList<>(slice.getNumPoints());
                final Coordinate[] points = slice.getCoordinates();

                for (final Coordinate coordinate : points)
                {
                    // Because country shapes do not share border, we are rounding coordinate first
                    // to consider very close nodes as one.
                    roundCoordinate(coordinate);

                    Node node = coordinateToNode.get(coordinate);

                    if (node != null)
                    {
                        // This is a case where the existing node might be exactly at the border
                        // where the cut is made. It should have the tag, because it would then
                        // qualify as a boundary node. However finding it might be more complicated
                        // that expected. It is not a missing node (easy, below) and it has to be
                        // right on the boundary. But it should not be mistaken for all the other
                        // nodes that are already existing and that are not on the boundary.
                        if (/* TODO Find a way to isolate those */false)
                        {
                            final Map<String, String> boundaryNodeTags = Maps.hashMap(
                                    SyntheticBoundaryNodeTag.KEY,
                                    SyntheticBoundaryNodeTag.EXISTING.name().toLowerCase());
                            final Collection<Tag> tags = new ArrayList<>();
                            tags.addAll(node.getTags());

                            // Still add the boundary node tags
                            tags.addAll(
                                    TAG_MAP_TO_TAG_COLLECTION_CONVERTER.convert(boundaryNodeTags));
                            node.getTags().clear();
                            node.getTags().addAll(tags);
                        }

                        // The node exists, reuse it
                        newWayNodes.add(new WayNode(node.getId()));
                    }
                    else
                    {
                        // Node doesn't exist, try to create a new node
                        if (!nodeIdentifierFactory.hasMore())
                        {
                            logger.error(
                                    "Country slicing exceeded max number({}) of supported new nodes for way {}",
                                    AbstractIdentifierFactory.IDENTIFIER_SCALE, way.getId());
                            return Optional.empty();
                        }

                        final Map<String, String> boundaryNodeTags = Maps.hashMap(
                                SyntheticBoundaryNodeTag.KEY,
                                SyntheticBoundaryNodeTag.YES.name().toLowerCase());
                        node = this.store.createNode(nodeIdentifierFactory.nextIdentifier(),
                                coordinate.y, coordinate.x, boundaryNodeTags);
                        this.changeSet.addCreatedNode(node);

                        // Store the coordinate and identifier to avoid duplicated node
                        coordinateToNode.put(coordinate, node);
                        identifierToNode.put(node.getId(), node);
                        newWayNodes.add(new WayNode(node.getId()));
                    }
                }

                final String countryCode = CountryBoundaryMap.getGeometryProperty(slice,
                        ISOCountryTag.KEY);
                final String usingNearestNeighbor = CountryBoundaryMap.getGeometryProperty(slice,
                        SyntheticNearestNeighborCountryCodeTag.KEY);

                // Create a new way
                final Way createdWay = this.store.createWay(way,
                        wayIdentifierFactory.nextIdentifier(), newWayNodes);

                // Set country code
                createdWay.getTags().add(new Tag(ISOCountryTag.KEY, countryCode));

                // Update nearest neighbor value
                if (usingNearestNeighbor != null)
                {
                    createdWay.getTags().add(new Tag(SyntheticNearestNeighborCountryCodeTag.KEY,
                            usingNearestNeighbor));
                }

                // Store the way
                createdWays.add(createdWay);

                // Doing a hack here to assign country code to edge nodes since it's hard to handle
                // nodes right on the border by only using geometry check.
                if (this.store.isAtlasEdge(createdWay))
                {
                    final Node nodeStart = identifierToNode.get(newWayNodes.get(0).getNodeId());
                    final Node nodeEnd = identifierToNode
                            .get(newWayNodes.get(newWayNodes.size() - 1).getNodeId());
                    tagCountry(nodeStart, countryCode, usingNearestNeighbor);
                    tagCountry(nodeEnd, countryCode, usingNearestNeighbor);
                }
            }

            // Store the created ways
            for (final Way createdWay : createdWays)
            {
                this.changeSet.addCreatedWay(createdWay, way.getId());
            }

            this.changeSet.addDeletedWay(way);
            return Optional.of(createdWays);
        }
        else
        {
            logger.error(
                    "Country slicing exceeded maximum number of supported pieces {} for way {}",
                    AbstractIdentifierFactory.IDENTIFIER_SCALE, way.getId());
            return Optional.empty();
        }
    }

    private Geometry toGeometry(final Way way)
    {
        final List<WayNode> wayNodes = way.getWayNodes();

        if (wayNodes.size() < 2)
        {
            // Invalid way
            return null;
        }

        final List<Coordinate> coordinates = new ArrayList<>();

        // A lookup table to find existing nodes. Right now, it only builds for each individual way,
        // may consider to use global cache if we want force no overlapping node
        final Map<Coordinate, Node> coordinateToNode = new HashMap<>(wayNodes.size() + 2);

        wayNodes.forEach(wayNode ->
        {
            final Node node = this.store.getNode(wayNode.getNodeId());
            final Coordinate coordinate = new Coordinate(node.getLongitude(), node.getLatitude());
            coordinates.add(coordinate);
            coordinateToNode.put(coordinate, node);
        });

        final Geometry geometry;
        if (!way.isClosed())
        {
            // Not an area
            geometry = JtsUtility.buildLineString(coordinates.toArray(new Coordinate[0]));
        }
        else
        {
            // An area
            geometry = JtsUtility.toPolygon(coordinates);
        }

        return geometry;
    }
}
