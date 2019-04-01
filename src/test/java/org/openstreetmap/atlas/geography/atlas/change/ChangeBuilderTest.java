package org.openstreetmap.atlas.geography.atlas.change;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.Polygon;
import org.openstreetmap.atlas.geography.atlas.complete.CompleteArea;
import org.openstreetmap.atlas.geography.atlas.complete.CompleteLine;
import org.openstreetmap.atlas.geography.atlas.complete.CompleteNode;
import org.openstreetmap.atlas.geography.atlas.complete.CompletePoint;
import org.openstreetmap.atlas.utilities.collections.Maps;

/**
 * @author Yazad Khambata
 */
public class ChangeBuilderTest
{

    public static final long TEST_IDENTIFIER = 123L;

    @Test
    public void addAll()
    {
        final FeatureChange featureChange1 = newAreaFeatureChange();
        final FeatureChange featureChange2 = newLineFeatureChange();
        final FeatureChange featureChange3 = newNodeFeatureChange();
        final FeatureChange featureChange4 = newPointFeatureChange();

        final Change change1 = ChangeBuilder.newInstance().add(featureChange1).add(featureChange2)
                .add(featureChange3).add(featureChange4).get();

        final Change change2 = ChangeBuilder.newInstance().addAll(featureChange1, featureChange2)
                .addAll(Arrays.asList(featureChange3, featureChange4)).get();

        Assert.assertEquals(change1, change2);
    }

    @Test
    public void fluency()
    {
        final FeatureChange featureChange1 = newAreaFeatureChange();
        final FeatureChange featureChange2 = newLineFeatureChange();
        final ChangeBuilder verboseBuilder = new ChangeBuilder();
        verboseBuilder.add(featureChange1);
        verboseBuilder.add(featureChange2);
        final Change verboseChange = verboseBuilder.get();
        final Change fluentChange = ChangeBuilder.newInstance().add(featureChange1)
                .add(featureChange2).get();
        Assert.assertEquals(verboseChange, fluentChange);
    }

    @Test
    public void nameAvailable()
    {
        final Change change = ChangeBuilder.newInstance().withName(this.getClass().getName())
                .add(newAreaFeatureChange()).get();
        Assert.assertEquals(this.getClass().getName(), change.getName());
    }

    @Test
    public void nameNull()
    {
        final Change change = ChangeBuilder.newInstance().withName(null).add(newAreaFeatureChange())
                .get();
        Assert.assertEquals(String.valueOf(change.getIdentifier()), change.getName());
    }

    @Test
    public void nameNotAvailable()
    {
        final Change change = ChangeBuilder.newInstance().add(newAreaFeatureChange()).get();
        Assert.assertEquals(String.valueOf(change.getIdentifier()), change.getName());
    }

    private FeatureChange newAreaFeatureChange()
    {
        return new FeatureChange(ChangeType.ADD,
                new CompleteArea(TEST_IDENTIFIER, Polygon.CENTER, Maps.hashMap(), null));
    }

    private FeatureChange newLineFeatureChange()
    {
        return new FeatureChange(ChangeType.REMOVE,
                new CompleteLine(TEST_IDENTIFIER, PolyLine.CENTER, Maps.hashMap(), null));
    }

    private FeatureChange newNodeFeatureChange()
    {
        return new FeatureChange(ChangeType.ADD, new CompleteNode(TEST_IDENTIFIER, Location.CENTER,
                Maps.hashMap(), null, null, null));
    }

    private FeatureChange newPointFeatureChange()
    {
        return new FeatureChange(ChangeType.ADD,
                new CompletePoint(TEST_IDENTIFIER, Location.CENTER, Maps.hashMap(), null));
    }
}
