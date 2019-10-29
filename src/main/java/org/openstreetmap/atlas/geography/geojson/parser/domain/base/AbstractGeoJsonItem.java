package org.openstreetmap.atlas.geography.geojson.parser.domain.base;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.tuple.Pair;
import org.openstreetmap.atlas.geography.geojson.parser.domain.bbox.Bbox;
import org.openstreetmap.atlas.geography.geojson.parser.domain.bbox.Dimensions;
import org.openstreetmap.atlas.geography.geojson.parser.domain.foreign.ForeignFields;
import org.openstreetmap.atlas.geography.geojson.parser.domain.properties.Properties;

/**
 * @author Yazad Khambata
 */
public abstract class AbstractGeoJsonItem implements GeoJsonItem
{
    private Bbox bbox;
    private Properties properties;
    private ForeignFields foreignFields;

    public static Object extractBbox(final Map<String, Object> map)
    {
        final List<Double> list = (List<Double>) map.get("bbox");

        if (CollectionUtils.isEmpty(list))
        {
            return null;
        }

        final Object rawBbox = list.toArray(new Double[list.size()]);
        return rawBbox;
    }

    public static Map<String, Object> extractPropertiesMap(final Map<String, Object> map)
    {
        final Map<String, Object> properties = (Map<String, Object>) map.get("properties");

        return properties;
    }

    protected static Map<String, Object> extractForeignFields(final Map<String, Object> map,
            final HashSet<String> exclude)
    {
        return new HashMap<>(map).entrySet().stream()
                .map(entry -> Pair.of(entry.getKey(), entry.getValue()))
                .filter(pair -> !exclude.contains(pair.getKey()))
                .collect(Collectors.toMap(Pair::getKey, Pair::getValue));
    }

    private static Bbox toBbox(final Map<String, Object> map)
    {
        final Double[] coordinates = (Double[]) extractBbox(map);

        if (coordinates == null)
        {
            return null;
        }

        return Dimensions.toBbox(coordinates);
    }

    public AbstractGeoJsonItem(final Bbox bbox, final Properties properties,
            final ForeignFields foreignFields)
    {
        this.bbox = bbox;
        this.properties = properties;
        this.foreignFields = foreignFields;
    }

    public AbstractGeoJsonItem(final Map<String, Object> map, final ForeignFields foreignFields)
    {
        this(toBbox(map), new Properties(extractPropertiesMap(map)), foreignFields);
        Validate.notEmpty(map, "input map is empty.");
    }

    @Override
    public boolean equals(final Object that)
    {
        return EqualsBuilder.reflectionEquals(this, that);
    }

    @Override
    public Bbox getBbox()
    {
        return this.bbox;
    }

    @Override
    public ForeignFields getForeignFields()
    {
        return this.foreignFields;
    }

    @Override
    public Properties getProperties()
    {
        return this.properties;
    }

    @Override
    public int hashCode()
    {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this);
    }
}
