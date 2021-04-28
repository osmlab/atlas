package org.openstreetmap.atlas.geography.boundary;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.feature.FeatureIterator;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.prep.PreparedGeometry;
import org.locationtech.jts.geom.prep.PreparedGeometryFactory;
import org.locationtech.jts.geom.prep.PreparedPolygon;
import org.locationtech.jts.index.strtree.STRtree;
import org.locationtech.jts.io.WKTReader;
import org.locationtech.jts.io.WKTWriter;
import org.locationtech.jts.precision.GeometryPrecisionReducer;
import org.opengis.feature.Feature;
import org.opengis.feature.Property;
import org.opengis.feature.simple.SimpleFeature;
import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.MultiPolygon;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.Rectangle;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.geography.atlas.items.complex.boundaries.ComplexBoundary;
import org.openstreetmap.atlas.geography.atlas.items.complex.boundaries.ComplexBoundaryFinder;
import org.openstreetmap.atlas.geography.atlas.raw.slicing.CountryCodeProperties;
import org.openstreetmap.atlas.geography.boundary.converters.CountryBoundaryMapGeoJsonConverter;
import org.openstreetmap.atlas.geography.boundary.converters.CountryListTwoWayStringConverter;
import org.openstreetmap.atlas.geography.converters.jts.JtsMultiPolygonToMultiPolygonConverter;
import org.openstreetmap.atlas.geography.converters.jts.JtsPointConverter;
import org.openstreetmap.atlas.geography.converters.jts.JtsPolyLineConverter;
import org.openstreetmap.atlas.geography.converters.jts.JtsPrecisionManager;
import org.openstreetmap.atlas.geography.geojson.GeoJson;
import org.openstreetmap.atlas.geography.geojson.GeoJsonType;
import org.openstreetmap.atlas.streaming.resource.Resource;
import org.openstreetmap.atlas.streaming.resource.WritableResource;
import org.openstreetmap.atlas.tags.ISOCountryTag;
import org.openstreetmap.atlas.utilities.collections.StringList;
import org.openstreetmap.atlas.utilities.maps.MultiMap;
import org.openstreetmap.atlas.utilities.scalars.Distance;
import org.openstreetmap.osmosis.core.domain.v0_6.Node;
import org.openstreetmap.osmosis.core.domain.v0_6.Way;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.gson.JsonObject;

/**
 * This {@link CountryBoundaryMap} loads boundaries from given country boundary shape file into
 * spatial index, and then supports {@link Node} and {@link Way} queries.
 *
 * @author Tony Ma
 * @author Yiqing Jin
 * @author mgostintsev
 * @author mkalender
 * @author samg
 */
public class CountryBoundaryMap implements Serializable, GeoJson
{
    // Buffer values for slicing operation. If the remaining piece turns to be smaller than
    // buffer, we'll just ignore them.
    public static final double LINE_BUFFER = 0.000001;
    public static final double AREA_BUFFER = 0.0000000001;

    // Boundary file constants
    static final String COUNTRY_BOUNDARY_DELIMITER = "||";
    private static final long serialVersionUID = -1714710346834527699L;
    private static final Logger logger = LoggerFactory.getLogger(CountryBoundaryMap.class);
    // Old country code field
    private static final String ISO_COUNTRY = "ISO_COUNTR";
    // New country code field
    private static final String COUNTRY_CODE = "cntry_code";
    private static final List<String> COUNTRY_CODE_FIELDS = Arrays.asList(ISO_COUNTRY,
            COUNTRY_CODE);

    // WKT Helpers
    private static final WKTWriter WKT_WRITER = new WKTWriter();

    private static final String GEOMETRY_FIELD = "the_geom";
    private static final String LIST_SEPARATOR = "#";
    private static final String POLYGON_ID_KEY = "pid";

    private static final int DEFAULT_MAXIMUM_POLYGONS_TO_SLICE_WITH = 2000;
    private static final int EXPANDED_MAXIMUM_POLYGONS_TO_SLICE_WITH = 25000;
    // Converters
    private static final JtsPolyLineConverter JTS_POLYLINE_CONVERTER = new JtsPolyLineConverter();
    private static final JtsMultiPolygonToMultiPolygonConverter JTS_MULTI_POLYGON_TO_MULTI_POLYGON_CONVERTER = new JtsMultiPolygonToMultiPolygonConverter();
    private static final JtsPointConverter JTS_POINT_CONVERTER = new JtsPointConverter();

    // Maps the ISO-3 country code to country boundary.
    private final MultiMap<String, Polygon> countryNameToBoundaryMap;
    private transient MultiMap<String, PreparedPolygon> countryNameToPreparedBoundaryPolyonMap;

    private boolean useExpandedPolygonLimit = true;
    private final transient GeometryPrecisionReducer reducer;
    private final CountryListTwoWayStringConverter countryListConverter = new CountryListTwoWayStringConverter();

    private transient STRtree spatialIndex;

    /**
     * @param countryGeometries
     *            A list of {@link Geometry}s to check
     * @return The set of country codes represented
     */
    public static Set<String> countryCodesIn(final Collection<?> countryGeometries)
    {
        return countryGeometries.stream().map(geometry ->
        {
            if (geometry instanceof Geometry)
            {
                return getGeometryProperty((Geometry) geometry, ISOCountryTag.KEY);
            }
            else if (geometry instanceof PreparedGeometry)
            {
                return getGeometryProperty(((PreparedGeometry) geometry).getGeometry(),
                        ISOCountryTag.KEY);
            }
            return StringUtils.EMPTY;
        }).filter(StringUtils::isNotEmpty).collect(Collectors.toSet());
    }

    /**
     * @param atlas
     *            {@link Atlas} to read boundaries
     * @return {@link CountryBoundaryMap} created from {@link Atlas}
     */
    public static CountryBoundaryMap fromAtlas(final Atlas atlas)
    {
        final CountryBoundaryMap map = new CountryBoundaryMap();
        map.readFromAtlas(atlas);
        return map;
    }

    /**
     * @param boundaries
     *            A {@link Map} from country names to country boundaries in {@link MultiPolygon}
     *            format
     * @return {@link CountryBoundaryMap} created from existing boundaries
     */
    public static CountryBoundaryMap fromBoundaryMap(final Map<String, List<Polygon>> boundaries)
    {
        final CountryBoundaryMap map = new CountryBoundaryMap(Rectangle.MAXIMUM);
        boundaries.forEach(
                (name, polygons) -> polygons.forEach(polygon -> map.addCountry(name, polygon)));
        return map;

    }

    /**
     * @param resource
     *            Text {@link Resource} to read boundaries
     * @return {@link CountryBoundaryMap} created from {@link Resource}
     */
    public static CountryBoundaryMap fromPlainText(final Resource resource)
    {
        final CountryBoundaryMap map = new CountryBoundaryMap();
        map.readFromPlainText(resource);
        return map;
    }

    /**
     * @param file
     *            Shape {@link File} to read boundaries
     * @return {@link CountryBoundaryMap} created from {@link File}
     */
    public static CountryBoundaryMap fromShapeFile(final File file)
    {
        final CountryBoundaryMap map = new CountryBoundaryMap();
        map.readFromShapeFile(file);
        return map;
    }

    public static Stream<Geometry> geometries(final GeometryCollection collection)
    {
        return IntStream.range(0, collection.getNumGeometries())
                .mapToObj(index -> collection.getGeometryN(index));
    }

    /**
     * Follows the same concept as {@link #setGeometryProperty(Geometry, String, String)}. Because
     * we're working with JTS {@link Polygon}s instead of {@link AtlasEntity}s, we don't have access
     * to a tag map and can't explicitly set tags. This wraps the {@link Polygon#getUserData} call
     * and is the single entry point that should be used for setting {@link Geometry} properties.
     *
     * @param geometry
     *            The {@link Geometry} whose userData we're interested in
     * @return The property map for the geometry, {@code null} if it doesn't exist
     */
    @SuppressWarnings("unchecked")
    public static Map<String, String> getGeometryProperties(final Geometry geometry)
    {
        final Map<String, String> result = new HashMap<>();
        // Grab the existing key/value map from the object
        final Map<String, String> propertyMap = (Map<String, String>) geometry.getUserData();

        if (propertyMap != null)
        {
            result.putAll(propertyMap);
        }
        return result;
    }

    /**
     * Follows the same concept as {@link #setGeometryProperty(Geometry, String, String)}. Because
     * we're working with JTS {@link Polygon}s instead of {@link AtlasEntity}s, we don't have access
     * to a tag map and can't explicitly set tags. This wraps the {@link Polygon#getUserData} call
     * and is the single entry point that should be used for setting {@link Geometry} properties.
     *
     * @param geometry
     *            The {@link Geometry} whose userData we're interested in
     * @param key
     *            The metadata lookup key to use
     * @return the string value of the given key, {@code null} if it doesn't exist
     */
    public static String getGeometryProperty(final Geometry geometry, final String key)
    {
        return getGeometryProperties(geometry).get(key);
    }

    /**
     * @param countryGeometries
     *            A list of {@link Geometry}s to check
     * @return {@code true} if all given {@link Geometry}s belong to the same country
     */
    public static boolean isSameCountry(final Collection<?> countryGeometries)
    {
        return numberCountries(countryGeometries) == 1;
    }

    /**
     * @param countryGeometries
     *            A list of {@link Geometry}s to check
     * @return The number of distinct countries represented
     */
    public static long numberCountries(final Collection<?> countryGeometries)
    {
        if (countryGeometries.isEmpty())
        {
            return 0;
        }

        if (countryGeometries.size() == 1)
        {
            return 1;
        }
        return countryCodesIn(countryGeometries).size();
    }

    /**
     * Because we're working with JTS {@link Polygon}s instead of {@link AtlasEntity}s, we don't
     * have access to a tag map and can't explicitly set tags. Instead, we use the
     * {@link Polygon#setUserData} call to store a {@link Map} of properties that we derived during
     * country slicing and country code assignment. This wraps the {@link Polygon#setUserData} call
     * and is the single entry point that should be used for setting {@link Geometry} properties.
     *
     * @param geometry
     *            The {@link Geometry} for which to save the key/value pair
     * @param key
     *            The key to store for this {@link Geometry}
     * @param value
     *            The value to store for the {@link Geometry}
     */
    @SuppressWarnings("unchecked")
    public static void setGeometryProperty(final Geometry geometry, final String key,
            final String value)
    {
        // Grab the existing key/value map from the object
        final Map<String, String> propertyMap = (Map<String, String>) geometry.getUserData();

        if (propertyMap == null)
        {
            // No user data exists - create one and store the data.
            final Map<String, String> newPropertyMap = new HashMap<>();
            newPropertyMap.put(key, value);
            geometry.setUserData(newPropertyMap);
        }
        else
        {
            final String existingValue = propertyMap.get(key);

            // Check for key existence
            if (existingValue == null)
            {
                // New key/value pair, store and update
                propertyMap.put(key, value);
                geometry.setUserData(propertyMap);
            }
            else
            {
                // Trying to override an existing value - this shouldn't happen!
                if (!Objects.equals(existingValue, value) && logger.isDebugEnabled())
                {
                    logger.debug(
                            "Trying to override existing '{}' key's value of '{}' with '{}' for geometry with values {}",
                            key, existingValue, value, propertyMap);
                }
            }
        }
    }

    /**
     * Default constructor
     */
    public CountryBoundaryMap()
    {
        this(Rectangle.MAXIMUM);
    }

    /**
     * Constructor with limited bounds
     *
     * @param bounds
     *            {@link Rectangle} bounds for boundary map calculation
     */
    public CountryBoundaryMap(final Rectangle bounds)
    {
        this.countryNameToBoundaryMap = new MultiMap<>();
        this.countryNameToPreparedBoundaryPolyonMap = new MultiMap<>();
        this.reducer = new GeometryPrecisionReducer(JtsPrecisionManager.getPrecisionModel());
        this.reducer.setPointwise(true);
        this.reducer.setChangePrecisionModel(true);
        this.spatialIndex = new STRtree();
    }

    public void addCountry(final String country, final Polygon polygon)
    {
        this.countryNameToBoundaryMap.add(country, polygon);
        final PreparedPolygon prepared = (PreparedPolygon) PreparedGeometryFactory.prepare(polygon);
        this.countryNameToPreparedBoundaryPolyonMap.add(country, prepared);
        setGeometryProperty(prepared.getGeometry(), ISOCountryTag.KEY, country);
        setGeometryProperty(prepared.getGeometry(), POLYGON_ID_KEY, "0");
        this.spatialIndex.insert(prepared.getGeometry().getEnvelopeInternal(), prepared);
    }

    public void addCountryWithoutPolygonIdKey(final String country, final Polygon polygon)
    {
        final PreparedPolygon prepared = (PreparedPolygon) PreparedGeometryFactory.prepare(polygon);
        this.countryNameToBoundaryMap.add(country, polygon);
        this.countryNameToPreparedBoundaryPolyonMap.add(country, prepared);
        setGeometryProperty(prepared.getGeometry(), ISOCountryTag.KEY, country);
        this.spatialIndex.insert(prepared.getGeometry().getEnvelopeInternal(), prepared);
    }

    /**
     * @return {@link List} of all country names
     */
    public List<String> allCountryNames()
    {
        final List<String> countries = new ArrayList<>();
        countries.addAll(this.countryNameToBoundaryMap.keySet());
        return countries;
    }

    /**
     * Get a GeoJSON representation of this {@link CountryBoundaryMap}.
     *
     * @return a GeoJSON representation of this {@link CountryBoundaryMap}
     */
    @Override
    public JsonObject asGeoJson()
    {
        return new CountryBoundaryMapGeoJsonConverter().prettyPrint(false).usePolygons(false)
                .convert(this);
    }

    /**
     * Query country boundaries which cover given {@link Location}
     *
     * @param location
     *            Any {@link Location}
     * @return a MultiMap of Polygons per country boundary
     */
    public MultiMap<String, Polygon> boundaries(final Location location)
    {
        final Point jtsPoint = JTS_POINT_CONVERTER.convert(location);
        return this.boundariesHelper(() -> this.query(jtsPoint.getEnvelopeInternal()),
                preparedGeom -> preparedGeom.covers(jtsPoint));
    }

    /**
     * Query country boundaries which cover given {@link Location}, with an extension square box
     *
     * @param location
     *            Any {@link Location}
     * @param extension
     *            Extension {@link Distance}
     * @return a MultiMap of Polygons per country boundary
     */
    public MultiMap<String, Polygon> boundaries(final Location location, final Distance extension)
    {
        return this.boundaries(location.boxAround(extension));
    }

    /**
     * Query country boundaries which cover/partially cover given {@link PolyLine}
     *
     * @param polyLine
     *            Any {@link PolyLine} or {@link Polygon}
     * @return a MultiMap of Polygons per country boundary
     */
    public MultiMap<String, Polygon> boundaries(final PolyLine polyLine)
    {
        final LineString jtsLine = JTS_POLYLINE_CONVERTER.convert(polyLine);
        return this.boundariesHelper(() -> this.query(jtsLine.getEnvelopeInternal()),
                preparedGeom -> preparedGeom.intersects(jtsLine));
    }

    /**
     * Query country boundaries, which intersect the given bound
     *
     * @param bound
     *            Bounding box
     * @return a MultiMap of Polygons per country boundary
     */
    public MultiMap<String, Polygon> boundaries(final Rectangle bound)
    {
        final GeometryFactory factory = new GeometryFactory();
        return this.boundariesHelper(() -> this.query(bound.asEnvelope()),
                polygon -> polygon.intersects(factory.toGeometry(bound.asEnvelope())));
    }

    /**
     * Return a list of country boundaries.
     *
     * @param countryName
     *            Country name in iso3
     * @return a MultiMap of Polygons per country boundary
     */
    public List<Polygon> countryBoundary(final String countryName)
    {
        final List<PreparedPolygon> geometries = this.countryNameToPreparedBoundaryPolyonMap
                .get(countryName);
        if (geometries == null || geometries.isEmpty())
        {
            return null;
        }

        final List<Polygon> boundaries = new ArrayList<>();
        for (final PreparedGeometry prep : geometries)
        {
            boundaries.add((Polygon) prep.getGeometry());
        }
        return boundaries;
    }

    /**
     * Query country names which intersect the given bound
     *
     * @param bound
     *            Bounding box
     * @return A {@link StringList} of country names
     */
    public StringList countryCodesOverlappingWith(final Rectangle bound)
    {
        return new StringList(query(bound.asEnvelope()).stream()
                .map(polygon -> getGeometryProperty(polygon.getGeometry(), ISOCountryTag.KEY))
                .collect(Collectors.toList()));
    }

    /**
     * Allows you to increase/decrease the number of polygons a way can be sliced with.
     *
     * @param value
     *            {@code true} to expand the slice limit, {@code false} to keep it at the default
     *            limit
     */
    public void expandPolygonSliceLimit(final boolean value)
    {
        this.useExpandedPolygonLimit = value;
    }

    /**
     * This defaults to slow mode, which tries to match all countries.
     *
     * @param geometry
     *            A JTS {@link Geometry}
     * @return the resulting {@link CountryCodeProperties}
     */
    public CountryCodeProperties getCountryCodeISO3(final Geometry geometry)
    {
        return this.getCountryCodeISO3(geometry, 0.0);
    }

    /**
     * @param geometry
     *            The {@link Geometry} which we're assigning a country code to
     * @param buffer
     *            The buffer distance of geometry, please note that if the geometry is 0 dimension
     *            then a envelope expend is used instead of a rounding buffer
     * @return the resulting {@link CountryCodeProperties}
     */
    public CountryCodeProperties getCountryCodeISO3(final Geometry geometry, final double buffer)
    {
        final StringList countryList = new StringList();
        final Geometry target;

        if (buffer > 0.0)
        {
            if (geometry.getDimension() == 0)
            {
                final Envelope envelope = geometry.getEnvelopeInternal();
                envelope.expandBy(buffer);
                target = geometry.getFactory().toGeometry(envelope);
            }
            else
            {
                target = geometry.buffer(buffer);
            }
        }
        else
        {
            target = geometry;
        }

        final List<PreparedPolygon> polygons = this.query(target);
        if (polygons.size() == 1 || isSameCountry(polygons))
        {
            countryList.add(getGeometryProperty(polygons.get(0).getGeometry(), ISOCountryTag.KEY));
        }
        else
        {
            countryList.addAll(polygons.stream()
                    .map(polygon -> getGeometryProperty(polygon.getGeometry(), ISOCountryTag.KEY))
                    .collect(Collectors.toList()));
        }

        return new CountryCodeProperties(this.countryListConverter.backwardConvert(countryList));
    }

    /**
     * @param location
     *            The {@link Location} to check
     * @return the resulting {@link CountryCodeProperties}
     * @see #getCountryCodeISO3(Geometry)
     */
    public CountryCodeProperties getCountryCodeISO3(final Location location)
    {
        return this.getCountryCodeISO3(JTS_POINT_CONVERTER.convert(location));
    }

    public MultiMap<String, PreparedPolygon> getCountryNameToBoundaryMap()
    {
        return this.countryNameToPreparedBoundaryPolyonMap;
    }

    @Override
    public GeoJsonType getGeoJsonType()
    {
        return GeoJsonType.FEATURE_COLLECTION;
    }

    /**
     * @return all the countries represented by this {@link CountryBoundaryMap}
     */
    public Set<String> getLoadedCountries()
    {
        return this.countryNameToPreparedBoundaryPolyonMap.keySet();
    }

    public int getPolygonSliceLimit()
    {
        if (this.useExpandedPolygonLimit)
        {
            return EXPANDED_MAXIMUM_POLYGONS_TO_SLICE_WITH;
        }
        else
        {
            return DEFAULT_MAXIMUM_POLYGONS_TO_SLICE_WITH;
        }
    }

    public List<PreparedPolygon> query(final Envelope envelope)
    {
        return this.query(JtsPrecisionManager.getGeometryFactory().toGeometry(envelope));
    }

    @SuppressWarnings("unchecked")
    public List<PreparedPolygon> query(final Geometry geometry)
    {
        final Geometry target;
        if (geometry.getDimension() == 0)
        {
            final Envelope expanded = geometry.getEnvelopeInternal();
            expanded.expandBy(LINE_BUFFER);
            target = JtsPrecisionManager.getGeometryFactory().toGeometry(expanded);
        }
        else
        {
            target = geometry;
        }
        final List<PreparedPolygon> result = new ArrayList<>();
        this.spatialIndex.query(target.getEnvelopeInternal()).forEach(boundaryPolygon ->
        {
            final PreparedPolygon boundary = (PreparedPolygon) boundaryPolygon;
            if (boundary.intersects(target))
            {
                result.add(boundary);
            }
        });
        return result;
    }

    /**
     * Read a {@link CountryBoundaryMap} from the {@link ComplexBoundary}(ies) inside an
     * {@link Atlas}
     *
     * @param atlas
     *            The {@link Atlas} to read from.
     */
    public void readFromAtlas(final Atlas atlas)
    {
        final Iterable<ComplexBoundary> deduplicatedComplexBoundaries = resolveOverlappingBorders(
                new ComplexBoundaryFinder().find(atlas));
        for (final ComplexBoundary complexBoundary : deduplicatedComplexBoundaries)
        {
            if (complexBoundary.hasCountryCode())
            {
                final List<String> countryCodes = new ArrayList<>();
                try
                {
                    final MultiPolygon outline = complexBoundary.getOutline();
                    final org.locationtech.jts.geom.MultiPolygon multiPolygon = JTS_MULTI_POLYGON_TO_MULTI_POLYGON_CONVERTER
                            .backwardConvert(outline);
                    for (int i = 0; i < multiPolygon.getNumGeometries(); i++)
                    {
                        final Polygon polygon = (Polygon) multiPolygon.getGeometryN(i);
                        complexBoundary.getCountries().forEach(isoCountry -> this
                                .addCountry(isoCountry.getIso3CountryCode(), polygon));
                    }
                }
                catch (final IllegalArgumentException e)
                {
                    throw new CoreException("Unable to read country boundary for country codes {}",
                            countryCodes, e);
                }
            }
        }
        this.spatialIndex.build();
    }

    /**
     * Read a {@link CountryBoundaryMap} from a {@link Resource} in plain text format
     *
     * @param resource
     *            {@link Resource} containing {@link CountryBoundaryMap} in plain text format
     */
    public void readFromPlainText(final Resource resource)
    {
        final Map<String, Integer> countryIdentifierMap = new HashMap<>();
        final WKTReader reader = new WKTReader(JtsPrecisionManager.getGeometryFactory());

        if (resource == null)
        {
            throw new CoreException("Boundary map resource is null and could not be read.");
        }

        for (final String line : resource.lines())
        {
            // Ignore empty lines
            if (line.isEmpty())
            {
                continue;
            }

            // Read the country boundaries
            final StringTokenizer boundaryTokenizer = new StringTokenizer(line,
                    COUNTRY_BOUNDARY_DELIMITER);
            final String country = boundaryTokenizer.nextToken();
            final String geometryString = boundaryTokenizer.nextToken();
            try
            {
                final StringTokenizer geometryTokenizer = new StringTokenizer(geometryString,
                        LIST_SEPARATOR);
                while (geometryTokenizer.hasMoreTokens())
                {
                    final String polygonString = geometryTokenizer.nextToken();
                    final Geometry geometry = reader.read(polygonString);
                    setGeometryProperty(geometry, ISOCountryTag.KEY, country);
                    final Integer identifier = countryIdentifierMap.get(country);
                    if (identifier == null)
                    {
                        countryIdentifierMap.put(country, 0);
                        setGeometryProperty(geometry, POLYGON_ID_KEY, String.valueOf(0));
                    }
                    else
                    {
                        countryIdentifierMap.put(country, identifier + 1);
                        setGeometryProperty(geometry, POLYGON_ID_KEY,
                                String.valueOf(identifier + 1));
                    }
                    this.addCountryWithoutPolygonIdKey(country, (Polygon) geometry);
                }
            }
            catch (final Exception e)
            {
                logger.error(
                        "Error while reading malformed file. Please ensure all lines are Polygons, and there is no GridIndex attached!");
                throw new CoreException("Invalid country boundary text file format.", e);
            }
        }
        this.spatialIndex.build();
    }

    /**
     * Read a {@link CountryBoundaryMap} from a shape {@link File}
     *
     * @param file
     *            Shape {@link File}
     */
    public void readFromShapeFile(final File file)
    {
        FileDataStore store = null;
        FeatureIterator<SimpleFeature> iterator = null;
        try
        {
            store = FileDataStoreFinder.getDataStore(file);
            iterator = store.getFeatureSource().getFeatures().features();
            while (iterator.hasNext())
            {
                final Feature feature = iterator.next();
                final Optional<Property> name = findCountryName(feature, COUNTRY_CODE_FIELDS);
                final Property geometry = feature.getProperty(GEOMETRY_FIELD);
                final String nameValue = (String) name.orElseThrow(() -> new CoreException(
                        "Can't read country code attribute from shape file")).getValue();
                final org.locationtech.jts.geom.MultiPolygon multiPolygon = (org.locationtech.jts.geom.MultiPolygon) geometry
                        .getValue();
                for (int i = 0; i < multiPolygon.getNumGeometries(); i++)
                {
                    this.addCountry(nameValue, (Polygon) multiPolygon.getGeometryN(i));
                }
            }
        }
        catch (final IOException e)
        {
            throw new CoreException("Error reading country boundary from file", e);
        }
        finally
        {
            if (iterator != null)
            {
                iterator.close();
            }
            if (store != null)
            {
                store.dispose();
            }
        }
        this.spatialIndex.build();
    }

    /**
     * Filters the given iterable of {@link ComplexBoundary}s to only allow a single country to
     * claim an outer boundary polygon.
     *
     * @param complexBoundaries
     *            The {@link ComplexBoundary}s to filter duplicate polygons from
     * @return An iterable of {@link ComplexBoundary} with no duplicate outer polygons
     */
    public Iterable<ComplexBoundary> resolveOverlappingBorders(
            final Iterable<ComplexBoundary> complexBoundaries)
    {
        final List<ComplexBoundary> deduplicatedComplexBoundaries = new ArrayList<>();
        final Set<org.openstreetmap.atlas.geography.Polygon> processedOuters = new HashSet<>();
        final List<ComplexBoundary> complexBoundaryList = Lists.newArrayList(complexBoundaries);
        Collections.sort(complexBoundaryList, (firstBoundary, secondBoundary) ->
        {
            if (firstBoundary.getIdentifier() > secondBoundary.getIdentifier())
            {
                return 1;
            }
            else if (secondBoundary.getIdentifier() > firstBoundary.getIdentifier())
            {
                return -1;
            }
            return 0;
        });
        for (final ComplexBoundary currentComplexBoundary : complexBoundaryList)
        {
            final MultiPolygon outline = currentComplexBoundary.getOutline();
            if (outline != null)
            {
                for (final org.openstreetmap.atlas.geography.Polygon outer : outline.outers())
                {
                    if (processedOuters.contains(outer))
                    {
                        currentComplexBoundary.removeOuter(outer);
                    }
                    // ensures that the outer is only claimed once it has been assigned to a country
                    else if (Lists.newArrayList(currentComplexBoundary.getCountries()).size() > 0)
                    {
                        processedOuters.add(outer);
                    }
                }
                deduplicatedComplexBoundaries.add(currentComplexBoundary);
            }
        }
        return deduplicatedComplexBoundaries;
    }

    /**
     * @return the number of countries represented by this {@link CountryBoundaryMap}
     */
    public int size()
    {
        return this.countryNameToBoundaryMap.size();
    }

    /**
     * <pre>
     * Write country boundary map into a text file using WKT format.
     * Output will have the format below, where each country will be on a new line:
     *
     * [ISO-Country-code]||[Country boundary Multi-Polygon]#
     * </pre>
     *
     * @param resource
     *            The output {@link WritableResource}
     * @throws IOException
     *             {@link IOException}
     */
    public void writeToFile(final WritableResource resource) throws IOException
    {
        try (BufferedWriter output = new BufferedWriter(
                new OutputStreamWriter(resource.write(), StandardCharsets.UTF_8)))
        {
            // Write country boundaries
            this.writeCountryBoundaries(output);
        }
    }

    /**
     * Given a {@link Supplier}, retrieves {@link List} of {@link Polygon}s and applies given
     * {@link Predicate}, and returns results as MultiMap
     *
     * @param supplier
     *            {@link Supplier} providing {@link List} of {@link Polygon}s
     * @param filter
     *            {@link Polygon} {@link Predicate} to be used as filter
     * @return a MultiMap of Polygons per country boundary
     */
    private MultiMap<String, Polygon> boundariesHelper(
            final Supplier<List<PreparedPolygon>> supplier, final Predicate<PreparedPolygon> filter)
    {
        final MultiMap<String, Polygon> map = new MultiMap<>();
        final List<PreparedPolygon> geometry = supplier.get();
        geometry.stream().filter(filter).forEach(polygon ->
        {
            final String countryCode = getGeometryProperty(polygon.getGeometry(),
                    ISOCountryTag.KEY);
            map.add(countryCode, (Polygon) polygon.getGeometry());
        });
        return map;
    }

    private Optional<Property> findCountryName(final Feature feature,
            final List<String> alternateNames)
    {
        final List<String> lowerCaseAlternateNames = alternateNames.stream()
                .map(String::toLowerCase).collect(Collectors.toList());
        return feature.getProperties().stream().filter(property -> lowerCaseAlternateNames
                .contains(property.getName().getURI().toLowerCase())).findFirst();
    }

    private void readObject(final java.io.ObjectInputStream inFile)
            throws IOException, ClassNotFoundException
    {
        inFile.defaultReadObject();
        if (this.countryNameToPreparedBoundaryPolyonMap == null)
        {
            this.countryNameToPreparedBoundaryPolyonMap = new MultiMap<>();
        }
        if (this.spatialIndex == null)
        {
            this.spatialIndex = new STRtree();
        }
        this.countryNameToBoundaryMap.entrySet()
                .forEach(entry -> entry.getValue().forEach(polygon ->
                {
                    final PreparedPolygon prepared = (PreparedPolygon) PreparedGeometryFactory
                            .prepare(polygon);
                    this.countryNameToPreparedBoundaryPolyonMap.add(entry.getKey(), prepared);
                    this.spatialIndex.insert(prepared.getGeometry().getEnvelopeInternal(),
                            prepared);
                }));
        this.spatialIndex.build();
    }

    private void writeCountryBoundaries(final BufferedWriter output) throws IOException
    {
        logger.info("Writing country boundaries to output");
        this.countryNameToPreparedBoundaryPolyonMap.forEach((country, polygons) ->

        polygons.forEach(polygon ->
        {
            try
            {
                output.write(country);
                output.write(COUNTRY_BOUNDARY_DELIMITER);
                output.write(WKT_WRITER.write(polygon.getGeometry()));
                output.write(LIST_SEPARATOR);
                output.write(System.lineSeparator());
            }
            catch (final IOException e)
            {
                throw new CoreException("Failed to write country boundaries.", e);
            }
        }));
    }
}
