package org.openstreetmap.atlas.test;

import java.util.Optional;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.AtlasResourceLoader;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.Node;
import org.openstreetmap.atlas.geography.atlas.items.Relation;
import org.openstreetmap.atlas.geography.atlas.items.TurnRestriction;
import org.openstreetmap.atlas.streaming.resource.File;
import org.openstreetmap.atlas.utilities.conversion.StringConverter;
import org.openstreetmap.atlas.utilities.runtime.Command;
import org.openstreetmap.atlas.utilities.runtime.CommandMap;

public class Debug3 extends Command
{
    Switch<String> INPUT_FOLDER = new Switch<>("inputFolder", "input folder of atlas",
            StringConverter.IDENTITY, Optionality.REQUIRED);

    public static void main(final String... args)
    {
        new Debug3().runWithoutQuitting(args);
    }

    @Override
    protected int onRun(final CommandMap command)
    {
        final String atlasDirectory = (String) command.get(this.INPUT_FOLDER);

        final Atlas atlas = new AtlasResourceLoader().load(new File(atlasDirectory));

        final Node node = atlas.node(1136084879000000L);
        final Edge edge1_1 = atlas.edge(152653190000001L);
        final Edge edge1_2 = atlas.edge(152653190000002L);
        final Edge edge2_1 = atlas.edge(28808672000001L);
        final Edge edge2_2 = atlas.edge(28808672000002L);
        final Edge edge2_3 = atlas.edge(28808672000003L);
        final Relation relation = atlas.relation(2063544000000L);

        System.out.println("Node 1136084879000000:");
        System.out.println(node);
        System.out.println("Edge 152653190000001:");
        System.out.println(edge1_1);
        System.out.println("Edge 152653190000002:");
        System.out.println(edge1_2);
        System.out.println("Edge 28808672000001:");
        System.out.println(edge2_1);
        System.out.println("Edge 28808672000002:");
        System.out.println(edge2_2);
        System.out.println("Edge 28808672000003:");
        System.out.println(edge2_3);
        System.out.println("Relation 2063544000000:");
        System.out.println(relation);

        System.out.println("Node.relations() 1136084879000000:");
        System.out.println(node.relations());
        System.out.println("Edge.relations() 152653190000001:");
        System.out.println(edge1_1.relations());
        System.out.println("Edge.relations() 152653190000002:");
        System.out.println(edge1_2.relations());
        System.out.println("Edge.relations() 28808672000001:");
        System.out.println(edge2_1.relations());
        System.out.println("Edge.relations() 28808672000002:");
        System.out.println(edge2_2.relations());
        System.out.println("Edge.relations() 28808672000003:");
        System.out.println(edge2_3.relations());

        if (relation != null)
        {
            final Optional<TurnRestriction> turnRestriction = TurnRestriction.from(relation);
            System.out.println("Relation as TurnRestriction:");
            System.out.println(turnRestriction);
        }

        return 0;
    }

    @Override
    protected SwitchList switches()
    {
        return new SwitchList().with(this.INPUT_FOLDER);
    }

}
