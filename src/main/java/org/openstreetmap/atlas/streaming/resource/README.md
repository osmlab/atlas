# FAQ/Notes on the recent [File](https://github.com/osmlab/atlas/blob/dev/src/main/java/org/openstreetmap/atlas/streaming/resource/File.java) refactor

---
### FAQ

1. **What has been updated?**

Our [`org.openstreetmap.atlas.streaming.resource.File`](https://github.com/osmlab/atlas/blob/dev/src/main/java/org/openstreetmap/atlas/streaming/resource/File.java)
implementation (hereafter referred to simply as `File`) has been refactored to entirely
use [`FileSystem`](https://docs.oracle.com/javase/8/docs/api/java/nio/file/FileSystem.html)
and [`java.nio.file`](https://docs.oracle.com/javase/8/docs/api/java/nio/file/package-summary.html) operations.
It no longer depends on [`java.io.File`](https://docs.oracle.com/javase/8/docs/api/java/io/File.html) for
any functionality. Additionally, the class is now
[heavily tested](https://github.com/osmlab/atlas/blob/dev/src/test/java/org/openstreetmap/atlas/streaming/resource/FileTest.java)
using [`jimfs`](https://github.com/google/jimfs).

For the curious, here are a few links discussing the differences between `java.io.File`
and the file manipulation code in `java.nio.file`:

https://docs.oracle.com/javase/tutorial/essential/io/legacy.html
https://docs.oracle.com/javase/8/docs/api/java/nio/file/package-summary.html#package.description


2. **Why were these updates made?**

Before these updates, any `File` object (through its dependence on `java.io.File`) was
at the mercy of the default `FileSystem`. Code components which used `File` were
difficult to test because they automatically relied on file system state external to
the test environment. Even modular code that operated on caller-provided paths still had
to be grounded in the default `FileSystem`. Because of this, unit/integration tests often
relied on creating temporary files and directories at the system default temporary location.
This approach is brittle, since testing code ultimately has no control over the state of
the system default temporary location. This is especially true when running local tests,
since a poorly crafted test could inadvertently be affected by file system state left from previous test runs.

These changes make it much easier to decouple file manipulation code from a concrete
file system. Unit and integration tests can now easily utilize a mock file system
(like [`jimfs`](https://github.com/google/jimfs)) for the code they are testing.

3. **My code uses a method that is now deprecated. What should I do?**

Your initial approach should be to decouple the file manipulation parts of your code from any
specific `FileSystem` implementation. This allows clients of your code to provide whatever implementation
they would like. See the below samples.

Coupled code using the old `File` implementation:
```java
public class MyOldCoupledComponent
{
    /*
     * This "computeSomething" uses the old deprecated methods in File.
     * Thus, MyOldCoupledComponent automatically uses the default FileSystem,
     * and there is nothing MyOldClient can do to change this.
     */

    public void computeSomething(String pathstring)
    {
        int i = 2 * 2;

        // Store result at user's home folder
        if (pathstring == null)
        {
            String userHome = System.getProperty("user.home");
            File result = new File(userHome).child("result");
            result.writeAndClose("result: " + i);
        }
        // Store result at alternate location
        else
        {
            File result = new File(pathstring).child("result");
            result.writeAndClose("result: " + i);
        }
    }
}

public class MyOldClient
{
    public void useComponent()
    {
        MyOldCoupledComponent c = new MyOldCoupledComponent();

        // Store the result in the home folder
        c.computeSomething(null);

        // Also store it somewhere else
        c.computeSomething("/foo/bar")
    }
}
```

Decoupled code using the new `File` implementation:
```java
public class MyNewDecoupledComponent
{
    /*
     * Below are two different ways you might refactor "computeSomething"
     * to utilize the new File implementation in a FileSystem agnostic way.
     */

    // This version uses the pathstring like before, but also receives a
    // FileSystem argument to contextualize the pathstring
    public void computeSomethingString(FileSystem filesystem, String pathstring)
    {
        int i = 2 * 2;

        // Store result at user's home folder in the provided FileSystem
        if (pathstring == null)
        {
            String userHome = System.getProperty("user.home");
            File result = new File(userHome, filesystem).child("result");
            result.writeAndClose("result: " + i);
        }
        // Store result at alternate location
        else
        {
            File result = new File(pathstring, filesystem).child("result");
            result.writeAndClose("result: " + i);
        }
    }

    // This version takes a Path, which is already implicitly tied to
    // a specific FileSystem. In this case, it would be the client's
    // responsibility to ensure the Path is tied to the FileSystem they want
    public void computeSomethingPath(Path path)
    {
        int i = 2 * 2;

        // Store result at user's home folder
        if (path == null)
        {
            // We cannot infer a FileSystem when no Path is given,
            // so just assume default
            String userHome = System.getProperty("user.home");
            File result = new File(userHome, FileSystems.getDefault()).child("result");
            result.writeAndClose("result: " + i);
        }
        // Store result at alternate location
        else
        {
            // No need to specify a FileSystem here, since it is already part of
            // the Path object
            File result = new File(path).child("result");
            result.writeAndClose("result: " + i);
        }
    }
}

public class MyNewClient
{
    /*
     * Notice that MyNewClient has complete control over the FileSystem used by
     * the File class in MyNewDecoupledComponent. This means that no matter how
     * complex the component is, we can always easily test it. It also means that
     * we can construct arbitrarily complex file system states in our unit tests,
     * making them that much more effective at identifying edge cases and brittle code.
     */

    public void useComponent()
    {
        MyNewDecoupledComponent c = new MyNewDecoupledComponent();

        // Store the result in the home folder on the default FileSystem
        c.computeSomethingString(FileSystems.getDefault(), null)
        c.computeSomethingPath(null);

        // Some ways to store it at "/foo/bar" in the default FileSystem
        c.computeSomethingString(FileSystems.getDefault(), "/foo/bar");
        c.computeSomethingPath(Paths.get("/foo/bar"));
        c.computeSomethingPath(Path.of("/foo", "bar"));
        c.computeSomethingPath(FileSystems.getDefault().getPath("/foo/bar"));

        // Store it at "/baz/bat" in an alternate filesystem (jimfs)
        try (FileSystem jimfsFileSystem = Jimfs.newFileSystem(Configuration.osX()))
        {
            c.computeSomethingString(jimfsFileSystem, "/baz/bat");
            c.computeSomethingPath(jimfsFileSystem.getPath("/baz/bat"))
        }
        catch (final IOException exception)
        {
            // ...
        }
    }
}
```

4. **I don't want to refactor my code. What can I do to remove the deprecated warnings?**

If you really want to avoid refactoring your code like in the above sample, then you can just use
the new version of each deprecated method that takes a `FileSystem`, and simply provide
`FileSystems.getDefault()` as an argument.

So `new File("/foo")` becomes `new File("/foo", FileSystems.getDefault())`,
`File.temporary()` becomes `File.temporary(FileSystems.getDefault())`, etc.

For code that uses the deprecated method `getFile`, you *will* have to change your code to
perform file operations using the `java.nio.file` package. For more information on this,
[see the documentation here.](https://docs.oracle.com/javase/tutorial/essential/io/legacy.html)
