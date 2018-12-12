package org.openstreetmap.atlas.utilities.command;

import org.openstreetmap.atlas.exception.CoreException;

/**
 * @author lcram
 */
public class AtlasShellToolsException extends CoreException
{
    private static final long serialVersionUID = -2538051525989047548L;

    public AtlasShellToolsException()
    {
        super("this should never happen");
    }
}
