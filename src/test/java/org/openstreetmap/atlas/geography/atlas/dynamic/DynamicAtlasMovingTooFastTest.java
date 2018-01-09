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
import org.openstreetmap.atlas.geography.atlas.dynamic.rules.DynamicAtlasMovingTooFastTestRule;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.sharding.Shard;
import org.openstreetmap.atlas.geography.sharding.SlippyTile;
import org.openstreetmap.atlas.geography.sharding.SlippyTileSharding;
import org.openstreetmap.atlas.utilities.collections.Iterables;

/**
 * @author matthieun
 */
public class DynamicAtlasMovingTooFastTest
{
    @Rule
    public DynamicAtlasMovingTooFastTestRule rule = new DynamicAtlasMovingTooFastTestRule();

    private DynamicAtlas dynamicAtlas;
    private Map<Shard, Atlas> store;

    private final Supplier<DynamicAtlasPolicy> policySupplier = () ->
    {
        final Set<Shard> initialTiles = new HashSet<>();
        initialTiles.add(new SlippyTile(5, 34, 6));
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
        }, new SlippyTileSharding(6), initialTiles, Rectangle.MAXIMUM).withExtendIndefinitely(false)
                .withDeferredLoading(true);
    };

    @Before
    public void prepare()
    {
        prepare(this.policySupplier.get());
    }

    public void prepare(final DynamicAtlasPolicy policy)
    {
        this.store = new HashMap<>();
        this.store.put(new SlippyTile(5, 34, 6),
                this.rule.getAtlas().subAtlas(new SlippyTile(5, 34, 6).bounds()).get());
        this.store.put(new SlippyTile(6, 34, 6),
                this.rule.getAtlas().subAtlas(new SlippyTile(6, 34, 6).bounds()).get());
        this.store.put(new SlippyTile(6, 35, 6),
                this.rule.getAtlas().subAtlas(new SlippyTile(6, 35, 6).bounds()).get());
        this.store.put(new SlippyTile(5, 35, 6),
                this.rule.getAtlas().subAtlas(new SlippyTile(5, 35, 6).bounds()).get());
        this.dynamicAtlas = new DynamicAtlas(policy);
        this.dynamicAtlas.preemptiveLoad();
    }

    @Test
    public void testNotMovingTooFast()
    {
        Assert.assertEquals(1, this.dynamicAtlas.numberOfEdges());
        final Iterable<Edge> edgesIntersecting = this.dynamicAtlas
                .edgesIntersecting(this.dynamicAtlas.edges().iterator().next().bounds());
        Assert.assertEquals(1, Iterables.size(edgesIntersecting));
        Assert.assertEquals("primary",
                edgesIntersecting.iterator().next().getTags().get("highway"));
    }
}
