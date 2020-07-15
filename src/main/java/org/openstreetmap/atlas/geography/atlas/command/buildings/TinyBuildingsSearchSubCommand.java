package org.openstreetmap.atlas.geography.atlas.command.buildings;

import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.StreamSupport;

import org.apache.commons.compress.utils.IOUtils;
import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.command.AbstractAtlasSubCommand;
import org.openstreetmap.atlas.geography.atlas.command.AtlasCommandConstants;
import org.openstreetmap.atlas.geography.atlas.items.ItemType;
import org.openstreetmap.atlas.geography.atlas.items.complex.buildings.ComplexBuilding;
import org.openstreetmap.atlas.geography.atlas.items.complex.buildings.ComplexBuildingFinder;
import org.openstreetmap.atlas.tags.ISOCountryTag;
import org.openstreetmap.atlas.utilities.runtime.Command.Optionality;
import org.openstreetmap.atlas.utilities.runtime.Command.Switch;
import org.openstreetmap.atlas.utilities.runtime.Command.SwitchList;
import org.openstreetmap.atlas.utilities.runtime.CommandMap;
import org.openstreetmap.atlas.utilities.scalars.Surface;

/**
 * Outputs information about buildings that have a surface area of less than 1 square meter
 *
 * @author cstaylor
 */
public class TinyBuildingsSearchSubCommand extends AbstractAtlasSubCommand
{
    /**
     * Logs some basic information about each building that fails the 1 sq meter test
     *
     * @author cstaylor
     */
    private static final class TinyBuildingLogger implements Consumer<ComplexBuilding>, Closeable
    {
        private final PrintStream output;

        private TinyBuildingLogger(final PrintStream output)
        {
            this.output = output;
        }

        @Override
        public void accept(final ComplexBuilding tinyBuilding)
        {
            final String url = String.format("http://www.openstreetmap.org/%s/%d",
                    tinyBuilding.getSource().getType() == ItemType.AREA ? "way" : "relation",
                    tinyBuilding.getOsmIdentifier());
            this.output.printf("%s,%d,%d,%s,%.2f\n",
                    tinyBuilding.getTag(ISOCountryTag.class, Optional.empty()).orElse("UNK"),
                    tinyBuilding.getIdentifier(), tinyBuilding.getOsmIdentifier(), url,
                    tinyBuilding.getOutline().get().surface().asMeterSquared());
        }

        @Override
        public void close() throws IOException
        {
            IOUtils.closeQuietly(this.output);
        }
    }

    private static final Switch<Surface> MINIMUM_BUILDING_SIZE_PARAMETER = new Switch<>("minimum",
            "The minimum area permitted for a building",
            value -> Surface.UNIT_METER_SQUARED_ON_EARTH_SURFACE.scaleBy(Double.valueOf(value)),
            Optionality.REQUIRED);

    private static final Switch<Path> OUTPUT_FILE_PARAMETER = new Switch<>("output",
            "File containing the CSV information about each tiny building", Paths::get,
            Optionality.REQUIRED);

    private Surface minimumSurface;
    private TinyBuildingLogger counter;

    public TinyBuildingsSearchSubCommand()
    {
        super("buildings-for-ants",
                "Lists all of the buildings with areas smaller than a given size");
    }

    @Override
    public SwitchList switches()
    {
        return super.switches().with(MINIMUM_BUILDING_SIZE_PARAMETER, OUTPUT_FILE_PARAMETER);
    }

    @Override
    public void usage(final PrintStream writer)
    {
        writer.printf(AtlasCommandConstants.INPUT_PARAMETER_DESCRIPTION);
        writer.printf("-minimum=[scale factor of square meters]\n");
        writer.printf("-output=/path/to/output/file\n");
    }

    @Override
    protected int finish(final CommandMap command)
    {
        try
        {
            this.counter.close();
        }
        catch (final IOException oops)
        {
            throw new CoreException("Failure to close", oops);
        }
        return 0;
    }

    @Override
    protected void handle(final Atlas atlas, final CommandMap command)
    {
        StreamSupport.stream(new ComplexBuildingFinder().find(atlas).spliterator(), false)
                .map(ComplexBuilding.class::cast).filter(this::tooSmall).forEach(this.counter);
    }

    @Override
    protected void start(final CommandMap command)
    {
        super.start(command);
        this.minimumSurface = (Surface) command.get(MINIMUM_BUILDING_SIZE_PARAMETER);
        this.counter = new TinyBuildingLogger(createStream(command));
    }

    private PrintStream createStream(final CommandMap command)
    {
        try
        {
            final Path output = (Path) command.get(OUTPUT_FILE_PARAMETER);
            try
            {
                Files.createDirectories(output.getParent());
            }
            catch (final IOException oops)
            {
                throw new CoreException("Error when creating output directory", oops);
            }
            return new PrintStream(new BufferedOutputStream(new FileOutputStream(output.toFile())));
        }
        catch (final IOException oops)
        {
            throw new CoreException("Failure to open output", oops);
        }
    }

    private boolean tooSmall(final ComplexBuilding building)
    {
        return building.getOutline().get().surface().isLessThanOrEqualTo(this.minimumSurface);
    }
}
