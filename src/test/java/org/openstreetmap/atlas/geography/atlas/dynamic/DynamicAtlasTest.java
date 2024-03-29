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
import org.openstreetmap.atlas.geography.atlas.BareAtlas;
import org.openstreetmap.atlas.geography.atlas.delta.AtlasDelta;
import org.openstreetmap.atlas.geography.atlas.dynamic.policy.DynamicAtlasPolicy;
import org.openstreetmap.atlas.geography.atlas.dynamic.rules.DynamicAtlasTestRule;
import org.openstreetmap.atlas.geography.atlas.items.Area;
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.Line;
import org.openstreetmap.atlas.geography.atlas.items.Node;
import org.openstreetmap.atlas.geography.atlas.items.Point;
import org.openstreetmap.atlas.geography.atlas.items.Relation;
import org.openstreetmap.atlas.geography.atlas.items.RelationMember;
import org.openstreetmap.atlas.geography.atlas.multi.MultiAtlas;
import org.openstreetmap.atlas.geography.atlas.multi.MultiRelation;
import org.openstreetmap.atlas.geography.atlas.packed.PackedRelation;
import org.openstreetmap.atlas.geography.sharding.Shard;
import org.openstreetmap.atlas.geography.sharding.SlippyTile;
import org.openstreetmap.atlas.geography.sharding.SlippyTileSharding;
import org.openstreetmap.atlas.utilities.collections.Iterables;
import org.openstreetmap.atlas.utilities.scalars.Distance;

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

    private final Supplier<DynamicAtlasPolicy> policySupplierWithMissingAtlas = () -> new DynamicAtlasPolicy(
            shard ->
            {
                if (shard.equals(new SlippyTile(1349, 1869, 12)))
                {
                    return Optional.empty();
                }
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
    public void testGetLoadedAtlases()
    {
        prepare(this.policySupplier.get().withDeferredLoading(true));
        this.dynamicAtlas.preemptiveLoad();

        final Set<Atlas> atlases = this.dynamicAtlas.getAtlasesLoaded();
        Assert.assertEquals(4, atlases.size());
    }

    @Test
    public void testGetPolicy()
    {
        Assert.assertEquals(this.policySupplier.get().getInitialShards(),
                this.dynamicAtlas.getPolicy().getInitialShards());
    }

    @Test
    public void testGetShardToAtlasMap()
    {
        prepare(this.policySupplierWithMissingAtlas.get().withDeferredLoading(true));
        this.dynamicAtlas.preemptiveLoad();

        final Map<Shard, Atlas> atlasMap = this.dynamicAtlas.getShardToAtlasMap();
        Assert.assertEquals(3, atlasMap.size());
        Assert.assertTrue(atlasMap.containsKey(new SlippyTile(1350, 1870, 12)));
        Assert.assertTrue(atlasMap.containsKey(new SlippyTile(1350, 1869, 12)));
        Assert.assertTrue(atlasMap.containsKey(new SlippyTile(1349, 1870, 12)));
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
        Assert.assertEquals(1, this.dynamicAtlas.getNumberOfShardsLoaded());

        // Prompts load of 12-1350-1869
        Assert.assertNotNull(this.dynamicAtlas.edge(2000000));
        Assert.assertEquals(6, this.dynamicAtlas.numberOfEdges());
        Assert.assertNotNull(this.dynamicAtlas.edge(3000000));
        Assert.assertEquals(6, this.dynamicAtlas.numberOfEdges());
        Assert.assertEquals(2, this.dynamicAtlas.getNumberOfShardsLoaded());

        // Prompts load of 12-1349-1869
        Assert.assertNotNull(this.dynamicAtlas.edge(4000000));
        Assert.assertEquals(8, this.dynamicAtlas.numberOfEdges());
        Assert.assertNotNull(this.dynamicAtlas.edge(5000000));
        Assert.assertEquals(8, this.dynamicAtlas.numberOfEdges());
        Assert.assertEquals(3, this.dynamicAtlas.getNumberOfShardsLoaded());

        // Prompts load of 12-1349-1870
        Assert.assertNotNull(this.dynamicAtlas.edge(6000000));
        Assert.assertEquals(9, this.dynamicAtlas.numberOfEdges());
        Assert.assertNotNull(this.dynamicAtlas.edge(7000000));
        Assert.assertEquals(9, this.dynamicAtlas.numberOfEdges());
        Assert.assertNotNull(this.dynamicAtlas.edge(8000000));
        Assert.assertEquals(9, this.dynamicAtlas.numberOfEdges());
        Assert.assertEquals(4, this.dynamicAtlas.getNumberOfShardsLoaded());
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
        Assert.assertEquals(1, this.dynamicAtlas.relation(3).members().size());

        // Prompts load of 12-1349-1870
        this.dynamicAtlas.edge(8000000);
        Assert.assertEquals(6, this.dynamicAtlas.numberOfEdges());

        // Prompts load of 12-1349-1869
        final Relation relation3 = this.dynamicAtlas.relation(3);
        Assert.assertEquals(3, relation3.members().size());
        Assert.assertEquals(8, this.dynamicAtlas.numberOfEdges());
    }

    /**
     * Check to make sure that {@link Atlas#relationsLowerOrderFirst()} works when the {@link Atlas}
     * is a {@link DynamicAtlas}. In older versions of the code, any relations that had members
     * which were also relations would be dropped from the set returned by
     * {@link BareAtlas#relationsLowerOrderFirst()}. This was due to a flaw in the membership
     * assumptions made by {@link BareAtlas#relationsLowerOrderFirst()}, which assumed that
     * relations in the main {@link DynamicAtlas} and their equivalent representation as a member
     * {@link AtlasEntity} of another relation in the same {@link DynamicAtlas} were of consistent
     * types. However, this was not the case. (The former representation would be of type
     * {@link DynamicRelation} and the latter would be of type {@link MultiRelation} or
     * {@link PackedRelation}). This has now been fixed, so this test should always pass.
     */
    @Test
    public void testRelationsLowerOrderFirstConsistency()
    {
        final DynamicAtlas localDynamicAtlas;
        final Map<Shard, Atlas> localStore = new HashMap<>();
        localStore.put(new SlippyTile(0, 0, 0), this.rule.getAtlasForRelationsTest());
        final Supplier<DynamicAtlasPolicy> localPolicySupplier = () -> new DynamicAtlasPolicy(
                shard ->
                {
                    if (localStore.containsKey(shard))
                    {
                        return Optional.of(localStore.get(shard));
                    }
                    else
                    {
                        return Optional.empty();
                    }
                }, new SlippyTileSharding(0), new SlippyTile(0, 0, 0), Rectangle.MAXIMUM);
        localDynamicAtlas = new DynamicAtlas(localPolicySupplier.get());

        final Set<Relation> returnedByRelations = new HashSet<>();
        final Set<Relation> returnedByRelationsLowerOrderFirst = new HashSet<>();

        for (final Relation relation : localDynamicAtlas.relations())
        {
            returnedByRelations.add(relation);
        }

        for (final Relation relation : localDynamicAtlas.relationsLowerOrderFirst())
        {
            returnedByRelationsLowerOrderFirst.add(relation);
        }

        // Assert that their sizes equal, if not then we can fail fast
        Assert.assertEquals(returnedByRelations.size(), returnedByRelationsLowerOrderFirst.size());

        // Now that we know they have equal sizes, check member equality
        Assert.assertEquals(returnedByRelations, returnedByRelationsLowerOrderFirst);
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

    @Test
    public void testSnapsLineItem()
    {
        final Atlas atlas = this.rule.getAtlas();
        Assert.assertEquals(5,
                atlas.snapsLineItem(atlas.node(1L).getLocation(), Distance.meters(100)).size());
        Assert.assertEquals(3,
                atlas.snaps(atlas.node(1L).getLocation(), Distance.meters(100)).size());
    }

    @Test
    public void testSpatialFilters()
    {
        final Area testArea = this.dynamicAtlas.area(1);
        Assert.assertTrue(this.dynamicAtlas
                .areasCovering(testArea.asPolygon().center(), area -> area.equals(testArea))
                .iterator().hasNext());
        Assert.assertTrue(this.dynamicAtlas
                .areasIntersecting(testArea.bounds(), area -> area.equals(testArea)).iterator()
                .hasNext());

        final Edge testEdge = this.dynamicAtlas.edge(1000000);
        Assert.assertTrue(this.dynamicAtlas
                .edgesContaining(testEdge.end().getLocation(), edge -> edge.equals(testEdge))
                .iterator().hasNext());
        Assert.assertTrue(this.dynamicAtlas
                .edgesIntersecting(testEdge.bounds(), edge -> edge.equals(testEdge)).iterator()
                .hasNext());

        final Line testLine = this.dynamicAtlas.line(1);
        Assert.assertTrue(this.dynamicAtlas
                .linesContaining(testLine.asPolyLine().first(), edge -> edge.equals(testLine))
                .iterator().hasNext());
        Assert.assertTrue(this.dynamicAtlas
                .linesIntersecting(testLine.bounds(), edge -> edge.equals(testLine)).iterator()
                .hasNext());

        final Node testNode = this.dynamicAtlas.node(1);
        Assert.assertTrue(
                this.dynamicAtlas.nodesWithin(testNode.bounds(), edge -> edge.equals(testNode))
                        .iterator().hasNext());

        final Point testPoint = this.dynamicAtlas.point(1);
        Assert.assertTrue(
                this.dynamicAtlas.pointsWithin(testPoint.bounds(), edge -> edge.equals(testPoint))
                        .iterator().hasNext());

        final Relation testRelation = this.dynamicAtlas.relation(1);
        Assert.assertTrue(this.dynamicAtlas.relationsWithEntitiesIntersecting(testRelation.bounds(),
                edge -> edge.equals(testRelation)).iterator().hasNext());
    }

    @Test
    public void testTypeOfReturnedRelationMembers()
    {
        final DynamicAtlas localDynamicAtlas;
        final Map<Shard, Atlas> localStore = new HashMap<>();
        localStore.put(new SlippyTile(0, 0, 0), this.rule.getAtlasForRelationsTest());
        final Supplier<DynamicAtlasPolicy> localPolicySupplier = () -> new DynamicAtlasPolicy(
                shard ->
                {
                    if (localStore.containsKey(shard))
                    {
                        return Optional.of(localStore.get(shard));
                    }
                    else
                    {
                        return Optional.empty();
                    }
                }, new SlippyTileSharding(0), new SlippyTile(0, 0, 0), Rectangle.MAXIMUM);
        localDynamicAtlas = new DynamicAtlas(localPolicySupplier.get());

        for (final Relation relation : localDynamicAtlas.relations())
        {
            for (final RelationMember member : relation.allKnownOsmMembers())
            {
                Assert.assertTrue(atlasEntityIsADynamicEntity(member.getEntity()));
            }
            for (final RelationMember member : relation.members())
            {
                Assert.assertTrue(atlasEntityIsADynamicEntity(member.getEntity()));
            }
        }
    }

    private boolean atlasEntityIsADynamicEntity(final AtlasEntity entity)
    {
        final boolean isDynamicPoint = entity instanceof DynamicPoint;
        final boolean isDynamicLine = entity instanceof DynamicLine;
        final boolean isDynamicArea = entity instanceof DynamicArea;
        final boolean isDynamicNode = entity instanceof DynamicNode;
        final boolean isDynamicEdge = entity instanceof DynamicEdge;
        final boolean isDynamicRelation = entity instanceof DynamicRelation;

        return isDynamicPoint || isDynamicLine || isDynamicArea || isDynamicNode || isDynamicEdge
                || isDynamicRelation;
    }
}
