# `Atlas Sectioning`

## Overview

Atlas Sectioning is the process of converting certain `Lines` and `Points` into navigable `Edges` and `Nodes`. As input, the process takes in an Atlas with no `Edges` or `Nodes` and produces one that has these entities. 

## Detailed Steps

The sectioning process is comprised of the following steps:

1. Assemble a `DynamicAtlas` that will be used for sectioning. The reason for a `DynamicAtlas` is to be able to capture all OSM Ways that go outside the `Shard` bounds being processed and also capture any intersections of the each of these ways. This is the driving force behind the guarantee of consistent `Edge` identifiers. If we can see a way in its entirety as well as all of its intersecting ways, we can section it deterministically and consistently, irrespective of which `Shard` we are looking from and apply this consistent view to the current `Shard` being processed. The logic for building the `DynamicAtlas` is to grab the full Atlas for the initial `Shard`, then proceed to expand out to surrounding Shards if there are any Edges bleeding over the initial `Shard` boundary. One caveat to call out here is that the `Sharding` being passed in, **must** be the same `Sharding` that was used to generate the input raw Atlas. If it's not, there may be inconsistencies in the understanding of the surrounding shards that are available.


2. Iterate over all the input `Line`s and identify the ones that become `Edges`, based on the tags for the `Line`. 
    1. If a `Line` is set to become an `Edge`, we identify all location-based `Nodes` for that `Edge`. A `Node` will be created in one of the 4 cases:
        * A self-intersection - there is a repeated non-consecutive intersection in the `Edge`
        * Configuration-based sectioning dependent on tagging (ex: section at a barrier)
        * Intersection with another `Edge` with the same `LayerTag` value
        * Intersection with another `Edge` with a different `LayerTag` value, that either starts or ends at the point of intersection
    2. If the `Line` is closed, apply some consistency logic to ensure we get a good sectioning result
        * If there are only 2 `Node` candidates, then check to see if it's a basic loop with a single `Node` at the beginning/end of the `Edge`
            * If so, make it into one `Edge`
            * Otherwise, it's disconnected from the rest of the `Edge` network, so split it artificially at the halfway point and make two `Edge`s for the loop
        * If it has more than 2 `Nodes`, put the last `Edge` section into a "remainder". If the beginning/end of the `Line` isn't connected to anything else, then we'll add the remainder to it so we don't create excess `Edge`s. Otherwise, we'll make an `Edge` from the remainder
    3. Using the calculated `Node` candidates and remainder geometry, make `Node` and `Edges` for the `Line`
    4. Remove the old `Line` from the `Atlas`
3. Iterate over the staged CompleteNode entities and add them as FeatureChanges-- waiting until after all the `Edges` have been processed ensures this process just simplifies things from a consistency standpoint
4. Iterate over all remaining `Points` in the `Atlas` and remove them if they don't have explicit tags or belong to any `Relations` 
5. If there were changes, make a new `ChangeAtlas` and return it, otherwise return the original `Atlas`

## Synthetic Tags

The sectioning process add a new synthetic tag as part of the final Atlas - `SyntheticInvalidWaySectionTag`. This tag signifies that we couldn't fully section the given Atlas Line. Specifically, we created 998 Edges and put the rest of the un-sectioned remnant into `Edge` 999. This is usually an indicator of bad data. We have seen this in the case of really long duplicated OSM Ways. If the two Ways are stacked and have over 1000 shape points, then each shape point would become an Atlas `Node` and result in sectioning. This is technically also possible for really long valid OSM Ways that have more than 999 intersections for the duration of the Way.
