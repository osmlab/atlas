package org.openstreetmap.atlas.geography.atlas.change;

import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.builder.RelationBean;
import org.openstreetmap.atlas.geography.atlas.complete.CompleteEdge;
import org.openstreetmap.atlas.geography.atlas.complete.CompleteNode;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.ItemType;
import org.openstreetmap.atlas.geography.atlas.items.Node;
import org.openstreetmap.atlas.geography.atlas.items.Relation;
import org.openstreetmap.atlas.geography.atlas.sub.AtlasCutType;
import org.openstreetmap.atlas.streaming.resource.File;
import org.openstreetmap.atlas.tags.JunctionTag;
import org.openstreetmap.atlas.utilities.collections.Iterables;
import org.openstreetmap.atlas.utilities.scalars.Distance;

/**
 * Test various use cases where multiple connected features change.
 *
 * @author matthieun
 */
public class MultipleChangeAtlasTest
{
    @Rule
    public MultipleChangeAtlasTestRule rule = new MultipleChangeAtlasTestRule();

    private Atlas atlas;
    private Atlas subAtlas;
    private ChangeAtlas changeAtlas;
    private final String saveLocally = null;

    @Test
    public void addTurnRestrictions()
    {
        resetAndChange("addTurnRestrictions", new AtlasChangeGeneratorAddTurnRestrictions());
        final Node via = this.changeAtlas.node(3985226613000000L);
        final Relation restriction = via.relations().iterator().next();
        Assert.assertNotNull(restriction);
        final RelationBean members = new RelationBean();
        members.addItem(221434099000002L, "from", ItemType.EDGE);
        members.addItem(via.getIdentifier(), "via", ItemType.NODE);
        members.addItem(634444999000000L, "to", ItemType.EDGE);
        members.addItem(-634444999000000L, "to", ItemType.EDGE);
        Assert.assertEquals(members, restriction.members().asBean());
    }

    @Test
    public void allEdgesAreStraight()
    {
        final Predicate<Edge> straight = edge -> edge.asPolyLine().size() == 2;
        resetAndChange("allEdgesAreStraight", atlas ->
        {
            return Iterables.stream(atlas.edges()).filter(straight.negate()).map(edge ->
            {
                return CompleteEdge.shallowFrom(edge).withPolyLine(
                        new PolyLine(edge.start().getLocation(), edge.end().getLocation()));
            }).map(completeEdge -> FeatureChange.add(completeEdge, atlas)).collectToSet();
        });
        final long straightEdges = Iterables.size(this.changeAtlas.edges(straight));
        final long originalAtlasStraightEdges = Iterables.size(this.atlas.edges(straight));
        final long subAtlasStraightEdges = Iterables.size(this.subAtlas.edges(straight));
        Assert.assertEquals(428, straightEdges);
        Assert.assertEquals(337, originalAtlasStraightEdges);
        Assert.assertEquals(266, subAtlasStraightEdges);
        Assert.assertEquals(straightEdges - originalAtlasStraightEdges + subAtlasStraightEdges,
                this.subAtlas.numberOfEdges());
    }

    @Test
    public void allNodesAreTrafficLights()
    {
        resetAndChange("allNodesAreTrafficLights", atlas ->
        {
            return Iterables.stream(atlas.nodes())
                    .map(node -> CompleteNode.shallowFrom(node).withAddedTag("highway",
                            "traffic_signals"))
                    .map(completeNode -> FeatureChange.add(completeNode, atlas)).collectToSet();
        });
        final Predicate<Node> trafficSignal = node -> "traffic_signals".equals(node.tag("highway"));
        final long changeAtlasNodesWithTrafficSignals = Iterables
                .size(this.changeAtlas.nodes(trafficSignal));
        final long originalAtlasNodesWithTrafficSignals = Iterables
                .size(this.atlas.nodes(trafficSignal));
        final long subAtlasNodesWithTrafficSignals = Iterables
                .size(this.subAtlas.nodes(trafficSignal));
        Assert.assertEquals(162, changeAtlasNodesWithTrafficSignals);
        Assert.assertEquals(9, originalAtlasNodesWithTrafficSignals);
        Assert.assertEquals(4, subAtlasNodesWithTrafficSignals);
        Assert.assertEquals(changeAtlasNodesWithTrafficSignals
                - originalAtlasNodesWithTrafficSignals + subAtlasNodesWithTrafficSignals,
                this.subAtlas.numberOfNodes());
    }

    @Test
    public void removeAllReverseEdges()
    {
        resetAndChange("removeAllReverseEdges", new AtlasChangeGeneratorRemoveReverseEdges());
        final long changeAtlasReverseEdges = Iterables
                .size(this.changeAtlas.edges(edge -> !edge.isMasterEdge()));
        final long subAtlasReverseEdges = Iterables
                .size(this.subAtlas.edges(edge -> !edge.isMasterEdge()));
        final long atlasReverseEdges = Iterables
                .size(this.atlas.edges(edge -> !edge.isMasterEdge()));
        Assert.assertEquals(48, changeAtlasReverseEdges);
        Assert.assertEquals(166, subAtlasReverseEdges);
        // The reverse edges from the subAtlas (marked for removal) plus the ones from the
        // changeAtlas = all the initial reverse edges.
        Assert.assertEquals(changeAtlasReverseEdges + subAtlasReverseEdges, atlasReverseEdges);
    }

    @Test
    public void splitRoundaboutEdges()
    {
        resetAndChange("splitRoundaboutEdges", new AtlasChangeGeneratorSplitRoundabout());
        Assert.assertEquals(6, Iterables.size(this.atlas.edges(JunctionTag::isRoundabout)));
        Assert.assertEquals(12, Iterables.size(this.changeAtlas.edges(JunctionTag::isRoundabout)));
        final Set<Long> extectedParentRelations = this.atlas.edge(221434104000005L).relations()
                .stream().map(Relation::getIdentifier).collect(Collectors.toSet());
        Assert.assertEquals(extectedParentRelations, this.changeAtlas.edge(14L).relations().stream()
                .map(Relation::getIdentifier).collect(Collectors.toSet()));
        Assert.assertEquals(extectedParentRelations, this.changeAtlas.edge(15L).relations().stream()
                .map(Relation::getIdentifier).collect(Collectors.toSet()));
        Assert.assertTrue(this.changeAtlas.relation(3001321000000L).members().asBean()
                .getItemFor(14L, ItemType.EDGE).isPresent());
        Assert.assertTrue(this.changeAtlas.relation(3001321000000L).members().asBean()
                .getItemFor(15L, ItemType.EDGE).isPresent());
        Assert.assertTrue(this.atlas.relation(3001321000000L).members().asBean()
                .getItemFor(221434104000005L, ItemType.EDGE).isPresent());
        // Make sure the removed edge was not added back by a mishap in the relation bean merging
        // somewhere
        Assert.assertFalse(this.changeAtlas.relation(3001321000000L).members().asBean()
                .getItemFor(221434104000005L, ItemType.EDGE).isPresent());
    }

    /**
     * Get the original test Atlas. Cut it smaller to a subAtlas, and use that smaller Atlas to
     * generate the changes. Apply the changes back to the original atlas.
     *
     * @param name
     *            The Atlas name for debugging
     * @param modificationsFunction
     *            The function generating the changes.
     */
    private void resetAndChange(final String name, final AtlasChangeGenerator atlasChangeGenerator)
    {
        this.atlas = this.rule.getAtlas();
        saveLocally("original.atlas", this.atlas);
        this.subAtlas = this.atlas.subAtlas(this.atlas.bounds().contract(Distance.meters(500)),
                AtlasCutType.HARD_CUT_ALL).get();
        saveLocally("sub.atlas", this.subAtlas);
        final Set<FeatureChange> featureChanges = atlasChangeGenerator.apply(this.subAtlas);
        final ChangeBuilder builder = new ChangeBuilder();
        featureChanges.forEach(builder::add);
        final Change change = builder.get();
        this.changeAtlas = new ChangeAtlas(this.atlas, change);
        saveLocally(name + "_change.atlas", this.changeAtlas.cloneToPackedAtlas());
    }

    private void saveLocally(final String name, final Atlas argument)
    {
        if (this.saveLocally != null)
        {
            final File folder = new File(this.saveLocally);
            argument.save(folder.child(name));
        }
    }
}
