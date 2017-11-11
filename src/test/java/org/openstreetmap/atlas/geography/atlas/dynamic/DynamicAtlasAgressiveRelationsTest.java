package org.openstreetmap.atlas.geography.atlas.dynamic;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
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
public class DynamicAtlasAgressiveRelationsTest
{
    @Rule
    public DynamicAtlasTestRule rule = new DynamicAtlasTestRule();

    private Map<Shard, Atlas> store;
    private final Supplier<DynamicAtlasPolicy> policySupplier = () -> new DynamicAtlasPolicy(
            shard ->
            {
                if (this.store.containsKey(shard))
                {
                    return Optional.of(this.store.get(shard));
                }
                else
                {
                    return Optional.empty();
                }
            }, new SlippyTileSharding(12), new SlippyTile(1350, 1870, 12), Rectangle.MAXIMUM);

    @Before
    public void prepare()
    {
        this.store = new HashMap<>();
        this.store.put(new SlippyTile(1350, 1870, 12), this.rule.getAtlasz12x1350y1870());
        this.store.put(new SlippyTile(1350, 1869, 12), this.rule.getAtlasz12x1350y1869());
        this.store.put(new SlippyTile(1349, 1869, 12), this.rule.getAtlasz12x1349y1869());
        this.store.put(new SlippyTile(1349, 1870, 12), this.rule.getAtlasz12x1349y1870());
    }

    @Test
    public void testRelationsAggressively()
    {
        final DynamicAtlas dynamicAtlas = new DynamicAtlas(
                this.policySupplier.get().withAggressivelyExploreRelations(true));

        // Prompts load of 12-1350-1869
        Assert.assertNotNull(dynamicAtlas.relation(1));
        Assert.assertEquals(6, dynamicAtlas.numberOfEdges());

        // Prompts load of 12-1349-1870
        Assert.assertNotNull(dynamicAtlas.relation(2));
        Assert.assertEquals(8, dynamicAtlas.numberOfEdges());

        // Prompts load of 12-1349-1869
        Assert.assertNotNull(dynamicAtlas.relation(3));
        Assert.assertEquals(9, dynamicAtlas.numberOfEdges());
    }
}
