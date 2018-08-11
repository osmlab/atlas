package org.openstreetmap.atlas.streaming.resource;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
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

/**
 * File from a local file system as a {@link AbstractWritableResource}.
 *
 * @author matthieun
 */
public class File extends AbstractWritableResource implements Comparable<File>
{
    private final java.io.File file;
    private String name = null;

    public static File temporary()
    {
        final Random random = new Random();
        File result = null;
        try
        {
            try
            {
                result = new File(java.io.File.createTempFile(
                        String.valueOf(random.nextInt(Integer.MAX_VALUE)),
                        FileSuffix.TEMPORARY.toString()));
            }
            catch (final Exception e)
            {
                // Re-try
                result = new File(java.io.File.createTempFile(
                        String.valueOf(random.nextInt(Integer.MAX_VALUE)),
                        FileSuffix.TEMPORARY.toString()));
            }
            return result;
        }
        catch (final Exception e)
        {
            throw new CoreException("Unable to get temporary file.", e);
        }
    }

    public static File temporary(final String prefix, final String suffix)
    {
        try
        {
            return new File(java.io.File.createTempFile(prefix, suffix));
        }
        catch (final IOException e)
        {
            throw new CoreException(
                    "Unable to create a temporary file with prefix {} and suffix {}", prefix,
                    suffix, e);
        }
    }

    public static File temporaryFolder()
    {
        File temporary = null;
        try
        {
            temporary = File.temporary();
            final File parent = new File(temporary.getParent())
                    .child(new Random().nextInt(Integer.MAX_VALUE) + "");
            parent.mkdirs();
            return parent;
        }
        finally
        {
            temporary.delete();
        }
    }

    public File(final java.io.File file)
    {
        this.file = file;
        if (file.getAbsolutePath().endsWith(FileSuffix.GZIP.toString()))
        {
            this.setCompressor(Compressor.GZIP);
            this.setDecompressor(Decompressor.GZIP);
        }
        if (this.file.getParentFile() != null)
        {
            this.file.getParentFile().mkdirs();
        }
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
        this(new java.io.File(path));
    }

    public File child(final String name)
    {
        if (!this.file.isDirectory())
        {
            throw new CoreException("Cannot create the child of a file. It has to be a folder.");
        }
        this.file.mkdirs();
        return new File(getAbsolutePath() + "/" + name);
    }

    @Override
    public int compareTo(final File other)
    {
        return this.getFile().compareTo(other.getFile());
    }

    public void delete()
    {
        try
        {
            Files.delete(getFile().toPath());
        }
        catch (final IOException e)
        {
            throw new CoreException("Cannot delete file", e);
        }
    }

    public void deleteRecursively()
    {
        final Path folder = getFile().toPath();
        try
        {
            Files.walkFileTree(folder, new SimpleFileVisitor<Path>()
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
        catch (final IOException e)
        {
            throw new CoreException("Cannot delete folder recursively", e);
        }
    }

    public boolean exists()
    {
        return getFile().exists();
    }

    public String getAbsolutePath()
    {
        return this.file.getAbsolutePath();
    }

    public java.io.File getFile()
    {
        return this.file;
    }

    @Override
    public String getName()
    {
        if (this.name != null)
        {
            return this.name;
        }
        return this.file.getName();
    }

    public String getParent()
    {
        return this.file.getParent();
    }

    public String getPath()
    {
        return this.file.getPath();
    }

    public boolean isDirectory()
    {
        return this.file.isDirectory();
    }

    @Override
    public long length()
    {
        return this.file.length();
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
        return this.file.mkdirs();
    }

    /**
     * @return {@link File} object of the parent directory
     */
    public File parent()
    {
        return new File(this.file.getParent());

    }

    @Override
    public String toString()
    {
        return this.file.getAbsolutePath();
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
            return new BufferedInputStream(new FileInputStream(this.file));
        }
        catch (final FileNotFoundException e)
        {
            throw new CoreException("Cannot read file " + this.file.getPath(), e);
        }
    }

    @Override
    protected OutputStream onWrite()
    {
        try
        {
            return new BufferedOutputStream(new FileOutputStream(this.file));
        }
        catch (final FileNotFoundException e)
        {
            throw new CoreException("Cannot write to file " + this.file.getPath(), e);
        }
    }
}
