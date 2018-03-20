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
 * Command that takes the data mining concat.csv and appends the slippytile counts for every zoom
 * layer to the file. This is meant to be used as a pre-processing step before creating the sharding
 * tree in DynamicTileSharding.
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
            "The resource where to save the formatted probe data counts.", File::new,
            Optionality.REQUIRED);
    private static final Switch<Integer> ZOOM = new Switch<>("zoom",
            "The zoom layer the csv represents", Integer::valueOf, Optionality.REQUIRED);

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

        // read in csv and put in slippytile keyed hashmap of counts
        int numberLines = 0;
        for (@SuppressWarnings("unused")
        final String line : definition.lines())
        {
            numberLines++;
        }
        logger.info("There are {} tiles.", numberLines);
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

        // start at level 17
        // take boxes of 4, take the sum of those boxes, write it to the csv with the corresponding
        // zoom 16 slippy tile name
        // run this for every zoom layer until 0
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

    private HashMap<SlippyTile, Long> writeTileCountsForZoom(final int zoomLayerToGenerate,
            final HashMap<SlippyTile, Long> countsAtHigherZoom)
    {
        long count = 0;
        long counter = 0;
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
                // write to file the slippy tile this count represents
                if (count != 0)
                {
                    countsAtThisZoom.put(new SlippyTile(x / 2, y / 2, zoomLayerToGenerate), count);
                }
                if (++counter % READER_REPORT_FREQUENCY == 0)
                {
                    logger.info("Calculated {} zoom level {} tiles.", counter, zoomLayerToGenerate);
                }
            }
        }
        return countsAtThisZoom;
    }

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
