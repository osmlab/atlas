package org.openstreetmap.atlas.geography.atlas.builder.proto;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.Latitude;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.Longitude;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.Polygon;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.AtlasMetaData;
import org.openstreetmap.atlas.geography.atlas.builder.AtlasSize;
import org.openstreetmap.atlas.geography.atlas.builder.RelationBean;
import org.openstreetmap.atlas.geography.atlas.items.Area;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.ItemType;
import org.openstreetmap.atlas.geography.atlas.items.Line;
import org.openstreetmap.atlas.geography.atlas.items.Node;
import org.openstreetmap.atlas.geography.atlas.items.Point;
import org.openstreetmap.atlas.geography.atlas.items.Relation;
import org.openstreetmap.atlas.geography.atlas.items.RelationMember;
import org.openstreetmap.atlas.geography.atlas.packed.PackedAtlas;
import org.openstreetmap.atlas.geography.atlas.packed.PackedAtlasBuilder;
import org.openstreetmap.atlas.geography.converters.proto.ProtoLocationConverter;
import org.openstreetmap.atlas.geography.converters.proto.ProtoTagListConverter;
import org.openstreetmap.atlas.proto.ProtoArea;
import org.openstreetmap.atlas.proto.ProtoAtlasContainer;
import org.openstreetmap.atlas.proto.ProtoAtlasContainer.Builder;
import org.openstreetmap.atlas.proto.ProtoEdge;
import org.openstreetmap.atlas.proto.ProtoLine;
import org.openstreetmap.atlas.proto.ProtoLocation;
import org.openstreetmap.atlas.proto.ProtoNode;
import org.openstreetmap.atlas.proto.ProtoPoint;
import org.openstreetmap.atlas.proto.ProtoRelation;
import org.openstreetmap.atlas.streaming.resource.Resource;
import org.openstreetmap.atlas.streaming.resource.WritableResource;
import org.openstreetmap.atlas.utilities.collections.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.InvalidProtocolBufferException;

/**
 * Build an Atlas from a naive proto serialized file, or write an Atlas to a naive proto serialized
 * file. At this stage, the builder can only handle Atlases that have tagged Points and Lines.
 *
 * @author lcram
 */
public class ProtoAtlasBuilder
{
    // File extension for naive protoatlas format
    public static final String SUGGESTED_FILE_EXTENSION = ".npatlas";

    private static final Logger logger = LoggerFactory.getLogger(ProtoAtlasBuilder.class);

    /**
     * Read a resource in naive ProtoAtlas format into a PackedAtlas.
     *
     * @param resource
     *            the resource in naive ProtoAtlas format
     * @return the constructed PackedAtlas
     */
    public PackedAtlas read(final Resource resource)
    {
        ProtoAtlasContainer protoAtlasContainer = null;

        // First, we need to construct the container object from the proto binary
        try
        {
            protoAtlasContainer = ProtoAtlasContainer.parseFrom(resource.readBytesAndClose());
        }
        catch (final InvalidProtocolBufferException exception)
        {
            throw new CoreException("Error deserializing the ProtoAtlasContainer from {}",
                    resource.getName(), exception);
        }

        // initialize atlas metadata
        final AtlasSize atlasSize = new AtlasSize(protoAtlasContainer.getNumberOfEdges(),
                protoAtlasContainer.getNumberOfNodes(), protoAtlasContainer.getNumberOfAreas(),
                protoAtlasContainer.getNumberOfLines(), protoAtlasContainer.getNumberOfPoints(),
                protoAtlasContainer.getNumberOfRelations());
        // TODO it would be nice to programmatically determine which protobuf version is in use
        final AtlasMetaData atlasMetaData = new AtlasMetaData(atlasSize, true, "unknown",
                "ProtoAtlas", "unknown", "unknown", Maps.hashMap());
        final PackedAtlasBuilder builder = new PackedAtlasBuilder().withSizeEstimates(atlasSize)
                .withMetaData(atlasMetaData).withName(resource.getName());

        // build the atlas features
        parsePoints(builder, protoAtlasContainer.getPointsList());
        parseLines(builder, protoAtlasContainer.getLinesList());
        parseAreas(builder, protoAtlasContainer.getAreasList());
        parseNodes(builder, protoAtlasContainer.getNodesList());
        parseEdges(builder, protoAtlasContainer.getEdgesList());
        parseRelations(builder, protoAtlasContainer.getRelationsList());

        return (PackedAtlas) builder.get();
    }

    /**
     * Write an Atlas to a resource in the naive ProtoAtlas format.
     *
     * @param atlas
     *            the Atlas to be written
     * @param resource
     *            the resource to write into
     */
    public void write(final Atlas atlas, final WritableResource resource)
    {
        final ProtoAtlasContainer.Builder protoAtlasBuilder = ProtoAtlasContainer.newBuilder();

        // put the Atlas features into the ProtoAtlasBuilder
        writePointsToBuilder(atlas, protoAtlasBuilder);
        writeLinesToBuilder(atlas, protoAtlasBuilder);
        writeAreasToBuilder(atlas, protoAtlasBuilder);
        writeNodesToBuilder(atlas, protoAtlasBuilder);
        writeEdgesToBuilder(atlas, protoAtlasBuilder);
        writeRelationsToBuilder(atlas, protoAtlasBuilder);

        final ProtoAtlasContainer protoAtlas = protoAtlasBuilder.build();
        resource.writeAndClose(protoAtlas.toByteArray());
    }

    private void parseAreas(final PackedAtlasBuilder builder, final List<ProtoArea> areas)
    {
        final ProtoLocationConverter protoLocationConverter = new ProtoLocationConverter();
        final ProtoTagListConverter protoTagListConverter = new ProtoTagListConverter();
        areas.forEach(protoArea ->
        {
            final long identifier = protoArea.getId();
            final List<Location> shapePoints = protoArea.getShapePointsList().stream()
                    .map(protoLocationConverter::convert).collect(Collectors.toList());
            final Polygon geometry = new Polygon(shapePoints);
            final Map<String, String> tags = protoTagListConverter.convert(protoArea.getTagsList());
            // TODO see Mike's PR note about duplicate points, since start and end are the same for
            // polygons. Make sure this is not happening. I think it's fine since I am building the
            // Polygon using the Polygon constructor from a list of points that contains no
            // duplicates.
            builder.addArea(identifier, geometry, tags);
        });
    }

    private void parseEdges(final PackedAtlasBuilder builder, final List<ProtoEdge> edges)
    {
        final ProtoLocationConverter protoLocationConverter = new ProtoLocationConverter();
        final ProtoTagListConverter protoTagListConverter = new ProtoTagListConverter();
        edges.forEach(protoEdge ->
        {
            final long identifier = protoEdge.getId();
            final List<Location> shapePoints = protoEdge.getShapePointsList().stream()
                    .map(protoLocationConverter::convert).collect(Collectors.toList());
            final PolyLine geometry = new PolyLine(shapePoints);
            final Map<String, String> tags = protoTagListConverter.convert(protoEdge.getTagsList());
            builder.addEdge(identifier, geometry, tags);
        });
    }

    private void parseLines(final PackedAtlasBuilder builder, final List<ProtoLine> lines)
    {
        final ProtoLocationConverter protoLocationConverter = new ProtoLocationConverter();
        final ProtoTagListConverter protoTagListConverter = new ProtoTagListConverter();
        lines.forEach(protoLine ->
        {
            final long identifier = protoLine.getId();
            final List<Location> shapePoints = protoLine.getShapePointsList().stream()
                    .map(protoLocationConverter::convert).collect(Collectors.toList());
            final PolyLine geometry = new PolyLine(shapePoints);
            final Map<String, String> tags = protoTagListConverter.convert(protoLine.getTagsList());
            builder.addLine(identifier, geometry, tags);
        });
    }

    private void parseNodes(final PackedAtlasBuilder builder, final List<ProtoNode> nodes)
    {
        final ProtoTagListConverter protoTagListConverter = new ProtoTagListConverter();
        nodes.forEach(protoNode ->
        {
            final long identifier = protoNode.getId();
            final Longitude longitude = Longitude.dm7(protoNode.getLocation().getLongitude());
            final Latitude latitude = Latitude.dm7(protoNode.getLocation().getLatitude());
            final Location geometry = new Location(latitude, longitude);
            final Map<String, String> tags = protoTagListConverter.convert(protoNode.getTagsList());
            builder.addNode(identifier, geometry, tags);
        });
    }

    private void parsePoints(final PackedAtlasBuilder builder, final List<ProtoPoint> points)
    {
        final ProtoTagListConverter protoTagListConverter = new ProtoTagListConverter();
        points.forEach(protoPoint ->
        {
            final long identifier = protoPoint.getId();
            final Longitude longitude = Longitude.dm7(protoPoint.getLocation().getLongitude());
            final Latitude latitude = Latitude.dm7(protoPoint.getLocation().getLatitude());
            final Location geometry = new Location(latitude, longitude);
            final Map<String, String> tags = protoTagListConverter
                    .convert(protoPoint.getTagsList());
            builder.addPoint(identifier, geometry, tags);
        });
    }

    private RelationBean parseRelationBean(final ProtoRelation protoRelation)
    {
        final RelationBean bean = new RelationBean();

        protoRelation.getBeansList().forEach(protoRelationBean ->
        {
            final long memberId = protoRelationBean.getMemberId();
            final String memberRole = protoRelationBean.getMemberRole();
            final ItemType memberType = ItemType
                    .forValue(protoRelationBean.getMemberType().getNumber());
            bean.addItem(memberId, memberRole, memberType);
        });

        return bean;
    }

    private void parseRelations(final PackedAtlasBuilder builder,
            final List<ProtoRelation> relations)
    {
        final ProtoTagListConverter protoTagListConverter = new ProtoTagListConverter();
        relations.forEach(protoRelation ->
        {
            final long identifier = protoRelation.getId();
            final RelationBean bean = parseRelationBean(protoRelation);
            final Map<String, String> tags = protoTagListConverter
                    .convert(protoRelation.getTagsList());
            // TODO should identifier and OSMIdentifier be the same?
            builder.addRelation(identifier, identifier, bean, tags);
        });
    }

    private void writeAreasToBuilder(final Atlas atlas, final Builder protoAtlasBuilder)
    {
        long numberOfAreas = 0;
        final ProtoLocationConverter protoLocationConverter = new ProtoLocationConverter();
        final ProtoTagListConverter protoTagListConverter = new ProtoTagListConverter();

        for (final Area area : atlas.areas())
        {
            final ProtoArea.Builder protoAreaBuilder = ProtoArea.newBuilder();
            protoAreaBuilder.setId(area.getIdentifier());

            final List<ProtoLocation> protoLocations = area.asPolygon().stream()
                    .map(protoLocationConverter::backwardConvert).collect(Collectors.toList());
            protoAreaBuilder.addAllShapePoints(protoLocations);

            final Map<String, String> tags = area.getTags();
            protoAreaBuilder.addAllTags(protoTagListConverter.backwardConvert(tags));

            numberOfAreas++;
            protoAtlasBuilder.addAreas(protoAreaBuilder.build());
        }
        protoAtlasBuilder.setNumberOfAreas(numberOfAreas);
    }

    private void writeEdgesToBuilder(final Atlas atlas, final Builder protoAtlasBuilder)
    {
        long numberOfEdges = 0;
        final ProtoLocationConverter protoLocationConverter = new ProtoLocationConverter();
        final ProtoTagListConverter protoTagListConverter = new ProtoTagListConverter();

        for (final Edge edge : atlas.edges())
        {
            final ProtoEdge.Builder protoEdgeBuilder = ProtoEdge.newBuilder();
            protoEdgeBuilder.setId(edge.getIdentifier());

            final List<ProtoLocation> protoLocations = edge.asPolyLine().stream()
                    .map(protoLocationConverter::backwardConvert).collect(Collectors.toList());
            protoEdgeBuilder.addAllShapePoints(protoLocations);

            final Map<String, String> tags = edge.getTags();
            protoEdgeBuilder.addAllTags(protoTagListConverter.backwardConvert(tags));

            numberOfEdges++;
            protoAtlasBuilder.addEdges(protoEdgeBuilder.build());
        }
        protoAtlasBuilder.setNumberOfEdges(numberOfEdges);
    }

    private void writeLinesToBuilder(final Atlas atlas,
            final ProtoAtlasContainer.Builder protoAtlasBuilder)
    {
        long numberOfLines = 0;
        final ProtoLocationConverter protoLocationConverter = new ProtoLocationConverter();
        final ProtoTagListConverter protoTagListConverter = new ProtoTagListConverter();

        for (final Line line : atlas.lines())
        {
            final ProtoLine.Builder protoLineBuilder = ProtoLine.newBuilder();
            protoLineBuilder.setId(line.getIdentifier());

            final List<ProtoLocation> protoLocations = line.asPolyLine().stream()
                    .map(protoLocationConverter::backwardConvert).collect(Collectors.toList());
            protoLineBuilder.addAllShapePoints(protoLocations);

            final Map<String, String> tags = line.getTags();
            protoLineBuilder.addAllTags(protoTagListConverter.backwardConvert(tags));

            numberOfLines++;
            protoAtlasBuilder.addLines(protoLineBuilder.build());
        }
        protoAtlasBuilder.setNumberOfLines(numberOfLines);
    }

    private void writeNodesToBuilder(final Atlas atlas, final Builder protoAtlasBuilder)
    {
        long numberOfNodes = 0;
        final ProtoLocationConverter protoLocationConverter = new ProtoLocationConverter();
        final ProtoTagListConverter protoTagListConverter = new ProtoTagListConverter();

        for (final Node node : atlas.nodes())
        {
            final ProtoNode.Builder protoNodeBuilder = ProtoNode.newBuilder();

            protoNodeBuilder.setId(node.getIdentifier());
            protoNodeBuilder
                    .setLocation(protoLocationConverter.backwardConvert(node.getLocation()));

            final Map<String, String> tags = node.getTags();
            protoNodeBuilder.addAllTags(protoTagListConverter.backwardConvert(tags));

            numberOfNodes++;
            protoAtlasBuilder.addNodes(protoNodeBuilder.build());
        }
        protoAtlasBuilder.setNumberOfNodes(numberOfNodes);
    }

    private void writePointsToBuilder(final Atlas atlas,
            final ProtoAtlasContainer.Builder protoAtlasBuilder)
    {
        long numberOfPoints = 0;
        final ProtoLocationConverter protoLocationConverter = new ProtoLocationConverter();
        final ProtoTagListConverter protoTagListConverter = new ProtoTagListConverter();

        for (final Point point : atlas.points())
        {
            final ProtoPoint.Builder protoPointBuilder = ProtoPoint.newBuilder();

            protoPointBuilder.setId(point.getIdentifier());
            protoPointBuilder
                    .setLocation(protoLocationConverter.backwardConvert(point.getLocation()));

            final Map<String, String> tags = point.getTags();
            protoPointBuilder.addAllTags(protoTagListConverter.backwardConvert(tags));

            numberOfPoints++;
            protoAtlasBuilder.addPoints(protoPointBuilder.build());
        }
        protoAtlasBuilder.setNumberOfPoints(numberOfPoints);
    }

    private void writeRelationsToBuilder(final Atlas atlas, final Builder protoAtlasBuilder)
    {
        long numberOfRelations = 0;
        final ProtoTagListConverter protoTagListConverter = new ProtoTagListConverter();

        for (final Relation relation : atlas.relations())
        {
            final ProtoRelation.Builder protoRelationBuilder = ProtoRelation.newBuilder();
            protoRelationBuilder.setId(relation.getIdentifier());
            for (final RelationMember member : relation.members())
            {
                final ProtoRelation.RelationBean.Builder beanBuilder = ProtoRelation.RelationBean
                        .newBuilder();
                beanBuilder.setMemberId(member.getEntity().getIdentifier());
                beanBuilder.setMemberRole(member.getRole());
                final ItemType type = ItemType.forEntity(member.getEntity());
                beanBuilder.setMemberType(ProtoRelation.ProtoItemType.valueOf(type.getValue()));

                // TODO there may be a problem here if there are more relations in this atlas than
                // possible int32 values. This is because protobuf internally stores the protobeans
                // list as an ArrayList
                protoRelationBuilder.addBeans(beanBuilder.build());

            }
            final Map<String, String> tags = relation.getTags();
            protoRelationBuilder.addAllTags(protoTagListConverter.backwardConvert(tags));

            numberOfRelations++;
            protoAtlasBuilder.addRelations(protoRelationBuilder.build());
        }
        protoAtlasBuilder.setNumberOfRelations(numberOfRelations);
    }
}
