package org.openstreetmap.atlas.utilities.command;

import org.openstreetmap.atlas.exception.CoreException;

/**
 * A special core exception for cases that should never happen. If users see this, it's a bug!
 *
 * @author lcram
 */
public class AtlasShellToolsException extends CoreException
{
    private static final long serialVersionUID = -2538051525989047548L;

    public AtlasShellToolsException()
    {
        super("This should never happen, you found a bug! Please report this stack trace, and the command line that caused it.");
    }
}
