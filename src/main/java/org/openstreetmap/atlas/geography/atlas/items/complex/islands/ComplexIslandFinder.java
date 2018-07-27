package org.openstreetmap.atlas.geography.atlas.items.complex.islands;

import static org.openstreetmap.atlas.geography.atlas.items.complex.waters.handler.LakeHandler.isLake;
import static org.openstreetmap.atlas.geography.atlas.items.complex.waters.handler.ReservoirHandler.isReservoir;
import static org.openstreetmap.atlas.geography.atlas.items.complex.waters.handler.RiverHandler.isRiver;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.items.Relation;
import org.openstreetmap.atlas.geography.atlas.items.Relation.Ring;
import org.openstreetmap.atlas.geography.atlas.items.complex.Finder;
import org.openstreetmap.atlas.utilities.collections.Iterables;

/**
 * @author Sid
 */
public class ComplexIslandFinder implements Finder<ComplexIsland>
{
    @Override
    public Iterable<ComplexIsland> find(final Atlas atlas)
    {
        /*
         * Right now we handle only islands in Lakes, Rivers and Reservoirs. This might need to
         * handle other water bodies too
         */
        final Iterable<Relation> relations = atlas.relations(
                relation -> relation.isMultiPolygon() && relation.hasMultiPolygonMembers(Ring.INNER)
                        && (isLake(relation) || isRiver(relation) || isReservoir(relation)));
        return Iterables.translate(relations, ComplexIsland::new);
    }
}
