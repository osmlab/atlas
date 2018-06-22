# Atlas

[![Build Status](https://travis-ci.org/osmlab/atlas.svg?branch=master)](https://travis-ci.org/osmlab/atlas)

[`Atlas`](src/main/java/org/openstreetmap/atlas/geography/atlas/Atlas.java) is a way to efficiently represent [OpenStreetMap](http://www.openstreetmap.org/) data in memory. A subset of the data is in a "navigable network" form, meaning anything that is assumed to be navigable will be in a form of `Node`s and `Edge`s in a way a routing algorithm could traverse it. It also provides easy to use APIs to access geographical data. On top of it all, it is easy to shard and re-stitch, making it perfect for distributed processing!

Projects using Atlas:
* [atlas-generator](https://github.com/osmlab/atlas-generator): A Spark job to distribute Atlas shards generation
* [atlas-checks](https://github.com/osmlab/atlas-checks): A suite of tools to check OSM data integrity using Atlas, and Spark.
* [josm-atlas](https://github.com/osmlab/josm-atlas): A JOSM plugin to visualize Atlas data.

# Getting started

For build instructions and to contribute, please see the [contributing guidelines](CONTRIBUTING.md).

# APIs

Language|Level
---|---
[Java](/src/main/java/org/openstreetmap/atlas/geography/atlas#using-atlas)|Full feature
[Python](/pyatlas#pyatlas)|Basic

# What's in it?

* A uni-directional navigable network ([`Edge`](src/main/java/org/openstreetmap/atlas/geography/atlas/items/Edge.java), [`Node`](src/main/java/org/openstreetmap/atlas/geography/atlas/items/Node.java))
* Non-navigable features ([`Area`](src/main/java/org/openstreetmap/atlas/geography/atlas/items/Area.java), [`Line`](src/main/java/org/openstreetmap/atlas/geography/atlas/items/Line.java), [`Point`](src/main/java/org/openstreetmap/atlas/geography/atlas/items/Point.java), [`Relation`](src/main/java/org/openstreetmap/atlas/geography/atlas/items/Relation.java))
* All tags

As well as other handy tools:

* [Create it from `.osm.pbf`](/src/main/java/org/openstreetmap/atlas/geography/atlas#building-an-atlas-from-an-osmpbf-file)
* [Sharding](/src/main/java/org/openstreetmap/atlas/geography/sharding#sharding)
* [Shard Stitching](/src/main/java/org/openstreetmap/atlas/geography/atlas/multi#multiatlas)
* [Shard Exploration](/src/main/java/org/openstreetmap/atlas/geography/atlas/dynamic#dynamicatlas)
* [Filtering](/src/main/java/org/openstreetmap/atlas/geography/atlas#filtering-an-atlas)
* [Cutting](/src/main/java/org/openstreetmap/atlas/geography/atlas#country-slicing)
* [Routing](/src/main/java/org/openstreetmap/atlas/geography/atlas/routing#routing)
* [Higher-level entities](/src/main/java/org/openstreetmap/atlas/geography/atlas/items/complex#complex-entities)
* [Saving](/src/main/java/org/openstreetmap/atlas/geography/atlas#saving-an-atlas) / [Loading](/src/main/java/org/openstreetmap/atlas/geography/atlas#using-atlas)

# Community

For more information, please contact our community projects lead [Andrew Wiseman](https://github.com/awisemanapple).
