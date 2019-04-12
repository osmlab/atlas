package org.openstreetmap.atlas.geography.atlas.items.complex.islands;

import java.util.Map;
import java.util.Set;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.items.Relation;
import org.openstreetmap.atlas.geography.atlas.items.Relation.Ring;
import org.openstreetmap.atlas.geography.atlas.items.complex.Finder;
import org.openstreetmap.atlas.geography.atlas.items.complex.waters.ComplexWaterEntityFinder;
import org.openstreetmap.atlas.streaming.resource.File;
import org.openstreetmap.atlas.streaming.resource.InputStreamResource;
import org.openstreetmap.atlas.utilities.collections.Iterables;
import org.openstreetmap.atlas.utilities.collections.Sets;
import org.openstreetmap.atlas.utilities.configuration.Configuration;
import org.openstreetmap.atlas.utilities.configuration.ConfigurationReader;
import org.openstreetmap.atlas.utilities.configuration.ConfiguredFilter;
import org.openstreetmap.atlas.utilities.configuration.StandardConfiguration;

/**
 * @author Sid
 * @author sbhalekar
 */
public class ComplexIslandFinder implements Finder<ComplexIsland>
{
    private final Configuration configuration;

    private final Set<String> validWaterTypes;

    private static final Set<String> ACCEPTABLE_WATER_FEATURES = Sets.hashSet("lake", "river",
            "reservoir");

    public ComplexIslandFinder()
    {
        this.configuration = new StandardConfiguration(
                new InputStreamResource(() -> ComplexWaterEntityFinder.class
                        .getResourceAsStream(ComplexWaterEntityFinder.WATER_RESOURCE)));
        this.validWaterTypes = getWaterTypesFromConfiguration();
    }

    public ComplexIslandFinder(final String resource)
    {
        this.configuration = new StandardConfiguration(new File(resource));
        this.validWaterTypes = getWaterTypesFromConfiguration();
    }

    private Set<String> getWaterTypesFromConfiguration()
    {
        final Set<String> validWaterTypes = new ConfigurationReader(
                ConfiguredFilter.CONFIGURATION_ROOT).configurationValue(configuration,
                        Map<String, Object>::keySet);

        // Right now we handle only islands in Lakes, Rivers and Reservoirs.
        validWaterTypes.retainAll(ACCEPTABLE_WATER_FEATURES);
        return validWaterTypes;
    }

    private boolean testWaterType(final Relation relation)
    {
        return this.validWaterTypes.stream()
                .anyMatch(type -> ConfiguredFilter.from(type, configuration).test(relation));
    }

    @Override
    public Iterable<ComplexIsland> find(final Atlas atlas)
    {
        final Iterable<Relation> relations = atlas.relations(relation -> relation.isMultiPolygon()
                && relation.hasMultiPolygonMembers(Ring.INNER) && testWaterType(relation));
        return Iterables.translate(relations, ComplexIsland::new);
    }
}
