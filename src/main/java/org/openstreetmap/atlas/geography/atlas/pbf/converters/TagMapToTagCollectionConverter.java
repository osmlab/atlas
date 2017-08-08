package org.openstreetmap.atlas.geography.atlas.pbf.converters;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.openstreetmap.atlas.utilities.conversion.Converter;
import org.openstreetmap.osmosis.core.domain.v0_6.Tag;

/**
 * @author matthieun
 */
public class TagMapToTagCollectionConverter
        implements Converter<Map<String, String>, Collection<Tag>>
{
    @Override
    public Collection<Tag> convert(final Map<String, String> object)
    {
        final List<Tag> result = new ArrayList<>();
        object.forEach((key, value) -> result.add(new Tag(key, value)));
        return result;
    }
}
