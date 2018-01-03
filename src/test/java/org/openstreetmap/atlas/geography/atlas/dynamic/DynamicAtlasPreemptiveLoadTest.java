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
public class DynamicAtlasPreemptiveLoadTest
{
    @Rule
    public DynamicAtlasPreemptiveLoadTestRule rule = new DynamicAtlasPreemptiveLoadTestRule();

    private DynamicAtlas dynamicAtlas;

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
            }, new SlippyTileSharding(9), new SlippyTile(240, 246, 9), Rectangle.MAXIMUM)
                    .withDeferredLoading(true).withExtendIndefinitely(false);

    @Test
    public void loadPreemptivelyTest()
    {
        this.dynamicAtlas.preemptiveLoad();
        Assert.assertEquals(3, this.dynamicAtlas.numberOfEdges());
    }

    @Before
    public void prepare()
    {
        prepare(this.policySupplier.get());
    }

    public void prepare(final DynamicAtlasPolicy policy)
    {
        this.store = new HashMap<>();
        this.store.put(new SlippyTile(240, 246, 9), this.rule.getAtlasZ9x240y246());
        this.store.put(new SlippyTile(240, 245, 9), this.rule.getAtlasZ9x240y245());
        this.store.put(new SlippyTile(241, 245, 9), this.rule.getAtlasZ9x241y245());
        this.store.put(new SlippyTile(241, 246, 9), this.rule.getAtlasZ9x241y246());
        this.dynamicAtlas = new DynamicAtlas(policy);
    }
}
