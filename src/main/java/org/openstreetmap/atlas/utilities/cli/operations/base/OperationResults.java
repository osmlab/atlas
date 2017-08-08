package org.openstreetmap.atlas.utilities.cli.operations.base;

import java.time.Duration;

/**
 * Shared methods of both SSH and SCP operation results.
 *
 * @author cstaylor
 */
public interface OperationResults
{
    /**
     * @return how long did it take to run?
     */
    Duration getElapsedTime();

    /**
     * @return Was there any output on stderr or stdout from the remote command?
     */
    String getOutput();

    /**
     * @return The exit code returned by the remote command
     */
    int getReturnValue();
}
