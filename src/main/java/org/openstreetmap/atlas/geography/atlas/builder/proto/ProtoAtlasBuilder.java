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
import org.openstreetmap.atlas.geography.atlas.items.Area;
import org.openstreetmap.atlas.geography.atlas.items.Line;
import org.openstreetmap.atlas.geography.atlas.items.Point;
import org.openstreetmap.atlas.geography.atlas.packed.PackedAtlas;
import org.openstreetmap.atlas.geography.atlas.packed.PackedAtlasBuilder;
import org.openstreetmap.atlas.proto.ProtoArea;
import org.openstreetmap.atlas.proto.ProtoAtlasContainer;
import org.openstreetmap.atlas.proto.ProtoAtlasContainer.Builder;
import org.openstreetmap.atlas.proto.ProtoLine;
import org.openstreetmap.atlas.proto.ProtoLocation;
import org.openstreetmap.atlas.proto.ProtoPoint;
import org.openstreetmap.atlas.streaming.resource.Resource;
import org.openstreetmap.atlas.streaming.resource.WritableResource;
import org.openstreetmap.atlas.utilities.collections.Maps;
import org.openstreetmap.atlas.utilities.conversion.ProtoLocationConverter;
import org.openstreetmap.atlas.utilities.conversion.ProtoTagListConverter;
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
            throw new CoreException("Error deserializing the ProtoAtlasContainer", exception);
        }

        // initialize atlas metadata
        final long numberOfPoints = protoAtlasContainer.getNumberOfPoints();
        final long numberOfLines = protoAtlasContainer.getNumberOfLines();
        final long numberOfAreas = 0;
        final long numberOfNodes = 0;
        final long numberOfEdges = 0;
        final long numberOfRelations = 0;
        final AtlasSize atlasSize = new AtlasSize(numberOfEdges, numberOfNodes, numberOfAreas,
                numberOfLines, numberOfPoints, numberOfRelations);
        // TODO it would be nice to programmatically decide which version of protobuf is being used
        // and store that with the atlas data
        final AtlasMetaData atlasMetaData = new AtlasMetaData(atlasSize, true, "unknown",
                "ProtoAtlas", "unknown", "unknown", Maps.hashMap());
        final PackedAtlasBuilder builder = new PackedAtlasBuilder().withSizeEstimates(atlasSize)
                .withMetaData(atlasMetaData).withName(resource.getName());

        // build the atlas features
        parsePoints(builder, protoAtlasContainer.getPointsList());
        parseLines(builder, protoAtlasContainer.getLinesList());
        parseAreas(builder, protoAtlasContainer.getAreasList());

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

        writePointsToBuilder(atlas, protoAtlasBuilder);
        writeLinesToBuilder(atlas, protoAtlasBuilder);
        writeAreasToBuilder(atlas, protoAtlasBuilder);

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
            // polygons, make sure this is not happening
            builder.addArea(identifier, geometry, tags);
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

    private void parsePoints(final PackedAtlasBuilder builder, final List<ProtoPoint> points)
    {
        final ProtoTagListConverter converter = new ProtoTagListConverter();
        points.forEach(protoPoint ->
        {
            final long identifier = protoPoint.getId();
            final Longitude longitude = Longitude.dm7(protoPoint.getLocation().getLongitude());
            final Latitude latitude = Latitude.dm7(protoPoint.getLocation().getLatitude());
            final Location geometry = new Location(latitude, longitude);
            final Map<String, String> tags = converter.convert(protoPoint.getTagsList());
            builder.addPoint(identifier, geometry, tags);
        });
    }

    private void writeAreasToBuilder(final Atlas atlas, final Builder protoAtlasBuilder)
    {
        long numberOfLines = 0;
        final ProtoLocationConverter protoLocationConverter = new ProtoLocationConverter();
        final ProtoTagListConverter protoTagListConverter = new ProtoTagListConverter();

        for (final Area area : atlas.areas())
        {
            final ProtoLine.Builder protoLineBuilder = ProtoLine.newBuilder();
            protoLineBuilder.setId(area.getIdentifier());

            final List<ProtoLocation> protoLocations = area.asPolygon().stream()
                    .map(protoLocationConverter::backwardConvert).collect(Collectors.toList());
            protoLineBuilder.addAllShapePoints(protoLocations);

            final Map<String, String> tags = area.getTags();
            protoLineBuilder.addAllTags(protoTagListConverter.backwardConvert(tags));

            numberOfLines++;
            protoAtlasBuilder.addLines(protoLineBuilder.build());
        }
        protoAtlasBuilder.setNumberOfLines(numberOfLines);
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
}
