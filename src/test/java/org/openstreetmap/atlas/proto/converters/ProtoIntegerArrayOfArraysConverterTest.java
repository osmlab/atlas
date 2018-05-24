package org.openstreetmap.atlas.proto.converters;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.proto.ProtoIntegerArray;
import org.openstreetmap.atlas.proto.ProtoIntegerArrayOfArrays;
import org.openstreetmap.atlas.utilities.arrays.IntegerArrayOfArrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author lcram
 */
public class ProtoIntegerArrayOfArraysConverterTest
{
    private static final Logger logger = LoggerFactory
            .getLogger(ProtoIntegerArrayOfArraysConverterTest.class);

    private static final int TEST_SIZE = 10;
    private static final int TEST_SUBARRAY_SIZE = 10;

    private static final String TEST_NAME = "test_name";

    @Test
    public void testArrayToProtoArray()
    {
        final ProtoIntegerArrayOfArraysConverter converter = new ProtoIntegerArrayOfArraysConverter();
        final IntegerArrayOfArrays array = new IntegerArrayOfArrays(TEST_SIZE);
        final ProtoIntegerArrayOfArrays.Builder builder = ProtoIntegerArrayOfArrays.newBuilder();

        for (int index = 0; index < TEST_SIZE; index++)
        {
            final int[] subarray = new int[TEST_SUBARRAY_SIZE];
            final ProtoIntegerArray.Builder subBuilder = ProtoIntegerArray.newBuilder();
            for (int subIndex = 0; subIndex < TEST_SUBARRAY_SIZE; subIndex++)
            {
                subarray[subIndex] = subIndex;
                subBuilder.addElements(subIndex);
            }
            builder.addArrays(subBuilder);
            array.add(subarray);
        }

        array.setName(TEST_NAME);
        builder.setName(TEST_NAME);

        final ProtoIntegerArrayOfArrays protoArray = builder.build();
        final ProtoIntegerArrayOfArrays convertedFrom = converter.backwardConvert(array);

        Assert.assertEquals(protoArray, convertedFrom);
    }

    @Test
    public void testProtoArrayToArray()
    {
        final ProtoIntegerArrayOfArraysConverter converter = new ProtoIntegerArrayOfArraysConverter();
        final IntegerArrayOfArrays array = new IntegerArrayOfArrays(TEST_SIZE);
        final ProtoIntegerArrayOfArrays.Builder builder = ProtoIntegerArrayOfArrays.newBuilder();

        for (int index = 0; index < TEST_SIZE; index++)
        {
            final int[] subarray = new int[TEST_SUBARRAY_SIZE];
            final ProtoIntegerArray.Builder subBuilder = ProtoIntegerArray.newBuilder();
            for (int subIndex = 0; subIndex < TEST_SUBARRAY_SIZE; subIndex++)
            {
                subarray[subIndex] = subIndex;
                subBuilder.addElements(subIndex);
            }
            builder.addArrays(subBuilder);
            array.add(subarray);
        }

        array.setName(TEST_NAME);
        builder.setName(TEST_NAME);

        final ProtoIntegerArrayOfArrays protoArray = builder.build();
        final IntegerArrayOfArrays convertedFrom = converter.convert(protoArray);

        Assert.assertEquals(array, convertedFrom);
    }
}
