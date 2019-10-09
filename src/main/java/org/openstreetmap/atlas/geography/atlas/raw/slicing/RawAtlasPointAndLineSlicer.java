package org.openstreetmap.atlas.geography.atlas.raw.slicing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.locationtech.jts.algorithm.distance.DiscreteHausdorffDistance;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.IntersectionMatrix;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.geom.TopologyException;
import org.locationtech.jts.index.strtree.GeometryItemDistance;
import org.locationtech.jts.operation.linemerge.LineMerger;
import org.locationtech.jts.precision.GeometryPrecisionReducer;
import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.Polygon;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.change.ChangeAtlas;
import org.openstreetmap.atlas.geography.atlas.change.ChangeBuilder;
import org.openstreetmap.atlas.geography.atlas.change.FeatureChange;
import org.openstreetmap.atlas.geography.atlas.complete.CompleteLine;
import org.openstreetmap.atlas.geography.atlas.complete.CompletePoint;
import org.openstreetmap.atlas.geography.atlas.complete.CompleteRelation;
import org.openstreetmap.atlas.geography.atlas.items.Line;
import org.openstreetmap.atlas.geography.atlas.items.Point;
import org.openstreetmap.atlas.geography.atlas.items.Relation;
import org.openstreetmap.atlas.geography.atlas.pbf.AtlasLoadingOption;
import org.openstreetmap.atlas.geography.atlas.pbf.slicing.identifier.CountrySlicingIdentifierFactory;
import org.openstreetmap.atlas.geography.atlas.pbf.slicing.identifier.PointIdentifierFactory;
import org.openstreetmap.atlas.geography.boundary.CountryBoundaryMap;
import org.openstreetmap.atlas.tags.ISOCountryTag;
import org.openstreetmap.atlas.tags.SyntheticBoundaryNodeTag;
import org.openstreetmap.atlas.tags.SyntheticNearestNeighborCountryCodeTag;
import org.openstreetmap.atlas.utilities.collections.Iterables;
import org.openstreetmap.atlas.utilities.time.Time;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;

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

    public RawAtlasPointAndLineSlicer(final Atlas atlas, final AtlasLoadingOption loadingOption)
    {
        super(loadingOption, new CoordinateToNewPointMapping(), atlas);
    }

    /**
     * Country-slice the {@link Point}s and {@link Line}s for the given Atlas.
     *
     * @return an {@link Atlas} with sliced {@link Point}s and {@link Line}s
     */
    @Override
    public Atlas slice()
    {
        Time time = Time.now();
        logger.info("Starting line slicing for Atlas {}", getShardOrAtlasName());
        final Atlas lineSlicedAtlas = sliceLines(getStartingAtlas());
        logger.info("Finished line slicing for Atlas {} in {}", getShardOrAtlasName(),
                time.elapsedSince());
        time = Time.now();
        logger.info("Starting point slicing for Atlas {}", getShardOrAtlasName());
        final Atlas pointAndLineSlicedAtlas = slicePoints(lineSlicedAtlas);

        logger.info("Finished point slicing for Atlas {} in {}", getShardOrAtlasName(),
                time.elapsedSince());

        return pointAndLineSlicedAtlas.cloneToPackedAtlas();
    }

    /**
     * Small helper method to take geometry and parse it before adding it to a result list. If the
     * geometry is in fact a collection, each geometry in the collection is tagged with a country
     * code before being added. Otherwise, the country code will be added elsewhere. Note that this
     * method relies on the ability to modify the passed-in collection directly so that there's no
     * need to return anything.
     *
     * @param geometry
     *            A geometry to add to the results collection
     * @param results
     *            The current collection of results to add the geometry to
     */
    private void addResult(final Geometry geometry, final List<Geometry> results)
    {
        if (geometry instanceof GeometryCollection)
        {
            final GeometryCollection collection = (GeometryCollection) geometry;
            CountryBoundaryMap.geometries(collection).forEach(part ->
            {
                CountryBoundaryMap.getGeometryProperties(geometry).forEach(
                        (key, value) -> CountryBoundaryMap.setGeometryProperty(part, key, value));
                this.addResult(part, results);
            });
        }
        else if (geometry instanceof LineString
                || geometry instanceof org.locationtech.jts.geom.Polygon)
        {
            results.add(geometry);
        }
        else
        {
            if (logger.isErrorEnabled())
            {
                logger.error("Resulting slice was a {}, ignoring it.", geometry.toText());
            }
        }
    }

    /**
     * Given a list of slices, check that all tags for each slice are the same (i.e. no
     * SyntheticNearestNeighborCountry tag on one slice but not the others)
     *
     * @param slices
     * @return True if all {@link Geometry} contain the same tags, false otherwise
     */
    private boolean allLineTagsEqual(final List<Geometry> slices)
    {
        if (slices == null || slices.isEmpty())
        {
            return false;
        }
        final Geometry firstSlice = slices.get(0);
        final Map<String, String> tagMap = CountryBoundaryMap.getGeometryProperties(firstSlice);
        for (final Geometry slice : slices)
        {
            if (!CountryBoundaryMap.getGeometryProperties(slice).equals(tagMap))
            {
                return false;
            }
        }
        return true;
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

        final boolean multipolygonResult = Iterables.stream(result)
                .anyMatch(geom -> geom instanceof org.locationtech.jts.geom.Polygon
                        && ((org.locationtech.jts.geom.Polygon) geom).getNumInteriorRing() > 0);
        if ((result == null || result.isEmpty()) && line.isClosed() || multipolygonResult)
        {
            if (multipolygonResult)
            {
                logger.warn(
                        "Line {} for Atlas {} had multipolygon slicing result, falling back to polyline",
                        line.getIdentifier(), getStartingAtlas().getName());
            }
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

    /**
     * Small helper method to generate a {@link Point} identifier that will avoid clashing with
     * previously existing point identifiers in the {@link Atlas} being sliced.
     *
     * @param pointIdentifierFactory
     *            An instantiated factory for generating new {@link Point} identifiers
     * @param lineIdentifier
     *            The identifier for the {@link Line} being sliced
     * @return An identifier that can safely be used to make a new {@link Point} in the
     *         {@link Atlas} being sliced
     */
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

    /**
     * Helper method to subtract each candidate out from the original {@link Geometry}. This process
     * helps identify bad slicing {@link Geometry} candidate, slicing artifacts, and whether
     * leftover {@link Geometry} exists (usually geometry that lays entirely outside the
     * {@link CountryBoundaryMap} used for cutting). If there is any significant remainder geometry,
     * it is returned.
     *
     * @param identifier
     *            The identifier of the {@link Line} being sliced
     * @param candidates
     *            The current collection of candidates {@link Geometry} objects from slicing
     * @param results
     *            The current collection of valid slice {@link Geometry}
     * @param target
     *            The original {@link Geometry} pre-slicing
     * @return Any leftover {@link Geometry} from the original target that isn't represented by the
     *         union of the slice candidate {@link Geometry} collection
     */
    private Geometry cutGeometry(final long identifier,
            final List<org.locationtech.jts.geom.Polygon> candidates, final List<Geometry> results,
            final Geometry target)
    {
        Geometry currentTarget = target;
        boolean fullyMatched = false;
        // Start cut process
        for (final org.locationtech.jts.geom.Polygon candidate : candidates)
        {
            Geometry clipped;
            try
            {
                clipped = currentTarget.intersection(candidate);
            }
            catch (final TopologyException exc)
            {
                logger.error(
                        "Error while using regular intersection for line {}, attempting again with reduced precision",
                        identifier, exc);
                try
                {
                    final GeometryPrecisionReducer precisionReducer = new GeometryPrecisionReducer(
                            new PrecisionModel(CountryBoundaryMap.PRECISION_MODEL));
                    precisionReducer.setPointwise(true);
                    precisionReducer.setChangePrecisionModel(false);
                    currentTarget = precisionReducer.reduce(target);
                    clipped = currentTarget.intersection(candidate);
                }
                catch (final Exception newExc)
                {
                    logger.error(
                            "Reduced precision still failed for line {}, rethrowing original exception",
                            identifier, newExc);
                    throw exc;
                }
            }

            // We don't want single point pieces
            if (clipped.getNumPoints() > 1)
            {
                // Add to the results
                final String countryCode = CountryBoundaryMap.getGeometryProperty(candidate,
                        ISOCountryTag.KEY);
                CountryBoundaryMap.setGeometryProperty(clipped, ISOCountryTag.KEY, countryCode);
                addResult(clipped, results);

                // Update target to be what's left after clipping
                currentTarget = currentTarget.difference(candidate);

                // If the remaining piece is very small and we ignore it. This helps avoid
                // cutting features just a little over boundary lines and generating too many
                // new nodes, which is both unnecessary and exhausts node identifier resources.
                if (isSignificantGeometry(currentTarget)
                        && new DiscreteHausdorffDistance(currentTarget, candidate)
                                .orientedDistance() < CountryBoundaryMap.LINE_BUFFER)
                {
                    fullyMatched = true;
                    break;
                }
            }
        }
        return fullyMatched ? null : currentTarget;
    }

    private boolean isSignificantGeometry(final Geometry geometry)
    {
        return geometry.getDimension() == 1 && geometry.getLength() > CountryBoundaryMap.LINE_BUFFER
                || geometry.getDimension() == 2
                        && geometry.getArea() > CountryBoundaryMap.AREA_BUFFER;
    }

    /**
     * Helper method that takes in a collection of slice {@link Geometry} and organizes it. If all
     * slices for a country have matching tags, their slices will be run through {@link LineMerger}
     * in an attempt to reduce any redundant slicing (i.e. two line slices with the same tags that
     * intersect at their endpoints will get merged into one line slice). The returned map is sorted
     * alphabetically by country code so that slice processing is always deterministic, regardless
     * of which countries are being sliced.
     *
     * @param slices
     *            Collection of slice {@link Geometry} for the {@link Line} being sliced
     * @return {@link SortedMap} mapping alphabetically sorted country codes to the slice
     *         {@link Geometry} for that country
     */
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

            if (allLineTagsEqual(slicesForCountry))
            {
                final Map<String, String> lineTags = new HashMap<>();
                lineTags.put(ISOCountryTag.KEY, countryCode);
                CountryBoundaryMap.getGeometryProperties(slicesForCountry.get(0))
                        .forEach((key, value) ->
                        {
                            if (!lineTags.containsKey(key))
                            {
                                lineTags.put(key, value);
                            }
                        });

                final LineMerger merger = new LineMerger();
                merger.add(slicesForCountry);
                merger.getMergedLineStrings()
                        .forEach(geometry -> lineTags.forEach((key, value) -> CountryBoundaryMap
                                .setGeometryProperty((Geometry) geometry, key, value)));
                sortedSlices.addAll(merger.getMergedLineStrings());
                processedSlices.put(countryCode, sortedSlices);
            }
            else
            {
                sortedSlices.addAll(slicesForCountry);
                processedSlices.put(countryCode, sortedSlices);
            }
        });
        return processedSlices;
    }

    /**
     * Processes each slice by updating corresponding tags ({@link ISOCountryTag},
     * {@link SyntheticNearestNeighborCountryCodeTag}, {@link SyntheticBoundaryNodeTag}, making a
     * new {@link FeatureChange} for new {@link Line}s based on each slice, then creates a
     * {@link FeatureChange} to remove the original {@link Line}.
     *
     * @param line
     *            The {@link Line} that was sliced
     * @param slices
     *            A collection of slice {@link Geometry} for the {@link Line} being sliced
     * @param atlas
     *            The {@link Atlas} being sliced
     * @param lineChanges
     *            The {@link ChangeBuilder} keeping track of {@link Line} slicing
     *            {@link FeatureChange}s
     */
    private void processLineSlices(final Line line, final List<Geometry> slices, final Atlas atlas,
            final ChangeBuilder lineChanges)
    {
        final CountrySlicingIdentifierFactory lineIdentifierFactory = new CountrySlicingIdentifierFactory(
                line.getIdentifier());
        final PointIdentifierFactory pointIdentifierFactory = new PointIdentifierFactory(
                line.getIdentifier());
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
                final long lineSliceIdentifier = lineIdentifierFactory.nextIdentifier();
                final PolyLine newLineGeometry = processSlice(slice, pointIdentifierFactory, line,
                        atlas, lineChanges);
                final Map<String, String> lineTags = createLineTags(slice, line.getTags());
                final CompleteLine newLineSlice = CompleteLine.from(line)
                        .withIdentifier(lineSliceIdentifier).withTags(lineTags)
                        .withPolyLine(newLineGeometry);
                if (isInsideWorkingBound(newLineSlice))
                {
                    lineChanges.add(FeatureChange.add(newLineSlice, atlas));
                    for (final Relation relation : line.relations())
                    {
                        final CompleteRelation updatedRelation = CompleteRelation.from(relation)
                                .withAddedMember(newLineSlice, line);
                        lineChanges.add(FeatureChange.add(updatedRelation, atlas));
                    }
                }
            }
        });

        lineChanges.add(FeatureChange.remove(CompleteLine.shallowFrom(line), atlas));
    }

    /**
     * Given a slice {@link Geometry}, create a {@link PolyLine} out of it, ensuring that all
     * {@link Location}s along the {@link PolyLine} have either {@link Point}s in the original
     * {@link Atlas}, {@link Point}s made by other slices, or new {@link Point}s using
     * {@link FeatureChange}s. Additionally, JTS frequently reverses geometry winding during the
     * slice operation, so the returned {@link PolyLine} will be checked to ensure its winding
     * matches the source entity's winding.
     *
     * @param slice
     *            The slice {@link Geometry} to make a new {@link Line} for
     * @param pointIdentifierFactory
     *            A {@link PointIdentifierFactory} for generating new {@link Point} identifiers if
     *            needed
     * @param line
     *            The {@link Line} being sliced
     * @param atlas
     *            The {@link Atlas} being sliced
     * @param lineChanges
     *            The {@link ChangeBuilder} tracking {@link Line} slicing changes
     * @return A {@link PolyLine} representing the slice
     */

    private PolyLine processSlice(final Geometry slice,
            final PointIdentifierFactory pointIdentifierFactory, final Line line, final Atlas atlas,
            final ChangeBuilder lineChanges)
    {
        final List<Location> slicedLineLocations = new ArrayList<>();

        // JTS geometry often adds unnecessary precision after slicing-- reduce it to the standard
        // precision to avoid errors
        final Coordinate[] jtsSliceCoordinates = PRECISION_REDUCER.edit(slice.getCoordinates(),
                slice);

        for (final Coordinate coordinate : jtsSliceCoordinates)
        {
            final Location coordinateLocation = JTS_LOCATION_CONVERTER.backwardConvert(coordinate);

            // If there aren't any points at this precision in the raw Atlas, need to
            // examine cache and possibly scale the coordinate to 6 digits of precision
            if (Iterables.isEmpty(atlas.pointsAt(coordinateLocation)))
            {
                // Add the scaled location to the line
                final Location scaledLocation = JTS_LOCATION_CONVERTER.backwardConvert(
                        getCoordinateToPointMapping().getScaledCoordinate(coordinate));
                slicedLineLocations.add(scaledLocation);

                // If the location doesn't have a point in the original Atlas or in the cache,
                // make a new Point and add it both
                if (Iterables.isEmpty(atlas.pointsAt(scaledLocation))
                        && !getCoordinateToPointMapping().containsCoordinate(coordinate))
                {
                    final Map<String, String> pointTags = createPointTags(coordinateLocation,
                            false);
                    pointTags.put(SyntheticBoundaryNodeTag.KEY,
                            SyntheticBoundaryNodeTag.YES.toString());
                    final long pointIdentifier = createNewPointIdentifier(pointIdentifierFactory,
                            line.getIdentifier());
                    final CompletePoint newPointFromSlice = new CompletePoint(pointIdentifier,
                            scaledLocation, pointTags, new HashSet<Long>());
                    getCoordinateToPointMapping().storeMapping(coordinate, pointIdentifier);
                    lineChanges.add(FeatureChange.add(newPointFromSlice, atlas));
                }
            }
            else
            {
                slicedLineLocations.add(coordinateLocation);
            }
        }

        PolyLine polylineForSlice = new PolyLine(slicedLineLocations);

        // JTS frequently reverses the winding during slicing-- this checks the winding of the
        // original geometry, and reverse the winding of the slice if needed
        if (line.isClosed())
        {
            final boolean originalClockwise = new Polygon(line.asPolyLine().truncate(0, 1))
                    .isClockwise();
            final boolean sliceClockwise = new Polygon(polylineForSlice.truncate(0, 1))
                    .isClockwise();
            if (originalClockwise != sliceClockwise)
            {
                polylineForSlice = polylineForSlice.reversed();
            }
        }
        return polylineForSlice;
    }

    /**
     * Helper method for slicing geometry operations that looks at each slice {@link Geometry} and
     * relates it to the original {@link Geometry}. Should the original entity be entirely within
     * one country, return true as a short-circuit to avoid further unnecessary operations.
     * Otherwise, tags each {@link Geometry} with an {@link ISOCountryTag} for the country it's in,
     * or removes if it's not a valid candidate.
     *
     * @param identifier
     *            The identifier for the original {@link Line} being sliced
     * @param candidates
     *            The collection of candidate slice {@link Geometry}
     * @param results
     *            The current running collection of valid slice {@link Geometry}
     * @param target
     *            The JTS {@link Geometry} for the {@link Line} being sliced
     * @return True if the entity lays entirely in one country, false otherwise.
     */
    private boolean relateCandidates(final long identifier,
            final List<org.locationtech.jts.geom.Polygon> candidates, final List<Geometry> results,
            final Geometry target)
    {
        // Check relation of target to all polygons
        final Iterator<org.locationtech.jts.geom.Polygon> candidateIterator = candidates.iterator();
        while (candidateIterator.hasNext())
        {
            final org.locationtech.jts.geom.Polygon candidate = candidateIterator.next();
            final String countryCode = CountryBoundaryMap.getGeometryProperty(candidate,
                    ISOCountryTag.KEY);
            if (Strings.isNullOrEmpty(countryCode))
            {
                logger.warn(
                        "Ignoring a candidate polygon from slicing way {}, because it is missing country tag.",
                        identifier);
            }
            else
            {
                try
                {
                    final IntersectionMatrix matrix = target.relate(candidate);
                    // Fully contained inside a single country, no need to go any further, just
                    // assign and return the country code
                    if (matrix.isWithin())
                    {
                        CountryBoundaryMap.setGeometryProperty(target, ISOCountryTag.KEY,
                                countryCode);
                        addResult(target, results);
                        return true;
                    }

                    // No intersection, remove from candidate list
                    if (!matrix.isIntersects())
                    {
                        candidateIterator.remove();
                    }
                }
                catch (final Exception e)
                {
                    logger.warn("error slicing way {}:", identifier, e);
                }
            }
        }
        return false;
    }

    /**
     * Slice the {@link Geometry} with given country boundary map and assign country code to each
     * piece. If the {@link Line} doesn't cross any border then it contains only one item with
     * country code assigned. If the {@link Line} crosses borders then slice it by the border
     * geometry and assign country codes for each piece. If there is any segment not contained by
     * any country boundary, it will be assigned to nearest country with the
     * {@link SyntheticNearestNeighborCountryCodeTag}.
     *
     * @param geometry
     *            The JTS {@link Geometry} to be sliced
     * @param line
     *            The source Atlas {@link Line} being sliced
     * @return A list of sliced {@link Geometry} objects
     */
    private List<Geometry> sliceGeometry(final Geometry geometry, final Line line)
    {
        try
        {
            final CountryBoundaryMap boundary = getCountryBoundaryMap();
            final List<Geometry> results = new ArrayList<>();
            if (Objects.isNull(geometry))
            {
                return results;
            }

            final Geometry target = geometry;
            List<org.locationtech.jts.geom.Polygon> candidates = boundary
                    .query(target.getEnvelopeInternal());

            // Performance improvement, if only one polygon returned no need to do any further
            // evaluation (except when geometry has to be sliced at all costs)
            if (shouldSkipSlicing(candidates, line))
            {
                final String countryCode = CountryBoundaryMap.getGeometryProperty(candidates.get(0),
                        ISOCountryTag.KEY);
                CountryBoundaryMap.setGeometryProperty(target, ISOCountryTag.KEY, countryCode);
                addResult(target, results);
                return results;
            }

            // Remove duplicates and avoid slicing across too many polygons for performance reasons
            candidates = candidates.stream().distinct().collect(Collectors.toList());
            if (candidates.size() > boundary.getPolygonSliceLimit())
            {
                logger.warn("Skipping slicing way {} due to too many intersecting polygons [{}]",
                        line.getIdentifier(), candidates.size());
                return results;
            }

            final long numberCountries = CountryBoundaryMap.numberCountries(candidates);
            if (numberCountries > CountryBoundaryMap.MAXIMUM_EXPECTED_COUNTRIES_TO_SLICE_WITH)
            {
                logger.warn("Slicing way {} with {} countries.", line.getIdentifier(),
                        numberCountries);
            }

            if (relateCandidates(line.getIdentifier(), candidates, results, target))
            {
                return results;
            }

            // Performance: short circuit, if all intersected polygons in same country, skip
            // cutting (except when geometry has to be sliced at all costs)
            if (shouldSkipSlicing(candidates, line))
            {
                final String countryCode = CountryBoundaryMap.getGeometryProperty(candidates.get(0),
                        ISOCountryTag.KEY);
                CountryBoundaryMap.setGeometryProperty(target, ISOCountryTag.KEY, countryCode);
                addResult(target, results);
                return results;
            }

            // Sort intersecting polygons for consistent slicing
            Collections.sort(candidates, (final org.locationtech.jts.geom.Polygon first,
                    final org.locationtech.jts.geom.Polygon second) ->
            {
                final int countryCodeComparison = CountryBoundaryMap
                        .getGeometryProperty(first, ISOCountryTag.KEY).compareTo(
                                CountryBoundaryMap.getGeometryProperty(second, ISOCountryTag.KEY));
                if (countryCodeComparison != 0)
                {
                    return countryCodeComparison;
                }

                return first.compareTo(second);
            });

            final Geometry remainder = cutGeometry(line.getIdentifier(), candidates, results,
                    target);

            if (remainder == null)
            {
                return results;
            }

            // Part or all of the geometry is not inside any country, assign with nearest country.
            final Geometry nearestGeometry = boundary.nearestNeighbour(target.getEnvelopeInternal(),
                    remainder, new GeometryItemDistance());
            if (nearestGeometry == null)
            {
                return results;
            }
            for (int i = 0; i < remainder.getNumGeometries(); i++)
            {
                final Geometry current = remainder.getGeometryN(i);
                if (isSignificantGeometry(current))
                {
                    final String nearestCountryCode = CountryBoundaryMap
                            .getGeometryProperty(nearestGeometry, ISOCountryTag.KEY);
                    CountryBoundaryMap.setGeometryProperty(current, ISOCountryTag.KEY,
                            nearestCountryCode);
                    CountryBoundaryMap.setGeometryProperty(current,
                            SyntheticNearestNeighborCountryCodeTag.KEY,
                            SyntheticNearestNeighborCountryCodeTag.YES.toString());
                    addResult(current, results);
                }
            }
            return results;
        }
        catch (final TopologyException e)
        {
            logger.error("Topology Exception when slicing Line {} for Atlas {}",
                    line.getIdentifier(), getShardOrAtlasName(), e);
            return Collections.emptyList();
        }
    }

    /**
     * Converts the given {@link Line} to a JTS {@link Geometry} and slices it. If the results of
     * slicing are empty, or are from the same country, simply updates the original geometry with
     * the country tag. Otherwise, calls a helper function to replace this {@link Line} with the
     * slices.
     *
     * @param line
     *            The {@link Line} to slice
     * @param atlas
     *            The {@link Atlas} being sliced
     * @param lineChanges
     *            The {@link ChangeBuilder} used to keep track of the {@link Line} slicing changes
     */
    private void sliceLine(final Line line, final Atlas atlas, final ChangeBuilder lineChanges)
    {
        final List<Geometry> slices = convertToJtsGeometryAndSlice(line);
        if (slices == null || slices.isEmpty())
        {
            final CompleteLine updatedLine = CompleteLine.shallowFrom(line).withTags(line.getTags())
                    .withAddedTag(ISOCountryTag.KEY, ISOCountryTag.COUNTRY_MISSING);
            lineChanges.add(FeatureChange.add(updatedLine, atlas));
        }
        else if (CountryBoundaryMap.isSameCountry(slices) && !shouldForceSlicing(line))
        {
            final CompleteLine updatedLine = CompleteLine.shallowFrom(line)
                    .withTags(createLineTags(slices.get(0), line.getTags()));
            if (isInsideWorkingBound(updatedLine))
            {
                lineChanges.add(FeatureChange.add(updatedLine, atlas));
            }
            else
            {
                lineChanges.add(FeatureChange.remove(CompleteLine.shallowFrom(line), atlas));
            }
        }
        else if (slices.size() > CountrySlicingIdentifierFactory.IDENTIFIER_SCALE_DEFAULT)
        {
            logger.error(
                    "Country slicing exceeded maximum line identifier name space of {} for Line {} for Atlas {}. It will be added as is, with two or more country codes.",
                    CountrySlicingIdentifierFactory.IDENTIFIER_SCALE_DEFAULT, line.getIdentifier(),
                    getShardOrAtlasName());
            final Set<String> allCountries = slices.stream().map(
                    geometry -> CountryBoundaryMap.getGeometryProperty(geometry, ISOCountryTag.KEY))
                    .collect(Collectors.toCollection(TreeSet::new));
            final String countryString = String.join(",", allCountries);
            final CompleteLine afterLine = CompleteLine.shallowFrom(line).withTags(line.getTags())
                    .withAddedTag(ISOCountryTag.KEY, countryString);
            lineChanges.add(FeatureChange.add(afterLine, atlas));
        }
        else
        {
            processLineSlices(line, slices, atlas, lineChanges);
        }
    }

    /**
     * Given a point-sliced {@link Atlas}, slice its {@link Line}s by making {@link FeatureChange}s,
     * then rebuilding using {@link ChangeAtlas}. Should no {@link Line}s qualify for slicing,
     * returns the original {@link Atlas} unmodified.
     *
     * @param pointSlicedAtlas
     *            The Atlas to be line sliced
     * @return An Atlas with lines sliced
     */
    private Atlas sliceLines(final Atlas pointSlicedAtlas)
    {
        final ChangeBuilder lineChangeBuilder = new ChangeBuilder();
        StreamSupport.stream(pointSlicedAtlas.lines().spliterator(), true)
                .forEach(line -> sliceLine(line, pointSlicedAtlas, lineChangeBuilder));
        if (lineChangeBuilder.peekNumberOfChanges() == 0)
        {
            return pointSlicedAtlas;
        }
        return new ChangeAtlas(pointSlicedAtlas, lineChangeBuilder.get());
    }

    /**
     * Tag all {@link Point}s in the {@link Atlas} with country codes by making
     * {@link FeatureChange}s, then rebuilding using {@link ChangeAtlas}. Should no {@link Point}s
     * need tagging, return the original {@link Atlas}.
     *
     * @param rawAtlas
     *            A raw {@link Atlas} to be point-tagged
     * @return An {@link Atlas} with all points country tagged
     */
    private Atlas slicePoints(final Atlas lineSlicedAtlas)
    {
        final ChangeBuilder pointChanges = new ChangeBuilder();
        StreamSupport.stream(lineSlicedAtlas.points().spliterator(), true).forEach(point ->
        {
            if (!point.getTag(ISOCountryTag.KEY).isPresent())
            {
                final CompletePoint afterPoint = CompletePoint.shallowFrom(point)
                        .withTags(point.getTags());
                createPointTags(point.getLocation(), true).forEach(afterPoint::withAddedTag);
                pointChanges.add(FeatureChange.add(afterPoint, lineSlicedAtlas));
            }
        });
        if (pointChanges.peekNumberOfChanges() == 0)

        {
            return lineSlicedAtlas;
        }
        return new ChangeAtlas(lineSlicedAtlas, pointChanges.get());
    }
}
