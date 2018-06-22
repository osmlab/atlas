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
import org.openstreetmap.atlas.geography.atlas.delta.AtlasDelta;
import org.openstreetmap.atlas.geography.atlas.dynamic.policy.DynamicAtlasPolicy;
import org.openstreetmap.atlas.geography.atlas.dynamic.rules.DynamicAtlasTestRule;
import org.openstreetmap.atlas.geography.atlas.items.Relation;
import org.openstreetmap.atlas.geography.atlas.multi.MultiAtlas;
import org.openstreetmap.atlas.geography.sharding.Shard;
import org.openstreetmap.atlas.geography.sharding.SlippyTile;
import org.openstreetmap.atlas.geography.sharding.SlippyTileSharding;
import org.openstreetmap.atlas.utilities.collections.Iterables;

/**
 * @author matthieun
 */
public class DynamicAtlasTest
{
    @Rule
    public DynamicAtlasTestRule rule = new DynamicAtlasTestRule();

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
            }, new SlippyTileSharding(12), new SlippyTile(1350, 1870, 12), Rectangle.MAXIMUM);

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
        // Already loaded: 12-1350-1870
        Assert.assertEquals(4, this.dynamicAtlas.numberOfEdges());
        // Test an area that does not exist
        Assert.assertNull(this.dynamicAtlas.area(5));
        Assert.assertNotNull(this.dynamicAtlas.area(1));
        Assert.assertEquals(4, this.dynamicAtlas.numberOfEdges());
        // Prompts load of 12-1350-1869
        Assert.assertNotNull(this.dynamicAtlas.area(2));
        Assert.assertEquals(6, this.dynamicAtlas.numberOfEdges());
    }

    @Test
    public void testLoadEdgeByIdentifier()
    {
        // Already loaded: 12-1350-1870
        Assert.assertEquals(4, this.dynamicAtlas.numberOfEdges());
        Assert.assertNull(this.dynamicAtlas.edge(6000000));
        Assert.assertEquals(4, this.dynamicAtlas.numberOfEdges());
        Assert.assertNotNull(this.dynamicAtlas.edge(1000000));
        Assert.assertTrue(this.dynamicAtlas.edge(1000000).hasReverseEdge());
        Assert.assertEquals(4, this.dynamicAtlas.numberOfEdges());

        // Prompts load of 12-1350-1869
        Assert.assertNotNull(this.dynamicAtlas.edge(2000000));
        Assert.assertEquals(6, this.dynamicAtlas.numberOfEdges());
        Assert.assertNotNull(this.dynamicAtlas.edge(3000000));
        Assert.assertEquals(6, this.dynamicAtlas.numberOfEdges());

        // Prompts load of 12-1349-1869
        Assert.assertNotNull(this.dynamicAtlas.edge(4000000));
        Assert.assertEquals(8, this.dynamicAtlas.numberOfEdges());
        Assert.assertNotNull(this.dynamicAtlas.edge(5000000));
        Assert.assertEquals(8, this.dynamicAtlas.numberOfEdges());

        // Prompts load of 12-1349-1870
        // Fixed by {@link MultiAtlasBorderFixer} due to inconsistent relations
        Assert.assertNull(this.dynamicAtlas.edge(6000000));
        Assert.assertNotNull(this.dynamicAtlas.edge(6000001));
        Assert.assertEquals(9, this.dynamicAtlas.numberOfEdges());
        Assert.assertNotNull(this.dynamicAtlas.edge(7000000));
        Assert.assertEquals(9, this.dynamicAtlas.numberOfEdges());
        Assert.assertNotNull(this.dynamicAtlas.edge(8000000));
        Assert.assertEquals(9, this.dynamicAtlas.numberOfEdges());
    }

    @Test
    public void testLoadIndefinitely()
    {
        Assert.assertEquals(9, Iterables.size(this.dynamicAtlas.edges()));
    }

    @Test
    public void testLoadLineByIdentifier()
    {
        // Already loaded: 12-1350-1870
        Assert.assertEquals(4, this.dynamicAtlas.numberOfEdges());
        // Test an area that does not exist
        Assert.assertNull(this.dynamicAtlas.line(5));
        Assert.assertNotNull(this.dynamicAtlas.line(1));
        Assert.assertEquals(4, this.dynamicAtlas.numberOfEdges());
        // Prompts load of 12-1350-1869
        Assert.assertNotNull(this.dynamicAtlas.line(2));
        Assert.assertEquals(6, this.dynamicAtlas.numberOfEdges());
    }

    @Test
    public void testLoadNodeByIdentifier()
    {
        // Already loaded: 12-1350-1870
        Assert.assertEquals(4, this.dynamicAtlas.numberOfEdges());
        Assert.assertNotNull(this.dynamicAtlas.node(1));
        Assert.assertEquals(4, this.dynamicAtlas.numberOfEdges());
        Assert.assertNotNull(this.dynamicAtlas.node(2));
        Assert.assertEquals(4, this.dynamicAtlas.numberOfEdges());
        Assert.assertNull(this.dynamicAtlas.node(4));

        // Prompts load of 12-1350-1869
        Assert.assertEquals(1, this.dynamicAtlas.node(3).outEdges().size());
        Assert.assertNotNull(this.dynamicAtlas.node(3));
        Assert.assertEquals(6, this.dynamicAtlas.numberOfEdges());
        Assert.assertNotNull(this.dynamicAtlas.node(4));
        Assert.assertEquals(6, this.dynamicAtlas.numberOfEdges());
        Assert.assertNull(this.dynamicAtlas.node(6));

        // Prompts load of 12-1349-1869
        Assert.assertEquals(1, this.dynamicAtlas.node(5).outEdges().size());
        Assert.assertNotNull(this.dynamicAtlas.node(5));
        Assert.assertEquals(8, this.dynamicAtlas.numberOfEdges());
        Assert.assertNotNull(this.dynamicAtlas.node(6));
        Assert.assertEquals(8, this.dynamicAtlas.numberOfEdges());
        Assert.assertNotNull(this.dynamicAtlas.node(8));

        // Prompts load of 12-1349-1870
        Assert.assertEquals(1, this.dynamicAtlas.node(7).outEdges().size());
        Assert.assertNotNull(this.dynamicAtlas.node(8));
        Assert.assertEquals(9, this.dynamicAtlas.numberOfEdges());
        Assert.assertNotNull(this.dynamicAtlas.node(8));
        Assert.assertEquals(9, this.dynamicAtlas.numberOfEdges());
    }

    @Test
    public void testLoadNotIndefinitely()
    {
        prepare(this.policySupplier.get().withExtendIndefinitely(false));
        Assert.assertEquals(8, Iterables.size(this.dynamicAtlas.edges()));
    }

    @Test
    public void testLoadPointByIdentifier()
    {
        // Already loaded: 12-1350-1870
        Assert.assertEquals(4, this.dynamicAtlas.numberOfEdges());
        Assert.assertNotNull(this.dynamicAtlas.point(1));
        Assert.assertEquals(4, this.dynamicAtlas.numberOfEdges());
        Assert.assertNotNull(this.dynamicAtlas.point(2));
        Assert.assertEquals(4, this.dynamicAtlas.numberOfEdges());
        Assert.assertNull(this.dynamicAtlas.point(3));

        // Prompts load of 12-1350-1869
        this.dynamicAtlas.edge(2000000);
        Assert.assertNotNull(this.dynamicAtlas.point(3));
        Assert.assertEquals(6, this.dynamicAtlas.numberOfEdges());
        Assert.assertNotNull(this.dynamicAtlas.point(4));
        Assert.assertEquals(6, this.dynamicAtlas.numberOfEdges());
        Assert.assertNull(this.dynamicAtlas.point(5));

        // Prompts load of 12-1349-1869
        this.dynamicAtlas.edge(4000000);
        Assert.assertNotNull(this.dynamicAtlas.point(5));
        Assert.assertEquals(8, this.dynamicAtlas.numberOfEdges());
        Assert.assertNotNull(this.dynamicAtlas.point(6));
        Assert.assertEquals(8, this.dynamicAtlas.numberOfEdges());
        Assert.assertNull(this.dynamicAtlas.point(7));

        // Prompts load of 12-1349-1870
        this.dynamicAtlas.edge(6000000);
        Assert.assertNotNull(this.dynamicAtlas.point(7));
        Assert.assertEquals(9, this.dynamicAtlas.numberOfEdges());
        Assert.assertNotNull(this.dynamicAtlas.point(8));
        Assert.assertEquals(9, this.dynamicAtlas.numberOfEdges());
    }

    @Test
    public void testLoadRelationByIdentifier()
    {
        // Already loaded: 12-1350-1870
        final Relation relation1 = this.dynamicAtlas.relation(1);
        Assert.assertEquals(1, relation1.members().size());
        final Relation relation2 = this.dynamicAtlas.relation(2);
        Assert.assertEquals(1, relation2.members().size());

        // Prompts load of 12-1350-1869
        this.dynamicAtlas.edge(2000000);
        Assert.assertEquals(2, relation1.members().size());
        Assert.assertEquals(1, relation2.members().size());

        // Prompts load of 12-1349-1870
        this.dynamicAtlas.edge(8000000);
        Assert.assertEquals(2, relation1.members().size());
        Assert.assertEquals(2, relation2.members().size());
    }

    @Test
    public void testLoadRelationWithOverlappingMembersByIdentifier()
    {
        // Already loaded: 12-1350-1870
        Assert.assertEquals(4, this.dynamicAtlas.numberOfEdges());
        Assert.assertNull(this.dynamicAtlas.relation(3));

        // Prompts load of 12-1349-1870
        this.dynamicAtlas.edge(8000000);
        Assert.assertEquals(6, this.dynamicAtlas.numberOfEdges());

        // Prompts load of 12-1349-1869
        final Relation relation3 = this.dynamicAtlas.relation(3);
        Assert.assertEquals(2, relation3.members().size());
        Assert.assertEquals(8, this.dynamicAtlas.numberOfEdges());

    }

    @Test
    public void testRuleIntegrity()
    {
        final Atlas atlas = this.rule.getAtlas();
        final Atlas atlasz12x1350y1870 = this.store.get(new SlippyTile(1350, 1870, 12));
        final Atlas atlasz12x1350y1869 = this.store.get(new SlippyTile(1350, 1869, 12));
        final Atlas atlasz12x1349y1869 = this.store.get(new SlippyTile(1349, 1869, 12));
        final Atlas atlasz12x1349y1870 = this.store.get(new SlippyTile(1349, 1870, 12));
        final Atlas multiAtlas = new MultiAtlas(atlasz12x1350y1870, atlasz12x1350y1869,
                atlasz12x1349y1869, atlasz12x1349y1870);
        Assert.assertEquals("Found differences: " + new AtlasDelta(atlas, multiAtlas).toString(),
                atlas, multiAtlas);
    }
}
