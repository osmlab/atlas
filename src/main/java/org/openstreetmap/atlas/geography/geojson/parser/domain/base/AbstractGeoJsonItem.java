package org.openstreetmap.atlas.geography.geojson.parser.domain.base;

import org.openstreetmap.atlas.geography.geojson.parser.domain.bbox.Bbox;
import org.openstreetmap.atlas.geography.geojson.parser.domain.base.type.Type;
import org.openstreetmap.atlas.geography.geojson.parser.domain.foreign.ForeignFields;

import java.util.Map;

/**
 * @author Yazad Khambata
 */
public abstract class AbstractGeoJsonItem implements GeoJsonItem {
    private Bbox bbox;
    private Class<? extends Type> subTypeClass;
    private ForeignFields foreignFields;

    public AbstractGeoJsonItem(final Class<? extends Type> subTypeClass) {
        this.subTypeClass = subTypeClass;
    }

    @Override
    public Type getType() {
        return Type.fromName(subTypeClass, this.getClass().getSimpleName());
    }

    @Override
    public Bbox getBbox() {
        return bbox;
    }

    @Override
    public ForeignFields getForeignFields() {
        return foreignFields;
    }

    @Override
    public String toString() {
        return "AbstractGeoJsonItem{" +
                "bbox=" + bbox +
                ", subTypeClass=" + subTypeClass +
                ", foreignFields=" + foreignFields +
                '}';
    }
}
