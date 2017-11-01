package org.openstreetmap.atlas.geography.atlas.pbf;

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
import org.openstreetmap.atlas.tags.HighwayTag;
import org.openstreetmap.atlas.tags.SyntheticBoundaryNodeTag;
import org.openstreetmap.atlas.utilities.collections.Maps;
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

    private MultiPolygon countryShape1;
    private MultiPolygon countryShape2;
    private CountryBoundaryMap countryBoundariesAll;
    private CountryBoundaryMap countryBoundaries1;
    private AtlasPrimitiveObjectStore store;

    @Before
    public void init()
    {
        final Polygon polygon1 = new Polygon(Location.TEST_6, Location.TEST_2, Location.TEST_5);
        final Polygon polygon2 = new Polygon(Location.TEST_1, Location.TEST_2, Location.TEST_5);
        this.countryShape1 = MultiPolygon.forPolygon(polygon1);
        this.countryShape2 = MultiPolygon.forPolygon(polygon2);
        final Map<String, MultiPolygon> boundaries = new HashMap<>();
        boundaries.put(COUNTRY_1_NAME, this.countryShape1);
        this.countryBoundaries1 = new CountryBoundaryMap(boundaries);
        boundaries.put(COUNTRY_2_NAME, this.countryShape2);
        this.countryBoundariesAll = new CountryBoundaryMap(boundaries);
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
        this.store.addNode(new AtlasPrimitiveLocationItem(8,
                Location.forString("37.328076,-122.031869"), Maps.stringMap()));
        this.store.addNode(new AtlasPrimitiveLocationItem(9,
                new Segment(Location.TEST_6, Location.TEST_7).middle(),
                Maps.stringMap("tag_key", "tag_value")));

        // Add Edges
        this.store.addEdge(new AtlasPrimitiveLineItem(3,
                new PolyLine(Location.CROSSING_85_280, Location.forString("37.328076,-122.031869"),
                        Location.TEST_7),
                Maps.stringMap(HighwayTag.KEY, HighwayTag.MOTORWAY.name().toLowerCase())));
        this.store.addEdge(new AtlasPrimitiveLineItem(7,
                new PolyLine(Location.CROSSING_85_280, Location.CROSSING_85_17),
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

    @Test
    public void testEmptyRelations()
    {
        final OsmosisReaderMock osmosis = new OsmosisReaderMock(this.store);
        final OsmPbfLoader osmPbfLoader = new OsmPbfLoader(() -> osmosis, MultiPolygon.MAXIMUM,
                AtlasLoadingOption.createOptionWithAllEnabled(this.countryBoundariesAll)
                        .setAdditionalCountryCodes(COUNTRY_1_NAME));
        final Atlas atlas = osmPbfLoader.read();
        logger.info("{}", atlas);
        Assert.assertEquals(1, atlas.numberOfLines());
        Assert.assertEquals(1, atlas.numberOfRelations());
        final Relation relation = atlas.relations().iterator().next();
        Assert.assertEquals(1, relation.members().size());
        Assert.assertEquals(2, atlas.numberOfEdges());
    }

    @Test
    public void testNoFilter()
    {
        final OsmosisReaderMock osmosis = new OsmosisReaderMock(this.store);
        final OsmPbfLoader osmPbfLoader = new OsmPbfLoader(() -> osmosis, MultiPolygon.MAXIMUM,
                AtlasLoadingOption.withNoFilter());
        final Atlas atlas = osmPbfLoader.read();
        logger.info("{}", atlas);
        Assert.assertEquals(3, atlas.numberOfLines());
    }

    @Test
    public void testOutsideWayBoundaryNodes()
    {
        final OsmosisReaderMock osmosis = new OsmosisReaderMock(this.store);
        final OsmPbfLoader osmPbfLoader = new OsmPbfLoader(() -> osmosis,
                this.countryBoundaries1.countryBoundary(COUNTRY_1_NAME).iterator().next()
                        .getBoundary(),
                AtlasLoadingOption.createOptionWithAllEnabled(this.countryBoundaries1)
                        .setAdditionalCountryCodes(COUNTRY_1_NAME));
        final Atlas atlas = osmPbfLoader.read();
        logger.info("{}", atlas);
        final Edge edge = atlas.edgesIntersecting(Location.CROSSING_85_17.bounds()).iterator()
                .next();
        final Node node = edge.end();
        Assert.assertNotNull(node.tag(SyntheticBoundaryNodeTag.KEY));
        Assert.assertEquals(1, atlas.numberOfPoints());
    }
}
