package org.openstreetmap.atlas.utilities.archive;

import java.io.File;

/**
 * Sample veto delegate that skips files we don't want on OS X
 *
 * @author cstaylor
 * @param <T>
 *            the archiver or extractor for this file
 */
public class DefaultZipVetoDelegate<T> implements ArchiveVetoDelegate<T>
{
    @Override
    public boolean shouldSkip(final T source, final File item)
    {
        final String name = item.getAbsolutePath();
        return name.contains(".DS_Store") || name.contains("__MACOSX");
    }
}
