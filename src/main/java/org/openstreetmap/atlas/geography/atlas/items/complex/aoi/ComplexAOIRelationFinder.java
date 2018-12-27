package org.openstreetmap.atlas.geography.atlas.items.complex.aoi;

import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.items.complex.Finder;
import org.openstreetmap.atlas.tags.filters.TaggableFilter;
import org.openstreetmap.atlas.utilities.collections.Iterables;

/**
 * {@link ComplexAOIRelation} finder.
 *
 * @author sayas01
 */
public class ComplexAOIRelationFinder implements Finder<ComplexAOIRelation>
{
    /**
     * Finds all relations that are candidates for {@link ComplexAOIRelation} and converts them into
     * {@link ComplexAOIRelation}.
     *
     * @param atlas The {@link Atlas} to browse.
     * @return {@link Iterables} of {@link ComplexAOIRelation}.
     */
    @Override
    public Iterable<ComplexAOIRelation> find(final Atlas atlas)
    {
        return Iterables.asIterable(StreamSupport
                .stream(atlas.relations().spliterator(), false)
                .map(ComplexAOIRelation::getComplexAOIRelation)
                .filter(Optional::isPresent).map(Optional::get).collect(Collectors.toList()));
    }

    /**
     * Finds all relations that are candidates for {@link ComplexAOIRelation} with given AOI tags
     * and converts them into {@link ComplexAOIRelation}.
     *
     * @param atlas Atlas to build the ComplexAOiRelation
     * @param aoiFilter {@link TaggableFilter} aoi taggable filter
     * @return {@link Iterables} of {@link ComplexAOIRelation}.
     */
    public Iterable<ComplexAOIRelation> find(final Atlas atlas, final TaggableFilter aoiFilter)
    {
        return Iterables.asIterable(StreamSupport
                .stream(atlas.relations().spliterator(), false)
                .map(relation-> ComplexAOIRelation.getComplexAOIRelation(relation,aoiFilter))
                .filter(Optional::isPresent).map(Optional::get).collect(Collectors.toList()));
    }
}
