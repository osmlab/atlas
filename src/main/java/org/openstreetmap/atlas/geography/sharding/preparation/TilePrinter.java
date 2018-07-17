package org.openstreetmap.atlas.geography.sharding.preparation;

import java.util.Iterator;

import org.openstreetmap.atlas.geography.Rectangle;
import org.openstreetmap.atlas.geography.sharding.SlippyTile;
import org.openstreetmap.atlas.streaming.Streams;
import org.openstreetmap.atlas.streaming.resource.File;
import org.openstreetmap.atlas.streaming.writers.SafeBufferedWriter;
import org.openstreetmap.atlas.utilities.runtime.Command;
import org.openstreetmap.atlas.utilities.runtime.CommandMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Print all the rectangles of all {@link SlippyTile}s at a specified zoom level in a {@link File}.
 * This class creates a SQL file that can be executed towards an OSM database, and will return the
 * way counts for each slippy tile.
 *
 * @author matthieun
 */
public class TilePrinter extends Command
{
    private static final Logger logger = LoggerFactory.getLogger(TilePrinter.class);
    private static final int MAX_SHARDS_PER_FILE = 5_000_000;

    private static final Switch<File> OUTPUT_FOLDER = new Switch<>("output", "The output folder",
            value -> new File(value));
    private static final Switch<Integer> ZOOM_SWITCH = new Switch<>("zoom", "The zoom",
            value -> Integer.valueOf(value));

    private int index;
    private File folder;
    private int zoom;

    public static void main(final String[] args)
    {
        new TilePrinter().run(args);
    }

    @Override
    protected int onRun(final CommandMap command)
    {
        this.index = 0;
        this.zoom = (int) command.get(ZOOM_SWITCH);
        this.folder = (File) command.get(OUTPUT_FOLDER);
        this.folder.mkdirs();
        SafeBufferedWriter writer = getNextFile().writer();
        writer.writeLine("CREATE SCHEMA sharding AUTHORIZATION osm;");
        writer.writeLine("DROP TABLE sharding.tiles;");
        writer.writeLine(
                "CREATE TABLE sharding.tiles(tile text, bounds geometry) WITH ( OIDS=FALSE );");
        writer.writeLine("ALTER TABLE sharding.tiles OWNER TO osm;");
        writer.writeLine("DROP TABLE sharding.counts;");
        writer.writeLine(
                "CREATE TABLE sharding.counts(tile text, count integer) WITH ( OIDS=FALSE );");
        writer.writeLine("ALTER TABLE sharding.counts OWNER TO osm;");

        final Iterator<SlippyTile> tileIterator = SlippyTile.allTilesIterator(this.zoom,
                Rectangle.MAXIMUM);
        while (tileIterator.hasNext())
        {
            Streams.close(writer);
            writer = getNextFile().writer();
            writer.writeLine("INSERT INTO sharding.tiles(tile, bounds) VALUES ");
            SlippyTile tile = null;
            int counter = 0;
            while (tileIterator.hasNext() && counter < MAX_SHARDS_PER_FILE)
            {
                tile = tileIterator.next();
                final Rectangle bounds = tile.bounds();
                writer.write("(\'" + tile.getName() + "\', ST_MakeEnvelope("
                        + bounds.lowerLeft().getLongitude().asDegrees() + ","
                        + bounds.lowerLeft().getLatitude().asDegrees() + ","
                        + bounds.upperRight().getLongitude().asDegrees() + ","
                        + bounds.upperRight().getLatitude().asDegrees() + ",4326))");
                counter++;
                if (tileIterator.hasNext() && counter < MAX_SHARDS_PER_FILE)
                {
                    writer.write(",");
                }
                else
                {
                    writer.write(";");
                }
                writer.write("\n");
            }
        }
        Streams.close(writer);
        writer = getNextFile().writer();
        writer.writeLine("CREATE OR REPLACE FUNCTION countForTiles() RETURNS void AS $$");
        writer.writeLine("DECLARE");
        writer.writeLine("s_tile text;");
        writer.writeLine("s_bounds geometry;");
        writer.writeLine("s_count integer;");
        writer.writeLine("BEGIN");
        writer.writeLine("    FOR s_tile, s_bounds IN SELECT tile, bounds FROM sharding.tiles");
        writer.writeLine("    LOOP");
        writer.writeLine(
                "        SELECT count(*) INTO s_count FROM public.ways WHERE ST_Intersects(s_bounds, linestring);");
        writer.writeLine(
                "        INSERT INTO sharding.counts(tile,count) VALUES (s_tile, s_count);");
        writer.writeLine("    END LOOP;");
        writer.writeLine("    RETURN;");
        writer.writeLine("END");
        writer.writeLine("$$ LANGUAGE 'plpgsql' ;");
        Streams.close(writer);
        return 0;
    }

    @Override
    protected SwitchList switches()
    {
        return new SwitchList().with(ZOOM_SWITCH, OUTPUT_FOLDER);
    }

    private File getNextFile()
    {
        final File result = this.folder.child("tiles-" + this.zoom + "_" + this.index++ + ".sql");
        logger.info("Generating file {}", result);
        return result;
    }
}
