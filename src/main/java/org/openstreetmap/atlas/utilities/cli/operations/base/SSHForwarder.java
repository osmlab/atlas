package org.openstreetmap.atlas.utilities.cli.operations.base;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.IOUtils;
import org.openstreetmap.atlas.exception.CoreException;

/**
 * This class spawns a child process that will connect via SSH to a remote host that will act as a
 * packet forwarder for us.
 * <p>
 * We need this to bypass some of the firewall restrictions.
 *
 * @author cstaylor
 */
public class SSHForwarder
{
    private static final int DEFAULT_SSH_PORT = 22;
    private static final String LOGIN_FORMAT = "%s@%s";
    private static final String PROXY_FORMAT = "%d:%s:%d";
    private static final String FORWARD_CREDENTIALS = "-A";
    private static final String PORT_MAPPING = "-L";
    private static final String DISABLE_STRICT_HOST_CHECKING = "-oStrictHostKeyChecking=no";
    private static final String SSH_COMMAND = "ssh";
    private static final String CAT_COMMAND = "cat";
    private static final String READ_FROM_STDIN = "-";
    private static final int SSH_OPERATION_FAILURE_CODE = 255;
    private String hostname;
    private String username;
    private int forwardingLocalPort = -1;
    private int forwardingRemotePort = DEFAULT_SSH_PORT;
    private String forwardingToHostname;
    private Process remoteConnection;

    public SSHForwarder()
    {
    }

    /**
     * @param username
     *            the SSH username for connecting with the remote server
     * @return fluent interface returns this
     */
    public SSHForwarder asUser(final String username)
    {
        this.username = username;
        return this;
    }

    public String getHost()
    {
        return this.hostname;
    }

    public String getUser()
    {
        return this.username;
    }

    public SSHForwarder onHost(final String hostname)
    {
        this.hostname = hostname;
        return this;
    }

    /**
     * Connects via ssh to the remote server and holds the connection open
     *
     * @return the results (error code, stdout/stderr output, timing)
     * @throws IOException
     *             if there's a network problem
     * @throws InterruptedException
     *             if our thread is interrupted while waiting for the remote command to finish
     */
    public int startProxy() throws IOException, InterruptedException
    {
        if (this.hostname == null)
        {
            throw new IllegalStateException("Hostname must be defined");
        }
        if (this.username == null)
        {
            this.username = System.getProperty("user.name");
        }
        if (this.forwardingToHostname == null)
        {
            throw new IllegalStateException("forwardingToHostname must be defined");
        }
        if (this.forwardingLocalPort < 0)
        {
            this.forwardingLocalPort = AvailableSocketFinder.takePort();
        }
        final List<String> arguments = new ArrayList<>();
        arguments.add(SSH_COMMAND);
        arguments.add(FORWARD_CREDENTIALS);
        arguments.add(DISABLE_STRICT_HOST_CHECKING);
        arguments.add(PORT_MAPPING);
        arguments.add(String.format(PROXY_FORMAT, this.forwardingLocalPort,
                this.forwardingToHostname, this.forwardingRemotePort));
        arguments.add(String.format(LOGIN_FORMAT, this.username, this.hostname));
        arguments.add(CAT_COMMAND);
        arguments.add(READ_FROM_STDIN);
        final ProcessBuilder builder = new ProcessBuilder(arguments);
        builder.redirectErrorStream(true);
        // We need to check for errors
        this.remoteConnection = builder.start();
        if (this.remoteConnection.waitFor(1L, TimeUnit.SECONDS)
                && this.remoteConnection.exitValue() == SSH_OPERATION_FAILURE_CODE)
        {
            final String remoteOutput = new String(
                    IOUtils.toByteArray(this.remoteConnection.getInputStream()));
            throw new CoreException("Error when connecting to proxy: {}", remoteOutput);
        }
        return this.forwardingLocalPort;
    }

    public void stopProxy() throws IOException, InterruptedException
    {
        this.remoteConnection.destroyForcibly();
        this.remoteConnection.waitFor();
    }

    public SSHForwarder withForwardingHostname(final String hostname)
    {
        this.forwardingToHostname = hostname;
        return this;
    }

    public SSHForwarder withForwardingLocalPort(final int port)
    {
        this.forwardingLocalPort = port;
        return this;
    }

    public SSHForwarder withForwardingRemotePort(final int port)
    {
        this.forwardingRemotePort = port;
        return this;
    }
}
