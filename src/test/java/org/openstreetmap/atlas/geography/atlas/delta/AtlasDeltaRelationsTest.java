package org.openstreetmap.atlas.geography.atlas.delta;

import java.util.Map;
import java.util.SortedSet;

import org.junit.Assert;
import org.junit.Test;
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
public class AtlasDeltaRelationsTest
{
    private static final Logger logger = LoggerFactory.getLogger(AtlasDeltaRelationsTest.class);

    @Test
    public void testDifferentRelations()
    {
        final PackedAtlasBuilder baseBuilder = new PackedAtlasBuilder();
        final PackedAtlasBuilder alterBuilder = new PackedAtlasBuilder();
        final Map<String, String> tags = RandomTagsSupplier.randomTags(5);
        baseBuilder.addArea(1, Polygon.SILICON_VALLEY, tags);
        alterBuilder.addArea(1, Polygon.SILICON_VALLEY, tags);

        final RelationBean baseRelationBean = new RelationBean();
        baseRelationBean.addItem(1L, "inner", ItemType.AREA);

        final RelationBean alterRelationBean = new RelationBean();
        alterRelationBean.addItem(1L, "outer", ItemType.AREA);

        baseBuilder.addRelation(5, 5, baseRelationBean, tags);
        alterBuilder.addRelation(5, 5, alterRelationBean, tags);

        final Atlas base = baseBuilder.get();
        final Atlas alter = alterBuilder.get();

        final SortedSet<Diff> diffs = new AtlasDelta(base, alter).generate().getDifferences();

        Assert.assertEquals(2, diffs.size());
        logger.debug("testDifferentRelations(): {}", Diff.toString(diffs));
        logger.debug("testDifferentRelationsHumanFriendly(): {}",
                Diff.toDiffViewFriendlyString(diffs));

        boolean foundRelation = false;
        for (final Diff diff : diffs)
        {
            Assert.assertEquals(DiffType.CHANGED, diff.getDiffType());
            if (diff.getIdentifier() == 5)
            {
                foundRelation = true;
            }
        }
        if (!foundRelation)
        {
            Assert.fail("Did not find the changed relation");
        }
    }

    @Test
    public void testReportedParentRelations()
    {
        final PackedAtlasBuilder baseBuilder = new PackedAtlasBuilder();
        final PackedAtlasBuilder alterBuilder = new PackedAtlasBuilder();
        final Map<String, String> tags = RandomTagsSupplier.randomTags(5);
        baseBuilder.addArea(1, Polygon.SILICON_VALLEY, tags);
        alterBuilder.addArea(1, Polygon.SILICON_VALLEY, tags);

        final RelationBean baseRelationBean = new RelationBean();
        baseRelationBean.addItem(1L, "inner", ItemType.AREA);

        baseBuilder.addRelation(5, 5, baseRelationBean, tags);

        final Atlas base = baseBuilder.get();
        final Atlas alter = alterBuilder.get();

        final SortedSet<Diff> diffs = new AtlasDelta(base, alter).generate().getDifferences();
        logger.info("testDifferentRelationsHumanFriendly(): {}",
                Diff.toDiffViewFriendlyString(diffs));

        // Diff size should be 2:
        // 1. The Area reports different parent relations across atlases
        // 2. The Relation with ID 5 is not present in the alter atlas
        Assert.assertEquals(2, diffs.size());
    }

    @Test
    public void testSameRelations()
    {
        final PackedAtlasBuilder baseBuilder = new PackedAtlasBuilder();
        final PackedAtlasBuilder alterBuilder = new PackedAtlasBuilder();
        final Map<String, String> tags = RandomTagsSupplier.randomTags(5);
        baseBuilder.addArea(1, Polygon.SILICON_VALLEY, tags);
        alterBuilder.addArea(1, Polygon.SILICON_VALLEY, tags);

        final RelationBean baseRelationBean = new RelationBean();
        baseRelationBean.addItem(1L, "inner", ItemType.AREA);

        final RelationBean alterRelationBean = new RelationBean();
        alterRelationBean.addItem(1L, "inner", ItemType.AREA);

        baseBuilder.addRelation(5, 5, baseRelationBean, tags);
        alterBuilder.addRelation(5, 5, alterRelationBean, tags);

        final Atlas base = baseBuilder.get();
        final Atlas alter = alterBuilder.get();

        final SortedSet<Diff> diffs = new AtlasDelta(base, alter).generate().getDifferences();

        Assert.assertEquals(0, diffs.size());
    }
}
