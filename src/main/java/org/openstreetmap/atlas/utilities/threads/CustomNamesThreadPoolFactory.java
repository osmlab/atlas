package org.openstreetmap.atlas.utilities.threads;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Give our threads pretty names
 *
 * @author cstaylor
 * @author matthieun
 */
class CustomNamesThreadPoolFactory implements ThreadFactory
{
    private final AtomicInteger poolNumber = new AtomicInteger(1);
    private final ThreadGroup group;
    private final AtomicInteger threadNumber = new AtomicInteger(1);
    private final String namePrefix;

    protected CustomNamesThreadPoolFactory(final String poolName)
    {
        final SecurityManager securityManager = System.getSecurityManager();
        this.group = securityManager != null ? securityManager.getThreadGroup()
                : Thread.currentThread().getThreadGroup();
        this.namePrefix = poolName + "(" + this.poolNumber.getAndIncrement() + ")-thread-";
    }

    @Override
    public Thread newThread(final Runnable runme)
    {
        /**
         * 0 isn't magic: it's what the javadocs say we should pass if we want the JVM to decide the
         * appropriate stack depth
         */
        final String newThreadName = this.namePrefix + this.threadNumber.getAndIncrement();
        return new Thread(this.group, runme, newThreadName, 0);
    }
}
