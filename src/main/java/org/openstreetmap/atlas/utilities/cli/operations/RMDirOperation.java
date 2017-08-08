package org.openstreetmap.atlas.utilities.cli.operations;

import java.io.IOException;
import java.nio.file.Path;

import org.openstreetmap.atlas.utilities.cli.operations.base.SSHOperationResults;

/**
 * Removes a directory from a remote server over SSH.
 * <p>
 * See that rm -rf? Be very careful...
 *
 * @author cstaylor
 */
public class RMDirOperation extends AbstractOperation
{
    @Override
    public RMDirOperation asUser(final String username)
    {
        super.asUser(username);
        return this;
    }

    @Override
    public RMDirOperation onHost(final String host)
    {
        super.onHost(host);
        return this;
    }

    @Override
    public RMDirOperation onPort(final int portNumber)
    {
        super.onPort(portNumber);
        return this;
    }

    public boolean rmdir(final Path remotePath) throws InterruptedException, IOException
    {
        if (remotePath == null)
        {
            throw new IllegalArgumentException("remotePath can't be null");
        }

        /**
         * Not the best sanity check in the world.
         */
        if (remotePath.toString().contains("*"))
        {
            throw new IllegalArgumentException("Please, be careful with the asterisks");
        }
        this.ssh().addArgs("rm", "-rf", remotePath.toString());
        final SSHOperationResults results = handleResults(this.ssh().execute());
        return results.getReturnValue() == STANDARD_SUCCESS_CODE;
    }
}
