package org.openstreetmap.atlas.utilities.conversion;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.proto.ProtoTag;
import org.openstreetmap.atlas.utilities.collections.Maps;
import org.openstreetmap.atlas.utilities.collections.StringList;
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

    private static final String TAG_DELIMITER = "=";

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
                logger.warn("buildProtoTagListFromTagMap found null key");
                keyText = "null";
            }
            else
            {
                keyText = entry.getKey();
            }

            if (entry.getValue() == null)
            {
                logger.warn("buildProtoTagListFromTagMap found null value");
                valueText = "null";
            }
            else
            {
                valueText = entry.getValue();
            }

            final String fullTag = keyText + TAG_DELIMITER + valueText;
            tagBuilder.setTagText(fullTag);
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
        catch (final Throwable error)
        {
            throw new CoreException("Unable to parse proto tags", error);
        }
    }

}
