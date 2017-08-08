package org.openstreetmap.atlas.geography.atlas.builder.text;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.geography.Polygon;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.packed.PackedAtlasBuilder;
import org.openstreetmap.atlas.geography.atlas.packed.RandomPackedAtlasBuilder;
import org.openstreetmap.atlas.streaming.resource.StringResource;
import org.openstreetmap.atlas.streaming.resource.WritableResource;
import org.openstreetmap.atlas.utilities.collections.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author matthieun
 */
public class TextAtlasBuilderTest
{
    private static final Logger logger = LoggerFactory.getLogger(TextAtlasBuilderTest.class);

    private final TextAtlasBuilder textAtlasBuilder = new TextAtlasBuilder();

    @Test
    public void testDataIntegrity()
    {
        final Atlas atlas = RandomPackedAtlasBuilder.generate(2, 0);
        final StringResource resource = new StringResource();
        this.textAtlasBuilder.write(atlas, resource);
        resource.lines().forEach(System.out::println);
        final Atlas read = this.textAtlasBuilder.read(resource);
        Assert.assertEquals(atlas, read);
    }

    @Test
    public void testFunkyTags()
    {
        final PackedAtlasBuilder builder = new PackedAtlasBuilder();
        builder.addArea(1, Polygon.SILICON_VALLEY,
                Maps.hashMap("key", "line1" + System.lineSeparator() + "line2"));
        builder.addArea(2, Polygon.SILICON_VALLEY, Maps.hashMap("key && key", "value"));
        final Atlas atlas = builder.get();

        final WritableResource resource = new StringResource();
        final TextAtlasBuilder textAtlasBuilder = new TextAtlasBuilder();
        textAtlasBuilder.write(atlas, resource);

        final Atlas read = textAtlasBuilder.read(resource);
        logger.info("{}", read.size());
        Assert.assertEquals(2, read.numberOfAreas());
    }
}
