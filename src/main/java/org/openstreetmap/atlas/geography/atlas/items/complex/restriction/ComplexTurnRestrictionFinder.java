package org.openstreetmap.atlas.geography.atlas.items.complex.restriction;

import java.util.function.Predicate;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.complex.Finder;
import org.openstreetmap.atlas.tags.TurnRestrictionTag;
import org.openstreetmap.atlas.utilities.collections.Iterables;

/**
 * A simple finder for a {@link ComplexTurnRestriction}
 *
 * @author matthieun
 */
public class ComplexTurnRestrictionFinder implements Finder<ComplexTurnRestriction>
{
    private final Predicate<Edge> acceptableEdges;

    public ComplexTurnRestrictionFinder()
    {
        this(x -> true);
    }

    public ComplexTurnRestrictionFinder(final Predicate<Edge> acceptableEdges)
    {
        this.acceptableEdges = acceptableEdges == null ? x -> true : acceptableEdges;
    }

    @Override
    public Iterable<ComplexTurnRestriction> find(final Atlas atlas)
    {
        return Iterables.translate(atlas.relations(TurnRestrictionTag::isRestriction),
                relation -> new ComplexTurnRestriction(relation, this.acceptableEdges));
    }
}
