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
 * {@link ComplexAreaOfInterest} finder.
 *
 * @author sayas01
 */
public class ComplexAreaOfInterestFinder implements Finder<ComplexAreaOfInterest>
{
    /**
     * Finds all relations and areas that are candidates for {@link ComplexAreaOfInterest} and
     * converts them into {@link ComplexAreaOfInterest}.
     *
     * @param atlas
     *            The {@link Atlas} to browse.
     * @return {@link Iterables} of {@link ComplexAreaOfInterest}.
     */
    @Override
    public Iterable<ComplexAreaOfInterest> find(final Atlas atlas)
    {
        final Iterable<ComplexAreaOfInterest> iterableOfComplexAOIRelations = StreamSupport
                .stream(atlas.relations().spliterator(), true)
                .map(ComplexAreaOfInterest::getComplexAOI).filter(Optional::isPresent)
                .map(Optional::get).collect(Collectors.toList());
        final Iterable<ComplexAreaOfInterest> iterableOfComplexAOIAreas = StreamSupport
                .stream(atlas.areas().spliterator(), true).map(ComplexAreaOfInterest::getComplexAOI)
                .filter(Optional::isPresent).map(Optional::get).collect(Collectors.toList());
        return new MultiIterable<>(iterableOfComplexAOIRelations, iterableOfComplexAOIAreas);
    }

    /**
     * Finds all relations and areas that are candidates for {@link ComplexAreaOfInterest} with
     * given AOI tags and converts them into {@link ComplexAreaOfInterest}.
     *
     * @param atlas
     *            Atlas to build the ComplexAOiRelation
     * @param aoiFilter
     *            {@link TaggableFilter} aoi taggable filter
     * @return {@link Iterables} of {@link ComplexAreaOfInterest}.
     */
    public Iterable<ComplexAreaOfInterest> find(final Atlas atlas, final TaggableFilter aoiFilter)
    {
        final Iterable<ComplexAreaOfInterest> iterableOfComplexAOIRelations = StreamSupport
                .stream(atlas.relations().spliterator(), true)
                .map(relation -> ComplexAreaOfInterest.getComplexAOI(relation, aoiFilter))
                .filter(Optional::isPresent).map(Optional::get).collect(Collectors.toList());
        final Iterable<ComplexAreaOfInterest> iterableOfComplexAOIAreas = StreamSupport
                .stream(atlas.areas().spliterator(), true)
                .map(area -> ComplexAreaOfInterest.getComplexAOI(area, aoiFilter))
                .filter(Optional::isPresent).map(Optional::get).collect(Collectors.toList());
        return new MultiIterable<>(iterableOfComplexAOIRelations, iterableOfComplexAOIAreas);
    }
}
