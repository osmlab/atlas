package org.openstreetmap.atlas.geography.atlas;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.utilities.collections.Iterables;

/**
 * @author matthieun
 */
public class BareAtlasTest
{
    @Rule
    public final BareAtlasTestRule rule = new BareAtlasTestRule();

    @Test
    public void testRelationsOrder()
    {
        final Atlas atlas = this.rule.getAtlas();
        final List<Long> expectedRelationIdentifiers = new ArrayList<>();
        expectedRelationIdentifiers.add(1L);
        expectedRelationIdentifiers.add(2L);
        expectedRelationIdentifiers.add(3L);
        expectedRelationIdentifiers.add(5L);
        expectedRelationIdentifiers.add(4L);
        Assert.assertEquals(expectedRelationIdentifiers,
                Iterables.stream(atlas.relationsLowerOrderFirst())
                        .map(relation -> relation.getIdentifier()).collectToList());
    }
}
