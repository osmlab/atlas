package org.openstreetmap.atlas.utilities.cli.operations;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import org.openstreetmap.atlas.utilities.cli.operations.base.SSHOperationResults;

/**
 * Returns the list of filenames from a directory on a remote server over SSH
 *
 * @author cstaylor
 */
public class LSOperation extends AbstractOperation
{
    @Override
    public LSOperation asUser(final String username)
    {
        super.asUser(username);
        return this;
    }

    public List<String> list(final Path remotePath) throws InterruptedException, IOException
    {
        if (remotePath == null)
        {
            throw new IllegalArgumentException("remotePath can't be null");
        }
        this.ssh().addArgs("ls", "-1", remotePath.toString());
        final SSHOperationResults results = handleResults(this.ssh().execute());
        return Arrays.asList(results.getOutput().split("\n"));
    }

    @Override
    public LSOperation onHost(final String host)
    {
        super.onHost(host);
        return this;
    }

    @Override
    public LSOperation onPort(final int portNumber)
    {
        super.onPort(portNumber);
        return this;
    }
}
