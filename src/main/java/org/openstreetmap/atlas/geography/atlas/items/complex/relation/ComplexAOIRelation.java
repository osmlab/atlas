package org.openstreetmap.atlas.geography.atlas.items.complex.relation;

import java.util.ArrayList;
import java.util.List;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.MultiPolygon;
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.geography.atlas.items.Relation;
import org.openstreetmap.atlas.geography.atlas.items.complex.ComplexEntity;
import org.openstreetmap.atlas.geography.atlas.items.complex.RelationOrAreaToMultiPolygonConverter;
import org.openstreetmap.atlas.tags.filters.TaggableFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Complex Entity for AOI Relations
 *
 * @author sayas01
 */
public class ComplexAOIRelation extends ComplexEntity
{

    private static final Logger logger = LoggerFactory.getLogger(ComplexAOIRelation.class);
    private static final RelationOrAreaToMultiPolygonConverter RELATION_TO_MULTI_POLYGON_CONVERTER = new RelationOrAreaToMultiPolygonConverter();
    private static final TaggableFilter AOI_TAG_FILTER = TaggableFilter.forDefinition("amenity->FESTIVAL_GROUNDS,GRAVE_YARD|landuse->CEMETERY,RECREATION_GROUND,VILLAGE_GREEN\n"
            + "boundary->NATIONAL_PARKPROTECTED_AREA|leisure->NATURE_RESERVEPARK\n"
            + "historic->BATTLEFIELD landuse->FOREST|natural->WOOD,BEACH\n"
            + "leisure->PARK,GARDEN,RECREATION_GROUND,GOLF_COURSE\n"
            + "sport->GOLF|tourism->ZOO");
    private MultiPolygon multiPolygon;
    private TaggableFilter aoiFilter;


    /**
     * Construct a {@link ComplexAOIRelation}
     *
     * @param source
     */
    public ComplexAOIRelation(final AtlasEntity source)
    {
        super(source);
        try
        {
            populateGeometry();
        }
        catch (final Exception exception)
        {
            logger.warn("Unable to create complex relations from {}", source, exception);
            setInvalidReason("Unable to create complex islands", exception);
            return;
        }
    }

    /**
     * Construct a {@link ComplexAOIRelation} with {@link TaggableFilter}
     *
     * @param source
     * @param aoiFilter
     */
    public ComplexAOIRelation(final AtlasEntity source, final TaggableFilter aoiFilter)
    {
        super(source);
        this.aoiFilter=aoiFilter;
        try
        {
            populateGeometry();
        }
        catch (final Exception exception)
        {
            logger.warn("Unable to create complex relations from {}", source, exception);
            setInvalidReason("Unable to create complex islands", exception);
            return;
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

    /**
     * Convert relation to MultiPolygonRelation
     */
    private void populateGeometry()
    {
        final AtlasEntity source = getSource();
        if (source instanceof Relation)
        {
            final Relation relation = (Relation) source;
            if(relation.isMultiPolygon() && hasAoiTag(relation))
            {
                this.multiPolygon = RELATION_TO_MULTI_POLYGON_CONVERTER
                        .convert(relation);
                return;
            }
        }
        throw new CoreException("Geometry is not set for {}", source);
    }

    /**
     * Verifies if the {@link Relation} has any of the AOI tags
     *
     * @param relation
     * @return
     */
    private boolean hasAoiTag(final Relation relation)
    {
        return AOI_TAG_FILTER.test(relation)||this.aoiFilter.test(relation);
    }
}

