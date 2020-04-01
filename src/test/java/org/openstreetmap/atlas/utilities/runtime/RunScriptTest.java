package org.openstreetmap.atlas.utilities.runtime;

import java.util.Optional;
import java.util.function.Supplier;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.utilities.collections.Iterables;
import org.openstreetmap.atlas.utilities.collections.StringList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author matthieun
 */
public class RunScriptTest
{
    private static final Logger logger = LoggerFactory.getLogger(RunScriptTest.class);
    private static final Supplier<SingleLineMonitor> JAVA_VERSION_MONITOR_CREATOR = () ->
    {
        return new SingleLineMonitor()
        {
            @Override
            protected Optional<String> parseResult(final String line)
            {
                if (line.contains(" version"))
                {
                    final StringList spaceSplit = StringList.split(line, " ");
                    for (final String item : spaceSplit)
                    {
                        if (item.startsWith("\""))
                        {
                            return Optional.of(item.replace("\"", ""));
                        }
                    }
                }
                return Optional.empty();
            }
        };
    };

    @Test
    public void testRunScript()
    {
        final SingleLineMonitor javaVersionMonitor = JAVA_VERSION_MONITOR_CREATOR.get();
        RunScript.run(new String[] { "java", "-version" }, Iterables.toList(javaVersionMonitor));
        // RunScript.run("java -version");
        logger.info("Parsed Java Version: {}", javaVersionMonitor.getResult());
        Assert.assertTrue(javaVersionMonitor.getResult().isPresent());
    }
}
