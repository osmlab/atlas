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
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.streaming.compression.Compressor;
import org.openstreetmap.atlas.streaming.compression.Decompressor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * File from a local file system as an {@link AbstractWritableResource}.
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
     * test cases much more control and flexibility over their environments. If your code needs to
     * use the default filesystem, then call {@link File#temporary(FileSystem)} with
     * {@link FileSystems#getDefault()}. However, your code would be more flexible if it were
     * file-system-agnostic and received the {@link FileSystem} as an input from calling code. See
     * the unit tests for the {@link File} class for details on how to use jimfs as an alternative
     * {@link FileSystem} in testing cases.
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
     * {@link File#temporary(FileSystem)}, which should allow test cases much more control and
     * flexibility over their environments. If your code needs to use the default filesystem, then
     * call {@link File#temporary(FileSystem)} with {@link FileSystems#getDefault()}. However, your
     * code would be more flexible if it were file-system-agnostic and received the
     * {@link FileSystem} as an input from calling code. See the unit tests for the {@link File}
     * class for details on how to use jimfs as an alternative {@link FileSystem} in testing cases.
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

    /**
     * Create a temporary file at the system default temporary location. The name of the file will
     * be generated randomly and will have the suffix given by {@link FileSuffix#TEMPORARY}.
     *
     * @param fileSystem
     *            the {@link FileSystem} to use for this {@link TemporaryFile}, use
     *            {@link FileSystems#getDefault()} for the default local {@link FileSystem}
     * @return the file's {@link TemporaryFile}
     */
    public static TemporaryFile temporary(final FileSystem fileSystem)
    {
        return temporary(fileSystem, null, FileSuffix.TEMPORARY.toString());
    }

    /**
     * Create a temporary file with a given prefix and suffix at the system default temporary
     * location. The name of the file will be generated randomly and will be prefixed by the given
     * prefix. The file will have a suffix given by the suffix parameter.
     *
     * @param fileSystem
     *            the {@link FileSystem} to use for this {@link TemporaryFile}, use
     *            {@link FileSystems#getDefault()} for the default local {@link FileSystem}
     * @param prefix
     *            a string prefix to use for the temporary file
     * @param suffix
     *            a string suffix to use for the temporary file
     * @return the file's {@link TemporaryFile}
     */
    public static TemporaryFile temporary(final FileSystem fileSystem, final String prefix,
            final String suffix)
    {
        final Path directory = fileSystem.getPath(JAVA_TEMPORARY_DIRECTORY);
        return temporary(directory, prefix, suffix);
    }

    /**
     * Create a temporary file with a given prefix and suffix at a given directory. The name of the
     * file will be generated randomly and will be prefixed by the given prefix. The file will have
     * a suffix given by the suffix parameter.
     *
     * @param directory
     *            the parent directory for this temporary file
     * @param prefix
     *            a string prefix to use for the temporary file
     * @param suffix
     *            a string suffix to use for the temporary file
     * @return the file's {@link TemporaryFile}
     */
    public static TemporaryFile temporary(final Path directory, final String prefix,
            final String suffix)
    {
        /*
         * Create the directory and all parents if it/they do not exist. Since Files.createTempFile
         * will not actually create parent directories, this step is necessary in some cases.
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

    /**
     * Create a temporary folder at the system default temporary location. The name of the folder
     * will be generated randomly.
     *
     * @param fileSystem
     *            the {@link FileSystem} to use for this {@link TemporaryFile}, use
     *            {@link FileSystems#getDefault()} for the default local {@link FileSystem}
     * @return the folder's {@link TemporaryFile}
     */
    public static TemporaryFile temporaryFolder(final FileSystem fileSystem)
    {
        return temporaryFolder(fileSystem, null);
    }

    /**
     * Create a temporary folder with a given prefix at the system default temporary location. The
     * name of the folder will be generated randomly and will be prefixed by the given prefix.
     * 
     * @param fileSystem
     *            the {@link FileSystem} to use for this {@link TemporaryFile}, use *
     *            {@link FileSystems#getDefault()} for the default local {@link FileSystem}
     * @param prefix
     *            a string prefix to use for the temporary folder
     * @return the folder's {@link TemporaryFile}
     */
    public static TemporaryFile temporaryFolder(final FileSystem fileSystem, final String prefix)
    {
        final Path directory = fileSystem.getPath(JAVA_TEMPORARY_DIRECTORY);
        return temporaryFolder(directory, prefix);
    }

    /**
     * Get a {@link TemporaryFile} folder in the default location using the default
     * {@link FileSystem}. This method is deprecated in favor of
     * {@link File#temporaryFolder(FileSystem)}, which should allow test cases much more control and
     * flexibility over their environments. If your code needs to use the default filesystem, then
     * call {@link File#temporaryFolder(FileSystem)} with {@link FileSystems#getDefault()}. However,
     * your code would be more flexible if it were file-system-agnostic and received the
     * {@link FileSystem} as an input from calling code. See the unit tests for the {@link File}
     * class for details on how to use jimfs as an alternative {@link FileSystem} in testing cases.
     *
     * @return the temporary folder
     * @deprecated please use {@link File#temporaryFolder(FileSystem)} instead.
     */
    @Deprecated
    public static TemporaryFile temporaryFolder()
    {
        return temporaryFolder(FileSystems.getDefault());
    }

    /**
     * Create a temporary folder with a given prefix at a given directory. The name of the folder
     * will be generated randomly and will be prefixed by the given prefix.
     *
     * @param directory
     *            the parent directory for this temporary folder
     * @param prefix
     *            a string prefix to use for the temporary folder
     * @return the folder's {@link TemporaryFile}
     */
    public static TemporaryFile temporaryFolder(final Path directory, final String prefix)
    {
        /*
         * Create the directory and all parents if it/they do not exist. Since Files.createTempFile
         * will not actually create parent directories, this step is necessary in some cases.
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

    /**
     * Create a new {@link File} from a {@link java.io.File}, creating all necessary parent
     * directories.
     *
     * @param file
     *            the {@link java.io.File} to use
     * @deprecated please use {@link File#File(Path)}
     */
    @Deprecated
    public File(final java.io.File file)
    {
        this(file, true);
    }

    /**
     * Create a new {@link File} from a given {@link Path}, creating all necessary parent
     * directories.
     *
     * @param path
     *            the {@link Path} to use
     */
    public File(final Path path)
    {
        this(path, true);
    }

    /**
     * Create a new {@link File} from a given {@link Path}, optionally creating all necessary parent
     * directories.
     *
     * @param path
     *            the {@link Path} to use
     * @param createParentDirectories
     *            whether or not to create necessary parent directories
     */
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

    /**
     * Create a new {@link File} from a {@link java.io.File}, optionally creating all necessary
     * parent directories.
     *
     * @param file
     *            the {@link java.io.File} to use
     * @param createParentDirectories
     *            whether or not to create necessary parent directories
     * @deprecated please use {@link File#File(Path, boolean)}
     */
    @Deprecated
    public File(final java.io.File file, final boolean createParentDirectories)
    {
        this(file.toPath(), createParentDirectories);
    }

    /**
     * Create a new {@link File} from a given path string, using the given {@link FileSystem} to
     * resolve the path string into an actual {@link Path}. Automatically create all necessary
     * parent directories.
     *
     * @param pathString
     *            the path string to the file
     * @param fileSystem
     *            the {@link FileSystem} to use for resolution
     */
    public File(final String pathString, final FileSystem fileSystem)
    {
        this(pathString, fileSystem, true);
    }

    /**
     * Create a new {@link File} from a given path string, using the given {@link FileSystem} to
     * resolve the path string into an actual {@link Path}. Optionally create all necessary parent
     * directories.
     *
     * @param pathString
     *            the path string to the file
     * @param fileSystem
     *            the {@link FileSystem} to use for resolution
     * @param createParentDirectories
     *            whether or not to create necessary parent directories
     */
    public File(final String pathString, final FileSystem fileSystem,
            final boolean createParentDirectories)
    {
        this(fileSystem.getPath(pathString), createParentDirectories);
    }

    /**
     * Create a new {@link File} from a given path string, using the default {@link FileSystem}
     * (i.e. {@link FileSystems#getDefault()}) to resolve the path string into an actual
     * {@link Path}. Automatically create all necessary parent directories.
     *
     * @param pathString
     *            the path string to the file
     */
    @Deprecated
    public File(final String pathString)
    {
        this(pathString, FileSystems.getDefault(), true);
    }

    /**
     * Create a new {@link File} from a given path string, using the default {@link FileSystem}
     * (i.e. {@link FileSystems#getDefault()}) to resolve the path string into an actual
     * {@link Path}. Optionally create all necessary parent directories.
     *
     * @param pathString
     *            the path string to the file
     * @param createParentDirectories
     *            whether or not to create necessary parent directories
     */
    @Deprecated
    public File(final String pathString, final boolean createParentDirectories)
    {
        this(pathString, FileSystems.getDefault(), createParentDirectories);
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
    public String getAbsolutePathString()
    {
        return this.toAbsolutePath().toString();
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
     * Get the name of this {@link File} {@link Resource}, as specified by the {@link Resource}
     * interface. By default, this implementation defers to the name of the {@link File} as given by
     * the underlying {@link FileSystem}. However, through the use of {@link File#withName(String)},
     * it is possible this name may deviate from this {@link File}'s true basename. For the
     * ground-truth file system basename, try {@link File#basename()}. We recommend you use
     * {@link File#basename()} in all cases where the actual {@link File} name is what you care
     * about.
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
    public String getParentPathString()
    {
        return this.toParentPath().toString();
    }

    /**
     * Return a string version of this {@link File}'s path. This may be absolute or relative,
     * depending on the status of the underlying {@link Path}.
     *
     * @return the path of this {@link File} as a string
     */
    public String getPathString()
    {
        return this.toPath().toString();
    }

    @Override
    public int hashCode()
    {
        return this.path.hashCode();
    }

    /**
     * Determine if this {@link File} is a directory. See
     * {@link Files#isDirectory(Path, LinkOption...)} for more details.
     *
     * @return if this {@link File} is a directory
     */
    public boolean isDirectory()
    {
        return Files.isDirectory(this.path);
    }

    /**
     * Get the size in bytes of this {@link File}. This method defers to the implementation of the
     * underlying {@link FileSystem} to compute the file size. See {@link Files#size(Path)} for
     * details.
     *
     * @return the size in bytes of this {@link File}
     */
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
     * Get a {@link List} of all {@link File} objects at this {@link File}'s path, excluding any
     * directories. If this {@link File} is a regular file, then return a {@link List} containing
     * only itself. For example, suppose this {@link File} is a directory called "foo/" containing
     * "bar", "baz/", "bat/", and "fred". This method would return the following {@link List}: [bar,
     * fred].
     *
     * @return a {@link List} of all file-only {@link File}s (not directories) at this
     *         {@link File}'s path
     */
    public List<File> listFiles()
    {
        return listFiles(false);
    }

    /**
     * Get a {@link List} of all {@link File} objects at this {@link File}'s path, optionally
     * including or excluding any directories. If this {@link File} is a regular file, then return a
     * {@link List} containing only itself. For example, suppose this {@link File} is a directory
     * called "foo/" containing "bar", "baz/", "bat/", and "fred". When includeDirectories is
     * specified "true", this method would return the following {@link List}: [bar, baz, bat, fred].
     * When includeDirectories is specified "false", this method would instead return the following
     * {@link List}: [bar, fred].
     *
     * @param includeDirectories
     *            whether or not to include directories in the list
     * @return a {@link List} of all {@link File}s at this {@link File}'s path
     */
    public List<File> listFiles(final boolean includeDirectories)
    {
        final List<File> result = new ArrayList<>();
        if (!this.isDirectory())
        {
            result.add(this);
            return result;
        }
        try (Stream<Path> pathStream = Files.list(this.path))
        {
            pathStream.forEach(path0 ->
            {
                final File file = new File(path0, false);
                if (file.isDirectory())
                {
                    if (includeDirectories)
                    {
                        result.add(file);
                    }
                }
                else
                {
                    result.add(file);
                }
            });
        }
        catch (final IOException exception)
        {
            throw new CoreException("Could not list files at {}", this.path);
        }

        return result;
    }

    /**
     * If this {@link File} is a directory, recursively list all {@link File}s contained by this
     * {@link File}. Subdirectories will themselves be listed recursively. If this {@link File} is a
     * file, simply return a singleton list containing this {@link File}. The 'includeDirectories'
     * parameter will only control whether directories are included in the final list, not whether
     * subdirectories are expanded. All subdirectories are always expanded.
     *
     * @param includeDirectories
     *            whether or not to include directories in the list
     * @return the {@link List} of all {@link File}s contained in this {@link File}
     */
    public List<File> listFilesRecursively(final boolean includeDirectories)
    {
        final List<File> result = new ArrayList<>();
        if (!this.isDirectory())
        {
            result.add(this);
            return result;
        }

        for (final File file : this.listFiles(true))
        {
            final File listedFile = new File(file.toAbsolutePath());
            if (listedFile.isDirectory())
            {
                if (includeDirectories)
                {
                    result.add(listedFile);
                }
                // We need to carry through the value of includeDirectories
                result.addAll(listedFile.listFilesRecursively(includeDirectories));
            }
            else
            {
                result.add(listedFile);
            }
        }

        return result;
    }

    /**
     * If this {@link File} is a directory, recursively list all {@link File}s contained by this
     * {@link File}. Subdirectories will themselves be listed recursively. The final result will
     * contain only leaf {@link File} objects. If this {@link File} is a file, simply return a
     * singleton list containing this {@link File}.
     *
     * @return the {@link List} of all {@link File}s contained in this {@link File}
     */
    public List<File> listFilesRecursively()
    {
        return listFilesRecursively(false);
    }

    /**
     * Create a directory for this {@link File}'s {@link Path} by creating all non-existent parent
     * directories first. See {@link Files#createDirectories(Path, FileAttribute[])}. For example,
     * if this {@link File} is specified by the {@link Path} "/foo/bar/baz", then this method will
     * first create "foo" if necessary, followed by "bar", and then finally "baz".
     */
    public void mkdirs()
    {
        createDirectoriesForPath(this.path);
    }

    /**
     * Get a {@link File} object representing the parent directory of this {@link File}.
     *
     * @return a {@link File} object of the parent directory
     */
    public File parent()
    {
        return new File(this.path.getParent());
    }

    /**
     * Get the absolute {@link Path} object associated with this {@link File}.
     *
     * @return the absolute {@link Path} for this {@link File}
     */
    public Path toAbsolutePath()
    {
        if (this.path.isAbsolute())
        {
            return this.path;
        }
        return this.path.toAbsolutePath();
    }

    /**
     * Get the parent {@link Path} of the {@link Path} object associated with this {@link File}.
     *
     * @return the parent {@link Path} of this {@link File}.
     */
    public Path toParentPath()
    {
        return this.parent().toPath();
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
        return this.getAbsolutePathString();
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
