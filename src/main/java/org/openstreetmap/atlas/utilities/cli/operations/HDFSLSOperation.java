package org.openstreetmap.atlas.utilities.cli.operations;

import java.io.IOException;
import java.nio.file.Path;
import java.util.stream.Stream;

import org.openstreetmap.atlas.utilities.cli.operations.base.SSHOperationResults;

/**
 * Implements the HDFS ls command over SSH and returns the file names, not the absolute paths
 *
 * @author cstaylor
 */
public class HDFSLSOperation extends AbstractHDFSOperation
{
    @Override
    public HDFSLSOperation asUser(final String username)
    {
        super.asUser(username);
        return this;
    }

    public Stream<String> list(final Path remotePath) throws InterruptedException, IOException
    {
        if (remotePath == null)
        {
            throw new IllegalArgumentException("remotePath can't be null");
        }
        prepareSSH().addArgs("-ls", preparePath(remotePath.toString()));
        final SSHOperationResults results = handleResults(this.ssh().execute());
        return Stream.of(results.getOutput().split("\n")).filter(i -> i.indexOf('/') != -1)
                .map(line ->
                {
                    final String[] pieces = line.split(" ");
                    return pieces[pieces.length - 1];
                });
    }

    @Override
    public HDFSLSOperation onHost(final String host)
    {
        super.onHost(host);
        return this;
    }

    @Override
    public HDFSLSOperation onPort(final int portNumber)
    {
        super.onPort(portNumber);
        return this;
    }

    @Override
    public HDFSLSOperation withConfiguration(final String configuration)
    {
        super.withConfiguration(configuration);
        return this;
    }
}
