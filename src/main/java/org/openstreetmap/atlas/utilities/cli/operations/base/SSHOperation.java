package org.openstreetmap.atlas.utilities.cli.operations.base;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;

/**
 * Wrapper for running a remote program with the local ssh utility
 *
 * @author cstaylor
 */
public class SSHOperation
{
    private static final Logger logger = LoggerFactory.getLogger(SSHOperation.class);

    private static final String LOGIN_FORMAT = "%s@%s";
    private static final String SSH_COMMAND = "ssh";
    private static final String DISABLE_STRICT_HOST_CHECKING = "-oStrictHostKeyChecking=no";
    private static final String QUIET_MODE = "-q";
    private static final String PORT_OVERRIDE = "-p";
    private final List<String> args;
    private String hostname;
    private String username;
    private Optional<Integer> possiblePort;
    private boolean debug;

    public SSHOperation()
    {
        this.args = new ArrayList<>();
        this.possiblePort = Optional.empty();
    }

    /**
     * Add arguments that will be sent to the remote server via SSH
     *
     * @param args
     *            remote linux commands and their arguments
     * @return fluent interface returns this
     */
    public SSHOperation addArgs(final String... args)
    {
        if (args == null || args.length == 0)
        {
            return this;
        }
        this.args.addAll(Arrays.asList(args));
        return this;
    }

    /**
     * @param username
     *            the SSH username for connecting with the remote server
     * @return fluent interface returns this
     */
    public SSHOperation asUser(final String username)
    {
        this.username = username;
        return this;
    }

    public SSHOperation enableDebug()
    {
        this.debug = true;
        return this;
    }

    /**
     * Connects via ssh to the remote server and executes the linux command
     *
     * @return the results (error code, stdout/stderr output, timing)
     * @throws IOException
     *             if there's a network problem
     * @throws InterruptedException
     *             if our thread is interrupted while waiting for the remote command to finish
     */
    public SSHOperationResults execute() throws IOException, InterruptedException
    {
        if (this.hostname == null)
        {
            throw new IllegalStateException("Hostname must be defined");
        }
        if (this.username == null)
        {
            this.username = System.getProperty("user.name");
        }
        final List<String> arguments = buildArguments();
        final ProcessBuilder builder = new ProcessBuilder(arguments);
        if (this.debug)
        {
            logger.debug(Joiner.on(" ").join(arguments));
        }
        builder.redirectErrorStream(true);
        final SSHOperationResults results = new SSHOperationResults();
        final Process process = builder.start();
        final String remoteOutput = new String(IOUtils.toByteArray(process.getInputStream()));
        final int returnCode = process.waitFor();
        if (this.debug)
        {
            logger.debug("[{}] with output:\n{}", returnCode, remoteOutput);
        }
        return results.finish(remoteOutput, returnCode);
    }

    public String getHost()
    {
        return this.hostname;
    }

    public String getUser()
    {
        return this.username;
    }

    public SSHOperation onHost(final String hostname)
    {
        this.hostname = hostname;
        return this;
    }

    public SSHOperation onPort(final int port)
    {
        this.possiblePort = Optional.of(port);
        return this;
    }

    private List<String> buildArguments()
    {
        if (this.args.size() == 0)
        {
            throw new IllegalStateException("You must have at least one argument");
        }
        final List<String> arguments = new ArrayList<>();
        arguments.add(SSH_COMMAND);
        arguments.add(DISABLE_STRICT_HOST_CHECKING);
        arguments.add(QUIET_MODE);
        this.possiblePort.ifPresent(port ->
        {
            arguments.add(PORT_OVERRIDE);
            arguments.add(String.valueOf(port));
        });
        arguments.add(String.format(LOGIN_FORMAT, this.username, this.hostname));
        arguments.addAll(this.args);
        return arguments;
    }
}
