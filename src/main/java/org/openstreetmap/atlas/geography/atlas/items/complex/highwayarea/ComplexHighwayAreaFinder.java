package org.openstreetmap.atlas.geography.atlas.items.complex.highwayarea;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.complex.Finder;
import org.openstreetmap.atlas.tags.AreaTag;
import org.openstreetmap.atlas.tags.HighwayTag;
import org.openstreetmap.atlas.tags.annotations.validation.Validators;
import org.openstreetmap.atlas.utilities.collections.Iterables;

/**
 * A simple finder for a {@link ComplexHighwayArea}
 *
 * @author isabellehillberg
 */
public class ComplexHighwayAreaFinder implements Finder<ComplexHighwayArea>
{
    private static Optional<ComplexHighwayArea> toEntity(final Edge edge)
    {
        final ComplexHighwayArea complexHighwayAreaEntity = new ComplexHighwayArea(edge);
        return Optional.of(complexHighwayAreaEntity);
    }

    private static boolean validEdge(final Edge edge)
    {
        return Validators.isNotOfType(edge, HighwayTag.class, HighwayTag.NO)
                && Validators.isOfType(edge, AreaTag.class, AreaTag.YES) && edge.isMainEdge();
    }

    @Override
    public Iterable<ComplexHighwayArea> find(final Atlas atlas)
    {
        final Set<Long> visitedEdgeIdentifiers = new HashSet<>();
        return Iterables.stream(atlas.edges())
                .flatMap(edges -> processEntity(edges, visitedEdgeIdentifiers));
    }

    private List<ComplexHighwayArea> processEntity(final Edge edge,
            final Set<Long> visitedEdgeIdentifiers)
    {
        final List<ComplexHighwayArea> returnValue = new ArrayList<>();
        if (!visitedEdgeIdentifiers.contains(edge.getIdentifier()))
        {
            Stream.of(edge).filter(ComplexHighwayAreaFinder::validEdge)
                    .map(ComplexHighwayAreaFinder::toEntity).filter(Optional::isPresent)
                    .map(Optional::get).forEach(area ->
                    {
                        visitedEdgeIdentifiers.addAll(area.getVisitedEdgeIdentifiers());
                        returnValue.add(area);
                    });
        }
        return returnValue;
    }
}
