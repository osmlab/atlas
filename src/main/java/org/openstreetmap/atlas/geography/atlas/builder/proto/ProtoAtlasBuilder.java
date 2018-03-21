package org.openstreetmap.atlas.geography.atlas.builder.proto;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.Latitude;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.Longitude;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.AtlasMetaData;
import org.openstreetmap.atlas.geography.atlas.builder.AtlasSize;
import org.openstreetmap.atlas.geography.atlas.items.Line;
import org.openstreetmap.atlas.geography.atlas.items.Point;
import org.openstreetmap.atlas.geography.atlas.packed.PackedAtlas;
import org.openstreetmap.atlas.geography.atlas.packed.PackedAtlasBuilder;
import org.openstreetmap.atlas.proto.ProtoAtlasContainer;
import org.openstreetmap.atlas.proto.ProtoLine;
import org.openstreetmap.atlas.proto.ProtoLocation;
import org.openstreetmap.atlas.proto.ProtoPoint;
import org.openstreetmap.atlas.proto.ProtoTag;
import org.openstreetmap.atlas.streaming.resource.Resource;
import org.openstreetmap.atlas.streaming.resource.WritableResource;
import org.openstreetmap.atlas.utilities.collections.Maps;
import org.openstreetmap.atlas.utilities.collections.StringList;
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

    private static final String TAG_DELIMITER = "=";

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
        // TODO do something smarter here?
        catch (final InvalidProtocolBufferException exception)
        {
            throw new CoreException("Error parsing the protobuf");
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
        final AtlasMetaData atlasMetaData = new AtlasMetaData(atlasSize, true, "unknown",
                "ProtoAtlas", "unknown", "unknown", Maps.hashMap());
        final PackedAtlasBuilder builder = new PackedAtlasBuilder().withSizeEstimates(atlasSize)
                .withMetaData(atlasMetaData).withName(resource.getName());

        // build the atlas features
        parsePoints(builder, protoAtlasContainer.getPointsList());
        parseLines(builder, protoAtlasContainer.getLinesList());

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

        final ProtoAtlasContainer protoAtlas = protoAtlasBuilder.build();
        resource.writeAndClose(protoAtlas.toByteArray());
    }

    private List<ProtoTag> buildProtoTagListFromTagMap(final Map<String, String> tags)
    {
        final List<ProtoTag> protoTags = new ArrayList<>();
        for (final Map.Entry<String, String> entry : tags.entrySet())
        {
            final ProtoTag.Builder tagBuilder = ProtoTag.newBuilder();

            // TODO what happens if entry.getValue() returns null?
            // this could happen if someone inserted a null entry at that key
            // maybe use entry.getValue() != null ? entry.getValue() : ""
            final String fullTag = entry.getKey() + TAG_DELIMITER + entry.getValue();
            tagBuilder.setTagText(fullTag);
            protoTags.add(tagBuilder.build());
        }
        return protoTags;
    }

    private ProtoLocation convertLocationToProtoLocation(final Location location)
    {
        final ProtoLocation.Builder protoLocationBuilder = ProtoLocation.newBuilder();
        protoLocationBuilder.setLatitude(Math.toIntExact(location.getLatitude().asDm7()));
        protoLocationBuilder.setLongitude(Math.toIntExact(location.getLongitude().asDm7()));
        return protoLocationBuilder.build();
    }

    private Location convertProtoLocationToLocation(final ProtoLocation protoLocation)
    {
        final Longitude longitude = Longitude.dm7(protoLocation.getLongitude());
        final Latitude latitude = Latitude.dm7(protoLocation.getLatitude());
        return new Location(latitude, longitude);
    }

    private void parseLines(final PackedAtlasBuilder builder, final List<ProtoLine> lines)
    {
        lines.forEach(line ->
        {
            final long identifier = line.getId();
            final List<Location> shapePoints = line.getShapePointsList().stream()
                    .map(this::convertProtoLocationToLocation).collect(Collectors.toList());
            final PolyLine geometry = new PolyLine(shapePoints);
            final Map<String, String> tags = parseTags(line.getTagsList());
            builder.addLine(identifier, geometry, tags);
        });
    }

    private void parsePoints(final PackedAtlasBuilder builder, final List<ProtoPoint> points)
    {
        points.forEach(point ->
        {
            final long identifier = point.getId();
            final Longitude longitude = Longitude.dm7(point.getLocation().getLongitude());
            final Latitude latitude = Latitude.dm7(point.getLocation().getLatitude());
            final Location geometry = new Location(latitude, longitude);
            final Map<String, String> tags = parseTags(point.getTagsList());
            builder.addPoint(identifier, geometry, tags);
        });
    }

    private Map<String, String> parseTags(final List<ProtoTag> tagsList)
    {
        try
        {
            final Map<String, String> result = Maps.hashMap();
            for (final ProtoTag tag : tagsList)
            {
                final StringList tagSplit = StringList.split(tag.getTagText(), TAG_DELIMITER);
                if (tagSplit.size() == 2)
                {
                    result.put(tagSplit.get(0), tagSplit.get(1));
                }
                if (tagSplit.size() == 1)
                {
                    result.put(tagSplit.get(0), "");
                }
            }
            return result;
        }
        // TODO do something smarter here?
        catch (final Throwable error)
        {
            throw new CoreException("Unable to parse tags.");
        }
    }

    private void writeLinesToBuilder(final Atlas atlas,
            final ProtoAtlasContainer.Builder protoAtlasBuilder)
    {
        long numberOfLines = 0;

        for (final Line line : atlas.lines())
        {
            final ProtoLine.Builder protoLineBuilder = ProtoLine.newBuilder();
            protoLineBuilder.setId(line.getIdentifier());

            final List<ProtoLocation> protoLocations = StreamSupport
                    .stream(line.getRawGeometry().spliterator(), false)
                    .map(this::convertLocationToProtoLocation).collect(Collectors.toList());
            protoLineBuilder.addAllShapePoints(protoLocations);

            final Map<String, String> tags = line.getTags();
            protoLineBuilder.addAllTags(buildProtoTagListFromTagMap(tags));

            numberOfLines++;
            protoAtlasBuilder.addLines(protoLineBuilder.build());
        }
        protoAtlasBuilder.setNumberOfLines(numberOfLines);
    }

    private void writePointsToBuilder(final Atlas atlas,
            final ProtoAtlasContainer.Builder protoAtlasBuilder)
    {
        long numberOfPoints = 0;

        for (final Point point : atlas.points())
        {
            final ProtoPoint.Builder protoPointBuilder = ProtoPoint.newBuilder();

            protoPointBuilder.setId(point.getIdentifier());
            protoPointBuilder.setLocation(convertLocationToProtoLocation(point.getLocation()));

            final Map<String, String> tags = point.getTags();
            protoPointBuilder.addAllTags(buildProtoTagListFromTagMap(tags));

            numberOfPoints++;
            protoAtlasBuilder.addPoints(protoPointBuilder.build());
        }
        protoAtlasBuilder.setNumberOfPoints(numberOfPoints);
    }
}
