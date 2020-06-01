package org.openstreetmap.atlas.utilities.command.subcommands;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.streaming.resource.File;
import org.openstreetmap.atlas.streaming.resource.InputStreamResource;
import org.openstreetmap.atlas.streaming.resource.Resource;
import org.openstreetmap.atlas.streaming.resource.StringResource;
import org.openstreetmap.atlas.utilities.collections.StringList;
import org.openstreetmap.atlas.utilities.testing.OsmFileParser;
import org.openstreetmap.atlas.utilities.testing.OsmFileToPbf;

/**
 * @author lcram
 */
public class AtlasSearchCommandTest
{
    @Test
    public void testIdSearch()
    {
        final File folder = File.temporaryFolder();
        try
        {
            final StringList arguments = new StringList();
            arguments.add(createTestAtlas(folder));
            arguments.add("--id=103436000000");

            final String[] args = new String[arguments.size()];
            for (int index = 0; index < arguments.size(); index++)
            {
                args[index] = arguments.get(index);
            }
            final AtlasSearchCommand command = new AtlasSearchCommand().withUnitTestMode();
            command.runSubcommand(args);
            Assert.assertEquals(1, command.getMatchedEntities().size());
            Assert.assertEquals(103436000000L, command.getMatchedEntities().get(0).getIdentifier());
        }
        finally
        {
            folder.deleteRecursively();
        }
    }

    @Test
    public void testInOutEdgeSearch()
    {
        final File folder = File.temporaryFolder();
        try
        {
            final StringList arguments = new StringList();
            arguments.add(createTestAtlas(folder));
            arguments.add("--inEdge=103423000001");
            arguments.add("--outEdge=103423000002");

            final String[] args = new String[arguments.size()];
            for (int index = 0; index < arguments.size(); index++)
            {
                args[index] = arguments.get(index);
            }
            final AtlasSearchCommand command = new AtlasSearchCommand().withUnitTestMode();
            command.runSubcommand(args);
            Assert.assertEquals(1, command.getMatchedEntities().size());
            final List<Long> matched = command.getMatchedEntities().stream()
                    .map(AtlasEntity::getIdentifier).sorted().collect(Collectors.toList());
            Assert.assertEquals(new Long(103422000000L), matched.get(0));
        }
        finally
        {
            folder.deleteRecursively();
        }
    }

    @Test
    public void testStartEndNodeSearch()
    {
        final File folder = File.temporaryFolder();
        try
        {
            final StringList arguments = new StringList();
            arguments.add(createTestAtlas(folder));
            arguments.add("--startNode=103422000000");
            arguments.add("--endNode=103426000000");

            final String[] args = new String[arguments.size()];
            for (int index = 0; index < arguments.size(); index++)
            {
                args[index] = arguments.get(index);
            }
            final AtlasSearchCommand command = new AtlasSearchCommand().withUnitTestMode();
            command.runSubcommand(args);
            Assert.assertEquals(1, command.getMatchedEntities().size());
            final List<Long> matched = command.getMatchedEntities().stream()
                    .map(AtlasEntity::getIdentifier).sorted().collect(Collectors.toList());
            Assert.assertEquals(new Long(103423000002L), matched.get(0));
        }
        finally
        {
            folder.deleteRecursively();
        }
    }

    @Test
    public void testTagSearch()
    {
        final File folder = File.temporaryFolder();
        try
        {
            final StringList arguments = new StringList();
            arguments.add(createTestAtlas(folder));
            arguments.add("--taggableFilter=highway->motorway");

            final String[] args = new String[arguments.size()];
            for (int index = 0; index < arguments.size(); index++)
            {
                args[index] = arguments.get(index);
            }
            final AtlasSearchCommand command = new AtlasSearchCommand().withUnitTestMode();
            command.runSubcommand(args);
            Assert.assertEquals(2, command.getMatchedEntities().size());
            final List<Long> matched = command.getMatchedEntities().stream()
                    .map(AtlasEntity::getIdentifier).sorted().collect(Collectors.toList());
            Assert.assertEquals(new Long(103423000001L), matched.get(0));
            Assert.assertEquals(new Long(103423000002L), matched.get(1));
        }
        finally
        {
            folder.deleteRecursively();
        }
    }

    private String createTestAtlas(final File folder)
    {
        final Resource resource = new InputStreamResource(
                () -> PbfToAtlasCommandTest.class.getResourceAsStream("testPbf2Atlas.josm.osm"));
        final File pbfFile = folder.child("AtlasSearchCommandTest.pbf");
        final StringResource osmFile = new StringResource();
        new OsmFileParser().update(resource, osmFile);
        new OsmFileToPbf().update(osmFile, pbfFile);

        final StringList arguments = new StringList();
        arguments.add(pbfFile.getAbsolutePathString());
        arguments.add("--output=" + folder.getAbsolutePathString());
        arguments.add("--countryName=FRA");

        final String[] args = new String[arguments.size()];
        for (int index = 0; index < arguments.size(); index++)
        {
            args[index] = arguments.get(index);
        }
        new PbfToAtlasCommand().runSubcommand(args);
        return folder.child("FRA_AtlasSearchCommandTest.atlas").getAbsolutePathString();
    }
}
