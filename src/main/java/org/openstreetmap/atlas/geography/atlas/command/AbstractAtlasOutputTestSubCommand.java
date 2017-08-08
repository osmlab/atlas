package org.openstreetmap.atlas.geography.atlas.command;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentHashMap.KeySetView;
import java.util.stream.StreamSupport;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.Rectangle;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.geography.atlas.multi.MultiAtlas;
import org.openstreetmap.atlas.geography.atlas.packed.PackedAtlas;
import org.openstreetmap.atlas.geography.atlas.packed.PackedAtlasCloner;
import org.openstreetmap.atlas.streaming.resource.File;
import org.openstreetmap.atlas.utilities.runtime.Command.Optionality;
import org.openstreetmap.atlas.utilities.runtime.Command.Switch;
import org.openstreetmap.atlas.utilities.runtime.Command.SwitchList;
import org.openstreetmap.atlas.utilities.runtime.CommandMap;
import org.openstreetmap.atlas.utilities.scalars.Distance;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;

/**
 * Shared code for generating a single text-based test atlas for any building that passes the filter
 * as defined by concrete subclasses.
 *
 * @author cstaylor
 */
abstract class AbstractAtlasOutputTestSubCommand extends AbstractAtlasSubCommand
{
    private static final Switch<Path> OUTPUT_TO_PACKED_ATLAS_PARAMETER = new Switch<>(
            "packed-output", "Outputs the found atlas pieces into a packed atlas for testing",
            Paths::get, Optionality.OPTIONAL);
    private static final Switch<Path> OUTPUT_TO_TEXT_PARAMETER = new Switch<>("text-output",
            "Outputs the found atlas pieces into text for testing", Paths::get,
            Optionality.OPTIONAL);

    private static final Switch<Double> DISTANCE_IN_METERS_PARAMETER = new Switch<>("expand",
            "Expands the search bounds by this optional parameter in meters", Double::new,
            Optionality.OPTIONAL);

    private KeySetView<Atlas, Boolean> subAtlases;

    private Optional<Double> distanceInMeters;

    private Path outputTextPath;
    private Path packedAtlasPath;

    protected AbstractAtlasOutputTestSubCommand(final String name, final String description)
    {
        super(name, description);
    }

    @Override
    public SwitchList switches()
    {
        return super.switches().with(OUTPUT_TO_PACKED_ATLAS_PARAMETER, OUTPUT_TO_TEXT_PARAMETER,
                DISTANCE_IN_METERS_PARAMETER);
    }

    @Override
    public void usage(final PrintStream writer)
    {
        writer.printf(AtlasCommandConstants.INPUT_PARAMETER_DESCRIPTION);
        writer.printf(
                "-packed-output=/output/to/packed/atlas : the file we should send the packed Atlas to if it exists\n");
        writer.printf(
                "-text-output=/output/to/text/file : the file we should send the text data of the Atlas if it exists\n");
        writer.printf(
                "-expand=[distance in meters] : how far we should expand around the building we've found for the subatlas we're saving\n");
    }

    protected abstract boolean filter(AtlasEntity entity);

    @Override
    protected int finish(final CommandMap command)
    {
        if (this.subAtlases.isEmpty())
        {
            LoggerFactory.getLogger(getClass()).info("No items found");
            return -1;
        }
        if (this.packedAtlasPath != null)
        {
            final Atlas atlas = new MultiAtlas(this.subAtlases);
            try
            {
                final PackedAtlas saveMe = new PackedAtlasCloner().cloneFrom(atlas);
                Files.createDirectories(this.packedAtlasPath.getParent());
                saveMe.save(new File(this.packedAtlasPath.toFile()));
                LoggerFactory.getLogger(getClass()).info("Packed atlas saved to {}",
                        this.packedAtlasPath);
            }
            catch (final IOException oops)
            {
                throw new CoreException("Error when saving packed atlas", oops);
            }

        }
        if (this.outputTextPath != null)
        {
            new MultiAtlas(ImmutableList.copyOf(this.subAtlases.iterator()))
                    .saveAsText(new File(this.outputTextPath.toFile()));
            LoggerFactory.getLogger(getClass()).info("Text atlas saved to {}", this.outputTextPath);
        }
        return 0;
    }

    @Override
    protected void handle(final Atlas atlas, final CommandMap command)
    {
        StreamSupport.stream(atlas.entities(i -> i.getOsmIdentifier() > 0).spliterator(), false)
                .filter(this::filter).forEach(this::output);

    }

    @SuppressWarnings("unchecked")
    @Override
    protected void start(final CommandMap command)
    {
        this.subAtlases = ConcurrentHashMap.newKeySet();
        this.distanceInMeters = (Optional<Double>) command.getOption(DISTANCE_IN_METERS_PARAMETER);
        ((Optional<Path>) command.getOption(OUTPUT_TO_TEXT_PARAMETER)).ifPresent(path ->
        {
            this.outputTextPath = path;
        });
        ((Optional<Path>) command.getOption(OUTPUT_TO_PACKED_ATLAS_PARAMETER)).ifPresent(path ->
        {
            this.packedAtlasPath = path;
        });

        if (this.outputTextPath == null && this.packedAtlasPath == null)
        {
            throw new CoreException("Either -packed-output or -text-output must have a value");
        }
    }

    private void output(final AtlasEntity item)
    {
        Rectangle rectangle = item.bounds();
        if (this.distanceInMeters.isPresent())
        {
            rectangle = rectangle.expand(Distance.meters(this.distanceInMeters.get()));
        }
        item.getAtlas().subAtlas(rectangle).ifPresent(this.subAtlases::add);
    }
}
