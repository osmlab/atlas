package org.openstreetmap.atlas.utilities.archive;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Collections;

import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.io.FileUtils;
import org.openstreetmap.atlas.streaming.NotifyingIOUtils;
import org.openstreetmap.atlas.streaming.NotifyingIOUtils.IOProgressListener;

/**
 * Class for extracting the contents of a ZIP archive to a specified directory
 *
 * @author cstaylor
 */
public final class Extractor extends AbstractArchiverOrExtractor<Extractor>
{
    /**
     * Responsible for tracking the process of a file being extracted from an archive
     *
     * @author cstaylor
     */
    class Progress implements IOProgressListener
    {
        private final File file;

        private final long length;

        Progress(final File file, final long length)
        {
            this.file = file;
            this.length = length;
        }

        @Override
        public void completed()
        {
            fireItemCompleted(this.file);
        }

        @Override
        public void failed(final IOException oops)
        {
            Extractor.this.errorCount++;
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
     * Where we are extracting to
     */
    private final File outputDirectory;

    /**
     * The number of errors that occurred during extraction
     */
    private int errorCount;

    /**
     * Skip existing?
     */
    private boolean skipExisting;

    /**
     * Static factory method for creating a new Extractor with the ZIP archive format; currently the
     * only format supported
     *
     * @param outputDirectory
     *            the destination directory for any extraction operations
     * @return the Extractor instance; useful for chaining method calls
     * @throws ArchiveException
     *             if something compression related failed
     * @throws IOException
     *             if something I/O related failed
     */
    public static Extractor extractZipArchive(final File outputDirectory)
            throws ArchiveException, IOException
    {
        if (outputDirectory == null)
        {
            throw new IllegalArgumentException("outputDirectory is null");
        }
        return new Extractor(outputDirectory);
    }

    public static Extractor extractZipArchive(final Path outputDirectory)
            throws ArchiveException, IOException
    {
        if (outputDirectory == null)
        {
            throw new IllegalArgumentException("outputDirectory is null");
        }
        return new Extractor(outputDirectory.toFile());
    }

    /**
     * Creates a new Extractor specifying where the contents of the zip file will be extracted to.
     *
     * @param output
     *            the destination directory
     */
    private Extractor(final File output)
    {
        super(Extractor.class);
        this.outputDirectory = output;
        setVetoDelegate(new DefaultZipVetoDelegate<Extractor>());
    }

    @Override
    public Extractor addArchiverEventListener(final ArchiverEventListener<Extractor> listener)
    {
        super.addArchiverEventListener(listener);
        return this;
    }

    /**
     * Extracts the contents of inputFile to the previously set outputDirectory.
     * <p>
     * Algorithm:<br>
     * <ul>
     * <li>Sanity check the current state of the Extractor and the inputFile argument</li>
     * <li>Fire the archive started event</li>
     * <li>Walk the contents of the inputFile
     * <ul>
     * <li>Check if the output file's parent directory is created; if not, create it</li>
     * <li>Copy the contents of the archive entry to the output file</li>
     * <li>Close both the current archive entry input stream and the output file's output stream
     * </li>
     * </ul>
     * </li>
     * <li>If at least one error occurred, fire the archive failed event</li>
     * <li>Otherwise, fire the archive completed event</li>
     * <li>Close the zip file</li>
     * </ul>
     *
     * @param inputFile
     *            the zip file we want to extract; must not be null
     * @return the Extractor instance; useful for chaining method calls
     * @throws ArchiveException
     *             if something compression related failed
     * @throws IOException
     *             if something I/O related failed
     */
    public Extractor extract(final File inputFile) throws ArchiveException, IOException
    {
        if (inputFile == null)
        {
            throw new IllegalArgumentException("inputFile can't be null");
        }
        if (this.outputDirectory == null)
        {
            throw new IllegalStateException("outputDirectory can't be null");
        }
        if (this.outputDirectory.exists())
        {
            FileUtils.deleteQuietly(this.outputDirectory);
        }
        if (!this.outputDirectory.mkdirs())
        {
            throw new IOException(
                    String.format("%s can't be created", this.outputDirectory.getAbsolutePath()));
        }

        fireArchiveStarted();
        try (ZipFile file = new ZipFile(inputFile))
        {
            for (final ZipArchiveEntry current : Collections.list(file.getEntries()))
            {
                final File outputFile = new File(this.outputDirectory, current.getName());

                if (shouldSkip(outputFile))
                {
                    fireItemSkipped(outputFile);
                }
                else if (outputFile.exists() && this.skipExisting)
                {
                    fireItemSkipped(outputFile);
                }
                else if (current.isDirectory())
                {
                    continue;
                }
                else
                {
                    try (BufferedOutputStream bos = new BufferedOutputStream(
                            new FileOutputStream(outputFile));
                            InputStream inputStream = file.getInputStream(current))
                    {
                        outputFile.getParentFile().mkdirs();
                        NotifyingIOUtils.copy(inputStream, bos,
                                new Progress(outputFile, current.getSize()));
                    }
                }

            }
            if (this.errorCount > 0)
            {
                fireArchiveFailed();
            }
            else
            {
                fireArchiveCompleted();
            }
        }
        return this;
    }

    public Extractor extract(final Path inputPath) throws ArchiveException, IOException
    {
        if (inputPath == null)
        {
            throw new IllegalArgumentException("inputFile can't be null");
        }
        return extract(inputPath.toFile());
    }

    public Extractor overwriteExisting()
    {
        this.skipExisting = false;
        return this;
    }

    @Override
    public Extractor removeArchiverEventListener(final ArchiverEventListener<Extractor> listener)
    {
        super.removeArchiverEventListener(listener);
        return this;
    }

    @Override
    public Extractor setVetoDelegate(final ArchiveVetoDelegate<Extractor> delegate)
    {
        super.setVetoDelegate(delegate);
        return this;
    }

    public Extractor skipExisting()
    {
        this.skipExisting = true;
        return this;
    }
}
