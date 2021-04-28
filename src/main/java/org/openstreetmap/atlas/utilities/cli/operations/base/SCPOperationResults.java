package org.openstreetmap.atlas.utilities.cli.operations.base;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

/**
 * When an scp command is run, the output from the command is captured by this class, including:
 * <ul>
 * <li>stdout/stderr output</li>
 * <li>duration of call</li>
 * <li>source file (local or remote) sent</li>
 * <li>destination file (local or remote) written</li>
 * </ul>
 *
 * @see SCPOperation
 * @see OperationResults
 * @author cstaylor
 */
public class SCPOperationResults implements OperationResults
{
    private final String destination;
    private long end = -1;
    private String output;
    private int returnValue;
    private final String source;
    private long start = -1;

    /**
     * @param source
     *            the local or remote file being read
     * @param destination
     *            the local or remote file being written
     */
    SCPOperationResults(final String source, final String destination)
    {
        this.start = System.currentTimeMillis();
        this.source = source;
        this.destination = destination;
    }

    /**
     * @return the local or remote file being written
     */
    public String getDestination()
    {
        return this.destination;
    }

    @Override
    public Duration getElapsedTime()
    {
        if (this.end == -1)
        {
            this.end = System.currentTimeMillis();
        }
        return Duration.of(this.end - this.start, ChronoUnit.MILLIS);
    }

    @Override
    public String getOutput()
    {
        return this.output;
    }

    @Override
    public int getReturnValue()
    {
        return this.returnValue;
    }

    /**
     * @return the local or remote file being read
     */
    public String getSource()
    {
        return this.source;
    }

    @Override
    public String toString()
    {
        return String.format("%d\n%s\n", getReturnValue(), getOutput());
    }

    /**
     * When the scp operation is completed, this method should be called so the duration is
     * recorded, the stdout/stderr output captured, and the remote return code saved
     *
     * @param output
     *            possible stdout/stderr from the scp process
     * @param returnCode
     *            standard unix exit code. See man scp for details
     * @return fluent interface returns this
     */
    SCPOperationResults finish(final String output, final int returnCode)
    {
        this.output = output;
        this.returnValue = returnCode;
        getElapsedTime();
        return this;
    }
}
