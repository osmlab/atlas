package org.openstreetmap.atlas.geography.atlas.raw.slicing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.TopologyException;
import org.locationtech.jts.operation.linemerge.LineMerger;
import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.Polygon;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.items.Line;
import org.openstreetmap.atlas.geography.atlas.items.Point;
import org.openstreetmap.atlas.geography.atlas.pbf.AtlasLoadingOption;
import org.openstreetmap.atlas.geography.atlas.pbf.slicing.identifier.CountrySlicingIdentifierFactory;
import org.openstreetmap.atlas.geography.atlas.pbf.slicing.identifier.PointIdentifierFactory;
import org.openstreetmap.atlas.geography.atlas.raw.slicing.changeset.ChangeSetHandler;
import org.openstreetmap.atlas.geography.atlas.raw.slicing.changeset.SimpleChangeSet;
import org.openstreetmap.atlas.geography.atlas.raw.slicing.changeset.SimpleChangeSetHandler;
import org.openstreetmap.atlas.geography.atlas.raw.temporary.TemporaryLine;
import org.openstreetmap.atlas.geography.atlas.raw.temporary.TemporaryPoint;
import org.openstreetmap.atlas.geography.boundary.CountryBoundaryMap;
import org.openstreetmap.atlas.tags.ISOCountryTag;
import org.openstreetmap.atlas.tags.SyntheticBoundaryNodeTag;
import org.openstreetmap.atlas.tags.SyntheticNearestNeighborCountryCodeTag;
import org.openstreetmap.atlas.utilities.collections.Iterables;
import org.openstreetmap.atlas.utilities.time.Time;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link RawAtlasPointAndLineSlicer} consumes an un-sliced raw Atlas and produces an Atlas with
 * sliced {@link Point}s and {@link Line}s.
 *
 * @author mgostintsev
 * @author samgass
 */
public class RawAtlasPointAndLineSlicer extends RawAtlasSlicer
{
    private static final Logger logger = LoggerFactory.getLogger(RawAtlasPointAndLineSlicer.class);

    // Keeps track of all changes made during slicing
    private final SimpleChangeSet slicedPointAndLineChanges;

    // Keeps track of points marked for removal
    private final Set<Long> pointsMarkedForRemoval = new HashSet<>();

    public RawAtlasPointAndLineSlicer(final Atlas atlas, final AtlasLoadingOption loadingOption)
    {
        super(loadingOption, new CoordinateToNewPointMapping(), atlas);
        this.slicedPointAndLineChanges = new SimpleChangeSet();
    }

    /**
     * Country-slice the {@link Point}s and {@link Line}s for the given Atlas.
     *
     * @return an {@link Atlas} with sliced {@link Point}s and {@link Line}s
     */
    @Override
    public Atlas slice()
    {
        final Time time = Time.now();
        logger.info("Starting Point and Line Slicing for Atlas {}", getShardOrAtlasName());

        // Slice lines and points
        sliceLines();
        slicePoints();

        // Apply changes and rebuild the atlas with the changes before slicing relations
        final ChangeSetHandler simpleChangeBuilder = new SimpleChangeSetHandler(getStartingAtlas(),
                this.slicedPointAndLineChanges);
        final Atlas atlasWithSlicedWaysAndPoints = simpleChangeBuilder.applyChanges();

        logger.info("Finished Point and Line Slicing for Atlas {} in {}", getShardOrAtlasName(),
                time.elapsedSince());
        getStatistics().summary();
        return atlasWithSlicedWaysAndPoints;
    }

    /**
     * Takes a location, a list of points from the raw Atlas at that location, and a list of point
     * IDs. Ensures the tags for the point are updated, ensures the points are not removed from the
     * raw Atlas, and adds the points the list of points for the line.
     *
     * @param location
     *            The location for the point(s)
     * @param rawAtlasPoints
     *            The points from the raw Atlas
     * @param line
     *            The List representing the points for the line to add the points to
     */
    private void addRawAtlasPointsToLine(final Location location,
            final Iterable<Point> rawAtlasPoints, final List<Long> line)
    {
        final Map<String, String> pointTags = createPointTags(location, true);
        for (final Point rawAtlasPoint : rawAtlasPoints)
        {
            this.pointsMarkedForRemoval.remove(rawAtlasPoint.getIdentifier());
            this.slicedPointAndLineChanges.updatePointTags(rawAtlasPoint.getIdentifier(),
                    pointTags);
            line.add(rawAtlasPoint.getIdentifier());
        }
    }

    /**
     * Converts the given {@link Line} into a JTS {@link Geometry} and slices it.
     *
     * @param line
     *            The {@link Line} to convert and slice
     * @return the resulting {@link Geometry} slices
     */
    private List<Geometry> convertToJtsGeometryAndSlice(final Line line)
    {
        List<Geometry> result;
        final long lineIdentifier = line.getIdentifier();

        // Create the JTS Geometry from Line
        Geometry geometry;

        if (isAtlasEdge(line))
        {
            geometry = JTS_POLYLINE_CONVERTER.convert(line.asPolyLine());
        }
        else if (line.isClosed())
        {
            // A Polygon
            geometry = JTS_POLYGON_CONVERTER.convert(new Polygon(line));
        }
        else
        {
            // A PolyLine
            geometry = JTS_POLYLINE_CONVERTER.convert(line.asPolyLine());
        }

        // Slice the JTS Geometry
        result = sliceGeometry(geometry, line);

        if ((result == null || result.isEmpty()) && line.isClosed())
        {
            // If we failed to slice an invalid Polygon (self-intersecting for example), let's try
            // to slice it as a PolyLine. Only if we cannot do that, then return an empty list.
            geometry = JTS_POLYLINE_CONVERTER.convert(line.asPolyLine());
            result = sliceGeometry(geometry, line);
        }

        if (result == null || result.isEmpty())
        {
            logger.error("Invalid Geometry for line {} for Atlas {}", lineIdentifier,
                    getShardOrAtlasName());
        }

        return result;
    }

    private long createNewPointIdentifier(final PointIdentifierFactory pointIdentifierFactory,
            final long lineIdentifier)
    {
        while (pointIdentifierFactory.hasMore())
        {
            final long identifier = pointIdentifierFactory.nextIdentifier();
            if (getStartingAtlas().point(identifier) == null)
            {
                return identifier;
            }
        }
        throw new CoreException(
                "Slicing Line {} exceeded maximum number {} of supported new Points for Atlas {}",
                pointIdentifierFactory.getIdentifierScale(), lineIdentifier, getShardOrAtlasName());
    }

    private boolean isOutsideWorkingBound(final Map<String, String> tags)
    {
        if (getCountries() != null && !getCountries().isEmpty())
        {
            final String tagValue = tags.get(ISOCountryTag.KEY);
            final String[] countryCodes = tagValue.split(ISOCountryTag.COUNTRY_DELIMITER);
            for (final String countryCode : countryCodes)
            {
                // Break if any one the countries is inside the bound
                if (getCountries().contains(countryCode))
                {
                    return false;
                }
            }

            // We've gone through all the countries and haven't seen one inside, it must be outside
            return true;
        }

        // Assume it's inside
        return false;
    }

    @SuppressWarnings("unchecked")
    private SortedMap<String, Collection<Geometry>> preprocessSlices(final List<Geometry> slices)
    {
        final SortedMap<String, Collection<Geometry>> processedSlices = new TreeMap<>();

        final Map<String, List<Geometry>> slicesByCountryCode = slices.stream()
                .collect(Collectors.groupingBy(geometry -> CountryBoundaryMap
                        .getGeometryProperty(geometry, ISOCountryTag.KEY)));

        slicesByCountryCode.keySet().forEach(countryCode ->
        {
            final SortedSet<Geometry> sortedSlices = new TreeSet<>();
            final List<Geometry> slicesForCountry = slicesByCountryCode.get(countryCode);

            final Map<String, String> mergedLineTags = new HashMap<>();
            mergedLineTags.put(ISOCountryTag.KEY, countryCode);
            slicesForCountry.forEach(
                    slice -> CountryBoundaryMap.getGeometryProperties(slice).forEach((key, value) ->
                    {
                        if (!mergedLineTags.containsKey(key))
                        {
                            mergedLineTags.put(key, value);
                        }
                    }));

            final LineMerger merger = new LineMerger();
            merger.add(slicesForCountry);
            merger.getMergedLineStrings()
                    .forEach(geometry -> mergedLineTags.forEach((key, value) -> CountryBoundaryMap
                            .setGeometryProperty((Geometry) geometry, key, value)));
            sortedSlices.addAll(merger.getMergedLineStrings());
            processedSlices.put(countryCode, sortedSlices);
        });
        return processedSlices;
    }

    /**
     * Processes each slice by updating corresponding tags ({@link ISOCountryTag},
     * {@link SyntheticNearestNeighborCountryCodeTag}, {@link SyntheticBoundaryNodeTag} and creating
     * {@link SimpleChangeSet}s to keep track of created, updated and deleted {@link Point}s and
     * {@link Line}s.
     *
     * @param line
     *            The {@link Line} that was sliced
     * @param slices
     *            The resulting {@link Geometry} slices
     */
    private void processLineSlices(final Line line, final List<Geometry> slices)
    {
        // Used to generate identifiers for new points and lines
        final CountrySlicingIdentifierFactory lineIdentifierFactory = new CountrySlicingIdentifierFactory(
                line.getIdentifier());
        final PointIdentifierFactory pointIdentifierFactory = new PointIdentifierFactory(
                line.getIdentifier());
        final List<TemporaryLine> createdLines = new ArrayList<>();
        slices.forEach(slice ->
        {
            if (slice instanceof org.locationtech.jts.geom.Polygon)
            {
                final org.locationtech.jts.geom.Polygon polygonForSlice = (org.locationtech.jts.geom.Polygon) slice;
                if (polygonForSlice.getNumInteriorRing() > 0)
                {
                    logger.warn("Line {} had a multipolygon result for slicing for Atlas {}!",
                            line.getIdentifier(), getShardOrAtlasName());
                }
            }
        });

        final SortedMap<String, Collection<Geometry>> mergedSlices = preprocessSlices(slices);
        mergedSlices.keySet().forEach(countryCode ->
        {
            for (final Geometry slice : mergedSlices.get(countryCode))
            {
                // Check if the slice is within the working bound and mark all points for this
                // slice for removal if so
                if (isOutsideWorkingBound(slice))
                {
                    // increment the identifier factory, but don't bother slicing the line
                    // this guarantees deterministic id assignment regardless of which countries
                    // are being sliced
                    lineIdentifierFactory.nextIdentifier();
                    removeShapePointsFromFilteredSliced(slice);
                }
                else
                {
                    final long lineSliceIdentifier = lineIdentifierFactory.nextIdentifier();
                    final List<Long> newLineShapePoints = processSlice(slice,
                            pointIdentifierFactory, line);
                    // Extract relevant tag values for this slice
                    final Map<String, String> lineTags = createLineTags(slice, line.getTags());
                    createdLines.add(
                            new TemporaryLine(lineSliceIdentifier, newLineShapePoints, lineTags));
                }
            }
        });
        // Update the change with the added and removed lines
        createdLines.forEach(this.slicedPointAndLineChanges::createLine);
        this.slicedPointAndLineChanges.createDeletedToCreatedMapping(line.getIdentifier(),
                createdLines.stream().map(TemporaryLine::getIdentifier)
                        .collect(Collectors.toSet()));

        // Record a successful slice
        getStatistics().recordSlicedLine();
    }

    private List<Long> processSlice(final Geometry slice,
            final PointIdentifierFactory pointIdentifierFactory, final Line line)
    {

        // Keep track of identifiers that form the geometry of the new line
        final List<Long> newLineShapePoints = new ArrayList<>(slice.getNumPoints());

        // Because country shapes do not share border, we are reducing the precision of
        // the geometry
        final Coordinate[] jtsSliceCoordinates = PRECISION_REDUCER.edit(slice.getCoordinates(),
                slice);

        for (final Coordinate coordinate : jtsSliceCoordinates)
        {
            final Location coordinateLocation = JTS_LOCATION_CONVERTER.backwardConvert(coordinate);
            final Iterable<Point> rawAtlasPointsAtSliceVertex = getStartingAtlas()
                    .pointsAt(coordinateLocation);

            // If there aren't any points at this precision in the raw Atlas, need to
            // examine cache and possibly scale the coordinate to 6 digits of precision
            if (Iterables.isEmpty(rawAtlasPointsAtSliceVertex))
            {
                // Check the cache for this coordinate -- if it already exists, we'll
                // use it. Otherwise, scale to 6-digits and continue
                if (getCoordinateToPointMapping().containsCoordinate(coordinate))
                {
                    newLineShapePoints
                            .add(getCoordinateToPointMapping().getPointForCoordinate(coordinate));
                }
                else
                {
                    final Location scaledLocation = JTS_LOCATION_CONVERTER.backwardConvert(
                            getCoordinateToPointMapping().getScaledCoordinate(coordinate));
                    final Iterable<Point> rawAtlasPointsAtScaledCoordinate = getStartingAtlas()
                            .pointsAt(scaledLocation);

                    // If the raw Atlas doesn't contain the 6-digit coordinate either,
                    // then we'll make a point at that location and update the cache and
                    // changeset with it. Otherwise, use the existing Atlas point by
                    // making sure it's kept and adding it to the line
                    if (Iterables.isEmpty(rawAtlasPointsAtScaledCoordinate))
                    {
                        final Map<String, String> pointTags = createPointTags(scaledLocation,
                                false);
                        pointTags.put(SyntheticBoundaryNodeTag.KEY,
                                SyntheticBoundaryNodeTag.YES.toString());
                        final long pointIdentifier = createNewPointIdentifier(
                                pointIdentifierFactory, line.getIdentifier());
                        final TemporaryPoint newPoint = createNewPoint(
                                getCoordinateToPointMapping().getScaledCoordinate(coordinate),
                                pointIdentifier, pointTags);
                        getCoordinateToPointMapping().storeMapping(coordinate,
                                newPoint.getIdentifier());
                        newLineShapePoints.add(newPoint.getIdentifier());
                        this.slicedPointAndLineChanges.createPoint(newPoint);
                    }
                    else
                    {
                        addRawAtlasPointsToLine(scaledLocation, rawAtlasPointsAtScaledCoordinate,
                                newLineShapePoints);
                    }
                }
            }
            else
            {
                addRawAtlasPointsToLine(coordinateLocation, rawAtlasPointsAtSliceVertex,
                        newLineShapePoints);
            }
        }
        return newLineShapePoints;
    }

    /**
     * Given a slice, that is outside the working bound, mark all shape points for this slice for
     * removal from the final atlas. Since we are removing the slice, we don't need its shape
     * points.
     *
     * @param slice
     *            The {@link Geometry} slice being filtered
     */
    private void removeShapePointsFromFilteredSliced(final Geometry slice)
    {
        final Coordinate[] jtsSliceCoordinates = slice.getCoordinates();
        for (final Coordinate coordinate : jtsSliceCoordinates)
        {
            final Location location = JTS_LOCATION_CONVERTER.backwardConvert(coordinate);
            final Iterable<Point> existingRawAtlasPoints = getStartingAtlas().pointsAt(location);
            existingRawAtlasPoints
                    .forEach(point -> this.pointsMarkedForRemoval.add(point.getIdentifier()));
        }
    }

    /**
     * Slices the given {@link Geometry} into multiple geometries and returns them as a list.
     *
     * @param geometry
     *            The {@link Geometry} to slice
     * @param line
     *            The {@link Line} being sliced
     * @return a list of {@link Geometry} slices
     */
    private List<Geometry> sliceGeometry(final Geometry geometry, final Line line)
    {
        try
        {
            return getCountryBoundaryMap().slice(line.getIdentifier(), geometry, line);
        }
        catch (final TopologyException e)
        {
            logger.error("Topology Exception when slicing Line {} for Atlas {}",
                    line.getIdentifier(), getShardOrAtlasName(), e);
            return Collections.emptyList();
        }
    }

    /**
     * Converts the given {@link Line} to a JTS {@link Geometry}, slices the geometry and updates
     * all corresponding {@link Point}s and {@link Line}s in the given raw Atlas.
     *
     * @param line
     *            The {@link Line} to slice
     */
    private void sliceLine(final Line line)
    {
        getStatistics().recordProcessedLine();
        final List<Geometry> slices = convertToJtsGeometryAndSlice(line);
        if (slices == null || slices.isEmpty())
        {
            // No slices generated or an error in slicing, create missing country code
            final Map<String, String> tags = new HashMap<>();
            tags.put(ISOCountryTag.KEY, ISOCountryTag.COUNTRY_MISSING);
            this.slicedPointAndLineChanges.updateLineTags(line.getIdentifier(), tags);
            updateLineShapePoints(line);
        }
        else if (slicesBelongToSingleCountry(slices)
                && !getCountryBoundaryMap().shouldForceSlicing(line))
        {
            // This line belongs to a single country, check to make sure it's the right one
            if (isOutsideWorkingBound(slices.get(0)))
            {
                this.slicedPointAndLineChanges.createDeletedToCreatedMapping(line.getIdentifier(),
                        Collections.emptySet());
            }
            else
            {
                this.slicedPointAndLineChanges.updateLineTags(line.getIdentifier(),
                        createLineTags(slices.get(0), line.getTags()));
                updateLineShapePoints(line);
            }
        }
        else if (slices.size() < CountrySlicingIdentifierFactory.IDENTIFIER_SCALE_DEFAULT)
        {
            processLineSlices(line, slices);
        }
        else
        {
            logger.error(
                    "Country slicing exceeded maximum line identifier name space of {} for Line {} for Atlas {}. It will be added as is, with two or more country codes.",
                    CountrySlicingIdentifierFactory.IDENTIFIER_SCALE_DEFAULT, line.getIdentifier(),
                    getShardOrAtlasName());
            // Update to use all country codes
            updateLineToHaveCountryCodesFromAllSlices(line, slices);
            getStatistics().recordSkippedLine();
        }
    }

    /**
     * Slices all the {@link Line}s in the given raw Atlas.
     */
    private void sliceLines()
    {
        StreamSupport.stream(getStartingAtlas().lines().spliterator(), true)
                .forEach(this::sliceLine);
    }

    /**
     * Updates all points that haven't been assigned a country code after line-slicing. This
     * includes any stand-alone points (e.g. trees, barriers) or points that didn't get sliced
     * during line slicing.
     */
    private void slicePoints()
    {
        StreamSupport.stream(getStartingAtlas().points().spliterator(), true).forEach(point ->
        {
            final long pointIdentifier = point.getIdentifier();

            // Only update points that haven't been assigned a country code after way slicing or
            // those marked for removal
            if (!this.slicedPointAndLineChanges.getUpdatedPointTags().containsKey(pointIdentifier)
                    && !this.pointsMarkedForRemoval.contains(pointIdentifier))
            {
                getStatistics().recordProcessedPoint();
                final Map<String, String> updatedTags = createPointTags(point.getLocation(), true);
                if (isOutsideWorkingBound(updatedTags))
                {
                    // This point is outside our boundary, remove it
                    this.slicedPointAndLineChanges.deletePoint(pointIdentifier);
                }
                else
                {
                    // Update the point tags
                    this.slicedPointAndLineChanges.updatePointTags(pointIdentifier, updatedTags);
                }
            }
        });

        // Update all removed points
        this.pointsMarkedForRemoval.forEach(this.slicedPointAndLineChanges::deletePoint);
    }

    /**
     * Checks if there is a single slice or if all of the slices are in the same country.
     *
     * @param slices
     *            The sliced pieces to check
     * @return {@code true} if the slices for this line are all part of the same country
     */
    private boolean slicesBelongToSingleCountry(final List<Geometry> slices)
    {
        return slices.size() == 1 || CountryBoundaryMap.isSameCountry(slices);
    }

    /**
     * Updates all of the given {@link Line}'s shape points' tags. Under the covers, uses
     * {@link CountryBoundaryMap} spatial index call.
     *
     * @param line
     *            The {@link Line} whose shape points to update
     */
    private void updateLineShapePoints(final Line line)
    {
        for (final Location location : line.asPolyLine())
        {
            for (final Point point : getStartingAtlas().pointsAt(location))
            {
                getStatistics().recordProcessedPoint();
                this.slicedPointAndLineChanges.updatePointTags(point.getIdentifier(),
                        createPointTags(location, true));
            }
        }
    }

    /**
     * For {@link Line}s that could not be cut (because of too many created points or too many
     * created line segments), we will gather the country codes for all the slices and assign the
     * multiple-country code value to the un-sliced line. As a result, the same un-sliced line will
     * appear in Atlas files for all spanning countries.
     *
     * @param line
     *            The {@link Line} in question
     * @param slices
     *            The {@link Geometry} slices for the given {@link Line}
     */
    private void updateLineToHaveCountryCodesFromAllSlices(final Line line,
            final List<Geometry> slices)
    {
        final Map<String, String> tags = new HashMap<>();
        final Set<String> allCountries = slices.stream().map(
                geometry -> CountryBoundaryMap.getGeometryProperty(geometry, ISOCountryTag.KEY))
                .collect(Collectors.toCollection(TreeSet::new));
        final String countryString = String.join(",", allCountries);
        tags.put(ISOCountryTag.KEY, countryString);
        this.slicedPointAndLineChanges.updateLineTags(line.getIdentifier(), tags);
        updateLineShapePoints(line);
    }
}
