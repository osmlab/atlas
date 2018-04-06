package org.openstreetmap.atlas.proto.adapters;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.proto.ProtoIntegerArray;
import org.openstreetmap.atlas.proto.ProtoIntegerArrayOfArrays;
import org.openstreetmap.atlas.proto.ProtoSerializable;
import org.openstreetmap.atlas.utilities.arrays.IntegerArrayOfArrays;

import com.google.common.primitives.Ints;
import com.google.protobuf.InvalidProtocolBufferException;

/**
 * Implements the {@link ProtoAdapter} interface to connect {@link IntegerArrayOfArrays} and
 * {@link ProtoIntegerArrayOfArrays}.
 *
 * @author lcram
 */
public class ProtoIntegerArrayOfArraysAdapter implements ProtoAdapter
{
    @Override
    public ProtoSerializable deserialize(final byte[] byteArray)
    {
        ProtoIntegerArrayOfArrays protoIntegerArrayOfArrays = null;
        try
        {
            protoIntegerArrayOfArrays = ProtoIntegerArrayOfArrays.parseFrom(byteArray);
        }
        catch (final InvalidProtocolBufferException exception)
        {
            throw new CoreException("Error encountered while parsing protobuf bytestream",
                    exception);
        }

        final IntegerArrayOfArrays integerArrayOfArrays = new IntegerArrayOfArrays(
                protoIntegerArrayOfArrays.getArraysCount());
        integerArrayOfArrays.setName(protoIntegerArrayOfArrays.getName());
        for (int index = 0; index < protoIntegerArrayOfArrays.getArraysCount(); index++)
        {
            final int[] items = Ints
                    .toArray(protoIntegerArrayOfArrays.getArrays(index).getElementsList());
            integerArrayOfArrays.add(items);
        }

        return integerArrayOfArrays;
    }

    @Override
    public byte[] serialize(final ProtoSerializable serializable)
    {
        if (!(serializable instanceof IntegerArrayOfArrays))
        {
            throw new CoreException(
                    "Invalid ProtoSerializable type was provided to {}: cannot serialize {}",
                    this.getClass().getName(), serializable.getClass().getName());
        }
        final IntegerArrayOfArrays integerArrayOfArrays = (IntegerArrayOfArrays) serializable;

        if (integerArrayOfArrays.size() > Integer.MAX_VALUE)
        {
            // TODO see note about this check in ProtoLongArrayOfArraysAdapter
            throw new CoreException("Cannot serialize provided {}, size {} too large",
                    serializable.getClass().getName(), integerArrayOfArrays.size());
        }

        final ProtoIntegerArrayOfArrays.Builder arraysBuilder = ProtoIntegerArrayOfArrays
                .newBuilder();
        for (int index = 0; index < integerArrayOfArrays.size(); index++)
        {
            final ProtoIntegerArray.Builder subArrayBuilder = ProtoIntegerArray.newBuilder();
            final int[] subArray = integerArrayOfArrays.get(index);
            for (int subIndex = 0; subIndex < subArray.length; subIndex++)
            {
                subArrayBuilder.addElements(subArray[subIndex]);
            }
            arraysBuilder.addArrays(subArrayBuilder);
        }
        final String name = integerArrayOfArrays.getName() == null ? ""
                : integerArrayOfArrays.getName();
        arraysBuilder.setName(name);

        final ProtoIntegerArrayOfArrays protoArrays = arraysBuilder.build();
        return protoArrays.toByteArray();
    }
}
