package org.openstreetmap.atlas.streaming.resource;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.streaming.compression.Compressor;
import org.openstreetmap.atlas.streaming.compression.Decompressor;
import org.openstreetmap.atlas.utilities.runtime.Retry;
import org.openstreetmap.atlas.utilities.scalars.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * File from a local file system as a {@link AbstractWritableResource}.
 *
 * @author matthieun
 */
public class File extends AbstractWritableResource implements Comparable<File>
{
    private static final Logger logger = LoggerFactory.getLogger(File.class);
    private static final Random RANDOM = new Random();

    private final Path path;
    private String name = null;

    public static TemporaryFile temporary()
    {
        return new Retry(1, Duration.ZERO).run(() ->
        {
            try
            {
                return new TemporaryFile(java.io.File.createTempFile(
                        String.valueOf(RANDOM.nextInt(Integer.MAX_VALUE)),
                        FileSuffix.TEMPORARY.toString()));
            }
            catch (final IOException e)
            {
                throw new CoreException("Unable to get temporary file.", e);
            }
        });
    }

    public static TemporaryFile temporary(final String prefix, final String suffix)
    {
        try
        {
            return new TemporaryFile(java.io.File.createTempFile(prefix, suffix));
        }
        catch (final IOException e)
        {
            throw new CoreException(
                    "Unable to create a temporary file with prefix {} and suffix {}", prefix,
                    suffix, e);
        }
    }

    public static TemporaryFile temporaryFolder()
    {
        try (TemporaryFile temporary = File.temporary())
        {
            final TemporaryFile parent = new TemporaryFile(new TemporaryFile(temporary.getParent()) // NOSONAR
                    .child(RANDOM.nextInt(Integer.MAX_VALUE) + "").getFile());
            parent.mkdirs();
            return parent;
        }
    }

    public File(final java.io.File file)
    {
        this(file, true);
    }

    public File(final Path path)
    {
        this(path, true);
    }

    public File(final Path path, final boolean createParentDirectories)
    {
        this.path = path;
        if (path.toAbsolutePath().endsWith(FileSuffix.GZIP.toString()))
        {
            this.setCompressor(Compressor.GZIP);
            this.setDecompressor(Decompressor.GZIP);
        }
        if (this.path.getParent() != null && createParentDirectories)
        {
            try
            {
                Files.createDirectories(this.path.getParent());
            }
            catch (final IOException exception)
            {
                throw new CoreException("Could not create directories for path {}", this.path,
                        exception);
            }
        }
    }

    public File(final java.io.File file, final boolean createParentDirectories)
    {
        this(file.toPath(), createParentDirectories);
    }

    /**
     * Create a {@link WritableResource} from a {@link java.io.File}.
     * <p>
     * Compression is automatically added for the following deflaters:
     * <ol>
     * <li>GZIP</li>
     * </ol>
     *
     * @param path
     *            The path of the file.
     */
    public File(final String path)
    {
        this(Path.of(path), true);
    }

    public File(final String path, final boolean createParentDirectories)
    {
        this(Path.of(path), createParentDirectories);
    }

    public File child(final String name)
    {
        if (!Files.isDirectory(this.path))
        {
            throw new CoreException("Cannot create the child of file {}. It has to be a folder.",
                    this.path);
        }
        try
        {
            Files.createDirectories(this.path);
        }
        catch (final IOException exception)
        {
            throw new CoreException("Could not create directories for path {}", this.path,
                    exception);
        }
        return new File(this.path.resolve(name));
    }

    @Override
    public int compareTo(final File other)
    {
        return this.path.compareTo(other.toPath());
    }

    public void delete()
    {
        try
        {
            Files.delete(this.path);
        }
        catch (final IOException e)
        {
            throw new CoreException("Cannot delete file", e);
        }
    }

    public void deleteRecursively()
    {
        try
        {
            Files.walkFileTree(this.path, new SimpleFileVisitor<Path>()
            {
                @Override
                public FileVisitResult postVisitDirectory(final Path dir, final IOException exc)
                        throws IOException
                {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs)
                        throws IOException
                {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }
            });
        }
        catch (final IOException exception)
        {
            throw new CoreException("Cannot delete folder {} recursively", this.path, exception);
        }
    }

    @Override
    public boolean equals(final Object other)
    {
        if (other instanceof File)
        {
            return this.path.equals(((File) other).toPath());
        }
        return false;
    }

    public boolean exists()
    {
        return getFile().exists();
    }

    public String getAbsolutePath()
    {
        return this.path.toAbsolutePath().toString();
    }

    /**
     * Get a {@link java.io.File} version of this {@link File} resource. Note that this will force
     * the file to be resolved against the default filesystem. Please avoid using this method, it is
     * here for backwards-compatibility purposes.
     *
     * @return a {@link java.io.File} version of this {@link File} resource
     */
    @Deprecated
    public java.io.File getFile()
    {
        return new java.io.File(this.path.toAbsolutePath().toString());
    }

    @Override
    public String getName()
    {
        if (this.name != null)
        {
            return this.name;
        }
        return this.path.getFileName().toString();
    }

    public String getParent()
    {
        return this.path.getParent().toString();
    }

    public String getPath()
    {
        return this.path.toString();
    }

    @Override
    public int hashCode()
    {
        return this.path.hashCode();
    }

    public boolean isDirectory()
    {
        return Files.isDirectory(this.path);
    }

    @Override
    public long length()
    {
        try
        {
            return Files.size(this.path);
        }
        catch (final IOException exception)
        {
            throw new CoreException("Could not get length of file {}", this.path, exception);
        }
    }

    /**
     * @return An list of files in the directory and sub directories if this object a directory. The
     *         list will be empty if the directory is empty. If this object is a file, the list will
     *         contain only the file itself.
     */
    public List<File> listFilesRecursively()
    {
        final List<File> result = new ArrayList<>();
        if (this.getFile().isDirectory())
        {
            for (final java.io.File file : this.getFile().listFiles())
            {
                final File newFile = new File(file.getAbsolutePath());
                if (file.isDirectory())
                {
                    result.addAll(newFile.listFilesRecursively());
                }
                else
                {
                    result.add(newFile);
                }
            }
        }
        else if (this.getFile().exists())
        {
            result.add(this);
        }
        return result;
    }

    public boolean mkdirs()
    {
        try
        {
            Files.createDirectories(this.path);
            return true;
        }
        catch (final IOException exception)
        {
            logger.error("Could not create directories for path {}", this.path, exception);
            return false;
        }
    }

    /**
     * @return {@link File} object of the parent directory
     */
    public File parent()
    {
        return new File(this.path.getParent());

    }

    public Path toPath()
    {
        return this.path;
    }

    @Override
    public String toString()
    {
        return this.path.toAbsolutePath().toString();
    }

    public File withCompressor(final Compressor compressor)
    {
        this.setCompressor(compressor);
        return this;
    }

    public File withDecompressor(final Decompressor decompressor)
    {
        this.setDecompressor(decompressor);
        return this;
    }

    public File withName(final String name)
    {
        this.name = name;
        return this;
    }

    @Override
    protected InputStream onRead()
    {
        try
        {
            return new BufferedInputStream(Files.newInputStream(this.path));
        }
        catch (final FileNotFoundException exception)
        {
            throw new CoreException("Cannot find file {}", this.path, exception);
        }
        catch (final IOException exception)
        {
            throw new CoreException("Cannot read file {}", this.path, exception);
        }
    }

    @Override
    protected OutputStream onWrite()
    {
        try
        {
            return new BufferedOutputStream(Files.newOutputStream(this.path));
        }
        catch (final FileNotFoundException exception)
        {
            throw new CoreException("Cannot find file {}", this.path, exception);
        }
        catch (final IOException exception)
        {
            throw new CoreException("Cannot write to file {}", this.path, exception);
        }
    }
}
