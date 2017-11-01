package org.openstreetmap.atlas.geography.atlas.items.complex.restriction;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.StreamSupport;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.Rectangle;
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.Relation;
import org.openstreetmap.atlas.geography.atlas.items.Route;
import org.openstreetmap.atlas.geography.atlas.items.TurnRestriction;
import org.openstreetmap.atlas.geography.atlas.items.complex.ComplexEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A Complex turn restriction from one atlas.
 *
 * @author matthieun
 * @author cstaylor
 */
public class ComplexTurnRestriction extends ComplexEntity
{
    private static final Logger logger = LoggerFactory.getLogger(ComplexTurnRestriction.class);

    private static final long serialVersionUID = 8558201688502883714L;

    private TurnRestriction turnRestriction;

    protected ComplexTurnRestriction(final AtlasEntity source, final Predicate<Edge> validEdge)
    {
        super(source);
        try
        {
            this.turnRestriction = TurnRestriction.from((Relation) source)
                    .orElseThrow(() -> new CoreException(
                            "{} is not a turn restriction according to Atlas",
                            source.getIdentifier()));

            final Route route = this.turnRestriction.route();
            final int routeLength = route.size();

            if (routeLength < 2)
            {
                throw new CoreException("Must have at least two edges in the route");
            }

            final long filteredLength = StreamSupport.stream(route.spliterator(), false)
                    .filter(validEdge).count();
            if (filteredLength < routeLength)
            {
                throw new CoreException("{} invalid edges", routeLength - filteredLength);
            }
        }
        catch (final Exception oops)
        {
            logger.trace("Unable to create ComplexTurnRestriction from {}", source, oops);
            setInvalidReason("Couldn't create ComplexTurnRestriction", oops);
        }
    }

    @Override
    public Rectangle bounds()
    {
        return this.route().bounds();
    }

    @Override
    public List<ComplexEntityError> getAllInvalidations()
    {
        final List<ComplexEntityError> returnValue = new ArrayList<>();
        if (!isValid())
        {
            returnValue.add(new ComplexEntityError(this, "turn restriction is null"));
        }
        return returnValue;
    }

    public TurnRestriction getTurnRestriction()
    {
        return this.turnRestriction;
    }

    /**
     * Proxy for TurnRestriction.route()
     *
     * @return The Route represented by this {@link ComplexTurnRestriction}
     */
    public Route route()
    {
        if (this.turnRestriction != null)
        {
            return this.turnRestriction.route();
        }
        else
        {
            return null;
        }
    }

    @Override
    public String toString()
    {
        return "[ComplexTurnRestriction: " + this.turnRestriction + "]";
    }
}
