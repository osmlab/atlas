package org.openstreetmap.atlas.utilities.conversion;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * OSM tag conversion. Relies upon splitting upon the following exact string (within brackets here):
 * [", "].
 *
 * @author tony
 */
public class TagConverter implements StringConverter<Map<String, String>>
{
    private static final Logger logger = LoggerFactory.getLogger(TagConverter.class);

    @Override
    public Map<String, String> convert(final String tagsString)
    {
        // A typical string a = "name"=>"St. Thomas Street", "oneway"=>"yes", "highway"=>"secondary"
        final Map<String, String> tags = new HashMap<>();
        if (tagsString.length() > 1)
        {
            final String[] pairs = tagsString.substring(1, tagsString.length() - 1)
                    .split("\", \\\"");
            for (final String pair : pairs)
            {
                final String[] values = pair.split("=>");

                final String key = values[0].substring(0, values[0].length() - 1);
                final String value = values[1].substring(1, values[1].length());

                if (tags.containsKey(key))
                {
                    logger.warn("Duplicate tags: {}", tagsString);
                }
                else
                {
                    tags.put(key, value);
                }
            }
        }
        return tags;
    }
}
