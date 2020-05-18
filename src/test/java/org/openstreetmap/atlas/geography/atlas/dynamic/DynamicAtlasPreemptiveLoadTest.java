package org.openstreetmap.atlas.geography.atlas.dynamic;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.Rectangle;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.change.AtlasEntityKey;
import org.openstreetmap.atlas.geography.atlas.dynamic.policy.DynamicAtlasPolicy;
import org.openstreetmap.atlas.geography.atlas.dynamic.rules.DynamicAtlasPreemptiveLoadTestRule;
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.geography.sharding.Shard;
import org.openstreetmap.atlas.geography.sharding.SlippyTile;
import org.openstreetmap.atlas.geography.sharding.SlippyTileSharding;

/**
 * @author matthieun
 */
public class DynamicAtlasPreemptiveLoadTest
{
    private static final Shard INITIAL_SHARD = new SlippyTile(240, 246, 9);

    @Rule
    public DynamicAtlasPreemptiveLoadTestRule rule = new DynamicAtlasPreemptiveLoadTestRule();

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
            }, new SlippyTileSharding(9), INITIAL_SHARD, Rectangle.MAXIMUM)
                    .withDeferredLoading(true).withExtendIndefinitely(false);
    private final Supplier<DynamicAtlasPolicy> allInitialShardsPolicySupplier = () -> new DynamicAtlasPolicy(
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
            }, new SlippyTileSharding(9),
            Rectangle.forLocated(INITIAL_SHARD.bounds().center(),
                    new SlippyTile(241, 246, 9).bounds().center()),
            Rectangle.MAXIMUM).withDeferredLoading(true).withExtendIndefinitely(false);
    private final Supplier<DynamicAtlasPolicy> singleHitPolicySupplier = () -> new DynamicAtlasPolicy(
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
            }, new SlippyTileSharding(9), INITIAL_SHARD, Rectangle.MAXIMUM)
                    .withDeferredLoading(true).withExtendIndefinitely(true)
                    .withAtlasEntitiesToConsiderForExpansion(new Predicate<>()
                    {
                        private final Map<AtlasEntityKey, Integer> identifiersChecked = new HashMap<>();

                        @Override
                        public boolean test(final AtlasEntity atlasEntity)
                        {
                            final AtlasEntityKey key = AtlasEntityKey.from(atlasEntity.getType(),
                                    atlasEntity.getIdentifier());
                            if (this.identifiersChecked.containsKey(key)
                                    && this.identifiersChecked.get(key) > 4)
                            {
                                throw new CoreException("Checked {} {} times!", key,
                                        this.identifiersChecked.get(key));
                            }
                            else
                            {
                                int newCount = 1;
                                if (this.identifiersChecked.containsKey(key))
                                {
                                    newCount += this.identifiersChecked.get(key);
                                }
                                this.identifiersChecked.put(key, newCount);
                            }
                            return true;
                        }
                    });

    @Test
    public void loadPreemptivelyCapExpansionCheckTest()
    {
        final DynamicAtlas dynamicAtlas = new DynamicAtlas(this.singleHitPolicySupplier.get());
        dynamicAtlas.preemptiveLoad();
        Assert.assertEquals(3, dynamicAtlas.numberOfEdges());
        for (int i = 0; i < 10; i++)
        {
            // Make sure that every time the preemptive load is done, the DynamicAtlas does not do
            // further geometry checks for each feature request
            dynamicAtlas.nodes().forEach(node -> Assert.assertTrue(node.within(Rectangle.MAXIMUM)));
        }
        Assert.assertEquals(2, dynamicAtlas.getTimesMultiAtlasWasBuiltUnderneath());
    }

    @Test
    public void loadPreemptivelyTest()
    {
        final DynamicAtlas dynamicAtlas = new DynamicAtlas(this.policySupplier.get());
        dynamicAtlas.preemptiveLoad();
        Assert.assertEquals(3, dynamicAtlas.numberOfEdges());
        Assert.assertEquals(2, dynamicAtlas.getTimesMultiAtlasWasBuiltUnderneath());
    }

    @Test
    public void loadPreemptivelyWithAllShardsAsInitialTest()
    {
        final DynamicAtlas dynamicAtlas = new DynamicAtlas(
                this.allInitialShardsPolicySupplier.get());
        dynamicAtlas.preemptiveLoad();
        Assert.assertEquals(3, dynamicAtlas.numberOfEdges());
        Assert.assertEquals(1, dynamicAtlas.getTimesMultiAtlasWasBuiltUnderneath());
    }

    @Before
    public void prepare()
    {
        this.store = new HashMap<>();
        this.store.put(new SlippyTile(240, 246, 9), this.rule.getAtlasZ9x240y246());
        this.store.put(new SlippyTile(240, 245, 9), this.rule.getAtlasZ9x240y245());
        this.store.put(new SlippyTile(241, 245, 9), this.rule.getAtlasZ9x241y245());
        this.store.put(new SlippyTile(241, 246, 9), this.rule.getAtlasZ9x241y246());
    }
}
