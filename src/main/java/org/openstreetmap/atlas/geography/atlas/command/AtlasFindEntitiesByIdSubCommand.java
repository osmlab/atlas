package org.openstreetmap.atlas.geography.atlas.command;

import java.io.PrintStream;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.utilities.runtime.Command.Optionality;
import org.openstreetmap.atlas.utilities.runtime.Command.Switch;
import org.openstreetmap.atlas.utilities.runtime.Command.SwitchList;
import org.openstreetmap.atlas.utilities.runtime.CommandMap;

/**
 * Creates a new packed atlas for testing purposes on any atlas items if an item matches a set of
 * expected osm identifiers.
 *
 * @author cstaylor
 */
public class AtlasFindEntitiesByIdSubCommand extends AbstractAtlasOutputTestSubCommand
{
    private static final Switch<Set<Long>> OSM_ID_PARAMETER = new Switch<>("osmid",
            "list of comma-delimited OSM Identifiers of the entities we want to export",
            possibleMultipleOSMIdentifier -> Stream.of(possibleMultipleOSMIdentifier.split(","))
                    .map(Long::parseLong).collect(Collectors.toSet()),
            Optionality.REQUIRED);

    private Set<Long> identifiers;

    public AtlasFindEntitiesByIdSubCommand()
    {
        super("atlas-with-this-entity",
                "Creates a new atlas containing the area around any items found that match the set of ids provided by the -osmid parameter");
    }

    @Override
    public SwitchList switches()
    {
        return super.switches().with(OSM_ID_PARAMETER);
    }

    @Override
    public void usage(final PrintStream writer)
    {
        super.usage(writer);
        writer.println(
                "-osmid=OSM identifier : comma-separated numeric osm identifiers of the items we're trying to locate");
    }

    @Override
    protected boolean filter(final AtlasEntity item)
    {
        return this.identifiers.contains(item.getOsmIdentifier());
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void start(final CommandMap command)
    {
        super.start(command);
        this.identifiers = (Set<Long>) command.get(OSM_ID_PARAMETER);
    }
}
