package org.openstreetmap.atlas.geography.atlas.delta;

import java.util.Map;
import java.util.SortedSet;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.Polygon;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.builder.RelationBean;
import org.openstreetmap.atlas.geography.atlas.delta.Diff.DiffType;
import org.openstreetmap.atlas.geography.atlas.items.ItemType;
import org.openstreetmap.atlas.geography.atlas.packed.PackedAtlasBuilder;
import org.openstreetmap.atlas.utilities.random.RandomTagsSupplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author matthieun
 */
public class AtlasDeltaRelationTest
{
    private static final Logger logger = LoggerFactory.getLogger(AtlasDeltaRelationTest.class);

    @Test
    public void testDifferentMemberOrder()
    {
        final PackedAtlasBuilder baseBuilder = new PackedAtlasBuilder();
        final PackedAtlasBuilder alterBuilder = new PackedAtlasBuilder();
        final Map<String, String> tags = RandomTagsSupplier.randomTags(5);
        baseBuilder.addArea(1, Polygon.SILICON_VALLEY, tags);
        alterBuilder.addArea(1, Polygon.SILICON_VALLEY, tags);
        baseBuilder.addPoint(9, Location.TEST_2, tags);
        alterBuilder.addPoint(9, Location.TEST_2, tags);

        final RelationBean baseRelationBean = new RelationBean();
        baseRelationBean.addItem(1L, "inner", ItemType.AREA);
        baseRelationBean.addItem(9L, "outer", ItemType.POINT);

        final RelationBean alterRelationBean = new RelationBean();
        // Different order
        alterRelationBean.addItem(9L, "outer", ItemType.POINT);
        alterRelationBean.addItem(1L, "inner", ItemType.AREA);

        baseBuilder.addRelation(5, 5, baseRelationBean, tags);
        alterBuilder.addRelation(5, 5, alterRelationBean, tags);

        final Atlas base = baseBuilder.get();
        final Atlas alter = alterBuilder.get();

        final SortedSet<Diff> diffs = new AtlasDelta(base, alter).generate().getDifferences();

        // The relations members are now ordered by member identifier. So this should always return
        // 0 differences.
        Assert.assertEquals(0, diffs.size());
        logger.debug("testDifferentMemberOrder(): {}", Diff.toString(diffs));

        boolean foundRelation = false;
        for (final Diff diff : diffs)
        {
            Assert.assertEquals(DiffType.CHANGED, diff.getDiffType());
            if (diff.getIdentifier() == 5)
            {
                foundRelation = true;
            }
        }
        if (/* ! */foundRelation)
        {
            Assert.fail("Did not find the changed relation");
        }
    }

    @Test
    public void testDifferentMembersRoles()
    {
        final PackedAtlasBuilder baseBuilder = new PackedAtlasBuilder();
        final PackedAtlasBuilder alterBuilder = new PackedAtlasBuilder();
        final Map<String, String> tags = RandomTagsSupplier.randomTags(5);
        baseBuilder.addArea(1, Polygon.SILICON_VALLEY, tags);
        alterBuilder.addArea(1, Polygon.SILICON_VALLEY, tags);
        baseBuilder.addPoint(9, Location.TEST_2, tags);
        alterBuilder.addPoint(9, Location.TEST_2, tags);

        final RelationBean baseRelationBean = new RelationBean();
        baseRelationBean.addItem(1L, "inner", ItemType.AREA);
        baseRelationBean.addItem(9L, "outer", ItemType.POINT);

        final RelationBean alterRelationBean = new RelationBean();
        alterRelationBean.addItem(1L, "inner", ItemType.AREA);
        // Different role
        alterRelationBean.addItem(9L, "other outer", ItemType.POINT);

        baseBuilder.addRelation(5, 5, baseRelationBean, tags);
        alterBuilder.addRelation(5, 5, alterRelationBean, tags);

        final Atlas base = baseBuilder.get();
        final Atlas alter = alterBuilder.get();

        final SortedSet<Diff> diffs = new AtlasDelta(base, alter).generate().getDifferences();

        Assert.assertEquals(2, diffs.size());
        logger.debug("testDifferentMembersRoles(): {}", Diff.toString(diffs));
        logger.debug("testDifferentMembersRoles(): {}", Diff.toDiffViewFriendlyString(diffs));

        boolean foundRelation = false;
        for (final Diff diff : diffs)
        {
            if (diff.getIdentifier() == 5)
            {
                Assert.assertEquals(DiffType.CHANGED, diff.getDiffType());
                foundRelation = true;
            }
        }
        if (!foundRelation)
        {
            Assert.fail("Did not find the changed relation");
        }
    }
}
