# `DynamicAtlas`

## Locally complete map with shards

When splitting the map in many shards for easier distributed processing, the complexity of the tasks working on those shards goes up. The data is local, and not global, there are boundary effects at the shard delimitations, and the data might look different. For example, a road might be in shard A, but its intersection with another road is not visible, as it is inside shard B. One process doing anything related to shard A cannot just load shard A, it would then have to load shard A and B. To alleviate those concerns, and make shard-based map processing easier and more intuitive, `DynamicAtlas` abstracts most of the shard resolution and stitching, to present a simple Atlas API that takes care of expansion, stitching and shard fetching under the cover.

## Creation

All the user has to provide to create a `DynamicAtlas` is:
- **Sharding Tree**: A `Sharding` tree, which contains the definition of how the world is split. The currently supported implementations are `SlippyTileSharding` (all shards at the same zoom level) and `DynamicTileSharding` which is a simple quad tree split of the world. Any other implementation of `Sharding` would work too.
- **Starting Point**: The initial shard(s) of interest, or a `Polygon` covering the intial shard(s) of interest.
- **Atlas Fetcher**: A function called "fetcher" that tells the `DynamicAtlas` how to find the Atlas shards it needs. This function is very generic, and takes a `Shard` in input and returns an `Optional<Atlas>`. The returned Atlas can either not exist (exhausted the map data), or come from memory, disk, remote storage... The user chooses.
- **Expansion Policy**: An "expansion policy" that specifies how the `DynamicAtlas` needs to explore neighboring shards. Several options are described below.

## Atlas Fetcher

The most simple use case is where the user has a local folder `myFolder` with all the Dominica (DMA) Atlas shards saved using `PackedAtlas`, and the file names are equal to the shard names. For example, a shard at zoom 8, x index 2345 and y index 4567 would correspond to a file called `myFolder/DMA_8-2345-4567.atlas`. Then a simple atlas fetcher function is as follows:

```java
Function<Shard, Optional<Atlas>> fetcher = shard -> {
    File folder = new File("myFolder");
    File atlasFile = folder.child("DMA_" + shard.getName() + ".atlas");
    if (file.exists())
    {
        try
        {
            Atlas atlas = PackedAtlas.load(atlasFile);
            return Optional.of(atlas);
        }
        catch(Exception e)
        {
            logger.info("Unable to load shard {}", shard.getName(), e);
        }
    }
    return Optional.empty();
}
```

As it is in the above example, the user providing the fetcher is also responsible for handling load failures, and missing or nonexistent resources.

### Shard filtering

As the user controls the fetcher, it also controls the whole loading process from resource to Atlas object. Sometimes, it can be useful to also filter Atlas shards prior to providing them to the DynamicAtlas through the fetcher, when the user knows that only a subset of the map data will be useful.

Here is the same example but with a user interested only in motorways:

```java
Predicate<AtlasEntity> filter = entity -> entity.getType()
    HighwayTag.tag(entity).orElse(null) == HighwayTag.MOTORWAY;

Function<Shard, Optional<Atlas>> fetcher = shard -> {
    File folder = new File("myFolder");
    File atlasFile = folder.child("DMA_" + shard.getName() + ".atlas");
    if (file.exists())
    {
        try
        {
            Atlas atlas = PackedAtlas.load(atlasFile);
            return atlas.subAtlas(filter);
        }
        catch(Exception e)
        {
            logger.info("Unable to load shard {}", shard.getName(), e);
        }
    }
    return Optional.empty();
}
```

### Remote access and caching

When loading shards from a remote location (network drive, object storage service like Azure Blob or S3) it is recommended to cache the Atlas files locally on disk to avoid un-necessary bandwidth usage.


Remote without caching:

```java
// For the example, we assume this Hadoop FileSystem is already
// initialized.
FileSystem fileSystem;
Function<Shard, Optional<Atlas>> fetcher = shard -> {
    String path = "myStorage://myFolder/"
            + "DMA_" + shard.getName() + ".atlas";
    if (fileSystem.exists(path))
    {
        try
        {
            Resource atlasResource = new InputStreamResource(() ->
                    fileSystem.open(path));
            Atlas atlas = PackedAtlas.load(atlasResource);
            return Optional.of(atlas);
        }
        catch(Exception e)
        {
            logger.info("Unable to load shard {}", shard.getName(), e);
        }
    }
    return Optional.empty();
}
```

Here is the same example with caching enabled (Uses [`HadoopAtlasFileCache`](https://github.com/osmlab/atlas-generator/blob/4.0.9/src/main/java/org/openstreetmap/atlas/generator/tools/caching/HadoopAtlasFileCache.java) from the [atlas-generator](https://github.com/osmlab/atlas-generator) project):

```java
// Here are the Hadoop configuration strings, that can contain the
// credentials to connect to the remote storage.
Map<String, String> hadoopConfiguration;
HadoopAtlasFileCache cache =
        new HadoopAtlasFileCache("myStorage://myFolder/",
            hadoopConfiguration);
Function<Shard, Optional<Atlas>> fetcher = shard -> {
    Optional<Resource> atlasResource = cache.get("DMA", shard);
    if (atlasResource.isPresent())
    {
        try
        {
            Atlas atlas = PackedAtlas.load(atlasResource.get());
            return Optional.of(atlas);
        }
        catch(Exception e)
        {
            logger.info("Unable to load shard {}", shard.getName(), e);
        }
    }
    return Optional.empty();
}
```

## Expansion Policy

The expansion policy ([`DynamicAtlasPolicy`](policy/DynamicAtlasPolicy.java)) determines the behavior of the expansion steps of the `DynamicAtlas`, and can have vast impact on performance and ease of use. It also contains the sharding tree, initial shard(s) and fetcher function.

### Expansion

As the user queries features from the `DynamicAtlas`, the `DynamicAtlas` intercepts the calls and checks if that feature intersects the initial shard boundaries. If it does, it blocks the call, removes the current stitched set of shards, loads the needed shards using the fetcher, and re-stitches (using `MultiAtlas`). It then returns the proper feature with all the data around it properly loaded.

#### Indefinite Expansion

By default, the policy is set for indefinite expansion. That means that as long as features intersect loaded shards boundaries, they will trigger a new MultiAtlas load. This is good for debugging and testing, but most production cases need to cap extension.

#### Finite Expansion

Finite expansion is toggled with `DynamicAtlasPolicy.withExtendIndefinitely(false);`. Once this is set, the features that will trigger a new shard load and MultiAtlas load are only the ones that intersect the initial shard(s) of interest, or the initial `Polygon`. That way, all the features intersecting the initial area of interest will always have a locally complete map around them, but the expansion will be capped.

#### Indiscriminate Expansion

By default, the expansion can be triggered by any feature requested that falls outside of the shard boundaries.

#### Discriminate Expansion

Discriminate expansion can be enabled by using a `DynamicAtlasPolicy` that contains `final Predicate<AtlasEntity> atlasEntitiesToConsiderForExpansion`. When this is provided, every time the `DynamicAtlas` needs to check if a feature needs to trigger a shard expansion, it will do so only if that feature passes the `atlasEntitiesToConsiderForExpansion` predicate. For example, this is useful when a user is interested only in a certain type of roads, and expanding based on parks or lakes does not provide any more context.

### Deferred and Preemptive Loading

When _finite expansion is enabled_ (or indefinite expansion disabled), and deferred loading is true (`DynamicAtlasPolicy.withDeferredLocaing(true);`), there is an option to toggle preemptive loading: `DynamicAtlas.preemptiveLoad();`. No shard load will be triggered until `preemptiveLoad` is called.

This will ensure that all the shards needed will be pre-loaded once and for all. It also disables the "interception" of queries for features by the user, as the `DynamicAtlas` is already sure everything is properly loaded. This is the same as having an "intelligent" MultiAtlas that knows what shards the user needs based on each area of interest.
