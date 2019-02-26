package org.openstreetmap.atlas.utilities.command.subcommands;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.sub.AtlasCutType;
import org.openstreetmap.atlas.geography.converters.jts.JtsPolygonConverter;
import org.openstreetmap.atlas.streaming.resource.File;
import org.openstreetmap.atlas.streaming.resource.FileSuffix;
import org.openstreetmap.atlas.utilities.collections.StringList;
import org.openstreetmap.atlas.utilities.command.AtlasShellToolsException;
import org.openstreetmap.atlas.utilities.command.abstractcommand.CommandOutputDelegate;
import org.openstreetmap.atlas.utilities.command.abstractcommand.OptionAndArgumentDelegate;
import org.openstreetmap.atlas.utilities.command.parsing.OptionOptionality;
import org.openstreetmap.atlas.utilities.command.subcommands.templates.AtlasLoaderCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author lcram
 */
public class SubAtlasCommand extends AtlasLoaderCommand
{
    private static final Logger logger = LoggerFactory.getLogger(SubAtlasCommand.class);

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

    private AtlasCutType cutType;

    public static void main(final String[] args)
    {
        new SubAtlasCommand().runSubcommandAndExit(args);
    }

    public SubAtlasCommand()
    {
        super();
        this.optionAndArgumentDelegate = this.getOptionAndArgumentDelegate();
        this.outputDelegate = this.getCommandOutputDelegate();
        this.cutType = AtlasCutType.SOFT_CUT;
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
        this.registerOptionWithRequiredArgument(CUT_TYPE_OPTION_LONG, CUT_TYPE_OPTION_DESCRIPTION,
                OptionOptionality.OPTIONAL, CUT_TYPE_OPTION_HINT);
        super.registerOptionsAndArguments();
    }

    @Override
    protected void processAtlas(final Atlas atlas, final String atlasFileName,
            final File atlasResource)
    {
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

        final Optional<Atlas> subbedAtlas;
        if (geometry instanceof Polygon)
        {
            final org.openstreetmap.atlas.geography.Polygon polygon = new JtsPolygonConverter()
                    .backwardConvert((Polygon) geometry);
            subbedAtlas = atlas.subAtlas(polygon, this.cutType);
        }
        else
        {
            this.outputDelegate.printlnErrorMessage("unsupported geometry type " + wkt);
            subbedAtlas = Optional.empty();
        }

        if (subbedAtlas.isPresent())
        {
            final String fileName = AtlasLoaderCommand.removeSuffixFromFileName(atlasFileName);
            final Path concatenatedPath = Paths.get(getOutputPath().toAbsolutePath().toString(),
                    fileName);
            final File outputFile = new File(
                    concatenatedPath.toAbsolutePath().toString() + "_sub" + FileSuffix.ATLAS);
            subbedAtlas.get().save(outputFile);
            if (this.optionAndArgumentDelegate.hasVerboseOption())
            {
                this.outputDelegate.printlnCommandMessage(
                        "saved to " + outputFile.getFile().getAbsolutePath());
            }
        }
        else
        {
            this.outputDelegate.printlnWarnMessage(
                    "skipping save of empty subatlas cut from " + atlasResource.getPath());
        }
    }

    @Override
    protected int start()
    {
        final String cutTypeString = this.optionAndArgumentDelegate
                .getOptionArgument(CUT_TYPE_OPTION_LONG).orElse("SOFT_CUT");

        try
        {
            this.cutType = AtlasCutType.valueOf(cutTypeString.toUpperCase());
        }
        catch (final IllegalArgumentException exception)
        {
            this.outputDelegate.printlnErrorMessage("invalid cut type " + cutTypeString);
            this.outputDelegate.printlnStderr("Try " + new StringList(CUT_TYPE_STRINGS).join(", "));
            return 1;
        }
        return 0;
    }
}
