package org.openstreetmap.atlas.geography.atlas.command;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.items.complex.bignode.BigNodeFinder;
import org.openstreetmap.atlas.geography.atlas.items.complex.buildings.ComplexBuildingFinder;
import org.openstreetmap.atlas.geography.atlas.items.complex.islands.ComplexIslandFinder;
import org.openstreetmap.atlas.geography.atlas.items.complex.restriction.ComplexTurnRestrictionFinder;
import org.openstreetmap.atlas.geography.atlas.items.complex.waters.ComplexWaterEntityFinder;
import org.openstreetmap.atlas.streaming.resource.File;
import org.openstreetmap.atlas.utilities.collections.Iterables;
import org.openstreetmap.atlas.utilities.runtime.Command.Optionality;
import org.openstreetmap.atlas.utilities.runtime.Command.Switch;
import org.openstreetmap.atlas.utilities.runtime.Command.SwitchList;
import org.openstreetmap.atlas.utilities.runtime.CommandMap;

import com.google.common.collect.Table;
import com.google.common.collect.TreeBasedTable;

/**
 * Prints out the number of features for the given atlas files
 *
 * @author ahsieh
 */
public class AtlasFeatureCountsSubCommand extends AbstractAtlasSubCommand
{
    /**
     * The types of Atlas entities to be processed (edges, lines, areas, etc.)
     */
    protected enum AtlasType
    {
        NODE,
        LINE,
        AREA,
        POINT,
        EDGE,
        RELATION,
        COMPLEX_BUILDING,
        COMPLEX_WATER,
        COMPLEX_ISLAND,
        COMPLEX_TURN_RESTRICTION,
        COMPLEX_BIG_NODE
    }

    private static final Switch<File> OUTPUT_PARAMETER = new Switch<>("output",
            "The output file to save the statistics", value -> new File(value),
            Optionality.REQUIRED);

    private final Table<String, AtlasType, Long> featureCounts;

    public AtlasFeatureCountsSubCommand()
    {
        super("featureCounts", "lists counts of objects found");
        this.featureCounts = TreeBasedTable.create();
    }

    @Override
    public SwitchList switches()
    {
        return super.switches().with(OUTPUT_PARAMETER);
    }

    @Override
    public void usage(final PrintStream writer)
    {
        writer.printf(AtlasCommandConstants.INPUT_PARAMETER_DESCRIPTION);
    }

    @Override
    protected int finish(final CommandMap command)
    {
        final File file = (File) command.get(OUTPUT_PARAMETER);

        try (PrintStream out = new PrintStream(
                new BufferedOutputStream(new FileOutputStream(file.getFile(), true))))
        {
            for (final String country : this.featureCounts.rowKeySet())
            {
                for (final AtlasType type : AtlasType.values())
                {
                    out.println(String.format("%s-%s: %d", country, type,
                            this.featureCounts.contains(country, type)
                                    ? this.featureCounts.get(country, type) : 0));
                }

                out.println();
            }
        }
        catch (final IOException oops)
        {
            throw new CoreException("Error writing to file: {}", file.getAbsolutePath().toString(),
                    oops);
        }

        return 0;
    }

    @Override
    protected void handle(final Atlas atlas, final CommandMap command)
    {
        for (final AtlasType type : AtlasType.values())
        {
            updateHashMapForAtlasType(type, atlas);
        }
    }

    private void updateHashMapForAtlasType(final AtlasType type, final Atlas atlas)
    {
        long oldCount = 0;
        final long additionalCount;
        final String country = atlas.metaData().getCountry().orElseGet(() -> "UNKNOWN");

        switch (type)
        {
            case NODE:
                additionalCount = atlas.numberOfNodes();
                break;
            case LINE:
                additionalCount = atlas.numberOfLines();
                break;
            case AREA:
                additionalCount = atlas.numberOfAreas();
                break;
            case POINT:
                additionalCount = atlas.numberOfPoints();
                break;
            case EDGE:
                additionalCount = atlas.numberOfEdges();
                break;
            case RELATION:
                additionalCount = atlas.numberOfRelations();
                break;
            case COMPLEX_BUILDING:
                additionalCount = Iterables.count(new ComplexBuildingFinder().find(atlas), i -> 1L);
                break;
            case COMPLEX_WATER:
                additionalCount = Iterables.count(new ComplexWaterEntityFinder().find(atlas),
                        i -> 1L);
                break;
            case COMPLEX_ISLAND:
                additionalCount = Iterables.count(new ComplexIslandFinder().find(atlas), i -> 1L);
                break;
            case COMPLEX_TURN_RESTRICTION:
                additionalCount = Iterables.count(new ComplexTurnRestrictionFinder().find(atlas),
                        i -> 1L);
                break;
            case COMPLEX_BIG_NODE:
                additionalCount = Iterables.count(new BigNodeFinder().find(atlas), i -> 1L);
                break;
            default:
                throw new CoreException("Unexpected AtlasType: " + type.toString());
        }

        // check if there's already a value in the Table
        synchronized (this)
        {
            if (this.featureCounts.contains(country, type))
            {
                oldCount = this.featureCounts.get(country, type);
            }

            this.featureCounts.put(country, type, oldCount + additionalCount);
        }
    }
}
