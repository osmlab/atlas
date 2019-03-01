package org.openstreetmap.atlas.geography.geojson;

/**
 * Interface for all GeoJson collection types {@link GeojsonGeometryCollection} and
 * {@link GeoJsonFeatureCollection}
 * 
 * @param <T>
 *            The type of GeoJsonGeometry in the collection
 * @author jklamer
 */
public interface GeoJsonCollection<T extends GeoJsonGeometry> extends GeoJson
{
    Iterable<T> getGeoJsonObjects();
}
