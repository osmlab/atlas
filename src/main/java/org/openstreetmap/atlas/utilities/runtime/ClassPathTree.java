package org.openstreetmap.atlas.utilities.runtime;

import java.net.URL;
import java.net.URLClassLoader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Print the origin of a class in the class loader.
 *
 * @author cstaylor
 * @author matthieun
 */
public final class ClassPathTree
{
    private static final Logger logger = LoggerFactory.getLogger(ClassPathTree.class);

    public static void print(final Class<?> target)
    {
        try
        {
            final ClassLoader loader = target.getClassLoader();
            print(loader);
        }
        catch (final Exception e)
        {
            logger.error("Could not print ClassPathTree for {}", target, e);
        }
    }

    public static void print(final String className)
    {
        try
        {
            print(Class.forName(className));
        }
        catch (final ClassNotFoundException e)
        {
            logger.error("Could not print ClassPathTree for {}", className, e);
        }
    }

    private static void print(final ClassLoader loader)
    {
        logger.debug("### Classloader Class: {} [{}] ###\n\n", loader.getClass().getName(),
                System.identityHashCode(loader));

        if (loader instanceof URLClassLoader)
        {
            final URL[] urls = ((URLClassLoader) loader).getURLs();
            for (final URL url : urls)
            {
                logger.debug("* {}\n", url);
            }
        }
        else
        {
            logger.debug("[Not Using URLClassLoader]");
        }
        logger.debug("");
        final ClassLoader parent = loader.getParent();
        if (parent != null)
        {
            print(parent);
        }
    }

    private ClassPathTree()
    {
    }
}
