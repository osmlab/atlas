package org.openstreetmap.atlas.proto.adapters;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.Polygon;
import org.openstreetmap.atlas.geography.StringCompressedPolyLine;
import org.openstreetmap.atlas.proto.ProtoPolygonArray;
import org.openstreetmap.atlas.proto.ProtoSerializable;
import org.openstreetmap.atlas.utilities.arrays.PolygonArray;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;

/**
 * Implements the {@link ProtoAdapter} interface to connect {@link PolygonArray} and
 * {@link ProtoPolygonArray}.
 *
 * @author lcram
 */
public class ProtoPolygonArrayAdapter implements ProtoAdapter
{
    @Override
    public ProtoSerializable deserialize(final byte[] byteArray)
    {
        ProtoPolygonArray protoPolygonArray = null;
        try
        {
            protoPolygonArray = ProtoPolygonArray.parseFrom(byteArray);
        }
        catch (final InvalidProtocolBufferException exception)
        {
            throw new CoreException("Error encountered while parsing protobuf bytestream",
                    exception);
        }
        final int encodingsCount = protoPolygonArray.getEncodingsCount();
        final PolygonArray polygonArray = new PolygonArray(encodingsCount, encodingsCount,
                encodingsCount);
        for (final ByteString encoding : protoPolygonArray.getEncodingsList())
        {
            polygonArray.add(
                    new Polygon(new StringCompressedPolyLine(encoding.toByteArray()).asPolyLine()));
        }

        if (protoPolygonArray.hasName())
        {
            polygonArray.setName(protoPolygonArray.getName());
        }

        return polygonArray;
    }

    @Override
    public byte[] serialize(final ProtoSerializable serializable)
    {
        if (!(serializable instanceof PolygonArray))
        {
            throw new CoreException(
                    "Invalid ProtoSerializable type was provided to {}: cannot serialize {}",
                    this.getClass().getName(), serializable.getClass().getName());
        }
        final PolygonArray polygonArray = (PolygonArray) serializable;

        if (polygonArray.size() > Integer.MAX_VALUE)
        {
            throw new CoreException("Cannot serialize {}, size too large ({})",
                    polygonArray.getClass().getName(), polygonArray.size());
        }

        final ProtoPolygonArray.Builder protoPolygonArrayBuilder = ProtoPolygonArray.newBuilder();
        for (final Polygon polygon : polygonArray)
        {
            if (polygon == null)
            {
                throw new CoreException("{} cannot serialize arrays with null elements",
                        this.getClass().getName());
            }
            protoPolygonArrayBuilder.addEncodings(
                    ByteString.copyFrom(new StringCompressedPolyLine(polygon).getEncoding()));
        }

        if (polygonArray.getName() != null)
        {
            protoPolygonArrayBuilder.setName(polygonArray.getName());
        }

        return protoPolygonArrayBuilder.build().toByteArray();
    }
}
