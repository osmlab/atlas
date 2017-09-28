package org.openstreetmap.atlas.geography.atlas.dynamic;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.geography.Rectangle;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.dynamic.policy.DynamicAtlasPolicy;
import org.openstreetmap.atlas.geography.sharding.Shard;
import org.openstreetmap.atlas.geography.sharding.SlippyTile;
import org.openstreetmap.atlas.geography.sharding.SlippyTileSharding;

/**
 * @author matthieun
 */
public class DynamicAtlasMultipleInitialShardTest
{
    @Rule
    public DynamicAtlasTestRule rule = new DynamicAtlasTestRule();

    private DynamicAtlas dynamicAtlas;
    private Map<Shard, Atlas> store;
    private final Supplier<DynamicAtlasPolicy> policySupplier = () ->
    {
        final Set<Shard> initialShards = new HashSet<>();
        initialShards.add(new SlippyTile(1350, 1870, 12));
        initialShards.add(new SlippyTile(1349, 1870, 12));
        return new DynamicAtlasPolicy(shard ->
        {
            if (this.store.containsKey(shard))
            {
                return Optional.of(this.store.get(shard));
            }
            else
            {
                return Optional.empty();
            }
        }, new SlippyTileSharding(12), initialShards, Rectangle.MAXIMUM);
    };

    @Before
    public void prepare()
    {
        prepare(this.policySupplier.get());
    }

    public void prepare(final DynamicAtlasPolicy policy)
    {
        this.store = new HashMap<>();
        this.store.put(new SlippyTile(1350, 1870, 12), this.rule.getAtlasz12x1350y1870());
        this.store.put(new SlippyTile(1350, 1869, 12), this.rule.getAtlasz12x1350y1869());
        this.store.put(new SlippyTile(1349, 1869, 12), this.rule.getAtlasz12x1349y1869());
        this.store.put(new SlippyTile(1349, 1870, 12), this.rule.getAtlasz12x1349y1870());
        this.dynamicAtlas = new DynamicAtlas(policy);
    }

    @Test
    public void testLoadAreaByIdentifier()
    {
        // Already loaded: 12-1350-1870 and 12-1349-1870
        Assert.assertEquals(6, this.dynamicAtlas.numberOfEdges());
        // Test an area that does not exist
        Assert.assertNull(this.dynamicAtlas.area(5));
        Assert.assertNotNull(this.dynamicAtlas.area(1));
        Assert.assertEquals(6, this.dynamicAtlas.numberOfEdges());
        // Prompts load of 12-1350-1869
        Assert.assertNotNull(this.dynamicAtlas.area(2));
        Assert.assertEquals(8, this.dynamicAtlas.numberOfEdges());
    }

    @Test
    public void testLoadEdgeByIdentifier()
    {
        // Already loaded: 12-1350-1870 and 12-1349-1870
        Assert.assertEquals(6, this.dynamicAtlas.numberOfEdges());
        Assert.assertNull(this.dynamicAtlas.edge(5000000));
        Assert.assertEquals(6, this.dynamicAtlas.numberOfEdges());
        Assert.assertNotNull(this.dynamicAtlas.edge(1000000));
        Assert.assertTrue(this.dynamicAtlas.edge(1000000).hasReverseEdge());
        Assert.assertEquals(6, this.dynamicAtlas.numberOfEdges());

        // Prompts load of 12-1350-1869
        Assert.assertNotNull(this.dynamicAtlas.edge(2000000));
        Assert.assertEquals(8, this.dynamicAtlas.numberOfEdges());
        Assert.assertNotNull(this.dynamicAtlas.edge(3000000));
        Assert.assertEquals(8, this.dynamicAtlas.numberOfEdges());

        // Prompts load of 12-1349-1869
        Assert.assertNotNull(this.dynamicAtlas.edge(4000000));
        Assert.assertEquals(9, this.dynamicAtlas.numberOfEdges());
        Assert.assertNotNull(this.dynamicAtlas.edge(5000000));
        Assert.assertEquals(9, this.dynamicAtlas.numberOfEdges());
    }
}
