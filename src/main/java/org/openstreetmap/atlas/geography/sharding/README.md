# Sharding

Sharding is a way to split the world in small units of work, with the goal of having each unit be roughly the same effort.

## SlippyTileSharding

Each shard is a bounding box referenced with a zoom-level, x and y coordinates. All shards have the same zoom-level and the same size.

Example: Invoke with `Sharding.forString("slippy@10");`

## DynamicTreeSharding

Each shard is a bounding box referenced with a zoom-level, x and y coordinates. All shards are leaf nodes in a quad tree, the root of which is `Rectangle.MAXIMUM`. The quad tree can be serialized to a simple text file.

Example: Invoke with `Sharding.forString("dynamic@file:///path/to/tree.txt");`

## GeohashSharding

Each shard is a bounding box referenced with a geohash encoded string. All shards have the same precision and the same size.

Example: Invoke with `Sharding.forString("geohash@4");`
