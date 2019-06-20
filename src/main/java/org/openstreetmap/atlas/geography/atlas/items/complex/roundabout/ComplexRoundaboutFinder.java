package org.openstreetmap.atlas.geography.atlas.items.complex.roundabout;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.complex.Finder;
import org.openstreetmap.atlas.utilities.collections.Iterables;

/**
 * {@link Finder} for {@link ComplexRoundabout}s.
 *
 * @author bbreithaupt
 */
public class ComplexRoundaboutFinder implements Finder<ComplexRoundabout>
{
    private final Set<Long> checkedIds = new HashSet<>();

    @Override
    public Iterable<ComplexRoundabout> find(final Atlas atlas)
    {
        final List<ComplexRoundabout> complexRoundabouts = new ArrayList<>();
        Iterables.stream(atlas.edges()).forEach(edge ->
        {
            if (!this.checkedIds.contains(edge.getIdentifier()))
            {
                final ComplexRoundabout complexRoundabout = new ComplexRoundabout(edge);
                this.checkedIds.addAll(complexRoundabout.getRoundaboutEdgeSet().stream()
                        .map(Edge::getIdentifier).collect(Collectors.toSet()));
                if (complexRoundabout.isValid())
                {
                    complexRoundabouts.add(complexRoundabout);
                }
            }
        });
        return complexRoundabouts;
    }
}
