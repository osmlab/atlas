package org.openstreetmap.atlas.geography.atlas.builder;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.streaming.resource.InputStreamResource;
import org.openstreetmap.atlas.streaming.resource.StringResource;
import org.openstreetmap.atlas.utilities.collections.Iterables;

/**
 * @author matthieun
 */
public class GeoJsonAtlasBuilderTest
{
    @Test
    public void testCreation()
    {
        final StringResource resource = new StringResource(new InputStreamResource(
                GeoJsonAtlasBuilderTest.class.getResourceAsStream("overpass-turbo.geojson")));
        final Atlas created = new GeoJsonAtlasBuilder().create(resource);
        System.out.println(created);
        Assert.assertEquals(50, Iterables.size(created.edges()));
    }
}
