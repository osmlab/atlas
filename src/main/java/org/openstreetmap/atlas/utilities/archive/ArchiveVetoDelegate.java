package org.openstreetmap.atlas.utilities.archive;

import java.io.File;

/**
 * Callback that informs an object of type T for a given file if it should be processed or not. Some
 * files should be skipped (for example, .DS_Store on the OS X), so this lets us configure the
 * Archiver to skip certain files.
 *
 * @param <T>
 *            the owner of item (in this case, an Archiver or Extractor)
 * @author cstaylor
 */
public interface ArchiveVetoDelegate<T>
{
    /**
     * For a given file item, should we skip it?
     *
     * @param source
     *            the potential owner of the file
     * @param item
     *            the file to possibly be skipped
     * @return true if this item should be skipped, false otherwise
     */
    boolean shouldSkip(T source, File item);
}
