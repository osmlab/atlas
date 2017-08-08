package org.openstreetmap.atlas.geography.atlas.builder.text;

import org.junit.Test;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.AtlasIntegrationTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author matthieun
 */
public class TextAtlasBuilderIntegrationTest
{
    private static final Logger logger = LoggerFactory
            .getLogger(TextAtlasBuilderIntegrationTest.class);

    @Test
    public void testLoad()
    {
        final Atlas atlas = AtlasIntegrationTest.loadCuba();
        logger.info("{}", atlas.metaData());
    }
}
