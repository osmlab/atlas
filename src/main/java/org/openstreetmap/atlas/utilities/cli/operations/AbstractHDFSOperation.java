package org.openstreetmap.atlas.utilities.cli.operations;

import java.util.Optional;

import org.openstreetmap.atlas.utilities.cli.operations.base.SSHOperation;

/**
 * Brings all of the common argument values for remote HDFS operations into a single superclass
 * along with permitting custom hadoop configuration settings
 *
 * @author cstaylor
 */
public abstract class AbstractHDFSOperation extends AbstractOperation
{
    private Optional<String> customConfiguration;

    private Optional<String> customHostname;

    protected AbstractHDFSOperation()
    {
        this.customConfiguration = Optional.empty();
        this.customHostname = Optional.empty();
    }

    public AbstractHDFSOperation withConfiguration(final String configuration)
    {
        this.customConfiguration = Optional.ofNullable(configuration);
        return this;
    }

    public AbstractHDFSOperation withCustomHostname(final String customHostname)
    {
        this.customHostname = Optional.ofNullable(customHostname);
        return this;
    }

    protected String preparePath(final String input)
    {
        if (input == null || input.startsWith("hdfs://"))
        {
            return input;
        }
        if (this.customHostname.isPresent())
        {
            return String.format("hdfs://%s%s", this.customHostname.get(), input);
        }
        return input;
    }

    protected SSHOperation prepareSSH()
    {
        final SSHOperation returnValue = this.ssh();
        returnValue.addArgs("hdfs");
        this.customConfiguration.ifPresent(configuration ->
        {
            returnValue.addArgs("--config", configuration);
        });
        returnValue.addArgs("dfs");
        return returnValue;
    }
}
