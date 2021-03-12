package org.openstreetmap.atlas.utilities.command.subcommands;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.openstreetmap.atlas.geography.Polygon;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.geography.atlas.sub.AtlasCutType;
import org.openstreetmap.atlas.geography.converters.jts.JtsPolygonConverter;
import org.openstreetmap.atlas.streaming.resource.File;
import org.openstreetmap.atlas.streaming.resource.FileSuffix;
import org.openstreetmap.atlas.utilities.collections.StringList;
import org.openstreetmap.atlas.utilities.command.abstractcommand.CommandOutputDelegate;
import org.openstreetmap.atlas.utilities.command.abstractcommand.OptionAndArgumentDelegate;
import org.openstreetmap.atlas.utilities.command.parsing.OptionOptionality;
import org.openstreetmap.atlas.utilities.command.subcommands.templates.AtlasLoaderCommand;
import org.openstreetmap.atlas.utilities.command.subcommands.templates.PredicateTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author lcram
 */
public class SubAtlasCommand extends AtlasLoaderCommand
{
    private static final Logger logger = LoggerFactory.getLogger(SubAtlasCommand.class);

    private static final String POLYGON_OPTION_LONG = "polygon";
    private static final String POLYGON_OPTION_DESCRIPTION = "The WKT of the polygon with which to cut.";
    private static final String POLYGON_OPTION_HINT = "wkt";

    private static final List<String> CUT_TYPE_STRINGS = Arrays.stream(AtlasCutType.values())
            .map(AtlasCutType::toString).collect(Collectors.toList());
    private static final String CUT_TYPE_OPTION_LONG = "cut-type";
    private static final String CUT_TYPE_OPTION_DESCRIPTION = "The cut-type of this subatlas. Valid settings are: "
            + new StringList(CUT_TYPE_STRINGS).join(", ") + ". Defaults to SOFT_CUT.";
    private static final String CUT_TYPE_OPTION_HINT = "type";

    private static final String SLICE_FIRST_OPTION_LONG = "slice-first";
    private static final String SLICE_FIRST_OPTION_DESCRIPTION = "Cut with supplied geometry before applying the predicate.";

    private static final List<String> IMPORTS_ALLOW_LIST = Arrays.asList(
            "org.openstreetmap.atlas.geography.atlas.items",
            "org.openstreetmap.atlas.tags.annotations",
            "org.openstreetmap.atlas.tags.annotations.validation",
            "org.openstreetmap.atlas.tags.annotations.extraction", "org.openstreetmap.atlas.tags",
            "org.openstreetmap.atlas.tags.names", "org.openstreetmap.atlas.geography",
            "org.openstreetmap.atlas.utilities.collections");

    private final OptionAndArgumentDelegate optionAndArgumentDelegate;
    private final CommandOutputDelegate outputDelegate;

    private AtlasCutType cutType;
    private Polygon polygon;
    private Predicate<AtlasEntity> matcher;

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
        registerManualPageSectionsFromTemplate(new PredicateTemplate());
        super.registerManualPageSections();
    }

    @Override
    public void registerOptionsAndArguments()
    {
        this.registerOptionWithRequiredArgument(POLYGON_OPTION_LONG, POLYGON_OPTION_DESCRIPTION,
                OptionOptionality.OPTIONAL, POLYGON_OPTION_HINT);
        this.registerOptionsAndArgumentsFromTemplate(new PredicateTemplate());
        this.registerOptionWithRequiredArgument(CUT_TYPE_OPTION_LONG, CUT_TYPE_OPTION_DESCRIPTION,
                OptionOptionality.OPTIONAL, CUT_TYPE_OPTION_HINT);
        this.registerOption(SLICE_FIRST_OPTION_LONG, SLICE_FIRST_OPTION_DESCRIPTION,
                OptionOptionality.OPTIONAL);
        super.registerOptionsAndArguments();
    }

    @Override
    protected void processAtlas(final Atlas atlas, final String atlasFileName,
            final File atlasResource)
    {
        Optional<Atlas> subbedAtlas = Optional.empty();

        if (this.optionAndArgumentDelegate.hasOption(SLICE_FIRST_OPTION_LONG))
        {
            if (this.polygon != null)
            {
                subbedAtlas = atlas.subAtlas(this.polygon, this.cutType);
            }

            if (this.matcher != null)
            {
                subbedAtlas = subbedAtlas.orElse(atlas).subAtlas(this.matcher, this.cutType);
            }
        }
        else
        {
            if (this.matcher != null)
            {
                subbedAtlas = atlas.subAtlas(this.matcher, this.cutType);
            }

            if (this.polygon != null)
            {
                subbedAtlas = subbedAtlas.orElse(atlas).subAtlas(this.polygon, this.cutType);
            }
        }

        if (subbedAtlas.isPresent())
        {
            final String fileName = "sub_"
                    + AtlasLoaderCommand.removeSuffixFromFileName(atlasFileName);
            final Path concatenatedPath = Paths.get(getOutputPath().toAbsolutePath().toString(),
                    fileName);
            final File outputFile = new File(
                    concatenatedPath.toAbsolutePath().toString() + FileSuffix.ATLAS,
                    this.getFileSystem());
            subbedAtlas.get().save(outputFile);
            if (this.optionAndArgumentDelegate.hasVerboseOption())
            {
                this.outputDelegate
                        .printlnCommandMessage("saved to " + outputFile.getAbsolutePathString());
            }
        }
        else
        {
            this.outputDelegate.printlnWarnMessage(
                    "skipping save of empty subatlas cut from " + atlasResource.getPathString());
        }
    }

    @Override
    protected int start()
    {
        final Optional<String> cutTypeParameter = this.optionAndArgumentDelegate
                .getOptionArgument(CUT_TYPE_OPTION_LONG);
        final Optional<String> wktParameter = this.optionAndArgumentDelegate
                .getOptionArgument(POLYGON_OPTION_LONG);

        if (cutTypeParameter.isPresent())
        {
            try
            {
                this.cutType = AtlasCutType.valueOf(cutTypeParameter.get().toUpperCase());
            }
            catch (final IllegalArgumentException exception)
            {
                this.outputDelegate
                        .printlnErrorMessage("invalid cut type " + cutTypeParameter.get());
                this.outputDelegate
                        .printlnStderr("Try " + new StringList(CUT_TYPE_STRINGS).join(", "));
                return 1;
            }
        }

        if (wktParameter.isPresent())
        {
            final WKTReader reader = new WKTReader();
            Geometry geometry = null;
            try
            {
                geometry = reader.read(wktParameter.get());
            }
            catch (final ParseException exception)
            {
                logger.error("unable to parse {}", wktParameter.get(), exception);
                return 1;
            }

            if (geometry instanceof org.locationtech.jts.geom.Polygon)
            {
                this.polygon = new JtsPolygonConverter()
                        .backwardConvert((org.locationtech.jts.geom.Polygon) geometry);
            }
            else
            {
                this.outputDelegate
                        .printlnErrorMessage("unsupported geometry type " + wktParameter.get());
                return 1;
            }
        }
        this.matcher = PredicateTemplate.getPredicate(AtlasEntity.class, IMPORTS_ALLOW_LIST, this)
                .orElse(null);

        return 0;
    }
}
