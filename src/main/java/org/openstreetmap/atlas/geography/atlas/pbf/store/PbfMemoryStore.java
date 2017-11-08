package org.openstreetmap.atlas.geography.atlas.pbf.store;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.Latitude;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.Longitude;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.Polygon;
import org.openstreetmap.atlas.geography.atlas.builder.AtlasBuilder;
import org.openstreetmap.atlas.geography.atlas.pbf.AtlasLoadingOption;
import org.openstreetmap.atlas.geography.atlas.pbf.converters.TagMapToTagCollectionConverter;
import org.openstreetmap.atlas.geography.atlas.pbf.slicing.ChangeSet;
import org.openstreetmap.atlas.geography.atlas.raw.slicing.CountryCodeProperties;
import org.openstreetmap.atlas.geography.boundary.CountryBoundaryMap;
import org.openstreetmap.atlas.geography.boundary.converters.CountryListTwoWayStringConverter;
import org.openstreetmap.atlas.geography.converters.jts.JtsPointConverter;
import org.openstreetmap.atlas.tags.AtlasTag;
import org.openstreetmap.atlas.tags.ISOCountryTag;
import org.openstreetmap.atlas.tags.SyntheticNearestNeighborCountryCodeTag;
import org.openstreetmap.atlas.tags.Taggable;
import org.openstreetmap.atlas.utilities.collections.Maps;
import org.openstreetmap.atlas.utilities.collections.StringList;
import org.openstreetmap.atlas.utilities.scalars.Counter;
import org.openstreetmap.osmosis.core.container.v0_6.EntityContainer;
import org.openstreetmap.osmosis.core.container.v0_6.NodeContainer;
import org.openstreetmap.osmosis.core.container.v0_6.RelationContainer;
import org.openstreetmap.osmosis.core.container.v0_6.WayContainer;
import org.openstreetmap.osmosis.core.domain.v0_6.CommonEntityData;
import org.openstreetmap.osmosis.core.domain.v0_6.Entity;
import org.openstreetmap.osmosis.core.domain.v0_6.EntityType;
import org.openstreetmap.osmosis.core.domain.v0_6.Node;
import org.openstreetmap.osmosis.core.domain.v0_6.OsmUser;
import org.openstreetmap.osmosis.core.domain.v0_6.Relation;
import org.openstreetmap.osmosis.core.domain.v0_6.RelationMember;
import org.openstreetmap.osmosis.core.domain.v0_6.Tag;
import org.openstreetmap.osmosis.core.domain.v0_6.Way;
import org.openstreetmap.osmosis.core.domain.v0_6.WayNode;
import org.openstreetmap.osmosis.core.task.v0_6.Sink;
import org.openstreetmap.osmosis.core.task.v0_6.SinkRunnableSource;
import org.openstreetmap.osmosis.xml.common.CompressionMethod;
import org.openstreetmap.osmosis.xml.v0_6.XmlWriter;

/**
 * This {@link PbfMemoryStore} holds all the information needed by country slicing, way sectioning
 * and {@link AtlasBuilder}
 *
 * @author tony
 */
public class PbfMemoryStore implements SinkRunnableSource
{
    // All the converters
    private static final JtsPointConverter JTS_POINT_CONVERTER = new JtsPointConverter();
    private static final TagMapToTagCollectionConverter TAG_MAP_TO_TAG_COLLECTION_CONVERTER = new TagMapToTagCollectionConverter();
    private static final CountryListTwoWayStringConverter COUNTRY_LIST_CONVERTER = new CountryListTwoWayStringConverter();

    private final Map<Long, Node> nodes;
    private final Map<Long, Way> ways;
    private final Map<Long, Relation> relations;
    private final Set<Long> nodesAtEndOfEdges;
    private final Set<Long> nodesInRelations;
    private final CountryBoundaryMap countryBoundaryMap;
    private final Set<String> defaultCountries;
    private final AtlasLoadingOption atlasLoadingOption;

    private Sink sink;

    public PbfMemoryStore(final AtlasLoadingOption atlasLoadingOption)
    {
        this.nodes = new HashMap<>();
        this.ways = new HashMap<>();
        this.relations = new HashMap<>();
        this.nodesAtEndOfEdges = new HashSet<>();
        this.nodesInRelations = new HashSet<>();
        this.countryBoundaryMap = atlasLoadingOption.getCountryBoundaryMap();
        this.defaultCountries = atlasLoadingOption.getCountryCodes();
        this.atlasLoadingOption = atlasLoadingOption;
    }

    public void addNode(final Node node)
    {
        this.nodes.put(node.getId(), node);
    }

    public void addNodeAtEndOfEdge(final long identifier)
    {
        this.nodesAtEndOfEdges.add(identifier);
    }

    public void addNodeInRelations(final long identifier)
    {
        this.nodesInRelations.add(identifier);
    }

    public void addRelation(final Relation relation)
    {
        this.relations.put(relation.getId(), relation);
    }

    public void addWay(final Way way)
    {
        if (isAtlasEdge(way))
        {
            final List<WayNode> wayNodes = way.getWayNodes();
            this.nodesAtEndOfEdges.add(wayNodes.get(0).getNodeId());
            this.nodesAtEndOfEdges.add(wayNodes.get(wayNodes.size() - 1).getNodeId());
        }
        this.ways.put(way.getId(), way);
    }

    public void apply(final ChangeSet changeSet)
    {
        apply(changeSet, true);
    }

    /**
     * @param changeSet
     *            the change set to apply
     * @param delete
     *            if also remove records marked as deleted.
     */
    public void apply(final ChangeSet changeSet, final boolean delete)
    {
        changeSet.getCreatedNodes().forEach(this::addNode);
        changeSet.getCreatedWays().forEach(this::addWay);
        changeSet.getCreatedRelations().forEach(this::addRelation);
        if (delete)
        {
            changeSet.getDeletedWays().forEach(way -> this.removeWay(way.getId()));
            changeSet.getDeletedRelations()
                    .forEach(relation -> this.removeRelation(relation.getId()));
        }
    }

    public long atlasAreaCount()
    {
        return atlasAreas().count();
    }

    public Stream<Way> atlasAreas()
    {
        return this.ways.values().stream().filter(this::isAtlasArea);
    }

    public long atlasEdgeCount()
    {
        final Counter counter = new Counter();
        atlasEdges().forEach(way ->
        {
            if (PbfOneWay.NO == new TagMap(way.getTags()).getOneWay())
            {
                counter.add(2);
            }
            else
            {
                counter.increment();
            }
        });
        return counter.getValue();
    }

    public Stream<Way> atlasEdges()
    {
        return this.ways.values().stream().filter(this::isAtlasEdge);
    }

    public long atlasLineCount()
    {
        return atlasLines().count();
    }

    public Stream<Way> atlasLines()
    {
        return this.ways.values().stream().filter(this::isAtlasLine);
    }

    public Set<Way> atlasLinesAndAreas()
    {
        return this.ways.values().stream().filter(way -> !isAtlasEdge(way))
                .collect(Collectors.toSet());
    }

    public long atlasNodeCount()
    {
        return this.nodesAtEndOfEdges.size();
    }

    public Stream<Node> atlasNodes()
    {
        return this.nodesAtEndOfEdges.stream().map(this::getNode);
    }

    public long atlasPointCount()
    {
        return atlasPoints().count();
    }

    public Stream<Node> atlasPoints()
    {
        return this.nodes.values().stream().filter(this::isAtlasPoint);
    }

    public void clear()
    {
        clearNodes();
        clearWays();
        this.relations.clear();
        this.nodesAtEndOfEdges.clear();
        this.nodesInRelations.clear();
    }

    public void clearNodes()
    {
        this.nodes.clear();
    }

    public void clearWays()
    {
        this.ways.clear();
    }

    @Override
    public void complete()
    {
    }

    public boolean containsNode(final long identifier)
    {
        return this.nodes.containsKey(identifier);
    }

    public boolean containsNodeAtEndOfEdges(final long identifier)
    {
        return this.nodesAtEndOfEdges.contains(identifier);
    }

    public boolean containsNodeInRelations(final long identifier)
    {
        return this.nodesInRelations.contains(identifier);
    }

    public boolean containsRelation(final long identifier)
    {
        return this.relations.containsKey(identifier);
    }

    public boolean containsWay(final long identifier)
    {
        return this.ways.containsKey(identifier);
    }

    public Node createNode(final long identifier, final double latitude, final double longitude)
    {
        return new Node(createNewEntityData(identifier, Maps.hashMap()), latitude, longitude);
    }

    public Node createNode(final long identifier, final double latitude, final double longitude,
            final Map<String, String> tags)
    {
        return new Node(createNewEntityData(identifier, tags), latitude, longitude);
    }

    public Polygon createPolygon(final Way way)
    {
        final List<WayNode> wayNodes = way.getWayNodes();
        final List<Location> locations = new ArrayList<>();
        for (int i = 0; i < wayNodes.size() - 1; i++)
        {
            locations.add(getNodeLocation(wayNodes.get(i).getNodeId()));
        }
        return new Polygon(locations);
    }

    public PolyLine createPolyline(final Way way)
    {
        final List<WayNode> wayNodes = way.getWayNodes();
        final List<Location> locations = new ArrayList<>();
        wayNodes.forEach(node -> locations.add(getNodeLocation(node.getNodeId())));
        return new PolyLine(locations);
    }

    public Relation createRelation(final Relation reference, final long identifier,
            final List<RelationMember> members)
    {
        return new Relation(createCommonEntityData(reference, identifier), members);
    }

    public RelationMember createRelationMember(final RelationMember reference,
            final long identifier)
    {
        return new RelationMember(identifier, reference.getMemberType(), reference.getMemberRole());
    }

    public Way createWay(final Way reference, final long identifier, final List<WayNode> wayNodes)
    {
        return new Way(createCommonEntityData(reference, identifier), wayNodes);
    }

    public void forEachAtlasEdge(final Consumer<Way> action)
    {
        this.ways.values().stream().filter(this::isAtlasEdge).forEach(action);
    }

    public void forEachNode(final BiConsumer<Long, Node> action)
    {
        this.nodes.forEach(action);
    }

    public void forEachRelation(final BiConsumer<Long, Relation> action)
    {
        this.relations.forEach(action);
    }

    public void forEachWay(final BiConsumer<Long, Way> action)
    {
        this.ways.forEach(action);
    }

    public AtlasLoadingOption getAtlasLoadingOption()
    {
        return this.atlasLoadingOption;
    }

    public Entity getEntity(final RelationMember relationMember)
    {
        switch (relationMember.getMemberType())
        {
            case Node:
                return this.getNode(relationMember.getMemberId());
            case Way:
                return this.getWay(relationMember.getMemberId());
            case Relation:
                return this.getRelation(relationMember.getMemberId());
            default:
                throw new CoreException("Unsupported relation member type, {}",
                        relationMember.getMemberType().toString());
        }
    }

    public Node getNode(final long identifier)
    {
        return this.nodes.get(identifier);
    }

    public Location getNodeLocation(final long identifier)
    {
        return getNodeLocation(getNode(identifier));
    }

    public Location getNodeLocation(final Node node)
    {
        return new Location(Latitude.degrees(node.getLatitude()),
                Longitude.degrees(node.getLongitude()));
    }

    public Map<Long, Node> getNodes()
    {
        return this.nodes;
    }

    public Set<Long> getNodesAtEndOfEdges()
    {
        return this.nodesAtEndOfEdges;
    }

    public Set<Long> getNodesInRelations()
    {
        return this.nodesInRelations;
    }

    public Relation getRelation(final long identifier)
    {
        return this.relations.get(identifier);
    }

    public Map<Long, Relation> getRelations()
    {
        return this.relations;
    }

    public Map<String, String> getTagMap(final Entity entity)
    {
        return new TagMap(entity.getTags()).getTags();
    }

    public Way getWay(final long identifier)
    {
        return this.ways.get(identifier);
    }

    public Map<Long, Way> getWays()
    {
        return this.ways;
    }

    public boolean hasSameCountryCode(final Node node)
    {
        final String countries = tagsWithCountryCode(node).get(ISOCountryTag.KEY);
        if (ISOCountryTag.COUNTRY_MISSING.equals(countries))
        {
            return true;
        }
        return hasSameCountryCode(countries);
    }

    public boolean hasSameCountryCode(final String countries)
    {
        final StringList countryList = COUNTRY_LIST_CONVERTER.convert(countries);
        if (this.defaultCountries.size() == 0)
        {
            return true;
        }
        for (final String country : countryList)
        {
            if (this.defaultCountries.contains(country))
            {
                return true;
            }
        }
        return false;
    }

    @Override
    public void initialize(final Map<String, Object> metaData)
    {
    }

    public boolean isAtlasArea(final long identifier)
    {
        final Way way = getWay(identifier);
        if (way != null)
        {
            return isAtlasArea(way);
        }
        return false;
    }

    public boolean isAtlasArea(final Way way)
    {
        final TagMap tags = new TagMap(way.getTags());
        return isRing(way) && moreThanThreeNodes(way) && !tags.hasHighwayTag() && !tags.matchFerry()
                && !tags.matchPier() && !tags.matchRailway();
    }

    public boolean isAtlasEdge(final long identifier)
    {
        final Way way = getWay(identifier);
        if (way != null)
        {
            return isAtlasEdge(way);
        }
        return false;
    }

    public boolean isAtlasEdge(final Way way)
    {
        return this.atlasLoadingOption.getEdgeFilter().test(Taggable.with(way.getTags()));
    }

    public boolean isAtlasLine(final long identifier)
    {
        final Way way = getWay(identifier);
        if (way != null)
        {
            return isAtlasLine(way);
        }
        return false;
    }

    public boolean isAtlasLine(final Way way)
    {
        return !isAtlasEdge(way) && (!isRing(way) || isOneNodeWay(way));
    }

    public boolean isAtlasNode(final long identifier)
    {
        return this.nodesAtEndOfEdges.contains(identifier);
    }

    public boolean isAtlasPoint(final long identifier)
    {
        final Node node = getNode(identifier);
        if (node != null)
        {
            return isAtlasPoint(node);
        }
        return false;
    }

    public boolean isAtlasPoint(final Node node)
    {
        if (containsNodeInRelations(node.getId()))
        {
            return true;
        }
        // All the OSM features will have tags that are added by the Atlas generation: last edit
        // time, last user (from PBF) as well as some synthetic boundary tags for the nodes that are
        // created at the provided boundary. Because an OSM Node becomes a Point only when it has
        // tags, the logic here needs to make sure not to count the synthetic tags to make that
        // decision.
        // Tags from OSM are the tags that all the nodes will have
        if (node.getTags().size() > AtlasTag.TAGS_FROM_OSM.size())
        {
            int counter = 0;
            for (final Tag tag : node.getTags())
            {
                // Tags from Atlas are the tags that only some nodes will have
                if (AtlasTag.TAGS_FROM_ATLAS.contains(tag.getKey()))
                {
                    counter++;
                }
            }
            return node.getTags().size() > AtlasTag.TAGS_FROM_OSM.size() + counter;
        }
        return false;
    }

    public boolean isOneNodeWay(final Way way)
    {
        return way.getWayNodes().size() == 1;
    }

    public boolean isRing(final Way way)
    {
        final List<WayNode> wayNodes = way.getWayNodes();
        return wayNodes.get(0).getNodeId() == wayNodes.get(wayNodes.size() - 1).getNodeId()
                && wayNodes.size() > 2;
    }

    public boolean moreThanThreeNodes(final Way way)
    {
        final int three = 3;
        return way.getWayNodes().size() > three;
    }

    public int nodeCount()
    {
        return this.nodes.size();
    }

    @Override
    public void process(final EntityContainer entityContainer)
    {
        final Entity entity = entityContainer.getEntity();
        switch (entity.getType())
        {
            case Node:
                this.nodes.put(entity.getId(), (Node) entity);
                break;
            case Way:
                this.ways.put(entity.getId(), (Way) entity);
                break;
            case Relation:
                this.relations.put(entity.getId(), (Relation) entity);
                break;
            default:
                break;
        }
    }

    public void rebuildNodesAtEndOfEdges()
    {
        this.nodesAtEndOfEdges.clear();
        atlasEdges().forEach(edge ->
        {
            final List<WayNode> wayNodes = edge.getWayNodes();
            this.nodesAtEndOfEdges.add(wayNodes.get(0).getNodeId());
            this.nodesAtEndOfEdges.add(wayNodes.get(wayNodes.size() - 1).getNodeId());
        });
    }

    public void rebuildNodesInRelations()
    {
        this.nodesInRelations.clear();
        for (final Relation relation : this.relations.values())
        {
            for (final RelationMember member : relation.getMembers())
            {
                if (member.getMemberType() == EntityType.Node)
                {
                    final Long identifier = member.getMemberId();
                    final Node node = getNode(identifier);
                    if (node != null && hasSameCountryCode(node))
                    {
                        this.nodesInRelations.add(identifier);
                    }
                }
            }
        }
    }

    public int relationCount()
    {
        return this.relations.size();
    }

    @Override
    public void release()
    {
    }

    public Node removeNode(final long identifier)
    {
        if (this.nodesAtEndOfEdges.contains(identifier))
        {
            this.nodesAtEndOfEdges.remove(identifier);
        }
        return this.nodes.remove(identifier);
    }

    public void removeOutsideWays()
    {
        final Set<Long> waysToRemove = new HashSet<>();
        getWays().values().forEach(way ->
        {
            final String countries = getTagMap(way).get(ISOCountryTag.KEY);
            if (countries != null && !hasSameCountryCode(countries))
            {
                waysToRemove.add(way.getId());
            }
        });
        waysToRemove.forEach(this::removeWay);
    }

    public Relation removeRelation(final long identifier)
    {
        return this.relations.remove(identifier);
    }

    public Way removeWay(final long identifier)
    {
        return this.ways.remove(identifier);
    }

    public List<WayNode> reverse(final List<WayNode> wayNodes)
    {
        final List<WayNode> copy = new ArrayList<>(wayNodes);
        Collections.reverse(copy);
        return copy;
    }

    public Way reverse(final Way reference)
    {
        return createWay(reference, reference.getId(), reverse(reference.getWayNodes()));
    }

    @Override
    public void run()
    {
        forEachNode((nodeIdentifier, node) -> this.sink.process(new NodeContainer(node)));
        forEachWay((wayIdentifier, way) -> this.sink.process(new WayContainer(way)));
        forEachRelation((relationIdentifier, relation) -> this.sink
                .process(new RelationContainer(relation)));
        this.sink.complete();
        this.sink.release();
    }

    @Override
    public void setSink(final Sink sink)
    {
        this.sink = sink;
    }

    public Map<String, String> tagsWithCountryCode(final Node node)
    {
        final Map<String, String> tags = new HashMap<>();
        tags.putAll(getTagMap(node));

        // For nodes generated by country slicing, they should have country code already, we don't
        // want to assign new country code for them
        if (tags.get(ISOCountryTag.KEY) == null)
        {
            if (this.countryBoundaryMap != null)
            {
                // Get the country code details
                final CountryCodeProperties countryProperties = this.countryBoundaryMap
                        .getCountryCodeISO3(JTS_POINT_CONVERTER
                                .convert(new Location(Latitude.degrees(node.getLatitude()),
                                        Longitude.degrees(node.getLongitude()))),
                                true);

                // Store the country code
                tags.put(ISOCountryTag.KEY, countryProperties.getIso3CountryCode());

                // If we used nearest neighbor logic to determine the country code, add a tag
                // to indicate this
                if (countryProperties.usingNearestNeighbor())
                {
                    tags.put(SyntheticNearestNeighborCountryCodeTag.KEY,
                            SyntheticNearestNeighborCountryCodeTag.YES.toString());
                }
            }
            else
            {
                tags.put(ISOCountryTag.KEY, ISOCountryTag.COUNTRY_MISSING);
            }
        }
        return tags;
    }

    public int wayCount()
    {
        return this.ways.size();
    }

    public void writeXml(final File file) throws IOException
    {
        final XmlWriter writer = new XmlWriter(file, CompressionMethod.None);
        this.setSink(writer);
        this.run();
    }

    private CommonEntityData createCommonEntityData(final Entity entity, final long identifier)
    {
        return new CommonEntityData(identifier, entity.getVersion(), entity.getTimestampContainer(),
                entity.getUser(), entity.getChangesetId(), entity.getTags());
    }

    private CommonEntityData createNewEntityData(final long identifier,
            final Map<String, String> tags)
    {
        final int fakeIdentifier = 195873;
        final String fakeUser = "country_slicer";
        final CommonEntityData data = new CommonEntityData(identifier, 1, new Date(),
                new OsmUser(fakeIdentifier, fakeUser), 1,
                TAG_MAP_TO_TAG_COLLECTION_CONVERTER.convert(tags));
        return data;
    }
}
