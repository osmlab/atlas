package org.openstreetmap.atlas.utilities.archive;

import java.io.File;
import java.io.IOException;

/**
 * Callback methods for status updates during an extraction (Extractor) or archival (Archiver)
 * operation.
 *
 * @author cstaylor
 * @param <T>
 *            either the Archiver or Extractor class
 */
public interface ArchiverEventListener<T>
{
    /**
     * Called once after the operation completes successfully
     *
     * @param source
     *            the source Archiver or Extractor object
     */
    void archiveCompleted(T source);

    /**
     * Called once after the operation completes with errors
     *
     * @param source
     *            the source Archiver or Extractor object
     */
    void archiveFailed(T source);

    /**
     * Called once before the operation begins
     *
     * @param source
     *            the source Archiver or Extractor object
     */
    void archiveStarted(T source);

    /**
     * Called once for every item that has been extracted or archived successfully
     *
     * @param source
     *            the source Archiver or Extractor object
     * @param item
     *            the item that will be extracted or archived
     */
    void itemCompleted(T source, File item);

    /**
     * Called once for every item that has been extracted or archived with errors
     *
     * @param source
     *            the source Archiver or Extractor object
     * @param item
     *            the item that will be extracted or archived
     * @param oops
     *            the IOException that caused the extraction or archival to fail
     */
    void itemFailed(T source, File item, IOException oops);

    /**
     * Called after each partial copy of an item with the number of bytes read and the total number
     * of bytes that will be read
     *
     * @param source
     *            the source Archiver or Extractor object
     * @param item
     *            the item that is being extracted or archived
     * @param bytesRead
     *            the number of bytes that have already been read
     * @param bytesTotal
     *            the total number of bytes in item
     */
    void itemInProgress(T source, File item, long bytesRead, long bytesTotal);

    /**
     * Called once for every item that was not processed because the implementation decided to skip
     * it
     *
     * @param source
     *            the source Archiver or Extractor object
     * @param item
     *            the item that was skipped
     */
    void itemSkipped(T source, File item);

    /**
     * Called once for every item that will be extracted or archived
     *
     * @param source
     *            the source Archiver or Extractor object
     * @param item
     *            the item that will be extracted or archived
     */
    void itemStarted(T source, File item);
}
