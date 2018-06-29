# `DynamicAtlas`

`DynamicAtlas` is an implementation of the `Atlas` API which works well with distributed environments.

One common problem when sharding the world into pieces (shards) to distribute processing is the lack of a complete map view at shard boundaries. For example, a road might be in shard A, but its intersection with another road is not visible, as it is inside shard B. One process doing anything related to shard A cannot just load shard A, it would then have to load shard A and B.

`DynamicAtlas` takes care of that issue directly, by expanding to other shards, and loading them all in one single map dataset, all under the covers, and on demand.

Although there are many options available, the most common user will start with a shard, and provide an expansion policy. That policy defines where the other shard's data comes from (disk, memory, network... anything that can create an `Atlas` object is eligible), and how and how far it can expand away from the initial shard.
