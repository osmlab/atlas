package org.openstreetmap.atlas.utilities.cli.operations;

import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Collectors;

import org.openstreetmap.atlas.utilities.cli.operations.base.SSHOperationResults;

/**
 * Implements the HDFS cat command (dump contents of files in HDFS to stdout) over SSH
 *
 * @author cstaylor
 */
public class HDFSCatOperation extends AbstractHDFSOperation
{
    @Override
    public HDFSCatOperation asUser(final String username)
    {
        super.asUser(username);
        return this;
    }

    public SSHOperationResults cat(final String... paths) throws InterruptedException, IOException
    {
        if (paths.length == 0)
        {
            throw new IllegalArgumentException("source can't be null");
        }

        final String pathsAsString = Arrays.asList(paths).stream().map(this::preparePath)
                .collect(Collectors.joining(" "));

        prepareSSH().addArgs("-cat", pathsAsString);
        return handleResults(this.ssh().execute());
    }

    @Override
    public HDFSCatOperation onHost(final String host)
    {
        super.onHost(host);
        return this;
    }

    @Override
    public HDFSCatOperation onPort(final int portNumber)
    {
        super.onPort(portNumber);
        return this;
    }

    @Override
    public HDFSCatOperation withConfiguration(final String configuration)
    {
        super.withConfiguration(configuration);
        return this;
    }
}
