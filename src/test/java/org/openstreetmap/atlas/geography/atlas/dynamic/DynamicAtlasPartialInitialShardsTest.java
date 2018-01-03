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
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;

/**
 * @author matthieun
 */
public class DynamicAtlasPartialInitialShardsTest
{
    /**
     * @author matthieun
     */
    public static class DynamicAtlasPartialInitialShardsTestRule extends CoreTestRule
    {
        @TestAtlas(loadFromJosmOsmResource = "DynamicAtlasPartialInitialShardsTest.osm")
        private Atlas atlas;

        public Atlas getAtlas()
        {
            return this.atlas;
        }
    }

    @Rule
    public DynamicAtlasPartialInitialShardsTestRule rule = new DynamicAtlasPartialInitialShardsTestRule();
    private DynamicAtlas dynamicAtlas;
    private Map<Shard, Atlas> store;

    private final Supplier<DynamicAtlasPolicy> policySupplier = () ->
    {
        final Set<Shard> initialTiles = new HashSet<>();
        initialTiles.add(new SlippyTile(240, 247, 9));
        initialTiles.add(new SlippyTile(241, 247, 9));
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
        }, new SlippyTileSharding(9), initialTiles, Rectangle.MAXIMUM);
    };

    @Before
    public void prepare()
    {
        prepare(this.policySupplier.get());
    }

    public void prepare(final DynamicAtlasPolicy policy)
    {
        this.store = new HashMap<>();
        this.store.put(new SlippyTile(240, 247, 9), this.rule.getAtlas());
        this.dynamicAtlas = new DynamicAtlas(policy);
    }

    @Test
    public void testPartialInitialShards()
    {
        // Make sure that the DynamicAtlas is still loaded despite one of the initial shards being
        // missing.
        Assert.assertEquals(1, this.dynamicAtlas.numberOfAreas());
    }
}
