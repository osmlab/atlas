package org.openstreetmap.atlas.geography.atlas.builder.store;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.Rectangle;
import org.openstreetmap.atlas.geography.Segment;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.builder.AtlasSize;
import org.openstreetmap.atlas.geography.atlas.builder.RelationBean;
import org.openstreetmap.atlas.geography.atlas.items.Area;
import org.openstreetmap.atlas.geography.atlas.items.LineItem;
import org.openstreetmap.atlas.geography.atlas.items.LocationItem;
import org.openstreetmap.atlas.geography.atlas.items.Relation;
import org.openstreetmap.atlas.geography.atlas.items.RelationMember;
import org.openstreetmap.atlas.geography.atlas.packed.RandomPackedAtlasBuilder;
import org.openstreetmap.atlas.geography.atlas.packed.RandomPackedAtlasBuilder.AtlasStartIdentifiers;
import org.openstreetmap.atlas.utilities.scalars.Distance;

/**
 * @author tony
 */
public class AtlasPrimitiveObjectStoreTest
{
    private static final Map<String, String> EMPTY = new HashMap<>();

    @Test
    public void testBuild()
    {
        final AtlasPrimitiveObjectStore store = new AtlasPrimitiveObjectStore();

        store.addNode(new AtlasPrimitiveLocationItem(1, Location.TEST_6, EMPTY));
        store.addNode(new AtlasPrimitiveLocationItem(2, Location.TEST_5, EMPTY));
        store.addEdge(new AtlasPrimitiveLineItem(11, new Segment(Location.TEST_6, Location.TEST_5),
                EMPTY));
        store.addEdge(new AtlasPrimitiveLineItem(-11, new Segment(Location.TEST_5, Location.TEST_6),
                EMPTY));

        final Atlas atlas = store.build();
        Assert.assertEquals(2, atlas.numberOfEdges());
        Assert.assertEquals(2, atlas.numberOfNodes());
    }

    @Test
    public void testCreation()
    {
        final Atlas source = createSource();

        final AtlasPrimitiveObjectStore store = new AtlasPrimitiveObjectStore();
        source.nodes().forEach(node -> store.addNode(convert(node)));
        source.points().forEach(point -> store.addPoint(convert(point)));
        source.edges().forEach(edge -> store.addEdge(convert(edge)));
        source.lines().forEach(line -> store.addLine(convert(line)));
        source.areas().forEach(area -> store.addArea(convert(area)));
        source.relations().forEach(relation -> store.addRelation(convert(relation)));

        final Atlas copy = store.build();
        Assert.assertEquals(source.numberOfAreas(), copy.numberOfAreas());
        Assert.assertEquals(source.numberOfEdges(), copy.numberOfEdges());
        Assert.assertEquals(source.numberOfNodes(), copy.numberOfNodes());
        Assert.assertEquals(source.numberOfPoints(), copy.numberOfPoints());
        Assert.assertEquals(source.numberOfLines(), copy.numberOfLines());
        Assert.assertEquals(source.numberOfRelations(), copy.numberOfRelations());
    }

    @Test
    public void testIntegrity()
    {
        final AtlasPrimitiveObjectStore store = new AtlasPrimitiveObjectStore();

        store.addNode(new AtlasPrimitiveLocationItem(1, Location.TEST_6, EMPTY));
        store.addNode(new AtlasPrimitiveLocationItem(2, Location.TEST_5, EMPTY));
        store.addEdge(new AtlasPrimitiveLineItem(11, new Segment(Location.TEST_6, Location.TEST_5),
                EMPTY));

        Assert.assertFalse(store.checkDataIntegrity().isPresent());

        // Add an edge reference to a not existed node
        store.addEdge(new AtlasPrimitiveLineItem(12, new Segment(Location.TEST_1, Location.TEST_6),
                EMPTY));
        final Optional<TemporaryObjectStore> missingObjects = store.checkDataIntegrity();
        Assert.assertTrue(missingObjects.isPresent());
        Assert.assertEquals(Location.TEST_1, missingObjects.get().getLocations().iterator().next());
        Assert.assertEquals(1, missingObjects.get().size());
    }

    private AtlasPrimitiveArea convert(final Area area)
    {
        return new AtlasPrimitiveArea(area.getIdentifier(), area.asPolygon(), area.getTags());
    }

    private AtlasPrimitiveLineItem convert(final LineItem item)
    {
        return new AtlasPrimitiveLineItem(item.getIdentifier(), item.asPolyLine(), item.getTags());
    }

    private AtlasPrimitiveLocationItem convert(final LocationItem item)
    {
        return new AtlasPrimitiveLocationItem(item.getIdentifier(), item.getLocation(),
                item.getTags());
    }

    private AtlasPrimitiveRelation convert(final Relation relation)
    {
        final RelationBean bean = new RelationBean();
        for (final RelationMember member : relation.members())
        {
            bean.addItem(member.getEntity().getIdentifier(), member.getRole(),
                    member.getEntity().getType());
        }
        return new AtlasPrimitiveRelation(relation.getIdentifier(), relation.getIdentifier(), bean,
                relation.getTags(), relation.bounds());
    }

    private Atlas createSource()
    {
        final Rectangle bounds = Location.TEST_1.boxAround(Distance.TEN_MILES);
        final AtlasSize estimates = new AtlasSize(8, 10, 4, 7, 9, 2);
        final long startIdentifier = 100;
        final AtlasStartIdentifiers startIdentifiers = new AtlasStartIdentifiers(
                startIdentifier + 100, startIdentifier + 200, startIdentifier + 300,
                startIdentifier + 400, startIdentifier + 500, startIdentifier + 600);
        return new RandomPackedAtlasBuilder().generate(estimates, startIdentifiers, bounds);
    }
}
