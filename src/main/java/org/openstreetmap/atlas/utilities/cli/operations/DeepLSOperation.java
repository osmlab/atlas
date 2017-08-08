package org.openstreetmap.atlas.utilities.cli.operations;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.utilities.cli.operations.base.SSHOperationResults;

/**
 * Uses find on a remote system to find all files from a search path and returns them as a list of
 * Path objects
 *
 * @author cstaylor
 */
public class DeepLSOperation extends AbstractOperation
{
    @Override
    public DeepLSOperation asUser(final String username)
    {
        super.asUser(username);
        return this;
    }

    public List<Path> list(final Path remotePath) throws InterruptedException, IOException
    {
        if (remotePath == null)
        {
            throw new IllegalArgumentException("remotePath can't be null");
        }
        this.ssh().addArgs("find", remotePath.toString(), "-type", "f");
        final SSHOperationResults results = handleResults(this.ssh().execute());
        if (results.getReturnValue() == 1)
        {
            throw new CoreException("Error: {}@{}:{} doesn't exist", getUser(), getHost(),
                    remotePath);
        }
        return Arrays.asList(results.getOutput().split("\n")).stream()
                .map(child -> remotePath.resolve(child)).collect(Collectors.toList());
    }

    @Override
    public DeepLSOperation onHost(final String host)
    {
        super.onHost(host);
        return this;
    }

    @Override
    public DeepLSOperation onPort(final int portNumber)
    {
        super.onPort(portNumber);
        return this;
    }
}
