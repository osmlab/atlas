package org.openstreetmap.atlas.tags.annotations.validation;

import java.net.URL;

import org.reflections.Configuration;
import org.reflections.Reflections;
import org.reflections.scanners.Scanner;
import org.reflections.vfs.Vfs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Predicate;

/**
 * This class allows logging all the errors that occur during use of Reflections.
 *
 * @author cstaylor
 */
class LoggingReflections extends Reflections
{
    private static final Logger logger = LoggerFactory.getLogger(LoggingReflections.class);

    LoggingReflections()
    {
        super();
    }

    LoggingReflections(final Configuration configuration)
    {
        super(configuration);
    }

    LoggingReflections(final Object... params)
    {
        super(params);
    }

    LoggingReflections(final String prefix, final Scanner... scanners)
    {
        super(prefix, scanners);
    }

    @Override
    protected void scan(final URL url)
    {
        try
        {
            final Vfs.Dir dir = Vfs.fromURL(url);

            try
            {
                for (final Vfs.File file : dir.getFiles())
                {
                    // scan if inputs filter accepts file relative path or fqn
                    final Predicate<String> inputsFilter = this.configuration.getInputsFilter();
                    final String path = file.getRelativePath();
                    final String fullyQualifiedName = path.replace('/', '.');
                    if (inputsFilter == null || inputsFilter.apply(path)
                            || inputsFilter.apply(fullyQualifiedName))
                    {
                        Object classObject = null;
                        for (final Scanner scanner : this.configuration.getScanners())
                        {
                            try
                            {
                                if (scanner.acceptsInput(path)
                                        && scanner.acceptResult(fullyQualifiedName))
                                {
                                    classObject = scanner.scan(file, classObject);
                                }
                            }
                            catch (final Exception e)
                            {
                                logger.debug("cound not scan file {} in url {} with scanner {}",
                                        file.getRelativePath(), url.toExternalForm(),
                                        scanner.getClass().getSimpleName(), e);
                            }
                        }
                    }
                }
            }
            catch (final RuntimeException oops)
            {
                logger.error("Error when scanning {} found at {}", dir.getPath(), url, oops);
            }
            finally
            {
                dir.close();
            }
        }
        catch (final Throwable error)
        {
            logger.error("Error when scanning {}", url, error);
        }
    }
}
