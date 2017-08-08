package org.openstreetmap.atlas.utilities.cli.operations.base;

import java.io.IOException;
import java.net.ServerSocket;

import org.openstreetmap.atlas.exception.CoreException;

/**
 * Simple utility class that will try and find a socket to bind to and return that port number after
 * the socket has been closed
 *
 * @author cstaylor
 */
final class AvailableSocketFinder
{
    static int takePort()
    {
        Integer returnValue = null;
        try
        {
            final ServerSocket socket = new ServerSocket(0);
            returnValue = socket.getLocalPort();
            socket.close();
        }
        catch (final IOException oops)
        {
            throw new CoreException("Error when trying to reserve a port", oops);
        }
        return returnValue;
    }

    private AvailableSocketFinder()
    {

    }
}
