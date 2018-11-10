package org.openstreetmap.atlas.utilities.vectortiles;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

/**
 * Basic unit tests for MinimumZoom. The main thing here is that we want to make sure the object
 * instantiates without any exceptions being thrown. If your minimum-zooms.json is bogus, you'll get
 * an exception, and then these tests will fail.
 *
 * @author hallahan
 */
public class MinimumZoomTest
{

    @Test
    public void testLoadingMinimumZoomConfig()
    {
        final MinimumZoom minimumZoom = MinimumZoom.INSTANCE;
        Assert.assertNotNull(minimumZoom);
    }

    @Test
    public void testGettingAMinimumZoomForTags()
    {
        final Map<String, String> tags = new HashMap<>();
        tags.put("weird", "tag");
        final int zoom = MinimumZoom.INSTANCE.get(tags);
        Assert.assertEquals(14, zoom);
    }
}
