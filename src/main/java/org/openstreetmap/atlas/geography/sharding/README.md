# Sharding

Sharding is a way to split the world in small units of work, with the goal of having each unit be roughly the same effort.

## SlippyTileSharding

Each shard is a bounding box referenced with a zoom-level, x and y coordinates. All shards have the same zoom-level and the same size.

## DynamicTreeSharding

Each shard is a bounding box referenced with a zoom-level, x and y coordinates. All shards are leaf nodes in a quad tree, the root of which is `Rectangle.MAXIMUM`.
