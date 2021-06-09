package org.openstreetmap.atlas.geography.atlas.items.complex.landcover;

import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.items.complex.Finder;
import org.openstreetmap.atlas.tags.filters.TaggableFilter;
import org.openstreetmap.atlas.utilities.collections.Iterables;
import org.openstreetmap.atlas.utilities.collections.MultiIterable;

/**
 * {@link ComplexLandCover} finder.
 *
 * @author samg
 */
public class ComplexLandCoverFinder implements Finder<ComplexLandCover>
{
    /**
     * Finds all relations and areas that are candidates for {@link ComplexLandCover} and converts
     * them into {@link ComplexLandCover}.
     *
     * @param atlas
     *            The {@link Atlas} to browse.
     * @return {@link Iterables} of {@link ComplexLandCover}.
     */
    @Override
    public Iterable<ComplexLandCover> find(final Atlas atlas)
    {
        final Iterable<ComplexLandCover> iterableOfComplexLandCoverRelations = StreamSupport
                .stream(atlas.relations().spliterator(), true)
                .map(ComplexLandCover::getComplexLandCover).filter(Optional::isPresent)
                .map(Optional::get).collect(Collectors.toList());
        final Iterable<ComplexLandCover> iterableOfComplexLandCoverAreas = StreamSupport
                .stream(atlas.areas().spliterator(), true)
                .map(ComplexLandCover::getComplexLandCover).filter(Optional::isPresent)
                .map(Optional::get).collect(Collectors.toList());
        return new MultiIterable<>(iterableOfComplexLandCoverRelations,
                iterableOfComplexLandCoverAreas);
    }

    /**
     * Finds all relations and areas that are candidates for {@link ComplexLandCover} with given
     * land cover tags and converts them into {@link ComplexLandCover}.
     *
     * @param atlas
     *            Atlas to build the ComplexLandCover
     * @param landCoverFilter
     *            {@link TaggableFilter} land cover taggable filter
     * @return {@link Iterables} of {@link ComplexLandCover}.
     */
    public Iterable<ComplexLandCover> find(final Atlas atlas, final TaggableFilter landCoverFilter)
    {
        final Iterable<ComplexLandCover> iterableOfComplexLandCoverRelations = StreamSupport
                .stream(atlas.relations().spliterator(), true)
                .map(relation -> ComplexLandCover.getComplexLandCover(relation, landCoverFilter))
                .filter(Optional::isPresent).map(Optional::get).collect(Collectors.toList());
        final Iterable<ComplexLandCover> iterableOfComplexLandCoverAreas = StreamSupport
                .stream(atlas.areas().spliterator(), true)
                .map(area -> ComplexLandCover.getComplexLandCover(area, landCoverFilter))
                .filter(Optional::isPresent).map(Optional::get).collect(Collectors.toList());
        return new MultiIterable<>(iterableOfComplexLandCoverRelations,
                iterableOfComplexLandCoverAreas);
    }
}
