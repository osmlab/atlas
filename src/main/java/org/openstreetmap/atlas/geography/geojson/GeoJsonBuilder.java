package org.openstreetmap.atlas.geography.geojson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.Polygon;
import org.openstreetmap.atlas.utilities.collections.Iterables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

/**
 * @author matthieun
 * @author cuthbertm
 * @author mgostintsev
 * @author rmegraw
 */
public class GeoJsonBuilder
{
    /**
     * @author matthieun
     * @author mgostintsev
     */
    public enum GeoJsonType
    {
        POINT("Point"),
        LINESTRING("LineString"),
        POLYGON("Polygon"),
        MULTI_POINT("MultiPoint"),
        MULTI_LINESTRING("MultiLineString"),
        MULTI_POLYGON("MultiPolygon");

        private final String type;

        public static GeoJsonType forType(final String type)
        {
            for (final GeoJsonType value : values())
            {
                if (value.getType().equals(type))
                {
                    return value;
                }
            }
            throw new CoreException("Invalid geoJson type: {}", type);
        }

        GeoJsonType(final String type)
        {
            this.type = type;
        }

        public String getType()
        {
            return this.type;
        }
    }

    /**
     * Java bean to store geometry (as an {@link Iterable} of {@link Location}s) and properties as a
     * {@link String} to {@link Object} {@link Map}
     *
     * @author matthieun
     * @author rmegraw
     */
    public static class GeometryWithProperties
    {
        private final Iterable<Location> geometry;
        private final Map<String, Object> properties;

        public GeometryWithProperties(final Iterable<Location> geometry,
                final Map<String, Object> properties)
        {
            this.geometry = geometry;
            this.properties = properties;
        }

        public Iterable<Location> getGeometry()
        {
            return this.geometry;
        }

        public Map<String, Object> getProperties()
        {
            return this.properties;
        }
    }

    /**
     * Java bean to store the geometry (as an {@link Iterable} of {@link Location}s) and all the
     * tags as a {@link String} to {@link String} {@link Map}
     *
     * @deprecated instead use {@link GeometryWithProperties}
     * @author matthieun
     */
    @Deprecated
    public static class LocationIterableProperties
    {
        private final Iterable<Location> locations;
        private final Map<String, String> properties;

        public LocationIterableProperties(final Iterable<Location> locations,
                final Map<String, String> properties)
        {
            this.locations = locations;
            this.properties = properties;
        }

        public Iterable<Location> getLocations()
        {
            return this.locations;
        }

        public Map<String, String> getProperties()
        {
            return this.properties;
        }
    }

    public static final String COORDINATES = "coordinates";
    public static final String FEATURE = "Feature";
    public static final String FEATURES = "features";
    public static final String FEATURE_COLLECTION = "FeatureCollection";
    public static final String GEOMETRIES = "geometries";
    public static final String GEOMETRY = "geometry";
    public static final String GEOMETRY_COLLECTION = "GeometryCollection";
    public static final String PROPERTIES = "properties";
    public static final String TYPE = "type";
    private static final Logger logger = LoggerFactory.getLogger(GeoJsonBuilder.class);
    private final int logFrequency;

    /**
     * Converts iterable of deprecated LocationIterableProperties to iterable of
     * GeometryWithProperties.
     *
     * @param objects
     *            iterable of LocationIterableProperties
     * @return iterable of GeometryWithProperties
     */
    protected static final Iterable<GeometryWithProperties> toGeometriesWithProperties(
            final Iterable<LocationIterableProperties> objects)
    {
        final Iterable<GeometryWithProperties> geometriesWithProperties = Iterables
                .translate(objects, locationIterableProperties ->
                {
                    return toGeometryWithProperties(locationIterableProperties);
                });
        return geometriesWithProperties;
    }

    /**
     * Converts deprecated LocationIterableProperties to GeometryWithProperties.
     *
     * @param locationIterableProperties
     *            LocationIterableProperties object
     * @return GeometryWithProperties object
     */
    protected static final GeometryWithProperties toGeometryWithProperties(
            final LocationIterableProperties locationIterableProperties)
    {
        final Map<String, Object> propertiesObjects = new HashMap<>();
        propertiesObjects.putAll(locationIterableProperties.getProperties());
        return new GeometryWithProperties(locationIterableProperties.getLocations(),
                propertiesObjects);
    }

    public GeoJsonBuilder()
    {
        this.logFrequency = -1;
    }

    public GeoJsonBuilder(final int logFrequency)
    {
        this.logFrequency = logFrequency;
    }

    public GeoJsonObject create(final GeoJsonType type, final Location... locations)
    {
        return this.create(Iterables.iterable(locations), type);
    }

    /**
     * Creates a Json Feature from a {@link GeometryWithProperties}
     *
     * @param geometryWithProperties
     *            {@link GeometryWithProperties}
     * @return a GeoJson Feature
     */
    public JsonObject create(final GeometryWithProperties geometryWithProperties)
    {
        final Iterable<Location> geometry = geometryWithProperties.getGeometry();
        final Map<String, Object> properties = geometryWithProperties.getProperties();

        if (geometry instanceof Location)
        {
            return create((Location) geometry).withNewProperties(properties).jsonObject();
        }
        else if (geometry instanceof Polygon)
        {
            return create((Polygon) geometry).withNewProperties(properties).jsonObject();
        }
        else if (geometry instanceof PolyLine)
        {
            return create((PolyLine) geometry).withNewProperties(properties).jsonObject();
        }
        else
        {
            throw new CoreException("Unrecognized object type {}",
                    geometry.getClass().getSimpleName());
        }
    }

    /**
     * Creates a GeoJson Feature containing a Geometry
     *
     * @param locations
     *            geometry coordinates
     * @param type
     *            geometry type
     * @return a GeoJson Feature
     */
    public GeoJsonObject create(final Iterable<Location> locations, final GeoJsonType type)
    {
        final JsonObject result = new JsonObject();
        result.addProperty(TYPE, FEATURE);
        final JsonArray coordinates = new JsonArray();
        switch (type)
        {
            case POINT:
            {
                final Location location = locations.iterator().next();
                coordinates.add(new JsonPrimitive(location.getLongitude().asDegrees()));
                coordinates.add(new JsonPrimitive(location.getLatitude().asDegrees()));
                break;
            }
            case LINESTRING:
            case MULTI_POINT:
            case MULTI_LINESTRING:
            case MULTI_POLYGON:
            {
                for (final Location location : locations)
                {
                    final JsonArray locationArray = new JsonArray();
                    locationArray.add(new JsonPrimitive(location.getLongitude().asDegrees()));
                    locationArray.add(new JsonPrimitive(location.getLatitude().asDegrees()));
                    coordinates.add(locationArray);
                }
                break;
            }
            case POLYGON:
            {
                final JsonArray locationArray = new JsonArray();
                for (final Location location : locations)
                {
                    final JsonArray locationArray2 = new JsonArray();
                    locationArray2.add(new JsonPrimitive(location.getLongitude().asDegrees()));
                    locationArray2.add(new JsonPrimitive(location.getLatitude().asDegrees()));
                    locationArray.add(locationArray2);
                }
                coordinates.add(locationArray);
                break;
            }
            default:
                throw new CoreException("Unrecognized object type {}", type);
        }

        final JsonObject geometry = new JsonObject();
        geometry.addProperty(TYPE, type.getType());
        geometry.add(COORDINATES, coordinates);
        result.add(GEOMETRY, geometry);
        return new GeoJsonObject(result);
    }

    /**
     * Creates a GeoJson FeatureCollection containing a list of Features
     *
     * @param objects
     *            used to build each Feature
     * @return a GeoJson FeatureCollection
     * @deprecated use {@link #createFromGeometriesWithProperties(Iterable)} instead
     */
    @Deprecated
    public GeoJsonObject create(final Iterable<LocationIterableProperties> objects)
    {
        return createFromGeometriesWithProperties(toGeometriesWithProperties(objects));
    }

    /**
     * Creates a Point type GeoJson Feature
     *
     * @param location
     *            geometry
     * @return a Feature
     */
    public GeoJsonObject create(final Location location)
    {
        return this.create(location, GeoJsonType.POINT);
    }

    /**
     * Creates a Json Feature from a {@link LocationIterableProperties}
     *
     * @deprecated use {@link #create(GeometryWithProperties)} instead
     * @param object
     *            {@link LocationIterableProperties}
     * @return a GeoJson Feature
     */
    @Deprecated
    public JsonObject create(final LocationIterableProperties object)
    {
        final GeometryWithProperties geometryWithProperties = toGeometryWithProperties(object);
        return create(geometryWithProperties);
    }

    /**
     * Creates a Polygon type GeoJson Feature
     *
     * @param polygon
     *            geometry
     * @return a GeoJson Feature
     */
    public GeoJsonObject create(final Polygon polygon)
    {
        return this.create(polygon.closedLoop(), GeoJsonType.POLYGON);
    }

    /**
     * Creates a LineString type GeoJson Feature
     *
     * @param polyLine
     *            geometry
     * @return a GeoJson Feature
     */
    public GeoJsonObject create(final PolyLine polyLine)
    {
        return this.create(polyLine, GeoJsonType.LINESTRING);
    }

    /**
     * Creates a GeoJson FeatureCollection containing a list of GeoJsonObject Features
     *
     * @param objects
     *            the features
     * @return a GeoJson FeatureCollection
     */
    public GeoJsonObject createFeatureCollection(final Iterable<GeoJsonObject> objects)
    {
        final JsonObject result = new JsonObject();
        result.addProperty(TYPE, FEATURE_COLLECTION);
        final JsonArray features = new JsonArray();
        int counter = 0;
        for (final GeoJsonObject object : objects)
        {
            if (this.logFrequency > 0 && ++counter % this.logFrequency == 0)
            {
                logger.info("Processed {} features.", counter);
            }
            if (!Optional.ofNullable(object.jsonObject().get(TYPE))
                    .filter(jsonObject -> jsonObject.getAsString().equals(FEATURE)).isPresent())
            {
                throw new CoreException("Illegal GeoJson Type for Feature collection");
            }
            features.add(object.jsonObject());
        }
        result.add(FEATURES, features);
        return new GeoJsonObject(result);
    }

    /**
     * Creates a GeoJson FeatureCollection from an iterable of GeoJsonObject
     *
     * @param geoJsonObjects
     *            a iterable of GeoJsonObject
     * @return a GeoJson FeatureCollection
     */
    public GeoJsonObject createFromGeoJson(final Iterable<GeoJsonObject> geoJsonObjects)
    {
        final JsonObject result = new JsonObject();
        result.addProperty(TYPE, FEATURE_COLLECTION);
        final JsonArray features = new JsonArray();
        int counter = 0;
        for (final GeoJsonObject object : geoJsonObjects)
        {
            if (this.logFrequency > 0 && ++counter % this.logFrequency == 0)
            {
                logger.info("Processed {} features.", counter);
            }
            features.add(object.jsonObject());
        }
        result.add(FEATURES, features);
        return new GeoJsonObject(result);
    }

    /**
     * Creates a GeoJson FeatureCollection containing a list of Features
     *
     * @param geometriesWithProperties
     *            associated geometries and properties used to build each Feature
     * @return a GeoJson FeatureCollection
     */
    public GeoJsonObject createFromGeometriesWithProperties(
            final Iterable<GeometryWithProperties> geometriesWithProperties)
    {
        final JsonObject result = new JsonObject();
        result.addProperty(TYPE, FEATURE_COLLECTION);
        final JsonArray features = new JsonArray();
        int counter = 0;
        for (final GeometryWithProperties geometryWithProperties : geometriesWithProperties)
        {
            if (this.logFrequency > 0 && ++counter % this.logFrequency == 0)
            {
                logger.info("Processed {} features.", counter);
            }
            features.add(create(geometryWithProperties));
        }
        result.add(FEATURES, features);
        return new GeoJsonObject(result);
    }

    /**
     * Creates a GeometryCollection type Feature containing geometries derived from a collection of
     * {@link LocationIterableProperties}. <strong>Note:</strong> feature parameters are not present
     * in the resulting GeometryCollection and must be handled separately to avoid data loss.
     *
     * @deprecated use {@link #createGeometryCollectionFeature(Iterable)} instead
     * @param objects
     *            used to build each geometry
     * @return a GeoJson Feature
     */
    @Deprecated
    public GeoJsonObject createGeometryCollection(
            final Iterable<LocationIterableProperties> objects)
    {
        return createGeometryCollectionFeature(toGeometriesWithProperties(objects));
    }

    /**
     * Creates a GeometryCollection type Feature containing geometries derived from a collection of
     * {@link GeometryWithProperties}. <strong>Note:</strong> feature parameters are not present in
     * the resulting GeometryCollection and must be handled separately to avoid data loss.
     *
     * @param geometriesWithProperties
     *            used to build each geometry
     * @return a GeoJson Feature
     */
    public GeoJsonObject createGeometryCollectionFeature(
            final Iterable<GeometryWithProperties> geometriesWithProperties)
    {
        final JsonObject geometryCollection = new JsonObject();
        geometryCollection.addProperty(TYPE, GEOMETRY_COLLECTION);

        final Map<GeoJsonType, List<Iterable<Location>>> geometryMap = new HashMap<>();
        int counter = 0;
        for (final GeometryWithProperties geometryWithProperties : geometriesWithProperties)
        {
            if (this.logFrequency > 0 && ++counter % this.logFrequency == 0)
            {
                logger.info("Processed {} geometries.", counter);
            }

            final Iterable<Location> geometry = geometryWithProperties.getGeometry();
            final GeoJsonType geoJsonType;
            if (geometry instanceof Location)
            {
                geoJsonType = GeoJsonType.POINT;
            }
            else if (geometry instanceof Polygon)
            {
                geoJsonType = GeoJsonType.POLYGON;
            }
            else if (geometry instanceof PolyLine)
            {
                geoJsonType = GeoJsonType.LINESTRING;
            }
            else
            {
                throw new CoreException("Unrecognized object type {}",
                        geometry.getClass().getSimpleName());
            }
            geometryMap.computeIfAbsent(geoJsonType, key -> new ArrayList<>()).add(geometry);
        }

        final JsonArray geometries = new JsonArray();
        // Point vs MultiPoint
        if (geometryMap.containsKey(GeoJsonType.POINT))
        {
            final List<Iterable<Location>> points = geometryMap.get(GeoJsonType.POINT);
            if (points.size() > 1)
            {
                geometries.add(create(Iterables.translate(points, Iterables::head),
                        GeoJsonType.MULTI_POINT).jsonObject().getAsJsonObject(GEOMETRY));
            }
            else if (points.size() == 1)
            {
                geometries.add(create(Iterables.head(points), GeoJsonType.POINT).jsonObject()
                        .getAsJsonObject(GEOMETRY));
            }
        }

        // Polygon vs MultiPolygon
        if (geometryMap.containsKey(GeoJsonType.POLYGON))
        {
            final List<Iterable<Location>> polygons = geometryMap.get(GeoJsonType.POLYGON);
            if (polygons.size() > 1)
            {
                geometries.add(createMultiPolygons(Iterables.stream(polygons).map(Polygon::new))
                        .jsonObject().getAsJsonObject(GEOMETRY));
            }
            else if (polygons.size() == 1)
            {
                geometries.add(create(Iterables.head(polygons), GeoJsonType.POLYGON).jsonObject()
                        .getAsJsonObject(GEOMETRY));
            }
        }

        // LineString vs MultLineString
        if (geometryMap.containsKey(GeoJsonType.LINESTRING))
        {
            final List<Iterable<Location>> multiPolylines = geometryMap.get(GeoJsonType.LINESTRING);
            if (multiPolylines.size() > 1)
            {
                geometries.add(
                        createMultiLineStrings(Iterables.stream(multiPolylines).map(PolyLine::new))
                                .jsonObject().getAsJsonObject(GEOMETRY));
            }
            else if (multiPolylines.size() == 1)
            {
                geometries.add(create(Iterables.head(multiPolylines), GeoJsonType.LINESTRING)
                        .jsonObject().getAsJsonObject(GEOMETRY));
            }
        }

        geometryCollection.add(GEOMETRIES, geometries);

        final JsonObject result = new JsonObject();
        result.addProperty(TYPE, FEATURE);
        result.add(GEOMETRY, geometryCollection);

        return new GeoJsonObject(result);
    }

    /**
     * Creates a MultiLineString type GeoJson Feature
     *
     * @param polyLines
     *            geometry
     * @return a GeoJson Feature
     */
    public GeoJsonObject createMultiLineStrings(final Iterable<PolyLine> polyLines)
    {
        // Create the coordinates for each polyline
        final List<GeoJsonObject> objects = new ArrayList<>();
        for (final PolyLine polygon : polyLines)
        {
            objects.add(this.create(polygon, GeoJsonType.MULTI_LINESTRING));
        }

        final JsonObject result = new JsonObject();
        result.addProperty(TYPE, FEATURE);
        final JsonArray coordinates = new JsonArray();

        // Add the coordinates back for the entire object
        for (final GeoJsonObject object : objects)
        {
            coordinates
                    .add(object.jsonObject().getAsJsonObject(GEOMETRY).getAsJsonArray(COORDINATES));
        }

        final JsonObject geometry = new JsonObject();
        geometry.addProperty(TYPE, GeoJsonType.MULTI_LINESTRING.getType());
        geometry.add(COORDINATES, coordinates);
        result.add(GEOMETRY, geometry);
        return new GeoJsonObject(result);
    }

    /**
     * Creates a MultiPolygon type GeoJson Feature
     *
     * @param polygons
     *            geometries
     * @return a GeoJson Feature
     */
    public GeoJsonObject createMultiPolygons(final Iterable<Polygon> polygons)
    {
        // Create the coordinates for each polygon
        final List<GeoJsonObject> objects = new ArrayList<>();
        for (final Polygon polygon : polygons)
        {
            objects.add(this.create(polygon.closedLoop(), GeoJsonType.MULTI_POLYGON));
        }

        final JsonObject result = new JsonObject();
        result.addProperty(TYPE, FEATURE);
        final JsonArray coordinates = new JsonArray();

        // Add the coordinates back for the entire object
        for (final GeoJsonObject object : objects)
        {
            final JsonArray subCoordinates = new JsonArray();
            subCoordinates
                    .add(object.jsonObject().getAsJsonObject(GEOMETRY).getAsJsonArray(COORDINATES));
            coordinates.add(subCoordinates);
        }

        final JsonObject geometry = new JsonObject();
        geometry.addProperty(TYPE, GeoJsonType.MULTI_POLYGON.getType());
        geometry.add(COORDINATES, coordinates);
        result.add(GEOMETRY, geometry);
        return new GeoJsonObject(result);
    }

    /**
     * Creates multipolygon from {@link Iterable} of {@link Polygon}s where first polygon is assumed
     * to be the outer ring and the rest are inner.
     *
     * @param polygons
     *            an iterable of polygons where the first is assumed to be the outer polygon in a
     *            multipolygon
     * @return a MultiPolygon geojson feature with one polygon that geometrically represents a
     *         single outer Atlas Multipolygon
     */
    public GeoJsonObject createOneOuterMultiPolygon(final Iterable<Polygon> polygons)
    {
        // Create the coordinates for each polygon
        final List<GeoJsonObject> objects = new ArrayList<>();
        for (final Polygon polygon : polygons)
        {
            objects.add(this.create(polygon.closedLoop(), GeoJsonType.MULTI_POLYGON));
        }

        final JsonObject result = new JsonObject();
        result.addProperty(TYPE, FEATURE);
        final JsonArray coordinates = new JsonArray();

        // Add the coordinates back for the entire object
        for (final GeoJsonObject object : objects)
        {
            coordinates
                    .add(object.jsonObject().getAsJsonObject(GEOMETRY).getAsJsonArray(COORDINATES));
        }

        final JsonArray newCoordinates = new JsonArray();
        newCoordinates.add(coordinates);
        final JsonObject geometry = new JsonObject();
        geometry.addProperty(TYPE, GeoJsonType.MULTI_POLYGON.getType());
        geometry.add(COORDINATES, newCoordinates);
        result.add(GEOMETRY, geometry);
        result.add(PROPERTIES, new JsonObject());
        return new GeoJsonObject(result);
    }
}
