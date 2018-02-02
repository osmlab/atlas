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
import org.openstreetmap.atlas.geography.MultiPolygon;
import org.openstreetmap.atlas.geography.Polygon;
import org.openstreetmap.atlas.geography.Rectangle;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.dynamic.policy.DynamicAtlasPolicy;
import org.openstreetmap.atlas.geography.atlas.dynamic.rules.DynamicAtlasRestrainedExpansionWithPolygonTestRule;
import org.openstreetmap.atlas.geography.sharding.Shard;
import org.openstreetmap.atlas.geography.sharding.SlippyTile;
import org.openstreetmap.atlas.geography.sharding.SlippyTileSharding;
import org.openstreetmap.atlas.utilities.scalars.Distance;

/**
 * This test ensures that when the initial shards are supplied with a {@link Polygon} or
 * {@link MultiPolygon}, and the expansion policy is withExtendIndefinitely=false, then the
 * expansion does not consider the full initial shards for intersection, but the initial polygon.
 *
 * @author matthieun
 */
public class DynamicAtlasRestrainedExpansionWithPolygonTest
{
    @Rule
    public DynamicAtlasRestrainedExpansionWithPolygonTestRule rule = new DynamicAtlasRestrainedExpansionWithPolygonTestRule();

    private DynamicAtlas dynamicAtlas;
    private Map<Shard, Atlas> store;

    private final Supplier<DynamicAtlasPolicy> policySupplier = () ->
    {
        final Set<Shard> initialTiles = new HashSet<>();
        initialTiles.add(new SlippyTile(6, 34, 6));
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
        }, new SlippyTileSharding(6),
                initialTiles.iterator().next().bounds().expand(Distance.ONE_METER),
                Rectangle.MAXIMUM).withExtendIndefinitely(false).withDeferredLoading(true);
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
        this.store.put(new SlippyTile(4, 34, 6),
                this.rule.getAtlas().subAtlas(new SlippyTile(4, 34, 6).bounds()).get());
        this.dynamicAtlas = new DynamicAtlas(policy);
        this.dynamicAtlas.preemptiveLoad();
    }

    @Test
    public void testRestrainedExpansionWithPolygon()
    {
        Assert.assertEquals(2, this.dynamicAtlas.numberOfEdges());
    }
}
