package org.openstreetmap.atlas.geography.atlas.items.complex.islands;

import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.items.complex.Finder;
import org.openstreetmap.atlas.geography.atlas.items.complex.WaterIslandConfigurationReader;
import org.openstreetmap.atlas.streaming.resource.Resource;
import org.openstreetmap.atlas.utilities.collections.Iterables;

/**
 * @author Sid
 * @author sbhalekar
 */
public class ComplexIslandFinder implements Finder<ComplexIsland>
{
    private final WaterIslandConfigurationReader islandConfigurationReader;

    public ComplexIslandFinder()
    {
        this.islandConfigurationReader = new DefaultIslandConfigurationReader("islands.json");
    }

    public ComplexIslandFinder(final Resource resource)
    {
        this.islandConfigurationReader = new DefaultIslandConfigurationReader(resource);
    }

    /**
     * Use the configuration reader and convert all the allowed atlas entities into
     * {@link ComplexIsland}
     *
     * @param atlas
     *            The {@link Atlas} to browse.
     * @return an {@link Iterable} of the {@link ComplexIsland}s in the given {@link Atlas}
     */
    @Override
    public Iterable<ComplexIsland> find(final Atlas atlas)
    {
        return StreamSupport
                .stream(Iterables
                        .translate(atlas.entities(), this.islandConfigurationReader::convert)
                        .spliterator(), false)
                .filter(Optional::isPresent).map(Optional::get)
                .filter(object -> object instanceof ComplexIsland)
                .map(object -> (ComplexIsland) object).collect(Collectors.toList());
    }
}
