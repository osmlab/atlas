package org.openstreetmap.atlas.geography.geojson;

import com.google.gson.JsonObject;

/**
 * From the spec https://tools.ietf.org/html/rfc7946#section-3.3
 * 
 * <pre>
 *     A GeoJSON object with the type "FeatureCollection" is a
 *    FeatureCollection object.  A FeatureCollection object has a member
 *    with the name "features".  The value of "features" is a JSON array.
 *    Each element of the array is a Feature object as defined above.  It
 *    is possible for this array to be empty.
 * </pre>
 *
 * @param <T>
 *            The Type of object that implements the {@link GeoJsonFeature} interface that is
 *            returned by this implementation
 * @author jklamer
 */
public interface GeoJsonFeatureCollection<T extends GeoJsonFeature>
        extends GeoJsonCollection<T>, GeoJsonProperties
{
    @Override
    default JsonObject asGeoJson()
    {
        return GeoJsonUtils.featureCollection(this);
    }

    @Override
    default GeoJsonType getGeoJsonType()
    {
        return GeoJsonType.FEATURE_COLLECTION;
    }
}
