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
import org.openstreetmap.atlas.geography.atlas.dynamic.rules.DynamicAtlasTestRule;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.Relation;
import org.openstreetmap.atlas.geography.sharding.Shard;
import org.openstreetmap.atlas.geography.sharding.SlippyTile;
import org.openstreetmap.atlas.geography.sharding.SlippyTileSharding;
import org.openstreetmap.atlas.tags.HighwayTag;
import org.openstreetmap.atlas.tags.annotations.validation.Validators;

/**
 * @author matthieun
 */
public class DynamicAtlasFilteredEntitiesTest
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
    public void testLoadEdgesOnlyByTag()
    {
        final DynamicAtlas dynamicAtlas = new DynamicAtlas(this.policySupplier.get()
                .withAtlasEntitiesToConsiderForExpansion(entity -> Validators.isOfType(entity,
                        HighwayTag.class, HighwayTag.SECONDARY)));
        runLoadEdgesOnlyTest(dynamicAtlas);
    }

    @Test
    public void testLoadEdgesOnlyByType()
    {
        final DynamicAtlas dynamicAtlas = new DynamicAtlas(this.policySupplier.get()
                .withAtlasEntitiesToConsiderForExpansion(entity -> entity instanceof Edge));
        runLoadEdgesOnlyTest(dynamicAtlas);
    }

    @Test
    public void testLoadNoEdgesByTag()
    {
        final DynamicAtlas dynamicAtlas = new DynamicAtlas(this.policySupplier.get()
                .withAtlasEntitiesToConsiderForExpansion(
                        entity -> "relation".equals(entity.getTag("type").orElse("")))
                .withAggressivelyExploreRelations(true));
        runLoadNoEdgesTest(dynamicAtlas);
    }

    @Test
    public void testLoadNoEdgesByType()
    {
        final DynamicAtlas dynamicAtlas = new DynamicAtlas(this.policySupplier.get()
                .withAtlasEntitiesToConsiderForExpansion(entity -> entity instanceof Relation)
                .withAggressivelyExploreRelations(true));
        runLoadNoEdgesTest(dynamicAtlas);
    }

    private void runLoadEdgesOnlyTest(final DynamicAtlas dynamicAtlas)
    {
        // Already loaded: 12-1350-1870
        Assert.assertEquals(4, dynamicAtlas.numberOfEdges());
        // Test an area that does not exist
        Assert.assertNull(dynamicAtlas.area(5));
        Assert.assertNotNull(dynamicAtlas.area(1));
        Assert.assertEquals(4, dynamicAtlas.numberOfEdges());
        // Usually prompts load of 12-1350-1869, but not here
        Assert.assertNotNull(dynamicAtlas.area(2));
        // Still 4
        Assert.assertEquals(4, dynamicAtlas.numberOfEdges());

        // Now onto edges, the behavior should be the same as without the predicate
        // Already loaded: 12-1350-1870
        Assert.assertEquals(4, dynamicAtlas.numberOfEdges());
        Assert.assertNull(dynamicAtlas.edge(6000000));
        Assert.assertEquals(4, dynamicAtlas.numberOfEdges());
        Assert.assertNotNull(dynamicAtlas.edge(1000000));
        Assert.assertTrue(dynamicAtlas.edge(1000000).hasReverseEdge());
        Assert.assertEquals(4, dynamicAtlas.numberOfEdges());

        // Prompts load of 12-1350-1869
        Assert.assertNotNull(dynamicAtlas.edge(2000000));
        Assert.assertEquals(6, dynamicAtlas.numberOfEdges());
        Assert.assertNotNull(dynamicAtlas.edge(3000000));
        Assert.assertEquals(6, dynamicAtlas.numberOfEdges());

        // Prompts load of 12-1349-1869
        Assert.assertNotNull(dynamicAtlas.edge(4000000));
        Assert.assertEquals(8, dynamicAtlas.numberOfEdges());
        Assert.assertNotNull(dynamicAtlas.edge(5000000));
        Assert.assertEquals(8, dynamicAtlas.numberOfEdges());

        // Prompts load of 12-1349-1870
        // Fixed by {@link MultiAtlasBorderFixer} due to inconsistent relations
        Assert.assertNull(dynamicAtlas.edge(6000000));
        Assert.assertNotNull(dynamicAtlas.edge(6000001));
        Assert.assertEquals(9, dynamicAtlas.numberOfEdges());
        Assert.assertNotNull(dynamicAtlas.edge(7000000));
        Assert.assertEquals(9, dynamicAtlas.numberOfEdges());
        Assert.assertNotNull(dynamicAtlas.edge(8000000));
        Assert.assertEquals(9, dynamicAtlas.numberOfEdges());
    }

    private void runLoadNoEdgesTest(final DynamicAtlas dynamicAtlas)
    {
        // Already loaded: 12-1350-1870
        Assert.assertEquals(4, dynamicAtlas.numberOfEdges());
        // Test an area that does not exist
        Assert.assertNull(dynamicAtlas.area(5));
        Assert.assertNotNull(dynamicAtlas.area(1));
        Assert.assertEquals(4, dynamicAtlas.numberOfEdges());
        // Usually prompts load of 12-1350-1869, but not here
        Assert.assertNotNull(dynamicAtlas.area(2));
        // Still 4
        Assert.assertEquals(4, dynamicAtlas.numberOfEdges());

        // Already loaded: 12-1350-1870
        Assert.assertEquals(4, dynamicAtlas.numberOfEdges());
        Assert.assertNull(dynamicAtlas.edge(6000000));
        Assert.assertEquals(4, dynamicAtlas.numberOfEdges());
        Assert.assertNotNull(dynamicAtlas.edge(1000000));
        Assert.assertTrue(dynamicAtlas.edge(1000000).hasReverseEdge());
        Assert.assertEquals(4, dynamicAtlas.numberOfEdges());

        // Does NOT Prompt load of 12-1350-1869
        Assert.assertNotNull(dynamicAtlas.edge(2000000));
        Assert.assertEquals(4, dynamicAtlas.numberOfEdges());
        Assert.assertNull(dynamicAtlas.edge(3000000));
        Assert.assertEquals(4, dynamicAtlas.numberOfEdges());

        // Does NOT Prompt load of 12-1349-1869
        Assert.assertNull(dynamicAtlas.edge(4000000));
        Assert.assertEquals(4, dynamicAtlas.numberOfEdges());
        Assert.assertNull(dynamicAtlas.edge(5000000));
        Assert.assertEquals(4, dynamicAtlas.numberOfEdges());

        // Does NOT Prompt load of 12-1349-1870
        Assert.assertNull(dynamicAtlas.edge(6000000));
        Assert.assertEquals(4, dynamicAtlas.numberOfEdges());
        Assert.assertNull(dynamicAtlas.edge(7000000));
        Assert.assertEquals(4, dynamicAtlas.numberOfEdges());
        // Was already there from the initial shard
        Assert.assertNotNull(dynamicAtlas.edge(8000000));
        Assert.assertEquals(4, dynamicAtlas.numberOfEdges());

        // Prompts load of 12-1350-1869
        Assert.assertNotNull(dynamicAtlas.relation(1));
        Assert.assertEquals(6, dynamicAtlas.numberOfEdges());
    }
}
