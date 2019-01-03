package org.openstreetmap.atlas.geography.atlas.items.complex.aoi;

import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.items.complex.Finder;
import org.openstreetmap.atlas.tags.filters.TaggableFilter;
import org.openstreetmap.atlas.utilities.collections.Iterables;
import org.openstreetmap.atlas.utilities.collections.MultiIterable;

/**
 * {@link ComplexAOI} finder.
 *
 * @author sayas01
 */
public class ComplexAOIFinder implements Finder<ComplexAOI>
{
    /**
     * Finds all relations and areas that are candidates for {@link ComplexAOI} and converts them
     * into {@link ComplexAOI}.
     *
     * @param atlas
     *            The {@link Atlas} to browse.
     * @return {@link Iterables} of {@link ComplexAOI}.
     */
    @Override
    public Iterable<ComplexAOI> find(final Atlas atlas)
    {
        final Iterable<ComplexAOI> iterableOfComplexAOIRelations = Iterables
                .asIterable(StreamSupport.stream(atlas.relations().spliterator(), false)
                        .map(ComplexAOI::getComplexAOI).filter(Optional::isPresent)
                        .map(Optional::get).collect(Collectors.toList()));
        final Iterable<ComplexAOI> iterableOfComplexAOIAreas = Iterables.asIterable(StreamSupport
                .stream(atlas.areas().spliterator(), false).map(ComplexAOI::getComplexAOI)
                .filter(Optional::isPresent).map(Optional::get).collect(Collectors.toList()));
        return new MultiIterable<>(iterableOfComplexAOIRelations, iterableOfComplexAOIAreas);
    }

    /**
     * Finds all relations and areas that are candidates for {@link ComplexAOI} with given AOI tags
     * and converts them into {@link ComplexAOI}.
     *
     * @param atlas
     *            Atlas to build the ComplexAOiRelation
     * @param aoiFilter
     *            {@link TaggableFilter} aoi taggable filter
     * @return {@link Iterables} of {@link ComplexAOI}.
     */
    public Iterable<ComplexAOI> find(final Atlas atlas, final TaggableFilter aoiFilter)
    {
        final Iterable<ComplexAOI> iterableOfComplexAOIRelations = Iterables
                .asIterable(StreamSupport.stream(atlas.relations().spliterator(), false)
                        .map(relation -> ComplexAOI.getComplexAOI(relation, aoiFilter))
                        .filter(Optional::isPresent).map(Optional::get)
                        .collect(Collectors.toList()));
        final Iterable<ComplexAOI> iterableOfComplexAOIAreas = Iterables.asIterable(StreamSupport
                .stream(atlas.areas().spliterator(), false)
                .map(area -> ComplexAOI.getComplexAOI(area, aoiFilter)).filter(Optional::isPresent)
                .map(Optional::get).collect(Collectors.toList()));
        return new MultiIterable<>(iterableOfComplexAOIRelations, iterableOfComplexAOIAreas);
    }
}
