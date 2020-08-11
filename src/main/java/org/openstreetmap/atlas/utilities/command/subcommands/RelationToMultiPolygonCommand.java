package org.openstreetmap.atlas.utilities.command.subcommands;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.locationtech.jts.geom.MultiPolygon;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.items.ItemType;
import org.openstreetmap.atlas.geography.atlas.items.complex.RelationOrAreaToMultiPolygonConverter;
import org.openstreetmap.atlas.geography.atlas.multi.MultiAtlas;
import org.openstreetmap.atlas.geography.atlas.packed.PackedAtlas;
import org.openstreetmap.atlas.geography.atlas.packed.PackedAtlas.AtlasSerializationFormat;
import org.openstreetmap.atlas.geography.atlas.sub.SubAtlasCreator;
import org.openstreetmap.atlas.geography.converters.jts.JtsMultiPolygonToMultiPolygonConverter;
import org.openstreetmap.atlas.streaming.resource.File;
import org.openstreetmap.atlas.utilities.command.abstractcommand.CommandOutputDelegate;
import org.openstreetmap.atlas.utilities.command.abstractcommand.OptionAndArgumentDelegate;
import org.openstreetmap.atlas.utilities.command.parsing.OptionOptionality;
import org.openstreetmap.atlas.utilities.command.subcommands.templates.AtlasLoaderCommand;

/**
 * @author samg
 */
public class RelationToMultiPolygonCommand extends AtlasLoaderCommand
{
    // maximum WKT string length for terminal printing
    private static final int MAX_LENGTH = 1024 * 150;
    private static final String OUTPUT_ATLAS_FORMAT = "%d.atlas";
    private static final String OUTPUT_WKT_FORMAT = "%d.wkt";

    private static final String RELATION_ID_OPTION_LONG = "id";
    private static final String RELATION_ID_OPTION_DESCRIPTION = "A relation ID to build a multipolygon for";
    private static final String RELATION_ID_OPTION_HINT = "id";

    private static final String OUTPUT_WKT_OPTION_LONG = "wkt";
    private static final String OUTPUT_WKT_OPTION_DESCRIPTION = "Only output WKT file";

    private final OptionAndArgumentDelegate optionAndArgumentDelegate;
    private final CommandOutputDelegate outputDelegate;

    private long relationId;
    private final Set<Atlas> relationAtlases = new HashSet<>();

    public static void main(final String[] args)
    {
        new RelationToMultiPolygonCommand().runSubcommandAndExit(args);
    }

    public RelationToMultiPolygonCommand()
    {
        super();
        this.optionAndArgumentDelegate = this.getOptionAndArgumentDelegate();
        this.outputDelegate = this.getCommandOutputDelegate();
    }

    @Override
    public String getCommandName()
    {
        return "relation2multipolygon";
    }

    @Override
    public String getSimpleDescription()
    {
        return "build a multipolygon from a relation spanning a given set of atlases";
    }

    @Override
    public void registerManualPageSections()
    {
        addManualPageSection("DESCRIPTION", RelationToMultiPolygonCommand.class
                .getResourceAsStream("RelationToMultiPolygonDescriptionSection.txt"));
        addManualPageSection("EXAMPLES", RelationToMultiPolygonCommand.class
                .getResourceAsStream("RelationToMultiPolygonCommandExamplesSection.txt"));
        super.registerManualPageSections();
    }

    @Override
    public void registerOptionsAndArguments()
    {
        registerOptionWithRequiredArgument(RELATION_ID_OPTION_LONG, RELATION_ID_OPTION_DESCRIPTION,
                OptionOptionality.REQUIRED, RELATION_ID_OPTION_HINT);
        registerOption(OUTPUT_WKT_OPTION_LONG, OUTPUT_WKT_OPTION_DESCRIPTION,
                OptionOptionality.OPTIONAL);
        super.registerOptionsAndArguments();
    }

    @Override
    public int start()
    {
        this.relationId = this.optionAndArgumentDelegate
                .getOptionArgument(RELATION_ID_OPTION_LONG, Long::valueOf).orElse(-1L);

        if (this.relationId == -1L)
        {
            this.outputDelegate.printlnErrorMessage("invalid relation identifier!");
            return -1;
        }
        return 0;
    }

    @Override
    protected int finish()
    {
        if (this.relationAtlases.isEmpty())
        {
            this.outputDelegate.printlnErrorMessage("no atlases found with relation");
            return 1;
        }

        final MultiAtlas multiAtlas = new MultiAtlas(this.relationAtlases);
        final MultiPolygon relationMultiPolygon;
        final org.openstreetmap.atlas.geography.MultiPolygon atlasMultiPolygon;
        try
        {
            final RelationOrAreaToMultiPolygonConverter converter = new RelationOrAreaToMultiPolygonConverter();
            atlasMultiPolygon = converter.convert(multiAtlas.relation(this.relationId));
            if (!atlasMultiPolygon.isOGCValid())
            {
                this.outputDelegate
                        .printlnWarnMessage("warning: relation multipolygon is not OGC valid!");
                if (!atlasMultiPolygon.isOSMValid())
                {
                    this.outputDelegate.printlnErrorMessage(
                            "warning: relation multipolygon is not OSM valid, cannot construct WKT!");
                    return 1;
                }
            }
            final JtsMultiPolygonToMultiPolygonConverter jtsconverter = new JtsMultiPolygonToMultiPolygonConverter();
            relationMultiPolygon = jtsconverter.backwardConvert(atlasMultiPolygon);
        }
        catch (final Exception exception)
        {
            this.outputDelegate
                    .printlnErrorMessage("error building valid multipolygon from relation: ");
            this.outputDelegate.printlnErrorMessage(exception.getMessage());
            return 1;
        }

        if (relationMultiPolygon.toText().length() < MAX_LENGTH)
        {
            this.outputDelegate.printlnCommandMessage("Relation WKT:");
            this.outputDelegate.printlnCommandMessage(relationMultiPolygon.toText());
        }
        else
        {
            this.outputDelegate.printlnWarnMessage("Relation WKT is too large to print!");
        }

        final Path concatenatedWktPath = Paths.get(getOutputPath().toAbsolutePath().toString(),
                String.format(OUTPUT_WKT_FORMAT, this.relationId));

        final File wktOutputFile = new File(concatenatedWktPath.toAbsolutePath().toString(),
                this.getFileSystem());
        wktOutputFile.writeAndClose(relationMultiPolygon.toText());

        if (!this.optionAndArgumentDelegate.hasOption(OUTPUT_WKT_OPTION_LONG))
        {
            final Path concatenatedPath = Paths.get(getOutputPath().toAbsolutePath().toString(),
                    String.format(OUTPUT_ATLAS_FORMAT, this.relationId));
            final File outputFile = new File(concatenatedPath.toAbsolutePath().toString(),
                    this.getFileSystem());
            final PackedAtlas outputAtlas = multiAtlas.cloneToPackedAtlas();
            outputAtlas.setSaveSerializationFormat(AtlasSerializationFormat.PROTOBUF);
            outputAtlas.save(outputFile);
            if (this.optionAndArgumentDelegate.hasVerboseOption())
            {
                this.outputDelegate
                        .printlnCommandMessage("saved atlas to " + concatenatedPath.toString());
            }
        }

        return 0;
    }

    @Override
    protected void processAtlas(final Atlas atlas, final String atlasFileName,
            final File atlasResource)
    {
        if (atlas.relation(this.relationId) != null)
        {
            final Optional<Atlas> relationSubAtlas = SubAtlasCreator.softCut(atlas,
                    entity -> entity.getType().equals(ItemType.RELATION)
                            && entity.getIdentifier() == this.relationId);
            if (relationSubAtlas.isPresent())
            {
                this.relationAtlases.add(relationSubAtlas.get());
            }
        }
    }
}
