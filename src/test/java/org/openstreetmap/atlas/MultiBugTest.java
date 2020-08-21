package org.openstreetmap.atlas;

import java.nio.file.FileSystems;

import org.junit.Test;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.AtlasResourceLoader;
import org.openstreetmap.atlas.geography.atlas.multi.MultiAtlas;
import org.openstreetmap.atlas.streaming.resource.File;

/**
 * @author lcram
 */
public class MultiBugTest
{
    @Test
    public void test()
    {
        final File smallAtlasFile = new File(
                "/Users/lucascram/Desktop/multi-bug/FJI_8-255-140.atlas", FileSystems.getDefault());
        final File ferryAtlasFile = new File("/Users/lucascram/Desktop/multi-bug/FJI_ferry.atlas",
                FileSystems.getDefault());

        final Atlas smallAtlas = new AtlasResourceLoader().load(smallAtlasFile);
        final Atlas ferryAtlas = new AtlasResourceLoader().load(ferryAtlasFile);
        final MultiAtlas multiAtlas = new MultiAtlas(ferryAtlas);

        final long id1 = smallAtlas.edge(-40539392001000L) != null
                ? smallAtlas.edge(-40539392001000L).start().getIdentifier()
                : -1L;
        final long id2 = ferryAtlas.edge(-40539392001000L) != null
                ? ferryAtlas.edge(-40539392001000L).start().getIdentifier()
                : -1L;
        final long id3 = multiAtlas.edge(-40539392001000L) != null
                ? multiAtlas.edge(-40539392001000L).start().getIdentifier()
                : -1L;

        System.out.println(id1 + ", " + id2 + ", " + id3);
    }
}
