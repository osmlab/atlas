package org.openstreetmap.atlas.geography.atlas.command;

import java.io.PrintStream;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.utilities.runtime.Command;
import org.openstreetmap.atlas.utilities.runtime.CommandMap;

/**
 * Searches a collection of Atlases for a set of ids. The Atlases containing each id are reported.
 * Optionally, all Atlases that contain one of the ids are joined and output to a single Atlas file.
 *
 * @author bbreithaupt
 */
public class AtlasFindByAtlasIdentifierSubCommand extends AbstractAtlasSubCommand
{
    private static final Command.Switch<Set<Long>> ATLAS_ID_PARAMETER = new Command.Switch<>("id",
            "List of comma-delimited Atlas identifiers",
            possibleMultipleOSMIdentifier -> Stream.of(possibleMultipleOSMIdentifier.split(","))
                    .map(Long::parseLong).collect(Collectors.toSet()),
            Command.Optionality.REQUIRED);

    private static final Command.Switch<String> JOINED_OUTPUT_PARAMETER = new Command.Switch<>(
            "joinedOutput",
            "The Atlas file to save the joined output to (optional). If not passed the found shards will not be joined and only appear in the console.",
            String::toString, Command.Optionality.OPTIONAL);

    private final Set<Long> identifiers = new HashSet<>();

    private final Set<String> shardNames = new HashSet<>();

    public AtlasFindByAtlasIdentifierSubCommand()
    {
        super("find-atlas-id",
                "Find which atlas files contain particular Atlas features using a given set of Atlas identifiers");
    }

    @Override
    public Command.SwitchList switches()
    {
        return super.switches().with(ATLAS_ID_PARAMETER, JOINED_OUTPUT_PARAMETER);
    }

    @Override
    public void usage(final PrintStream writer)
    {
        writer.printf(AtlasCommandConstants.INPUT_PARAMETER_DESCRIPTION);
        writer.printf("-id=1000000,2000000 : comma separated Atlas identifiers to search for\n");
        writer.printf("-joinedOutput=path/to/joined.atlas : the path to the output Atlas file\n");
    }

    @Override
    protected void start(final CommandMap command)
    {
        // Collect ids
        this.identifiers.addAll((Set) command.get(ATLAS_ID_PARAMETER));
    }

    @Override
    protected void handle(final Atlas atlas, final CommandMap command)
    {
        // Get all atlas entities with ids matching the input list
        atlas.entities(identifierCheck()).forEach(item ->
        {
            // Print atlas and item information
            System.out.printf(formatAtlasObject(item));
            // Record shard name
            this.shardNames.add(atlas.getName());
        });
    }

    @Override
    protected int finish(final CommandMap command)
    {
        final Optional output = command.getOption(JOINED_OUTPUT_PARAMETER);
        // If joining is requested and there are shards to join...
        if (output.isPresent() && !this.shardNames.isEmpty())
        {
            System.out.printf("Stitching shards and saving to output %s\n", output.get());
            // Use AtlasJoinerSubCommand to join found atlases
            AtlasReader.main("join",
                    String.format("-input=%s", command.get(super.switches().get(0))),
                    String.format("-output=%s", output.get()),
                    String.format("-atlases=%s", String.join(",", this.shardNames)));
        }
        return 0;
    }

    /**
     * Predicate to check an {@link AtlasObject} against the list of ids.
     *
     * @param <T>
     *            Object type
     * @return {@link Predicate}
     */
    private <T extends AtlasObject> Predicate<T> identifierCheck()
    {
        return object -> this.identifiers.contains(object.getIdentifier());
    }

    /**
     * Creates an informative {@link String} for an {@link AtlasEntity} and the {@link Atlas} that
     * contains it.
     *
     * @param entity
     *            {@link AtlasEntity} to create the string for
     * @return formatted string
     */
    private String formatAtlasObject(final AtlasEntity entity)
    {
        final String shardName = entity.getAtlas().metaData().getShardName().orElse("UNKNOWN");
        return String.format("[%s] [%d] [%d] --> [%s:%s] Tags: [%s]\n", entity.getType(),
                entity.getOsmIdentifier(), entity.getIdentifier(), shardName,
                entity.getAtlas().getName(), entity.getTags());
    }
}
