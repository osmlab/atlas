package org.openstreetmap.atlas;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.AtlasResourceLoader;
import org.openstreetmap.atlas.geography.atlas.items.Point;
import org.openstreetmap.atlas.streaming.resource.File;
import org.openstreetmap.atlas.streaming.resource.FileSuffix;
import org.openstreetmap.atlas.utilities.command.abstractcommand.AbstractAtlasShellToolsCommand;
import org.openstreetmap.atlas.utilities.command.parsing.ArgumentArity;
import org.openstreetmap.atlas.utilities.command.parsing.ArgumentOptionality;
import org.openstreetmap.atlas.utilities.scalars.Duration;
import org.openstreetmap.atlas.utilities.time.Time;

/**
 * @author lcram
 */
public class ZipTestingCommand extends AbstractAtlasShellToolsCommand
{
    public static void main(final String[] args)
    {
        new ZipTestingCommand().runSubcommandAndExit(args);
    }

    @Override
    public int execute()
    {
        for (final String fileName : this.getOptionAndArgumentDelegate()
                .getVariadicArgument("files"))
        {
            final File file = new File(fileName, this.getFileSystem());
            this.getCommandOutputDelegate()
                    .printlnCommandMessage("about to read " + file.getAbsolutePathString());
            if (FileSuffix.pathFilter(FileSuffix.ATLAS).test(file.toAbsolutePath()) || FileSuffix
                    .pathFilter(FileSuffix.ATLAS, FileSuffix.TEXT).test(file.toAbsolutePath()))
            {
                final Atlas atlas = new AtlasResourceLoader().load(file);
                final Time now = Time.now();
                final Point point = atlas.point(1L);
                final Duration timeToRead = now.elapsedSince();
                this.getCommandOutputDelegate()
                        .printlnCommandMessage("was an atlas, Point 1L is " + point);
                this.getCommandOutputDelegate()
                        .printlnCommandMessage("took " + timeToRead.asMilliseconds()
                                + "ms to read point data from file of size " + file.length());
            }
            else
            {
                final Time now = Time.now();
                final String contents = file.readAndClose();
                final Duration timeToRead = now.elapsedSince();
                this.getCommandOutputDelegate()
                        .printlnCommandMessage("was a file, contents:\n" + contents);
                this.getCommandOutputDelegate()
                        .printlnCommandMessage("took " + timeToRead.asMilliseconds()
                                + "ms to read file data from file of size " + file.length());
            }

            // sleep for 3 seconds to simulate a slow operation
            this.getCommandOutputDelegate().printlnCommandMessage("sleeping for 3 sec...");
            try
            {
                Thread.sleep(3000);
            }
            catch (final InterruptedException exception)
            {
                throw new CoreException("Interrupted", exception);
            }
        }

        return 0;
    }

    @Override
    public String getCommandName()
    {
        return "zip-fd-test";
    }

    @Override
    public String getSimpleDescription()
    {
        return "for testing resource closing";
    }

    @Override
    public void registerManualPageSections()
    {

    }

    @Override
    public void registerOptionsAndArguments()
    {
        registerArgument("files", ArgumentArity.VARIADIC, ArgumentOptionality.REQUIRED);
        super.registerOptionsAndArguments();
    }
}
