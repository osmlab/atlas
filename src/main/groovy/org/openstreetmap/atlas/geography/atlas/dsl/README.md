# Atlas Query Language (AQL)

# Introduction
Atlas Query Language or AQL is a new feature that allows developers to write queries directly against the Atlas files. This will allow rapid development where developers can write select statements against their Atlas or OSM files locally and then port these queries in their applications (spark or otherwise) to run the same queries at scale.

AQL currently supports using (as in use Atlas/OSM files), select, update, delete, explain (as in explain plan), commit and diff (as in difference between two atlas schemas).

The AQL statements (select, update, delete) support complex where clauses with geospatial and tag querying among other criterion. AQL also supports nested inner queries..

AQL leverages Atlas as the underlying framework and doesn't reinvent the wheels. AQL is efficient in the sense that queries are automatically optimized to benefit from index-ing which can be either atlas id based or geospatial based.

Atlas allows working against all 6 major entities, node, point, edge, line, relation, area, exposed as "tables" in an atlas schema.

# Quick Start Guide

The easiest way to get started is to have an IDE with Groovy support. Ensure that you have Atlas 
as a dependency. IntelliJ with Groovy plugin offers excellent support including auto-complete of
queries as demonstrated below.

- Imports

Create a groovy file with 2 imports listed below. Notice these are `static` star imports,

```groovy
/*Optional package statement if applicable.*/

import static org.openstreetmap.atlas.geography.atlas.dsl.query.QueryBuilderFactory.*
import static org.openstreetmap.atlas.geography.atlas.dsl.schema.AtlasDB.getEdge
```

- Load Atlas (or OSM) file(s)

```sql
/*
examples,
a. One Atlas File on disk:- file:///home/me/dir1/dir2//ButterflyPark.atlas
b. One Atlas File omn disk (without explicitly mentioning file: scheme):- /home/me/dir1/dir2//ButterflyPark.atlas
d. All Atlas files in a directory:- /home/dir1/dir2/allAtlasesInsideDirNoRecurse
e. A file from the classpath:- classpath:/data/ButterflyPark/ButterflyPark.osm
f. Multiple files from classpath:- classpath:/atlas1/something1.atlas,something2.atlas;/atlas2/Alcatraz.atlas,Butterfly.atlas
*/
myAtlas = using "<your atlas or OSM file>"
```

- Write queries against `myAtlas`

Here are some examples,

* Select everything from node "table"

```sql
select node._ from myAtlas.node
```

* Select with where clause

```sql
select node.id, node.osmId, node.tags from myAtlas.node where node.hasId(123000000) or node.hasTag("amenity": "college")
```

* Update statement

```sql
update myAtlas.node set node.addTag(hello: "world") where node.hasIds(123456789, 9087655432)
```

* Delete statement

```sql
delete myAtlas.edge where edge.hasTag(highway: "footway") and not(edge.hasTag(foot: "yes"))
```

* An example where we commit to create a new Atlas (schema),

```sql

/*Load*/ 
atlas = using "classpath:/data/Alcatraz/Alcatraz.osm"

/*Run some select queries to explore the data.*/
select edge._ from atlas.edge limit 100

/*
Run some update and/or delete statement(s)
*/
update1 = update atlas.edge set edge.addTag(website: "https://www.nps.gov/alca") where edge.hasTagLike(name: /Pier/) or edge.hasTagLike(name: /Island/) or edge.hasTagLike(name: /Road/)
update2 = update atlas.edge set edge.addTag(wikipedia: "https://en.wikipedia.org/wiki/Alcatraz_Island") where edge.hasTagLike(/foot/)

/*
Note edge.hasTagLike(name: /Pier/), in groovy you can use forward slashes instead of double 
quotes to work with RegEx and that allows you to skip the double escaping that we do in Java 
when working with RegExes.
*/

/*
Commit the changes
*/
changedAtlas = commit update1, update2

/*
Run Selects against the new atlas schema, notice changedAtlas.edge instead of atlas.edge here,
*/
select edge._ from changedAtlas.edge where edge.hasTag("website") or edge.hasTag("wikipedia")

/*
Run some commands like diff and explain plan
*/
diff atlas, changedAtlas

explain update1
```

# Supported Tables

- node
- point
- line
- edge
- relation
- area
