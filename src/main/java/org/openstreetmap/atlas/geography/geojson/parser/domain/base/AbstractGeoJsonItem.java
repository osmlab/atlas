package org.openstreetmap.atlas.geography.geojson.parser.domain.base;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.openstreetmap.atlas.geography.geojson.parser.domain.bbox.Bbox;
import org.openstreetmap.atlas.geography.geojson.parser.domain.base.type.Type;
import org.openstreetmap.atlas.geography.geojson.parser.domain.bbox.Dimensions;
import org.openstreetmap.atlas.geography.geojson.parser.domain.foreign.ForeignFields;

import java.util.List;
import java.util.Map;

/**
 * @author Yazad Khambata
 */
public abstract class AbstractGeoJsonItem implements GeoJsonItem {
    private Bbox bbox;
    private ForeignFields foreignFields;

    public AbstractGeoJsonItem(final Bbox bbox,
                               final ForeignFields foreignFields) {
        this.bbox = bbox;
        this.foreignFields = foreignFields;
    }

    public AbstractGeoJsonItem(final Map<String, Object> map) {
        this(toBbox(map), extractForeignFields(map));
        Validate.notEmpty(map, "input map is empty.");
    }

    private static Bbox toBbox(final Map<String, Object> map) {
        final Double[] coordinates = (Double[]) extractBbox(map);

        if (coordinates == null) {
            return null;
        }

        return Dimensions.toBbox(coordinates);
    }

    @Override
    public Bbox getBbox() {
        return bbox;
    }

    @Override
    public ForeignFields getForeignFields() {
        return foreignFields;
    }

    public static Object extractBbox(final Map<String, Object> map) {
        final List<Double> list = (List<Double>) map.get("bbox");

        if (CollectionUtils.isEmpty(list)) {
            return null;
        }

        final Object rawBbox = list.toArray(new Double[list.size()]);
        return rawBbox;
    }

    public static ForeignFields extractForeignFields(final Map<String, Object> map) {
        //TODO:
        return null;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public boolean equals(final Object that) {
        return EqualsBuilder.reflectionEquals(this, that);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }
}
