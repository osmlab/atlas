package org.openstreetmap.atlas.utilities.cli.operations.base;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.io.IOUtils;

/**
 * Wrapper for making a remote call via the command-line scp utility
 *
 * @author cstaylor
 */
public class SCPOperation
{
    private static final String QUIET_ARG = "-q";
    private static final String SCP_COMMAND = "scp";
    private static final String PORT_OVERRIDE = "-P";
    private Optional<Integer> possiblePortNumber = Optional.empty();

    /**
     * Send locally to remote with scp
     *
     * @param fromLocal
     *            the local file to send
     * @param toRemote
     *            the remote file saved
     * @return the results (error code, stdout/stderr output, timing)
     * @throws IOException
     *             if there's a network problem or the local file can't be read
     * @throws InterruptedException
     *             if our thread is interrupted while waiting for the remote command to finish
     */
    public SCPOperationResults copy(final Path fromLocal, final RemoteObject toRemote)
            throws IOException, InterruptedException
    {
        return copy(fromLocal.toString(), toRemote.toString());
    }

    /**
     * Read remote file and save it locally
     *
     * @param fromRemote
     *            the remote file to read
     * @param toLocal
     *            the local file saved
     * @return the results (error code, stdout/stderr output, timing)
     * @throws IOException
     *             if there's a network problem
     * @throws InterruptedException
     *             if our thread is interrupted while waiting for the remote command to finish
     */
    public SCPOperationResults copy(final RemoteObject fromRemote, final Path toLocal)
            throws IOException, InterruptedException
    {
        return copy(fromRemote.toString(), toLocal.toString());
    }

    /**
     * Send remote file to another remote file
     *
     * @param fromRemote
     *            the remote file to read
     * @param toRemote
     *            the remote file saved
     * @return the results (error code, stdout/stderr output, timing)
     * @throws IOException
     *             if there's a network problem
     * @throws InterruptedException
     *             if our thread is interrupted while waiting for the remote command to finish
     */
    public SCPOperationResults copy(final RemoteObject fromRemote, final RemoteObject toRemote)
            throws IOException, InterruptedException
    {
        return copy(fromRemote.toString(), toRemote.toString());
    }

    public SCPOperation onPort(final int portNumber)
    {
        this.possiblePortNumber = Optional.of(portNumber);
        return this;
    }

    /**
     * The helper method that does the actual call to SCP
     *
     * @param fromResource
     *            the local or remote file to be read
     * @param toResource
     *            the local or remote file that will be saved
     * @return the results (error code, stdout/stderr output, timing)
     * @throws IOException
     *             if there's a network problem or the local file can't be read or saved
     * @throws InterruptedException
     *             if our thread is interrupted while waiting for the remote command to finish
     */
    private SCPOperationResults copy(final String fromResource, final String toResource)
            throws IOException, InterruptedException
    {
        final List<String> args = new ArrayList<>();
        args.add(SCP_COMMAND);
        args.add(QUIET_ARG);
        this.possiblePortNumber.ifPresent(portNumber ->
        {
            args.add(PORT_OVERRIDE);
            args.add(String.valueOf(portNumber));
        });
        args.add(fromResource);
        args.add(toResource);
        final ProcessBuilder builder = new ProcessBuilder(args);
        builder.redirectErrorStream(true);
        final SCPOperationResults results = new SCPOperationResults(fromResource, toResource);
        final Process process = builder.start();
        final String remoteOutput = new String(IOUtils.toByteArray(process.getInputStream()));
        final int returnCode = process.waitFor();
        return results.finish(remoteOutput, returnCode);
    }
}
