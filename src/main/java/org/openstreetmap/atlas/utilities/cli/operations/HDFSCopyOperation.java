package org.openstreetmap.atlas.utilities.cli.operations;

import java.io.IOException;
import java.nio.file.Path;

import org.openstreetmap.atlas.utilities.cli.operations.base.SSHOperationResults;

/**
 * Implements the HDFS copy command (copy files from one area of HDFS to another) over SSH
 *
 * @author cstaylor
 */
public class HDFSCopyOperation extends AbstractHDFSOperation
{
    @Override
    public HDFSCopyOperation asUser(final String username)
    {
        super.asUser(username);
        return this;
    }

    public SSHOperationResults copy(final Path source, final Path destination)
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
        prepareSSH().addArgs("-cp", preparePath(source.toString()),
                preparePath(destination.toString()));

        return handleResults(this.ssh().execute());
    }

    @Override
    public HDFSCopyOperation onHost(final String host)
    {
        super.onHost(host);
        return this;
    }

    @Override
    public HDFSCopyOperation onPort(final int portNumber)
    {
        super.onPort(portNumber);
        return this;
    }

    @Override
    public HDFSCopyOperation withConfiguration(final String configuration)
    {
        super.withConfiguration(configuration);
        return this;
    }
}
