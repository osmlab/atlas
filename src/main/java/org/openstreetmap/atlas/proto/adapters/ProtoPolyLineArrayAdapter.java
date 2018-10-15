package org.openstreetmap.atlas.proto.adapters;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.StringCompressedPolyLine;
import org.openstreetmap.atlas.proto.ProtoPolyLineArray;
import org.openstreetmap.atlas.proto.ProtoSerializable;
import org.openstreetmap.atlas.utilities.arrays.PolyLineArray;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;

/**
 * Implements the {@link ProtoAdapter} interface to connect {@link PolyLineArray} and
 * {@link ProtoPolyLineArray}.
 *
 * @author lcram
 */
public class ProtoPolyLineArrayAdapter implements ProtoAdapter
{
    @Override
    public ProtoSerializable deserialize(final byte[] byteArray)
    {
        ProtoPolyLineArray protoPolyLineArray = null;
        try
        {
            protoPolyLineArray = ProtoPolyLineArray.parseFrom(byteArray);
        }
        catch (final InvalidProtocolBufferException exception)
        {
            throw new CoreException("Error encountered while parsing protobuf bytestream",
                    exception);
        }
        final int encodingsCount = protoPolyLineArray.getEncodingsCount();
        final PolyLineArray polyLineArray = new PolyLineArray(encodingsCount, encodingsCount,
                encodingsCount);
        for (final ByteString encoding : protoPolyLineArray.getEncodingsList())
        {
            polyLineArray.add(new StringCompressedPolyLine(encoding.toByteArray()).asPolyLine());
        }

        if (protoPolyLineArray.hasName())
        {
            polyLineArray.setName(protoPolyLineArray.getName());
        }

        return polyLineArray;
    }

    @Override
    public byte[] serialize(final ProtoSerializable serializable)
    {
        if (!(serializable instanceof PolyLineArray))
        {
            throw new CoreException(
                    "Invalid ProtoSerializable type was provided to {}: cannot serialize {}",
                    this.getClass().getName(), serializable.getClass().getName());
        }
        final PolyLineArray polyLineArray = (PolyLineArray) serializable;

        if (polyLineArray.size() > Integer.MAX_VALUE)
        {
            throw new CoreException("Cannot serialize {}, size too large ({})",
                    polyLineArray.getClass().getName(), polyLineArray.size());
        }

        final ProtoPolyLineArray.Builder protoPolyLineArrayBuilder = ProtoPolyLineArray
                .newBuilder();
        for (final PolyLine polyLine : polyLineArray)
        {
            if (polyLine == null)
            {
                throw new CoreException("{} cannot serialize arrays with null elements",
                        this.getClass().getName());
            }
            protoPolyLineArrayBuilder.addEncodings(
                    ByteString.copyFrom(new StringCompressedPolyLine(polyLine).getEncoding()));
        }

        if (polyLineArray.getName() != null)
        {
            protoPolyLineArrayBuilder.setName(polyLineArray.getName());
        }

        return protoPolyLineArrayBuilder.build().toByteArray();
    }
}
