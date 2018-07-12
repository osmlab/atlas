package org.openstreetmap.atlas.geography.boundary;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
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

import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.feature.FeatureIterator;
import org.geotools.measure.Longitude;
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
import org.openstreetmap.atlas.streaming.resource.Resource;
import org.openstreetmap.atlas.streaming.resource.WritableResource;
import org.openstreetmap.atlas.tags.ISOCountryTag;
import org.openstreetmap.atlas.tags.SyntheticNearestNeighborCountryCodeTag;
import org.openstreetmap.atlas.tags.Taggable;
import org.openstreetmap.atlas.utilities.collections.StringList;
import org.openstreetmap.atlas.utilities.maps.MultiMap;
import org.openstreetmap.atlas.utilities.scalars.Distance;
import org.openstreetmap.atlas.utilities.scalars.Duration;
import org.openstreetmap.atlas.utilities.time.Time;
import org.openstreetmap.osmosis.core.domain.v0_6.Node;
import org.openstreetmap.osmosis.core.domain.v0_6.Way;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;
import com.vividsolutions.jts.algorithm.distance.DiscreteHausdorffDistance;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.IntersectionMatrix;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.TopologyException;
import com.vividsolutions.jts.index.strtree.AbstractNode;
import com.vividsolutions.jts.index.strtree.GeometryItemDistance;
import com.vividsolutions.jts.index.strtree.ItemBoundable;
import com.vividsolutions.jts.index.strtree.ItemDistance;
import com.vividsolutions.jts.index.strtree.STRtree;
import com.vividsolutions.jts.io.ParseException;
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
 * @author mkalender
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
    static final String COUNTRY_BOUNDARY_DELIMITER = "||";
    private static final String LIST_SEPARATOR = "#";
    private static final String GRID_ENVELOPE_DELIMITER = "::";
    private static final String GRID_INDEX_DELIMITER = ";;";
    private static final int GRID_INDEX_MIN_LENGTH = 3;
    private static final int GRID_INDEX_FIRST_CELL_INDEX = 2;
    private static final String POLYGON_ID_KEY = "pid";

    // For backward compatibility
    // TODO Remove once all files move to the new format
    private static final String SPATIAL_INDEX_DELIMITER = "--";

    // Buffer values for slicing operation. If the remaining piece turns to be smaller than
    // buffer, we'll just ignore them.
    private static final double LINE_BUFFER = 0.000001;
    private static final double AREA_BUFFER = 0.000000001;
    private static final double MAX_AREA_FOR_NEAREST_NEIGHBOR = 100;
    private static final double ANTIMERIDIAN = Longitude.MAX_VALUE;

    // Slicing constants
    private static final int MAXIMUM_EXPECTED_COUNTRIES_TO_SLICE_WITH = 3;
    private static final int DEFAULT_MAXIMUM_POLYGONS_TO_SLICE_WITH = 2000;
    private static final int EXPANDED_MAXIMUM_POLYGONS_TO_SLICE_WITH = 25000;

    // Converters
    private static final JtsMultiPolygonConverter JTS_MULTI_POLYGON_TO_POLYGON_CONVERTER = new JtsMultiPolygonConverter();
    private static final JtsPolyLineConverter JTS_POLYLINE_CONVERTER = new JtsPolyLineConverter();
    private static final JtsMultiPolygonToMultiPolygonConverter JTS_MULTI_POLYGON_TO_MULTI_POLYGON_CONVERTER = new JtsMultiPolygonToMultiPolygonConverter();
    private static final JtsPointConverter JTS_POINT_CONVERTER = new JtsPointConverter();

    // WKT Helpers
    private static final WKTWriter WKT_WRITER = new WKTWriter();

    // The envelope of country boundary map.
    private final Envelope envelope;

    // The envelope of indexed area.
    private Envelope gridIndexEnvelope;

    // Maps the ISO-3 country code to country boundary.
    private final MultiMap<String, Polygon> countryNameToBoundaryMap;

    // The R-Tree containing all country boundaries for this CountryBoundaryMap.
    private final STRtree rawIndex;

    // We create a quad tree along the boundary of a country. Each leaf cell in the tree is
    // guaranteed to only intersect a single country. This gives a performance boost during country
    // code assignment for all features. To optimize the leaf cell lookup for areas or long ways, we
    // store the quad tree cells into a R-Tree, which is the grid index.
    private STRtree gridIndex;

    private boolean useExpandedPolygonLimit = true;
    private transient Predicate<Taggable> shouldAlwaysSlicePredicate = taggable -> false;
    private transient GeometryPrecisionReducer reducer;
    private final CountryListTwoWayStringConverter countryListConverter = new CountryListTwoWayStringConverter();

    /**
     * Collects leaf nodes of given {@link AbstractNode} into the given {@link MultiMap} from
     * {@link Geometry} to {@link Envelope}s.
     *
     * @param node
     *            Starting node for collection
     * @param cells
     *            {@link MultiMap} to save cells into
     */
    @SuppressWarnings("unchecked")
    static void collectCells(final AbstractNode node, final MultiMap<Geometry, Envelope> cells)
    {
        if (node.getLevel() > 0)
        {
            node.getChildBoundables().stream().forEach(childNode ->
            {
                collectCells((AbstractNode) childNode, cells);
            });
        }
        else if (node.getLevel() == 0)
        {
            node.getChildBoundables().stream().forEach(item ->
            {
                final ItemBoundable boundable = (ItemBoundable) item;
                final Geometry polygon = (Geometry) boundable.getItem();
                final Envelope bounds = (Envelope) boundable.getBounds();
                cells.add(polygon, bounds);
            });
        }
    }

    /**
     * @param countryGeometries
     *            A list of {@link Geometry}s to check
     * @return The set of country codes represented
     */
    public static Set<String> countryCodesIn(final List<? extends Geometry> countryGeometries)
    {
        return countryGeometries.stream()
                .map(geometry -> getGeometryProperty(geometry, ISOCountryTag.KEY))
                .collect(Collectors.toSet());
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
    public static CountryBoundaryMap fromBoundaryMap(final Map<String, MultiPolygon> boundaries)
    {
        final CountryBoundaryMap map = new CountryBoundaryMap(Rectangle.MAXIMUM);
        boundaries.forEach((name, multiPolygon) -> map.addCountry(name,
                JTS_MULTI_POLYGON_TO_MULTI_POLYGON_CONVERTER.backwardConvert(multiPolygon)));
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
    public static boolean isSameCountry(final List<? extends Geometry> countryGeometries)
    {
        return numberCountries(countryGeometries) == 1;
    }

    /**
     * @param countryGeometries
     *            A list of {@link Geometry}s to check
     * @return The number of distinct countries represented
     */
    public static long numberCountries(final List<? extends Geometry> countryGeometries)
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
                if (!Objects.equals(existingValue, value))
                {
                    logger.error(
                            "Trying to override existing '{}' key's value of '{}' with '{}' for geometry {}",
                            key, existingValue, value, geometry.toString());
                }
            }
        }
    }

    private static Stream<Geometry> geometries(final GeometryCollection collection)
    {
        return IntStream.range(0, collection.getNumGeometries())
                .mapToObj(index -> collection.getGeometryN(index));
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
        this.envelope = bounds.asEnvelope();
        this.countryNameToBoundaryMap = new MultiMap<>();
        this.rawIndex = new STRtree();
        this.gridIndex = null;

        this.reducer = new GeometryPrecisionReducer(JtsPrecisionManager.getPrecisionModel());
        this.reducer.setPointwise(true);
        this.reducer.setChangePrecisionModel(true);
    }

    void addCountry(final String country,
            final com.vividsolutions.jts.geom.MultiPolygon multiPolygon)
    {
        if (!this.envelope.intersects(multiPolygon.getEnvelopeInternal()))
        {
            return;
        }

        Geometry fixedPolygon = this.reducer.reduce(multiPolygon);
        if (fixedPolygon instanceof Polygon)
        {
            fixedPolygon = new com.vividsolutions.jts.geom.MultiPolygon(
                    new Polygon[] { (Polygon) fixedPolygon },
                    JtsPrecisionManager.getGeometryFactory());
        }

        final List<Geometry> parts = geometries(
                (com.vividsolutions.jts.geom.MultiPolygon) fixedPolygon)
                        .collect(Collectors.toList());
        int polygonIdentifier = -1;
        for (final Geometry part : parts)
        {
            polygonIdentifier++;
            final Polygon polygon = (Polygon) part;
            this.countryNameToBoundaryMap.add(country, polygon);

            if (this.envelope.intersects(polygon.getEnvelopeInternal()))
            {
                setGeometryProperty(polygon, ISOCountryTag.KEY, country);
                setGeometryProperty(polygon, POLYGON_ID_KEY, String.valueOf(polygonIdentifier));
                this.rawIndex.insert(polygon.getEnvelopeInternal(), polygon);
            }
        }
    }

    /**
     * @return A {@link Map} from {@link Geometry}s to {@link List} of {@link Envelope} cells that
     *         forms the grid index.
     */
    MultiMap<Geometry, Envelope> getCells()
    {
        if (this.gridIndex == null)
        {
            return null;
        }

        final MultiMap<Geometry, Envelope> polygonToCells = new MultiMap<>();
        collectCells(this.gridIndex.getRoot(), polygonToCells);
        return polygonToCells;
    }

    /**
     * @return {@link STRtree} grid index used by this {@link CountryBoundaryMap}
     */
    STRtree getGridIndex()
    {
        return this.gridIndex;
    }

    /**
     * @return the raw {@link STRtree} index used by this {@link CountryBoundaryMap}
     */
    STRtree getRawIndex()
    {
        return this.rawIndex;
    }

    /**
     * Read a {@link CountryBoundaryMap} from the {@link ComplexBoundary}(ies) inside an
     * {@link Atlas}
     *
     * @param atlas
     *            The {@link Atlas} to read from.
     */
    void readFromAtlas(final Atlas atlas)
    {
        for (final ComplexBoundary complexBoundary : new ComplexBoundaryFinder().find(atlas))
        {
            if (complexBoundary.hasCountryCode())
            {
                final List<String> countryCodes = new ArrayList<>();
                try
                {
                    final MultiPolygon outline = complexBoundary.getOutline();
                    final com.vividsolutions.jts.geom.MultiPolygon multiPolygon = JTS_MULTI_POLYGON_TO_MULTI_POLYGON_CONVERTER
                            .backwardConvert(outline);
                    complexBoundary.getCountries().forEach(isoCountry -> this
                            .addCountry(isoCountry.getIso3CountryCode(), multiPolygon));
                }
                catch (final IllegalArgumentException e)
                {
                    throw new CoreException("Unable to read country boundary for country codes {}",
                            countryCodes, e);
                }
            }
        }
    }

    /**
     * Read a {@link CountryBoundaryMap} from a {@link Resource} in plain text format
     *
     * @param resource
     *            {@link Resource} containing {@link CountryBoundaryMap} in plain text format
     */
    void readFromPlainText(final Resource resource)
    {
        final Map<String, Integer> countryIdentifierMap = new HashMap<>();
        final WKTReader reader = new WKTReader();

        // Last piece is cells
        STRtree gridIndexFromFile = null;

        for (final String line : resource.lines())
        {
            // Ignore empty lines
            if (line.isEmpty())
            {
                continue;
            }

            // Ignore previous version of grid index indicator. Previously generated boundary map
            // files had grid indices serialized not the right way.
            if (line.startsWith(SPATIAL_INDEX_DELIMITER))
            {
                logger.warn("Found previous version of grid index. Grid index will be ignored.");
                continue;
            }

            // Read line and take respective action
            if (line.startsWith(GRID_INDEX_DELIMITER))
            {
                if (this.countryNameToBoundaryMap.isEmpty())
                {
                    logger.warn("Cannot read grid index, because no country boundary is supplied.");
                }

                if (gridIndexFromFile == null)
                {
                    gridIndexFromFile = new STRtree();
                }

                // Strip the initial delimiter, and split on the second one
                final String[] gridIndexParts = line.substring(GRID_INDEX_DELIMITER.length())
                        .split(GRID_INDEX_DELIMITER);
                final int length = gridIndexParts.length;

                if (length < GRID_INDEX_MIN_LENGTH)
                {
                    throw new CoreException("Grid index entry is malformed.");
                }

                // First piece is polygon identifier
                final String country = gridIndexParts[0];
                final String identifier = gridIndexParts[1];
                final Geometry polygon = this.countryNameToBoundaryMap.get(country)
                        .get(Integer.valueOf(identifier));
                if (polygon == null)
                {
                    throw new CoreException("Grid index entry is malformed missing polygon.");
                }

                // Starting from second item go over and parse cells
                try
                {
                    for (int index = GRID_INDEX_FIRST_CELL_INDEX; index < length; index++)
                    {
                        final String cellWkt = gridIndexParts[index];
                        if (Strings.isNullOrEmpty(cellWkt))
                        {
                            continue;
                        }

                        final Geometry cell = reader.read(cellWkt);
                        gridIndexFromFile.insert(cell.getEnvelopeInternal(), polygon);
                    }
                }
                catch (final Exception e)
                {
                    throw new CoreException("Failed to create grid index cells.", e);
                }
            }
            else if (line.startsWith(GRID_ENVELOPE_DELIMITER))
            {
                try
                {
                    final Geometry envelope = reader
                            .read(line.substring(GRID_ENVELOPE_DELIMITER.length()));
                    this.gridIndexEnvelope = envelope.getEnvelopeInternal();
                }
                catch (final ParseException e)
                {
                    throw new CoreException("Failed to read grid index envelope.", e);
                }
            }
            else
            {
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

                        // NOTE: This is for backward compatibility. Older boundary maps save
                        // MultiPolygons, but newer ones save Polygons and use the order per country
                        // for grid indexing.
                        // TODO Remove MultiPolygon part once all files move to the new format
                        if (geometry instanceof Polygon)
                        {
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
                            this.addCountry(country, (Polygon) geometry);
                        }
                        else if (geometry instanceof com.vividsolutions.jts.geom.MultiPolygon)
                        {
                            this.addCountry(country,
                                    (com.vividsolutions.jts.geom.MultiPolygon) geometry);
                        }
                    }
                }
                catch (final Exception e)
                {
                    throw new CoreException("Invalid country boundary text file format.", e);
                }
            }
        }

        if (gridIndexFromFile != null)
        {
            logger.info("Successfully read grid index of size {} from file.",
                    gridIndexFromFile.size());
            gridIndexFromFile.build();
            this.gridIndex = gridIndexFromFile;
        }
        else
        {
            logger.warn("Given boundary file didn't have grid index.");
        }
    }

    /**
     * Read a {@link CountryBoundaryMap} from a shape {@link File}
     *
     * @param file
     *            Shape {@link File}
     */
    void readFromShapeFile(final File file)
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
                this.addCountry(nameValue, multiPolygon);
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
    }

    /**
     * @return {@link List} of all country names
     */
    public List<String> allCountryNames()
    {
        return this.boundaries(Rectangle.MAXIMUM).stream().map(CountryBoundary::getCountryName)
                .collect(Collectors.toList());
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
        return this.boundariesHelper(() -> this.query(location.bounds().asEnvelope()),
                boundary -> boundary.covers(JTS_POINT_CONVERTER.convert(location)));
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
        return this.boundaries(location.boxAround(extension));
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
        return this.boundariesHelper(() -> this.query(polyLine.bounds().asEnvelope()),
                boundary -> boundary.intersects(JTS_POLYLINE_CONVERTER.convert(polyLine)));
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
        return this.boundariesHelper(
                () -> this.query(polyLine.bounds().expand(extension).asEnvelope()),
                boundary -> boundary.intersects(JTS_POLYLINE_CONVERTER.convert(polyLine)));
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
        return this.boundariesHelper(() -> this.query(bound.asEnvelope(), true), polygon -> true);
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
        final List<Polygon> polygons = this.query(target.getEnvelopeInternal());

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
                    geometries(collection).forEach(point ->
                    {
                        setGeometryProperty(point, ISOCountryTag.KEY, containedCountryCode);
                        results.add((LineString) point);
                    });
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
        final List<Polygon> geometries = this.countryNameToBoundaryMap.get(countryName);
        if (geometries == null || geometries.isEmpty())
        {
            return null;
        }

        final List<CountryBoundary> boundaries = new ArrayList<>();
        boundaries.add(new CountryBoundary(countryName, JTS_MULTI_POLYGON_TO_POLYGON_CONVERTER
                .backwardConvert(geometries.stream().collect(Collectors.toSet()))));
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
        return new StringList(query(bound.asEnvelope(), true).stream()
                .map(polygon -> getGeometryProperty(polygon, ISOCountryTag.KEY))
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
     * @see #getCountryCodeISO3(Geometry, boolean)
     */
    public CountryCodeProperties getCountryCodeISO3(final Geometry geometry)
    {
        return this.getCountryCodeISO3(geometry, false, LINE_BUFFER);
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
        return this.getCountryCodeISO3(geometry, fastMode, LINE_BUFFER);
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

        final List<Polygon> polygons = this.query(target.getEnvelopeInternal()).stream()
                .filter(polygon -> polygon.intersects(target)).collect(Collectors.toList());
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
        return this.getCountryCodeISO3(JTS_POINT_CONVERTER.convert(location));
    }

    /**
     * @return all the countries represented by this {@link CountryBoundaryMap}
     */
    public Set<String> getLoadedCountries()
    {
        return this.countryNameToBoundaryMap.keySet();
    }

    /**
     * @return true if a {@link STRtree} grid index is available
     */
    public boolean hasGridIndex()
    {
        return this.gridIndex != null;
    }

    /**
     * Create a secondary spatial index with data intersecting the given
     * {@link com.vividsolutions.jts.geom.MultiPolygon}. This will accelerate performance of
     * geometry check by reducing unnecessary operations.
     *
     * @param area
     *            Area for grid index initialization
     */
    @SuppressWarnings("unchecked")
    public synchronized void initializeGridIndex(
            final com.vividsolutions.jts.geom.MultiPolygon area)
    {
        if (Objects.isNull(area))
        {
            logger.error("Given area is null. Skipping grid index initialization.");
            return;
        }

        this.gridIndexEnvelope = area.getEnvelopeInternal();
        final List<Polygon> boundaries = this.rawIndex.query(this.gridIndexEnvelope);
        final DynamicGridIndexBuilder builder = new DynamicGridIndexBuilder(boundaries,
                this.gridIndexEnvelope, this.rawIndex);
        this.gridIndex = builder.getIndex();
        logger.info("Grid index of size {} created.", this.gridIndex.size());
    }

    /**
     * Create a secondary spatial index for given countries.
     *
     * @param countries
     *            Country codes of countries to build index for
     * @see #initializeGridIndex(com.vividsolutions.jts.geom.MultiPolygon)
     */
    public void initializeGridIndex(final Set<String> countries)
    {
        logger.info("Building grid index for {}.", countries);
        MultiPolygon multiPolygon = new MultiPolygon(new MultiMap<>());
        for (final String countryCode : countries)
        {
            final List<CountryBoundary> boundaries = this.countryBoundary(countryCode);
            if (boundaries != null)
            {
                for (final CountryBoundary boundary : boundaries)
                {
                    multiPolygon = multiPolygon.concatenate(boundary.getBoundary());
                }
            }
        }
        final com.vividsolutions.jts.geom.MultiPolygon area = JTS_MULTI_POLYGON_TO_MULTI_POLYGON_CONVERTER
                .backwardConvert(multiPolygon);
        this.initializeGridIndex(area);

        // Verify that all countries had at least one grid index
        final Set<String> countriesWithoutGrids = new HashSet<>(countries);
        this.getCells().keySet().forEach(geometry -> countriesWithoutGrids
                .remove(getGeometryProperty(geometry, ISOCountryTag.KEY)));
        if (!countriesWithoutGrids.isEmpty())
        {
            throw new CoreException(
                    "Countries {} didn't have any grid index generated for them. "
                            + "Please check the input used for boundary generation.",
                    countriesWithoutGrids);
        }
    }

    public void setShouldAlwaysSlicePredicate(final Predicate<Taggable> shouldAlwaysSlicePredicate)
    {
        this.shouldAlwaysSlicePredicate = shouldAlwaysSlicePredicate;
    }

    public boolean shouldForceSlicing(final Taggable... source)
    {
        return source != null && source.length > 0 && this.shouldAlwaysSlicePredicate != null
                && this.shouldAlwaysSlicePredicate.test(source[0]);
    }

    public boolean shouldSkipSlicing(final List<Polygon> candidates, final Taggable... source)
    {
        return isSameCountry(candidates) && !shouldForceSlicing(source);
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
     * @param source
     *            An optional {@link Taggable} object representing the tags of the source for that
     *            geometry.
     * @return a list of geometry objects. If target doesn't cross any border then it contains only
     *         one item with country code assigned. If target cross border then slice it by the
     *         border line and assign country code for each piece. If a feature is not contained by
     *         any country boundary, it will be assigned to nearest country.
     * @throws TopologyException
     *             When the slicing could not be made.
     */
    public List<Geometry> slice(final long identifier, final Geometry geometry,
            final Taggable... source) throws TopologyException
    {
        if (Objects.isNull(geometry))
        {
            return null;
        }

        Geometry target = geometry;
        final List<Geometry> results = new ArrayList<>();
        List<Polygon> candidates = this.query(target.getEnvelopeInternal());

        // Performance improvement, if only one polygon returned no need to do any further
        // evaluation (except when geometry has to be sliced at all costs)
        // In this method, source contains only one element.
        if (shouldSkipSlicing(candidates, source))
        {
            final String countryCode = getGeometryProperty(candidates.get(0), ISOCountryTag.KEY);
            setGeometryProperty(target, ISOCountryTag.KEY, countryCode);
            addResult(target, results);
            return results;
        }

        // Remove duplicates
        candidates = candidates.stream().distinct().collect(Collectors.toList());
        final long numberCountries = numberCountries(candidates);

        // Avoid slicing across too many polygons for performance reasons
        if (candidates.size() > this.getPolygonSliceLimit())
        {
            RuntimeCounter.waySkipped(identifier);
            logger.warn("Skipping slicing way {} due to too many intersecting polygons [{}]",
                    identifier, candidates.size());
            return null;
        }

        RuntimeCounter.geometryChecked();
        boolean fullyMatched = false;
        boolean isWarned = false;
        final Time time = Time.now();

        if (numberCountries > MAXIMUM_EXPECTED_COUNTRIES_TO_SLICE_WITH)
        {
            logger.warn("Slicing way {} with {} countries.", identifier, numberCountries);
            if (logger.isTraceEnabled())
            {
                final Map<String, List<Polygon>> countries = candidates.stream().collect(Collectors
                        .groupingBy(polygon -> getGeometryProperty(polygon, ISOCountryTag.KEY)));
                countries.forEach((key, value) -> logger.trace("{} : {}", key, value.size()));
            }
        }

        // Check relation of target to all polygons
        final Iterator<Polygon> candidateIterator = candidates.iterator();
        while (candidateIterator.hasNext())
        {
            final Polygon candidate = candidateIterator.next();
            final String countryCode = getGeometryProperty(candidate, ISOCountryTag.KEY);
            if (Strings.isNullOrEmpty(countryCode))
            {
                logger.warn(
                        "Ignoring a candidate polygon from slicing, because it is missing country tag.");
                continue;
            }

            final IntersectionMatrix matrix;
            try
            {
                matrix = target.relate(candidate);
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
                setGeometryProperty(target, ISOCountryTag.KEY, countryCode);
                this.addResult(target, results);
                fullyMatched = true;
                return results;
            }

            // No intersection, remove from candidate list
            if (!matrix.isIntersects())
            {
                RuntimeCounter.geometryCheckedNoIntersect();
                candidateIterator.remove();
            }
        }

        // Performance: short circuit, if all intersected polygons in same country, skip cutting.
        // (except when geometry has to be sliced at all costs)
        if (shouldSkipSlicing(candidates, source))
        {
            final String countryCode = getGeometryProperty(candidates.get(0), ISOCountryTag.KEY);
            setGeometryProperty(target, ISOCountryTag.KEY, countryCode);
            this.addResult(target, results);
            return results;
        }

        // Sort intersecting polygons for consistent slicing
        Collections.sort(candidates, (final Polygon first, final Polygon second) ->
        {
            final int countryCodeComparison = getGeometryProperty(first, ISOCountryTag.KEY)
                    .compareTo(getGeometryProperty(second, ISOCountryTag.KEY));
            if (countryCodeComparison != 0)
            {
                return countryCodeComparison;
            }

            return first.compareTo(second);
        });

        // Start cut process
        for (final Polygon candidate : candidates)
        {
            RuntimeCounter.geometryCheckedIntersect();
            final Geometry clipped = target.intersection(candidate);

            // We don't want single point pieces
            if (clipped.getNumPoints() < 2)
            {
                continue;
            }

            // Add to the results
            final String countryCode = getGeometryProperty(candidate, ISOCountryTag.KEY);
            setGeometryProperty(clipped, ISOCountryTag.KEY, countryCode);
            this.addResult(clipped, results);

            // Update target to be what's left after clipping
            target = target.difference(candidate);
            if (target.getDimension() == 1 && target.getLength() < LINE_BUFFER
                    || target.getDimension() == 2 && target.getArea() < AREA_BUFFER
                            && new DiscreteHausdorffDistance(target, candidate)
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
                this.addResult(target, results);
            }
        }

        if (logger.isDebugEnabled() && time.untilNow().isMoreThan(Duration.ONE_MINUTE))
        {
            logger.debug("Took {} to slice way {}", time.untilNow(), identifier);
        }
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

            // Write grid index cells
            this.writeGridIndex(output);
        }
    }

    private void addCountry(final String country, final Polygon polygon)
    {
        this.countryNameToBoundaryMap.add(country, polygon);

        if (this.envelope.intersects(polygon.getEnvelopeInternal()))
        {
            this.rawIndex.insert(polygon.getEnvelopeInternal(), polygon);
        }
    }

    private void addResult(final Geometry geometry, final List<Geometry> results)
    {
        if (geometry instanceof GeometryCollection)
        {
            final GeometryCollection collection = (GeometryCollection) geometry;
            geometries(collection).forEach(part ->
            {
                getGeometryProperties(geometry).forEach((key, value) ->
                {
                    setGeometryProperty(part, key, value);
                });
                this.addResult(part, results);
            });
        }
        else if (geometry instanceof LineString || geometry instanceof Polygon)
        {
            results.add(geometry);
        }
        else
        {
            logger.error("Resulting slice was a {}, ignoring it.", geometry.toText());
        }
    }

    /**
     * Given a {@link Supplier}, retrieves {@link List} of {@link Polygon}s and applies given
     * {@link Predicate}, and returns results as {@link CountryBoundary}.
     *
     * @param supplier
     *            {@link Supplier} providing {@link List} of {@link Polygon}s
     * @param filter
     *            {@link Polygon} {@link Predicate} to be used as filter
     * @return Filtered {@link Polygon}s as {@link CountryBoundary}s
     */
    private List<CountryBoundary> boundariesHelper(final Supplier<List<Polygon>> supplier,
            final Predicate<Polygon> filter)
    {
        final MultiMap<String, Polygon> map = new MultiMap<>();
        final List<Polygon> geometry = supplier.get();
        geometry.stream().filter(filter).forEach(polygon ->
        {
            final String countryCode = getGeometryProperty(polygon, ISOCountryTag.KEY);
            if (countryCode == null)
            {
                logger.error("Null country code for {}", polygon.toString());
            }
            else
            {
                map.add(countryCode, polygon);
            }
        });

        return this.toCountryBoundaryList(map);
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
        return this.query(envelope, false);
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
            final Envelope bbox1 = new Envelope(Longitude.MIN_VALUE, envelope.getMinX(),
                    envelope.getMinY(), envelope.getMaxY());
            final Envelope bbox2 = new Envelope(envelope.getMaxX(), Longitude.MAX_VALUE,
                    envelope.getMinY(), envelope.getMaxY());
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
            if (this.gridIndex != null && this.gridIndexEnvelope.contains(bbox))
            {
                result.addAll(this.gridIndex.query(bbox));
            }
            if (result.isEmpty())
            {
                result.addAll(this.rawIndex.query(bbox));
            }
        }

        return result;
    }

    private List<CountryBoundary> toCountryBoundaryList(final MultiMap<String, Polygon> map)
    {
        final List<CountryBoundary> list = new ArrayList<>();
        for (final Map.Entry<String, List<Polygon>> entry : map.entrySet())
        {
            final String name = entry.getKey();
            final List<Polygon> polygons = entry.getValue();
            final MultiPolygon multiPolygon = JTS_MULTI_POLYGON_TO_POLYGON_CONVERTER
                    .backwardConvert(new HashSet<>(polygons));
            final CountryBoundary boundary = new CountryBoundary(name, multiPolygon);
            list.add(boundary);
        }
        return list;
    }

    private void writeCountryBoundaries(final BufferedWriter output) throws IOException
    {
        logger.info("Writing country boundaries to output");
        this.countryNameToBoundaryMap.forEach((country, polygons) ->
        {
            polygons.forEach(polygon ->
            {
                try
                {
                    output.write(country);
                    output.write(COUNTRY_BOUNDARY_DELIMITER);
                    output.write(WKT_WRITER.write(polygon));
                    output.write(LIST_SEPARATOR);
                    output.write(System.lineSeparator());
                }
                catch (final IOException e)
                {
                    throw new CoreException("Failed to write country boundaries.", e);
                }
            });

        });
    }

    private void writeGridIndex(final BufferedWriter writer)
    {
        if (this.gridIndex == null)
        {
            logger.warn("Skipping grid index serialization, because it is null.");
            return;
        }

        try
        {
            // Write envelope
            writer.write(GRID_ENVELOPE_DELIMITER);
            final Polygon envelopeAsPolygon = AbstractGridIndexBuilder.buildGeoBox(
                    this.gridIndexEnvelope.getMinX(), this.gridIndexEnvelope.getMaxX(),
                    this.gridIndexEnvelope.getMinY(), this.gridIndexEnvelope.getMaxY());
            writer.write(WKT_WRITER.write(envelopeAsPolygon));
            writer.write(System.lineSeparator());

            // Collect cells
            final MultiMap<Geometry, Envelope> polygonToCells = this.getCells();

            // Write parts
            polygonToCells.forEach((polygon, cells) ->
            {
                final String country = getGeometryProperty(polygon, ISOCountryTag.KEY);

                try
                {
                    // Write country
                    writer.write(GRID_INDEX_DELIMITER);
                    writer.write(country);

                    // Write polygon identifier
                    writer.write(GRID_INDEX_DELIMITER);
                    writer.write(getGeometryProperty(polygon, POLYGON_ID_KEY));

                    // Write cells
                    cells.forEach(cell ->
                    {
                        try
                        {
                            writer.write(GRID_INDEX_DELIMITER);
                            writer.write(WKT_WRITER
                                    .write(DynamicGridIndexBuilder.buildGeoBox(cell.getMinX(),
                                            cell.getMaxX(), cell.getMinY(), cell.getMaxY())));
                        }
                        catch (final Exception e)
                        {
                            throw new CoreException("Failed to write cell {} for {}", cell, country,
                                    e);
                        }

                    });

                    writer.write(System.lineSeparator());
                }
                catch (final Exception e)
                {
                    throw new CoreException("Failed to write cells for {}.", country, e);
                }

            });
        }
        catch (final IOException e)
        {
            throw new CoreException("Failed to write grid index.", e);
        }
    }
}
