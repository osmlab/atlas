# Using Atlas

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

# Building an `Atlas` from an `.osm.pbf` file

Building an `Atlas` from an `.osm.pbf` file involves multiple steps, described below. First create a "Raw Atlas" that is a simple copy of all the items in the PBF file into the Atlas format. Then (optionally) apply country slicing, and finally call the way-sectioning algorithm to create the "navigable network" part of the Atlas.

Without country slicing:

```
final File pbfFile;

final Atlas rawAtlas = new RawAtlasGenerator(pbfFile).build();
final Atlas atlas = new WaySectionProcessor(rawAtlas, AtlasLoadingOption.createOptionWithAllEnabled()).run();
```

With country slicing:

```
final File pbfFile;
final Set<String> countries;
final CountryBoundaryMap boundaries;
        
final Atlas rawAtlas = new RawAtlasGenerator(pbfFile).build();
final Atlas slicedRawAtlas = new RawAtlasCountrySlicer(countries, boundaries).slice(rawAtlas);
final Atlas atlas = new WaySectionProcessor(slicedRawAtlas, AtlasLoadingOption.createOptionWithAllEnabled(boundaries)).run();
```

## Way Sectioning

OSM ways usually span multiple intersections in the case of roads. To make the road network a navigable network, the process of loading an `.osm.pbf` file runs "way sectioning". It will follow a set of rules to break ways at intersections, and create `Atlas` `Edge`s. For that, it pads the OSM feature identifiers with six digits starting from 1 to the number of sections. For example, way 123 would become `Edge`s 123000001, 123000002 and 123000003 if it has to be broken twice. If no sectioning takes place, the edge identifier would end in `000`.

## Country Slicing

In case of building an Atlas that is hard-cut along a polygon (usually a country boundary with boundary=administrative and admin_level=2), the process of loading an `.osm.pbf` file runs "country slicing". All the features that span outside of the boundary will be cut at the boundary and excluded. All the features that are inside will be assigned a country code tag if a country code is given with the AtlasLoadingOption.

The `AtlasLoadingOption` contains all the country boundaries, along with the country codes in a `CountryBoundaryMap`.

All feature identifiers will be padded and the first 3 digits of the 6 digit padding (described above) will be a country counter. If a `Line` 123 spans 2 countries, gets out and comes back for example, it will ship with 123001000 and 123001002 within the first country, and 123002001 in the country where it spans out (in a separate `Atlas`).

## Configuration

Way-sectioning logic, edge definition and which pbf entities (Way, Node, Relation) are brought into an `Atlas` are all configurable. The default configurations can be found in the main resources directory as json files (see atlas-way-section.json for an example of the way section configuration). These configurations are initialized and can be set in `AtlasLoadingOption`.

# Building an `Atlas` from scratch

The `PackedAtlasBuilder` is here for that. It ensures that all the data that makes its way to an Atlas is consistent (for example making sure that if an `Edge` says its start `Node` is 123, then `Node` 123 really exists) and that an Atlas is final and cannot be modified once it has been accessed once.

* First add all the `Node`s
* Then add all the `Edge`s, `Area`s, `Line`s and `Point`s in any order.
* Finally add all the `Relation`s from the lowest order (no other `Relation` is within its members) to the higher order (other `Relation`s are within its members). The `PackedAtlasBuilder` will throw an exception if a `Relation` is added and any of the listed members have not already been added.

# Saving an `Atlas`

The `Atlas` API offers a `save(WritableResource)` method, that is implemented by `PackedAtlas`. Trying to save a `MultiAtlas` will result in an exception suggesting to copy the `Atlas` to a `PackedAtlas` first.

# Copying an `Atlas`

From any `Atlas`, a `PackedAtlas` can be created and saved to a `WritableResource` (A File for example). This is done with the `PackedAtlasCloner`:

```
final Atlas atlas1;
final Atlas atlas2;
new PackedAtlasCloner().cloneFrom(new MultiAtlas(atlas1, atlas2)).save(new File("/path/to/file.atlas"));
```

# Filtering an `Atlas`

Atlas objects can be soft-filtered based on a `Predicate` or a `Polygon`.

```
final Atlas atlas;

final Predicate<AtlasEntity> predicate;
final Atlas predicateAtlas = atlas.subAtlas(predicate);

final Polygon polygon;
final Atlas polygonAtlas = atlas.subAtlas(polygon);
```
