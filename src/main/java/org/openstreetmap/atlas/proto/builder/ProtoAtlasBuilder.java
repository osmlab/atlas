package org.openstreetmap.atlas.proto.builder;

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
import org.openstreetmap.atlas.geography.atlas.packed.PackedAtlasSerializer;
import org.openstreetmap.atlas.geography.atlas.pbf.slicing.identifier.ReverseIdentifierFactory;
import org.openstreetmap.atlas.proto.ProtoArea;
import org.openstreetmap.atlas.proto.ProtoAtlas;
import org.openstreetmap.atlas.proto.ProtoAtlasMetaData;
import org.openstreetmap.atlas.proto.ProtoEdge;
import org.openstreetmap.atlas.proto.ProtoLine;
import org.openstreetmap.atlas.proto.ProtoLocation;
import org.openstreetmap.atlas.proto.ProtoNode;
import org.openstreetmap.atlas.proto.ProtoPoint;
import org.openstreetmap.atlas.proto.ProtoRelation;
import org.openstreetmap.atlas.proto.converters.ProtoLocationConverter;
import org.openstreetmap.atlas.proto.converters.ProtoTagListConverter;
import org.openstreetmap.atlas.streaming.resource.Resource;
import org.openstreetmap.atlas.streaming.resource.WritableResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.InvalidProtocolBufferException;

/**
 * Build an {@link Atlas} from a ProtoAtlas formatted file, or write an {@link Atlas} to a
 * ProtoAtlas formatted file. ProtoAtlas is a naive encoding for {@link Atlas}es using protocol
 * buffers. A more compact and performant encoding can be obtained by using
 * {@link PackedAtlasSerializer}.
 *
 * @author lcram
 */
public class ProtoAtlasBuilder
{
    private static final ProtoLocationConverter PROTOLOCATION_CONVERTER = new ProtoLocationConverter();
    private static final ProtoTagListConverter PROTOTAG_LIST_CONVERTER = new ProtoTagListConverter();
    private static final ReverseIdentifierFactory REVERSE_IDENTIFIER_FACTORY = new ReverseIdentifierFactory();
    private static final Logger logger = LoggerFactory.getLogger(ProtoAtlasBuilder.class);

    /*
     * When performing serialization, metadata fields with 'null' values will be serialized as
     * "unknown". When deserializing, fields not present in the proto object will be interpreted
     * with this value.
     */
    private static final String NULL_SENTINEL = "unknown";

    /*
     * String that describes the data format of the atlas. This is used by the AtlasMetaData class
     * to record this version.
     */
    public static final String PROTOATLAS_DATA_VERSION = "ProtoAtlas";

    /**
     * Read a resource in naive ProtoAtlas format into a PackedAtlas.
     *
     * @param resource
     *            the resource in naive ProtoAtlas format
     * @return the constructed PackedAtlas
     */
    public PackedAtlas read(final Resource resource)
    {
        ProtoAtlas protoAtlas = null;

        // First, we need to construct the container object from the proto binary
        try
        {
            protoAtlas = ProtoAtlas.parseFrom(resource.readBytesAndClose());
        }
        catch (final InvalidProtocolBufferException exception)
        {
            throw new CoreException("Error deserializing the ProtoAtlasContainer from {}",
                    resource.getName(), exception);
        }

        // TODO make sure metadata read is consistent with what is written
        final ProtoAtlasMetaData protoAtlasMetaData = protoAtlas.getMetaData();

        AtlasSize atlasSize = null;
        final boolean hasAllAtlasSizeFeatures = protoAtlasMetaData.hasEdgeNumber()
                && protoAtlasMetaData.hasNodeNumber() && protoAtlasMetaData.hasAreaNumber()
                && protoAtlasMetaData.hasLineNumber() && protoAtlasMetaData.hasPointNumber()
                && protoAtlasMetaData.hasRelationNumber();
        if (hasAllAtlasSizeFeatures)
        {
            atlasSize = new AtlasSize(protoAtlasMetaData.getEdgeNumber(),
                    protoAtlasMetaData.getNodeNumber(), protoAtlasMetaData.getAreaNumber(),
                    protoAtlasMetaData.getLineNumber(), protoAtlasMetaData.getPointNumber(),
                    protoAtlasMetaData.getRelationNumber());
        }
        else
        {
            logger.warn("Could not deserialize AtlasSize, using defaults");
            atlasSize = AtlasSize.DEFAULT;
        }

        final String codeVersion = protoAtlasMetaData.hasCodeVersion()
                ? protoAtlasMetaData.getCodeVersion() : NULL_SENTINEL;
        final String dataVersion = protoAtlasMetaData.hasDataVersion()
                ? protoAtlasMetaData.getDataVersion() : NULL_SENTINEL;
        final String country = protoAtlasMetaData.hasCountry() ? protoAtlasMetaData.getCountry()
                : NULL_SENTINEL;
        final String shardName = protoAtlasMetaData.hasShardName()
                ? protoAtlasMetaData.getShardName() : NULL_SENTINEL;

        final Map<String, String> tags = PROTOTAG_LIST_CONVERTER
                .convert(protoAtlasMetaData.getTagsList());

        final AtlasMetaData atlasMetaData = new AtlasMetaData(atlasSize,
                protoAtlasMetaData.getOriginal(), codeVersion, dataVersion, country, shardName,
                tags);

        final PackedAtlasBuilder builder = new PackedAtlasBuilder().withSizeEstimates(atlasSize)
                .withMetaData(atlasMetaData).withName(resource.getName());

        // build the atlas features
        parsePoints(builder, protoAtlas.getPointsList());
        parseLines(builder, protoAtlas.getLinesList());
        parseAreas(builder, protoAtlas.getAreasList());
        parseNodes(builder, protoAtlas.getNodesList());
        parseEdges(builder, protoAtlas.getEdgesList());
        parseRelations(builder, protoAtlas.getRelationsList());

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
        final ProtoAtlas.Builder protoAtlasBuilder = ProtoAtlas.newBuilder();

        // put the Atlas features into the ProtoAtlasBuilder
        writePointsToBuilder(atlas, protoAtlasBuilder);
        writeLinesToBuilder(atlas, protoAtlasBuilder);
        writeAreasToBuilder(atlas, protoAtlasBuilder);
        writeNodesToBuilder(atlas, protoAtlasBuilder);
        writeEdgesToBuilder(atlas, protoAtlasBuilder);
        writeRelationsToBuilder(atlas, protoAtlasBuilder);

        final AtlasMetaData atlasMetaData = atlas.metaData();
        final ProtoAtlasMetaData.Builder protoMetaDataBuilder = ProtoAtlasMetaData.newBuilder();
        if (atlasMetaData.getSize() != null)
        {
            protoMetaDataBuilder.setEdgeNumber(atlasMetaData.getSize().getEdgeNumber());
            protoMetaDataBuilder.setNodeNumber(atlasMetaData.getSize().getNodeNumber());
            protoMetaDataBuilder.setAreaNumber(atlasMetaData.getSize().getAreaNumber());
            protoMetaDataBuilder.setLineNumber(atlasMetaData.getSize().getLineNumber());
            protoMetaDataBuilder.setPointNumber(atlasMetaData.getSize().getPointNumber());
            protoMetaDataBuilder.setRelationNumber(atlasMetaData.getSize().getRelationNumber());
        }
        protoMetaDataBuilder.setOriginal(atlasMetaData.isOriginal());

        atlasMetaData.getCodeVersion().ifPresent(value ->
        {
            protoMetaDataBuilder.setCodeVersion(value);
        });

        atlasMetaData.getDataVersion().ifPresent(value ->
        {
            protoMetaDataBuilder.setDataVersion(value);
        });

        atlasMetaData.getCountry().ifPresent(value ->
        {
            protoMetaDataBuilder.setCountry(value);
        });

        atlasMetaData.getShardName().ifPresent(value ->
        {
            protoMetaDataBuilder.setShardName(value);
        });

        if (atlasMetaData.getTags() != null)
        {
            protoMetaDataBuilder
                    .addAllTags(PROTOTAG_LIST_CONVERTER.backwardConvert(atlasMetaData.getTags()));
        }

        protoAtlasBuilder.setMetaData(protoMetaDataBuilder);

        final ProtoAtlas protoAtlas = protoAtlasBuilder.build();
        resource.writeAndClose(protoAtlas.toByteArray());
    }

    private void parseAreas(final PackedAtlasBuilder builder, final List<ProtoArea> areas)
    {
        areas.forEach(protoArea ->
        {
            final long identifier = protoArea.getId();
            final List<Location> shapePoints = protoArea.getShapePointsList().stream()
                    .map(ProtoAtlasBuilder.PROTOLOCATION_CONVERTER::convert)
                    .collect(Collectors.toList());
            final Polygon geometry = new Polygon(shapePoints);
            final Map<String, String> tags = ProtoAtlasBuilder.PROTOTAG_LIST_CONVERTER
                    .convert(protoArea.getTagsList());
            builder.addArea(identifier, geometry, tags);
        });
    }

    private void parseEdges(final PackedAtlasBuilder builder, final List<ProtoEdge> edges)
    {
        edges.forEach(protoEdge ->
        {
            final long identifier = protoEdge.getId();
            final List<Location> shapePoints = protoEdge.getShapePointsList().stream()
                    .map(ProtoAtlasBuilder.PROTOLOCATION_CONVERTER::convert)
                    .collect(Collectors.toList());
            final PolyLine geometry = new PolyLine(shapePoints);
            final Map<String, String> tags = ProtoAtlasBuilder.PROTOTAG_LIST_CONVERTER
                    .convert(protoEdge.getTagsList());
            builder.addEdge(identifier, geometry, tags);
        });
    }

    private void parseLines(final PackedAtlasBuilder builder, final List<ProtoLine> lines)
    {
        lines.forEach(protoLine ->
        {
            final long identifier = protoLine.getId();
            final List<Location> shapePoints = protoLine.getShapePointsList().stream()
                    .map(ProtoAtlasBuilder.PROTOLOCATION_CONVERTER::convert)
                    .collect(Collectors.toList());
            final PolyLine geometry = new PolyLine(shapePoints);
            final Map<String, String> tags = ProtoAtlasBuilder.PROTOTAG_LIST_CONVERTER
                    .convert(protoLine.getTagsList());
            builder.addLine(identifier, geometry, tags);
        });
    }

    private void parseNodes(final PackedAtlasBuilder builder, final List<ProtoNode> nodes)
    {
        nodes.forEach(protoNode ->
        {
            final long identifier = protoNode.getId();
            final Longitude longitude = Longitude.dm7(protoNode.getLocation().getLongitude());
            final Latitude latitude = Latitude.dm7(protoNode.getLocation().getLatitude());
            final Location geometry = new Location(latitude, longitude);
            final Map<String, String> tags = ProtoAtlasBuilder.PROTOTAG_LIST_CONVERTER
                    .convert(protoNode.getTagsList());
            builder.addNode(identifier, geometry, tags);
        });
    }

    private void parsePoints(final PackedAtlasBuilder builder, final List<ProtoPoint> points)
    {
        points.forEach(protoPoint ->
        {
            final long identifier = protoPoint.getId();
            final Longitude longitude = Longitude.dm7(protoPoint.getLocation().getLongitude());
            final Latitude latitude = Latitude.dm7(protoPoint.getLocation().getLatitude());
            final Location geometry = new Location(latitude, longitude);
            final Map<String, String> tags = ProtoAtlasBuilder.PROTOTAG_LIST_CONVERTER
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
        relations.forEach(protoRelation ->
        {
            final long identifier = protoRelation.getId();
            final RelationBean bean = parseRelationBean(protoRelation);
            final Map<String, String> tags = ProtoAtlasBuilder.PROTOTAG_LIST_CONVERTER
                    .convert(protoRelation.getTagsList());
            builder.addRelation(identifier,
                    ProtoAtlasBuilder.REVERSE_IDENTIFIER_FACTORY.getOsmIdentifier(identifier), bean,
                    tags);
        });
    }

    private void writeAreasToBuilder(final Atlas atlas, final ProtoAtlas.Builder protoAtlasBuilder)
    {
        long numberOfAreas = 0;

        for (final Area area : atlas.areas())
        {
            final ProtoArea.Builder protoAreaBuilder = ProtoArea.newBuilder();
            protoAreaBuilder.setId(area.getIdentifier());

            final List<ProtoLocation> protoLocations = area.asPolygon().stream()
                    .map(ProtoAtlasBuilder.PROTOLOCATION_CONVERTER::backwardConvert)
                    .collect(Collectors.toList());
            protoAreaBuilder.addAllShapePoints(protoLocations);

            final Map<String, String> tags = area.getTags();
            protoAreaBuilder
                    .addAllTags(ProtoAtlasBuilder.PROTOTAG_LIST_CONVERTER.backwardConvert(tags));

            numberOfAreas++;
            protoAtlasBuilder.addAreas(protoAreaBuilder.build());
        }
        protoAtlasBuilder.setNumberOfAreas(numberOfAreas);
    }

    private void writeEdgesToBuilder(final Atlas atlas, final ProtoAtlas.Builder protoAtlasBuilder)
    {
        long numberOfEdges = 0;

        for (final Edge edge : atlas.edges())
        {
            final ProtoEdge.Builder protoEdgeBuilder = ProtoEdge.newBuilder();
            protoEdgeBuilder.setId(edge.getIdentifier());

            final List<ProtoLocation> protoLocations = edge.asPolyLine().stream()
                    .map(ProtoAtlasBuilder.PROTOLOCATION_CONVERTER::backwardConvert)
                    .collect(Collectors.toList());
            protoEdgeBuilder.addAllShapePoints(protoLocations);

            final Map<String, String> tags = edge.getTags();
            protoEdgeBuilder
                    .addAllTags(ProtoAtlasBuilder.PROTOTAG_LIST_CONVERTER.backwardConvert(tags));

            numberOfEdges++;
            protoAtlasBuilder.addEdges(protoEdgeBuilder.build());
        }
        protoAtlasBuilder.setNumberOfEdges(numberOfEdges);
    }

    private void writeLinesToBuilder(final Atlas atlas, final ProtoAtlas.Builder protoAtlasBuilder)
    {
        long numberOfLines = 0;

        for (final Line line : atlas.lines())
        {
            final ProtoLine.Builder protoLineBuilder = ProtoLine.newBuilder();
            protoLineBuilder.setId(line.getIdentifier());

            final List<ProtoLocation> protoLocations = line.asPolyLine().stream()
                    .map(ProtoAtlasBuilder.PROTOLOCATION_CONVERTER::backwardConvert)
                    .collect(Collectors.toList());
            protoLineBuilder.addAllShapePoints(protoLocations);

            final Map<String, String> tags = line.getTags();
            protoLineBuilder
                    .addAllTags(ProtoAtlasBuilder.PROTOTAG_LIST_CONVERTER.backwardConvert(tags));

            numberOfLines++;
            protoAtlasBuilder.addLines(protoLineBuilder.build());
        }
        protoAtlasBuilder.setNumberOfLines(numberOfLines);
    }

    private void writeNodesToBuilder(final Atlas atlas, final ProtoAtlas.Builder protoAtlasBuilder)
    {
        long numberOfNodes = 0;

        for (final Node node : atlas.nodes())
        {
            final ProtoNode.Builder protoNodeBuilder = ProtoNode.newBuilder();

            protoNodeBuilder.setId(node.getIdentifier());
            protoNodeBuilder.setLocation(
                    ProtoAtlasBuilder.PROTOLOCATION_CONVERTER.backwardConvert(node.getLocation()));

            final Map<String, String> tags = node.getTags();
            protoNodeBuilder
                    .addAllTags(ProtoAtlasBuilder.PROTOTAG_LIST_CONVERTER.backwardConvert(tags));

            numberOfNodes++;
            protoAtlasBuilder.addNodes(protoNodeBuilder.build());
        }
        protoAtlasBuilder.setNumberOfNodes(numberOfNodes);
    }

    private void writePointsToBuilder(final Atlas atlas, final ProtoAtlas.Builder protoAtlasBuilder)
    {
        long numberOfPoints = 0;

        for (final Point point : atlas.points())
        {
            final ProtoPoint.Builder protoPointBuilder = ProtoPoint.newBuilder();

            protoPointBuilder.setId(point.getIdentifier());
            protoPointBuilder.setLocation(
                    ProtoAtlasBuilder.PROTOLOCATION_CONVERTER.backwardConvert(point.getLocation()));

            final Map<String, String> tags = point.getTags();
            protoPointBuilder
                    .addAllTags(ProtoAtlasBuilder.PROTOTAG_LIST_CONVERTER.backwardConvert(tags));

            numberOfPoints++;
            protoAtlasBuilder.addPoints(protoPointBuilder.build());
        }
        protoAtlasBuilder.setNumberOfPoints(numberOfPoints);
    }

    private void writeRelationsToBuilder(final Atlas atlas,
            final ProtoAtlas.Builder protoAtlasBuilder)
    {
        long numberOfRelations = 0;

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
                protoRelationBuilder.addBeans(beanBuilder.build());
            }
            final Map<String, String> tags = relation.getTags();
            protoRelationBuilder
                    .addAllTags(ProtoAtlasBuilder.PROTOTAG_LIST_CONVERTER.backwardConvert(tags));

            numberOfRelations++;
            protoAtlasBuilder.addRelations(protoRelationBuilder.build());
        }
        protoAtlasBuilder.setNumberOfRelations(numberOfRelations);
    }
}
