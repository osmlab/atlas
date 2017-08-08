package org.openstreetmap.atlas.tags.annotations.validation;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.tags.AreaTag;
import org.openstreetmap.atlas.tags.BuildingTag;
import org.openstreetmap.atlas.tags.HighwayTag;
import org.openstreetmap.atlas.utilities.testing.TestTaggable;

/**
 * Simple test case verifying String to enum value conversion using reflection
 *
 * @author cstaylor
 */
public class HighwayTagTestCase
{
    @Test
    public void testClassificationEquality()
    {
        Assert.assertTrue(HighwayTag.MOTORWAY.isOfEqualClassification(HighwayTag.MOTORWAY));
        Assert.assertTrue(HighwayTag.PRIMARY.isOfEqualClassification(HighwayTag.PRIMARY_LINK));
        Assert.assertFalse(HighwayTag.PRIMARY.isOfEqualClassification(HighwayTag.SECONDARY));
    }

    @Test
    public void testClassificationMatch()
    {
        Assert.assertTrue(HighwayTag.MOTORWAY.isIdenticalClassification(HighwayTag.MOTORWAY));
        Assert.assertFalse(HighwayTag.PRIMARY.isIdenticalClassification(HighwayTag.SECONDARY));
    }

    @Test
    public void testHighwayFromLink()
    {
        Assert.assertEquals(HighwayTag.PRIMARY, HighwayTag.PRIMARY_LINK.getHighwayFromLink().get());
        Assert.assertEquals(HighwayTag.SECONDARY,
                HighwayTag.SECONDARY_LINK.getHighwayFromLink().get());
        Assert.assertEquals(HighwayTag.TERTIARY,
                HighwayTag.TERTIARY_LINK.getHighwayFromLink().get());
        Assert.assertEquals(HighwayTag.TRUNK, HighwayTag.TRUNK_LINK.getHighwayFromLink().get());
        Assert.assertEquals(HighwayTag.MOTORWAY,
                HighwayTag.MOTORWAY_LINK.getHighwayFromLink().get());
        Assert.assertEquals(Optional.empty(), HighwayTag.BRIDLEWAY.getHighwayFromLink());
    }

    @Test
    public void testIsCarNavigableHighway()
    {
        for (final String test : Arrays.asList("MOTOrWAY", "TRUNK", "PRIMARY", "SECONDARY",
                "TERTIARY", "UNCLASSIFIED", "RESIDENTIAL", "SERVICE", "MOTORWAY_LINK", "TRUNK_LINK",
                "PRIMARY_LINK", "SECONDARY_LINK", "TERTIARY_LINK", "LIvING_STREET", "TRACK",
                "ROAD"))
        {
            Assert.assertTrue(
                    HighwayTag.isCarNavigableHighway(new TestTaggable(HighwayTag.KEY, test)));

        }
    }

    @Test
    public void testIsCarNotNavigableHighway()
    {
        Assert.assertFalse(
                HighwayTag.isCarNavigableHighway(new TestTaggable(HighwayTag.KEY, "dog")));
    }

    @Test
    public void testIsHighwayArea()
    {
        final Map<String, String> tags = new HashMap<>();
        tags.put(HighwayTag.KEY, HighwayTag.PEDESTRIAN.getTagValue());
        tags.put(BuildingTag.KEY, BuildingTag.YES.name().toLowerCase());
        Assert.assertTrue(HighwayTag.isHighwayArea(new TestTaggable(tags)));

        tags.put(HighwayTag.KEY, HighwayTag.FOOTWAY.getTagValue());
        Assert.assertTrue(HighwayTag.isHighwayArea(new TestTaggable(tags)));

        tags.remove(BuildingTag.KEY);
        Assert.assertFalse(HighwayTag.isHighwayArea(new TestTaggable(tags)));

        tags.put(AreaTag.KEY, AreaTag.YES.name().toLowerCase());
        Assert.assertTrue(HighwayTag.isHighwayArea(new TestTaggable(tags)));

        tags.put(HighwayTag.KEY, HighwayTag.TRACK.getTagValue());
        Assert.assertFalse(HighwayTag.isHighwayArea(new TestTaggable(tags)));
    }

    @Test
    public void testLessImportant()
    {
        Assert.assertTrue(HighwayTag.SECONDARY.isLessImportantThan(HighwayTag.PRIMARY));
        Assert.assertTrue(HighwayTag.TERTIARY.isLessImportantThan(HighwayTag.TRUNK));

        // Testing equivalence
        Assert.assertTrue(HighwayTag.PRIMARY.isLessImportantThanOrEqualTo(HighwayTag.PRIMARY));
    }

    @Test
    public void testLinkFromHighway()
    {
        Assert.assertEquals(HighwayTag.PRIMARY_LINK, HighwayTag.PRIMARY.getLinkFromHighway().get());
        Assert.assertEquals(HighwayTag.SECONDARY_LINK,
                HighwayTag.SECONDARY.getLinkFromHighway().get());
        Assert.assertEquals(HighwayTag.TERTIARY_LINK,
                HighwayTag.TERTIARY.getLinkFromHighway().get());
        Assert.assertEquals(HighwayTag.TRUNK_LINK, HighwayTag.TRUNK.getLinkFromHighway().get());
        Assert.assertEquals(HighwayTag.MOTORWAY_LINK,
                HighwayTag.MOTORWAY.getLinkFromHighway().get());
        Assert.assertEquals(Optional.empty(), HighwayTag.BRIDLEWAY.getLinkFromHighway());
    }

    @Test
    public void testMoreImportant()
    {
        Assert.assertTrue(HighwayTag.MOTORWAY.isMoreImportantThan(HighwayTag.TERTIARY));
        Assert.assertTrue(HighwayTag.PRIMARY.isMoreImportantThan(HighwayTag.SECONDARY));

        // Testing equivalence
        Assert.assertTrue(HighwayTag.PRIMARY.isMoreImportantThanOrEqualTo(HighwayTag.PRIMARY));
    }
}
