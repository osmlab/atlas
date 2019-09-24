package org.openstreetmap.atlas.geography.atlas.change;

import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.complete.CompleteEdge;
import org.openstreetmap.atlas.geography.atlas.complete.CompleteEntity;
import org.openstreetmap.atlas.geography.atlas.complete.CompleteNode;
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.geography.atlas.items.Node;
import org.openstreetmap.atlas.utilities.collections.Maps;
import org.openstreetmap.atlas.utilities.collections.Sets;

/**
 * @author matthieun
 */
public class AtlasChangeGeneratorTest
{
    public static final String HIGHWAY = "highway";

    @Rule
    public final AtlasChangeGeneratorTestRule rule = new AtlasChangeGeneratorTestRule();

    @Test
    public void testAddLocation()
    {
        final Atlas source = this.rule.getNodeBoundsExpansionAtlas();
        final Set<FeatureChange> result = new HashSet<>();
        for (final Node node : source.nodes())
        {
            final CompleteNode completeNode = CompleteNode.shallowFrom(node)
                    .withLocation(node.getLocation()).withTags(node.getTags());
            result.add(FeatureChange.add(completeNode));
        }
        final Set<FeatureChange> changes = new FeatureChangeBoundsExpander(result, source).apply();
        for (final FeatureChange featureChange : changes)
        {
            if (featureChange.getIdentifier() == 177633000000L)
            {
                Assert.assertEquals(Location.forWkt("POINT (4.2194855 38.8231656)"),
                        ((CompleteNode) featureChange.getAfterView()).getLocation());
                Assert.assertEquals(
                        "POLYGON ((4.2177433 38.8228217, 4.2177433 38.8235147, 4.2197697 38.8235147,"
                                + " 4.2197697 38.8228217, 4.2177433 38.8228217))",
                        featureChange.bounds().toWkt());
            }
        }
    }

    @Test
    public void testEmptyChange()
    {
        final AtlasChangeGenerator generator = atlas -> new HashSet<>();
        final Atlas source = this.rule.getNodeBoundsExpansionAtlas();
        Assert.assertTrue(generator.apply(source).isEmpty());
    }

    @Test
    public void testExpandNode()
    {
        final Atlas source = this.rule.getNodeBoundsExpansionAtlas();

        final Set<FeatureChange> result = new HashSet<>();
        final String key = "changed";
        final String value = "yes";
        for (final AtlasEntity entity : source)
        {
            final CompleteEntity completeEntity = ((CompleteEntity) CompleteEntity.from(entity))
                    .withAddedTag(key, value);
            result.add(FeatureChange.add((AtlasEntity) completeEntity));
        }
        // bonus!
        result.add(FeatureChange.add(new CompleteEdge(123L, PolyLine.wkt(
                "LINESTRING (4.2194855 38.8231656, 4.2202479 38.8233871, 4.2200000 38.8235147)"),
                Maps.hashMap(HIGHWAY, "primary"), 177633000000L, 456L, Sets.hashSet())));
        result.add(FeatureChange.add(new CompleteNode(456L,
                Location.forWkt("POINT (4.2200000 38.8235147)"), Maps.hashMap(HIGHWAY, "primary"),
                Sets.treeSet(), Sets.treeSet(123L), Sets.hashSet())));
        final Set<FeatureChange> changes = new FeatureChangeBoundsExpander(result, source).apply();
        for (final FeatureChange featureChange : changes)
        {
            if (featureChange.getIdentifier() == 177633000000L)
            {
                Assert.assertEquals(
                        "POLYGON ((4.2177433 38.8228217, 4.2177433 38.8235147, 4.2202479 38.8235147,"
                                + " 4.2202479 38.8228217, 4.2177433 38.8228217))",
                        featureChange.bounds().toWkt());
            }
        }
    }

    @Test
    public void testExpandRelation()
    {
        final Atlas source = this.rule.getNodeBoundsExpansionAtlas();
        final Set<FeatureChange> result = new HashSet<>();
        result.add(FeatureChange.add(CompleteEdge.from(source.edge(177630000000L))));
        final Set<FeatureChange> changes = new FeatureChangeBoundsExpander(result, source).apply();
        for (final FeatureChange featureChange : changes)
        {
            if (featureChange.getIdentifier() == 177763000000L)
            {
                Assert.assertEquals(
                        "POLYGON ((4.2177433 38.8228217, 4.2177433 38.8235147, 4.2197697 38.8235147,"
                                + " 4.2197697 38.8228217, 4.2177433 38.8228217))",
                        featureChange.bounds().toWkt());
            }
        }
    }

    @Test
    public void testValidBeforeView()
    {
        final Atlas source = this.rule.getNodeBoundsExpansionAtlas();
        final AtlasChangeGenerator generator = atlas -> Sets.hashSet(FeatureChange.add(CompleteNode
                .shallowFrom(source.node(177628000000L)).withAddedTag(HIGHWAY, "traffic_signals")));
        Assert.assertNotNull(generator.apply(source).iterator().next().getBeforeView());
    }
}
