# `Raw Atlas Slicing`

## Overview

Raw Atlas Slicing is the process of assigning country codes to all Atlas features given an intermediary raw Atlas file. As input, the process takes in a raw Atlas and produces a sliced raw Atlas. It's important to note that both the input and output Atlas files will contain only Points, Lines and Relations. One of the main advantages of the country slicing process is that it will leverage the `DynamicAtlas` and attempt to build Relations that span multiple shards in their entirety. Once the Relation is built, it can be properly sliced along the given country boundaries and as a result, each country can contain valid, country-specific portions of multi-polygons that span one or more countries. In accomplishing this, we can achieve full OSM parity and be able to ingest OSM administrative boundaries and coast lines. Both of these features are currently left out of the Atlas.

## Terminology

This section calls out the main terms and concepts that are key to the slicing code.

* We rely on the JTS (Java Topology Suite) library for all geometric operations (spatial queries, intersection and cutting requests) and some key data structures (R-tree, JTS geometry representations).
* The `CountryBoundaryMap` class is the main driver behind all slicing operations. This class contains the raw country boundary `MultiPolygon` representations as well as an underlying optimization (the grid index) that allows for quick lookup to see whether a specific feature is contained in a boundary.   
* The main drivers behind the slicing code are the `RawAtlasPointAndLineSlicer` and the `RawAtlasRelationSlicer`. The `RawAtlasPointAndLineSlicer` is responsible for slicing all Points and Lines, updating Relations with any member changes as a result and rebuilding the Atlas in an intermediate state to set it up for Relation slicing. In turn, the `RawAtlasRelationSlicer` then slices all of the Relations - ensuring that if a Relation contains members found in multiple countries, that Relation is created for each corresponding country.
* The country-slicing code follows a pattern of creating change sets for both slicing iterations and using corresponding change set builders to apply the changes and rebuild the intermediate and final Atlas.

## Detailed Steps

The slicing process is comprised of the following steps:

1. First, slice all of lines in the given Atlas. This involves converting each Atlas `Line` to a JTS Geometry entity, performing the actual slice operation using the `CountryBoundaryMap` class and finally assigning the proper `ISOCountryTag`, `SyntheticBoundaryNodeTag` and `SyntheticNearestNeighborCountryCodeTag` to the resulting line features. We must also assign country codes to all `Point` features that are part of the sliced `Line` feature. During this process, there must be careful considerations for several cases. The first case is to keep track of when to create new Points at the country boundary for cases when a feature is crossing boundaries one or more times. It's also vital to keep a mapping between the newly created Lines and the one that was sliced, so that the appropriate Relations can be updated and remain valid.

2. Once the line slicing is completed - we need to look at any points that haven't been assigned a country code. These are Points that were not part of a `Line` feature - examples include stand alone Points such as trees, barriers, etc. This is one of the simpler slicing operations - since it's just a location containment check using the underlying grid index from the `CountryBoundaryMap`. 

3. Once both Points and Lines have been sliced, an intermediate Atlas is built to be able to slice Relations. The intermediate changes are aggregated using a `SimpleChangeSet` and applied with the `SimpleChangeSetHandler`. It's important to note that during the build - we must replace any Lines that were sliced by their corresponding sliced Lines. 

4. This next step is to slice all Relations. We care mostly about the `MultiPolygon` and `Boundary` type Relations, since these are the relations that will combine their members to form a specific `Polygon` or enclosed area. All other Relations represent more abstract notions (such as turn restrictions or routes) and can be (for the most part) handled by simple grouping individual members organized by country code. To handle the `MultiPolygon` and `Boundary` type relations, we take the following steps:
    1. We first group together all the inner and outer members so we have fast access to each
    2. We then build all of the outer and inner rings, if it's possible (invalid Relations may exist)
    3. Convert the combination of outer and inner members into a JTS `Polygon`
    4. We clip the resulting `Polygon` along the country boundary
    5. If there was no clipping - then no action is needed, the Relation falls fully within a country. If there was a clipping, then we need to create new Points, Lines and update the Relation to include these as members. As a result, we have effectively patched the MultiPolygon Relation to be closed at the country boundary. 
    6. Once the cutting has been done, we try to reconcile any Relation Role inconsistencies that may have arisen as a result. Essentially, we are trying to merge any inners and outers into a single member. To achieve this, we create inner and outer lists of closed members, find any intersection location between the inners and outers and for any such intersection, we take the JTS difference of the two, updating our Points, Lines and Relation members for the new piece that may have been created.

5. Once the MultiPolygon-specific logic has been executed, the next step is to loop through all the Relations and group the members by country. If there is more than a single country present, then the Relation is split into two or more separate Relations, one for each country present.

6. Once all Relations have been sliced and assigned country codes, the final sliced Atlas is built by applying the `RelationChangeSet` changes using the `RelationChangeSetHandler`.

Note: A step needs to be added between 3) and 4) to leverage `DynamicAtlas` to be able to properly build Relations that span multiple shards. This will allow for proper processing of large water bodies, coast lines and administrative boundaries.

## Synthetic Tags

There are two synthetic tags generated by the country-slicing process:

1. The `SyntheticRelationMemberAdded` - which indicates a Relation that had an added member as a result of country-slicing. An example includes closing a water body MultiPolygon relation with a new `Line` member that runs along a country boundary.
2. The `SyntheticRelationRoleUpdated` - which indicates a Relation member role update, any time that some combination of inner/outer relation members was merged.

## Code Sample

The slicing code has a single entry point in the `RawAtlasCountrySlicer` class and has two basic use cases:

1. Slice against a given country and `CountryBoundaryMap`.
2. Slice against a given set of countries and `CountryBoundaryMap`.

Below are examples of each case:

```java
// Country boundary shapes backed by a spatial index
final CountryBoundaryMap countryBoundaryMap;

// Set of ISO-3 country codes that will be sliced against
final Set<String> countries;

// A pre-generated raw atlas
final Atlas rawAtlas;

final Atlas slicedRawAtlas = new RawAtlasCountrySlicer(countries, countryBoundaryMap).slice(rawAtlas);
```
and:

```java
// Country boundary shapes backed by a spatial index
final CountryBoundaryMap countryBoundaryMap;

// A pre-generated raw atlas
final Atlas rawAtlas;

// Slicing against target country with iso-3 country code of ABC
final Atlas slicedRawAtlas = new RawAtlasCountrySlicer("ABC", countryBoundaryMap).slice(rawAtlas);
```
