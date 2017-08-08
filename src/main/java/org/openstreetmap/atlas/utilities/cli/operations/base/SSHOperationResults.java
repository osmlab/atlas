package org.openstreetmap.atlas.utilities.cli.operations.base;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

/**
 * When an ssh command is run, the output from the command is captured by this class, including:
 * <ul>
 * <li>stdout/stderr output</li>
 * <li>duration of call</li>
 * </ul>
 *
 * @see SSHOperation
 * @see OperationResults
 * @author cstaylor
 */
public class SSHOperationResults implements OperationResults
{
    private long end = -1;
    private String output;
    private int returnValue;
    private long start = -1;

    public SSHOperationResults()
    {
        this.start = System.currentTimeMillis();
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
     * When the ssh operation is completed, this method should be called so the duration is
     * recorded, the stdout/stderr output captured, and the remote return code saved
     *
     * @param output
     *            possible stdout/stderr from the scp process
     * @param returnValue
     *            standard unix exit code. See man scp for details
     * @return fluent interface returns this
     */
    protected SSHOperationResults finish(final String output, final int returnValue)
    {
        this.output = output;
        this.returnValue = returnValue;
        getElapsedTime();
        return this;
    }
}
