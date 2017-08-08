package org.openstreetmap.atlas.utilities.cli.operations;

import org.openstreetmap.atlas.utilities.cli.operations.base.SSHOperation;
import org.openstreetmap.atlas.utilities.cli.operations.base.SSHOperationResults;

/**
 * Helpful base class containing some shared methods used by the various remote ssh commands
 *
 * @author cstaylor
 */
public abstract class AbstractOperation implements Operation
{
    private final SSHOperation ssh;

    protected AbstractOperation()
    {
        this.ssh = new SSHOperation();
    }

    @Override
    public AbstractOperation asUser(final String username)
    {
        this.ssh.asUser(username);
        return this;
    }

    @Override
    public AbstractOperation onHost(final String hostname)
    {
        this.ssh.onHost(hostname);
        return this;
    }

    @Override
    public AbstractOperation onPort(final int portNumber)
    {
        this.ssh.onPort(portNumber);
        return this;
    }

    protected String getHost()
    {
        return this.ssh.getHost();
    }

    protected String getUser()
    {
        return this.ssh.getUser();
    }

    protected SSHOperationResults handleResults(final SSHOperationResults results)
    {
        // We can do logging here in a central place for all of our commands
        return results;
    }

    protected SSHOperation ssh()
    {
        return this.ssh;
    }
}
