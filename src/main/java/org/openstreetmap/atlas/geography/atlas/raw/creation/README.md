# `Raw Atlas Creation`

## Overview

This package is responsible for the initial OSM PBF file to Atlas file transformation. The result is an intermediate Atlas file, that is not country-sliced or way-sectioned. The advantage to this is that we get to leverage the Atlas API layer and most importantly, the spatial index to make slicing and sectioning as straight-forward as possible.

## Raw Atlas Terminology

### Stages

The raw Atlas flow is comprised of three separate stages. They're explained in detail below:

1. Raw Atlas - this is the first Atlas type that is created. It's a direct representation of the PBF data in Atlas format. This Atlas contains only Points, Lines and Relations. It does **not** contain country code assignment or way-sectioning. 

2. Sliced Raw Atlas - the sliced raw Atlas is made up of Points, Lines and Relations with each one containing one or more country code for which country it belongs to. For details on this process, see the Slicing package README file.

3. Sectioned Raw Atlas - this is the final sliced and sectioned artifact, which is made up of all Atlas entities - Points, Nodes, Edges, Lines, Areas and Relations and is fully ready for processing.

### General Principles

There are a few general principles that are observed throughout the ingest process. 

1. The desired goal is to achieve parity with OSM and get as accurate a representation as possible - including ingesting bad data. The reasoning for this is to be able to leverage the Atlas and write [atlas-checks](https://github.com/osmlab/atlas-checks) to detect bad data, which will lead to OSM data fixes.
2. Do all complex processing once. If we have access to all the data during the ingest piece and if we're building MultiAtlases to country-slice and section the road network correctly, bundle all similar processing together to save any downstream users the hassle of having to re-build MutliAtlases or make inferences based on impartial Atlas views.  
3. Rely on synthetic tags. Instead of silently fixing bad data or making data-altering decisions - rely on synthetic tags where possible to identify specific cases that may require additional attention or custom atlas-checks.

### Temporary Entities

The concept of a Temporary Entity is present across both slicing and sectioning code. Because each stage results in the creation of new Atlas entities or the manipulation of Relations and their members, there needs to be a very simple way to track changes and apply them when rebuilding the Atlas. The concept of Temporary Entity comes in here. The idea is that a temporary Atlas Entity contains the bare minimum required to construct some `AtlasEntity`. At its most basic state, this is an identifier and a collection of tags. Depending on the type of entity, there may also be some geometry or Relation properties. See the [raw temporary package](https://github.com/osmlab/atlas/tree/dev/src/main/java/org/openstreetmap/atlas/geography/atlas/raw/temporary) for all temporary entities.

## Raw Atlas Implementation Details

The PBF ingestion process is configuration driven. This means that the user has full control of what features end up in the final Atlas. The default configuration, found in the [pbf resource package](https://github.com/osmlab/atlas/tree/dev/src/main/resources/org/openstreetmap/atlas/geography/atlas/pbf), attempts full parity with OSM - meaning it ingests almost all features.

There are a couple of implementation details to call out for the raw Atlas creation. The input protobuf file is created using the [Osmosis library](https://github.com/openstreetmap/osmosis) and it's structured with a distinct order - the file contains the Nodes first, then the Ways and lastly the Relations. Each Way references the Node identifiers that are used to construct itself. This is something problematic, because we have no Node location or tag properties of the individual Nodes when processing each Way. To solve this, we must either create a Node map or make two passes over the PBF file - once to read the ways and a second time to read the Nodes for the Ways we're interested in. It's a lot faster to read the file twice, rather than resize the underlying `PackedAtlas` arrays during build time. In the actual implementation, the `OsmPbfCounter` class is responsible for identifying what to bring in, keeping track of relevant Nodes and counts. The `OsmPbfReader` will then go through the file a second time and build the raw Atlas using the information from the counter.

## Synthetic Tags

The raw Atlas creation process add a new synthetic tag as part of the final Atlas - `SyntheticDuplicateOsmNodeTag`. This tag signifies that the input OSM data contains two or more stacked Nodes at the same Location. This is almost always a data error, that we are handling graciously and deterministically. The Node with the lowest identifier is kept, while all others are excluded from the final Atlas. There are two caveats to call out here. The first caveat is that if there are Nodes with different `LayerTag` values at the same `Location`, we will keep the lowest occurring Node for each layer in order to preserve proper connectivity. The second caveat is that we can potentially remove a Node that has rich tagging and keep the Node that has no tagging. This is a potential problem, but the only way to ensure deterministic processing. Ideally, the presence of this synthetic tag will prompt the creation of an atlas-check that will result in data fixes for such cases.  

## Raw Atlas Ingestion Logic

The specific logic for what gets ingested into the raw Atlas is as follows:

1. For each OSM Node, if it's inside the `Shard` boundary that's being processed, bring it in. As a side note, we keep track of all Nodes that were not brought in, in case we have to pull them in later. 
2. For each OSM Way, if the Way has a Node that was brought in, bring in the entire Way and all other Nodes that are part of this Way (since they may not have been originally brought in as they could be outside the target `Shard` boundary).
3. For each OSM Relation, process Relations in a queue structure - where we only look at Relations that have no member Relations or have had their member Relations already processed. For each Relation, if it contains a member that was brought into the Atlas (Node, Way or Relation) then bring in this Relation in to the Atlas. 

## Code Sample

There are a few ways to create raw Atlas files. Each one is outlined here. 

The first and most simple one is to build a raw Atlas from some PBF resource, irrespective of `Shard` boundary or any kind of `AtlasLoadingOption`:

```java
final String pbfPath = "/path/to/pbf/resource";

final RawAtlasGenerator rawAtlasGenerator = new RawAtlasGenerator(new File(pbfPath));
final Atlas rawAtlas = rawAtlasGenerator.build();
```

The second way is to build a raw Atlas given a PBF resource and a specific boundary of interest:

```java
final String pbfPath = "/path/to/pbf/resource";

// The boundary of interest
final MultiPolygon boundary;

final RawAtlasGenerator rawAtlasGenerator = new RawAtlasGenerator(new File(pbfPath), boundary);
final Atlas rawAtlas = rawAtlasGenerator.build();
```

Lastly, we can specify all three fields of interest - the PBF resource, the specific `AtlasLoadingOption` and boundary of interest:

```java
final String pbfPath = "/path/to/pbf/resource";

// The boundary of interest
final MultiPolygon boundary;

// Country boundary shapes backed by a spatial index
final CountryBoundaryMap countryBoundaryMap;

final AtlasLoadingOption loadingOption = AtlasLoadingOption.createOptionWithAllEnabled(countryBoundaryMap);
final RawAtlasGenerator generator = new RawAtlasGenerator(new File(pbfPath), loadingOption, boundary);

final Atlas rawAtlas = rawAtlasGenerator.build();
```