package org.openstreetmap.atlas.geography.atlas.delta;

import java.util.Map;
import java.util.SortedSet;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.PolyLine;
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
        baseBuilder.addNode(2, Location.COLOSSEUM, tags);
        alterBuilder.addNode(2, Location.COLOSSEUM, tags);
        baseBuilder.addNode(3, Location.EIFFEL_TOWER, tags);
        alterBuilder.addNode(3, Location.EIFFEL_TOWER, tags);
        baseBuilder.addEdge(4, new PolyLine(Location.COLOSSEUM, Location.EIFFEL_TOWER), tags);
        alterBuilder.addEdge(4, new PolyLine(Location.COLOSSEUM, Location.EIFFEL_TOWER), tags);

        final RelationBean baseRelationBean = new RelationBean();
        baseRelationBean.addItem(1L, "inner", ItemType.AREA);
        baseRelationBean.addItem(2L, "node1", ItemType.NODE);
        baseRelationBean.addItem(3L, "node2", ItemType.NODE);
        baseRelationBean.addItem(4L, "someEdge", ItemType.EDGE);

        baseBuilder.addRelation(5, 5, baseRelationBean, tags);

        final Atlas base = baseBuilder.get();
        final Atlas alter = alterBuilder.get();

        final SortedSet<Diff> diffs = new AtlasDelta(base, alter).generate().getDifferences();
        logger.debug("testDifferentRelationsHumanFriendly(): {}",
                Diff.toDiffViewFriendlyString(diffs));

        // Diff size should be 5:
        // 1. The Area with ID 1 reports different parent relations set across atlases
        // 2. The Node with ID 2 reports different parent relations set across atlases
        // 4. The Node with ID 3 reports different parent relations set across atlases
        // 5. The Edge with ID 4 reports different parent relations set across atlases
        // 5. The Relation with ID 5 is not present in the alter atlas
        Assert.assertEquals(5, diffs.size());
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
