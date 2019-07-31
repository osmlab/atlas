package org.openstreetmap.atlas.geography.atlas.raw.slicing;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.precision.PrecisionReducerCoordinateOperation;
import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.Line;
import org.openstreetmap.atlas.geography.atlas.items.Point;
import org.openstreetmap.atlas.geography.atlas.pbf.AtlasLoadingOption;
import org.openstreetmap.atlas.geography.atlas.raw.temporary.TemporaryPoint;
import org.openstreetmap.atlas.geography.boundary.CountryBoundaryMap;
import org.openstreetmap.atlas.geography.converters.MultiplePolyLineToPolygonsConverter;
import org.openstreetmap.atlas.geography.converters.jts.JtsLinearRingConverter;
import org.openstreetmap.atlas.geography.converters.jts.JtsLocationConverter;
import org.openstreetmap.atlas.geography.converters.jts.JtsPolyLineConverter;
import org.openstreetmap.atlas.geography.converters.jts.JtsPolygonConverter;
import org.openstreetmap.atlas.tags.ISOCountryTag;
import org.openstreetmap.atlas.tags.SyntheticBoundaryNodeTag;
import org.openstreetmap.atlas.tags.SyntheticNearestNeighborCountryCodeTag;
import org.openstreetmap.atlas.tags.Taggable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;
import com.google.common.collect.Sets;

/**
 * The abstract class that contains all common raw Atlas slicing functionality.
 *
 * @author mgostintsev
 */
public abstract class RawAtlasSlicer
{
    // JTS converters
    protected static final JtsPolygonConverter JTS_POLYGON_CONVERTER = new JtsPolygonConverter();
    protected static final JtsPolyLineConverter JTS_POLYLINE_CONVERTER = new JtsPolyLineConverter();
    protected static final JtsLocationConverter JTS_LOCATION_CONVERTER = new JtsLocationConverter();
    protected static final JtsLinearRingConverter JTS_LINEAR_RING_CONVERTER = new JtsLinearRingConverter();
    protected static final MultiplePolyLineToPolygonsConverter MULTIPLE_POLY_LINE_TO_POLYGON_CONVERTER = new MultiplePolyLineToPolygonsConverter();
    private static final Logger logger = LoggerFactory.getLogger(RawAtlasSlicer.class);
    // JTS precision handling
    private static final Integer SEVEN_DIGIT_PRECISION_SCALE = 10_000_000;
    private static final PrecisionModel PRECISION_MODEL = new PrecisionModel(
            SEVEN_DIGIT_PRECISION_SCALE);
    protected static final PrecisionReducerCoordinateOperation PRECISION_REDUCER = new PrecisionReducerCoordinateOperation(
            PRECISION_MODEL, false);

    private final AtlasLoadingOption loadingOption;

    // Tracks all changes during the slicing process
    private final RawAtlasSlicingStatistic statistics = new RawAtlasSlicingStatistic(logger);

    // Mapping between Coordinate and created Temporary Point identifiers. This is to avoid
    // duplicate points at the same locations and to allow fast lookup to construct new lines
    // requiring the temporary point as a Line shape point
    private final CoordinateToNewPointMapping newPointCoordinates;

    private final String shardOrAtlasName;

    private final Atlas startingAtlas;

    /**
     * Assigns {@link ISOCountryTag} and {@link SyntheticNearestNeighborCountryCodeTag} values for a
     * given {@link Geometry}, which is a {@link Line} since we don't have other non-Point
     * geometries in the raw Atlas.
     *
     * @param geometry
     *            The {@link Geometry} to create the tags for
     * @param tags
     *            The tags to which to add to
     * @return the resulting tags
     */
    protected static Map<String, String> createLineTags(final Geometry geometry,
            final Map<String, String> tags)
    {
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
     * @param pointIdentifier
     *            The identifier to give the new point
     * @param pointTags
     *            The tags for this new point
     * @return the {@link TemporaryPoint}
     */
    protected static TemporaryPoint createNewPoint(final Coordinate coordinate,
            final long pointIdentifier, final Map<String, String> pointTags)
    {
        // Add the synthetic boundary node tags
        pointTags.put(SyntheticBoundaryNodeTag.KEY, SyntheticBoundaryNodeTag.YES.toString());

        return new TemporaryPoint(pointIdentifier,
                JTS_LOCATION_CONVERTER.backwardConvert(coordinate), pointTags);
    }

    /**
     * Determines if the two given {@link Line}s belong to the same country.
     *
     * @param one
     *            The first {@link Line}
     * @param two
     *            The second {@link Line}
     * @return {@code true} if both lines belong to the same country
     */
    protected static boolean fromSameCountry(final Line one, final Line two)
    {
        final Optional<String> firstTagValue = one.getTag(ISOCountryTag.KEY);
        final Optional<String> secondTagValue = two.getTag(ISOCountryTag.KEY);
        if (firstTagValue.isPresent() && secondTagValue.isPresent())
        {
            final Set<String> firstCountries = new HashSet<>(
                    Arrays.asList(firstTagValue.get().split(ISOCountryTag.COUNTRY_DELIMITER)));
            final Set<String> secondCountries = new HashSet<>(
                    Arrays.asList(secondTagValue.get().split(ISOCountryTag.COUNTRY_DELIMITER)));

            firstCountries.retainAll(new HashSet<>(secondCountries));
            return !firstCountries.isEmpty();
        }
        throw new CoreException(
                "All raw Atlas lines must have a country code by the time Relation slicing is done. One of the two Lines {} or {} does not!",
                one.getIdentifier(), two.getIdentifier());
    }

    public RawAtlasSlicer(final AtlasLoadingOption loadingOption,
            final CoordinateToNewPointMapping newPointCoordinates, final Atlas startingAtlas)
    {
        this.loadingOption = loadingOption;
        this.newPointCoordinates = newPointCoordinates;
        this.startingAtlas = startingAtlas;
        this.shardOrAtlasName = getShardOrAtlasName(startingAtlas);
    }

    public String getShardOrAtlasName()
    {
        return this.shardOrAtlasName;
    }

    /**
     * @return the {@link Atlas} to be sliced
     */
    public Atlas getStartingAtlas()
    {
        return this.startingAtlas;
    }

    /**
     * @return the {@link Atlas} after slicing.
     */
    public abstract Atlas slice();

    /**
     * Assigns {@link ISOCountryTag}, {@link SyntheticNearestNeighborCountryCodeTag} and
     * {@link SyntheticBoundaryNodeTag} values for a given {@link Location}. The logic behind
     * {@link SyntheticBoundaryNodeTag} assignment is if we're creating a new {@link Point}, it's
     * only going to happen at the boundary, since that's where slicing happens. We have two ways of
     * retrieving Points - 1. from the original un-sliced Raw Atlas 2. from the newPointCoordinates
     * mapping. We first check to see if there is already a {@link TemporaryPoint} for this
     * {@link Coordinate} in the map (to avoid creating duplicate points). If there's not, we check
     * the raw Atlas to see if there is already a {@link Point} at this {@link Location}. If the
     * atlas has a {@link Point} there, we know to assign the
     * {@link SyntheticBoundaryNodeTag#EXISTING} value, as this {@link Point} is already part of the
     * raw OSM data. If the raw Atlas doesn't contain a {@link Point} at this {@link Location}, then
     * we create a new {@link TemporaryPoint} and assign the {@link SyntheticBoundaryNodeTag#YES}
     * value.
     *
     * @param location
     *            The {@link Location} for which to assign country code tags
     * @param fromRawAtlas
     *            {@code true} if this {@link Location} is already an {@link Point} in the raw Atlas
     * @return the created tags
     */
    protected Map<String, String> createPointTags(final Location location,
            final boolean fromRawAtlas)
    {
        final Map<String, String> tags = new HashMap<>();

        // Get the country code details
        final CountryCodeProperties countryDetails = getCountryBoundaryMap()
                .getCountryCodeISO3(location);

        // Store the country code, enforce alphabetical order if there are multiple
        if (countryDetails.inMultipleCountries())
        {
            tags.put(ISOCountryTag.KEY, Joiner.on(",").join(Sets.newTreeSet(Arrays.asList(
                    countryDetails.getIso3CountryCode().split(ISOCountryTag.COUNTRY_DELIMITER)))));
        }
        else
        {
            tags.put(ISOCountryTag.KEY, countryDetails.getIso3CountryCode());
        }

        // If we used nearest neighbor logic to determine the country code, add a tag
        // to indicate this
        if (countryDetails.usingNearestNeighbor())
        {
            tags.put(SyntheticNearestNeighborCountryCodeTag.KEY,
                    SyntheticNearestNeighborCountryCodeTag.YES.toString());
            tags.put(SyntheticBoundaryNodeTag.KEY, SyntheticBoundaryNodeTag.EXISTING.toString());
        }

        // For any border nodes, add the existing tag
        if (fromRawAtlas && countryDetails.inMultipleCountries())
        {
            // TODO - Edge case: ferries that end just short of the country boundary should have an
            // existing synthetic boundary node tag. One approach to generate these is to snap the
            // ferry end point to the closest MultiPolygon boundary and if it's within a configured
            // distance, assign the tag. However, this is facing performance issues and will have to
            // be addressed in the future.
            tags.put(SyntheticBoundaryNodeTag.KEY, SyntheticBoundaryNodeTag.EXISTING.toString());
        }

        return tags;
    }

    protected CoordinateToNewPointMapping getCoordinateToPointMapping()
    {
        return this.newPointCoordinates;
    }

    protected Set<String> getCountries()
    {
        if (this.loadingOption.getCountryCodes().isEmpty())
        {
            final HashSet<String> allCountryCodes = new HashSet<>();
            allCountryCodes.addAll(this.loadingOption.getCountryBoundaryMap().allCountryNames());
            this.loadingOption.setAdditionalCountryCodes(allCountryCodes);
        }
        return this.loadingOption.getCountryCodes();
    }

    protected CountryBoundaryMap getCountryBoundaryMap()
    {
        return this.loadingOption.getCountryBoundaryMap();
    }

    protected RawAtlasSlicingStatistic getStatistics()
    {
        return this.statistics;
    }

    /**
     * Determines if the given raw atlas {@link Line} qualifies to be an {@link Edge} in the final
     * atlas. Relies on the underlying {@link AtlasLoadingOption} configuration to make the
     * decision.
     *
     * @param line
     *            The {@link Line} to check
     * @return {@code true} if the given raw atlas {@link Line} qualifies to be an {@link Edge} in
     *         the final atlas.
     */
    protected boolean isAtlasEdge(final Line line)
    {
        return this.loadingOption.getEdgeFilter().test(line);
    }

    protected boolean isInsideWorkingBound(final AtlasEntity entity)
    {
        final Optional<String> countryCodes = entity.getTag(ISOCountryTag.KEY);
        if (countryCodes.isPresent() && this.getCountries() != null
                && !this.getCountries().isEmpty())
        {
            for (final String countryCode : countryCodes.get()
                    .split(ISOCountryTag.COUNTRY_DELIMITER))
            {
                if (this.getCountries().contains(countryCode))
                {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Check if the {@link Geometry} should be filtered out based on the provided bound.
     *
     * @param geometry
     *            The {@link Geometry} to check.
     * @return {@code true} if the given geometry should be filtered out.
     */
    protected boolean isOutsideWorkingBound(final Geometry geometry)
    {
        final String countryCode = CountryBoundaryMap.getGeometryProperty(geometry,
                ISOCountryTag.KEY);

        if (countryCode != null)
        {
            return this.getCountries() != null && !this.getCountries().isEmpty()
                    && !this.getCountries().contains(countryCode);
        }

        // Assume it's inside the bound
        return false;
    }

    protected boolean shouldForceSlicing(final Taggable... source)
    {
        if (this.loadingOption.getForceSlicingFilter() == null)
        {
            return false;
        }
        return source != null && source.length > 0
                && this.loadingOption.getForceSlicingFilter().test(source[0]);
    }

    protected boolean shouldSkipSlicing(final List<Polygon> candidates, final Taggable... source)
    {
        return CountryBoundaryMap.isSameCountry(candidates) && !shouldForceSlicing(source);
    }

    private String getShardOrAtlasName(final Atlas atlas)
    {
        return atlas.metaData().getShardName().orElse(atlas.getName());
    }
}
