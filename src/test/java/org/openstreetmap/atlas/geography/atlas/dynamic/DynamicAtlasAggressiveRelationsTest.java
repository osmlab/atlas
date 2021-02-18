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
import org.openstreetmap.atlas.geography.atlas.dynamic.rules.DynamicAtlasAggressiveRelationsTestRule;
import org.openstreetmap.atlas.geography.atlas.dynamic.rules.DynamicAtlasTestRule;
import org.openstreetmap.atlas.geography.atlas.items.Relation;
import org.openstreetmap.atlas.geography.sharding.Shard;
import org.openstreetmap.atlas.geography.sharding.SlippyTile;
import org.openstreetmap.atlas.geography.sharding.SlippyTileSharding;

/**
 * @author matthieun
 */
public class DynamicAtlasAggressiveRelationsTest
{
    @Rule
    public DynamicAtlasTestRule rule = new DynamicAtlasTestRule();

    @Rule
    public DynamicAtlasAggressiveRelationsTestRule rule2 = new DynamicAtlasAggressiveRelationsTestRule();

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

    private final Supplier<DynamicAtlasPolicy> policySupplier2 = () -> new DynamicAtlasPolicy(
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
            }, new SlippyTileSharding(11), new SlippyTile(998, 708, 11), Rectangle.MAXIMUM);

    @Before
    public void prepare()
    {
        this.store = new HashMap<>();
        this.store.put(new SlippyTile(1350, 1870, 12), this.rule.getAtlasz12x1350y1870());
        this.store.put(new SlippyTile(1350, 1869, 12), this.rule.getAtlasz12x1350y1869());
        this.store.put(new SlippyTile(1349, 1869, 12), this.rule.getAtlasz12x1349y1869());
        this.store.put(new SlippyTile(1349, 1870, 12), this.rule.getAtlasz12x1349y1870());
        this.store.put(new SlippyTile(998, 708, 11), this.rule2.getAtlasZ11X998Y708());
        this.store.put(new SlippyTile(999, 708, 11), this.rule2.getAtlasZ11X999Y708());
    }

    @Test
    public void testRelationsAggressively()
    {
        final DynamicAtlas dynamicAtlas = new DynamicAtlas(
                this.policySupplier.get().withAggressivelyExploreRelations(true)
                        .withExtendIndefinitely(false).withDeferredLoading(true));

        // Prompts load of 12-1350-1869, 12-1349-1870 and 12-1349-1869
        dynamicAtlas.preemptiveLoad();
        Assert.assertNotNull(dynamicAtlas.relation(1));
        Assert.assertNotNull(dynamicAtlas.relation(2));
        Assert.assertNotNull(dynamicAtlas.relation(3));
        Assert.assertEquals(9, dynamicAtlas.numberOfEdges());
    }

    @Test
    public void testRelationsAggressivelyWithExpantionIndefinite()
    {
        final DynamicAtlas dynamicAtlas = new DynamicAtlas(this.policySupplier2.get()
                .withAggressivelyExploreRelations(true).withExtendIndefinitely(true)
                .withDeferredLoading(true)
                .withAtlasEntitiesToConsiderForExpansion(entity -> entity instanceof Relation));

        // Prompts load of 11-999-708
        dynamicAtlas.preemptiveLoad();
        final Relation relation = dynamicAtlas.relation(99756000000L);
        Assert.assertNotNull(relation);
        Assert.assertEquals(2, relation.members().size());
    }
}
