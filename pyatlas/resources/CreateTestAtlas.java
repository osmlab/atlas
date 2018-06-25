package org.openstreetmap.atlas.test;

import java.util.ArrayList;
import java.util.List;

import org.openstreetmap.atlas.geography.Latitude;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.Longitude;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.Polygon;
import org.openstreetmap.atlas.geography.atlas.AtlasMetaData;
import org.openstreetmap.atlas.geography.atlas.builder.AtlasSize;
import org.openstreetmap.atlas.geography.atlas.builder.RelationBean;
import org.openstreetmap.atlas.geography.atlas.items.ItemType;
import org.openstreetmap.atlas.geography.atlas.packed.PackedAtlas;
import org.openstreetmap.atlas.geography.atlas.packed.PackedAtlas.AtlasSerializationFormat;
import org.openstreetmap.atlas.geography.atlas.packed.PackedAtlasBuilder;
import org.openstreetmap.atlas.streaming.resource.File;
import org.openstreetmap.atlas.utilities.collections.Maps;

/*
 * Code sample that uses Java Atlas API to create the test.atlas used by Pyatlas unit tests.
 */

public class CreateTestAtlas
{
    public static void main(final String[] args)
    {
        createTestAtlas();
    }

    private static void createTestAtlas()
    {
        final PackedAtlasBuilder builder = new PackedAtlasBuilder();

        final AtlasMetaData metaData = new AtlasMetaData(AtlasSize.DEFAULT, true, "testCodeVersion",
                "testDataVersion", "testCountry", "testShardName",
                Maps.hashMap("metakey", "metaval"));
        builder.setMetaData(metaData);

        // add points
        builder.addPoint(1, new Location(Latitude.degrees(38), Longitude.degrees(-118)),
                Maps.hashMap());
        builder.addPoint(2, new Location(Latitude.degrees(38.01), Longitude.degrees(-118.01)),
                Maps.hashMap("some_tag", "some_value"));
        builder.addPoint(3, new Location(Latitude.degrees(38.02), Longitude.degrees(-118.01)),
                Maps.hashMap("key1", "value2", "key2", "value2"));
        builder.addPoint(4, new Location(Latitude.degrees(38), Longitude.degrees(-118.05)),
                Maps.hashMap("", ""));
        builder.addPoint(5, new Location(Latitude.degrees(38.05), Longitude.degrees(-118.03)), Maps
                .hashMap("key1", "value2", "key2", "value2", "key3:subkey1", "value3:subvalue1"));

        // add lines
        final List<Location> shapePoints = new ArrayList<>();
        shapePoints.add(new Location(Latitude.degrees(38.02), Longitude.degrees(-118.02)));
        shapePoints.add(new Location(Latitude.degrees(38.03), Longitude.degrees(-118.01)));
        shapePoints.add(new Location(Latitude.degrees(38.06), Longitude.degrees(-118.05)));
        builder.addLine(1, new PolyLine(shapePoints),
                Maps.hashMap("key1", "value2", "key2", "value2"));
        shapePoints.clear();
        shapePoints.add(new Location(Latitude.degrees(38.06), Longitude.degrees(-118.09)));
        shapePoints.add(new Location(Latitude.degrees(38.03), Longitude.degrees(-118.1)));
        builder.addLine(2, new PolyLine(shapePoints),
                Maps.hashMap("key1", "value2", "key2", "value2"));

        // add areas
        shapePoints.add(new Location(Latitude.degrees(38.1), Longitude.degrees(-118.02)));
        shapePoints.add(new Location(Latitude.degrees(38.2), Longitude.degrees(-118.01)));
        shapePoints.add(new Location(Latitude.degrees(38.09), Longitude.degrees(-118.05)));
        builder.addArea(1, new Polygon(shapePoints),
                Maps.hashMap("key1", "value2", "key2", "value2"));
        shapePoints.clear();
        shapePoints.add(new Location(Latitude.degrees(39.1), Longitude.degrees(-118.06)));
        shapePoints.add(new Location(Latitude.degrees(39.2), Longitude.degrees(-118.02)));
        shapePoints.add(new Location(Latitude.degrees(38.09), Longitude.degrees(-118.03)));
        builder.addArea(2, new Polygon(shapePoints),
                Maps.hashMap("random key", "value2", "key2", "somenewvalue"));

        // add nodes
        builder.addNode(1, new Location(Latitude.degrees(39), Longitude.degrees(-118)),
                Maps.hashMap());
        builder.addNode(2, new Location(Latitude.degrees(39.02), Longitude.degrees(-119.01)),
                Maps.hashMap("key1", "value2", "key2", "value2"));
        builder.addNode(3, new Location(Latitude.degrees(39), Longitude.degrees(-119.05)),
                Maps.hashMap("asd", "asdf"));
        builder.addNode(4, new Location(Latitude.degrees(39.05), Longitude.degrees(-119.03)), Maps
                .hashMap("key1", "value2", "key2", "value2", "key3:subkey1", "value3:subvalue1"));

        // add edges
        shapePoints.clear();
        shapePoints.add(new Location(Latitude.degrees(39), Longitude.degrees(-119.05)));
        shapePoints.add(new Location(Latitude.degrees(39.02), Longitude.degrees(-119.01)));
        builder.addEdge(1, new PolyLine(shapePoints),
                Maps.hashMap("key1", "value2", "key2", "value2"));
        shapePoints.clear();
        shapePoints.add(new Location(Latitude.degrees(39), Longitude.degrees(-118)));
        shapePoints.add(new Location(Latitude.degrees(39.05), Longitude.degrees(-119.03)));
        builder.addEdge(2, new PolyLine(shapePoints),
                Maps.hashMap("key5", "asdsavalue2", "key2", "value2"));
        shapePoints.clear();
        shapePoints.add(new Location(Latitude.degrees(39.05), Longitude.degrees(-119.03)));
        shapePoints.add(new Location(Latitude.degrees(39), Longitude.degrees(-119.05)));
        builder.addEdge(3, new PolyLine(shapePoints),
                Maps.hashMap("key5", "asdsavalue2", "key2", "value2"));

        // add relations
        RelationBean bean = new RelationBean();
        bean.addItem(1L, "node 1", ItemType.NODE);
        bean.addItem(2L, "an edge", ItemType.EDGE);
        builder.addRelation(1, 1, bean, Maps.hashMap("type", "road", "status", "foo"));

        bean = new RelationBean();
        bean.addItem(1L, "a point", ItemType.POINT);
        bean.addItem(2L, "another point", ItemType.POINT);
        builder.addRelation(2, 2, bean, Maps.hashMap("key5", "qwertyvalue2", "key5", "asdvalue2"));

        final PackedAtlas atlas = (PackedAtlas) builder.get();
        atlas.setSaveSerializationFormat(AtlasSerializationFormat.PROTOBUF);
        final File resource = new File("test.atlas");
        System.out.println("Test atlas dumped to: " + resource.getAbsolutePath());
        atlas.save(resource);
    }
}
