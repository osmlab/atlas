package org.openstreetmap.atlas.utilities.cli.operations;

import java.io.IOException;
import java.nio.file.Path;

import org.openstreetmap.atlas.utilities.cli.operations.base.SSHOperationResults;

/**
 * Implements the HDFS stat command so we can check if a file exists in a remote HDFS cluster
 *
 * @author cstaylor
 */
public class HDFSCheckIfFileExistsOperation extends AbstractHDFSOperation
{
    @Override
    public HDFSCheckIfFileExistsOperation asUser(final String username)
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
        prepareSSH().addArgs("-stat", preparePath(remotePath.toString()));
        final SSHOperationResults results = handleResults(this.ssh().execute());
        return results.getReturnValue() == STANDARD_SUCCESS_CODE;
    }

    @Override
    public HDFSCheckIfFileExistsOperation onHost(final String host)
    {
        super.onHost(host);
        return this;
    }

    @Override
    public HDFSCheckIfFileExistsOperation onPort(final int portNumber)
    {
        super.onPort(portNumber);
        return this;
    }

    @Override
    public HDFSCheckIfFileExistsOperation withConfiguration(final String configuration)
    {
        super.withConfiguration(configuration);
        return this;
    }
}
