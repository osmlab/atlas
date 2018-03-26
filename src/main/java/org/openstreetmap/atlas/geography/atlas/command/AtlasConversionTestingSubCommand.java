package org.openstreetmap.atlas.geography.atlas.command;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.builder.proto.ProtoAtlasBuilder;
import org.openstreetmap.atlas.geography.atlas.packed.PackedAtlas;
import org.openstreetmap.atlas.geography.atlas.packed.PackedAtlasBuilder;
import org.openstreetmap.atlas.streaming.resource.File;
import org.openstreetmap.atlas.utilities.runtime.Command.SwitchList;
import org.openstreetmap.atlas.utilities.runtime.CommandMap;
import org.openstreetmap.atlas.utilities.runtime.FlexibleSubCommand;

/**
 * @author lcram
 */
public class AtlasConversionTestingSubCommand implements FlexibleSubCommand
{

    private static final String NAME = "atlas-conversion-testing";
    private static final String DESCRIPTION = "hardcoded class for testing atlas conversions";

    @Override
    public int execute(final CommandMap map)
    {
        final PackedAtlasBuilder packedAtlasBuilder = new PackedAtlasBuilder();
        final Map<String, String> tags = new HashMap<>();
        final List<Location> shapePoints = new ArrayList<>();

        // add points
        tags.put("building", "yes");
        tags.put("name", "eiffel_tower");
        packedAtlasBuilder.addPoint(1, Location.EIFFEL_TOWER, tags);
        tags.clear();
        tags.put("building", "yes");
        tags.put("name", "colosseum");
        packedAtlasBuilder.addPoint(2, Location.COLOSSEUM, tags);

        // // add lines
        // tags.clear();
        // tags.put("path", "yes");
        // shapePoints.add(Location.EIFFEL_TOWER);
        // shapePoints.add(Location.COLOSSEUM);
        // packedAtlasBuilder.addLine(3, new PolyLine(shapePoints), tags);
        //
        // // add areas
        // tags.clear();
        // tags.put("triangle", "yes");
        // tags.put("size", "stupidbig");
        // shapePoints.clear();
        // shapePoints.add(Location.EIFFEL_TOWER);
        // shapePoints.add(Location.COLOSSEUM);
        // shapePoints.add(Location.STEVENS_CREEK);
        // packedAtlasBuilder.addArea(4, new Polygon(shapePoints), tags);
        //
        // // add nodes
        // tags.clear();
        // tags.put("sometag:namespace", "somevalue");
        // packedAtlasBuilder.addNode(5, Location.forString("48.3406719,10.5563445"), tags);
        // tags.clear();
        // packedAtlasBuilder.addNode(6, Location.forString("48.34204,10.55844"), tags);
        //
        // // add edges
        // tags.clear();
        // tags.put("edge", "yes");
        // shapePoints.clear();
        // shapePoints.add(Location.forString("48.3406719,10.5563445"));
        // shapePoints.add(Location.forString("48.34204,10.55844"));
        // packedAtlasBuilder.addEdge(7, new PolyLine(shapePoints), tags);
        //
        // // add relations
        // tags.clear();
        // tags.put("relationtag", "somevalue");
        // RelationBean bean = new RelationBean();
        // bean.addItem(1L, "This is the Eiffel Tower Point", ItemType.POINT);
        // packedAtlasBuilder.addRelation(8, 8, bean, tags);
        //
        // tags.clear();
        // tags.put("name", "coolstuff");
        // tags.put("has_subrelation", "yes");
        // bean = new RelationBean();
        // bean.addItem(1L, "Eiffel Tower", ItemType.POINT);
        // bean.addItem(2L, "Colosseum", ItemType.POINT);
        // bean.addItem(8L, "subrelation", ItemType.RELATION);
        // packedAtlasBuilder.addRelation(9, 9, bean, tags);

        final Atlas atlas = packedAtlasBuilder.get();
        atlas.save(new File("/Users/lucascram/Desktop/test.atlas"));

        PackedAtlas.load(new File("/Users/lucascram/Desktop/test.atlas"))
                .saveAsProto(new File("/Users/lucascram/Desktop/test.npatlas"));

        new ProtoAtlasBuilder().read(new File("/Users/lucascram/Desktop/test.npatlas"))
                .save(new File("/Users/lucascram/Desktop/test_out.atlas"));

        PackedAtlas.load(new File("/Users/lucascram/Desktop/test.atlas"))
                .saveAsText(new File("/Users/lucascram/Desktop/test.textatlas"));

        PackedAtlas.load(new File("/Users/lucascram/Desktop/test_out.atlas"))
                .saveAsText(new File("/Users/lucascram/Desktop/test_out.textatlas"));

        return 0;
    }

    @Override
    public String getDescription()
    {
        return DESCRIPTION;
    }

    @Override
    public String getName()
    {
        return NAME;
    }

    @Override
    public SwitchList switches()
    {
        return new SwitchList();
    }

    @Override
    public void usage(final PrintStream writer)
    {

    }

}
