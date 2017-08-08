package org.openstreetmap.atlas.geography.atlas.command;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.StreamSupport;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.packed.PackedAtlas;
import org.openstreetmap.atlas.geography.atlas.packed.PackedAtlasCloner;
import org.openstreetmap.atlas.geography.sharding.SlippyTile;
import org.openstreetmap.atlas.streaming.resource.File;
import org.openstreetmap.atlas.streaming.resource.FileSuffix;
import org.openstreetmap.atlas.utilities.runtime.Command;
import org.openstreetmap.atlas.utilities.runtime.CommandMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Create several atlas files from one atlas file as input with fixed zoom level using SlippyTile as
 * the sharding method
 *
 * @author yalimu
 */
public class AtlasSplitterWithSlippyTileCommand extends AbstractAtlasSubCommand
{
    private static final Logger logger = LoggerFactory
            .getLogger(AtlasSplitterWithSlippyTileCommand.class);

    private static final Command.Switch<Integer> ZOOM_LEVEL = new Command.Switch<>("zoom_level",
            "Input zoom level", Integer::new, Command.Optionality.REQUIRED);
    private static final Command.Switch<Path> OUTPUT_FOLDER_PARAMETER = new Command.Switch<>(
            "output", "The path to save Atlas files", Paths::get, Command.Optionality.REQUIRED);

    public AtlasSplitterWithSlippyTileCommand()
    {
        super("split", "Split one Atlas file into several small Atlas files with fixed zoom level");
    }

    @Override
    public Command.SwitchList switches()
    {
        return super.switches().with(ZOOM_LEVEL, OUTPUT_FOLDER_PARAMETER);
    }

    @Override
    protected void handle(final Atlas atlas, final CommandMap command)
    {
        final int zoomLevel = (int) command.get(ZOOM_LEVEL);
        final Path path = (Path) command.get(OUTPUT_FOLDER_PARAMETER);
        try
        {
            Files.createDirectories(path);
        }
        catch (final IOException e)
        {
            logger.error("Error when creating output directory", e);
            return;
        }

        StreamSupport.stream(SlippyTile.allTiles(zoomLevel, atlas.bounds()).spliterator(), false)
                .map(tile -> buildAtlasBasedOnTile(tile, atlas)).forEach(newAtlas ->
                {
                    if (newAtlas != null)
                    {
                        final String outputFileName = path.toString() + "/" + atlas.getName() + "_"
                                + newAtlas.getIdentifier() + FileSuffix.ATLAS;
                        logger.info("Saving Atlas file into {}", outputFileName);
                        newAtlas.save(new File(outputFileName));
                    }
                });
    }

    private PackedAtlas buildAtlasBasedOnTile(final SlippyTile tile, final Atlas atlas)
    {
        return atlas.subAtlas(tile.bounds())
                .map(subAtlas -> new PackedAtlasCloner().cloneFrom(subAtlas)).orElse(null);
    }

    @Override
    public void usage(final PrintStream writer)
    {
        writer.printf(AtlasCommandConstants.INPUT_PARAMETER_DESCRIPTION);
        writer.printf(AtlasCommandConstants.INPUT_ZOOM_LEVEL);
        writer.printf(AtlasCommandConstants.OUTPUT_FOLDER_DESCRIPTION);
    }
}
