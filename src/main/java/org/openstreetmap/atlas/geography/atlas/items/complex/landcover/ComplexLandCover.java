package org.openstreetmap.atlas.geography.atlas.items.complex.landcover;

import java.io.InputStreamReader;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.MultiPolygon;
import org.openstreetmap.atlas.geography.atlas.items.Area;
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.geography.atlas.items.Relation;
import org.openstreetmap.atlas.geography.atlas.items.complex.ComplexEntity;
import org.openstreetmap.atlas.geography.atlas.items.complex.RelationOrAreaToMultiPolygonConverter;
import org.openstreetmap.atlas.tags.Taggable;
import org.openstreetmap.atlas.tags.filters.TaggableFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

/**
 * @author sbhalekar
 * @author samg
 */
public final class ComplexLandCover extends ComplexEntity
{
    private static final Logger logger = LoggerFactory.getLogger(ComplexLandCover.class);
    private static final long serialVersionUID = 220683230343177634L;
    private static final RelationOrAreaToMultiPolygonConverter MULTIPOLYGON_CONVERTER = new RelationOrAreaToMultiPolygonConverter();
    private static final String LAND_COVER_RESOURCE = "land-cover-tag-filter.json";
    // The default LandCover tags
    private static List<TaggableFilter> defaultTaggableFilter;
    private final MultiPolygon multiPolygon;

    /**
     * This method creates a {@link ComplexLandCover} for the specified {@link AtlasEntity} if it
     * meets the requirements for Complex LandCover relation. The LandCover tags are checked against
     * the default tags.
     *
     * @param source
     *            The {@link AtlasEntity} for which the ComplexEntity is created
     * @return {@link ComplexLandCover} if created, else return empty.
     */
    public static Optional<ComplexLandCover> getComplexLandCover(final AtlasEntity source)
    {
        if (defaultTaggableFilter == null)
        {
            computeDefaultFilter();
        }
        return getComplexLandCover(source, ComplexLandCover::hasLandCoverTag);
    }

    /**
     * This method creates a {@link ComplexLandCover} for the specified {@link AtlasEntity} and
     * {@link TaggableFilter}. The land cover tags are checked against the landCoverFilter param as
     * well as the default tags.
     *
     * @param source
     *            The {@link AtlasEntity} for which the ComplexEntity is created
     * @param landCoverFilter
     *            The {@link TaggableFilter} of land cover tags against which the relation is
     *            checked for land cover tags
     * @return {@link ComplexLandCover} if created, else return empty.
     */
    public static Optional<ComplexLandCover> getComplexLandCover(final AtlasEntity source,
            final Predicate<Taggable> landCoverFilter)
    {
        try
        {
            return ((source instanceof Relation || source instanceof Area)
                    && landCoverFilter.test(source)) ? Optional.of(new ComplexLandCover(source))
                            : Optional.empty();
        }
        catch (final Exception exception)
        {
            logger.warn("Unable to create complex land cover relations from {}", source, exception);
            return Optional.empty();
        }
    }

    private static void computeDefaultFilter()
    {
        try (InputStreamReader reader = new InputStreamReader(
                ComplexLandCover.class.getResourceAsStream(LAND_COVER_RESOURCE)))
        {
            final JsonElement element = new JsonParser().parse(reader);
            final JsonArray filters = element.getAsJsonObject().get("filters").getAsJsonArray();
            defaultTaggableFilter = StreamSupport.stream(filters.spliterator(), false)
                    .map(jsonElement -> TaggableFilter.forDefinition(jsonElement.getAsString()))
                    .collect(Collectors.toList());
        }
        catch (final Exception exception)
        {
            throw new CoreException(
                    "There was a problem parsing aoi-tag-filter.json. Check if the JSON file has valid structure.",
                    exception);
        }
    }

    /**
     * Checks for land cover tags in the object
     *
     * @param source
     *            {@link AtlasEntity} that needs to be checked for land cover tags
     * @return {@code true} if the source has land cover tags
     */
    private static boolean hasLandCoverTag(final Taggable source)
    {
        return defaultTaggableFilter.stream()
                .anyMatch(taggableFilter -> taggableFilter.test(source));
    }

    private ComplexLandCover(final AtlasEntity source)
    {
        super(source);
        try
        {
            this.multiPolygon = MULTIPOLYGON_CONVERTER.convert(source);
        }
        catch (final Exception exception)
        {
            setInvalidReason("Unable to convert the AtlasEntity to MultiPolygon", exception);
            throw new CoreException("Unable to convert the AtlasEntity {} to MultiPolygon",
                    source.getOsmIdentifier(), exception);
        }
    }

    @Override
    public boolean equals(final Object other)
    {
        return other instanceof ComplexLandCover && super.equals(other);
    }

    public MultiPolygon getGeometry()
    {
        return this.multiPolygon;
    }

    @Override
    public int hashCode()
    {
        return super.hashCode();
    }

    @Override
    public String toString()
    {
        return this.getClass().getName() + " " + getSource();
    }
}
