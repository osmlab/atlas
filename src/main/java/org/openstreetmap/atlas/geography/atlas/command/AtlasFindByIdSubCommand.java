package org.openstreetmap.atlas.geography.atlas.command;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.utilities.runtime.Command;
import org.openstreetmap.atlas.utilities.runtime.CommandMap;

import java.io.PrintStream;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AtlasFindByIdSubCommand extends AbstractAtlasSubCommand
{
    private static final Command.Switch<Set<String>> FEATURE_ID_PARAMETER = new Command.Switch<>(
            "id", "list of comma-delimited Atlas extended feature identifier",
            possibleMultipleOSMIdentifier -> Stream.of(possibleMultipleOSMIdentifier.split(","))
                    .collect(Collectors.toSet()), Command.Optionality.REQUIRED);

    private final Set<Long> ids = new HashSet<>();

    public AtlasFindByIdSubCommand()
    {
        super("find-id",
                "find which atlas files contain particular Atlas features using a given set of Atlas identifiers");
    }

    @Override public Command.SwitchList switches()
    {
        return super.switches().with(FEATURE_ID_PARAMETER);
    }

    @Override public void usage(final PrintStream writer)
    {
        writer.printf(AtlasCommandConstants.INPUT_PARAMETER_DESCRIPTION);
        writer.printf(
                "-id=1000000,2000000 : comma separated Atlas feature identifiers to search for\n");
    }

    @Override protected void start(final CommandMap command)
    {
        this.ids.addAll((Set) command.get(FEATURE_ID_PARAMETER));
    }

    @Override protected void handle(final Atlas atlas, final CommandMap command)
    {
        atlas.nodes(idCheck()).forEach(item -> {
            System.out.printf(formatAtlasObject("NODE", atlas, item));
        });

        atlas.points(idCheck()).forEach(item -> {
            System.out.printf(formatAtlasObject("POINT", atlas, item));
        });

        atlas.edges(idCheck()).forEach(item -> {
            System.out.printf(formatAtlasObject("EDGE", atlas, item));
        });

        atlas.lines(idCheck()).forEach(item -> {
            System.out.printf(formatAtlasObject("LINE", atlas, item));
        });

        atlas.areas(idCheck()).forEach(item -> {
            System.out.printf(formatAtlasObject("AREA", atlas, item));
        });

        atlas.relations(idCheck()).forEach(item -> {
            System.out.printf(formatAtlasObject("RELATION", atlas, item));
        });
    }

    private <T extends AtlasObject> Predicate<T> idCheck(){
        return object -> this.ids.contains(((Long) object.getIdentifier()).toString());
    }

    private String formatAtlasObject(final String type, final Atlas atlas,
            final AtlasEntity entity)
    {
        final String shardName = atlas.metaData().getShardName().orElse("UNKNOWN");
        return String.format("[%s] [%d] [%d] --> [%s:%s] Tags: [%s]\n", type,
                entity.getOsmIdentifier(), entity.getIdentifier(), shardName, atlas.getName(),
                entity.getTags());
    }
}
