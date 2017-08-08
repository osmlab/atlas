package org.openstreetmap.atlas.utilities.archive;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Abstract superclass for the Archiver and Extractor classes; handles all of the event listener
 * subscriptions and provides helper methods for sending events
 *
 * @author cstaylor
 * @param <T>
 *            either the Archiver or Extractor class
 */
abstract class AbstractArchiverOrExtractor<T>
{
    private final Collection<ArchiverEventListener<T>> listeners;

    private final Class<T> klass;

    private ArchiveVetoDelegate<T> delegate;

    protected AbstractArchiverOrExtractor(final Class<T> klass)
    {
        this.klass = klass;
        this.listeners = new CopyOnWriteArraySet<>();
    }

    public AbstractArchiverOrExtractor<T> addArchiverEventListener(
            final ArchiverEventListener<T> listener)
    {
        this.listeners.add(listener);
        return this;
    }

    public AbstractArchiverOrExtractor<T> removeArchiverEventListener(
            final ArchiverEventListener<T> listener)
    {
        this.listeners.remove(listener);
        return this;
    }

    public AbstractArchiverOrExtractor<T> setVetoDelegate(final ArchiveVetoDelegate<T> delegate)
    {
        this.delegate = delegate;
        return this;
    }

    protected void fireArchiveCompleted()
    {
        for (final ArchiverEventListener<T> listener : this.listeners)
        {
            listener.archiveCompleted(this.klass.cast(this));
        }
    }

    protected void fireArchiveFailed()
    {
        for (final ArchiverEventListener<T> listener : this.listeners)
        {
            listener.archiveFailed(this.klass.cast(this));
        }
    }

    protected void fireArchiveStarted()
    {
        for (final ArchiverEventListener<T> listener : this.listeners)
        {
            listener.archiveStarted(this.klass.cast(this));
        }
    }

    protected void fireItemCompleted(final File file)
    {
        for (final ArchiverEventListener<T> listener : this.listeners)
        {
            listener.itemCompleted(this.klass.cast(this), file);
        }
    }

    protected void fireItemFailed(final File file, final IOException oops)
    {
        for (final ArchiverEventListener<T> listener : this.listeners)
        {
            listener.itemFailed(this.klass.cast(this), file, oops);
        }
    }

    protected void fireItemInProgress(final File file, final long count, final long length)
    {
        for (final ArchiverEventListener<T> listener : this.listeners)
        {
            listener.itemInProgress(this.klass.cast(this), file, count, length);
        }
    }

    protected void fireItemSkipped(final File file)
    {
        for (final ArchiverEventListener<T> listener : this.listeners)
        {
            listener.itemSkipped(this.klass.cast(this), file);
        }
    }

    protected void fireItemStarted(final File file)
    {
        for (final ArchiverEventListener<T> listener : this.listeners)
        {
            listener.itemStarted(this.klass.cast(this), file);
        }
    }

    protected boolean shouldSkip(final File file)
    {
        return this.delegate != null && this.delegate.shouldSkip(this.klass.cast(this), file);
    }
}
