package org.openstreetmap.atlas.geography.atlas.pbf.slicing;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.Rectangle;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.AtlasIntegrationTest;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
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
    private static Atlas unSlicedAtlas;
    private static Atlas slicedAtlas;

    @Before
    public void createAtlas()
    {
        final Rectangle belizeCity = Rectangle.forLocated(
                Location.forString("17.521983, -88.213739"),
                Location.forString("17.491327, -88.178071"));
        if (unSlicedAtlas == null)
        {
            unSlicedAtlas = loadBelizeRaw(belizeCity, AtlasLoadingOption.createOptionWithNoSlicing()
                    .setLoadWaysSpanningCountryBoundaries(false));
        }
        if (slicedAtlas == null)
        {
            slicedAtlas = loadBelizeRaw(belizeCity, AtlasLoadingOption.createOptionWithNoSlicing()
                    .setWaySectioning(true).setLoadWaysSpanningCountryBoundaries(false));
        }
    }

    @Test
    public void testPrimaryWay()
    {
        // Primary way should be sliced
        final long primaryWayShouldBeSliced = 279042065L;
        final AbstractIdentifierFactory factory = new WaySectionIdentifierFactory(
                PaddingIdentifierFactory.pad(primaryWayShouldBeSliced));
        Assert.assertEquals(21,
                Iterables.size(unSlicedAtlas.edge(primaryWayShouldBeSliced).getRawGeometry()));
        Assert.assertNull(slicedAtlas.edge(factory.getReferenceIdentifier()));
        Assert.assertEquals(2,
                Iterables.size(slicedAtlas.edge(factory.nextIdentifier()).getRawGeometry()));
        Assert.assertEquals(2,
                Iterables.size(slicedAtlas.edge(factory.nextIdentifier()).getRawGeometry()));
        Assert.assertEquals(2,
                Iterables.size(slicedAtlas.edge(factory.nextIdentifier()).getRawGeometry()));
        Assert.assertEquals(2, Iterables.size(slicedAtlas.edge(279042065000015L).getRawGeometry()));
        Assert.assertEquals(5, Iterables.size(slicedAtlas.edge(279042065000016L).getRawGeometry()));
        Assert.assertEquals(2, Iterables.size(slicedAtlas.edge(279042065000017L).getRawGeometry()));
        Assert.assertNull(slicedAtlas.edge(279042065000018L));
    }

    @Test
    public void testSelfIntersectionRing()
    {
        // Way 295734091 will be sliced into 295734091000001 and 295734091000002. However because
        // 295734091000002 is a ring, which should be further sliced into 295734091000003 and
        // 295734091000004
        final long selfIntersectionWay = 295734091;
        Assert.assertNotNull(unSlicedAtlas.edge(selfIntersectionWay));
        final AbstractIdentifierFactory factory = new WaySectionIdentifierFactory(
                PaddingIdentifierFactory.pad(selfIntersectionWay));
        Assert.assertNull(slicedAtlas.edge(factory.getReferenceIdentifier()));
        Assert.assertNotNull(slicedAtlas.edge(factory.nextIdentifier()));
        Assert.assertNotNull(slicedAtlas.edge(factory.nextIdentifier()));
    }

    @Test
    public void testShapePointOrder()
    {
        // The identifier order of split way should be same as the order of shape point for way with
        // tag oneway=-1
        final long reversedWay = 25977940L;
        final Edge nonSplit = unSlicedAtlas.edge(reversedWay);
        Assert.assertNotNull(nonSplit);
        final AbstractIdentifierFactory factory = new WaySectionIdentifierFactory(
                PaddingIdentifierFactory.pad(reversedWay));
        Assert.assertNull(slicedAtlas.edge(factory.getReferenceIdentifier()));
        final Edge firstSplit = slicedAtlas.edge(factory.nextIdentifier());
        final Edge secondSplit = slicedAtlas.edge(factory.nextIdentifier());
        Assert.assertNotNull(firstSplit);
        Assert.assertNotNull(secondSplit);
        Assert.assertEquals(nonSplit.start().getLocation(), firstSplit.start().getLocation());
        Assert.assertEquals(nonSplit.end().getLocation(), secondSplit.end().getLocation());
    }

    @Test
    public void testTotalNumber()
    {
        // Total Numbers
        Assert.assertEquals(1020, unSlicedAtlas.numberOfEdges());
        Assert.assertEquals(2843, slicedAtlas.numberOfEdges());

        Assert.assertEquals(8, unSlicedAtlas.numberOfLines());
        Assert.assertEquals(8, slicedAtlas.numberOfLines());

        Assert.assertEquals(959, unSlicedAtlas.numberOfNodes());
        Assert.assertEquals(1167, slicedAtlas.numberOfNodes());

        Assert.assertEquals(336, unSlicedAtlas.numberOfAreas());
        Assert.assertEquals(336, slicedAtlas.numberOfAreas());

        Assert.assertEquals(1, unSlicedAtlas.numberOfRelations());
        Assert.assertEquals(1, slicedAtlas.numberOfRelations());

        Assert.assertEquals(82, unSlicedAtlas.numberOfPoints());
        Assert.assertEquals(82, slicedAtlas.numberOfPoints());
    }

    @Test
    public void testWaterWay()
    {
        // Waterway (Atlas area) should not be sliced
        final long waterway = 25977495L;
        Assert.assertNotNull(unSlicedAtlas.area(waterway));
        Assert.assertNotNull(slicedAtlas.area(PaddingIdentifierFactory.pad(waterway)));
    }

    @Test
    public void testWayAcrossArea()
    {
        // Way across an area should not be sliced
        final long wayAcrossArea = 194237644L;
        Assert.assertEquals(2, Iterables.size(unSlicedAtlas.edge(wayAcrossArea).getRawGeometry()));
        Assert.assertEquals(2, Iterables.size(
                slicedAtlas.edge(PaddingIdentifierFactory.pad(wayAcrossArea)).getRawGeometry()));
    }
}
