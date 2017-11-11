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
import org.openstreetmap.atlas.geography.atlas.items.Relation;
import org.openstreetmap.atlas.geography.atlas.pbf.slicing.identifier.AbstractIdentifierFactory;
import org.openstreetmap.atlas.geography.atlas.pbf.slicing.identifier.CountrySlicingIdentifierFactory;
import org.openstreetmap.atlas.geography.boundary.CountryBoundaryMap;
import org.openstreetmap.atlas.geography.converters.jts.JtsLocationConverter;
import org.openstreetmap.atlas.geography.converters.jts.JtsPolyLineConverter;
import org.openstreetmap.atlas.geography.converters.jts.JtsPolygonConverter;
import org.openstreetmap.atlas.locale.IsoCountry;
import org.openstreetmap.atlas.tags.ISOCountryTag;
import org.openstreetmap.atlas.tags.ManMadeTag;
import org.openstreetmap.atlas.tags.RelationTypeTag;
import org.openstreetmap.atlas.tags.RouteTag;
import org.openstreetmap.atlas.tags.SyntheticBoundaryNodeTag;
import org.openstreetmap.atlas.tags.SyntheticNearestNeighborCountryCodeTag;
import org.openstreetmap.atlas.tags.annotations.validation.Validators;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Iterables;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.TopologyException;

/**
 * The {@link RawAtlasCountrySlicer} consumes a raw Atlas and produces a fully country-sliced Atlas
 * as output.
 *
 * @author mgostintsev
 */
public class RawAtlasCountrySlicer
{
    private static final Logger logger = LoggerFactory.getLogger(RawAtlasCountrySlicer.class);

    // JTS converters
    private static final JtsPolygonConverter JTS_POLYGON_CONVERTER = new JtsPolygonConverter();
    private static final JtsPolyLineConverter JTS_POLYLINE_CONVERTER = new JtsPolyLineConverter();
    private static final JtsLocationConverter JTS_LOCATION_CONVERTER = new JtsLocationConverter();

    // The raw Atlas to slice
    private Atlas rawAtlas;

    // The countries we're interested in slicing against
    private final Set<IsoCountry> countries;

    // Contains boundary MultiPolygons
    private final CountryBoundaryMap countryBoundaryMap;

    private RawAtlasChangeSet afterSlicingLinesAndPoints = new RawAtlasChangeSet();
    private final RawAtlasChangeSet afterSlicingRelations = new RawAtlasChangeSet();

    // Mapping between Coordinate and created Temporary Point identifiers. This is to avoid
    // duplicate points at the same locations and to allow fast lookup to construct new lines
    // requiring the temporary point as a Line shape point
    private final Map<Coordinate, Long> newPointCoordinates = new HashMap<>();

    private final RawAtlasSlicingStatistic statistics = new RawAtlasSlicingStatistic(logger);

    public RawAtlasCountrySlicer(final Atlas rawAtlas, final Set<IsoCountry> countries,
            final CountryBoundaryMap countryBoundaryMap)
    {
        this.rawAtlas = rawAtlas;
        this.countries = countries;
        this.countryBoundaryMap = countryBoundaryMap;
    }

    /**
     * TODO Complete once Relation slicing is implemented
     *
     * @return a country-sliced {@link Atlas}
     */
    public Atlas slice()
    {
        logger.info("Starting country slicing Atlas {}", this.rawAtlas.getName());

        // Slice lines and points
        sliceLines();
        slicePoints();

        // Apply changes and rebuild the Atlas with the changes before slicing Relations
        final RawAtlasChangeSetBuilder changeBuilder = new RawAtlasChangeSetBuilder(this.rawAtlas,
                this.afterSlicingLinesAndPoints);
        final Atlas withSlicedWaysAndPoints = changeBuilder.applyChanges();
        this.rawAtlas = withSlicedWaysAndPoints;

        // Clear for GC
        this.afterSlicingLinesAndPoints = null;

        // Slice all Relations and rebuild the Atlas
        sliceRelations();

        this.statistics.summary();

        logger.info("Finished country slicing Atlas {}", this.rawAtlas.getName());

        return this.rawAtlas;
    }

    /**
     * In case of Ferries and Piers that can extend out in the water and connect to other countries,
     * here is the option to force country slicing, even if there is no immediate country nearby.
     *
     * @param way
     *            The way to test for
     * @return {@code true} if eligible for mandatory slicing.
     */
    private boolean canSkipSlicingIfSingleCountry(final Line line)
    {
        return !Validators.isOfType(line, RouteTag.class, RouteTag.FERRY)
                && !Validators.isOfType(line, ManMadeTag.class, ManMadeTag.PIER);
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
        // Create the JTS Geometry from Line
        final Geometry geometry;
        if (line.isClosed())
        {
            // An Area
            geometry = JTS_POLYGON_CONVERTER.convert(new Polygon(line));
        }
        else
        {
            // A Line
            geometry = JTS_POLYLINE_CONVERTER.convert(line.asPolyLine());
        }

        // Slice the JTS Geometry
        try
        {
            return this.countryBoundaryMap.slice(line.getIdentifier(), geometry);
        }
        catch (final TopologyException e)
        {
            logger.error("Topology Exception when slicing Line {}", line.getIdentifier(), e);
            return Collections.emptyList();
        }
    }

    /**
     * Assigns {@link ISOCountryTag} and {@link SyntheticNearestNeighborCountryCodeTag} values for a
     * given {@link Geometry}, which is a {@link Line} since we don't have other non-Point
     * geometries in the raw Atlas.
     *
     * @param geometry
     *            The {@link Geoemtry} to create the tags for
     * @return the resulting tags
     */
    private Map<String, String> createLineTags(final Geometry geometry)
    {
        final Map<String, String> tags = new HashMap<>();
        final String countryCode = CountryBoundaryMap.getGeometryProperty(geometry,
                ISOCountryTag.KEY);
        tags.put(ISOCountryTag.KEY, countryCode);
        final String usingNearestNeighbor = CountryBoundaryMap.getGeometryProperty(geometry,
                SyntheticNearestNeighborCountryCodeTag.KEY);
        if (usingNearestNeighbor != null)
        {
            tags.put(SyntheticNearestNeighborCountryCodeTag.KEY, usingNearestNeighbor);
        }
        return tags;
    }

    /**
     * Creates a new {@link TemporaryPoint} at the given {@link Coordinate}. We can assume that any
     * new Points will be created at the country boundaries, so we can add the
     * {@link SyntheticBoundaryNodeTag} tag.
     *
     * @param coordinate
     *            The {@link Coordinate} of the new point
     * @param pointIdentifierFactory
     *            The {@link CountrySlicingIdentifierFactory} to calculate new point identifier
     * @param pointTags
     *            The tags for this new point
     * @return the {@link TemporaryPoint}
     */
    private TemporaryPoint createNewPoint(final Coordinate coordinate,
            final CountrySlicingIdentifierFactory pointIdentifierFactory,
            final Map<String, String> pointTags)
    {
        if (!pointIdentifierFactory.hasMore())
        {
            throw new CoreException(
                    "Country Slicing exceeded maximum number {} of supported new points at Coordinate {}",
                    AbstractIdentifierFactory.IDENTIFIER_SCALE, coordinate);
        }
        else
        {
            // Add the synthetic boundary node tags
            pointTags.put(SyntheticBoundaryNodeTag.KEY, SyntheticBoundaryNodeTag.YES.toString());

            return new TemporaryPoint(pointIdentifierFactory.nextIdentifier(),
                    JTS_LOCATION_CONVERTER.backwardConvert(coordinate), pointTags);
        }
    }

    /**
     * Assigns {@link ISOCountryTag}, {@link SyntheticNearestNeighborCountryCodeTag} and
     * {@link SyntheticBoundaryNodeTag} values for a given {@link Location}. The logic behind
     * {@link SyntheticBoundaryNodeTag} assignment is if we're creating a new {@link Point}, it's
     * only going to happen at the boundary, since that's where slicing happens. We have two ways of
     * retrieving Points - 1. from the original un-sliced Raw Atlas 2. from the newPointCoordinates
     * map which maps a {@link Coordinate} to a {@link TemporaryPoint} identifier. We first check to
     * see if there is already a {@link TemporaryPoint} for this {@link Coordinate} in the map (to
     * avoid creating duplicate points). If there's not, we check the raw Atlas to see if there is
     * already a {@link Point} at this {@link Location}. If the atlas has a {@link Point} there, we
     * know to assign the {@link SyntheticBoundaryNodeTag#EXISTING} value, as this {@link Point} is
     * already part of the raw OSM data. If the raw Atlas doesn't contain a {@link Point} at this
     * {@link Location}, then we create a new {@link TemporaryPoint} and assign the
     * {@link SyntheticBoundaryNodeTag#YES} value.
     *
     * @param location
     *            The {@link Location} for which to assign country code tags
     * @param fromRawAtlas
     *            {@code true} if this {@link Location} is already an {@link Point} in the raw Atlas
     * @return the created tags
     */
    private Map<String, String> createPointTags(final Location location, final boolean fromRawAtlas)
    {
        final Map<String, String> tags = new HashMap<>();

        // Get the country code details
        final CountryCodeProperties countryDetails = this.countryBoundaryMap
                .getCountryCodeISO3(location);

        // Store the country code
        tags.put(ISOCountryTag.KEY, countryDetails.getIso3CountryCode());

        // If we used nearest neighbor logic to determine the country code, add a tag
        // to indicate this
        if (countryDetails.usingNearestNeighbor())
        {
            tags.put(SyntheticNearestNeighborCountryCodeTag.KEY,
                    SyntheticNearestNeighborCountryCodeTag.YES.toString());
        }

        // For any border nodes, add the existing tag
        if (fromRawAtlas && countryDetails.inMultipleCountries())
        {
            tags.put(SyntheticBoundaryNodeTag.KEY, SyntheticBoundaryNodeTag.EXISTING.toString());
        }

        return tags;
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
            return this.countries != null && !this.countries.isEmpty()
                    && !this.countries.contains(countryCode.get());
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
        return slices.size() == 1
                || CountryBoundaryMap.isSameCountry(slices) && canSkipSlicingIfSingleCountry(line);
    }

    /**
     * Processes each slice by updating corresponding tags ({@link ISOCountryTag},
     * {@link SyntheticNearestNeighborCountryCodeTag}, {@link SyntheticBoundaryNodeTag} and creating
     * {@link RawAtlasChangeSet}s to keep track of created, updated and deleted {@link Point}s and
     * {@link Line}s.
     *
     * @param line
     *            The {@link Line} that was sliced
     * @param slices
     *            The resulting {@link Geometry} slices
     */
    private void processSlices(final Line line, final List<Geometry> slices)
    {
        if (slices == null || slices.isEmpty())
        {
            // No slices generated or an error in slicing, create missing country code
            final Map<String, String> tags = new HashMap<>();
            tags.put(ISOCountryTag.KEY, ISOCountryTag.COUNTRY_MISSING);
            this.afterSlicingLinesAndPoints.updateLineTags(line.getIdentifier(), tags);
            updateLineShapePoints(line);
        }
        else if (lineBelongsToSingleCountry(line, slices))
        {
            // This line belongs to a single country
            this.afterSlicingLinesAndPoints.updateLineTags(line.getIdentifier(),
                    createLineTags(slices.get(0)));
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
                        if (this.newPointCoordinates.containsKey(coordinate))
                        {
                            // A new point was already created for this coordinate. Look it up and
                            // use it for the line we're creating
                            newLineShapePoints.add(this.newPointCoordinates.get(coordinate));
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
                                this.newPointCoordinates.put(coordinate, newPoint.getIdentifier());

                                // Store this point to reconstruct the Line geometry
                                newLineShapePoints.add(newPoint.getIdentifier());

                                // Save the Point to add to the rebuilt atlas
                                this.afterSlicingLinesAndPoints.createPoint(newPoint);
                            }
                            else
                            {
                                // Grab the country code tags for this point
                                final Map<String, String> pointTags = createPointTags(
                                        coordinateLocation, true);

                                // There is at least one point at this location in the raw Atlas.
                                // Update
                                // all existing points to have the country code.
                                for (final Point rawAtlasPoint : rawAtlasPointsAtSliceVertex)
                                {
                                    // Update the country codes
                                    this.afterSlicingLinesAndPoints.updatePointTags(
                                            rawAtlasPoint.getIdentifier(), pointTags);

                                    // Add all point identifiers to make up the new Line
                                    newLineShapePoints.add(rawAtlasPoint.getIdentifier());
                                }
                            }
                        }
                    }

                    // Extract relevant tag values for this slice
                    final Map<String, String> lineTags = createLineTags(slice);

                    // Create and store the new line
                    final TemporaryLine createdLine = new TemporaryLine(
                            lineIdentifierFactory.nextIdentifier(), newLineShapePoints, lineTags);
                    createdLines.add(createdLine);
                }

                // Update the change with the added and removed lines
                createdLines.forEach(
                        createdLine -> this.afterSlicingLinesAndPoints.createLine(createdLine));

                this.afterSlicingLinesAndPoints.deleteLine(line.getIdentifier());
                this.afterSlicingLinesAndPoints.createDeletedToCreatedMapping(line.getIdentifier(),
                        createdLines.stream().map(TemporaryLine::getIdentifier)
                                .collect(Collectors.toList()));

                // Record a successful slice
                this.statistics.recordSlicedLine();
            }
            catch (final CoreException e)
            {
                // TODO - Consider shifting to a 4 digit namespace for identifiers
                logger.error(
                        "Country slicing exceeded maximum point identifier name space of {} for Line {}",
                        AbstractIdentifierFactory.IDENTIFIER_SCALE, line.getIdentifier(), e);
                this.statistics.recordSkippedLine();
            }
        }
        else
        {
            // TODO - Consider expanding to a 4 digit namespace for identifiers
            logger.error(
                    "Country slicing exceeded maximum line identifier name space of {} for Line {}",
                    AbstractIdentifierFactory.IDENTIFIER_SCALE, line.getIdentifier());
            this.statistics.recordSkippedLine();
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
        this.statistics.recordProcessedLine();
        final List<Geometry> slices = convertToJtsGeometryAndSlice(line);
        processSlices(line, slices);
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
            if (!this.afterSlicingLinesAndPoints.getUpdatedPointTags().containsKey(pointIdentifier))
            {
                this.afterSlicingLinesAndPoints.updatePointTags(pointIdentifier,
                        createPointTags(point.getLocation(), true));
            }
        });
    }

    // TODO come back and verify we're keeping track of all required statistics

    /**
     * TODO
     *
     * @param relation
     */
    private void sliceRelation(final Relation relation)
    {
        this.statistics.recordProcessedRelation();

        if (Validators.isOfType(relation, RelationTypeTag.class, RelationTypeTag.BOUNDARY,
                RelationTypeTag.MULTIPOLYGON))
        {
            // Process geometry based relations
        }
        else
        {
            // Process all other relations
        }
    }

    /**
     * Slices all {@link Relation}s in the given raw Atlas.
     */
    private void sliceRelations()
    {
        this.rawAtlas.relations().forEach(this::sliceRelation);
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
                this.afterSlicingLinesAndPoints.updatePointTags(point.getIdentifier(),
                        createPointTags(location, true));
            }
        }
    }
}
