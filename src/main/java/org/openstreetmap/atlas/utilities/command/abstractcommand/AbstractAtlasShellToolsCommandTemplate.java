package org.openstreetmap.atlas.utilities.command.abstractcommand;

/**
 * @author lcram
 */
public interface AbstractAtlasShellToolsCommandTemplate
{
    /**
     * Register some manual page sections associated with this template.
     *
     * @param parentCommand
     *            the parent {@link AbstractAtlasShellToolsCommand} for this template
     */
    void registerManualPageSections(final AbstractAtlasShellToolsCommand parentCommand);

    /**
     * Register some options and arguments associated with this template.
     *
     * @param parentCommand
     *            the parent {@link AbstractAtlasShellToolsCommand} for this template
     */
    void registerOptionsAndArguments(final AbstractAtlasShellToolsCommand parentCommand);
}
