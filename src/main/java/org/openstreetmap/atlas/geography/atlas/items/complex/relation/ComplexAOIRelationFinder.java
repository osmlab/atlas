package org.openstreetmap.atlas.geography.atlas.items.complex.relation;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.items.Relation;
import org.openstreetmap.atlas.geography.atlas.items.complex.Finder;
import org.openstreetmap.atlas.tags.filters.TaggableFilter;
import org.openstreetmap.atlas.utilities.collections.Iterables;

/**
 * {@link ComplexAOIRelation} finder
 *
 * @author sayas01
 */
public class ComplexAOIRelationFinder implements Finder<ComplexAOIRelation>
{
    private static final TaggableFilter AOI_TAG_FILTER = TaggableFilter.forDefinition("amenity->FESTIVAL_GROUNDS,GRAVE_YARD|landuse->CEMETERY,RECREATION_GROUND,VILLAGE_GREEN\n"
            + "boundary->NATIONAL_PARKPROTECTED_AREA|leisure->NATURE_RESERVEPARK\n"
            + "historic->BATTLEFIELD landuse->FOREST|natural->WOOD,BEACH\n"
            + "leisure->PARK,GARDEN,RECREATION_GROUND,GOLF_COURSE\n"
            + "sport->GOLF|tourism->ZOO");
    /**
     * Finds all relations that are candidates for {@link ComplexAOIRelation} and converts them into {@link ComplexAOIRelation}.
     * @param atlas The {@link Atlas} to browse.
     * @return {@link Iterables} of {@link ComplexAOIRelation}.
     */
    @Override
    public Iterable<ComplexAOIRelation> find(final Atlas atlas)
    {
        final Iterable<Relation> relations = atlas.relations(
                relation -> relation.isMultiPolygon() && AOI_TAG_FILTER.test(relation));
        return Iterables.translate(relations, ComplexAOIRelation::new);
    }

    /**
     * Finds all relations that are candidates for {@link ComplexAOIRelation} with given AOI tags
     * and converts them into {@link ComplexAOIRelation}.
     *
     * @param atlas
     * @param aoiFilter
     * @return
     */
    public Iterable<ComplexAOIRelation> find(final Atlas atlas, final TaggableFilter aoiFilter)
    {
        final Iterable<Relation> relations = atlas.relations(
                relation -> relation.isMultiPolygon() && (AOI_TAG_FILTER.test(relation)|| aoiFilter.test(relation)));
        return Iterables.translate(relations, ComplexAOIRelation::new);
    }
}
