package org.openstreetmap.atlas.geography.atlas.delta;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.atlas.AbstractAtlas;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.AtlasResourceLoader;
import org.openstreetmap.atlas.streaming.resource.File;
import org.openstreetmap.atlas.streaming.resource.FileSuffix;
import org.openstreetmap.atlas.utilities.conversion.StringConverter;
import org.openstreetmap.atlas.utilities.runtime.Command;
import org.openstreetmap.atlas.utilities.runtime.CommandMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author matthieun
 */
public class AtlasDeltaGenerator extends Command
{
    private static final Switch<String> BASE_SWITCH = new Switch<>("base",
            "The name of the base Atlas", StringConverter.IDENTITY, Optionality.REQUIRED);

    private static final Switch<String> ALTER_SWITCH = new Switch<>("alter",
            "The name of the alter Atlas", StringConverter.IDENTITY, Optionality.REQUIRED);

    private static final Switch<File> OUTPUT_FOLDER_SWITCH = new Switch<>("outputFolder",
            "The path of the output folder", string -> new File(string), Optionality.REQUIRED);

    private final Logger logger;

    public static void main(final String[] args)
    {
        new AtlasDeltaGenerator(LoggerFactory.getLogger(AtlasDeltaGenerator.class)).run(args);
    }

    public AtlasDeltaGenerator(final Logger logger)
    {
        this.logger = logger;
    }

    @Override
    protected int onRun(final CommandMap command)
    {
        final String baseName = (String) command.get("base");
        final String alterName = (String) command.get("alter");
        final File outputFolder = (File) command.get("outputFolder");
        run(baseName, alterName, outputFolder);
        return 0;
    }

    @Override
    protected SwitchList switches()
    {
        return new SwitchList().with(BASE_SWITCH, ALTER_SWITCH, OUTPUT_FOLDER_SWITCH);
    }

    private void compare(final String baseName, final Atlas base, final String alterName,
            final Atlas alter, final String folder)
    {
        final AtlasDelta delta = new AtlasDelta(base, alter).generate();
        final File diffFile = new File(folder)
                .child(baseName + "_vs_" + alterName + FileSuffix.TEXT.toString());
        diffFile.writeAndClose(delta.toString());
        this.logger.info("Saved diff file {}", diffFile);
        final File geoJsonFile = new File(folder)
                .child(baseName + "_vs_" + alterName + FileSuffix.GEO_JSON.toString());
        geoJsonFile.writeAndClose(delta.toGeoJson());
        this.logger.info("Saved geoJson file {}", geoJsonFile);
        final File relationsGeoJsonFile = new File(folder)
                .child(baseName + "_vs_" + alterName + "_relations.geojson");
        relationsGeoJsonFile.writeAndClose(delta.toRelationsGeoJson());
        this.logger.info("Saved relationsGeoJsonFile file {}", relationsGeoJsonFile);
    }

    private Atlas load(final String atlasName, final File outputFolder)
    {
        final Atlas atlas;
        final File file = outputFolder.child(atlasName + FileSuffix.ATLAS.toString());
        if (file.getFile().exists())
        {
            atlas = new AtlasResourceLoader().load(file);
        }
        else
        {
            final File pbfFile = outputFolder.child(atlasName + FileSuffix.PBF.toString());
            final File geoJsonFile = outputFolder.child(atlasName + FileSuffix.GEO_JSON.toString());
            final File listFile = outputFolder.child(atlasName + FileSuffix.TEXT.toString());
            if (pbfFile.getFile().exists())
            {
                atlas = AbstractAtlas.createAndSaveOsmPbf(pbfFile, file);
                atlas.saveAsGeoJson(geoJsonFile);
                atlas.saveAsList(listFile);
            }
            else
            {
                throw new CoreException("Neither " + file + " nor " + pbfFile + " exist.");
            }
        }
        return atlas;
    }

    private void run(final String baseName, final String alterName, final File outputFolder)
    {
        this.logger.info("Comparing {} and {}", baseName, alterName);
        final Atlas base = load(baseName, outputFolder);
        final Atlas alter = load(alterName, outputFolder);
        compare(baseName, base, alterName, alter, outputFolder.getPath());
    }
}
