package org.openstreetmap.atlas.geography.atlas.pbf.slicing;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.Rectangle;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.AtlasIntegrationTest;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.Line;
import org.openstreetmap.atlas.geography.atlas.pbf.AtlasLoadingOption;
import org.openstreetmap.atlas.geography.atlas.pbf.slicing.identifier.AbstractIdentifierFactory;
import org.openstreetmap.atlas.geography.atlas.pbf.slicing.identifier.PaddingIdentifierFactory;
import org.openstreetmap.atlas.geography.atlas.pbf.slicing.identifier.WaySectionIdentifierFactory;
import org.openstreetmap.atlas.utilities.collections.Iterables;

/**
 * @author tony
 */
public class WaySectionProcessorIntegrationTest extends AtlasIntegrationTest
{
    private static Atlas rawAtlas;
    private static Atlas sectionedAtlas;

    @Before
    public void createAtlas()
    {
        final Rectangle belizeCity = Rectangle.forLocated(
                Location.forString("17.521983, -88.213739"),
                Location.forString("17.491327, -88.178071"));

        // Both atlases are not country-sliced
        if (rawAtlas == null)
        {
            rawAtlas = loadBelizeRaw(belizeCity, AtlasLoadingOption.createOptionWithNoSlicing()
                    .setLoadWaysSpanningCountryBoundaries(false));
        }
        if (sectionedAtlas == null)
        {
            sectionedAtlas = loadBelizeRaw(belizeCity,
                    AtlasLoadingOption.createOptionWithNoSlicing().setWaySectioning(true)
                            .setLoadWaysSpanningCountryBoundaries(false));
        }
    }

    @Test
    public void testPrimaryWay()
    {
        // Primary way should be sectioned
        final long primaryWayShouldBeSectioned = 279042065L;
        final AbstractIdentifierFactory factory = new WaySectionIdentifierFactory(
                PaddingIdentifierFactory.pad(primaryWayShouldBeSectioned));
        Assert.assertEquals(21,
                Iterables.size(rawAtlas.line(primaryWayShouldBeSectioned).getRawGeometry()));
        Assert.assertNull(sectionedAtlas.edge(factory.getReferenceIdentifier()));
        Assert.assertEquals(2,
                Iterables.size(sectionedAtlas.edge(factory.nextIdentifier()).getRawGeometry()));
        Assert.assertEquals(2,
                Iterables.size(sectionedAtlas.edge(factory.nextIdentifier()).getRawGeometry()));
        Assert.assertEquals(2,
                Iterables.size(sectionedAtlas.edge(factory.nextIdentifier()).getRawGeometry()));
        Assert.assertEquals(2,
                Iterables.size(sectionedAtlas.edge(279042065000015L).getRawGeometry()));
        Assert.assertEquals(5,
                Iterables.size(sectionedAtlas.edge(279042065000016L).getRawGeometry()));
        Assert.assertEquals(2,
                Iterables.size(sectionedAtlas.edge(279042065000017L).getRawGeometry()));
        Assert.assertNull(sectionedAtlas.edge(279042065000018L));
    }

    @Test
    public void testSelfIntersectionRing()
    {
        // Way 295734091 will be sectioned into 295734091000001 and 295734091000002. However because
        // 295734091000002 is a ring, which should be further sectioned into 295734091000003 and
        // 295734091000004
        final long selfIntersectionWay = 295734091;
        Assert.assertNotNull(rawAtlas.line(selfIntersectionWay));
        final AbstractIdentifierFactory factory = new WaySectionIdentifierFactory(
                PaddingIdentifierFactory.pad(selfIntersectionWay));
        Assert.assertNull(sectionedAtlas.edge(factory.getReferenceIdentifier()));
        Assert.assertNotNull(sectionedAtlas.edge(factory.nextIdentifier()));
        Assert.assertNotNull(sectionedAtlas.edge(factory.nextIdentifier()));
    }

    @Test
    public void testShapePointOrder()
    {
        // The identifier order of split way should be same as the order of shape point for way with
        // tag oneway=-1
        final long reversedWay = 25977940L;
        final Line nonSplit = rawAtlas.line(reversedWay);
        Assert.assertNotNull(nonSplit);
        final AbstractIdentifierFactory factory = new WaySectionIdentifierFactory(
                PaddingIdentifierFactory.pad(reversedWay));
        Assert.assertNull(sectionedAtlas.edge(factory.getReferenceIdentifier()));
        final Edge firstSplit = sectionedAtlas.edge(factory.nextIdentifier());
        final Edge secondSplit = sectionedAtlas.edge(factory.nextIdentifier());
        Assert.assertNotNull(firstSplit);
        Assert.assertNotNull(secondSplit);
        Assert.assertEquals(nonSplit.asPolyLine().last(), secondSplit.start().getLocation());
        Assert.assertEquals(nonSplit.asPolyLine().first(), firstSplit.end().getLocation());
    }

    @Test
    public void testTotalCounts()
    {
        Assert.assertEquals(0, rawAtlas.numberOfEdges());
        Assert.assertEquals(2841, sectionedAtlas.numberOfEdges());

        Assert.assertEquals(918, rawAtlas.numberOfLines());
        Assert.assertEquals(9, sectionedAtlas.numberOfLines());

        Assert.assertEquals(0, rawAtlas.numberOfNodes());
        Assert.assertEquals(1165, sectionedAtlas.numberOfNodes());

        Assert.assertEquals(0, rawAtlas.numberOfAreas());
        Assert.assertEquals(336, sectionedAtlas.numberOfAreas());

        Assert.assertEquals(1, rawAtlas.numberOfRelations());
        Assert.assertEquals(1, sectionedAtlas.numberOfRelations());

        Assert.assertEquals(5380, rawAtlas.numberOfPoints());
        Assert.assertEquals(82, sectionedAtlas.numberOfPoints());
    }

    @Test
    public void testWaterWay()
    {
        // Waterway (Atlas area) should not be sectioned
        final long waterway = 25977495L;
        Assert.assertNotNull(rawAtlas.line(waterway));
        Assert.assertNotNull(sectionedAtlas.area(PaddingIdentifierFactory.pad(waterway)));
    }

    @Test
    public void testWayAcrossArea()
    {
        // Way across an area should not be sectioned
        final long wayAcrossArea = 194237644L;
        Assert.assertEquals(2, Iterables.size(rawAtlas.line(wayAcrossArea).getRawGeometry()));
        Assert.assertEquals(2, Iterables.size(
                sectionedAtlas.edge(PaddingIdentifierFactory.pad(wayAcrossArea)).getRawGeometry()));
    }
}
