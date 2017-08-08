package org.openstreetmap.atlas.utilities.cli.operations;

import java.io.IOException;
import java.nio.file.Path;

import org.openstreetmap.atlas.utilities.cli.operations.base.SSHOperationResults;

/**
 * This command checks if a file exists on a remote server via SSH
 *
 * @author cstaylor
 */
public class CheckIfFileExistsOperation extends AbstractOperation
{
    @Override
    public CheckIfFileExistsOperation asUser(final String username)
    {
        super.asUser(username);
        return this;
    }

    public boolean exists(final Path remotePath) throws InterruptedException, IOException
    {
        if (remotePath == null)
        {
            throw new IllegalArgumentException("remotePath can't be null");
        }
        this.ssh().addArgs("stat", remotePath.toString());
        final SSHOperationResults results = handleResults(this.ssh().execute());
        return !results.getOutput().contains("No such file or directory");
    }

    @Override
    public CheckIfFileExistsOperation onHost(final String host)
    {
        super.onHost(host);
        return this;
    }

    @Override
    public CheckIfFileExistsOperation onPort(final int portNumber)
    {
        super.onPort(portNumber);
        return this;
    }
}
