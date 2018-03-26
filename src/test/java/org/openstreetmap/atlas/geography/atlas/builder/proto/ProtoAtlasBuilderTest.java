package org.openstreetmap.atlas.geography.atlas.builder.proto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.Polygon;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.builder.RelationBean;
import org.openstreetmap.atlas.geography.atlas.items.ItemType;
import org.openstreetmap.atlas.geography.atlas.packed.PackedAtlasBuilder;
import org.openstreetmap.atlas.streaming.resource.ByteArrayResource;
import org.openstreetmap.atlas.streaming.resource.WritableResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author lcram
 */
public class ProtoAtlasBuilderTest
{
    private static final Logger logger = LoggerFactory.getLogger(ProtoAtlasBuilderTest.class);

    private static long idCounter = 0L;

    private static long getNextId()
    {
        idCounter++;
        return idCounter;
    }

    @Test
    public void testReadWriteConsistency()
    {
        final WritableResource resource = new ByteArrayResource();
        final ProtoAtlasBuilder protoAtlasBuilder = new ProtoAtlasBuilder();
        final PackedAtlasBuilder packedAtlasBuilder = setUpTestAtlasBuilder();

        // make sure the atlases are the same
        final Atlas outAtlas = packedAtlasBuilder.get();
        protoAtlasBuilder.write(outAtlas, resource);
        final Atlas inAtlas = protoAtlasBuilder.read(resource);

        logger.debug("ATLAS THAT WAS WRITTEN OUT:\n{}\n", outAtlas);
        logger.debug("ATLAS THAT WAS READ BACK:\n{}\n", inAtlas);

        Assert.assertEquals(outAtlas, inAtlas);
    }

    private PackedAtlasBuilder setUpTestAtlasBuilder()
    {
        final PackedAtlasBuilder packedAtlasBuilder = new PackedAtlasBuilder();
        final Map<String, String> tags = new HashMap<>();
        final List<Location> shapePoints = new ArrayList<>();

        // add points
        tags.put("building", "yes");
        tags.put("name", "eiffel_tower");
        packedAtlasBuilder.addPoint(getNextId(), Location.EIFFEL_TOWER, tags);
        tags.clear();
        tags.put("building", "yes");
        tags.put("name", "colosseum");
        packedAtlasBuilder.addPoint(getNextId(), Location.COLOSSEUM, tags);

        // add lines
        tags.clear();
        tags.put("path", "yes");
        shapePoints.add(Location.EIFFEL_TOWER);
        shapePoints.add(Location.COLOSSEUM);
        packedAtlasBuilder.addLine(getNextId(), new PolyLine(shapePoints), tags);

        // add areas
        tags.clear();
        tags.put("triangle", "yes");
        tags.put("size", "stupidbig");
        shapePoints.clear();
        shapePoints.add(Location.EIFFEL_TOWER);
        shapePoints.add(Location.COLOSSEUM);
        shapePoints.add(Location.STEVENS_CREEK);
        packedAtlasBuilder.addArea(getNextId(), new Polygon(shapePoints), tags);

        // add nodes
        tags.clear();
        tags.put("sometag:namespace", "somevalue");
        packedAtlasBuilder.addNode(getNextId(), Location.forString("48.3406719,10.5563445"), tags);
        tags.clear();
        packedAtlasBuilder.addNode(getNextId(), Location.forString("48.34204,10.55844"), tags);

        // add edges
        tags.clear();
        tags.put("edge", "yes");
        shapePoints.clear();
        shapePoints.add(Location.forString("48.3406719,10.5563445"));
        shapePoints.add(Location.forString("48.34204,10.55844"));
        packedAtlasBuilder.addEdge(getNextId(), new PolyLine(shapePoints), tags);

        // add relations
        tags.clear();
        tags.put("relationtag", "somevalue");
        RelationBean bean = new RelationBean();
        bean.addItem(1L, "This is the Eiffel Tower Point", ItemType.POINT);
        packedAtlasBuilder.addRelation(getNextId(), idCounter, bean, tags);

        tags.clear();
        tags.put("name", "coolstuff");
        tags.put("has_subrelation", "yes");
        bean = new RelationBean();
        bean.addItem(1L, "Eiffel Tower", ItemType.POINT);
        bean.addItem(2L, "Colosseum", ItemType.POINT);
        bean.addItem(8L, "subrelation", ItemType.RELATION);
        packedAtlasBuilder.addRelation(getNextId(), idCounter, bean, tags);

        return packedAtlasBuilder;
    }
}
