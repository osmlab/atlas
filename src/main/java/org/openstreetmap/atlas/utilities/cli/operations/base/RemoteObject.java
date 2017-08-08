package org.openstreetmap.atlas.utilities.cli.operations.base;

import java.nio.file.Path;

/**
 * When making a transfer call via SCP, a RemoteObject is something to read or write from a remote
 * machine.
 *
 * @author cstaylor
 */
public class RemoteObject
{
    /**
     * SSH connect string
     */
    private final String connectString;

    public RemoteObject(final String username, final String hostname, final Path path)
    {
        this.connectString = String.format("%s@%s:%s", username, hostname, path.toString());
    }

    /**
     * @return a valid parameter for a remote file resource (username@hostname:path) to be used with
     *         SCP
     */
    @Override
    public String toString()
    {
        return this.connectString;
    }
}
