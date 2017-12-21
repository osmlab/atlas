package org.openstreetmap.atlas.geography.atlas.raw.slicing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.Polygon;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.items.Line;
import org.openstreetmap.atlas.geography.atlas.items.Point;
import org.openstreetmap.atlas.geography.atlas.pbf.slicing.identifier.AbstractIdentifierFactory;
import org.openstreetmap.atlas.geography.atlas.pbf.slicing.identifier.CountrySlicingIdentifierFactory;
import org.openstreetmap.atlas.geography.atlas.raw.slicing.changeset.ChangeSetHandler;
import org.openstreetmap.atlas.geography.atlas.raw.slicing.changeset.SimpleChangeSet;
import org.openstreetmap.atlas.geography.atlas.raw.slicing.changeset.SimpleChangeSetHandler;
import org.openstreetmap.atlas.geography.atlas.raw.slicing.temporary.TemporaryLine;
import org.openstreetmap.atlas.geography.atlas.raw.slicing.temporary.TemporaryPoint;
import org.openstreetmap.atlas.geography.boundary.CountryBoundaryMap;
import org.openstreetmap.atlas.locale.IsoCountry;
import org.openstreetmap.atlas.tags.ISOCountryTag;
import org.openstreetmap.atlas.tags.SyntheticBoundaryNodeTag;
import org.openstreetmap.atlas.tags.SyntheticNearestNeighborCountryCodeTag;
import org.openstreetmap.atlas.utilities.collections.Iterables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    public RawAtlasPointAndLineSlicer(final Set<IsoCountry> countries,
            final CountryBoundaryMap countryBoundaryMap, final Atlas atlas,
            final SimpleChangeSet changeSet, final CoordinateToNewPointMapping newPointCoordinates)
    {
        super(countries, countryBoundaryMap, newPointCoordinates);
        this.rawAtlas = atlas;
        this.slicedPointAndLineChanges = changeSet;
    }

    /**
     * Country-slice the {@link Point}s and {@link Line}s for the given Atlas.
     *
     * @return an {@link Atlas} with sliced {@link Point}s and {@link Line}s
     */
    @Override
    public Atlas slice()
    {
        logger.info("Starting slicing Lines and Points for Raw Atlas {}", this.rawAtlas.getName());

        // Slice lines and points
        sliceLines();
        slicePoints();

        // Apply changes and rebuild the atlas with the changes before slicing relations
        final ChangeSetHandler simpleChangeBuilder = new SimpleChangeSetHandler(this.rawAtlas,
                this.slicedPointAndLineChanges);
        final Atlas atlasWithSlicedWaysAndPoints = simpleChangeBuilder.applyChanges();

        logger.info("Finished slicing Lines and Points for Raw Atlas {}", this.rawAtlas.getName());

        getStatistics().summary();
        return atlasWithSlicedWaysAndPoints;
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
        result = sliceGeometry(geometry, lineIdentifier);
        if (result.isEmpty() && line.isClosed())
        {
            // If we failed to slice an invalid Polygon (self-intersecting for example), let's try
            // to slice it as a PolyLine. Only if we cannot do that, then return an empty list.
            geometry = JTS_POLYLINE_CONVERTER.convert(line.asPolyLine());
            result = sliceGeometry(geometry, lineIdentifier);
        }

        if (result.isEmpty())
        {
            logger.error("Invalid Geometry for line {}", lineIdentifier);
        }

        return result;
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
        final Optional<IsoCountry> countryCode = IsoCountry.forCountryCode(
                CountryBoundaryMap.getGeometryProperty(geometry, ISOCountryTag.KEY));
        if (countryCode.isPresent())
        {
            return getCountries() != null && !getCountries().isEmpty()
                    && !getCountries().contains(countryCode.get());
        }

        // Assume it's inside the bound
        return false;
    }

    /**
     * Checks if there is a single slice or if all of the slices are in the same country AND the
     * line being sliced isn't a feature that extends into the water, such as a Ferry or Pier.
     *
     * @param line
     *            The {@link Line} that was sliced
     * @param slices
     *            The resulting sliced pieces
     * @return {@code true} if the slices for this line are all part of the same country
     */
    private boolean lineBelongsToSingleCountry(final Line line, final List<Geometry> slices)
    {
        // TODO - this is an optimization that hides the corner case of not slicing any pier or
        // ferry that extends into the ocean. Because the ocean isn't viewed as another country, the
        // pier and ferries are not sliced at the country boundary and ocean. This should be fixed
        // for consistency issues.
        return slices.size() == 1 || CountryBoundaryMap.isSameCountry(slices);
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
        else if (lineBelongsToSingleCountry(line, slices))
        {
            // This line belongs to a single country
            this.slicedPointAndLineChanges.updateLineTags(line.getIdentifier(),
                    createLineTags(slices.get(0), line.getTags()));
            updateLineShapePoints(line);
        }
        else if (slices.size() < AbstractIdentifierFactory.IDENTIFIER_SCALE)
        {
            // Used to generate identifiers for new points and lines
            final CountrySlicingIdentifierFactory lineIdentifierFactory = new CountrySlicingIdentifierFactory(
                    line.getIdentifier());
            final CountrySlicingIdentifierFactory pointIdentifierFactory = new CountrySlicingIdentifierFactory(
                    line.getIdentifier());

            final List<TemporaryLine> createdLines = new ArrayList<>();

            try
            {
                for (final Geometry slice : slices)
                {
                    // Check if the slice is within the working bound
                    if (isOutsideWorkingBound(slice))
                    {
                        continue;
                    }

                    // Keep track of identifiers that form the geometry of the new line
                    final List<Long> newLineShapePoints = new ArrayList<>(slice.getNumPoints());

                    final Coordinate[] jtsSliceCoordinates = slice.getCoordinates();
                    for (final Coordinate coordinate : jtsSliceCoordinates)
                    {
                        // Because country shapes do not share border, we are rounding coordinate
                        // first to consider very close nodes as one
                        roundCoordinate(coordinate);

                        if (getCoordinateToPointMapping().containsCoordinate(coordinate))
                        {
                            // A new point was already created for this coordinate. Look it up and
                            // use it for the line we're creating
                            newLineShapePoints.add(getCoordinateToPointMapping()
                                    .getPointForCoordinate(coordinate));
                        }
                        else
                        {
                            // The point in the original Raw Atlas or we need to create a new one
                            final Location coordinateLocation = JTS_LOCATION_CONVERTER
                                    .backwardConvert(coordinate);
                            final Iterable<Point> rawAtlasPointsAtSliceVertex = this.rawAtlas
                                    .pointsAt(coordinateLocation);

                            if (Iterables.isEmpty(rawAtlasPointsAtSliceVertex))
                            {
                                // Grab the country code tags for this point
                                final Map<String, String> pointTags = createPointTags(
                                        coordinateLocation, false);

                                // Need to create a new point
                                final TemporaryPoint newPoint = createNewPoint(coordinate,
                                        pointIdentifierFactory, pointTags);

                                // Store coordinate to avoid creating duplicate Points
                                getCoordinateToPointMapping().storeMapping(coordinate,
                                        newPoint.getIdentifier());

                                // Store this point to reconstruct the Line geometry
                                newLineShapePoints.add(newPoint.getIdentifier());

                                // Save the Point to add to the rebuilt atlas
                                this.slicedPointAndLineChanges.createPoint(newPoint);
                            }
                            else
                            {
                                // Grab the country code tags for this point
                                final Map<String, String> pointTags = createPointTags(
                                        coordinateLocation, true);

                                // There is at least one point at this location in the raw Atlas.
                                // Update all existing points to have the country code.
                                for (final Point rawAtlasPoint : rawAtlasPointsAtSliceVertex)
                                {
                                    // Update the country codes
                                    this.slicedPointAndLineChanges.updatePointTags(
                                            rawAtlasPoint.getIdentifier(), pointTags);

                                    // Add all point identifiers to make up the new Line
                                    newLineShapePoints.add(rawAtlasPoint.getIdentifier());
                                }
                            }
                        }
                    }

                    // Extract relevant tag values for this slice
                    final Map<String, String> lineTags = createLineTags(slice, line.getTags());

                    // Create and store the new line
                    final TemporaryLine createdLine = new TemporaryLine(
                            lineIdentifierFactory.nextIdentifier(), newLineShapePoints, lineTags);
                    createdLines.add(createdLine);
                }

                // Update the change with the added and removed lines
                createdLines.forEach(
                        createdLine -> this.slicedPointAndLineChanges.createLine(createdLine));

                this.slicedPointAndLineChanges.deleteLine(line.getIdentifier());
                this.slicedPointAndLineChanges.createDeletedToCreatedMapping(line.getIdentifier(),
                        createdLines.stream().map(TemporaryLine::getIdentifier)
                                .collect(Collectors.toSet()));

                // Record a successful slice
                getStatistics().recordSlicedLine();
            }
            catch (final CoreException e)
            {
                // TODO - Consider shifting to a 4 digit name space for identifiers
                logger.error(
                        "Country slicing exceeded maximum point identifier name space of {} for Line {}",
                        AbstractIdentifierFactory.IDENTIFIER_SCALE, line.getIdentifier(), e);
                getStatistics().recordSkippedLine();
            }
        }
        else
        {
            // TODO - Consider expanding to a 4 digit name space for identifiers
            logger.error(
                    "Country slicing exceeded maximum line identifier name space of {} for Line {}",
                    AbstractIdentifierFactory.IDENTIFIER_SCALE, line.getIdentifier());
            getStatistics().recordSkippedLine();
        }
    }

    /**
     * Slices the given {@link Geometry} into multiple geometries and returns them as a list.
     *
     * @param geometry
     *            The {@link Geometry} to slice
     * @param identifier
     *            The {@link Line} identifier being sliced
     * @return a list of {@link Geometry} slices
     */
    private List<Geometry> sliceGeometry(final Geometry geometry, final long identifier)
    {
        try
        {
            return getCountryBoundaryMap().slice(identifier, geometry);
        }
        catch (final TopologyException e)
        {
            logger.error("Topology Exception when slicing Line {}", identifier, e);
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
        this.rawAtlas.lines().forEach(this::sliceLine);
    }

    /**
     * Updates all points that haven't been assigned a country code after line-slicing. This
     * includes any stand-alone points (e.g. trees, barriers) or points that fell outside of any
     * country boundary.
     */
    private void slicePoints()
    {
        this.rawAtlas.points().forEach(point ->
        {
            final long pointIdentifier = point.getIdentifier();

            // Only update points that haven't been assigned a country code after way slicing
            if (!this.slicedPointAndLineChanges.getUpdatedPointTags().containsKey(pointIdentifier))
            {
                getStatistics().recordProcessedPoint();
                this.slicedPointAndLineChanges.updatePointTags(pointIdentifier,
                        createPointTags(point.getLocation(), true));
            }
        });
    }

    /**
     * Updates all of the given {@link Line}'s shape points' tags.
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

}
