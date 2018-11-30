package org.openstreetmap.atlas.geography.atlas.change;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.atlas.bloated.BloatedArea;
import org.openstreetmap.atlas.geography.atlas.bloated.BloatedEdge;
import org.openstreetmap.atlas.geography.atlas.bloated.BloatedLine;
import org.openstreetmap.atlas.geography.atlas.bloated.BloatedNode;
import org.openstreetmap.atlas.geography.atlas.bloated.BloatedPoint;
import org.openstreetmap.atlas.geography.atlas.bloated.BloatedRelation;
import org.openstreetmap.atlas.geography.atlas.items.Relation;
import org.openstreetmap.atlas.utilities.collections.Iterables;
import org.openstreetmap.atlas.utilities.collections.Maps;
import org.openstreetmap.atlas.utilities.collections.Sets;

/**
 * @author matthieun
 */
public class FeatureChangeTest
{
    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Test
    public void testAreaSuperShallow()
    {
        this.expectedException.expect(CoreException.class);
        this.expectedException.expectMessage("does not contain anything useful.");

        new FeatureChange(ChangeType.ADD, new BloatedArea(123L, null, null, null));
    }

    @Test
    public void testEdgeSuperShallow()
    {
        this.expectedException.expect(CoreException.class);
        this.expectedException.expectMessage("does not contain anything useful.");

        new FeatureChange(ChangeType.ADD, new BloatedEdge(123L, null, null, null, null, null));
    }

    @Test
    public void testLineSuperShallow()
    {
        this.expectedException.expect(CoreException.class);
        this.expectedException.expectMessage("does not contain anything useful.");

        new FeatureChange(ChangeType.ADD, new BloatedLine(123L, null, null, null));
    }

    @Test
    public void testMergeAddRemove()
    {
        this.expectedException.expect(CoreException.class);
        this.expectedException.expectMessage("Cannot merge two feature changes");

        final FeatureChange featureChange1 = new FeatureChange(ChangeType.ADD,
                new BloatedPoint(123L, null, Maps.hashMap(), null));
        final FeatureChange featureChange2 = new FeatureChange(ChangeType.REMOVE,
                new BloatedPoint(123L, null, null, null));
        featureChange1.merge(featureChange2);
    }

    @Test
    public void testMergeDifferentType()
    {
        this.expectedException.expect(CoreException.class);
        this.expectedException.expectMessage("Cannot merge two feature changes");

        final FeatureChange featureChange1 = new FeatureChange(ChangeType.ADD,
                new BloatedPoint(123L, null, Maps.hashMap(), null));
        final FeatureChange featureChange2 = new FeatureChange(ChangeType.ADD,
                new BloatedArea(123L, null, Maps.hashMap(), null));
        featureChange1.merge(featureChange2);
    }

    @Test
    public void testMergeRelations()
    {
        final FeatureChange featureChange1 = new FeatureChange(ChangeType.ADD,
                new BloatedArea(123L, null, null, Sets.hashSet(456L)));
        final FeatureChange featureChange2 = new FeatureChange(ChangeType.ADD,
                new BloatedArea(123L, null, null, Sets.hashSet(567L)));
        Assert.assertEquals(Sets.hashSet(456L, 567L),
                Iterables.stream(featureChange1.merge(featureChange2).getReference().relations())
                        .map(Relation::getIdentifier).collectToSet());
    }

    @Test
    public void testMergeTags()
    {
        final FeatureChange featureChange1 = new FeatureChange(ChangeType.ADD,
                new BloatedArea(123L, null, Maps.hashMap("key1", "value1"), null));
        final FeatureChange featureChange2 = new FeatureChange(ChangeType.ADD,
                new BloatedArea(123L, null, Maps.hashMap("key2", "value2"), null));
        Assert.assertEquals(Maps.hashMap("key1", "value1", "key2", "value2"),
                featureChange1.merge(featureChange2).getReference().getTags());
    }

    @Test
    public void testMergeTagsCollision()
    {
        this.expectedException.expect(CoreException.class);
        this.expectedException.expectMessage("Cannot merge two feature changes");

        final FeatureChange featureChange1 = new FeatureChange(ChangeType.ADD,
                new BloatedArea(123L, null, Maps.hashMap("key1", "value1"), null));
        final FeatureChange featureChange2 = new FeatureChange(ChangeType.ADD,
                new BloatedArea(123L, null, Maps.hashMap("key1", "value2"), null));
        featureChange1.merge(featureChange2);
    }

    @Test
    public void testNodeSuperShallow()
    {
        this.expectedException.expect(CoreException.class);
        this.expectedException.expectMessage("does not contain anything useful.");

        new FeatureChange(ChangeType.ADD, new BloatedNode(123L, null, null, null, null, null));
    }

    @Test
    public void testPointSuperShallow()
    {
        this.expectedException.expect(CoreException.class);
        this.expectedException.expectMessage("does not contain anything useful.");

        new FeatureChange(ChangeType.ADD, new BloatedPoint(123L, null, null, null));
    }

    @Test
    public void testRelationSuperShallow()
    {
        this.expectedException.expect(CoreException.class);
        this.expectedException.expectMessage("does not contain anything useful.");

        new FeatureChange(ChangeType.ADD,
                new BloatedRelation(123L, null, null, null, null, null, null, null));
    }
}
