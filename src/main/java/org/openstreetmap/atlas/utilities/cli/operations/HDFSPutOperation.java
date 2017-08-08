package org.openstreetmap.atlas.utilities.cli.operations;

import java.io.IOException;
import java.nio.file.Path;

import org.openstreetmap.atlas.utilities.cli.operations.base.SSHOperationResults;

/**
 * Implements the HDFS put command (copy a file from the local filesystem into HDFS) over SSH
 *
 * @author cstaylor
 */
public class HDFSPutOperation extends AbstractHDFSOperation
{
    @Override
    public HDFSPutOperation asUser(final String username)
    {
        super.asUser(username);
        return this;
    }

    @Override
    public HDFSPutOperation onHost(final String host)
    {
        super.onHost(host);
        return this;
    }

    @Override
    public HDFSPutOperation onPort(final int portNumber)
    {
        super.onPort(portNumber);
        return this;
    }

    public SSHOperationResults put(final Path source, final Path destination)
            throws InterruptedException, IOException
    {
        if (source == null)
        {
            throw new IllegalArgumentException("source can't be null");
        }
        if (destination == null)
        {
            throw new IllegalArgumentException("destination can't be null");
        }
        prepareSSH().addArgs("-put", preparePath(source.toString()),
                preparePath(destination.toString()));
        return handleResults(this.ssh().execute());
    }

    @Override
    public HDFSPutOperation withConfiguration(final String configuration)
    {
        super.withConfiguration(configuration);
        return this;
    }
}
