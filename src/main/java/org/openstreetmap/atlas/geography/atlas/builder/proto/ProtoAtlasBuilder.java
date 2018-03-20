package org.openstreetmap.atlas.geography.atlas.builder.proto;

import java.util.Map;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.items.Point;
import org.openstreetmap.atlas.geography.atlas.packed.PackedAtlas;
import org.openstreetmap.atlas.proto.ProtoAtlasContainer;
import org.openstreetmap.atlas.proto.ProtoPoint;
import org.openstreetmap.atlas.proto.ProtoTag;
import org.openstreetmap.atlas.streaming.resource.Resource;
import org.openstreetmap.atlas.streaming.resource.WritableResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Build an Atlas from a naive proto serialized file, or write an Atlas to a naive proto serialized
 * file.
 *
 * @author lcram
 */
public class ProtoAtlasBuilder
{
    private static final Logger logger = LoggerFactory.getLogger(ProtoAtlasBuilder.class);

    private static final String TAG_DELIMITER = "=";

    public PackedAtlas read(final Resource resource)
    {
        final long numberOfNodes = 0L;
        final long numberOfEdges = 0L;
        final long numberOfAreas = 0L;
        final long numberOfLines = 0L;
        final long numberOfPoints = 0L;
        final long numberOfRelations = 0L;

        return null;
    }

    public void write(final Atlas atlas, final WritableResource resource)
    {
        final ProtoAtlasContainer.Builder protoAtlasBuilder = ProtoAtlasContainer.newBuilder();
        for (final Point point : atlas.points())
        {
            logger.debug("PROCESSING POINT:\n" + point.toString());
            final ProtoPoint.Builder protoPointBuilder = ProtoPoint.newBuilder();
            protoPointBuilder.setId(point.getIdentifier());

            // TODO We have to cast to an int here, since asDm7() returns a long.
            // To do this safely, we use Math.toIntExact, which throws an ArithmeticException
            // when the long is out of range. Here I have caught it and rethrown a CoreException.
            // Should I just leave it to throw the ArithmeticException?
            try
            {
                protoPointBuilder
                        .setLatitude(Math.toIntExact(point.getLocation().getLatitude().asDm7()));
                protoPointBuilder
                        .setLongitude(Math.toIntExact(point.getLocation().getLongitude().asDm7()));
            }
            catch (final ArithmeticException exception)
            {
                throw new CoreException(
                        "Point " + point.toString() + " had latlong out of int32 range.");
            }

            final Map<String, String> tags = point.getTags();
            logger.debug("tag set");
            for (final Map.Entry<String, String> entry : tags.entrySet())
            {
                final ProtoTag.Builder tagBuilder = ProtoTag.newBuilder();
                final String fullTag = entry.getKey() + TAG_DELIMITER + entry.getValue();
                logger.debug(fullTag);
                tagBuilder.setTagText(fullTag);
                protoPointBuilder.addTags(tagBuilder.build());
            }

            protoAtlasBuilder.addPoints(protoPointBuilder.build());

            // TODO how do you write a resource to a file?
        }
    }
}
