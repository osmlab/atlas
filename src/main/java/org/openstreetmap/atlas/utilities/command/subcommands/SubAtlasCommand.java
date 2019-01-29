package org.openstreetmap.atlas.utilities.command.subcommands;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.AtlasResourceLoader;
import org.openstreetmap.atlas.geography.atlas.packed.PackedAtlas;
import org.openstreetmap.atlas.geography.atlas.packed.PackedAtlasCloner;
import org.openstreetmap.atlas.geography.atlas.sub.AtlasCutType;
import org.openstreetmap.atlas.geography.converters.jts.JtsPolygonConverter;
import org.openstreetmap.atlas.streaming.resource.File;
import org.openstreetmap.atlas.streaming.resource.FileSuffix;
import org.openstreetmap.atlas.utilities.collections.StringList;
import org.openstreetmap.atlas.utilities.command.AtlasShellToolsException;
import org.openstreetmap.atlas.utilities.command.abstractcommand.CommandOutputDelegate;
import org.openstreetmap.atlas.utilities.command.abstractcommand.OptionAndArgumentDelegate;
import org.openstreetmap.atlas.utilities.command.parsing.OptionOptionality;
import org.openstreetmap.atlas.utilities.command.subcommands.templates.VariadicAtlasLoaderCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

/**
 * @author lcram
 */
public class SubAtlasCommand extends VariadicAtlasLoaderCommand
{
    private static final Logger logger = LoggerFactory.getLogger(SubAtlasCommand.class);

    private static final String PARALLEL_OPTION_LONG = "parallel";
    private static final Character PARALLEL_OPTION_SHORT = 'p';
    private static final String PARALLEL_OPTION_DESCRIPTION = "Process the atlases in parallel.";

    private static final String WKT_OPTION_LONG = "wkt";
    private static final String WKT_OPTION_DESCRIPTION = "WKT of the polygon with which to cut";
    private static final String WKT_OPTION_HINT = "wkt";

    private static final List<String> CUT_TYPE_STRINGS = Arrays.stream(AtlasCutType.values())
            .map(AtlasCutType::toString).collect(Collectors.toList());
    private static final String CUT_TYPE_OPTION_LONG = "cut-type";
    private static final String CUT_TYPE_OPTION_DESCRIPTION = "The cut-type of this subatlas. Valid settings are: "
            + new StringList(CUT_TYPE_STRINGS).join(", ") + ". Defaults to SOFT_CUT.";
    private static final String CUT_TYPE_OPTION_HINT = "type";

    private final OptionAndArgumentDelegate optionAndArgumentDelegate;
    private final CommandOutputDelegate outputDelegate;

    public static void main(final String[] args)
    {
        new SubAtlasCommand().runSubcommandAndExit(args);
    }

    public SubAtlasCommand()
    {
        super();
        this.optionAndArgumentDelegate = this.getOptionAndArgumentDelegate();
        this.outputDelegate = this.getCommandOutputDelegate();
    }

    @Override
    public int execute()
    {
        final List<File> atlasResourceList = this.getInputAtlasResources();
        if (atlasResourceList.isEmpty())
        {
            this.outputDelegate.printlnErrorMessage("no input atlases");
            return 1;
        }
        final Stream<File> atlasResourceStream = atlasResourceList.stream();

        final Optional<Path> outputParentPath = this.getOutputPath();
        if (!outputParentPath.isPresent())
        {
            this.outputDelegate.printlnErrorMessage("invalid output path");
            return 1;
        }

        if (this.optionAndArgumentDelegate.hasOption(PARALLEL_OPTION_LONG))
        {
            atlasResourceStream.parallel();
        }

        final String cutTypeString = this.optionAndArgumentDelegate
                .getOptionArgument(CUT_TYPE_OPTION_LONG).orElse("SOFT_CUT");
        final AtlasCutType cutType;
        try
        {
            cutType = AtlasCutType.valueOf(cutTypeString.toUpperCase());
        }
        catch (final IllegalArgumentException exception)
        {
            this.outputDelegate.printlnErrorMessage("invalid cut type " + cutTypeString);
            this.outputDelegate.printlnStderr("Try " + new StringList(CUT_TYPE_STRINGS).join(", "));
            return 1;
        }

        atlasResourceStream.forEach(fileResource ->
        {
            if (this.optionAndArgumentDelegate.hasVerboseOption())
            {
                this.outputDelegate.printlnStdout(
                        "Subatlasing " + fileResource.getFile().getAbsolutePath() + "...");
            }
            final Optional<Atlas> outputAtlas = processAtlas(fileResource, cutType);
            if (outputAtlas.isPresent())
            {
                final String filePath = this.getFileNameNoSuffix(fileResource);
                final Path concatenatedPath = Paths
                        .get(outputParentPath.get().toAbsolutePath().toString(), filePath);
                final File outputFile = new File(
                        concatenatedPath.toAbsolutePath().toString() + "_sub" + FileSuffix.ATLAS);
                outputAtlas.get().save(outputFile);
                if (this.optionAndArgumentDelegate.hasVerboseOption())
                {
                    this.outputDelegate
                            .printlnStdout("Saved to " + outputFile.getFile().getAbsolutePath());
                }
            }
            else
            {
                this.outputDelegate.printlnWarnMessage("skipping save of empty subatlas cut from "
                        + fileResource.getFile().getName());
            }
        });

        return 0;
    }

    @Override
    public String getCommandName()
    {
        return "subatlas";
    }

    @Override
    public String getSimpleDescription()
    {
        return "cut subatlases according to given parameters";
    }

    @Override
    public void registerManualPageSections()
    {
        addManualPageSection("DESCRIPTION",
                SubAtlasCommand.class.getResourceAsStream("SubAtlasCommandDescriptionSection.txt"));
        addManualPageSection("EXAMPLES",
                SubAtlasCommand.class.getResourceAsStream("SubAtlasCommandExamplesSection.txt"));
        super.registerManualPageSections();
    }

    @Override
    public void registerOptionsAndArguments()
    {
        this.registerOptionWithRequiredArgument(WKT_OPTION_LONG, WKT_OPTION_DESCRIPTION,
                OptionOptionality.REQUIRED, WKT_OPTION_HINT);
        this.registerOption(PARALLEL_OPTION_LONG, PARALLEL_OPTION_SHORT,
                PARALLEL_OPTION_DESCRIPTION, OptionOptionality.OPTIONAL);
        this.registerOptionWithRequiredArgument(CUT_TYPE_OPTION_LONG, CUT_TYPE_OPTION_DESCRIPTION,
                OptionOptionality.OPTIONAL, CUT_TYPE_OPTION_HINT);
        super.registerOptionsAndArguments();
    }

    private Optional<Atlas> processAtlas(final File resource, final AtlasCutType cutType)
    {
        final PackedAtlas atlas = new PackedAtlasCloner()
                .cloneFrom(new AtlasResourceLoader().load(resource));
        final String wkt = this.optionAndArgumentDelegate.getOptionArgument(WKT_OPTION_LONG)
                .orElseThrow(AtlasShellToolsException::new);

        final WKTReader reader = new WKTReader();
        Geometry geometry = null;
        try
        {
            geometry = reader.read(wkt);
        }
        catch (final ParseException exception)
        {
            logger.error("unable to parse {}", wkt, exception);
        }

        if (geometry instanceof Polygon)
        {
            final org.openstreetmap.atlas.geography.Polygon polygon = new JtsPolygonConverter()
                    .backwardConvert((Polygon) geometry);
            return atlas.subAtlas(polygon, cutType);
        }
        else
        {
            this.outputDelegate.printlnErrorMessage("unsupported geometry type " + wkt);
        }

        return Optional.empty();
    }
}
