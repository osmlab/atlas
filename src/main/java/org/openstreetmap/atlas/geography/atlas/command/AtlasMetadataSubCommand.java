package org.openstreetmap.atlas.geography.atlas.command;

import java.io.PrintStream;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.AtlasMetaData;
import org.openstreetmap.atlas.utilities.runtime.CommandMap;

/**
 * Reads the metadata from the input atlas files and writes it to stdout
 *
 * @author cstaylor
 */
public class AtlasMetadataSubCommand extends AbstractAtlasSubCommand
{
    public AtlasMetadataSubCommand()
    {
        super("metadata", "outputs all of the metadata found in the atlas files");
    }

    @Override
    public void usage(final PrintStream writer)
    {
        writer.printf(AtlasCommandConstants.INPUT_PARAMETER_DESCRIPTION);
        writer.printf(
                "-combine : merge all of the atlas files into a MultiAtlas before outputting geojson\n");
    }

    @Override
    protected void handle(final Atlas atlas, final CommandMap command)
    {
        final AtlasMetaData metaData = atlas.metaData();
        System.out.printf("Atlas Meta Data for %s:\n%s\n", atlas.getName(),
                metaData.toReadableString());
    }
}
