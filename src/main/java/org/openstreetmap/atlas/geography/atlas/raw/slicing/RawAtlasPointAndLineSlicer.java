package org.openstreetmap.atlas.geography.atlas.raw.slicing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.Polygon;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.items.Line;
import org.openstreetmap.atlas.geography.atlas.items.Point;
import org.openstreetmap.atlas.geography.atlas.pbf.slicing.identifier.AbstractIdentifierFactory;
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

import com.google.common.base.Joiner;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.TopologyException;

/**
 * The {@link RawAtlasPointAndLineSlicer} consumes an un-sliced raw Atlas and produces an Atlas with
 * sliced {@link Point}s and {@link Line}s.
 *
 * @author mgostintsev
 */
public class RawAtlasPointAndLineSlicer extends RawAtlasSlicer
{
    private static final Logger logger = LoggerFactory.getLogger(RawAtlasPointAndLineSlicer.class);

    // The raw Atlas to slice
    private final Atlas rawAtlas;

    // Keeps track of all changes made during slicing
    private final SimpleChangeSet slicedPointAndLineChanges;

    // Keeps track of points marked for removal
    private final Set<Long> pointsMarkedForRemoval = new HashSet<>();

    public RawAtlasPointAndLineSlicer(final Set<String> countries,
            final CountryBoundaryMap countryBoundaryMap, final Atlas atlas)
    {
        super(countries, countryBoundaryMap, new CoordinateToNewPointMapping());
        this.slicedPointAndLineChanges = new SimpleChangeSet();
        this.rawAtlas = atlas;
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
        logger.info("Starting Point and Line Slicing for {}", getShardOrAtlasName());

        // Slice lines and points
        sliceLines();
        slicePoints();

        // Apply changes and rebuild the atlas with the changes before slicing relations
        final ChangeSetHandler simpleChangeBuilder = new SimpleChangeSetHandler(this.rawAtlas,
                this.slicedPointAndLineChanges);
        final Atlas atlasWithSlicedWaysAndPoints = simpleChangeBuilder.applyChanges();

        logger.info("Finished Point and Line Slicing for {} in {}", getShardOrAtlasName(),
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

        if (line.isClosed())
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
            logger.error("Invalid Geometry for line {}", lineIdentifier);
        }

        return result;
    }

    private String getShardOrAtlasName()
    {
        return this.rawAtlas.metaData().getShardName().orElse(this.rawAtlas.getName());
    }

    /**
     * Check if the {@link Geometry} should be filtered out based on the provided bound.
     *
     * @param geometry
     *            The {@link Geometry} to check.
     * @return {@code true} if the given geometry should be filtered out.
     */
    private boolean isOutsideWorkingBound(final Geometry geometry)
    {
        final String countryCode = CountryBoundaryMap.getGeometryProperty(geometry,
                ISOCountryTag.KEY);

        if (countryCode != null)
        {
            return getCountries() != null && !getCountries().isEmpty()
                    && !getCountries().contains(countryCode);
        }

        // Assume it's inside the bound
        return false;
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
        else if (slices.size() < AbstractIdentifierFactory.IDENTIFIER_SCALE)
        {
            // Used to generate identifiers for new points and lines

            final CountrySlicingIdentifierFactory lineIdentifierFactory = new CountrySlicingIdentifierFactory(
                    line.getIdentifier());

            final Map<String, Queue<Long>> lineIdsByCountry = new HashMap<>();
            final List<String> countryCodesForSlices = new ArrayList<>();

            // Before processing slices, sort them and record country codes. Then, alphabetize the
            // country codes and pre-generate identifiers assigned to the country codes. This way,
            // all lines are deterministically assigned the same identifier regardless of what order
            // the slices are returned in or which country is slicing the line.
            for (final Geometry slice : slices)
            {

                final String countryCode = CountryBoundaryMap.getGeometryProperty(slice,
                        ISOCountryTag.KEY);
                countryCodesForSlices.add(countryCode);
            }
            Collections.sort(countryCodesForSlices);

            countryCodesForSlices.forEach(countryCode ->
            {
                if (lineIdsByCountry.containsKey(countryCode))
                {
                    lineIdsByCountry.get(countryCode).add(lineIdentifierFactory.nextIdentifier());
                }
                else
                {
                    final LinkedList<Long> idsForCountry = new LinkedList<>();
                    idsForCountry.add(lineIdentifierFactory.nextIdentifier());
                    lineIdsByCountry.put(countryCode, idsForCountry);
                }
            });

            final List<TemporaryLine> createdLines = new ArrayList<>();
            try
            {
                for (final Geometry slice : slices)
                {

                    // Check if the slice is within the working bound and mark all points for this
                    // slice for removal if so
                    if (isOutsideWorkingBound(slice))
                    {
                        removeShapePointsFromFilteredSliced(slice);
                        continue;
                    }

                    final String countryCode = CountryBoundaryMap.getGeometryProperty(slice,
                            ISOCountryTag.KEY);

                    // Keep track of identifiers that form the geometry of the new line
                    final List<Long> newLineShapePoints = new ArrayList<>(slice.getNumPoints());

                    // Because country shapes do not share border, we are reducing the precision of
                    // the geometry
                    final Coordinate[] jtsSliceCoordinates = PRECISION_REDUCER
                            .edit(slice.getCoordinates(), slice);

                    final Long countryLineId = lineIdsByCountry.get(countryCode).poll();
                    final PointIdentifierFactory pointIdentifierFactory = new PointIdentifierFactory(
                            countryLineId);
                    for (final Coordinate coordinate : jtsSliceCoordinates)
                    {
                        final Location coordinateLocation = JTS_LOCATION_CONVERTER
                                .backwardConvert(coordinate);
                        final Iterable<Point> rawAtlasPointsAtSliceVertex = this.rawAtlas
                                .pointsAt(coordinateLocation);

                        // If there aren't any points at this precision in the raw Atlas, need to
                        // examine cache and possibly scale the coordinate to 6 digits of precision
                        if (Iterables.isEmpty(rawAtlasPointsAtSliceVertex))
                        {

                            // Check the cache for this coordinate -- if it already exists, we'll
                            // use it. Otherwise, scale to 6-digits and continue
                            if (getCoordinateToPointMapping().containsCoordinate(coordinate))
                            {
                                newLineShapePoints.add(getCoordinateToPointMapping()
                                        .getPointForCoordinate(coordinate));
                            }
                            else
                            {
                                final Location scaledLocation = JTS_LOCATION_CONVERTER
                                        .backwardConvert(getCoordinateToPointMapping()
                                                .getScaledCoordinate(coordinate));
                                final Iterable<Point> rawAtlasPointsAtScaledCoordinate = this.rawAtlas
                                        .pointsAt(scaledLocation);

                                // If the raw Atlas doesn't contain the 6-digit coordinate either,
                                // then we'll make a point at that location and update the cache and
                                // changeset with it. Otherwise, use the existing Atlas point by
                                // making sure it's kept and adding it to the line
                                if (Iterables.isEmpty(rawAtlasPointsAtScaledCoordinate))
                                {
                                    final Map<String, String> pointTags = createPointTags(
                                            scaledLocation, false);
                                    pointTags.put(SyntheticBoundaryNodeTag.KEY,
                                            SyntheticBoundaryNodeTag.YES.toString());
                                    final TemporaryPoint newPoint = createNewPoint(
                                            getCoordinateToPointMapping().getScaledCoordinate(
                                                    coordinate),
                                            pointIdentifierFactory, pointTags);
                                    getCoordinateToPointMapping().storeMapping(coordinate,
                                            newPoint.getIdentifier());
                                    newLineShapePoints.add(newPoint.getIdentifier());
                                    this.slicedPointAndLineChanges.createPoint(newPoint);
                                }
                                else
                                {
                                    addRawAtlasPointsToLine(scaledLocation,
                                            rawAtlasPointsAtScaledCoordinate, newLineShapePoints);
                                }
                            }
                        }
                        else
                        {
                            addRawAtlasPointsToLine(coordinateLocation, rawAtlasPointsAtSliceVertex,
                                    newLineShapePoints);
                        }
                    }

                    // Extract relevant tag values for this slice
                    final Map<String, String> lineTags = createLineTags(slice, line.getTags());

                    // Create and store the new line
                    final TemporaryLine createdLine = new TemporaryLine(countryLineId.longValue(),
                            newLineShapePoints, lineTags);
                    createdLines.add(createdLine);
                }

                // Update the change with the added and removed lines
                createdLines.forEach(
                        createdLine -> this.slicedPointAndLineChanges.createLine(createdLine));
                this.slicedPointAndLineChanges.createDeletedToCreatedMapping(line.getIdentifier(),
                        createdLines.stream().map(TemporaryLine::getIdentifier)
                                .collect(Collectors.toSet()));

                // Record a successful slice
                getStatistics().recordSlicedLine();
            }
            catch (final CoreException e)
            {
                logger.error(
                        "Country slicing exceeded maximum point identifier name space of {} for Line {}. "
                                + "It will be added as is, with two or more country codes.",
                        AbstractIdentifierFactory.IDENTIFIER_SCALE, line.getIdentifier(), e);

                // Update to use all country codes
                updateLineToHaveCountryCodesFromAllSlices(line, slices);
                getStatistics().recordSkippedLine();
            }
        }
        else
        {
            logger.error(
                    "Country slicing exceeded maximum line identifier name space of {} for Line {}. "
                            + "It will be added as is, with two or more country codes.",
                    AbstractIdentifierFactory.IDENTIFIER_SCALE, line.getIdentifier());

            // Update to use all country codes
            updateLineToHaveCountryCodesFromAllSlices(line, slices);
            getStatistics().recordSkippedLine();
        }
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
            final Iterable<Point> existingRawAtlasPoints = this.rawAtlas.pointsAt(location);
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
            logger.error("Topology Exception when slicing Line {}", line.getIdentifier(), e);
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
        processLineSlices(line, slices);
    }

    /**
     * Slices all the {@link Line}s in the given raw Atlas.
     */
    private void sliceLines()
    {
        StreamSupport.stream(this.rawAtlas.lines().spliterator(), true).forEach(this::sliceLine);
    }

    /**
     * Updates all points that haven't been assigned a country code after line-slicing. This
     * includes any stand-alone points (e.g. trees, barriers) or points that didn't get sliced
     * during line slicing.
     */
    private void slicePoints()
    {
        StreamSupport.stream(this.rawAtlas.points().spliterator(), true).forEach(point ->
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
            for (final Point point : this.rawAtlas.pointsAt(location))
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
        final String countryString = Joiner.on(",").join(allCountries);
        tags.put(ISOCountryTag.KEY, countryString);
        this.slicedPointAndLineChanges.updateLineTags(line.getIdentifier(), tags);
        updateLineShapePoints(line);
    }

}
