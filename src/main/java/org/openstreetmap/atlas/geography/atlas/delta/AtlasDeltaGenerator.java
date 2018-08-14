package org.openstreetmap.atlas.geography.atlas.delta;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.AtlasResourceLoader;
import org.openstreetmap.atlas.streaming.resource.File;
import org.openstreetmap.atlas.streaming.resource.FileSuffix;
import org.openstreetmap.atlas.utilities.runtime.Command;
import org.openstreetmap.atlas.utilities.runtime.CommandMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author matthieun
 * @author nhallahan
 */
public class AtlasDeltaGenerator extends Command
{
    private static final Switch<Path> BEFORE_SWITCH = new Switch<>("before",
            "The before atlas from which to delta.", Paths::get, Optionality.REQUIRED);

    private static final Switch<Path> AFTER_SWITCH = new Switch<>("after",
            "The after atlas that the before atlas deltas to.", Paths::get, Optionality.REQUIRED);

    private static final Switch<Path> OUTPUT_DIR_SWITCH = new Switch<>("outputDir",
            "The path of the output directory.", Paths::get, Optionality.REQUIRED);

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
        final Path before = (Path) command.get("before");
        final Path after = (Path) command.get("after");
        final Path outputDir = (Path) command.get("outputDir");
        run(before, after, outputDir);
        return 0;
    }

    @Override
    protected SwitchList switches()
    {
        return new SwitchList().with(BEFORE_SWITCH, AFTER_SWITCH, OUTPUT_DIR_SWITCH);
    }

    private void compare(final Atlas beforeAtlas, final Atlas afterAtlas, final Path outputDir)
    {
        final String name = beforeAtlas.getName().split(".atlas")[0];

        final AtlasDelta delta = new AtlasDelta(beforeAtlas, afterAtlas).generate();

        final String txt = delta.toDiffViewFriendlyString();
        final File txtFile = new File(outputDir.resolve(name + FileSuffix.TEXT.toString()).toFile());
        txtFile.writeAndClose(txt);
        this.logger.info("Saved txt file {}", txtFile);

        final String geoJson = delta.toGeoJson();
        final File geoJsonFile = new File(outputDir.resolve(name + FileSuffix.GEO_JSON.toString()).toFile());
        geoJsonFile.writeAndClose(geoJson);
        this.logger.info("Saved GeoJSON file {}", geoJsonFile);

        final String relationsGeoJson = delta.toRelationsGeoJson();
        final String relationsGeoJsonFileName = name + "_relations" + FileSuffix.GEO_JSON.toString();
        final File relationsGeoJsonFile = new File(outputDir.resolve(relationsGeoJsonFileName).toFile());
        relationsGeoJsonFile.writeAndClose(relationsGeoJson);
        this.logger.info("Saved Relations GeoJSON file {}", relationsGeoJsonFile);
    }

    private Atlas load(final Path path)
    {
        final File file = new File(path.toFile());
        return new AtlasResourceLoader().load(file);
    }

    private void run(final Path before, final Path after, final Path outputDir)
    {
        this.logger.info("Comparing {} and {}", before, after);
        final Atlas beforeAtlas = load(before);
        final Atlas afterAtlas = load(after);
        compare(beforeAtlas, afterAtlas, outputDir);
    }
}
