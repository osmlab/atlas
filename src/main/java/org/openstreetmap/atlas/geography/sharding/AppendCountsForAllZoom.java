package org.openstreetmap.atlas.geography.sharding;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.HashMap;

import org.openstreetmap.atlas.streaming.Streams;
import org.openstreetmap.atlas.streaming.resource.File;
import org.openstreetmap.atlas.streaming.resource.WritableResource;
import org.openstreetmap.atlas.utilities.collections.StringList;
import org.openstreetmap.atlas.utilities.runtime.Command;
import org.openstreetmap.atlas.utilities.runtime.CommandMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Command that takes a file with feature counts for each {@link SlippyTile} at a certain zoom
 * level, and then calculates summed counts for every lower zoom layer to the file. This is meant to
 * be used as a pre-processing step before creating the sharding tree in
 * {@link DynamicTileSharding}.
 *
 * @author james-gage
 * @author lcram
 */
public class AppendCountsForAllZoom extends Command
{

    public static final Switch<WritableResource> DEFINITION = new Switch<>("definition",
            "Resource containing tile to feature count mapping for zoom 0 to maxZoom-1.", File::new,
            Optionality.REQUIRED);
    public static final Switch<WritableResource> OUTPUT = new Switch<>("output",
            "The resource where to save the feature counts for all zooms.", File::new,
            Optionality.REQUIRED);
    private static final Switch<Integer> ZOOM = new Switch<>("zoom",
            "The zoom layer the input csv represents", Integer::valueOf, Optionality.REQUIRED);

    private static final Logger logger = LoggerFactory.getLogger(AppendCountsForAllZoom.class);
    private static final int READER_REPORT_FREQUENCY = 10_000_000;

    public static void main(final String[] args)
    {
        new AppendCountsForAllZoom().run(args);
    }

    @Override
    protected int onRun(final CommandMap command)
    {
        final Integer zoom = (Integer) command.get(ZOOM);
        final WritableResource definition = (WritableResource) command.get(DEFINITION);
        final WritableResource output = (WritableResource) command.get(OUTPUT);
        final BufferedWriter bufferedWriter = output.writer();

        final int numberLines = linesInFile(definition);
        HashMap<SlippyTile, Long> countsAtZoom = new HashMap<>(numberLines);
        int counter = 0;
        for (final String line : definition.lines())
        {
            final StringList split = StringList.split(line, ",");
            final SlippyTile tile = SlippyTile.forName(split.get(0));
            countsAtZoom.put(tile, Long.valueOf(split.get(1)));
            if (++counter % READER_REPORT_FREQUENCY == 0)
            {
                logger.info("Read counts for {} zoom level {} tiles.", counter, zoom);
            }
        }
        writeToFile(countsAtZoom, bufferedWriter);

        for (int i = zoom - 1; i >= 0; i--)
        {
            countsAtZoom = writeTileCountsForZoom(i, countsAtZoom);
            writeToFile(countsAtZoom, bufferedWriter);
            logger.info("Wrote tiles for zoom {} to file!", i);
        }
        try
        {
            bufferedWriter.flush();
            bufferedWriter.close();
        }
        catch (final IOException e)
        {
            logger.error("Error closing file", bufferedWriter);
        }
        finally
        {
            Streams.close(bufferedWriter);
        }
        return 0;
    }

    @Override
    protected SwitchList switches()
    {
        return new SwitchList().with(DEFINITION, OUTPUT, ZOOM);
    }

    private int linesInFile(final WritableResource definition)
    {
        int numberLines = 0;
        for (@SuppressWarnings("unused")
        final String line : definition.lines())
        {
            numberLines++;
        }
        logger.info("There are {} tiles.", numberLines);
        return numberLines;
    }

    /**
     * Generates the counts for the nth zoom layer based on information from zoom layer n+1.
     *
     * @param zoomLayerToGenerate
     *            the zoom layer for which to generate counts
     * @param countsAtHigherZoom
     *            HashMap containing counts for all {@link SlippyTile} in zoomLayerToGenerate+1
     * @return a HashMap containing counts for all (@link SlippyTile} in zoomLayerToGenerate
     */
    private HashMap<SlippyTile, Long> writeTileCountsForZoom(final int zoomLayerToGenerate,
            final HashMap<SlippyTile, Long> countsAtHigherZoom)
    {
        long count = 0;
        long tilesCalculated = 0;
        final HashMap<SlippyTile, Long> countsAtThisZoom = new HashMap<>();
        for (int x = 0; x < Math.pow(2, zoomLayerToGenerate + 1); x += 2)
        {
            for (int y = 0; y < Math.pow(2, zoomLayerToGenerate + 1); y += 2)
            {
                count = 0;
                // top left
                count += countsAtHigherZoom
                        .getOrDefault(new SlippyTile(x, y, zoomLayerToGenerate + 1), (long) 0);
                // top right
                count += countsAtHigherZoom
                        .getOrDefault(new SlippyTile(x + 1, y, zoomLayerToGenerate + 1), (long) 0);
                // bottom left
                count += countsAtHigherZoom
                        .getOrDefault(new SlippyTile(x, y + 1, zoomLayerToGenerate + 1), (long) 0);
                // bottom right
                count += countsAtHigherZoom.getOrDefault(
                        new SlippyTile(x + 1, y + 1, zoomLayerToGenerate + 1), (long) 0);
                if (count != 0)
                {
                    countsAtThisZoom.put(new SlippyTile(x / 2, y / 2, zoomLayerToGenerate), count);
                }
                if (++tilesCalculated % READER_REPORT_FREQUENCY == 0)
                {
                    logger.info("Calculated {} zoom level {} tiles.", tilesCalculated,
                            zoomLayerToGenerate);
                }
            }
        }
        return countsAtThisZoom;
    }

    /**
     * Writes to file all the {@link SlippyTile} feature counts at a certain zoom level.
     *
     * @param countsAtZoom
     *            HashMap containing the feature counts at that zoom level.
     * @param bufferedWriter
     *            The writer to write to.
     */

    private void writeToFile(final HashMap<SlippyTile, Long> countsAtZoom,
            final BufferedWriter bufferedWriter)
    {
        final String lineSeparator = System.getProperty("line.separator");
        countsAtZoom.forEach((tile, count) ->
        {
            try
            {
                bufferedWriter.write(tile.getName() + "," + count);
                bufferedWriter.write(lineSeparator);
            }
            catch (final IOException e)
            {
                logger.error("Error writing converted data to {}", bufferedWriter);
            }
        });
    }
}
