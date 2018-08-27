# `PackedAtlas`

This is the before `Atlas`. It is immutable, and stores all its items in large arrays in memory.

## Array Storage

All information is kept in large arrays in memory. Each array contains data for a specific type, with each index in the array always representing the same feature. For example, there is an array that contains the `Edge` start nodes indices, another one that contains the `Edge` end node indices. In each of those two arrays, the item at index 3 is always in reference to the same `Edge` with OSM identifier 123. Here are all the most important arrays in the PackedAtlas:

* `dictionary`
* `edgeIdentifiers`
* `nodeIdentifiers`
* `areaIdentifiers`
* `lineIdentifiers`
* `pointIdentifiers`
* `relationIdentifiers`
* `edgeIdentifierToEdgeArrayIndex`
* `nodeIdentifierToNodeArrayIndex`
* `areaIdentifierToAreaArrayIndex`
* `lineIdentifierToLineArrayIndex`
* `pointIdentifierToPointArrayIndex`
* `relationIdentifierToRelationArrayIndex`
* `nodeLocations`
* `nodeInEdgesIndices`
* `nodeOutEdgesIndices`
* `nodeTags`
* `nodeIndexToRelationIndices`
* `edgeStartNodeIndex`
* `edgeEndNodeIndex`
* `edgePolyLines`
* `edgeTags`
* `edgeIndexToRelationIndices`
* `areaPolygons`
* `areaTags`
* `areaIndexToRelationIndices`
* `linePolyLines`
* `lineTags`
* `lineIndexToRelationIndices`
* `pointLocations`
* `pointTags`
* `pointIndexToRelationIndices`
* `relationMemberIndices`
* `relationMemberTypes`
* `relationMemberRoles`
* `relationTags`
* `relationIndexToRelationIndices`
* `relationOsmIdentifierToRelationIdentifiers`
* `relationOsmIdentifiers`

## Flyweight Atlas features

All Atlas features are following the flyweight design pattern. What that means is every [`PackedEdge`](/src/main/java/org/openstreetmap/atlas/geography/atlas/packed/PackedEdge.java), [`PackedNode`](/src/main/java/org/openstreetmap/atlas/geography/atlas/packed/PackedNode.java), [`PackedArea`](/src/main/java/org/openstreetmap/atlas/geography/atlas/packed/PackedArea.java), [`PackedLine`](/src/main/java/org/openstreetmap/atlas/geography/atlas/packed/PackedLine.java), [`PackedPoint`](/src/main/java/org/openstreetmap/atlas/geography/atlas/packed/PackedPoint.java) or [`PackedRelation`](/src/main/java/org/openstreetmap/atlas/geography/atlas/packed/PackedRelation.java) contains only two things: a reference to the Atlas object it belongs to, and the index it is positioned at in all the arrays in that Atlas. This makes the feature objects really lightweight and fast to create.

When a user asks for the incoming edges to a `PackedNode`, then the `PackedNode` relays the query to its own `PackedAtlas` along with its index, and the `PackedAtlas` returns the result which is relayed to the user by the `PackedNode` object.

## Serialization / De-serialization

A `PackedAtlas` can be serialized to a `WritableResource` using the `PackedAtlasSerializer`. During that process, all the arrays are pushed to a non-compressed zip stream in which each array is a zip entry with the same name. Each array is serialized in its zip entry using either standard Java serialization or protobuf.

In case the `Resource` is a file, then the user has random access to each zip entry. That means that all the arrays are lazily de-serialized only when needed. This is extremely useful when  opening an Atlas file just to check `Node` connectivity for example. Only the arrays relative with `Node`s and `Edge`s will be loaded.
