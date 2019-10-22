package org.openstreetmap.atlas.geography.geojson.concrete;

import org.openstreetmap.atlas.geography.geojson.concrete.bbox.Bbox;
import org.openstreetmap.atlas.geography.geojson.concrete.type.Type;

import java.util.Map;

/**
 * @author Yazad Khambata
 */
public abstract class AbstractGeoJsonItem implements GeoJsonItem {
    private Bbox bbox;
    private Class<? extends Type> subTypeClass;

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
    public Map<String, Object> getForeigners() {
        //TODO:
        return null;
    }
}
