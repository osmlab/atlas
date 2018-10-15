# `MultiAtlas`

This is a stitched `Atlas`. It is made of multiple sub-`Atlas` (which themselves can be `PackedAtlas` or `MultiAtlas`) and takes care of conflating and stitching at the intersection points.

## Flyweight `MultiAtlas`

When creating a `MultiAtlas` from multiple other `Atlas` objects, no data is copied. The `MultiAtlas` builds array references to remember for example where each available `Edge` is relative to the sub `Atlas`, but does not copy the `Edge` data. When an `Edge` is requested, it creates a `MultiEdge` that contains only its identifier and the `MultiAtlas` it belongs to. When a user asks for the `Node`s connected to that `Edge` for example, the `MultiEdge` relays the query to its `MultiAtlas` that relays the query to the appropriate sub-`Atlas` which returns the result to be relayed back to the user.
