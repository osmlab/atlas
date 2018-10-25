# `Raw Atlas Sectioning`

## Overview

Raw Atlas Sectioning is the process of way-sectioning an intermediary Atlas file. As input, the process takes in a sliced raw Atlas and produces a final sectioned Atlas. It's important to note that the sliced raw Atlas contains only Points, Lines and Relations, whereas the final output contains all Atlas entities. One of the main advantages of the raw Atlas sectioning logic is the guarantee of consistent `Edge` identifiers. This means that if an OSM Way spans multiple Shards, the corresponding Atlas `Edge` will have consistent identifiers for the same geometry, irrespective of which `Shard` that `Edge` is queried from. Under the covers, the sectioning logic relies on a `DynamicAtlas` to be able to follow an OSM Way to its completion, capture any intersections that the Way may have, section the full view of the `Edge` and save the portion that belongs inside the boundary of the `Shard` being processed.

## Detailed Steps

The sectioning process is comprised of the following steps:

1. Assemble a `DynamicAtlas` that will be used for sectioning. The reason for a `DynamicAtlas` is to be able to capture all OSM Ways that go outside the `Shard` bounds being processed and also capture any intersections of the each of these ways. This is the driving force behind the guarantee of consistent `Edge` identifiers. If we can see a way in its entirety as well as all of its intersecting ways, we can section it deterministically and consistently, irrespective of which `Shard` we are looking from and apply this consistent view to the current `Shard` being processed. The logic for building the `DynamicAtlas` is to grab the full Atlas for the initial `Shard`, then proceed to expand out to surrounding Shards if there are any Edges bleeding over the initial `Shard` boundary. One caveat to call out here is that the `Sharding` being passed in, **must** be the same `Sharding` that was used to generate the input sliced raw Atlas. If it's not, there may be inconsistencies in the understanding of the surrounding shards that are available.

2. Stream all the input Lines and identify the ones that becomes Edges or Areas. If a `Line` is set to become an `Edge`, we identify all location-based Nodes for that `Edge`. A `Node` will be created in one of the 4 cases:
    1. A self-intersection - there is a repeated non-consecutive intersection in the `Edge`.
    2. Configuration-based sectioning dependent on tagging (ex: section at a barrier).
    3. Intersection with another `Edge` with the same `LayerTag` value.
    4. Intersection with another `Edge` with a different `LayerTag` value, that either starts or ends at the point of intersection.
	
3. Stream all the input Points and distinguish between the ones that are shape points of underlying Atlas entities versus ones that become separate Atlas Points in the final Atlas. Most Points will become shape points and will be tracked by the underlying `PolyLine` or `Polygon` representation of the `Area`, `Line` or `Edge` that it's a part of.

4. Stream all the Lines that have been identified as Edges from step 2 and split them into one or more `Edge`. This step will take care of both open and closed Edges and handle forward and reverse `Edge` creation. It's important to note that the logic for sectioning closed Edges is slightly more complex since we try to avoid sectioning at the arbitrary start location where the OSM Way was drawn from. Instead, we try to section at any intersecting location with other Edges or if those don't exist, we have a single `Edge` that starts and stops at the first `Location` of the underlying `PolyLine`.

5. Build the final Atlas. This step makes uses of steps 1-4 and has the important role of updating all Relations to use the newly created Atlas entities to properly replace the previous members that may have been updated or deleted. 

## Synthetic Tags

The sectioning process add a new synthetic tag as part of the final Atlas - `SyntheticInvalidWaySectionTag`. This tag signifies that we couldn't fully section the given Atlas Line. Specifically, we created 998 Edges and put the rest of the un-sectioned remnant into `Edge` 999. This is usually an indicator of bad data. We have seen this in the case of really long duplicated OSM Ways. If the two Ways are stacked and have over 1000 shape points, then each shape point would become an Atlas `Node` and result in sectioning. This is technically also possible for really long valid OSM Ways that have more than 999 intersections for the duration of the Way.

## OSM to Atlas Feature Mapping

### OSM Node to Atlas Point or Node

An OSM Node will become an Atlas `Node` if it's connected to an end of an OSM Way that becomes an Atlas `Edge`. For that definition, please see the section below. 

An OSM Node will become an Atlas `Point` in the following 3 cases:
1. The OSM Node has explicit tagging. For example, a highway=traffic_signals tag would result in an Atlas `Point` at that location.
2. The OSM Node does **not** have explicit tagging, is part of a Relation and is **not** an Atlas `Node` (not at an intersection or end of an `Edge`).
3. The OSM Node does **not** have explicit tagging, is **not** part of a Relation and is **not** part of an OSM Way. This case is used to differentiate un-tagged OSM Nodes from OSM Nodes that are shape points for some OSM Way.

It's worth pointing out that some OSM Nodes will become both Atlas Points and Nodes. An example of this may be an OSM Node that has tagging and ends up at an intersection. In this case, it will become both an Atlas `Point` and an Atlas `Node`.

### OSM Way to Atlas Line, Edge or Area

For starters, we define an open OSM Way as a linear feature which does not share a first and last node. Conversely, a closed OSM Way has the last node shared with its first node. In general, the OSM Way to Atlas Entity translation is largely configuration driven. Please see the [pbf resource package](https://github.com/osmlab/atlas/tree/dev/src/main/resources/org/openstreetmap/atlas/geography/atlas/pbf) for a full view of default configurations. However, the generally accepted convention supported by the rest of the code base is that any navigable, open OSM Way (ex: highway, ferry, pier) becomes an Atlas `Edge`. An OSM Way that's non-navigable and open (ex: power line, wall) becomes an Atlas Line and anything that's non-navigable and closed becomes an Atlas `Area`.

### OSM Relation to Atlas Relation

An OSM Relation maps directly to an Atlas Relation. An Atlas Relation can be any combination of Atlas Points, Nodes, Edges, Lines and Areas.

## Code Sample

The sectioning code is pretty simple to use. It supports two basic use cases:
1. Given a sliced raw Atlas and an `AtlasLoadingOption` - run the sectioning processor and return the final Atlas.
2. Given some `Shard`, an `AtlasLoadingOption`, a way to understand what Shards are available and a way to fetch those Shards - run the sectioning processor and return the final Atlas.

Here is a simple example of sectioning that only requires a `CountryBoundaryMap` and a pre-created sliced raw Atlas:

```java
// Country boundary shapes backed by a spatial index
final CountryBoundaryMap countryBoundaryMap;

// A pre-generated sliced raw atlas
final Atlas slicedRawAtlas;

final AtlasLoadingOption loadingOption = AtlasLoadingOption.createOptionWithAllEnabled(countryBoundaryMap);
final Atlas atlas = new WaySectionProcessor(slicedRawAtlas, loadingOption).run();
```

Here is complex, but more generic, example that invokes the use of a `Sharding` and fetcher policy:

```java
// Country boundary shapes backed by a spatial index
final CountryBoundaryMap countryBoundaryMap;

// The shard being processed 
final Shard shard;

// A pre-generated sliced raw atlas
final Atlas slicedRawAtlas;

// The fetching policy used under the covers by the DynamicAtlas to obtain adjacent sliced raw atlas files
final Function<Shard, Optional<Atlas>> rawAtlasFetcher;

// The sharding policy used to identify which shards to fetch
final Sharding sharding;

final AtlasLoadingOption loadingOption = AtlasLoadingOption.createOptionWithAllEnabled(countryBoundaryMap);
final Atlas atlas = new WaySectionProcessor(shard, loadingOption, sharding, rawAtlasFetcher).run();
```
