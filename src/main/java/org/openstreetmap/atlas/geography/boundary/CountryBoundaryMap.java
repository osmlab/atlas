package org.openstreetmap.atlas.geography.boundary;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.feature.FeatureIterator;
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
import org.openstreetmap.atlas.geography.atlas.pbf.slicing.RuntimeCounter;
import org.openstreetmap.atlas.geography.atlas.raw.slicing.CountryCodeProperties;
import org.openstreetmap.atlas.geography.boundary.converters.CountryListTwoWayStringConverter;
import org.openstreetmap.atlas.geography.converters.jts.JtsMultiPolygonConverter;
import org.openstreetmap.atlas.geography.converters.jts.JtsMultiPolygonToMultiPolygonConverter;
import org.openstreetmap.atlas.geography.converters.jts.JtsPointConverter;
import org.openstreetmap.atlas.geography.converters.jts.JtsPolyLineConverter;
import org.openstreetmap.atlas.geography.converters.jts.JtsPrecisionManager;
import org.openstreetmap.atlas.locale.IsoCountry;
import org.openstreetmap.atlas.streaming.resource.Resource;
import org.openstreetmap.atlas.streaming.resource.WritableResource;
import org.openstreetmap.atlas.tags.ISOCountryTag;
import org.openstreetmap.atlas.tags.SyntheticNearestNeighborCountryCodeTag;
import org.openstreetmap.atlas.utilities.collections.StringList;
import org.openstreetmap.atlas.utilities.maps.MultiMap;
import org.openstreetmap.atlas.utilities.scalars.Distance;
import org.openstreetmap.atlas.utilities.time.Time;
import org.openstreetmap.osmosis.core.domain.v0_6.Node;
import org.openstreetmap.osmosis.core.domain.v0_6.Way;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.algorithm.distance.DiscreteHausdorffDistance;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.IntersectionMatrix;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.TopologyException;
import com.vividsolutions.jts.index.strtree.GeometryItemDistance;
import com.vividsolutions.jts.index.strtree.ItemDistance;
import com.vividsolutions.jts.index.strtree.STRtree;
import com.vividsolutions.jts.io.WKTReader;
import com.vividsolutions.jts.io.WKTWriter;
import com.vividsolutions.jts.precision.GeometryPrecisionReducer;

/**
 * This {@link CountryBoundaryMap} loads boundaries from given country boundary shape file into
 * spatial index, and then supports {@link Node} and {@link Way} queries.
 *
 * @author Tony Ma
 * @author Yiqing Jin
 * @author mgostintsev
 */
public class CountryBoundaryMap implements Serializable
{
    private static final long serialVersionUID = -1714710346834527699L;

    private static final Logger logger = LoggerFactory.getLogger(CountryBoundaryMap.class);
    // Old country code field
    private static final String ISO_COUNTRY = "ISO_COUNTR";
    // New country code field
    private static final String COUNTRY_CODE = "cntry_code";
    private static final List<String> COUNTRY_CODE_FIELDS = Arrays.asList(ISO_COUNTRY,
            COUNTRY_CODE);
    private static final String GEOMETRY_FIELD = "the_geom";

    // Boundary file constants
    private static final String COUNTRY_BOUNDARY_DELIMITER = "||";
    private static final String SPATIAL_INDEX_DELIMITER = "--";
    private static final String SPATIAL_INDEX_CELL_SEPARATOR = "==";
    private static final String LIST_SEPARATOR = "#";
    private static final String NEW_LINE = "\n";

    // Buffer values for slicing operation. If the remaining piece turns to be smaller than
    // buffer, we'll just ignore them.
    private static final double LINE_BUFFER = 0.000001;
    private static final double AREA_BUFFER = 0.000000001;
    private static final double MAX_AREA_FOR_NEAREST_NEIGHBOR = 100;
    private static final double ANTIMERIDIAN = 180;

    // Slicing constants
    private static final int MAXIMUM_EXPECTED_COUNTRIES_TO_SLICE_WITH = 3;
    private static final int DEFAULT_MAXIMUM_POLYGONS_TO_SLICE_WITH = 2000;
    private static final int EXPANDED_MAXIMUM_POLYGONS_TO_SLICE_WITH = 25000;

    protected static final Function<Iterable<String>, Predicate<String>> COUNTRY_FILTER_GENERATOR = countryList ->
    {
        return countryLine ->
        {
            final String readCountryName = StringList.split(countryLine, COUNTRY_BOUNDARY_DELIMITER)
                    .get(0);
            for (final String countryName : countryList)
            {
                if (countryName.equals(readCountryName))
                {
                    return true;
                }
            }
            return false;
        };
    };

    // Converters
    private static final JtsMultiPolygonConverter JTS_MULTI_POLYGON_TO_POLYGON_CONVERTER = new JtsMultiPolygonConverter();
    private static final JtsPolyLineConverter JTS_POLYLINE_CONVERTER = new JtsPolyLineConverter();
    private static final JtsMultiPolygonToMultiPolygonConverter JTS_MULTI_POLYGON_TO_MULTI_POLYGON_CONVERTER = new JtsMultiPolygonToMultiPolygonConverter();
    private static final JtsPointConverter JTS_POINT_CONVERTER = new JtsPointConverter();

    // The envelope of country boundary map.
    private Envelope envelope;

    // The envelope of indexed area.
    private Envelope gridIndexEnvelope;

    // Maps the ISO-3 country code to country boundary.
    private MultiMap<String, com.vividsolutions.jts.geom.MultiPolygon> countryNameToBoundaryMap;

    // The R-Tree containing all country boundaries for this CountryBoundaryMap.
    private STRtree rawIndex;

    // We create a quad tree along the boundary of a country. Each leaf cell in the tree is
    // guaranteed to only intersect a single country. This gives a performance boost during country
    // code assignment for all features. To optimize the leaf cell lookup for areas or long ways, we
    // store the quad tree cells into a R-Tree, which is the grid index.
    private STRtree gridIndex;

    private boolean useExpandedPolygonLimit = false;

    private transient GeometryPrecisionReducer reducer;
    private final CountryListTwoWayStringConverter countryListConverter = new CountryListTwoWayStringConverter();

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
    @SuppressWarnings("unchecked")
    public static String getGeometryProperty(final Geometry geometry, final String key)
    {
        // Grab the existing key/value map from the object
        final Map<String, String> propertyMap = (Map<String, String>) geometry.getUserData();

        if (propertyMap == null)
        {
            // No user data exists
            return null;
        }
        else
        {
            return propertyMap.get(key);
        }
    }

    /**
     * @param countryGeometries
     *            A list of {@link Geometry}s to check
     * @return {@code true} if all given {@link Geometry}s belong to the same country
     */
    public static boolean isSameCountry(final List<? extends Geometry> countryGeometries)
    {
        if (countryGeometries.isEmpty())
        {
            return false;
        }
        if (countryGeometries.size() == 1)
        {
            return true;
        }

        String sample = null;
        for (final Geometry geometry : countryGeometries)
        {
            final String countryCode = getGeometryProperty(geometry, ISOCountryTag.KEY);
            if (sample == null)
            {
                sample = countryCode;
            }
            else
            {
                if (!sample.equals(countryCode))
                {
                    return false;
                }
            }
        }
        return true;
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
            // Property map exists - check for key existence.
            if (propertyMap.containsKey(key))
            {
                // Trying to override an existing value - this shouldn't happen!
                logger.error(
                        "Trying to override existing '{}' key's value of '{}' with '{}' for geometry {}",
                        key, propertyMap.get(key), value, geometry.toString());
            }
            else
            {
                // New key/value pair, store and update.
                propertyMap.put(key, value);
                geometry.setUserData(propertyMap);
            }
        }
    }

    /**
     * Create a country boundary map from an {@link Atlas}
     *
     * @param atlas
     *            The {@link Atlas} to read the boundaries from.
     */
    public CountryBoundaryMap(final Atlas atlas)
    {
        this(atlas, Rectangle.MAXIMUM);
    }

    /**
     * Create a country boundary map from an {@link Atlas}
     *
     * @param atlas
     *            The {@link Atlas} to read the boundaries from.
     * @param bound
     *            A bound to filter all countries don't intersect with it.
     */
    public CountryBoundaryMap(final Atlas atlas, final Rectangle bound)
    {
        initialize(bound);
        readFromAtlas(atlas);
    }

    /**
     * Create a {@link CountryBoundaryMap} from shape file
     *
     * @param shapeFile
     *            Shape file ends with .shp
     */
    public CountryBoundaryMap(final File shapeFile)
    {
        this(shapeFile, Rectangle.MAXIMUM);
    }

    /**
     * Create a {@link CountryBoundaryMap} from shape file, with the the given bound
     *
     * @param shapeFile
     *            Shape file ends with .shp
     * @param bound
     *            A bound to filter all countries don't intersect with it.
     */
    public CountryBoundaryMap(final File shapeFile, final Rectangle bound)
    {
        initialize(bound);
        readFromFile(shapeFile);
    }

    /**
     * Constructor used explicitly for test purposes.
     *
     * @param boundaries
     *            Country to boundary mapping
     */
    public CountryBoundaryMap(final Map<String, MultiPolygon> boundaries)
    {
        initialize(Rectangle.MAXIMUM);
        boundaries.forEach((name, multiPolygon) -> addCountry(name,
                JTS_MULTI_POLYGON_TO_MULTI_POLYGON_CONVERTER.backwardConvert(multiPolygon)));
    }

    /**
     * Create a {@link CountryBoundaryMap} and load everything from {@link Resource}
     *
     * @param resource
     *            the resource in text format
     * @see #CountryBoundaryMap(Resource, Rectangle)
     */
    public CountryBoundaryMap(final Resource resource)
    {
        this(resource, Rectangle.MAXIMUM);
    }

    /**
     * Create a {@link CountryBoundaryMap} from a text file generated by
     * {@link CountryBoundaryMap#writeBoundariesAndGridIndexAsText(WritableResource, GridIndexParts)}
     * or {@link CountryBoundaryMap#writeBoundariesAsText(WritableResource)}
     *
     * @param resource
     *            The {@link Resource}, in text format
     * @param bound
     *            A bound to filter all countries don't intersect with it.
     */
    public CountryBoundaryMap(final Resource resource, final Rectangle bound)
    {
        initialize(bound);
        readFromText(resource);
    }

    /**
     * @return a {@link StringList} of all country names
     */
    public StringList allCountryNames()
    {
        final List<CountryBoundary> allBoundaries = boundaries(Rectangle.MAXIMUM);
        final StringList result = new StringList();
        allBoundaries.forEach(boundary -> result.add(boundary.getCountryName()));
        return result;
    }

    /**
     * Query country boundaries which cover given {@link Location}
     *
     * @param location
     *            Any {@link Location}
     * @return a list of {@link CountryBoundary}
     */
    public List<CountryBoundary> boundaries(final Location location)
    {
        final Point point = JTS_POINT_CONVERTER.convert(location);
        final MultiMap<String, com.vividsolutions.jts.geom.Polygon> map = new MultiMap<>();
        final List<com.vividsolutions.jts.geom.Polygon> geometry = query(
                location.bounds().asEnvelope());

        geometry.stream().filter(boundary -> boundary.covers(point)).forEach(polygon ->
        {
            final String countryCode = getGeometryProperty(polygon, ISOCountryTag.KEY);
            map.add(countryCode, polygon);

            if (countryCode == null)
            {
                logger.error("Null country code for {}", polygon.toString());
            }
        });

        return toCountryBoundaryList(map);
    }

    /**
     * Query country boundaries which cover given {@link Location}, with an extension square box
     *
     * @param location
     *            Any {@link Location}
     * @param extension
     *            Extension {@link Distance}
     * @return a list of {@link CountryBoundary}
     */
    public List<CountryBoundary> boundaries(final Location location, final Distance extension)
    {
        return boundaries(location.boxAround(extension));
    }

    /**
     * Query country boundaries which cover/partially cover given {@link PolyLine}
     *
     * @param polyLine
     *            Any {@link PolyLine} or {@link Polygon}
     * @return a list of {@link CountryBoundary}
     */
    public List<CountryBoundary> boundaries(final PolyLine polyLine)
    {
        final MultiMap<String, com.vividsolutions.jts.geom.Polygon> map = new MultiMap<>();
        final List<com.vividsolutions.jts.geom.Polygon> geometry = query(
                polyLine.bounds().asEnvelope());

        final com.vividsolutions.jts.geom.LineString lineString = JTS_POLYLINE_CONVERTER
                .convert(polyLine);
        geometry.stream().filter(boundary -> boundary.intersects(lineString)).forEach(polygon ->
        {
            final String countryCode = getGeometryProperty(polygon, ISOCountryTag.KEY);
            map.add(countryCode, polygon);
        });

        return toCountryBoundaryList(map);
    }

    /**
     * Query country boundaries which cover/partially cover given {@link PolyLine}, with an
     * extension square box
     *
     * @param polyLine
     *            Any {@link PolyLine} or {@link Polygon}
     * @param extension
     *            Extension {@link Distance}
     * @return a list of {@link CountryBoundary}
     */
    public List<CountryBoundary> boundaries(final PolyLine polyLine, final Distance extension)
    {
        final MultiMap<String, com.vividsolutions.jts.geom.Polygon> map = new MultiMap<>();
        final List<com.vividsolutions.jts.geom.Polygon> geometry = query(
                polyLine.bounds().expand(extension).asEnvelope());

        final com.vividsolutions.jts.geom.LineString lineString = JTS_POLYLINE_CONVERTER
                .convert(polyLine);

        geometry.stream().filter(boundary -> boundary.intersects(lineString)).forEach(polygon ->
        {
            final String countryCode = getGeometryProperty(polygon, ISOCountryTag.KEY);
            map.add(countryCode, polygon);
        });

        return toCountryBoundaryList(map);
    }

    /**
     * Query country boundaries, which intersect the given bound
     *
     * @param bound
     *            Bounding box
     * @return a list of {@link CountryBoundary}
     */
    public List<CountryBoundary> boundaries(final Rectangle bound)
    {
        final MultiMap<String, com.vividsolutions.jts.geom.Polygon> map = new MultiMap<>();
        final List<com.vividsolutions.jts.geom.Polygon> geometry = query(bound.asEnvelope(), true);
        geometry.stream().forEach(polygon ->
        {
            final String countryCode = getGeometryProperty(polygon, ISOCountryTag.KEY);
            map.add(countryCode, polygon);
        });
        return toCountryBoundaryList(map);
    }

    /**
     * Clips the the given geometry along the boundary.
     *
     * @param identifier
     *            The identifier of the feature we are clipping
     * @param geometry
     *            The {@link Polygon} we are clipping
     * @return The {@link LineString}s making up the clipped geometry
     * @throws TopologyException
     *             Indicating a slicing error
     */
    public List<LineString> clipBoundary(final long identifier, final Polygon geometry)
            throws TopologyException
    {
        if (Objects.isNull(geometry))
        {
            return null;
        }

        final Geometry target = geometry;
        final List<LineString> results = new ArrayList<>();
        final List<Polygon> polygons = query(target.getEnvelopeInternal());

        if (isSameCountry(polygons))
        {
            return results;
        }

        boolean isWarned = false;
        for (final Polygon polygon : polygons)
        {
            final IntersectionMatrix matrix;
            try
            {
                matrix = target.relate(polygon);
            }
            catch (final Exception e)
            {
                if (!isWarned)
                {
                    logger.warn("Error slicing feature: {}, {}", identifier, e.getMessage());
                }
                isWarned = true;
                continue;
            }

            if (matrix.isWithin())
            {
                return results;
            }
            else if (matrix.isIntersects())
            {
                final Geometry clipped = target.intersection(polygon.getBoundary());
                final String containedCountryCode = getGeometryProperty(polygon, ISOCountryTag.KEY);

                if (clipped instanceof GeometryCollection)
                {
                    final GeometryCollection collection = (GeometryCollection) clipped;
                    final int size = collection.getNumGeometries();
                    for (int index = 0; index < size; index++)
                    {
                        final Geometry point = collection.getGeometryN(index);
                        setGeometryProperty(point, ISOCountryTag.KEY, containedCountryCode);
                        results.add((LineString) point);
                    }
                }
                else if (clipped instanceof LineString)
                {
                    setGeometryProperty(clipped, ISOCountryTag.KEY, containedCountryCode);
                    results.add((LineString) clipped);
                }
                else
                {
                    throw new CoreException(
                            "Unexpected geometry {} encountered during country slicing.", clipped);
                }
            }
        }
        return results;
    }

    /**
     * Return a list of country boundaries. The reason why it needs to be a list is to handle
     * antimeridian issue. For countries crossing the antimeridian, two {@link CountryBoundary}
     * objects will be returned, one for each side.
     *
     * @param countryName
     *            Country name in iso3
     * @return a {@link CountryBoundary} list. Unless the country crosses antimeridian, the size
     *         should be 1
     */
    public List<CountryBoundary> countryBoundary(final String countryName)
    {
        final List<com.vividsolutions.jts.geom.MultiPolygon> geometries = this.countryNameToBoundaryMap
                .get(countryName);
        if (geometries == null || geometries.isEmpty())
        {
            return null;
        }

        final List<CountryBoundary> boundaries = new ArrayList<>();

        for (final com.vividsolutions.jts.geom.MultiPolygon geometry : geometries)
        {
            final Set<Polygon> set = new HashSet<>();
            for (int i = 0; i < geometry.getNumGeometries(); i++)
            {
                set.add((Polygon) geometry.getGeometryN(i));
            }

            final MultiPolygon multiPolygon = JTS_MULTI_POLYGON_TO_POLYGON_CONVERTER
                    .backwardConvert(set);
            boundaries.add(new CountryBoundary(countryName, multiPolygon));
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
        final Set<String> set = new HashSet<>();
        final List<com.vividsolutions.jts.geom.Polygon> polygons = query(bound.asEnvelope(), true);
        polygons.stream()
                .forEach(polygon -> set.add(getGeometryProperty(polygon, ISOCountryTag.KEY)));
        return new StringList(set);
    }

    /**
     * Create a secondary spatial index with data intersects with given multi-polygon. This will
     * accelerate performance of geometry check by reduce unnecessary operations. By default, will
     * not save the grid index cells.
     *
     * @param area
     *            The area to intersect
     * @return the builder used to build the grid index
     */
    public AbstractGridIndexBuilder createGridIndex(
            final com.vividsolutions.jts.geom.MultiPolygon area)
    {
        return createGridIndex(area, false);
    }

    /**
     * Create a secondary spatial index with data intersecting the given
     * {@link com.vividsolutions.jts.geom.MultiPolygon}. This will accelerate performance of
     * geometry check by reducing unnecessary operations.
     *
     * @param area
     *            The area to intersect
     * @param saveGridIndexCells
     *            {@code true} to save grid index cells
     * @return The builder used to build the grid index
     */
    @SuppressWarnings("unchecked")
    public AbstractGridIndexBuilder createGridIndex(
            final com.vividsolutions.jts.geom.MultiPolygon area, final boolean saveGridIndexCells)
    {
        if (Objects.isNull(area))
        {
            return null;
        }
        this.gridIndexEnvelope = area.getEnvelopeInternal();
        final List<Polygon> boundaries = this.rawIndex.query(this.gridIndexEnvelope);
        final DynamicGridIndexBuilder builder = new DynamicGridIndexBuilder(boundaries,
                this.gridIndexEnvelope, this.rawIndex);
        builder.saveGridIndexCells(saveGridIndexCells);
        this.gridIndex = builder.getIndex();
        logger.info("Grid index of size {} created.", this.gridIndex.size());
        return builder;
    }

    /**
     * Create a secondary spatial index for given countries. By default, will not save the grid
     * index cells.
     *
     * @param countryCodes
     *            Country codes of countries to build index for
     * @return the index builder used
     * @see #createGridIndex(com.vividsolutions.jts.geom.MultiPolygon)
     */
    public AbstractGridIndexBuilder createGridIndex(final Set<String> countryCodes)
    {
        return createGridIndex(countryCodes, false);
    }

    /**
     * Create a secondary spatial index for given countries.
     *
     * @param countryCodes
     *            Country codes of countries to build index for
     * @param saveGridIndexCells
     *            {@code true} to save grid index cells
     * @return the index builder used
     * @see #createGridIndex(com.vividsolutions.jts.geom.MultiPolygon)
     */
    public AbstractGridIndexBuilder createGridIndex(final Set<String> countryCodes,
            final boolean saveGridIndexCells)
    {
        MultiPolygon multiPolygon = new MultiPolygon(new MultiMap<>());
        for (final String countryCode : countryCodes)
        {
            final List<CountryBoundary> boundaries = countryBoundary(countryCode);
            for (final CountryBoundary boundary : boundaries)
            {
                multiPolygon = multiPolygon.concatenate(boundary.getBoundary());
            }
        }
        final com.vividsolutions.jts.geom.MultiPolygon area = JTS_MULTI_POLYGON_TO_MULTI_POLYGON_CONVERTER
                .backwardConvert(multiPolygon);
        return createGridIndex(area, saveGridIndexCells);
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
     * @see #getCountryCodeISO3(Geometry, boolean)
     */
    public CountryCodeProperties getCountryCodeISO3(final Geometry geometry)
    {
        return getCountryCodeISO3(geometry, false, LINE_BUFFER);
    }

    /**
     * @param geometry
     *            The {@link Geometry}
     * @param fastMode
     *            In fast mode, we only return the first country hit, so if a node is right on the
     *            border we'll still return one country code
     * @return the resulting {@link CountryCodeProperties}
     */
    public CountryCodeProperties getCountryCodeISO3(final Geometry geometry, final boolean fastMode)
    {
        return getCountryCodeISO3(geometry, fastMode, LINE_BUFFER);
    }

    /**
     * @param geometry
     *            The {@link Geometry} which we're assigning a country code to
     * @param fastMode
     *            In fast mode, we only return the first country hit, so if a node is right on the
     *            border, we'll only return one country code
     * @param buffer
     *            The buffer distance of geometry, please note that if the geometry is 0 dimension
     *            then a envelope expend is used instead of a rounding buffer
     * @return the resulting {@link CountryCodeProperties}
     */
    public CountryCodeProperties getCountryCodeISO3(final Geometry geometry, final boolean fastMode,
            final double buffer)
    {
        StringList countryList = new StringList();
        final Geometry target;

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

        final List<Polygon> polygons = query(target.getEnvelopeInternal());
        boolean usingNearestNeighbor = false;
        if (polygons.size() == 1 || isSameCountry(polygons))
        {
            countryList.add(getGeometryProperty(polygons.get(0), ISOCountryTag.KEY));
        }
        else
        {
            try
            {
                if (fastMode)
                {
                    final Optional<String> match = polygons.stream()
                            .filter(polygon -> polygon.intersects(target))
                            .map(polygon -> getGeometryProperty(polygon, ISOCountryTag.KEY))
                            .findFirst();
                    match.ifPresent(countryList::add);
                }
                else
                {
                    countryList = new StringList(
                            polygons.stream().filter(polygon -> polygon.intersects(target))
                                    .map(polygon -> getGeometryProperty(polygon, ISOCountryTag.KEY))
                                    .collect(Collectors.toList()));
                }
                if (countryList.isEmpty())
                {
                    // The node isn't within any country boundary - try to assign the iso_code
                    // based on the nearest country
                    final Geometry nearestGeometry = nearestNeighbour(target.getEnvelopeInternal(),
                            target, new GeometryItemDistance());
                    if (nearestGeometry != null)
                    {
                        usingNearestNeighbor = true;
                        final String nearestCountryCode = getGeometryProperty(nearestGeometry,
                                ISOCountryTag.KEY);
                        countryList.add(nearestCountryCode);
                    }
                    else
                    {
                        countryList.add(ISOCountryTag.COUNTRY_MISSING);
                    }
                }
            }
            catch (final Exception e)
            {
                logger.warn(
                        "There was exception when trying to find out country code for geometry {}, {}",
                        geometry, e.getMessage());
                countryList.add(ISOCountryTag.COUNTRY_MISSING);
            }
        }

        return new CountryCodeProperties(this.countryListConverter.backwardConvert(countryList),
                usingNearestNeighbor);
    }

    /**
     * @param location
     *            The {@link Location} to check
     * @return the resulting {@link CountryCodeProperties}
     * @see #getCountryCodeISO3(Geometry)
     */
    public CountryCodeProperties getCountryCodeISO3(final Location location)
    {
        return getCountryCodeISO3(JTS_POINT_CONVERTER.convert(location));
    }

    /**
     * @return all the countries represented by this {@link CountryBoundaryMap}
     */
    public Set<String> getLoadedCountries()
    {
        return this.countryNameToBoundaryMap.keySet();
    }

    /**
     * @return the raw {@link STRtree} index used by this {@link CountryBoundaryMap}
     */
    public STRtree getRawIndex()
    {
        return this.rawIndex;
    }

    /**
     * @return the number of countries represented by this {@link CountryBoundaryMap}
     */
    public int size()
    {
        return this.countryNameToBoundaryMap.size();
    }

    /**
     * Slice the {@link Geometry} with given country boundary map and assign country code to each
     * piece.
     *
     * @param identifier
     *            id of object being sliced.
     * @param geometry
     *            The object to be sliced.
     * @return a list of geometry objects. If target doesn't cross any border then it contains only
     *         one item with country code assigned. If target cross border then slice it by the
     *         border line and assign country code for each piece. If a feature is not contained by
     *         any country boundary, it will be assigned to nearest country.
     * @throws TopologyException
     *             When the slicing could not be made.
     */
    public List<Geometry> slice(final long identifier, final Geometry geometry)
            throws TopologyException
    {
        if (Objects.isNull(geometry))
        {
            return null;
        }

        Geometry target = geometry;
        final List<Geometry> results = new ArrayList<>();
        List<Polygon> polygons = query(target.getEnvelopeInternal());

        // Performance improvement, if only one polygon returned no need to do any further
        // evaluation.
        if (isSameCountry(polygons))
        {
            final String countryCode = getGeometryProperty(polygons.get(0), ISOCountryTag.KEY);
            setGeometryProperty(target, ISOCountryTag.KEY, countryCode);
            addResult(target, results);
            return results;
        }

        // Remove duplicates
        polygons = removeDuplicate(polygons);

        // Avoid slicing across too many polygons for performance reasons
        if (polygons.size() > getPolygonSliceLimit())
        {
            RuntimeCounter.waySkipped(identifier);
            logger.warn("Skipping slicing way {} due to too many intersecting polygons [{}]",
                    identifier, polygons.size());
            return null;
        }

        RuntimeCounter.geometryChecked();
        boolean fullyMatched = false;
        boolean isWarned = false;
        final Time time = Time.now();

        if (polygons.size() > MAXIMUM_EXPECTED_COUNTRIES_TO_SLICE_WITH)
        {
            logger.warn("slicing way {} with {} polygons", identifier, polygons.size());
            if (logger.isTraceEnabled())
            {
                final Map<String, List<Polygon>> countries = polygons.stream().collect(Collectors
                        .groupingBy(polygon -> getGeometryProperty(polygon, ISOCountryTag.KEY)));
                countries.forEach((key, value) -> logger.trace("{} : {}", key, value.size()));
            }
        }

        final List<Polygon> intersected = new ArrayList<>();

        // Check relation of target to all polygons
        for (final Polygon polygon : polygons)
        {
            final IntersectionMatrix matrix;
            try
            {
                matrix = target.relate(polygon);
            }
            catch (final Exception e)
            {
                // TODO, we should handle this.
                if (!isWarned)
                {
                    logger.warn("error slicing way: {}, {}", identifier, e.getMessage());
                }

                isWarned = true;
                continue;
            }

            // Fully contained inside a single country, no need to go any further, just assign and
            // return the country code
            if (matrix.isWithin())
            {
                RuntimeCounter.geometryCheckedWithin();
                final String countryCode = getGeometryProperty(polygon, ISOCountryTag.KEY);
                setGeometryProperty(target, ISOCountryTag.KEY, countryCode);
                addResult(target, results);
                fullyMatched = true;
                break;
            }

            if (!matrix.isIntersects())
            {
                RuntimeCounter.geometryCheckedNoIntersect();
            }
            else
            {
                intersected.add(polygon);
            }
        }

        // Performance: short circuit, if all intersected polygons in same country, skip cutting.
        if (isSameCountry(intersected))
        {
            final String countryCode = getGeometryProperty(intersected.get(0), ISOCountryTag.KEY);
            setGeometryProperty(target, ISOCountryTag.KEY, countryCode);
            addResult(target, results);
            return results;
        }

        // Start the cutting
        for (final Polygon intersection : intersected)
        {
            RuntimeCounter.geometryCheckedIntersect();
            final Geometry clipped = target.intersection(intersection);

            // We don't want single point pieces
            if (clipped.getNumPoints() < 2)
            {
                continue;
            }

            final String countryCode = getGeometryProperty(intersection, ISOCountryTag.KEY);
            setGeometryProperty(clipped, ISOCountryTag.KEY, countryCode);
            addResult(clipped, results);

            // Update target to be what's left after clipping
            target = target.difference(intersection);
            if (target.getDimension() == 1 && target.getLength() < LINE_BUFFER
                    || target.getDimension() == 2 && target.getArea() < AREA_BUFFER
                            && new DiscreteHausdorffDistance(target, intersection)
                                    .orientedDistance() < LINE_BUFFER)
            {
                // The remaining piece is very small and we ignore it. This also helps avoid
                // cutting features just a little over boundary lines and generating too many new
                // nodes, which is both unnecessary and exhausts node identifier resources.
                fullyMatched = true;
                break;
            }
        }

        // Part or all of the geometry is not inside any country, assign with nearest country.
        if (!fullyMatched)
        {
            final Geometry nearestGeometry = nearestNeighbour(target.getEnvelopeInternal(), target,
                    new GeometryItemDistance());
            if (nearestGeometry != null)
            {
                final String nearestCountryCode = getGeometryProperty(nearestGeometry,
                        ISOCountryTag.KEY);
                setGeometryProperty(target, ISOCountryTag.KEY, nearestCountryCode);
                setGeometryProperty(target, SyntheticNearestNeighborCountryCodeTag.KEY,
                        SyntheticNearestNeighborCountryCodeTag.YES.toString());
                addResult(target, results);
            }
        }

        logger.info("Took {} to slice way {}", time.untilNow(), identifier);
        return results;
    }

    /**
     * <pre>
     * Write country boundary map and the grid index into a text file using WKT format.
     * Output will have the format below, where each country will be on a new line:
     *
     * [ISO-Country-code]||[Country boundary Multi-Polygon]#
     * --[Grid Index Envelope Polygon]--[grid-cell-1]==[grid-cell-n]==
     * </pre>
     *
     * @param resource
     *            The output {@link WritableResource}
     * @param gridIndexParts
     *            The parts of the grid index used for reconstruction
     * @throws IOException
     *             {@link IOException}
     */
    public void writeBoundariesAndGridIndexAsText(final WritableResource resource,
            final GridIndexParts gridIndexParts) throws IOException
    {
        try (BufferedWriter out = new BufferedWriter(
                new OutputStreamWriter(resource.write(), StandardCharsets.UTF_8)))
        {
            final WKTWriter wktWriter = new WKTWriter();

            // First, write the boundaries
            writeBoundariesInternal(wktWriter, out, resource);

            // Next, write the grid index, if the parts are complete.
            if (gridIndexParts.areComplete())
            {
                // Write the grid index envelope first
                out.write(SPATIAL_INDEX_DELIMITER);
                final Envelope gridIndexEnvelope = gridIndexParts.getEnvelope();
                final Polygon envelopeAsPolygon = AbstractGridIndexBuilder.buildGeoBox(
                        gridIndexEnvelope.getMinX(), gridIndexEnvelope.getMaxX(),
                        gridIndexEnvelope.getMinY(), gridIndexEnvelope.getMaxY());

                out.write(wktWriter.write(envelopeAsPolygon));
                out.write(SPATIAL_INDEX_DELIMITER);

                // Next, write all the grid index cells
                logger.info("Writing grid index to output: {}", resource.getName());
                gridIndexParts.getSpatialIndexCells().allValues().forEach(cell ->
                {
                    try
                    {
                        final Polygon polygon = AbstractGridIndexBuilder.buildGeoBox(cell.getMinX(),
                                cell.getMaxX(), cell.getMinY(), cell.getMaxY());
                        out.write(wktWriter.write(polygon));
                        out.write(SPATIAL_INDEX_CELL_SEPARATOR);
                    }
                    catch (final IOException e)
                    {
                        throw new RuntimeException(e);
                    }
                });
            }
            else
            {
                logger.error(
                        "Incomplete grid index parts supplied, failed to write grid index to output: {}.",
                        resource.getName());
            }
        }
        catch (final Exception e)
        {
            logger.error("Error creating boundary and grid index text file at location: {}.",
                    resource.getName());
        }
    }

    /**
     * <pre>
     * Write country boundary map into a text file using WKT format.
     * Output will have the format below, where each country will be on a new line:
     *
     * [ISO-Country-code] || [Country boundary Multi-Polygon]#
     * </pre>
     *
     * @param resource
     *            The output {@link WritableResource}
     * @throws IOException
     *             {@link IOException}
     */
    public void writeBoundariesAsText(final WritableResource resource) throws IOException
    {
        try (BufferedWriter out = new BufferedWriter(
                new OutputStreamWriter(resource.write(), StandardCharsets.UTF_8)))
        {
            final WKTWriter wktWriter = new WKTWriter();
            writeBoundariesInternal(wktWriter, out, resource);
        }
        catch (final Exception e)
        {
            logger.error("Error creating boundary text file at location: {}.", resource.getName());
        }
    }

    private void addCountry(final String countryISO3,
            final com.vividsolutions.jts.geom.MultiPolygon multiPolygon)
    {
        if (!this.envelope.intersects(multiPolygon.getEnvelopeInternal()))
        {
            return;
        }

        Geometry fixedPolygon = this.reducer.reduce(multiPolygon);
        if (fixedPolygon instanceof com.vividsolutions.jts.geom.Polygon)
        {
            fixedPolygon = new com.vividsolutions.jts.geom.MultiPolygon(
                    new Polygon[] { (Polygon) fixedPolygon },
                    JtsPrecisionManager.getGeometryFactory());
        }

        final com.vividsolutions.jts.geom.MultiPolygon reducedPolygon = (com.vividsolutions.jts.geom.MultiPolygon) fixedPolygon;
        this.countryNameToBoundaryMap.add(countryISO3, reducedPolygon);

        for (int index = 0; index < reducedPolygon.getNumGeometries(); index++)
        {
            final com.vividsolutions.jts.geom.Polygon polygon = (com.vividsolutions.jts.geom.Polygon) reducedPolygon
                    .getGeometryN(index);
            if (!this.envelope.intersects(polygon.getEnvelopeInternal()))
            {
                continue;
            }

            setGeometryProperty(polygon, ISOCountryTag.KEY, countryISO3);
            this.rawIndex.insert(polygon.getEnvelopeInternal(), polygon);
        }
    }

    private void addResult(final Geometry geometry, final List<Geometry> results)
    {
        if (geometry instanceof GeometryCollection)
        {
            final GeometryCollection collection = (GeometryCollection) geometry;
            final int size = collection.getNumGeometries();
            for (int index = 0; index < size; index++)
            {
                final Geometry point = collection.getGeometryN(index);
                final String countryCode = getGeometryProperty(geometry, ISOCountryTag.KEY);
                setGeometryProperty(point, ISOCountryTag.KEY, countryCode);
                addResult(point, results);
            }
        }
        else if (geometry instanceof LineString || geometry instanceof Polygon)
        {
            results.add(geometry);
        }
        else
        {
            logger.error(geometry.toText());
        }
    }

    private Optional<Property> findCountryName(final Feature feature,
            final List<String> alternateNames)
    {
        final List<String> lowerCaseAlternateNames = alternateNames.stream()
                .map(String::toLowerCase).collect(Collectors.toList());

        return feature.getProperties().stream().filter(property -> lowerCaseAlternateNames
                .contains(property.getName().getURI().toLowerCase())).findFirst();
    }

    private int getPolygonSliceLimit()
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

    private void initialize(final Rectangle bound)
    {
        this.envelope = bound.asEnvelope();
        this.countryNameToBoundaryMap = new MultiMap<>();
        this.rawIndex = new STRtree();
        // this should be created by calling createGridIndex method.
        this.gridIndex = null;

        this.reducer = new GeometryPrecisionReducer(JtsPrecisionManager.getPrecisionModel());
        this.reducer.setPointwise(true);
        this.reducer.setChangePrecisionModel(true);
    }

    private Geometry nearestNeighbour(final Envelope envelope, final Object object,
            final ItemDistance distance)
    {
        if (envelope.getArea() > MAX_AREA_FOR_NEAREST_NEIGHBOR)
        {
            return null;
        }
        if (this.gridIndex != null)
        {
            return (Geometry) this.gridIndex.nearestNeighbour(envelope, object, distance);
        }
        else
        {
            return (Geometry) this.rawIndex.nearestNeighbour(envelope, object, distance);
        }
    }

    private List<Polygon> query(final Envelope envelope)
    {
        return query(envelope, false);
    }

    /**
     * @param envelope
     *            The {@link Envelope} as query input
     * @param isBound
     *            Indicate if this is a bounding box query. If yes it'll skip prime meridian check.
     * @return list of objects intersect with given envelope.
     */
    @SuppressWarnings("unchecked")
    private List<Polygon> query(final Envelope envelope, final boolean isBound)
    {
        // Handle the prime meridian case. This is a simplified solution since there are only a few
        // countries (Russia, USA, Fiji, Kiribati, New Zealand, Antarctica) crossing the line and
        // none of the areas seem to be able to trigger geometry check. So we are skipping the
        // geometry cut here, and only handling the envelope case. Also, this is depending on the
        // boundary map already been cut along the meridian.
        final List<Envelope> bboxes = new ArrayList<>();
        if (envelope.getWidth() >= ANTIMERIDIAN && !isBound)
        {
            final Envelope bbox1 = new Envelope(-180, envelope.getMinX(), envelope.getMinY(),
                    envelope.getMaxY());
            final Envelope bbox2 = new Envelope(envelope.getMaxX(), 180, envelope.getMinY(),
                    envelope.getMaxY());
            bboxes.add(bbox1);
            bboxes.add(bbox2);
        }
        else
        {
            bboxes.add(envelope);
        }

        final List<Polygon> result = new ArrayList<>();
        for (final Envelope bbox : bboxes)
        {
            if (this.gridIndex != null)
            {
                if (this.gridIndexEnvelope.contains(bbox))
                {
                    result.addAll(this.gridIndex.query(bbox));
                }
                else
                {
                    result.addAll(this.rawIndex.query(bbox));
                }
            }
            if (result.isEmpty())
            {
                result.addAll(this.rawIndex.query(bbox));
            }
        }

        return result;
    }

    /**
     * Read a {@link CountryBoundaryMap} from the {@link ComplexBoundary}(ies) inside an
     * {@link Atlas}
     *
     * @param atlas
     *            The {@link Atlas} to read from.
     */
    private void readFromAtlas(final Atlas atlas)
    {
        for (final ComplexBoundary complexBoundary : new ComplexBoundaryFinder().find(atlas))
        {
            if (complexBoundary.hasCountryCode())
            {
                final List<String> countryCodes = new ArrayList<>();
                try
                {
                    for (final IsoCountry isoCountry : complexBoundary.getCountries())
                    {
                        countryCodes.add(isoCountry.getIso3CountryCode());
                    }
                    final MultiPolygon outline = complexBoundary.getOutline();
                    final com.vividsolutions.jts.geom.MultiPolygon multiPolygon = JTS_MULTI_POLYGON_TO_MULTI_POLYGON_CONVERTER
                            .backwardConvert(outline);
                    for (final String countryCode : countryCodes)
                    {
                        addCountry(countryCode, multiPolygon);
                    }
                }
                catch (final IllegalArgumentException e)
                {
                    throw new CoreException("Unable to read country boundary for country codes {}",
                            countryCodes, e);
                }
            }
        }
    }

    private void readFromFile(final File file)
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
                final com.vividsolutions.jts.geom.MultiPolygon multiPolygon = (com.vividsolutions.jts.geom.MultiPolygon) geometry
                        .getValue();
                addCountry(nameValue, multiPolygon);
            }
        }
        catch (final IOException e)
        {
            e.printStackTrace();
            throw new CoreException("Error reading country boundary from file");
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
    }

    private void readFromText(final Resource resource)
    {
        final WKTReader reader = new WKTReader();
        for (final String line : resource.lines())
        {
            if (!line.isEmpty())
            {
                // Read the spatial index, if it exists
                if (line.startsWith(SPATIAL_INDEX_DELIMITER))
                {
                    logger.info("Reading grid index from file");

                    // Strip the initial delimiter, and split on the second one
                    final String[] gridIndexParts = line.substring(SPATIAL_INDEX_DELIMITER.length())
                            .split(SPATIAL_INDEX_DELIMITER);

                    if (gridIndexParts.length != 2)
                    {
                        throw new CoreException("Missing either size or geometry for grid index");
                    }

                    // Part 1 is the envelope
                    final String gridIndexEnvelope = gridIndexParts[0];

                    // Part 2 are all the grid index cells
                    final String gridIndexGeometry = gridIndexParts[1];
                    final STRtree gridIndex = new STRtree();

                    try
                    {
                        final StringTokenizer gridIndexGeometryTokenizer = new StringTokenizer(
                                gridIndexGeometry, SPATIAL_INDEX_CELL_SEPARATOR);
                        while (gridIndexGeometryTokenizer.hasMoreTokens())
                        {
                            final com.vividsolutions.jts.geom.Geometry geometry = reader
                                    .read(gridIndexGeometryTokenizer.nextToken());
                            gridIndex.insert(geometry.getEnvelopeInternal(), geometry);
                        }

                        final com.vividsolutions.jts.geom.Geometry envelope = reader
                                .read(gridIndexEnvelope);
                        this.gridIndexEnvelope = envelope.getEnvelopeInternal();

                        gridIndex.build();
                        this.gridIndex = gridIndex;
                    }
                    catch (final Exception e)
                    {
                        logger.error("Invalid grid index text file format.", e);
                        e.printStackTrace();
                    }

                    logger.info("Successfully read grid index of size {} from file.",
                            this.gridIndex.size());

                    // We can break here since the grid index is the last entry in the text file
                    break;
                }

                // Read the country boundaries
                final StringTokenizer boundaryTokenizer = new StringTokenizer(line,
                        COUNTRY_BOUNDARY_DELIMITER);
                final String countryISO = boundaryTokenizer.nextToken();
                final String wkt = boundaryTokenizer.nextToken();
                try
                {
                    final StringTokenizer wktTokenizer = new StringTokenizer(wkt, LIST_SEPARATOR);
                    while (wktTokenizer.hasMoreTokens())
                    {
                        final com.vividsolutions.jts.geom.MultiPolygon multiPolygon = (com.vividsolutions.jts.geom.MultiPolygon) reader
                                .read(wktTokenizer.nextToken());
                        addCountry(countryISO, multiPolygon);
                    }
                }
                catch (final Exception e)
                {
                    logger.error("Invalid country boundary text file format.", e);
                    e.printStackTrace();
                }
            }
        }
    }

    private List<Polygon> removeDuplicate(final List<Polygon> polygons)
    {
        // This is assuming the list size should be relatively small.
        final List<Polygon> temp = new ArrayList<>();
        for (final Polygon polygon : polygons)
        {
            boolean found = false;
            for (final Polygon polygon1 : temp)
            {
                if (polygon == polygon1)
                {
                    found = true;
                    break;
                }
            }
            if (found)
            {
                continue;
            }
            else
            {
                temp.add(polygon);
            }
        }
        return temp;
    }

    private List<CountryBoundary> toCountryBoundaryList(
            final MultiMap<String, com.vividsolutions.jts.geom.Polygon> map)
    {
        final List<CountryBoundary> list = new ArrayList<>();
        for (final Map.Entry<String, List<com.vividsolutions.jts.geom.Polygon>> entry : map
                .entrySet())
        {
            final String name = entry.getKey();
            final List<com.vividsolutions.jts.geom.Polygon> polygons = entry.getValue();
            final MultiPolygon multiPolygon = JTS_MULTI_POLYGON_TO_POLYGON_CONVERTER
                    .backwardConvert(new HashSet<>(polygons));
            final CountryBoundary boundary = new CountryBoundary(name, multiPolygon);
            list.add(boundary);
        }
        return list;
    }

    private void writeBoundariesInternal(final WKTWriter wktWriter, final BufferedWriter out,
            final WritableResource resource) throws IOException
    {
        logger.info("Writing country boundaries to output");
        this.countryNameToBoundaryMap.forEach((countryISO3, multiPolygon) ->
        {
            try
            {
                out.write(countryISO3);
                out.write(COUNTRY_BOUNDARY_DELIMITER);
                for (final com.vividsolutions.jts.geom.MultiPolygon geometry : multiPolygon)
                {
                    out.write(wktWriter.write(geometry));
                    out.write(LIST_SEPARATOR);
                }
                out.write(NEW_LINE);
            }
            catch (final IOException e)
            {
                throw new RuntimeException(e);
            }
        });
    }

}
