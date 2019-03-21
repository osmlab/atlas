package org.openstreetmap.atlas.geography.atlas.change;

import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.complete.CompleteEdge;
import org.openstreetmap.atlas.geography.atlas.complete.CompleteEntity;
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.utilities.collections.Maps;
import org.openstreetmap.atlas.utilities.collections.Sets;

/**
 * @author matthieun
 */
public class AtlasChangeGeneratorTest
{
    @Rule
    public final AtlasChangeGeneratorTestRule rule = new AtlasChangeGeneratorTestRule();

    @Test
    public void testExpandNode()
    {
        final Atlas source = this.rule.getNodeBoundsExpansionAtlas();

        final Set<FeatureChange> result = new HashSet<>();
        final String key = "changed";
        final String value = "yes";
        for (final AtlasEntity entity : source)
        {
            final CompleteEntity completeEntity = ((CompleteEntity) CompleteEntity.from(entity))
                    .withAddedTag(key, value);
            result.add(FeatureChange.add((AtlasEntity) completeEntity));
        }
        // bonus!
        result.add(FeatureChange.add(new CompleteEdge(123L, PolyLine
                .wkt("LINESTRING (4.2194855 38.8231656, 4.2202479 38.8233871,4.219666 38.8235147)"),
                Maps.hashMap("highway", "primary"), 177633000000L, 177649000000L, Sets.hashSet())));
        final Set<FeatureChange> changes = AtlasChangeGenerator.expandNodeBounds(source, result);
        for (final FeatureChange featureChange : changes)
        {
            if (featureChange.getIdentifier() == 177633000000L)
            {
                Assert.assertEquals(
                        "POLYGON ((4.2177433 38.8228217, 4.2177433 38.8235147, 4.2202479 38.8235147,"
                                + " 4.2202479 38.8228217, 4.2177433 38.8228217))",
                        featureChange.bounds().toWkt());
            }
        }
    }
}
