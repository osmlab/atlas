package org.openstreetmap.atlas.geography.atlas.items.complex.aoi;

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
 * Complex Entity for AOI Relations and AOI Areas. AOI relations are those that are
 * {@link MultiPolygon} and have AOI tags in it and AOI Areas are those {@link Area}s that have AOI
 * tags in it. AOI tags are checked against the {@link TaggableFilter} passed as an argument or to
 * the default {@link TaggableFilter} of AOI tags if the tags are not explicitly specified.
 *
 * @author sayas01
 */
public final class ComplexAreaOfInterest extends ComplexEntity
{
    private static final Logger logger = LoggerFactory.getLogger(ComplexAreaOfInterest.class);
    private static final RelationOrAreaToMultiPolygonConverter RELATION_OR_AREA_TO_MULTI_POLYGON_CONVERTER = new RelationOrAreaToMultiPolygonConverter();
    private static final String AOI_RESOURCE = "aoi-tag-filter.json";
    // The default AreasOfInterest(AOI) tags
    private static List<TaggableFilter> defaultTaggableFilter;
    private static final long serialVersionUID = 1191946548857888704L;
    private final MultiPolygon multiPolygon;

    /**
     * This method creates a {@link ComplexAreaOfInterest} for the specified {@link AtlasEntity} if
     * it meets the requirements for Complex AOI relation. The AOI tags are checked against the
     * default tags.
     *
     * @param source
     *            The {@link AtlasEntity} for which the ComplexEntity is created
     * @return {@link ComplexAreaOfInterest} if created, else return empty.
     */
    public static Optional<ComplexAreaOfInterest> getComplexAOI(final AtlasEntity source)
    {
        return getComplexAOI(source, customAoiFilter -> false);
    }

    /**
     * This method creates a {@link ComplexAreaOfInterest} for the specified {@link AtlasEntity} and
     * {@link TaggableFilter}. The AOI tags are checked against the aoiFilter param as well as the
     * default tags.
     *
     * @param source
     *            The {@link AtlasEntity} for which the ComplexEntity is created
     * @param aoiFilter
     *            The {@link TaggableFilter} of AOI tags against which the relation is checked for
     *            AOI tags
     * @return {@link ComplexAreaOfInterest} if created, else return empty.
     */
    public static Optional<ComplexAreaOfInterest> getComplexAOI(final AtlasEntity source,
            final Predicate<Taggable> aoiFilter)
    {
        try
        {
            if (defaultTaggableFilter == null)
            {
                computeDefaultFilter();
            }
            return ((source instanceof Relation || source instanceof Area)
                    && (hasAOITag(source) || aoiFilter.test(source)))
                            ? Optional.of(new ComplexAreaOfInterest(source))
                            : Optional.empty();
        }
        catch (final Exception exception)
        {
            logger.warn("Unable to create complex AOI relations from {}", source, exception);
            return Optional.empty();
        }
    }

    private static void computeDefaultFilter()
    {
        try (InputStreamReader reader = new InputStreamReader(
                ComplexAreaOfInterest.class.getResourceAsStream(AOI_RESOURCE)))
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
     * Checks for AOI tags in the object
     *
     * @param source
     *            {@link AtlasEntity} that needs to be checked for AOI tags
     * @return {@code true} if the source has AOI tags
     */
    private static boolean hasAOITag(final AtlasEntity source)
    {
        return defaultTaggableFilter.stream()
                .anyMatch(taggableFilter -> taggableFilter.test(source));
    }

    /**
     * Construct a {@link ComplexAreaOfInterest}
     *
     * @param source
     *            the {@link AtlasEntity} to construct the ComplexAoiRelation
     */
    private ComplexAreaOfInterest(final AtlasEntity source)
    {
        super(source);
        try
        {
            this.multiPolygon = RELATION_OR_AREA_TO_MULTI_POLYGON_CONVERTER.convert(source);
        }
        catch (final Exception exception)
        {
            setInvalidReason("Unable to convert the AtlasEntity to MultiPolygon", exception);
            throw new CoreException("Unable to convert the AtlasEntity to MultiPolygon", exception);
        }
    }

    @Override
    public boolean equals(final Object other)
    {
        return other instanceof ComplexAreaOfInterest && super.equals(other);
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
