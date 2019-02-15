package org.openstreetmap.atlas.geography.atlas.change;

import org.junit.Rule;
import org.junit.Test;

/**
 * @author Yazad Khambata
 */
public class TagChangeTest {

    @Rule
    public TagChangeTestRule tagChangeTestRule = new TagChangeTestRule();

    @Test
    public void test() {
        System.out.println(tagChangeTestRule.getAtlas());
    }
}
