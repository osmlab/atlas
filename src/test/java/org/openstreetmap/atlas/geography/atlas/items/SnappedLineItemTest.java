package org.openstreetmap.atlas.geography.atlas.items;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.utilities.scalars.Distance;

/**
 * @author chunzc
 */
public class SnappedLineItemTest
{
    @Rule
    public final SnappedLineItemTestRule rule = new SnappedLineItemTestRule();

    @Test
    public void testEquals()
    {
        final Atlas atlas = this.rule.getAtlas();
        final Location point = atlas.point(1L).getLocation();
        final LineItem targetLineItem = atlas.lineItems(item -> item.getIdentifier() == 8000000L)
                .iterator().next();
        final SnappedLineItem candidate = new SnappedLineItem(
                point.snapTo(targetLineItem.asPolyLine()), targetLineItem);
        final List<SnappedLineItem> snappedLineItemList = atlas
                .snapsLineItem(atlas.node(1L).getLocation(), Distance.meters(100));
        final SnappedLineItem snappedLineItem = snappedLineItemList
                .stream().filter(lineItem -> lineItem.getLineItem()
                        .getIdentifier() == targetLineItem.getIdentifier())
                .collect(Collectors.toList()).get(0);
        Assert.assertTrue(snappedLineItemList.contains(candidate));
        Assert.assertEquals(candidate, snappedLineItem);
        Assert.assertEquals(candidate.hashCode(), snappedLineItem.hashCode());
        Assert.assertNotEquals(targetLineItem, snappedLineItem);
        Assert.assertNotEquals(null, snappedLineItem);
        Assert.assertNotEquals(snappedLineItemList.get(0), snappedLineItemList.get(1));
    }

    @Test
    public void testToString()
    {
        final String targetString = "[SnappedLineItem: LineItem: 8000000, Origin: POINT (-61.336198 15.420563), Snap: POINT (-61.336198 15.420563)]";
        final Atlas atlas = this.rule.getAtlas();
        final Location point = atlas.point(1L).getLocation();
        final LineItem targetLineItem = atlas.lineItems(item -> item.getIdentifier() == 8000000L)
                .iterator().next();
        final SnappedLineItem candidate = new SnappedLineItem(
                point.snapTo(targetLineItem.asPolyLine()), targetLineItem);
        Assert.assertEquals(targetString, candidate.toString());
    }
}
