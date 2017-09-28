package org.openstreetmap.atlas.geography.sharding;

import java.util.Iterator;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.MultiPolygon;
import org.openstreetmap.atlas.geography.Rectangle;
import org.openstreetmap.atlas.utilities.collections.Iterables;
import org.openstreetmap.atlas.utilities.runtime.Command;
import org.openstreetmap.atlas.utilities.runtime.CommandMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author matthieun
 * @author mgostintsev
 */
public class SlippyTileTest extends Command
{
    private static final Logger logger = LoggerFactory.getLogger(SlippyTileTest.class);

    private static final Switch<Rectangle> BOUNDS = new Switch<>("bounds",
            "The bounds to get all the tiles from", bounds -> Rectangle.forString(bounds),
            Optionality.REQUIRED);
    private static final Switch<Integer> ZOOM = new Switch<>("zoom", "The zoom level for the tiles",
            zoom -> Integer.valueOf(zoom), Optionality.REQUIRED);

    public static void main(final String[] args)
    {
        new SlippyTileTest().run(args);
    }

    @Test
    public void testAll()
    {
        Assert.assertEquals(1_024, Iterables.size(SlippyTile.allTiles(5, Rectangle.MAXIMUM)));
        Assert.assertEquals(1, Iterables.size(SlippyTile.allTiles(17, Rectangle.TEST_RECTANGLE_2)));
    }

    @Test
    public void testMultiPolygonShards()
    {
        final MultiPolygon multiPolygon = MultiPolygon
                .wkt("MULTIPOLYGON (((40 40, 20 45, 45 30, 40 40)),"
                        + "((20 35, 10 30, 10 10, 30 5, 45 20, 20 35),"
                        + "(30 20, 20 15, 20 25, 30 20)))");
        @SuppressWarnings("unchecked")
        final Set<Shard> coveredShards = (Set<Shard>) new SlippyTileSharding(8)
                .shards(multiPolygon);
        Assert.assertFalse(coveredShards.contains(SlippyTile.forName("8-145-113")));
        Assert.assertTrue(coveredShards.contains(SlippyTile.forName("8-146-111")));
        Assert.assertFalse(coveredShards.contains(SlippyTile.forName("8-167-108")));
        Assert.assertTrue(coveredShards.contains(SlippyTile.forName("8-152-97")));
    }

    @Test
    public void testNeighbors()
    {
        final Iterator<SlippyTile> iterator = SlippyTile.allTiles(2, Rectangle.TEST_RECTANGLE_2)
                .iterator();
        while (iterator.hasNext())
        {
            final SlippyTile next = iterator.next();
            Assert.assertEquals(5, next.neighbors().size());
        }

        final SlippyTile tile = new SlippyTile(659, 1588, 12);
        Assert.assertEquals(12, tile.neighbors().size());
    }

    @Test
    public void testParent()
    {
        final SlippyTile tile = new SlippyTile(659, 1588, 12);
        final SlippyTile parent = tile.parent();
        logger.info("Tile: {}, Parent: {}", tile, parent);
        Assert.assertEquals(329, parent.getX());
        Assert.assertEquals(794, parent.getY());
        Assert.assertEquals(11, parent.getZoom());
    }

    @Test
    public void testSplit()
    {
        final SlippyTile tile = new SlippyTile(659, 1588, 12);
        final Set<SlippyTile> children = Iterables.asSet(tile.split());
        logger.info("Tile: {}, Children: {}", tile, children);
        Assert.assertEquals(4, children.size());
        Assert.assertTrue(children.contains(new SlippyTile(1318, 3176, 13)));
        Assert.assertTrue(children.contains(new SlippyTile(1318, 3177, 13)));
        Assert.assertTrue(children.contains(new SlippyTile(1319, 3176, 13)));
        Assert.assertTrue(children.contains(new SlippyTile(1319, 3177, 13)));
    }

    @Test
    public void testSplitZoomIn()
    {
        final SlippyTile tile = new SlippyTile(659, 1588, 12);
        final Set<SlippyTile> children = Iterables.asSet(tile.split(14));
        logger.info("Tile: {}, Children: {}", tile, children);
        Assert.assertEquals(16, children.size());
        Assert.assertTrue(children.contains(new SlippyTile(2638, 6354, 14)));
        Assert.assertTrue(children.contains(new SlippyTile(2638, 6355, 14)));
        Assert.assertTrue(children.contains(new SlippyTile(2639, 6354, 14)));
        Assert.assertTrue(children.contains(new SlippyTile(2639, 6355, 14)));
    }

    @Test
    public void testSplitZoomOut()
    {
        final SlippyTile tile = new SlippyTile(659, 1588, 12);
        final Set<SlippyTile> children = Iterables.asSet(tile.split(10));
        logger.info("Tile: {}, Children: {}", tile, children);
        Assert.assertEquals(1, children.size());
        Assert.assertTrue(children.contains(new SlippyTile(164, 397, 10)));
    }

    @Test
    public void testTile()
    {
        // http://c.tile.openstreetmap.org/17/21104/50868.png
        final SlippyTile tile = new SlippyTile(Location.TEST_6, 17);
        final SlippyTile same = new SlippyTile(21104, 50868, 17);
        Assert.assertEquals(same, tile);
    }

    @Override
    protected int onRun(final CommandMap command)
    {
        final Rectangle bounds = (Rectangle) command.get(BOUNDS.getName());
        final int zoom = (int) command.get(ZOOM.getName());
        for (final SlippyTile tile : SlippyTile.allTiles(zoom, bounds))
        {
            System.out.println(tile);
        }
        return 0;
    }

    @Override
    protected SwitchList switches()
    {
        return new SwitchList().with(BOUNDS, ZOOM);
    }
}
