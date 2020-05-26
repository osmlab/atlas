package org.openstreetmap.atlas.streaming.resource;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.streaming.compression.Compressor;
import org.openstreetmap.atlas.streaming.compression.Decompressor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * File from a local file system as a {@link AbstractWritableResource}.
 *
 * @author matthieun
 * @author lcram
 */
public class File extends AbstractWritableResource implements Comparable<File>
{
    private static final Logger logger = LoggerFactory.getLogger(File.class);
    private static final String COULD_NOT_CREATE_DIRECTORIES_FOR_PATH = "Could not create directories for path {}";

    private static final String JAVA_TEMPORARY_DIRECTORY;
    static
    {
        final String property = System.getProperty("java.io.tmpdir");
        if (property == null)
        {
            throw new CoreException("Could not get System property java.io.tmpdir");
        }
        else
        {
            JAVA_TEMPORARY_DIRECTORY = property;
        }
    }

    private final Path path;
    private String name = null;

    /**
     * Get a {@link TemporaryFile} in the default location using the default {@link FileSystem}.
     * This method is deprecated in favor of {@link File#temporary(FileSystem)}, which should allow
     * tests to avoid race conditions and other nasty global filesystem bugs. See the tests for the
     * {@link File} class for details on how to use jimfs as an alternative {@link FileSystem}.
     *
     * @return the temporary file
     * @deprecated please use {@link File#temporary(FileSystem)} instead.
     */
    @Deprecated
    public static TemporaryFile temporary()
    {
        return temporary(FileSystems.getDefault());
    }

    /**
     * Get a {@link TemporaryFile} in the default location using the default {@link FileSystem}, but
     * with a specified prefix and suffix. This method is deprecated in favor of
     * {@link File#temporary(FileSystem, String, String)}, which should allow tests to avoid race
     * conditions and other nasty global filesystem bugs. See the tests for the {@link File} class
     * for details on how to use jimfs as an alternative {@link FileSystem}.
     *
     * @param prefix
     *            the prefix to use
     * @param suffix
     *            the suffix to use
     * @return the temporary file
     * @deprecated please use {@link File#temporary(FileSystem, String, String)} instead.
     */
    @Deprecated
    public static TemporaryFile temporary(final String prefix, final String suffix)
    {
        return temporary(FileSystems.getDefault(), prefix, suffix);
    }

    public static TemporaryFile temporary(final FileSystem fileSystem)
    {
        return temporary(fileSystem, null, FileSuffix.TEMPORARY.toString());
    }

    public static TemporaryFile temporary(final FileSystem fileSystem, final String prefix,
            final String suffix)
    {
        final Path directory = fileSystem.getPath(JAVA_TEMPORARY_DIRECTORY);
        return temporary(directory, prefix, suffix);
    }

    public static TemporaryFile temporary(final Path directory, final String prefix,
            final String suffix)
    {
        /*
         * Create the directory and all parents if it/they do not exist. This may occur in cases
         * where the FileSystem is in-memory.
         */
        new File(directory).mkdirs();
        try
        {
            return new TemporaryFile(Files.createTempFile(directory, prefix, suffix));
        }
        catch (final IOException exception)
        {
            throw new CoreException(
                    "Unable to create a temporary file with prefix '{}' and suffix '{}' at {}",
                    prefix, suffix, directory.toAbsolutePath(), exception);
        }
    }

    public static TemporaryFile temporaryFolder(final FileSystem fileSystem)
    {
        return temporaryFolder(fileSystem, null);
    }

    public static TemporaryFile temporaryFolder(final FileSystem fileSystem, final String prefix)
    {
        final Path directory = fileSystem.getPath(JAVA_TEMPORARY_DIRECTORY);
        return temporaryFolder(directory, prefix);
    }

    /**
     * Get a {@link TemporaryFile} folder in the default location using the default
     * {@link FileSystem}. This method is deprecated in favor of
     * {@link File#temporaryFolder(FileSystem)}, which should allow tests to avoid race conditions
     * and other nasty global filesystem bugs. See the tests for the {@link File} class for details
     * on how to use jimfs as an alternative {@link FileSystem}.
     *
     * @return the temporary folder
     * @deprecated please use {@link File#temporaryFolder(FileSystem)} instead.
     */
    @Deprecated
    public static TemporaryFile temporaryFolder()
    {
        return temporaryFolder(FileSystems.getDefault());
    }

    public static TemporaryFile temporaryFolder(final Path directory, final String prefix)
    {
        /*
         * Create the directory and all parents if it/they do not exist. This may occur in cases
         * where the FileSystem is in-memory.
         */
        new File(directory).mkdirs();
        try
        {
            return new TemporaryFile(Files.createTempDirectory(directory, prefix));
        }
        catch (final IOException exception)
        {
            throw new CoreException("Unable to create a temporary folder with prefix '{}' at {}",
                    prefix, directory.toAbsolutePath(), exception);
        }
    }

    @Deprecated
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
        if (path.toAbsolutePath().toString().endsWith(FileSuffix.GZIP.toString()))
        {
            this.setCompressor(Compressor.GZIP);
            this.setDecompressor(Decompressor.GZIP);
        }
        if (this.path.getParent() != null && createParentDirectories)
        {
            /*
             * We must explicitly check the case where the direct parent already exists and is a
             * symbolic link. This is due to the fact that Files#createDirectories does not treat
             * symlinks to directories as directories themselves. So attempting to create parent
             * directories for the path "foo/bar/baz/bat" where baz is a symlink results in a
             * FileAlreadyExistsException on baz, even if baz points to a directory such that
             * "foo/bar/baz/bat" is still a valid path. This exception is not behaviour we want,
             * since all subsequent file operations will function normally. Rather, in this case, we
             * can simply refrain from even attempting to create the parent directories.
             */
            if (Files.isSymbolicLink(this.path.getParent()))
            {
                logger.debug(
                        "{} already existed and was a symbolic link, skipping parent directory creation",
                        this.path.getParent());
            }
            else
            {
                createDirectoriesForPath(this.path.getParent());
            }
        }
    }

    @Deprecated
    public File(final java.io.File file, final boolean createParentDirectories)
    {
        this(file.toPath(), createParentDirectories);
    }

    public File(final String path, final FileSystem fileSystem)
    {
        this(path, fileSystem, true);
    }

    public File(final String path, final FileSystem fileSystem,
            final boolean createParentDirectories)
    {
        this(fileSystem.getPath(path), createParentDirectories);
    }

    @Deprecated
    public File(final String path)
    {
        this(path, FileSystems.getDefault(), true);
    }

    @Deprecated
    public File(final String path, final boolean createParentDirectories)
    {
        this(path, FileSystems.getDefault(), createParentDirectories);
    }

    /**
     * Get the basename of this {@link File} object as given by the underlying {@link FileSystem}.
     *
     * @return the basename of this {@link File}.
     */
    public String basename()
    {
        return this.path.getFileName().toString();
    }

    /**
     * Get a child {@link File} object for this file with a given name. Note this method will not
     * actually create the child file in the underlying {@link FileSystem}. However, it will attempt
     * to create all necessary directories such that "child" can resolve successfully when actually
     * created, whether that be through {@link File#writeAndClose(String)}, {@link File#mkdirs()},
     * or something else. This method will fail if this {@link File} object is not resolvable to a
     * directory.
     *
     * @param name
     *            the name of the desired child
     * @return the child {@link File} object
     */
    public File child(final String name)
    {
        /*
         * We must explicitly check the case that this.path is a symbolic link. If it is, then we
         * want to skip the directory creation step. Why? Because the Files#createDirectories call
         * used in createDirectoriesForPath will fail when this.path is a symlink, even if it points
         * to a directory. However in this case, our Files.isDirectory() and Path#resolve() calls
         * later on will follow symlinks for us by default. So in the end, things resolve properly
         * in all cases.
         */
        if (Files.isSymbolicLink(this.path))
        {
            logger.debug("{} already existed and was a symbolic link, skipping directory creation",
                    this.path);
        }
        else
        {
            createDirectoriesForPath(this.path);
        }

        if (!Files.isDirectory(this.path))
        {
            throw new CoreException(
                    "Cannot create the child of file {} since it did not resolve to a directory",
                    this.path);
        }
        return new File(this.path.resolve(name));
    }

    /**
     * This comparison uses the underlying {@link Path#compareTo(Path)} (Object)} implementation for
     * each {@link File} object. This means that the file contents are not necessarily considered.
     * If you are looking to compare files strictly using contents, you may consider implementing
     * your own {@link Comparator} that uses something like {@link File#all()}.
     *
     * @param other
     *            the other {@link File}
     * @return the comparison value between the two {@link File}s
     */
    @Override
    public int compareTo(final File other)
    {
        return this.path.compareTo(other.toPath());
    }

    /**
     * Delete this {@link File} from the underlying {@link FileSystem}. This will fail to delete
     * non-empty directories. If you need that behaviour, see {@link File#deleteRecursively()}.
     */
    public void delete()
    {
        try
        {
            Files.delete(this.path);
        }
        catch (final IOException exception)
        {
            throw new CoreException("Cannot delete file {}", this.path, exception);
        }
    }

    /**
     * Delete this {@link File} from the underlying {@link FileSystem}. If this {@link File} is a
     * non-empty directory, recursively delete all contents before deleting this {@link File}.
     */
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

    /**
     * This equals check uses the underlying {@link Path#equals(Object)} implementation for each
     * {@link File} object. This means that the file contents are not necessarily considered. If you
     * are looking to compare files strictly using contents, you may consider comparing the values
     * of {@link File#all()}.
     *
     * @param other
     *            the other {@link File}
     * @return if the two {@link File} objects are equal as specified by
     *         {@link Path#equals(Object)}.
     */
    @Override
    public boolean equals(final Object other)
    {
        if (other instanceof File)
        {
            return this.path.equals(((File) other).toPath());
        }
        return false;
    }

    /**
     * Check if this {@link File} actually exists in the underlying {@link FileSystem}.
     *
     * @return true if this {@link File} exists
     */
    public boolean exists()
    {
        return Files.exists(this.path);
    }

    /**
     * Return a string version of this {@link File}'s absolute path.
     *
     * @return the absolute path of this {@link File} as a string
     */
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

    /**
     * Get the name of this {@link File} {@link Resource}, as specified by the latter interface. By
     * default, this implementation defers to the name of the {@link File} as given by the
     * underlying {@link FileSystem}. However, through the use of {@link File#withName(String)}, it
     * is possible this name may deviate from this {@link File}'s true basename. For the true
     * basename, try {@link File#basename()}.
     *
     * @return the name of this {@link File} {@link Resource}
     */
    @Override
    public String getName()
    {
        if (this.name != null)
        {
            return this.name;
        }
        return this.basename();
    }

    /**
     * Return a string version of this {@link File}'s parent path. This may be absolute or relative,
     * depending on the status of the underlying {@link Path}. For the example file
     * File("foo/bar/baz"), this method will return "foo/bar".
     *
     * @return the parent path of this {@link File} as a string
     */
    public String getParent()
    {
        return this.path.getParent().toString();
    }

    /**
     * Return a string version of this {@link File}'s path. This may be absolute or relative,
     * depending on the status of the underlying {@link Path}.
     *
     * @return the path of this {@link File} as a string
     */
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
     * TODO fix this method, it uses the depcreated getFile
     *
     * @return An list of files in the directory and sub directories if this object a directory. The
     *         list will be empty if the directory is empty. If this object is a file, the list will
     *         contain only the file itself.
     */
    @Deprecated
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

    /**
     * Create a directory for this {@link File}'s {@link Path} by creating all non-existent parent
     * directories first. See {@link Files#createDirectories(Path, FileAttribute[])}.
     */
    public void mkdirs()
    {
        createDirectoriesForPath(this.path);
    }

    /**
     * @return {@link File} object of the parent directory
     */
    public File parent()
    {
        return new File(this.path.getParent());

    }

    /**
     * Get the {@link Path} object associated with this {@link File}.
     *
     * @return the {@link Path} for this {@link File}
     */
    public Path toPath()
    {
        return this.path;
    }

    @Override
    public String toString()
    {
        return this.getAbsolutePath();
    }

    /**
     * Utilize a given {@link Compressor} when writing to this {@link File}.
     *
     * @param compressor
     *            the {@link Compressor} to use
     * @return this {@link File} for chaining
     */
    public File withCompressor(final Compressor compressor)
    {
        this.setCompressor(compressor);
        return this;
    }

    /**
     * Utilize a given {@link Decompressor} when reading from this {@link File}.
     *
     * @param decompressor
     *            the {@link Decompressor} to use
     * @return this {@link File} for chaining
     */
    public File withDecompressor(final Decompressor decompressor)
    {
        this.setDecompressor(decompressor);
        return this;
    }

    /**
     * Update the name of this {@link File} {@link Resource}. Note that this simply changes the name
     * metadata in the {@link Resource} object, it *will not* change the actual name of the file in
     * the filesystem. Generally, users should avoid this method as it may cause confusion in
     * downstream code.
     *
     * @param name
     *            the new name
     * @return this {@link File} for chaining
     */
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
        catch (final IOException exception)
        {
            throw new CoreException("Cannot write to file {}", this.path, exception);
        }
    }

    /**
     * Create a directory for a given {@link Path} by creating all non-existent parent directories
     * first. See documentation for {@link Files#createDirectories(Path, FileAttribute[])}.
     *
     * @param path
     *            the given {@link Path}
     */
    private void createDirectoriesForPath(final Path path)
    {
        try
        {
            Files.createDirectories(path);
        }
        catch (final IOException exception)
        {
            throw new CoreException(COULD_NOT_CREATE_DIRECTORIES_FOR_PATH, path, exception);
        }
    }
}
