package org.openstreetmap.atlas.geography.atlas.multi;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.Polygon;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.AtlasResourceLoader;
import org.openstreetmap.atlas.geography.atlas.builder.RelationBean;
import org.openstreetmap.atlas.geography.atlas.items.ItemType;
import org.openstreetmap.atlas.geography.atlas.packed.PackedAtlas;
import org.openstreetmap.atlas.geography.atlas.packed.PackedAtlasBuilder;
import org.openstreetmap.atlas.streaming.resource.ByteArrayResource;
import org.openstreetmap.atlas.utilities.collections.Maps;

/**
 * Expose bugs with the way {@link MultiAtlas} calculates parent relations of its constituent
 * entities. These tests should pass now that all bugs are fixed.
 *
 * @see <a href="https://github.com/osmlab/atlas/pull/126">The PR that demonstrates and corrects
 *      these bugs</a>
 * @author lcram
 */
public class MissingMultiEntityRelationTest
{
    @Test
    public void testAreasForMissingRelation()
    {
        final List<Location> shapePoints = new ArrayList<>();
        final PackedAtlasBuilder builderWithRelation = new PackedAtlasBuilder();
        shapePoints.add(Location.forString("1,1"));
        shapePoints.add(Location.forString("2,2"));
        shapePoints.add(Location.forString("2,0"));
        builderWithRelation.addArea(1L, new Polygon(shapePoints),
                Maps.hashMap("someArea", "inTwoAtlases"));
        final RelationBean bean = new RelationBean();
        bean.addItem(1L,
                "Area appears in multiple atlases, but parent relation is only in one atlas",
                ItemType.AREA);
        builderWithRelation.addRelation(2, 2, bean, Maps.hashMap());

        shapePoints.clear();
        final PackedAtlasBuilder builderNoRelation = new PackedAtlasBuilder();
        shapePoints.add(Location.forString("1,1"));
        shapePoints.add(Location.forString("2,2"));
        shapePoints.add(Location.forString("2,0"));
        builderNoRelation.addArea(1L, new Polygon(shapePoints),
                Maps.hashMap("someArea", "inTwoAtlases"));

        final PackedAtlas atlasWithRelation = (PackedAtlas) builderWithRelation.get();
        final PackedAtlas atlasNoRelation = (PackedAtlas) builderNoRelation.get();

        // 1 MiB resources
        final ByteArrayResource resourceWithRelation = new ByteArrayResource(1024 * 1024 * 1);
        final ByteArrayResource resourceNoRelation = new ByteArrayResource(1024 * 1024 * 1);

        atlasWithRelation.save(resourceWithRelation);
        atlasNoRelation.save(resourceNoRelation);

        /*
         * Add the Atlas that is missing the relation second so that it overwrites the first one.
         * See the MultiAtlas.populateReferences() function to see this overwrite happen.
         */
        final Atlas multiAtlas = new AtlasResourceLoader().load(resourceWithRelation,
                resourceNoRelation);

        Assert.assertTrue(!multiAtlas.area(1L).relations().isEmpty());
    }

    @Test
    public void testEdgesForMissingRelation()
    {
        final List<Location> shapePoints = new ArrayList<>();
        final PackedAtlasBuilder builderWithRelation = new PackedAtlasBuilder();
        builderWithRelation.addNode(1L, Location.forString("1,1"),
                Maps.hashMap("someNode", "inTwoAtlases"));
        builderWithRelation.addNode(2L, Location.forString("2,2"),
                Maps.hashMap("someNode", "inTwoAtlases"));
        shapePoints.add(Location.forString("1,1"));
        shapePoints.add(Location.forString("2,2"));
        builderWithRelation.addEdge(1L, new PolyLine(shapePoints),
                Maps.hashMap("someEdge", "inTwoAtlases"));
        final RelationBean bean = new RelationBean();
        bean.addItem(1L,
                "Node appears in multiple atlases, but parent relation is only in one atlas",
                ItemType.NODE);
        bean.addItem(2L,
                "Node appears in multiple atlases, but parent relation is only in one atlas",
                ItemType.NODE);
        bean.addItem(1L,
                "Edge appears in multiple atlases, but parent relation is only in one atlas",
                ItemType.EDGE);
        builderWithRelation.addRelation(4, 4, bean, Maps.hashMap());

        shapePoints.clear();
        final PackedAtlasBuilder builderNoRelation = new PackedAtlasBuilder();
        builderNoRelation.addNode(1L, Location.forString("1,1"),
                Maps.hashMap("someNode", "inTwoAtlases"));
        builderNoRelation.addNode(2L, Location.forString("2,2"),
                Maps.hashMap("someNode", "inTwoAtlases"));
        shapePoints.add(Location.forString("1,1"));
        shapePoints.add(Location.forString("2,2"));
        builderNoRelation.addEdge(1L, new PolyLine(shapePoints),
                Maps.hashMap("someEdge", "inTwoAtlases"));

        final PackedAtlas atlasWithRelation = (PackedAtlas) builderWithRelation.get();
        final PackedAtlas atlasNoRelation = (PackedAtlas) builderNoRelation.get();

        // 1 MiB resources
        final ByteArrayResource resourceWithRelation = new ByteArrayResource(1024 * 1024 * 1);
        final ByteArrayResource resourceNoRelation = new ByteArrayResource(1024 * 1024 * 1);

        atlasWithRelation.save(resourceWithRelation);
        atlasNoRelation.save(resourceNoRelation);

        /*
         * Add the atlas that is missing the relation second, since the MultiAtlasBorderFixer bug is
         * caused by a MultiMapWithSet.put() call that overwrites a set. What it should now be doing
         * (since the bug is fixed) is performing a set union, not a set overwrite.
         */
        final Atlas multiAtlas = new AtlasResourceLoader().load(resourceWithRelation,
                resourceNoRelation);

        Assert.assertTrue(!multiAtlas.edge(1L).relations().isEmpty());
    }

    @Test
    public void testLinesForMissingRelation()
    {
        final List<Location> shapePoints = new ArrayList<>();
        final PackedAtlasBuilder builderWithRelation = new PackedAtlasBuilder();
        shapePoints.add(Location.forString("1,1"));
        shapePoints.add(Location.forString("2,2"));
        builderWithRelation.addLine(1L, new PolyLine(shapePoints),
                Maps.hashMap("someLine", "inTwoAtlases"));
        final RelationBean bean = new RelationBean();
        bean.addItem(1L,
                "Line appears in multiple atlases, but parent relation is only in one atlas",
                ItemType.LINE);
        builderWithRelation.addRelation(2, 2, bean, Maps.hashMap());

        shapePoints.clear();
        final PackedAtlasBuilder builderNoRelation = new PackedAtlasBuilder();
        shapePoints.add(Location.forString("1,1"));
        shapePoints.add(Location.forString("2,2"));
        builderNoRelation.addLine(1L, new PolyLine(shapePoints),
                Maps.hashMap("someLine", "inTwoAtlases"));

        final PackedAtlas atlasWithRelation = (PackedAtlas) builderWithRelation.get();
        final PackedAtlas atlasNoRelation = (PackedAtlas) builderNoRelation.get();

        // 1 MiB resources
        final ByteArrayResource resourceWithRelation = new ByteArrayResource(1024 * 1024 * 1);
        final ByteArrayResource resourceNoRelation = new ByteArrayResource(1024 * 1024 * 1);

        atlasWithRelation.save(resourceWithRelation);
        atlasNoRelation.save(resourceNoRelation);

        /*
         * Add the Atlas that is missing the relation second so that it overwrites the first one.
         * See the MultiAtlas.populateReferences() function to see this overwrite happen.
         */
        final Atlas multiAtlas = new AtlasResourceLoader().load(resourceWithRelation,
                resourceNoRelation);

        Assert.assertTrue(!multiAtlas.line(1L).relations().isEmpty());
    }

    @Test
    public void testNodesForMissingRelation()
    {
        final PackedAtlasBuilder builderWithRelation = new PackedAtlasBuilder();
        builderWithRelation.addNode(1L, Location.forString("1,1"),
                Maps.hashMap("someNode", "inTwoAtlases"));
        final RelationBean bean = new RelationBean();
        bean.addItem(1L,
                "Node appears in multiple atlases, but parent relation is only in one atlas",
                ItemType.NODE);
        builderWithRelation.addRelation(2, 2, bean, Maps.hashMap());

        final PackedAtlasBuilder builderNoRelation = new PackedAtlasBuilder();
        builderNoRelation.addNode(1L, Location.forString("1,1"),
                Maps.hashMap("someNode", "inTwoAtlases"));

        final PackedAtlas atlasWithRelation = (PackedAtlas) builderWithRelation.get();
        final PackedAtlas atlasNoRelation = (PackedAtlas) builderNoRelation.get();

        // 1 MiB resources
        final ByteArrayResource resourceWithRelation = new ByteArrayResource(1024 * 1024 * 1);
        final ByteArrayResource resourceNoRelation = new ByteArrayResource(1024 * 1024 * 1);

        atlasWithRelation.save(resourceWithRelation);
        atlasNoRelation.save(resourceNoRelation);

        /*
         * Here, we want to add the atlas without the relation first, since the MultiAtlas appends
         * identical nodes into the MultiNode list. It then will always choose the first subNode to
         * get the parent relations. See the MultiAtlas.populateReferences() and
         * MultiNode.relations() to see what's happening.
         */
        final Atlas multiAtlas = new AtlasResourceLoader().load(resourceNoRelation,
                resourceWithRelation);

        Assert.assertTrue(!multiAtlas.node(1L).relations().isEmpty());
    }

    @Test
    public void testPointsForMissingRelation()
    {
        final PackedAtlasBuilder builderWithRelation = new PackedAtlasBuilder();
        builderWithRelation.addPoint(1L, Location.forString("1,1"),
                Maps.hashMap("somePoint", "inTwoAtlases"));
        final RelationBean bean = new RelationBean();
        bean.addItem(1L,
                "Point appears in multiple atlases, but parent relation is only in one atlas",
                ItemType.POINT);
        builderWithRelation.addRelation(2, 2, bean, Maps.hashMap());

        final PackedAtlasBuilder builderNoRelation = new PackedAtlasBuilder();
        builderNoRelation.addPoint(1L, Location.forString("1,1"),
                Maps.hashMap("somePoiont", "inTwoAtlases"));

        final PackedAtlas atlasWithRelation = (PackedAtlas) builderWithRelation.get();
        final PackedAtlas atlasNoRelation = (PackedAtlas) builderNoRelation.get();

        // 1 MiB resources
        final ByteArrayResource resourceWithRelation = new ByteArrayResource(1024 * 1024 * 1);
        final ByteArrayResource resourceNoRelation = new ByteArrayResource(1024 * 1024 * 1);

        atlasWithRelation.save(resourceWithRelation);
        atlasNoRelation.save(resourceNoRelation);

        /*
         * Add the Atlas that is missing the relation second so that it overwrites the first one.
         * See the MultiAtlas.populateReferences() function to see this overwrite happen.
         */
        final Atlas multiAtlas = new AtlasResourceLoader().load(resourceWithRelation,
                resourceNoRelation);

        Assert.assertTrue(!multiAtlas.point(1L).relations().isEmpty());
    }

    @Test
    public void testRelationsForMissingRelation()
    {
        final PackedAtlasBuilder builderWithRelation = new PackedAtlasBuilder();
        builderWithRelation.addPoint(1L, Location.forString("1,1"),
                Maps.hashMap("somePoint", "inTwoAtlases"));
        final RelationBean bean = new RelationBean();
        bean.addItem(1L, "some member point", ItemType.POINT);
        builderWithRelation.addRelation(1, 1, bean, Maps.hashMap());
        final RelationBean bean2 = new RelationBean();
        bean2.addItem(1L, "parent relation of subrelation", ItemType.RELATION);
        builderWithRelation.addRelation(2, 2, bean2, Maps.hashMap());

        final PackedAtlasBuilder builderNoRelation = new PackedAtlasBuilder();
        builderNoRelation.addPoint(1L, Location.forString("1,1"),
                Maps.hashMap("somePoint", "inTwoAtlases"));
        final RelationBean bean3 = new RelationBean();
        bean3.addItem(1L, "some member point", ItemType.POINT);
        builderNoRelation.addRelation(1, 1, bean3, Maps.hashMap());

        final PackedAtlas atlasWithRelation = (PackedAtlas) builderWithRelation.get();
        final PackedAtlas atlasNoRelation = (PackedAtlas) builderNoRelation.get();

        // 1 MiB resources
        final ByteArrayResource resourceWithRelation = new ByteArrayResource(1024 * 1024 * 1);
        final ByteArrayResource resourceNoRelation = new ByteArrayResource(1024 * 1024 * 1);

        atlasWithRelation.save(resourceWithRelation);
        atlasNoRelation.save(resourceNoRelation);

        /*
         * Here, we want to add the atlas without the relation first, since the MultiAtlas appends
         * identical relations into the MultiRelation list. It then will always choose the first
         * subRelation to get the parent relations. See the MultiAtlas.populateReferences() and
         * MultiRelation.relations() to see what's happening.
         */
        final Atlas multiAtlas = new AtlasResourceLoader().load(resourceNoRelation,
                resourceWithRelation);

        Assert.assertTrue(!multiAtlas.relation(1L).relations().isEmpty());
    }
}
