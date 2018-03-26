package org.openstreetmap.atlas.geography.converters.proto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.proto.ProtoTag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author lcram
 */
public class ProtoTagListConverterTest
{
    private static final Logger logger = LoggerFactory.getLogger(ProtoTagListConverterTest.class);

    @Test
    public void testEmptyConversion()
    {
        final ProtoTagListConverter converter = new ProtoTagListConverter();
        final Map<String, String> osmTagMap = new HashMap<>();
        final List<ProtoTag> protoTagList = new ArrayList<>();

        final List<ProtoTag> listFromMap = converter.backwardConvert(osmTagMap);
        Assert.assertEquals(protoTagList, listFromMap);

        final Map<String, String> mapFromList = converter.convert(protoTagList);
        Assert.assertEquals(osmTagMap, mapFromList);
    }

    @Test
    public void testOSMToProtoTagList()
    {
        final ProtoTagListConverter converter = new ProtoTagListConverter();
        final Map<String, String> osmTagMap = new HashMap<>();
        osmTagMap.put("key1", "value1");
        osmTagMap.put("key2", "value2");
        final List<ProtoTag> protoTagList = new ArrayList<>();
        protoTagList.add(ProtoTag.newBuilder().setKey("key1").setValue("value1").build());
        protoTagList.add(ProtoTag.newBuilder().setKey("key2").setValue("value2").build());

        final List<ProtoTag> listFromMap = converter.backwardConvert(osmTagMap);
        Assert.assertEquals(protoTagList, listFromMap);
    }

    @Test
    public void testProtoTagListToOSM()
    {
        final ProtoTagListConverter converter = new ProtoTagListConverter();
        final List<ProtoTag> protoTagList = new ArrayList<>();
        protoTagList.add(ProtoTag.newBuilder().setKey("key1").setValue("value1").build());
        protoTagList.add(ProtoTag.newBuilder().setKey("key2").setValue("value2").build());
        final Map<String, String> osmTagMap = new HashMap<>();
        osmTagMap.put("key1", "value1");
        osmTagMap.put("key2", "value2");

        final Map<String, String> mapFromList = converter.convert(protoTagList);
        Assert.assertEquals(osmTagMap, mapFromList);
    }
}
