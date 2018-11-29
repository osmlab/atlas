package org.openstreetmap.atlas.geography.atlas.change;

import java.util.Set;
import java.util.function.Function;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.builder.text.TextAtlasBuilder;
import org.openstreetmap.atlas.geography.atlas.delta.AtlasDelta;
import org.openstreetmap.atlas.geography.atlas.sub.AtlasCutType;
import org.openstreetmap.atlas.streaming.resource.File;
import org.openstreetmap.atlas.streaming.resource.StringResource;
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

    @Ignore
    @Test
    public void buildTextAtlas()
    {
        final Atlas atlas = this.rule.getAtlas();
        final TextAtlasBuilder textAtlasBuilder = new TextAtlasBuilder();
        final StringResource stringResource = new StringResource();
        textAtlasBuilder.write(atlas, stringResource);
        final Atlas atlas2 = textAtlasBuilder.read(stringResource);
        final AtlasDelta delta = new AtlasDelta(atlas, atlas2).generate();
        System.out.println(delta.toGeoJson());
        final String path = MultipleChangeAtlasTest.class
                .getResource("MultipleChangeAtlasTest.atlas.txt").getPath();
        System.out.println(path);
        textAtlasBuilder.write(atlas, new File(path));
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
    private void resetAndChange(final String name,
            final Function<Atlas, Set<FeatureChange>> modificationsFunction)
    {
        final File folder = new File("/Users/matthieun/Desktop/test/");
        this.atlas = this.rule.getAtlas();
        this.atlas.save(folder.child("original.atlas"));
        this.subAtlas = this.atlas.subAtlas(this.atlas.bounds().contract(Distance.meters(500)),
                AtlasCutType.HARD_CUT_ALL).get();
        this.subAtlas.save(folder.child("sub.atlas"));
        final Set<FeatureChange> featureChanges = modificationsFunction.apply(this.subAtlas);
        final ChangeBuilder builder = new ChangeBuilder();
        featureChanges.forEach(builder::add);
        final Change change = builder.get();
        this.changeAtlas = new ChangeAtlas(this.atlas, change);
        this.changeAtlas.cloneToPackedAtlas().save(folder.child(name + "_change.atlas"));
    }
}
