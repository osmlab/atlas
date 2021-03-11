package org.openstreetmap.atlas.utilities.command.abstractcommand;

import org.openstreetmap.atlas.utilities.command.subcommands.templates.ListOfNumbersTemplate;

/**
 * An {@link AbstractAtlasShellToolsCommandTemplate} provides an easy way for implementations of
 * {@link AbstractAtlasShellToolsCommand} to share options, arguments, man page sections, and common
 * functionality. For example, by using a template implementation, command authors will not need to
 * re-declare a common option (with the accompanying duplicated option parsing code) across many
 * different commands. See {@link ListOfNumbersTemplate} for an example implementation.
 *
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
    void registerManualPageSections(AbstractAtlasShellToolsCommand parentCommand);

    /**
     * Register some options and arguments associated with this template.
     *
     * @param parentCommand
     *            the parent {@link AbstractAtlasShellToolsCommand} for this template
     */
    void registerOptionsAndArguments(AbstractAtlasShellToolsCommand parentCommand);
}
