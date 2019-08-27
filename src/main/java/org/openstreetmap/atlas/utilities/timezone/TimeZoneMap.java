package org.openstreetmap.atlas.utilities.timezone;

import java.io.IOException;
import java.net.URL;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TimeZone;
import java.util.regex.Pattern;

import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.feature.FeatureIterator;
import org.opengis.feature.Feature;
import org.opengis.feature.Property;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.geometry.BoundingBox;
import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.Latitude;
import org.openstreetmap.atlas.geography.Located;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.Longitude;
import org.openstreetmap.atlas.geography.Polygon;
import org.openstreetmap.atlas.geography.Rectangle;
import org.openstreetmap.atlas.geography.index.JtsSpatialIndex;
import org.openstreetmap.atlas.geography.index.RTree;
import org.openstreetmap.atlas.utilities.conversion.Converter;
import org.openstreetmap.atlas.utilities.scalars.Distance;
import org.openstreetmap.atlas.utilities.scalars.Surface;
import org.openstreetmap.atlas.utilities.statistic.storeless.CounterWithStatistic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This {@link TimeZoneMap} can load partial time zone shape files provided by
 * <a href= "http://efele.net/maps/tz/world/" >efele.net</a> into spatial index, and then supports
 * thread-safe {@link TimeZone} queries with {@link Location}.
 *
 * @author tony
 */
public class TimeZoneMap implements Located
{
    /**
     * Convert from {@link Feature} (a record of time zone shape file) to a {@link TimeZoneBoundary}
     * object. If the {@link Feature} is a multipolygon, just store the outer one, there will be
     * another {@link Feature} for inner polygon
     */
    private static class TimeZoneBoundaryConverter implements Converter<Feature, TimeZoneBoundary>
    {
        private static final int PREFIX_LENGTH = "MULTIPOLYGON (((".length();
        private static final int SUFFIX_LENGTH = ")))".length();
        private static final Pattern MULTI_POLYGON_SEPARATOR = Pattern.compile("\\), \\(");
        private static final Pattern POINT_SEPARATOR = Pattern.compile(", ");
        private static final Pattern LATITUDE_LONGITUDE_SEPARATOR = Pattern.compile(" ");

        @Override
        public TimeZoneBoundary convert(final Feature feature)
        {
            // convert time zone
            final Collection<Property> timeZoneIdentifiers = feature.getProperties("TZID");
            if (timeZoneIdentifiers.isEmpty() || timeZoneIdentifiers.size() != 1)
            {
                logger.error("feature {} has more than two time zone identifiers", feature);
            }
            final TimeZone timeZone = TimeZone
                    .getTimeZone(timeZoneIdentifiers.iterator().next().getValue().toString());

            // convert geometry
            final Collection<Property> geometries = feature.getProperties("the_geom");
            if (geometries.isEmpty() || geometries.size() != 1)
            {
                logger.error("feature {} has more than two geometries", feature);
            }
            final String geometry = geometries.iterator().next().getValue().toString();

            // there could be multipolygons in one record, only store the outer polygon (first one)
            final String[] multiPolygons = MULTI_POLYGON_SEPARATOR
                    .split(geometry.substring(PREFIX_LENGTH, geometry.length() - SUFFIX_LENGTH));
            final String[] points = POINT_SEPARATOR.split(multiPolygons[0]);
            final List<Location> locations = new ArrayList<>(points.length);
            for (int j = 0; j < points.length; j++)
            {
                final String[] coordinate = LATITUDE_LONGITUDE_SEPARATOR.split(points[j]);
                locations.add(new Location(Latitude.degrees(Double.parseDouble(coordinate[1])),
                        Longitude.degrees(Double.parseDouble(coordinate[0]))));
            }
            return new TimeZoneBoundary(timeZone, new Polygon(locations));
        }
    }

    /**
     * Use two times sea territory distance as a buffer for extra long bridges, like the one near
     * Shanghai
     */
    private static final Distance SEA_TERRITORY_ZONE_WITH_BUFFER = Distance.SEA_TERRITORY_ZONE
            .scaleBy(2);
    private static final Logger logger = LoggerFactory.getLogger(TimeZoneMap.class);
    private static final int DEGREES_PER_TIME_ZONE = 15;
    private static final int LOG_PRINT_FREQUENCY = 1_000;

    private final JtsSpatialIndex<TimeZoneBoundary> index = new RTree<>();
    private final Rectangle bound;

    public TimeZoneMap()
    {
        this(Rectangle.MAXIMUM);
    }

    public TimeZoneMap(final Rectangle bound)
    {
        this.bound = bound;
        try
        {
            loadTimeZoneBoundaries();
        }
        catch (final IOException e)
        {
            logger.error("Errors happened when loading timezone", e);
        }
    }

    /**
     * @return All available time zone boundaries, note this doesn't include ocean strips
     */
    public List<TimeZoneBoundary> allTimeZoneBoundaries()
    {
        return this.index.get(Rectangle.MAXIMUM);
    }

    @Override
    public Rectangle bounds()
    {
        return this.bound;
    }

    /**
     * Main API for time zone query
     *
     * @param location
     *            The location to query
     * @return the specified TimeZone, or the GMT zone if in the middle of the sea
     */
    public synchronized TimeZone timeZone(final Location location)
    {
        List<TimeZoneBoundary> boundaries = this.index.get(location.bounds(),
                boundary -> boundary.getPolygon().fullyGeometricallyEncloses(location));

        // if find polygons contain location
        if (!boundaries.isEmpty())
        {
            final TimeZoneBoundary smallestBoundary = smallest(boundaries);
            if (smallestBoundary != null)
            {
                return smallestBoundary.getTimeZone();
            }
            else
            {
                throw new CoreException("Could not find smallest time zone boundary.");
            }
        }

        // expand bounding box by 12 * 2 nautical miles and search again
        final Rectangle larger = location.bounds().expand(SEA_TERRITORY_ZONE_WITH_BUFFER);
        boundaries = this.index.get(larger);

        // return any found one
        if (!boundaries.isEmpty())
        {
            return boundaries.get(0).getTimeZone();
        }

        // the location is in the middle of the sea, use normalized time zone (15 degree per zone)
        final int offset = (int) location.getLongitude().asDegrees() / DEGREES_PER_TIME_ZONE;
        return TimeZone.getTimeZone(ZoneOffset.of(offset > 0 ? "+" + offset : "" + offset));
    }

    /**
     * Partial loading time zone boundaries
     *
     * @throws IOException
     */
    private void loadTimeZoneBoundaries() throws IOException
    {
        final CounterWithStatistic counter = new CounterWithStatistic(logger);
        counter.setLogPrintFrequency(LOG_PRINT_FREQUENCY);
        final URL url = TimeZoneMap.class.getResource("tz_world.shp");
        final FileDataStore store = FileDataStoreFinder.getDataStore(url);
        final TimeZoneBoundaryConverter converter = new TimeZoneBoundaryConverter();
        final FeatureIterator<SimpleFeature> iterator = store.getFeatureSource().getFeatures()
                .features();
        try
        {
            while (iterator.hasNext())
            {
                final Feature feature = iterator.next();
                final BoundingBox boundingBox = feature.getBounds();
                final Rectangle featureBound = Rectangle.forLocations(
                        new Location(Latitude.degrees(boundingBox.getMinY()),
                                Longitude.degrees(boundingBox.getMinX())),
                        new Location(Latitude.degrees(boundingBox.getMaxY()),
                                Longitude.degrees(boundingBox.getMaxX())));
                // only load overlapped boundaries
                if (this.bound.overlaps(featureBound))
                {
                    final TimeZoneBoundary boundary = converter.convert(feature);
                    this.index.add(boundary.bounds(), boundary);
                    counter.increment();
                }
            }
        }
        finally
        {
            iterator.close();
            store.dispose();
        }

        counter.summary();
        logger.info("index size is {}", this.index.size());
    }

    /**
     * @return the boundary with minimum area (here we suppose the boundary with minimum area is the
     *         smallest boundary )
     */
    private TimeZoneBoundary smallest(final List<TimeZoneBoundary> boundaries)
    {
        if (boundaries.size() == 1)
        {
            return boundaries.get(0);
        }
        else
        {
            TimeZoneBoundary target = null;
            Surface minimum = Surface.MAXIMUM;
            for (final TimeZoneBoundary boundary : boundaries)
            {
                final Surface area = boundary.getPolygon().bounds().surface();
                if (area.isLessThan(minimum))
                {
                    minimum = area;
                    target = boundary;
                }
            }
            return target;
        }
    }
}
