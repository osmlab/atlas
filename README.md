# Atlas

[![Build Status](https://travis-ci.org/osmlab/atlas.svg?branch=master)](https://travis-ci.org/osmlab/atlas)

The [`Atlas`](src/main/java/org/openstreetmap/atlas/geography/atlas/Atlas.java) is a way to efficiently represent [OpenStreetMap](http://www.openstreetmap.org/) data in memory. A subset of the data is in a "navigable network" form, meaning anything that is assumed to be navigable will be in a form of `Node`s and `Edge`s in a way a routing algorithm could traverse it. It also provides easy to use APIs to access geographical data. On top of it all, it is easy to shard and re-stitch, making it perfect for distributed processing!

Projects using Atlas:
* [atlas-generator](https://github.com/osmlab/atlas-generator): A Spark job to distribute Atlas shards generation
* [atlas-checks](https://github.com/osmlab/atlas-checks): A suite of tools to check OSM data integrity using Atlas, and Spark.
* [josm-atlas](https://github.com/osmlab/josm-atlas): A JOSM plugin to visualize Atlas data.

# Getting started

To contribute to the project, please see the [contributing guidelines](CONTRIBUTING.md).

After initially cloning the project, first perform
```
$ cd /path/to/local/atlas/repo
$ ./gradlew clean build -x check -x javadoc -x sources
```
The '-x' options are not required but will make this initial build much faster. Once the build is complete, you will need to import the project into Eclipse. Go to 'File' -> 'Import' -> 'Existing Gradle Project' and select 'Finish'. Now that your project shows up in Eclipse, right click the project and select 'Refresh'. Then, right click it again, scroll down to 'Gradle', and select 'Refresh Gradle Project'. The project should now be ready!

## What's in it?

* [`Edge`](src/main/java/org/openstreetmap/atlas/geography/atlas/items/Edge.java)s and [`Node`](src/main/java/org/openstreetmap/atlas/geography/atlas/items/Node.java)s for navigable items (Roads, Ferries)
  * [`Edge`](src/main/java/org/openstreetmap/atlas/geography/atlas/items/Edge.java)s provide their start [`Node`](src/main/java/org/openstreetmap/atlas/geography/atlas/items/Node.java) and end [`Node`](src/main/java/org/openstreetmap/atlas/geography/atlas/items/Node.java)
  * [`Node`](src/main/java/org/openstreetmap/atlas/geography/atlas/items/Node.java)s provide their incoming [`Edge`](src/main/java/org/openstreetmap/atlas/geography/atlas/items/Edge.java)s and outgoing [`Edge`](src/main/java/org/openstreetmap/atlas/geography/atlas/items/Edge.java)s.
  * [`Edge`](src/main/java/org/openstreetmap/atlas/geography/atlas/items/Edge.java)s are uni-directional: a two-way road in OSM is represented as two reverse direction [`Edge`](src/main/java/org/openstreetmap/atlas/geography/atlas/items/Edge.java)s.
* [`Area`](src/main/java/org/openstreetmap/atlas/geography/atlas/items/Area.java), [`Line`](src/main/java/org/openstreetmap/atlas/geography/atlas/items/Line.java) and [`Point`](src/main/java/org/openstreetmap/atlas/geography/atlas/items/Point.java) for non-navigable features (Parks, Power Lines, POIs)
* [`Relation`](src/main/java/org/openstreetmap/atlas/geography/atlas/items/Relation.java)s (mirroring OSM Relations) linking features together
  * All features can return the [`Relation`](src/main/java/org/openstreetmap/atlas/geography/atlas/items/Relation.java)s they belong to
* Full tag map coming from OSM on each feature
* Full geometry for every feature except [`Relation`](src/main/java/org/openstreetmap/atlas/geography/atlas/items/Relation.java)s.
* All the entities are spatially indexed using R-Tree and Quad-Tree implementations from JTS
* [`ComplexEntity`](src/main/java/org/openstreetmap/atlas/geography/atlas/items/complex/ComplexEntity.java)(ies): Entities built on the fly from an Atlas. For concepts that are higher level than a simple feature. Example: [`ComplexBuilding`](src/main/java/org/openstreetmap/atlas/geography/atlas/items/complex/buildings) with multipolygon and parts, or [`ComplexTurnRestriction`](src/main/java/org/openstreetmap/atlas/geography/atlas/items/complex/restriction).
* Stitching: Combine multiple [`Atlas`](src/main/java/org/openstreetmap/atlas/geography/atlas/Atlas.java) into one [`MultiAtlas`](src/main/java/org/openstreetmap/atlas/geography/atlas/multi/MultiAtlas.java)
* Soft cut: create a new [`Atlas`](src/main/java/org/openstreetmap/atlas/geography/atlas/Atlas.java) from an origin [`Atlas`](src/main/java/org/openstreetmap/atlas/geography/atlas/Atlas.java) based on a polygon or a predicate.

## How it's built

![Atlas Diagram](/images/atlas.png)

## Using Atlas

* Grab one or more Atlas files (from [here](https://apple.box.com/s/3k3wcc0lq1fhqgozxr4mdi0llf95byo3) or after [building them yourself](#building-an-atlas-from-an-osmpbf-file)), and open them with [`AtlasResourceLoader`](src/main/java/org/openstreetmap/atlas/geography/atlas/AtlasResourceLoader.java):
```
final File atlasFile = new File("/path/to/source.atlas");
final Atlas atlas = new AtlasResourceLoader().load(atlasFile);
```
Query the data. Examples:

* Get all edges within boundaries:
```
Rectangle rectangle = ...;
atlas.edgesIntersecting(rectangle).forEach(edge -> ...);
```
* Find all the parks with less than 6 shape points:
```
Predicate<Area> filter = area -> {
    return Validators.isOfType(area, LeisureTag.class, LeisureTag.PARK)
        && area.asPolygon().size() < 6;
}
atlas.areas(filter).forEach(area -> ...);
```
or
```
Predicate<Area> filter = area -> {
    return "park".equals(area.getTags().get("leisure"))
        && area.asPolygon().size() < 6;
}
atlas.areas(filter).forEach(area -> ...);
```
* Find all buildings with a hole:
```
ComplexBuildingFinder finder = new ComplexBuildingFinder();
Iterables.stream(finder.find(atlas))
    .filter(complexBuilding -> !complexBuilding.getOutline().inners().isEmpty())
    .forEach(complexBuilding -> ...);
```
* How many `Edge`s are connected to a `Node`:
```
long identifier = 123;
int numberOfConnectedEdges = atlas.nodeForIdentifier(identifier).connectedEdges().size();
```
or
```
long identifier = 123;
int numberOfConnectedEdges = atlas.nodeForIdentifier(identifier).absoluteValence();
```

## Building an `Atlas` from an `.osm.pbf` file

### Generate an Atlas with everything that is in a pbf file

```
final File pbfFile;
final OsmPbfLoader loader = new OsmPbfLoader(pbfFile, MultiPolygon.MAXIMUM,
    AtlasLoadingOption.withNoFilter());
final Atlas atlas = loader.read();
```

### Generate an Atlas without boundaries and coastlines

Usually OSM boundaries and coastlines do not work well with JTS and country-slicing with themselves. The default `osmPbfWayFilter` in the `AtlasLoadingOption` will filter those out:

```
final File pbfFile;
final OsmPbfLoader loader = new OsmPbfLoader(pbfFile, MultiPolygon.MAXIMUM,
    AtlasLoadingOption.createOptionWithAllEnabled());
final Atlas atlas = loader.read();
```

### Generate an Atlas for a specific shard

When specifying a shard for building an `Atlas` from an `.osm.pbf` file, the generator will do a soft cut along the borders of the shard:

```
final File pbfFile;
final Rectangle shard;
final OsmPbfLoader loader = new OsmPbfLoader(pbfFile, MultiPolygon.forPolygon(shard),
    AtlasLoadingOption.createOptionWithAllEnabled());
final Atlas atlas = loader.read();
```

### Way Sectioning

OSM ways usually span multiple intersections in the case of roads. To make the road network a navigable network, the process of loading an `.osm.pbf` file runs "way sectioning". It will follow a set of rules to break ways at intersections, and create `Atlas` `Edge`s. For that, it pads the OSM feature identifiers with six digits starting from 1 to the number of sections. For example, way 123 would become `Edge`s 123000001, 123000002 and 123000003 if it has to be broken twice.

### Country Slicing

In case of building an Atlas that is hard-cut along a polygon (usually a country boundary with boundary=administrative and admin_level=2), the process of loading an `.osm.pbf` file runs "country slicing". All the features that span outside of the boundary will be cut at the boundary and excluded. All the features that are inside will be assigned a country code tag if a country code is given with the AtlasLoadingOption.

The `AtlasLoadingOption` contains all the country boundaries, along with the country codes in a `CountryBoundaryMap`.

All feature identifiers will be padded and the first 3 digits of the 6 digit padding (described above) will be a country counter. If a `Line` 123 spans 2 countries, gets out and comes back for example, it will ship with 123001000 and 123001002 within the first country, and 123002001 in the country where it spans out (in a separate `Atlas`).

### Configuration

Way-sectioning logic, edge definition and which pbf entities (Way, Node, Relation) are brought into an `Atlas` are all configurable. The default configurations can be found in the main resources directory as json files (see atlas-way-section.json for an example of the way section configuration). These configurations are initialized and can be set in `AtlasLoadingOption`.

## Building an `Atlas` from scratch

The `PackedAtlasBuilder` is here for that. It ensures that all the data that makes its way to an Atlas is consistent (for example making sure that if an `Edge` says its start `Node` is 123, then `Node` 123 really exists) and that an Atlas is final and cannot be modified once it has been accessed once.

* First add all the `Node`s
* Then add all the `Edge`s, `Area`s, `Line`s and `Point`s in any order.
* Finally add all the `Relation`s from the lowest order (no other `Relation` is within its members) to the higher order (other `Relation`s are within its members). The `PackedAtlasBuilder` will throw an exception if a `Relation` is added and any of the listed members have not already been added.

## Saving an `Atlas`

The `Atlas` API offers a `save(WritableResource)` method, that is implemented by `PackedAtlas`. Trying to save a `MultiAtlas` will result in an exception suggesting to copy the `Atlas` to a `PackedAtlas` first.

## Copying an `Atlas`

From any `Atlas`, a `PackedAtlas` can be created and saved to a `WritableResource` (A File for example). This is done with the `PackedAtlasCloner`:

```
final Atlas atlas1;
final Atlas atlas2;
new PackedAtlasCloner().cloneFrom(new MultiAtlas(atlas1, atlas2)).save(new File("/path/to/file.atlas"));
```

# PyAtlas

`Atlas` features a lightweight Python version of the core functionality. The `pyatlas` is available as a Python package from PyPI, but can also be built and installed locally. Check out the [pyatlas](pyatlas) subfolder and the [pyatlas/README.md](pyatlas/README.md) file for more information!

# Implementation Details

## [`PackedAtlas`](src/main/java/org/openstreetmap/atlas/geography/atlas/packed/PackedAtlas.java)

This is the base `Atlas`. It is immutable, and stores all its items in large arrays in memory.

### Array Storage

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

### Flyweight Atlas features

All Atlas features are following the flyweight design pattern. What that means is every [`PackedEdge`](src/main/java/org/openstreetmap/atlas/geography/atlas/packed/PackedEdge.java), [`PackedNode`](src/main/java/org/openstreetmap/atlas/geography/atlas/packed/PackedNode.java), [`PackedArea`](src/main/java/org/openstreetmap/atlas/geography/atlas/packed/PackedArea.java), [`PackedLine`](src/main/java/org/openstreetmap/atlas/geography/atlas/packed/PackedLine.java), [`PackedPoint`](src/main/java/org/openstreetmap/atlas/geography/atlas/packed/PackedPoint.java) or [`PackedRelation`](src/main/java/org/openstreetmap/atlas/geography/atlas/packed/PackedRelation.java) contains only two things: a reference to the Atlas object it belongs to, and the index it is positioned at in all the arrays in that Atlas. This makes the feature objects really lightweight and fast to create.

When a user asks for the incoming edges to a `PackedNode`, then the `PackedNode` relays the query to its own `PackedAtlas` along with its index, and the `PackedAtlas` returns the result which is relayed to the user by the `PackedNode` object.

### Serialization / De-serialization

A `PackedAtlas` can be serialized to a `WritableResource` using the `PackedAtlasSerializer`. During that process, all the arrays are pushed to a non-compressed zip stream in which each array is a zip entry with the same name. Each array is serialized in its zip entry using standard Java serialization.

In case the `Resource` is a file, then the user has random access to each zip entry. That means that all the arrays are lazily de-serialized only when needed. This is extremely useful when  opening an Atlas file just to check `Node` connectivity for example. Only the arrays relative with `Node`s and `Edge`s will be loaded.

## [`MultiAtlas`](src/main/java/org/openstreetmap/atlas/geography/atlas/multi/MultiAtlas.java)

This is a stitched `Atlas`. It is made of multiple sub-`Atlas` (which themselves can be `PackedAtlas` or `MultiAtlas`) and takes care of conflating and stitching at the intersection points.

### Flyweight [`MultiAtlas`](src/main/java/org/openstreetmap/atlas/geography/atlas/multi/MultiAtlas.java)

When creating a `MultiAtlas` from multiple other `Atlas` objects, no data is copied. The `MultiAtlas` builds array references to remember for example where each available `Edge` is relative to the sub `Atlas`, but does not copy the `Edge` data. When an `Edge` is requested, it creates a `MultiEdge` that contains only its identifier and the `MultiAtlas` it belongs to. When a user asks for the `Node`s connected to that `Edge` for example, the `MultiEdge` relays the query to its `MultiAtlas` that relays the query to the appropriate sub-`Atlas` which returns the result to be relayed back to the user.

### Way Sectioning Related Border Fixes

Way sectioning assumes knowledge of the surrounding geometries to be able to know where to make a cut. In some cases, for a specific way, a shard will have knowledge of an intersection at a point we can call P1. Another shard that contains the same way has knowledge of an intersection at another point P2. But both shards do not have knowledge of the other shard's intersection point. The first shard would then have a split way at P1, and the other shard the same way split at P2. To prevent from showing discrepancies, the `MultiAtlas` identifies those issues and would present a way that is split at P1 and P2, with the related fixes to `Edge` identifiers, and `Node` connectivity.

# Community

For more information, please contact our community projects lead [Andrew Wiseman](https://github.com/awisemanapple).
