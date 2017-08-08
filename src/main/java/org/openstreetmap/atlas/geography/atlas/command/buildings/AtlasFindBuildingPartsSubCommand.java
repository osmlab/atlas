package org.openstreetmap.atlas.geography.atlas.command.buildings;

import java.io.PrintStream;
import java.util.function.Predicate;
import java.util.stream.StreamSupport;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.command.AbstractAtlasSubCommand;
import org.openstreetmap.atlas.geography.atlas.command.AtlasCommandConstants;
import org.openstreetmap.atlas.geography.atlas.items.AtlasItem;
import org.openstreetmap.atlas.tags.BuildingPartTag;
import org.openstreetmap.atlas.tags.Taggable;
import org.openstreetmap.atlas.tags.annotations.validation.Validators;
import org.openstreetmap.atlas.utilities.runtime.CommandMap;

/**
 * Outputs all of the atlas items that have building:part tags
 *
 * @author cstaylor
 */
public class AtlasFindBuildingPartsSubCommand extends AbstractAtlasSubCommand
{
    private static void output(final AtlasItem item)
    {
        System.out.printf("[%25s] [%9d] %s\n", item.getAtlas().getName(), item.getOsmIdentifier(),
                item);
    }

    public AtlasFindBuildingPartsSubCommand()
    {
        super("building-parts", "Task for finding all of the OSM ids with building:part=yes");
    }

    @Override
    public void usage(final PrintStream writer)
    {
        writer.printf(AtlasCommandConstants.INPUT_PARAMETER_DESCRIPTION);
    }

    @Override
    protected void handle(final Atlas atlas, final CommandMap command)
    {
        final Predicate<Taggable> filter = Validators.hasValuesFor(BuildingPartTag.class);
        StreamSupport.stream(atlas.items().spliterator(), true).filter(filter)
                .forEach(AtlasFindBuildingPartsSubCommand::output);
    }
}
