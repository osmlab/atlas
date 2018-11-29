package org.openstreetmap.atlas.geography.atlas.change.rules;

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
import org.openstreetmap.atlas.geography.atlas.change.ChangeType;
import org.openstreetmap.atlas.geography.atlas.change.FeatureChange;

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
