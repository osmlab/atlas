package org.openstreetmap.atlas.utilities.cli.operations;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.openstreetmap.atlas.utilities.cli.operations.base.SSHOperationResults;
import org.openstreetmap.atlas.utilities.collections.StringList;
import org.openstreetmap.atlas.utilities.tuples.Tuple;

/**
 * Creates one or more directories on a remote system over SSH
 *
 * @author cstaylor
 */
public class MkdirOperation extends AbstractOperation
{
    private Optional<Consumer<Tuple<Path, String>>> errorHandler = Optional.empty();

    @Override
    public MkdirOperation asUser(final String username)
    {
        super.asUser(username);
        return this;
    }

    public boolean mkdir(final Path... remotePaths) throws InterruptedException, IOException
    {
        if (remotePaths.length == 0)
        {
            throw new IllegalArgumentException("Need at least one remote path");
        }
        this.ssh().addArgs("mkdir", "-p");
        Stream.of(remotePaths).map(Path::toString).forEach(item -> this.ssh().addArgs(item));
        final SSHOperationResults results = handleResults(this.ssh().execute());
        if (results.getReturnValue() != STANDARD_SUCCESS_CODE)
        {
            this.errorHandler.ifPresent(handler ->
            {
                Stream.of(results.getOutput().split("\n")).map(line -> StringList.split(line, ":"))
                        .forEach(stringList ->
                        {
                            handler.accept(
                                    new Tuple<>(Paths.get(stringList.get(1)), stringList.get(2)));
                        });
            });
        }
        return results.getReturnValue() == STANDARD_SUCCESS_CODE;
    }

    @Override
    public MkdirOperation onHost(final String host)
    {
        super.onHost(host);
        return this;
    }

    @Override
    public MkdirOperation onPort(final int portNumber)
    {
        super.onPort(portNumber);
        return this;
    }

    public MkdirOperation withErrorHandler(final Consumer<Tuple<Path, String>> errorHandler)
    {
        this.errorHandler = Optional.ofNullable(errorHandler);
        return this;
    }
}
