package org.openstreetmap.atlas.geography.atlas.pbf;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.MultiPolygon;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.Polygon;
import org.openstreetmap.atlas.geography.Rectangle;
import org.openstreetmap.atlas.geography.Segment;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.builder.RelationBean;
import org.openstreetmap.atlas.geography.atlas.builder.store.AtlasPrimitiveLineItem;
import org.openstreetmap.atlas.geography.atlas.builder.store.AtlasPrimitiveLocationItem;
import org.openstreetmap.atlas.geography.atlas.builder.store.AtlasPrimitiveObjectStore;
import org.openstreetmap.atlas.geography.atlas.builder.store.AtlasPrimitiveRelation;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.ItemType;
import org.openstreetmap.atlas.geography.atlas.items.Node;
import org.openstreetmap.atlas.geography.atlas.items.Relation;
import org.openstreetmap.atlas.geography.boundary.CountryBoundaryMap;
import org.openstreetmap.atlas.streaming.resource.InputStreamResource;
import org.openstreetmap.atlas.streaming.resource.Resource;
import org.openstreetmap.atlas.streaming.resource.StringResource;
import org.openstreetmap.atlas.streaming.resource.WritableResource;
import org.openstreetmap.atlas.tags.HighwayTag;
import org.openstreetmap.atlas.tags.SyntheticBoundaryNodeTag;
import org.openstreetmap.atlas.tags.filters.ConfiguredTaggableFilter;
import org.openstreetmap.atlas.utilities.collections.Maps;
import org.openstreetmap.atlas.utilities.configuration.StandardConfiguration;
import org.openstreetmap.atlas.utilities.scalars.Distance;
import org.openstreetmap.atlas.utilities.testing.OsmFileParser;
import org.openstreetmap.atlas.utilities.testing.OsmFileToPbf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author matthieun
 */
public class OsmPbfLoaderTest
{
    private static final Logger logger = LoggerFactory.getLogger(OsmPbfLoaderTest.class);

    private static final String COUNTRY_1_NAME = "COUNTRY_1";
    private static final String COUNTRY_2_NAME = "COUNTRY_2";
    private static final Location SHAPEPOINT = Location.forString("37.328076,-122.031869");
    private static final Location DE_ANZA_AT_280 = Location.forString("37.334384,-122.032327");
    private static final Location OUTSIDE_COUNTRY_1 = Location.forString("37.350987,-121.988496");

    private MultiPolygon countryShape1;
    private MultiPolygon countryShape2;
    private CountryBoundaryMap countryBoundariesAll;
    private CountryBoundaryMap countryBoundaries1;
    private AtlasPrimitiveObjectStore store;

    @Before
    public void init()
    {
        final Polygon polygon1 = new Polygon(
                Location.forString("37.330310627190194,-122.08282470703124"),
                Location.forString("37.25014416051375,-122.04025268554688"),
                Location.forString("37.242218476496625,-121.937255859375"),
                Location.forString("37.28060928450999,-121.95030212402344"),
                Location.forString("37.33310876404607,-122.02986717224121"),
                Location.forString("37.33604328341095,-122.06462860107422"));
        final Polygon polygon2 = new Polygon(
                Location.forString("37.34232140492521,-122.06904888153078"),
                Location.forString("37.33474664945566,-122.06102371215819"),
                Location.forString("37.331308755038414,-122.0297384262085"),
                Location.forString("37.34051308677875,-122.02124118804932"),
                Location.forString("37.3447096829136,-122.03505992889404"));
        this.countryShape1 = MultiPolygon.forPolygon(polygon1);
        this.countryShape2 = MultiPolygon.forPolygon(polygon2);
        final Map<String, MultiPolygon> boundaries = new HashMap<>();
        boundaries.put(COUNTRY_1_NAME, this.countryShape1);
        this.countryBoundaries1 = CountryBoundaryMap.fromBoundaryMap(boundaries);
        boundaries.put(COUNTRY_2_NAME, this.countryShape2);
        this.countryBoundariesAll = CountryBoundaryMap.fromBoundaryMap(boundaries);
        this.store = new AtlasPrimitiveObjectStore();

        // Add Nodes
        this.store.addNode(
                new AtlasPrimitiveLocationItem(1, Location.CROSSING_85_280, Maps.stringMap()));
        this.store.addNode(
                new AtlasPrimitiveLocationItem(2, Location.CROSSING_85_17, Maps.stringMap()));
        this.store.addNode(new AtlasPrimitiveLocationItem(3, Location.TEST_7, Maps.stringMap()));
        this.store.addNode(
                new AtlasPrimitiveLocationItem(4, Location.EIFFEL_TOWER, Maps.stringMap()));
        this.store.addNode(new AtlasPrimitiveLocationItem(5, Location.COLOSSEUM, Maps.stringMap()));
        this.store.addNode(new AtlasPrimitiveLocationItem(6, Location.TEST_6, Maps.stringMap()));
        this.store.addNode(new AtlasPrimitiveLocationItem(7, Location.TEST_6, Maps.stringMap()));
        this.store.addNode(new AtlasPrimitiveLocationItem(8, SHAPEPOINT, Maps.stringMap()));
        this.store.addNode(new AtlasPrimitiveLocationItem(9,
                new Segment(Location.TEST_6, Location.TEST_7).middle(),
                Maps.stringMap("tag_key", "tag_value")));
        this.store.addNode(new AtlasPrimitiveLocationItem(10, DE_ANZA_AT_280,
                Maps.stringMap("tag_key", "tag_value")));
        this.store.addNode(new AtlasPrimitiveLocationItem(11, OUTSIDE_COUNTRY_1, Maps.stringMap()));

        // Add Edges
        this.store.addEdge(new AtlasPrimitiveLineItem(3,
                new PolyLine(Location.CROSSING_85_280, SHAPEPOINT, Location.TEST_7),
                Maps.stringMap(HighwayTag.KEY, HighwayTag.MOTORWAY.name().toLowerCase())));
        this.store.addEdge(new AtlasPrimitiveLineItem(7,
                new PolyLine(Location.CROSSING_85_280, Location.CROSSING_85_17),
                Maps.stringMap(HighwayTag.KEY, HighwayTag.MOTORWAY.name().toLowerCase())));
        this.store.addEdge(
                new AtlasPrimitiveLineItem(8, new PolyLine(Location.TEST_7, DE_ANZA_AT_280),
                        Maps.stringMap(HighwayTag.KEY, HighwayTag.MOTORWAY.name().toLowerCase())));
        this.store.addEdge(
                new AtlasPrimitiveLineItem(9, new PolyLine(DE_ANZA_AT_280, OUTSIDE_COUNTRY_1),
                        Maps.stringMap(HighwayTag.KEY, HighwayTag.MOTORWAY.name().toLowerCase())));

        // Add Lines
        final PolyLine line4 = new Segment(Location.TEST_6, Location.TEST_7);
        this.store.addLine(new AtlasPrimitiveLineItem(4, line4, Maps.stringMap()));
        final PolyLine line5 = new Segment(Location.COLOSSEUM, Location.EIFFEL_TOWER);
        this.store.addLine(new AtlasPrimitiveLineItem(5, line5, Maps.stringMap()));
        this.store.addLine(
                new AtlasPrimitiveLineItem(6, line4, Maps.stringMap("boundary", "administrative")));

        // Add Relation
        final RelationBean relationBean = new RelationBean();
        relationBean.addItem(4L, "first", ItemType.LINE);
        relationBean.addItem(5L, "second", ItemType.LINE);
        this.store.addRelation(new AtlasPrimitiveRelation(123, 123, relationBean, Maps.stringMap(),
                Rectangle.forLocated(line4, line5)));
    }

    /**
     * In some cases, ways can be outside the loading area (still inside the osm PBF) and connected
     * to some other ways that are inside the loading area, but still inside the country boundary.
     * That happens at shard boundaries, which do a soft cut.
     *
     * @throws IOException
     *             Exception thrown when {@link OsmosisReaderMock} fails to close
     */
    @Test
    public void testBoundaryNodesForWayOutsideLoadingAreaButInsideCountryBoundary()
            throws IOException
    {
        try (OsmosisReaderMock osmosis = new OsmosisReaderMock(this.store))
        {
            final OsmPbfLoader osmPbfLoader = new OsmPbfLoader(() -> osmosis,
                    // Here reduce the loading bounds to a small area around Edge 3. In this case
                    // Node 3 is included in the country boundary but not in the loading area. It
                    // should not have any boundary tag.
                    MultiPolygon.forPolygon(SHAPEPOINT.boxAround(Distance.meters(10))),
                    AtlasLoadingOption.createOptionWithAllEnabled(this.countryBoundaries1)
                            .setAdditionalCountryCodes(COUNTRY_1_NAME));
            final Atlas atlas = osmPbfLoader.read();
            logger.info("{}", atlas);
            final Edge edgeIn = atlas.edgesIntersecting(DE_ANZA_AT_280.bounds()).iterator().next();
            final Node nodeIn = edgeIn.end();
            Assert.assertNull(nodeIn.tag(SyntheticBoundaryNodeTag.KEY));
        }
    }

    @Test
    public void testDifferentEdgeFilterIncludingAccessNo()
    {
        final Resource osmFromJosm = new InputStreamResource(
                () -> OsmPbfLoaderTest.class.getResourceAsStream("one_way_roads_in_AIA.osm"));
        final WritableResource osmFile = new StringResource();
        final WritableResource pbfFile = new StringResource();
        final Resource boundaries = new InputStreamResource(
                () -> OsmPbfLoaderTest.class.getResourceAsStream("AIA_boundary.txt"));
        new OsmFileParser().update(osmFromJosm, osmFile);
        new OsmFileToPbf().update(osmFile, pbfFile);
        final CountryBoundaryMap countryBoundaryMap = CountryBoundaryMap.fromPlainText(boundaries);
        final MultiPolygon boundary = countryBoundaryMap.countryBoundary("AIA").get(0)
                .getBoundary();
        logger.debug("Boundary: {}", boundary.toWkt());
        final AtlasLoadingOption option = AtlasLoadingOption
                .createOptionWithAllEnabled(countryBoundaryMap);
        option.setEdgeFilter(
                new ConfiguredTaggableFilter(new StandardConfiguration(new InputStreamResource(
                        () -> OsmPbfLoaderTest.class.getResourceAsStream("edge-filter.json")))));
        final OsmPbfLoader loader = new OsmPbfLoader(pbfFile, boundary, option);
        final Atlas atlas = loader.read();
        logger.debug("Atlas: {}", atlas);

        // Edges with access=no that need to be included
        Assert.assertNotNull(atlas.edge(205527844000002L));
        Assert.assertNotNull(atlas.edge(205527844000001L));
        Assert.assertNotNull(atlas.edge(205527857000000L));
        // Edge without access=no but that needs to be way-sectioned
        Assert.assertNotNull(atlas.edge(200693734000003L));
        Assert.assertNotNull(atlas.edge(200693734000004L));
    }

    @Test
    public void testEmptyRelations() throws IOException
    {
        try (OsmosisReaderMock osmosis = new OsmosisReaderMock(this.store))
        {
            final OsmPbfLoader osmPbfLoader = new OsmPbfLoader(() -> osmosis, MultiPolygon.MAXIMUM,
                    AtlasLoadingOption.createOptionWithAllEnabled(this.countryBoundariesAll)
                            .setAdditionalCountryCodes(COUNTRY_1_NAME));
            final Atlas atlas = osmPbfLoader.read();
            logger.info("{}", atlas);
            Assert.assertEquals(2, atlas.numberOfLines());
            Assert.assertEquals(1, atlas.numberOfRelations());
            final Relation relation = atlas.relations().iterator().next();
            Assert.assertEquals(2, relation.members().size());
            Assert.assertEquals(2, atlas.numberOfEdges());
        }
    }

    @Test
    public void testNoFilter() throws IOException
    {
        try (OsmosisReaderMock osmosis = new OsmosisReaderMock(this.store))
        {
            final OsmPbfLoader osmPbfLoader = new OsmPbfLoader(() -> osmosis, MultiPolygon.MAXIMUM,
                    AtlasLoadingOption.withNoFilter());
            final Atlas atlas = osmPbfLoader.read();
            logger.info("{}", atlas);
            Assert.assertEquals(3, atlas.numberOfLines());
        }
    }

    @Test
    public void testOutsideWayBoundaryNodes() throws IOException
    {
        try (OsmosisReaderMock osmosis = new OsmosisReaderMock(this.store))
        {
            final OsmPbfLoader osmPbfLoader = new OsmPbfLoader(() -> osmosis,
                    this.countryBoundaries1.countryBoundary(COUNTRY_1_NAME).iterator().next()
                            .getBoundary(),
                    AtlasLoadingOption.createOptionWithAllEnabled(this.countryBoundaries1)
                            .setAdditionalCountryCodes(COUNTRY_1_NAME));
            final Atlas atlas = osmPbfLoader.read();
            logger.info("{}", atlas);
            final Edge edgeOut = atlas.edgesIntersecting(OUTSIDE_COUNTRY_1.bounds()).iterator()
                    .next();
            final Node nodeOut = edgeOut.end();
            Assert.assertNotNull(nodeOut.tag(SyntheticBoundaryNodeTag.KEY));
            final Edge edgeIn = atlas.edgesIntersecting(Location.TEST_7.bounds()).iterator().next();
            final Node nodeIn = edgeIn.end();
            Assert.assertNull(nodeIn.tag(SyntheticBoundaryNodeTag.KEY));
            Assert.assertEquals(2, atlas.numberOfPoints());
        }
    }
}
