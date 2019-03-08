package org.openstreetmap.atlas.geography.atlas.command;

import java.util.Collections;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.AtlasResourceLoader;
import org.openstreetmap.atlas.streaming.resource.File;

/**
 * Unit tests for {@link OsmPbfToAtlasSubCommand}.
 *
 * @author bbreithaupt
 */
public class OsmPbfToAtlasSubCommandTest
{
    private static String PBF = OsmPbfToAtlasSubCommandTest.class
            .getResource("world_islands.osm.pbf").getPath();
    private static String COUNTRY_BOUNDARY_MAP_TEXT = OsmPbfToAtlasSubCommandTest.class
            .getResource("continent_map.txt").getPath();
    private static String EDGE_FILTER = OsmPbfToAtlasSubCommandTest.class
            .getResource("atlas-edge.json").getPath();
    private static String WAY_SECTIONING_FILTER = OsmPbfToAtlasSubCommandTest.class
            .getResource("atlas-way-section.json").getPath();
    private static String NODE_FILTER = OsmPbfToAtlasSubCommandTest.class
            .getResource("osm-pbf-node.json").getPath();
    private static String RELATION_FILTER = OsmPbfToAtlasSubCommandTest.class
            .getResource("osm-pbf-relation.json").getPath();
    private static String WAY_FILTER = OsmPbfToAtlasSubCommandTest.class
            .getResource("osm-pbf-way.json").getPath();

    private static String ATLAS_NAME = "test_temp.atlas";

    @Test
    public void testDefaultConversion()
    {
        final File temp = File.temporaryFolder();

        try
        {
            // Run OsmPbfToAtlasSubCommand
            final String[] args = { "pbf-to-atlas", String.format("-pbf=%s", PBF),
                    String.format("-output=%s/%s", temp, ATLAS_NAME) };
            new AtlasReader(args).runWithoutQuitting(args);

            // Load new atlas
            final Atlas atlas = new AtlasResourceLoader()
                    .load(new File(String.format("%s/%s", temp, ATLAS_NAME)));

            // Test for way sectioning
            Assert.assertNotNull(atlas.edge(87185620000002L));
            // Test for country map
            Assert.assertTrue(atlas.edge(87185039000000L).containsValue("iso_country_code",
                    Collections.singleton("UNK")));
            // Inverse test for country codes
            Assert.assertNotNull(atlas.point(1013787604000000L));
            // Test default edge filter
            Assert.assertNotNull(atlas.edge(87186304000001L));
            // Test default filter
            Assert.assertNotNull(atlas.point(3698322053000000L));
            // Test default relation filter
            Assert.assertNotNull(atlas.relation(2693943000000L));
            // Test default way filter
            Assert.assertNotNull(atlas.area(167578604000000L));
            // Test default way section filter
            Assert.assertNull(atlas.edge(87186195000018L));
        }
        finally
        {
            temp.deleteRecursively();
        }
    }

    @Test
    public void testFiltersTextMapCountryCodesConversion()
    {
        final File temp = File.temporaryFolder();

        try
        {
            // Run OsmPbfToAtlasSubCommand
            final String[] args = { "pbf-to-atlas", String.format("-pbf=%s", PBF),
                    String.format("-output=%s/%s", temp, ATLAS_NAME),
                    String.format("-country-boundary-map=%s", COUNTRY_BOUNDARY_MAP_TEXT),
                    "-country-codes=NAM,EUR", String.format("-edge-filter=%s", EDGE_FILTER),
                    String.format("-node-filter=%s", NODE_FILTER),
                    String.format("-relation-filter=%s", RELATION_FILTER),
                    String.format("-way-filter=%s", WAY_FILTER),
                    String.format("-way-section-filter=%s", WAY_SECTIONING_FILTER), };
            new AtlasReader(args).runWithoutQuitting(args);

            // Load new atlas
            final Atlas atlas = new AtlasResourceLoader()
                    .load(new File(String.format("%s/%s", temp, ATLAS_NAME)));

            // Test for way sectioning
            Assert.assertNotNull(atlas.edge(87185620000002L));
            // Test for country map
            Assert.assertTrue(atlas.edge(87185039000000L).containsValue("iso_country_code",
                    Collections.singleton("NAM")));
            // Test for country codes
            Assert.assertNull(atlas.point(1013787604000000L));
            // Test edge filter
            Assert.assertNull(atlas.edge(87186304000001L));
            // Test node filter
            Assert.assertNull(atlas.point(3698322053000000L));
            // Test relation filter
            Assert.assertNull(atlas.relation(2693943000000L));
            // Test way filter
            Assert.assertNotNull(atlas.area(167578604000000L));
            // Test way section filter
            Assert.assertNotNull(atlas.edge(87186195000018L));
        }
        finally
        {
            temp.deleteRecursively();
        }
    }

    @Test
    public void testNoSlicingNoRelationsConversion()
    {
        final File temp = File.temporaryFolder();

        try
        {
            // Run OsmPbfToAtlasSubCommand
            final String[] args = { "pbf-to-atlas", String.format("-pbf=%s", PBF),
                    String.format("-output=%s/%s", temp, ATLAS_NAME),
                    String.format("-country-boundary-map=%s", COUNTRY_BOUNDARY_MAP_TEXT),
                    "-country-codes=NAM,EUR", "-country-slicing=false", "-load-relations=false" };
            new AtlasReader(args).runWithoutQuitting(args);

            // Load new atlas
            final Atlas atlas = new AtlasResourceLoader()
                    .load(new File(String.format("%s/%s", temp, ATLAS_NAME)));
            // Test for country slicing
            Assert.assertFalse(atlas.edge(87185039000000L)
                    .containsKey(Collections.singleton("iso_country_code")));
            // Test no load relation
            Assert.assertFalse(atlas.relations().iterator().hasNext());
        }
        finally
        {
            temp.deleteRecursively();
        }
    }

    @Test
    public void testNoWaysConversion()
    {
        final File temp = File.temporaryFolder();

        try
        {
            // Run OsmPbfToAtlasSubCommand
            final String[] args = { "pbf-to-atlas", String.format("-pbf=%s", PBF),
                    String.format("-output=%s/%s", temp, ATLAS_NAME), "-load-ways=false" };
            new AtlasReader(args).runWithoutQuitting(args);

            // Load new atlas
            final Atlas atlas = new AtlasResourceLoader()
                    .load(new File(String.format("%s/%s", temp, ATLAS_NAME)));

            // Test no load ways
            Assert.assertFalse(atlas.areas().iterator().hasNext());
            Assert.assertFalse(atlas.edges().iterator().hasNext());
            Assert.assertFalse(atlas.lines().iterator().hasNext());
        }
        finally
        {
            temp.deleteRecursively();
        }
    }
}
