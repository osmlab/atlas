package org.openstreetmap.atlas.geography.atlas.packed;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.delta.AtlasDelta;

/**
 * @author matthieun
 */
public class PackedAtlasClonerTest
{
    private static String TEST_KEY_1 = "TEST_KEY_1";
    private static String TEST_KEY_2 = "TEST_KEY_2";
    private static String TEST_KEY_3 = "TEST_KEY_3";
    private static String TEST_KEY_4 = "TEST_KEY_4";

    private static String TEST_VALUE_1 = "TEST_VALUE_1";
    private static String TEST_VALUE_2 = "TEST_VALUE_2";
    private static String TEST_VALUE_3 = "TEST_VALUE_3";
    private static String TEST_VALUE_4 = "TEST_VALUE_4";

    @Test
    public void additionalMetaDataTagsTest()
    {
        final Map<String, String> additionalTags = new HashMap<>();
        additionalTags.put(TEST_KEY_1, TEST_VALUE_1);
        additionalTags.put(TEST_KEY_2, TEST_VALUE_2);
        additionalTags.put(TEST_KEY_3, TEST_VALUE_3);
        additionalTags.put(TEST_KEY_4, TEST_VALUE_4);

        final Atlas atlas = RandomPackedAtlasBuilder.generate(50, 0);

        final PackedAtlasCloner cloner = new PackedAtlasCloner()
                .withAdditionalMetaDataTags(additionalTags);
        final Atlas copy = cloner.cloneFrom(atlas);

        final Map<String, String> initialTags = atlas.metaData().getTags();
        final Map<String, String> finalTags = copy.metaData().getTags();

        Assert.assertEquals("Unexpected number of tags found", initialTags.size() + 4,
                finalTags.size());

        // Validate tags that were found in the old atlas
        initialTags.forEach((key, value) ->
        {
            Assert.assertEquals(value, finalTags.get(key));
        });

        // Validate additional tags added
        Assert.assertEquals(TEST_VALUE_1, finalTags.get(TEST_KEY_1));
        Assert.assertEquals(TEST_VALUE_2, finalTags.get(TEST_KEY_2));
        Assert.assertEquals(TEST_VALUE_3, finalTags.get(TEST_KEY_3));
        Assert.assertEquals(TEST_VALUE_4, finalTags.get(TEST_KEY_4));
    }

    @Test
    public void cloneTest()
    {
        for (int i = 0; i < 5; i++)
        {
            final Atlas atlas = RandomPackedAtlasBuilder.generate(50, 0);
            final PackedAtlasCloner cloner = new PackedAtlasCloner();
            final Atlas copy = cloner.cloneFrom(atlas);
            Assert.assertTrue(new AtlasDelta(atlas, copy).generate().getDifferences().isEmpty());
        }
    }
}
