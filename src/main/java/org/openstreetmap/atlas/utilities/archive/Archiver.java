package org.openstreetmap.atlas.utilities.archive;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.io.DirectoryWalker;
import org.apache.commons.io.IOUtils;
import org.openstreetmap.atlas.streaming.NotifyingIOUtils;
import org.openstreetmap.atlas.streaming.NotifyingIOUtils.IOProgressListener;

/**
 * Class for archiving the contents of a directory into a ZIP archive
 *
 * @author cstaylor
 */
public final class Archiver extends AbstractArchiverOrExtractor<Archiver>
{
    /**
     * Responsible for tracking the process of a file being archived
     *
     * @author cstaylor
     */
    class Progress implements IOProgressListener
    {
        private final File file;

        private final long length;

        Progress(final File file)
        {
            this.file = file;
            this.length = file.length();
        }

        @Override
        public void completed()
        {
            fireItemCompleted(this.file);
        }

        @Override
        public void failed(final IOException oops)
        {
            Archiver.this.errorCount++;
            fireItemFailed(this.file, oops);
        }

        @Override
        public void started()
        {
            fireItemStarted(this.file);
        }

        @Override
        public void statusUpdate(final long count)
        {
            fireItemInProgress(this.file, count, this.length);
        }
    }

    /**
     * Sets all files to the default mode of compression: no inspection of the type of file is done
     *
     * @author cstaylor
     */
    public class DefaultArchiveStorageProfileDelegate implements ArchiveStorageProfileDelegate
    {
        private final boolean defaultMode;

        public DefaultArchiveStorageProfileDelegate(final boolean mode)
        {
            this.defaultMode = mode;
        }

        @Override
        public boolean shouldCompress(final File item)
        {
            return this.defaultMode;
        }
    }

    /**
     * The guts of the archival algorithm: - On start, initialize the base path to the file's
     * absolute path and save the length of the name - On handleFile, copy the file contents into
     * the zip archive - on end, close the zip archive stream
     *
     * @author cstaylor
     */
    private class MyDirectoryWalker extends DirectoryWalker<File>
    {
        private String path;

        private int length;

        public void walk(final File file) throws IOException, ArchiveException
        {
            walk(file, new ArrayList<File>());
        }

        @Override
        protected void handleEnd(final Collection<File> items) throws IOException
        {
            Archiver.this.archiveOutputStream.finish();
            Archiver.this.archiveOutputStream.close();
            super.handleEnd(items);
        }

        @Override
        protected void handleFile(final File file, final int depth, final Collection<File> items)
                throws IOException
        {
            if (shouldSkip(file))
            {
                fireItemSkipped(file);
            }
            else
            {
                try (BufferedInputStream input = new BufferedInputStream(new FileInputStream(file)))
                {
                    final String path = file.getAbsolutePath().substring(this.length);
                    final ZipArchiveEntry entry = new ZipArchiveEntry(path);
                    if (Archiver.this.storageDelegate != null)
                    {
                        final int level = Archiver.this.storageDelegate.shouldCompress(file)
                                ? ZipArchiveEntry.DEFLATED
                                : ZipArchiveEntry.STORED;
                        entry.setMethod(level);
                    }
                    Archiver.this.archiveOutputStream.putArchiveEntry(entry);
                    NotifyingIOUtils.copy(input, Archiver.this.archiveOutputStream,
                            new Progress(file));
                    IOUtils.closeQuietly(input);
                    Archiver.this.archiveOutputStream.closeArchiveEntry();
                }
                catch (final FileNotFoundException oops)
                {
                    // This can happen on Linux if the filename is corrupt
                    // and we try to open the file
                    fireItemFailed(file, oops);
                }
            }
            super.handleFile(file, depth, items);
        }

        @Override
        protected void handleStart(final File file, final Collection<File> items) throws IOException
        {
            this.path = file.getAbsolutePath();
            this.length = this.path.length() + 1;
            super.handleStart(file, items);
        }
    }

    /**
     * Where we are compressing to
     */
    private final ArchiveOutputStream archiveOutputStream;

    /**
     * The number of errors that occurred during archival
     */
    private int errorCount;

    private ArchiveStorageProfileDelegate storageDelegate;

    /**
     * Static factory method for creating a new Archiver with the ZIP archive format; currently the
     * only format supported
     *
     * @param outputFile
     *            the destination zip file
     * @return the Archiver instance; useful for chaining method calls
     * @throws ArchiveException
     *             if something compression related failed
     * @throws IOException
     *             if something I/O related failed
     */
    public static Archiver createZipArchiver(final File outputFile)
            throws ArchiveException, IOException
    {
        return Archiver.createZipArchiver(outputFile, false);
    }

    /**
     * Static factory method for creating a new Archiver with the ZIP archive format; currently the
     * only format supported
     *
     * @param outputFile
     *            the destination zip file
     * @param compress
     *            true if we should compress all entries by default, false will just stored them
     *            uncompressed
     * @return the Archiver instance; useful for chaining method calls
     * @throws ArchiveException
     *             if something compression related failed
     * @throws IOException
     *             if something I/O related failed
     */
    public static Archiver createZipArchiver(final File outputFile, final boolean compress)
            throws ArchiveException, IOException
    {
        final ZipArchiveOutputStream zout = new ZipArchiveOutputStream(outputFile);
        zout.setEncoding("UTF-8");
        zout.setFallbackToUTF8(true);
        zout.setUseLanguageEncodingFlag(true);
        if (compress)
        {
            zout.setMethod(ZipArchiveOutputStream.DEFLATED);
        }
        else
        {
            zout.setMethod(ZipArchiveOutputStream.STORED);
        }
        zout.setCreateUnicodeExtraFields(
                ZipArchiveOutputStream.UnicodeExtraFieldPolicy.NOT_ENCODEABLE);
        return new Archiver(zout);
    }

    public static Archiver createZipArchiver(final Path outputPath)
            throws ArchiveException, IOException
    {
        if (outputPath == null)
        {
            throw new IllegalArgumentException("outputPath can't be null");
        }
        return createZipArchiver(outputPath.toFile());
    }

    public static Archiver createZipArchiver(final Path outputPath, final boolean compress)
            throws ArchiveException, IOException
    {
        if (outputPath == null)
        {
            throw new IllegalArgumentException("outputPath can't be null");
        }
        return createZipArchiver(outputPath.toFile(), compress);
    }

    /**
     * Creates a new Archiver specifying where the directory contents will be archived to.
     *
     * @param archiveOutputStream
     *            the destination archive output stream (ZIP)
     */
    private Archiver(final ArchiveOutputStream archiveOutputStream) throws IOException
    {
        super(Archiver.class);
        this.archiveOutputStream = archiveOutputStream;
        setVetoDelegate(new DefaultZipVetoDelegate<Archiver>());
    }

    @Override
    public Archiver addArchiverEventListener(final ArchiverEventListener<Archiver> listener)
    {
        super.addArchiverEventListener(listener);
        return this;
    }

    /**
     * Archives the contents of inputFile to the previously set zip archive stream. Algorithm: -
     * Sanity check the current state of the Extractor and the inputFile argument - Fire the archive
     * started event - Walk the contents of the inputFile; this will delegate all compression calls
     * - If at least one error occurred, fire the archive failed event - Otherwise, fire the archive
     * completed event
     *
     * @param inputFile
     *            the directory we want to extract; must not be null
     * @return the Archiver instance; useful for chaining method calls
     * @throws ArchiveException
     *             if something compression related failed
     * @throws IOException
     *             if something I/O related failed
     */
    public Archiver compress(final File inputFile) throws ArchiveException, IOException
    {
        if (inputFile == null)
        {
            throw new IllegalArgumentException("inputFile can't be null");
        }
        if (this.archiveOutputStream == null)
        {
            throw new IllegalStateException("os can't be null");
        }

        fireArchiveStarted();
        new MyDirectoryWalker().walk(inputFile);
        if (this.errorCount > 0)
        {
            fireArchiveFailed();
        }
        else
        {
            fireArchiveCompleted();
        }
        return this;
    }

    public Archiver compress(final Path inputFile) throws ArchiveException, IOException
    {
        if (inputFile == null)
        {
            throw new IllegalArgumentException("inputFile can't be null");
        }
        return compress(inputFile.toFile());
    }

    @Override
    public Archiver removeArchiverEventListener(final ArchiverEventListener<Archiver> listener)
    {
        super.removeArchiverEventListener(listener);
        return this;
    }

    public Archiver setStorageDelegate(final ArchiveStorageProfileDelegate storageDelegate)
    {
        this.storageDelegate = storageDelegate;
        return this;
    }

    @Override
    public Archiver setVetoDelegate(final ArchiveVetoDelegate<Archiver> delegate)
    {
        super.setVetoDelegate(delegate);
        return this;
    }
}
