package org.openstreetmap.atlas.utilities.cli.operations;

import java.io.IOException;
import java.nio.file.Path;
import java.util.stream.Stream;

import org.openstreetmap.atlas.utilities.cli.operations.base.SSHOperation;
import org.openstreetmap.atlas.utilities.cli.operations.base.SSHOperationResults;

/**
 * Implements the HDFS mkdir command over SSH
 *
 * @author cstaylor
 */
public class HDFSMkdirOperation extends AbstractHDFSOperation
{
    @Override
    public HDFSMkdirOperation asUser(final String username)
    {
        super.asUser(username);
        return this;
    }

    public boolean mkdir(final Path... remotePaths) throws InterruptedException, IOException
    {
        if (remotePaths.length == 0)
        {
            throw new IllegalArgumentException("Need at least one remote path");
        }
        final SSHOperation operation = prepareSSH().addArgs("-mkdir", "-p");

        Stream.of(remotePaths).map(Path::toString).map(this::preparePath)
                .forEach(operation::addArgs);

        final SSHOperationResults results = handleResults(this.ssh().execute());
        return results.getReturnValue() == STANDARD_SUCCESS_CODE;
    }

    @Override
    public HDFSMkdirOperation onHost(final String host)
    {
        super.onHost(host);
        return this;
    }

    @Override
    public HDFSMkdirOperation onPort(final int portNumber)
    {
        super.onPort(portNumber);
        return this;
    }

    @Override
    public HDFSMkdirOperation withConfiguration(final String configuration)
    {
        super.withConfiguration(configuration);
        return this;
    }
}
