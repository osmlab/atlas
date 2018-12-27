package org.openstreetmap.atlas.geography.atlas.items.complex.aoi;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.openstreetmap.atlas.geography.MultiPolygon;
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.geography.atlas.items.Relation;
import org.openstreetmap.atlas.geography.atlas.items.complex.ComplexEntity;
import org.openstreetmap.atlas.geography.atlas.items.complex.RelationOrAreaToMultiPolygonConverter;
import org.openstreetmap.atlas.tags.filters.TaggableFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Complex Entity for AOI Relations. AOI relations are those that are {@link MultiPolygon} and has
 * AOI tags in it. AOI tags are checked against the {@link TaggableFilter} passed as an argument or to the
 * default {@link TaggableFilter} of AOI tags if the tags are not explicitly specified.
 *
 * @author sayas01
 */
public final class ComplexAOIRelation extends ComplexEntity
{
    private static final Logger logger = LoggerFactory.getLogger(ComplexAOIRelation.class);
    private static final RelationOrAreaToMultiPolygonConverter RELATION_TO_MULTI_POLYGON_CONVERTER = new RelationOrAreaToMultiPolygonConverter();
    // The default AreasOfInterest(AOI) tags
    private static final TaggableFilter AOI_TAG_FILTER = TaggableFilter.forDefinition("amenity->FESTIVAL_GROUNDS,GRAVE_YARD|landuse->FOREST,CEMETERY,RECREATION_GROUND,VILLAGE_GREEN|\n"
            + "boundary->NATIONAL_PARKPROTECTED_AREA|\n"
            + "historic->BATTLEFIELD|natural->WOOD,BEACH|\n"
            + "leisure->PARK,GARDEN,RECREATION_GROUND,GOLF_COURSE,NATURE_RESERVEPARK|\n"
            + "sport->GOLF|tourism->ZOO");
    private MultiPolygon multiPolygon;

    /**
     * This method creates a {@link ComplexAOIRelation} for the specified {@link AtlasEntity}
     * if it meets the requirements for Complex AOI relation. The AOI tags are checked against the
     * default tags.
     *
     * @param source The {@link AtlasEntity} for which the ComplexEntity is created
     * @return {@link Optional<ComplexAOIRelation>} if created, else return empty.
     */
    public static Optional<ComplexAOIRelation> getComplexAOIRelation(final AtlasEntity source){
        if(!(source instanceof Relation && AOI_TAG_FILTER.test(source))){
            return Optional.empty();
        }
        return Optional.of(new ComplexAOIRelation(source));
    }

    /**
     * This method creates a {@link ComplexAOIRelation} for the specified {@link AtlasEntity} and
     * {@link TaggableFilter}. The AOI tags are checked against the aoiFilter param as well as the
     * default tags.
     *
     * @param source The {@link AtlasEntity} for which the ComplexEntity is created
     * @param aoiFilter The {@link TaggableFilter} of AOI tags against which the relation is checked for AOI tags
     * @return {@link Optional<ComplexAOIRelation>} if created, else return empty.
     */
    public static Optional<ComplexAOIRelation> getComplexAOIRelation(final AtlasEntity source, final TaggableFilter aoiFilter){
        if(!(source instanceof Relation && (AOI_TAG_FILTER.test(source)||aoiFilter.test(source)))){
            return Optional.empty();
        }
        return Optional.of(new ComplexAOIRelation(source));
    }

    /**
     * Construct a {@link ComplexAOIRelation}
     *
     * @param source the {@link AtlasEntity} to construct the ComplexAoiRelation
     */
    private ComplexAOIRelation(final AtlasEntity source)
    {
        super(source);
        try
        {
            this.multiPolygon = RELATION_TO_MULTI_POLYGON_CONVERTER
                .convert(source);
        }
        catch (final Exception exception)
        {
            logger.warn("Unable to create complex relations from {}", source, exception);
            setInvalidReason("Unable to create complex islands", exception);
        }
    }

    @Override public String toString()
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

