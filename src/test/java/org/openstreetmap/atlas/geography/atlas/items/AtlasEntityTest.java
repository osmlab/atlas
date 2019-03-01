package org.openstreetmap.atlas.geography.atlas.items;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author Yazad Khambata
 */
public class AtlasEntityTest {

    @Rule
    public final AreaEntityTestRule rule = new AreaEntityTestRule();

    @Test
    public void getName() {
        Assert.assertEquals("abc", rule.getAtlas().node(1).getName().get());
        Assert.assertTrue(!rule.getAtlas().node(2).getName().isPresent());
    }
}