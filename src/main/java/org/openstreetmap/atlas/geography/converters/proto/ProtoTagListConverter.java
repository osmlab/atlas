package org.openstreetmap.atlas.geography.converters.proto;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.proto.ProtoTag;
import org.openstreetmap.atlas.utilities.collections.Maps;
import org.openstreetmap.atlas.utilities.conversion.TwoWayConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Converts back and forth between a List of ProtoTags and an OSM tag Map.
 *
 * @author lcram
 */
public class ProtoTagListConverter implements TwoWayConverter<List<ProtoTag>, Map<String, String>>
{
    private static final Logger logger = LoggerFactory.getLogger(ProtoTagListConverter.class);

    @Override
    public List<ProtoTag> backwardConvert(final Map<String, String> osmTagMap)
    {
        final List<ProtoTag> protoTags = new ArrayList<>();
        for (final Map.Entry<String, String> entry : osmTagMap.entrySet())
        {
            final ProtoTag.Builder tagBuilder = ProtoTag.newBuilder();
            final String keyText;
            final String valueText;

            if (entry.getKey() == null)
            {
                logger.warn("Conversion from OSM tagmap found null key, skipping...");
                continue;
            }
            else
            {
                keyText = entry.getKey();
            }

            if (entry.getValue() == null)
            {
                logger.warn("Conversion from OSM tagmap found null value for key {}", keyText);
                valueText = "";
            }
            else
            {
                valueText = entry.getValue();
            }

            tagBuilder.setKey(keyText);
            tagBuilder.setValue(valueText);
            protoTags.add(tagBuilder.build());

        }
        return protoTags;
    }

    @Override
    public Map<String, String> convert(final List<ProtoTag> protoTagList)
    {
        try
        {
            final Map<String, String> result = Maps.hashMap();
            for (final ProtoTag tag : protoTagList)
            {
                result.put(tag.getKey(), tag.getValue());
            }
            return result;
        }
        catch (final Throwable error)
        {
            throw new CoreException("Unable to parse proto tags", error);
        }
    }

}
