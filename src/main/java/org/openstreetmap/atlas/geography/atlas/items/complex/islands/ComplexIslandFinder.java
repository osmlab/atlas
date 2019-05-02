package org.openstreetmap.atlas.geography.atlas.items.complex.islands;

import java.util.Set;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.items.Relation;
import org.openstreetmap.atlas.geography.atlas.items.Relation.Ring;
import org.openstreetmap.atlas.geography.atlas.items.complex.Finder;
import org.openstreetmap.atlas.geography.atlas.items.complex.waters.ComplexWaterEntityFinder;
import org.openstreetmap.atlas.geography.atlas.items.complex.waters.WaterConfigurationHandler;
import org.openstreetmap.atlas.streaming.resource.InputStreamResource;
import org.openstreetmap.atlas.streaming.resource.Resource;
import org.openstreetmap.atlas.utilities.collections.Iterables;
import org.openstreetmap.atlas.utilities.collections.Sets;

/**
 * @author Sid
 * @author sbhalekar
 */
public class ComplexIslandFinder implements Finder<ComplexIsland>
{
    private final WaterConfigurationHandler waterConfigurationHandler;

    private static final Set<String> ACCEPTABLE_WATER_FEATURES = Sets.hashSet("lake", "river",
            "reservoir");

    public ComplexIslandFinder()
    {
        this(new InputStreamResource(() -> ComplexWaterEntityFinder.class
                .getResourceAsStream(ComplexWaterEntityFinder.WATER_RESOURCE)));
    }

    public ComplexIslandFinder(final Resource resource)
    {
        this.waterConfigurationHandler = new WaterConfigurationHandler(resource);
    }

    private boolean testWaterType(final Relation relation)
    {
        return this.waterConfigurationHandler.getWaterHandlers().entrySet().stream()
                .filter(entry -> ACCEPTABLE_WATER_FEATURES.contains(entry.getKey()))
                .anyMatch(entry ->
                {
                    return entry.getValue().test(relation);
                });
    }

    @Override
    public Iterable<ComplexIsland> find(final Atlas atlas)
    {
        final Iterable<Relation> relations = atlas.relations(relation -> relation.isMultiPolygon()
                && relation.hasMultiPolygonMembers(Ring.INNER) && testWaterType(relation));
        return Iterables.translate(relations, ComplexIsland::new);
    }
}
