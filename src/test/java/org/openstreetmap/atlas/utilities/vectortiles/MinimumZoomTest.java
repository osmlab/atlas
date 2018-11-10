package org.openstreetmap.atlas.utilities.vectortiles;

import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class MinimumZoomTest {

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
