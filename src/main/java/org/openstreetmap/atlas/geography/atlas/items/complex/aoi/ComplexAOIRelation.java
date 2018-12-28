package org.openstreetmap.atlas.geography.atlas.items.complex.aoi;

import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.MultiPolygon;
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.geography.atlas.items.Relation;
import org.openstreetmap.atlas.geography.atlas.items.complex.ComplexEntity;
import org.openstreetmap.atlas.geography.atlas.items.complex.RelationOrAreaToMultiPolygonConverter;
import org.openstreetmap.atlas.tags.filters.TaggableFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

/**
 * Complex Entity for AOI Relations. AOI relations are those that are {@link MultiPolygon} and has
 * AOI tags in it. AOI tags are checked against the {@link TaggableFilter} passed as an argument or
 * to the default {@link TaggableFilter} of AOI tags if the tags are not explicitly specified.
 *
 * @author sayas01
 */
public final class ComplexAOIRelation extends ComplexEntity
{
    private static final Logger logger = LoggerFactory.getLogger(ComplexAOIRelation.class);
    private static final RelationOrAreaToMultiPolygonConverter RELATION_TO_MULTI_POLYGON_CONVERTER = new RelationOrAreaToMultiPolygonConverter();
    private static final String AOI_RESOURCE = "aoi-tag-filter.json";
    private MultiPolygon multiPolygon;
    // The default AreasOfInterest(AOI) tags
    private static TaggableFilter defaultTaggableFilter = null;

    /**
     * This method creates a {@link ComplexAOIRelation} for the specified {@link AtlasEntity} if it
     * meets the requirements for Complex AOI relation. The AOI tags are checked against the default
     * tags.
     *
     * @param source
     *            The {@link AtlasEntity} for which the ComplexEntity is created
     * @return {@link ComplexAOIRelation} if created, else return empty.
     */
    public static Optional<ComplexAOIRelation> getComplexAOIRelation(final AtlasEntity source)
    {

        if (defaultTaggableFilter == null)
        {
            computeDefaultFilter();
        }
        return source instanceof Relation && defaultTaggableFilter.test(source)
                ? Optional.of(new ComplexAOIRelation(source)) : Optional.empty();
    }

    /**
     * This method creates a {@link ComplexAOIRelation} for the specified {@link AtlasEntity} and
     * {@link TaggableFilter}. The AOI tags are checked against the aoiFilter param as well as the
     * default tags.
     *
     * @param source
     *            The {@link AtlasEntity} for which the ComplexEntity is created
     * @param aoiFilter
     *            The {@link TaggableFilter} of AOI tags against which the relation is checked for
     *            AOI tags
     * @return {@link ComplexAOIRelation} if created, else return empty.
     */
    public static Optional<ComplexAOIRelation> getComplexAOIRelation(final AtlasEntity source,
            final TaggableFilter aoiFilter)
    {
        if (defaultTaggableFilter == null)
        {
            computeDefaultFilter();
        }
        return source instanceof Relation
                && (defaultTaggableFilter.test(source) || aoiFilter.test(source))
                        ? Optional.of(new ComplexAOIRelation(source)) : Optional.empty();
    }

    private static void computeDefaultFilter()
    {
        try (InputStreamReader reader = new InputStreamReader(
                ComplexAOIRelation.class.getResourceAsStream(AOI_RESOURCE)))
        {
            final JsonElement element = new JsonParser().parse(reader);
            final String aoiTagFilter = element.getAsJsonObject().get("aoi.tag.filter")
                    .getAsString();
            defaultTaggableFilter = TaggableFilter.forDefinition(aoiTagFilter);
        }
        catch (final Exception exception)
        {
            throw new CoreException(
                    "There was a problem parsing aoi-tag-filter.json. Check if the JSON file has valid structure.",
                    exception);
        }
    }

    /**
     * Construct a {@link ComplexAOIRelation}
     *
     * @param source
     *            the {@link AtlasEntity} to construct the ComplexAoiRelation
     */
    private ComplexAOIRelation(final AtlasEntity source)
    {
        super(source);
        try
        {
            this.multiPolygon = RELATION_TO_MULTI_POLYGON_CONVERTER.convert(source);
        }
        catch (final Exception exception)
        {
            logger.warn("Unable to create complex AOI relations from {}", source, exception);
            setInvalidReason("Unable to create complex AOIs", exception);
        }
    }

    @Override
    public String toString()
    {
        return this.getClass().getName() + " " + getSource();
    }

    public MultiPolygon getGeometry()
    {
        return this.multiPolygon;
    }

    @Override
    public List<ComplexEntityError> getAllInvalidations()
    {
        final List<ComplexEntityError> returnValue = new ArrayList<>();
        if (!isValid() && getError().isPresent())
        {
            returnValue.add(getError().get());
        }
        return returnValue;
    }
}
