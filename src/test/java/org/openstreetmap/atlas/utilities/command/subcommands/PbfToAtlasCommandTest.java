package org.openstreetmap.atlas.utilities.command.subcommands;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.AtlasResourceLoader;
import org.openstreetmap.atlas.streaming.resource.File;
import org.openstreetmap.atlas.streaming.resource.InputStreamResource;
import org.openstreetmap.atlas.streaming.resource.Resource;
import org.openstreetmap.atlas.streaming.resource.StringResource;
import org.openstreetmap.atlas.utilities.collections.StringList;
import org.openstreetmap.atlas.utilities.testing.OsmFileParser;
import org.openstreetmap.atlas.utilities.testing.OsmFileToPbf;

/**
 * @author matthieun
 */
public class PbfToAtlasCommandTest
{
    @Test
    public void testCommand()
    {
        final File folder = File.temporaryFolder();
        try
        {
            final Resource resource = new InputStreamResource(() -> PbfToAtlasCommandTest.class
                    .getResourceAsStream("testPbf2Atlas.josm.osm"));
            final File pbfFile = folder.child("PbfToAtlasCommandTest.pbf");
            final StringResource osmFile = new StringResource();
            new OsmFileParser().update(resource, osmFile);
            new OsmFileToPbf().update(osmFile, pbfFile);

            final StringList arguments = new StringList();
            arguments.add(pbfFile.getAbsolutePath());
            arguments.add("--output=" + folder.getAbsolutePath());
            arguments.add("--countryName=FRA");

            final String[] args = new String[arguments.size()];
            for (int index = 0; index < arguments.size(); index++)
            {
                args[index] = arguments.get(index);
            }
            new PbfToAtlasCommand().runSubcommand(args);
            final Atlas atlas = new AtlasResourceLoader()
                    .load(folder.child("FRA_PbfToAtlasCommandTest.atlas"));
            System.out.println("Atlas result: " + atlas);
            Assert.assertEquals(5, atlas.numberOfNodes());
            Assert.assertEquals(4, atlas.numberOfEdges());
            Assert.assertEquals(1, atlas.numberOfAreas());
            Assert.assertEquals(1, atlas.numberOfLines());
            Assert.assertEquals(1, atlas.numberOfRelations());
        }
        finally
        {
            folder.deleteRecursively();
        }
    }
}
