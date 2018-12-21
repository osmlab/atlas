package org.openstreetmap.atlas.utilities.command.subcommands;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.AtlasResourceLoader;
import org.openstreetmap.atlas.geography.atlas.packed.PackedAtlas;
import org.openstreetmap.atlas.geography.atlas.packed.PackedAtlasCloner;
import org.openstreetmap.atlas.geography.atlas.sub.AtlasCutType;
import org.openstreetmap.atlas.geography.converters.jts.JtsPolygonConverter;
import org.openstreetmap.atlas.streaming.resource.File;
import org.openstreetmap.atlas.streaming.resource.FileSuffix;
import org.openstreetmap.atlas.utilities.command.AtlasShellToolsException;
import org.openstreetmap.atlas.utilities.command.abstractcommand.CommandOutputDelegate;
import org.openstreetmap.atlas.utilities.command.abstractcommand.OptionAndArgumentFetcher;
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

    private static final String DESCRIPTION_SECTION = "SubAtlasCommandDescriptionSection.txt";
    private static final String EXAMPLES_SECTION = "SubAtlasCommandExamplesSection.txt";

    private static final Integer WKT_CONTEXT = 3;

    private static final String PARALLEL_OPTION_LONG = "parallel";
    private static final Character PARALLEL_OPTION_SHORT = 'p';
    private static final String PARALLEL_OPTION_DESCRIPTION = "Process the atlases in parallel.";

    private static final String WKT_OPTION_LONG = "wkt";
    private static final String WKT_OPTION_DESCRIPTION = "WKT of the polygon with which to cut";
    private static final String WKT_OPTION_HINT = "wkt";

    private static final String CUT_TYPE_OPTION_LONG = "cut-type";
    private static final String CUT_TYPE_OPTION_DESCRIPTION = "The cut-type of this subatlas. Valid settings are: soft_cut, hard_cut_all, hard_cut_relations_only. Defaults to soft_cut.";
    private static final String CUT_TYPE_OPTION_HINT = "type";

    private static final String SUB_ATLAS_SUFFIX = ".sub" + FileSuffix.ATLAS;

    private final OptionAndArgumentFetcher fetcher;
    private final CommandOutputDelegate output;

    public static void main(final String[] args)
    {
        new SubAtlasCommand().runSubcommandAndExit(args);
    }

    public SubAtlasCommand()
    {
        super();
        this.fetcher = this.getOptionAndArgumentFetcher();
        this.output = this.getCommandOutputDelegate();
    }

    @Override
    public int execute()
    {
        final List<File> atlasResourceList = this.getInputAtlasResources();
        if (atlasResourceList.isEmpty())
        {
            this.output.printlnErrorMessage("no input atlases");
            return 1;
        }
        final Stream<File> atlasResourceStream = atlasResourceList.stream();

        final Optional<Path> outputParentPath = this.getOutputPath();
        if (!outputParentPath.isPresent())
        {
            this.output.printlnErrorMessage("invalid output path");
            return 1;
        }

        if (!this.fetcher.hasOption(WKT_OPTION_LONG))
        {
            this.output.printlnErrorMessage("missing required \'--wkt\' option");
            return 1;
        }

        if (this.fetcher.hasOption(PARALLEL_OPTION_LONG))
        {
            atlasResourceStream.parallel();
        }

        final String cutTypeString = this.fetcher.getOptionArgument(CUT_TYPE_OPTION_LONG)
                .orElse("SOFT_CUT");
        final AtlasCutType cutType;
        try
        {
            cutType = AtlasCutType.valueOf(cutTypeString.toUpperCase());
        }
        catch (final IllegalArgumentException exception)
        {
            this.output.printlnErrorMessage("invalid cut type " + cutTypeString);
            this.output.printlnStderr("Try soft_cut, hard_cut_all, or hard_cut_relations_only");
            return 1;
        }

        atlasResourceStream.forEach(fileResource ->
        {
            this.output.printlnStdout(
                    "Subatlasing " + fileResource.getFile().getAbsolutePath() + "...");
            final Optional<Atlas> outputAtlas = processAtlas(fileResource, cutType);
            if (outputAtlas.isPresent())
            {
                final Path filePath = Paths
                        .get(fileResource.getFile().getName() + SUB_ATLAS_SUFFIX);
                final Path concatenatedPath = Paths.get(
                        outputParentPath.get().toAbsolutePath().toString(),
                        filePath.getFileName().toString());
                final File outputFile = new File(concatenatedPath.toAbsolutePath().toString());
                outputAtlas.get().save(outputFile);
                this.output.printlnStdout("Saved to " + outputFile.getFile().getAbsolutePath());
            }
            else
            {
                this.output.printlnWarnMessage("skipping save of empty subatlas cut from "
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
                SubAtlasCommand.class.getResourceAsStream(DESCRIPTION_SECTION));
        addManualPageSection("EXAMPLES",
                SubAtlasCommand.class.getResourceAsStream(EXAMPLES_SECTION));
        super.registerManualPageSections();
    }

    @Override
    public void registerOptionsAndArguments()
    {
        this.registerOptionWithRequiredArgument(WKT_OPTION_LONG, WKT_OPTION_DESCRIPTION,
                WKT_OPTION_HINT, WKT_CONTEXT);
        this.registerOption(PARALLEL_OPTION_LONG, PARALLEL_OPTION_SHORT,
                PARALLEL_OPTION_DESCRIPTION, WKT_CONTEXT);
        this.registerOptionWithRequiredArgument(CUT_TYPE_OPTION_LONG, CUT_TYPE_OPTION_DESCRIPTION,
                CUT_TYPE_OPTION_HINT, WKT_CONTEXT);
        super.registerOptionsAndArguments();
    }

    private Optional<Atlas> processAtlas(final File resource, final AtlasCutType cutType)
    {
        if (this.fetcher.getParserContext() == WKT_CONTEXT)
        {
            final PackedAtlas atlas = new PackedAtlasCloner()
                    .cloneFrom(new AtlasResourceLoader().load(resource));
            final String wkt = this.fetcher.getOptionArgument(WKT_OPTION_LONG)
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
                this.output.printlnErrorMessage("unsupported geometry type " + wkt);
            }
        }
        return Optional.empty();
    }
}
