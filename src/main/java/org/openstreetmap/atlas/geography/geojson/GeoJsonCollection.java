package org.openstreetmap.atlas.geography.geojson;

/**
 * Interface for all GeoJson collection types {@link GeojsonGeometryCollection} and
 * {@link GeoJsonFeatureCollection}
 * 
 * @param <Type>
 *            The type of GeoJsonGeometry in the collection
 * @author jklamer
 */
public interface GeoJsonCollection<Type extends GeoJsonGeometry> extends GeoJson
{
    Iterable<Type> getObjects();
}
