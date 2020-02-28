# Indexing and Scan Strategy

## Introduction
Just like a database AQL supports queries that can benefit from an index. An AQL query can use 0 or 1 index.

A scan strategy indicates how are the records scanned to get the "affected records".

Note that while the examples used here are of `select` queries. The concept applies to `where` clauses
and hence applies to `update` and `delete` statements.

## Available Indexes

There are 2 indexes available,

1. Id based index

This is not a real index, however the atlas data is organized by entity type and entity id. 
This makes the data naturally ordered by id and can be used to more effectively "cherry pick" records from an Atlas
when it comes to fetching a record by id.

2. Spatial Index

Atlas internally creates a Quad Tree that can be used for spatial queries.

## Available Scan Strategies

1. Full Scan: Here no index is used, aql simply scans ALL the records in the "table". Example,

```sql
select node._ from atlasSchema.node where node.hasLastUserNameLike(/\w/)
``` 

No index is available when looking for nodes with a certain last user name (here regex).

This means that the `where` clause (including the `and`s, `or`s and `not`s) are converted to a
Java Predicate and executed against the each record in the "table".

2. Id Scan

When aql can directly access the record with the given `id` and doesn't need to scan through all the 
records. Example,

```sql
select node._ from atlasSchema.node where node.hasIds(1, 2, 3) and node.hasLastUserNameLike(/\w/)
```

Here the Id based index is used to fetch 3 records, and once these 3 records are fetched a check is
performed on only these 3 records to see if the last user name matches a certain regex pattern. The
check for last user name is the only part the converts to a predicate to check against each of the 3
records.

3. Spatial Scan

Here the universe of records is narrowed down by traversing through the quad-tree and then on this smaller 
subset the spatial checks are performed to get the relevant records.

```sql
select node._ from atlasSchema.node where node.isWithin(polygon)
```

Here the spatial tree is scanned to get a smaller subset of records that overlap with the "polygon", 
then these records are scanned to check if they are within the "polygon".

## Preferential Order

Users of AQL should strive to write queries that benefit from indexes. Having said that, here is the 
ranking from best to worst in terms of performance (which should come as no surprise),

1. Id based Scan Strategy
2. Spatial Scan Strategy
3. Full Scan Strategy

## When is an Index used (or not used)?

Indexing can be used IFF,
 
1. The FIRST constraint can fetch from an index (say looking upo by an Id)
2. One of the 2 conditions are met,
    1. There is only ONE constraint.
    2. The constrains are conjugated by AND
 
So to put it differently, indexing won't be used, if,

1. There is no `where` clause.
2. The first condition in the `where` clause requires a full scan (say a condition checking for tags).
    * Special Case: The first condition in the `where` clause uses a `not`, example, not(edge.hasId(10)).
3. The `where` clause uses an `or`.

Note that the reasons for non-use of an index can change and an index can be used with
[auto-optimization](AQLOptimization.md), which is explained in the output of the [`explain` command](AQLExplainPlan.md).
