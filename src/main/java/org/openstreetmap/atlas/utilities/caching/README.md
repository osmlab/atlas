# utilities.caching package

---

This package adds extensible `Resource` caching capabilities to `Atlas`.

The basic idea is that users can create a cache with a default population mechanism (fetcher), choose a caching strategy, and then fetch desired resources (keyed by `URI`s) using the strategy. A very simple use case might look like:

```java
// Let's read a resource at an arbitrary URI into a Resource.
// Notice the fetcher function provided to constructor conforms to the
// Function<URI, Resource> functional interface.

final URI LOCAL_TEST_FILE_URI = URI.create("file:///path/to/some/file.txt");

// cache file contents into memory (a byte array)
final ConcurrentResourceCache resourceCache = 
            new ConcurrentResourceCache(new ByteArrayCachingStrategy(), uri -> new File(uri.getPath()));

// this will cache miss the first time and populate the cache using the provided fetcher
Resource r1 = resourceCache.get(LOCAL_TEST_FILE_URI).get();

// this time we hit the cache, and read the bytes from memory instead of with the fetcher (from disk)
Resource r2 = resourceCache.get(LOCAL_TEST_FILE_URI).get();


// Manually setting fetchers and CachingStrategies can be a pain. You can abstract
// all this away by creating case-specific subclasses. This subclass cache uses
// ByteArrayCachingStrategy by default and abstracts the get URI parameter by just
// taking a path string as a parameter instead.
final LocalFileInMemoryCache fileCache = new LocalFileInMemoryCache();
Resource r3 = fileCache.get("/path/to/another/file.txt").get();
```
See the `CachingTests` class for more usage examples, and the `LocalFileInMemoryCache` class for an example of how to extend `ConcurrentResourceCache`.