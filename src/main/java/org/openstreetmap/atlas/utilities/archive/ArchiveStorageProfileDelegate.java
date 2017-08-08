package org.openstreetmap.atlas.utilities.archive;

import java.io.File;

/**
 * Callback that informs an archiver for a given file if it should be compressed or not. Some file
 * formats are already compressed, so compressing them again is pointless and may use extra storage.
 *
 * @author cstaylor
 */
public interface ArchiveStorageProfileDelegate
{
    /**
     * Determines if a given file should be compressed by the archiver
     *
     * @param item
     *            the file in question
     * @return true if we should compress, false otherwise
     */
    boolean shouldCompress(File item);
}
