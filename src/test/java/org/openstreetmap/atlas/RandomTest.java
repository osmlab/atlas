package org.openstreetmap.atlas;

import org.junit.Test;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.AtlasResourceLoader;
import org.openstreetmap.atlas.geography.atlas.items.Relation;
import org.openstreetmap.atlas.geography.atlas.raw.slicing.RawAtlasCountrySlicer;
import org.openstreetmap.atlas.geography.boundary.CountryBoundaryMap;
import org.openstreetmap.atlas.geography.boundary.CountryBoundaryMapArchiver;
import org.openstreetmap.atlas.streaming.resource.File;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author lcram
 */
public class RandomTest
{
    private static final Logger logger = LoggerFactory.getLogger(RandomTest.class);

    @Test
    public void test()
    {
        final Atlas rawAtlas = new AtlasResourceLoader().load(new File(""));

        final CountryBoundaryMap map = new CountryBoundaryMapArchiver().read(new File(""));

        final Atlas slicedAtlas = new RawAtlasCountrySlicer("RUS", map).slice(rawAtlas);

        for (final Relation relation : slicedAtlas.relations())
        {
            if ("N/A".equals(relation.getTag("iso_country_code").get()))
            {
                logger.warn("RELATION {}", relation);
            }
        }
    }
}
