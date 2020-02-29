# AQL Internals

## Understanding the nuts and bolts of AQL

If you plan to modify the AQL code to fix a bug or add a new feature it would be helpful to understand 
Groovy and DSL. At it's core AQL is a Domain Specific Language written in Groovy which acts as a façade
over the atlas api, creating an illusion of being able to "query" the atlas data with an SQL-like language.

## Where can I learn more about Groovy?

Start [here](https://groovy-lang.org/documentation.html#gettingstarted).

## What is a DSL and Groovy DSL?

DSL stands for Domain Specific Language, which is a language created to solve a specific domain of problem.
A good place to start to understand a DSL is [here](https://www.martinfowler.com/dsl.html), it also 
explains the distinction between Internal and External DSLs. AQL would be classified as an "Internal DSL" 
where we are essentially moulding the host language (here Groovy) to make it "look like" SQL.
 
## Critical classes and their purpose?

|Class(es)                                                                              |Purpose                                                                                       |
|---------------------------------------------------------------------------------------|----------------------------------------------------------------------------------------------| 
| org.openstreetmap.atlas.geography.atlas.dsl.query.{QueryBuilderFactory, QueryBuilder} | The QueryBuilderFactory is the entry point for all                                           |
| org.openstreetmap.atlas.geography.atlas.dsl.query.{Select, Update, Delete}            | Logic to actually execute the queries                                                        |
| org.openstreetmap.atlas.geography.atlas.dsl.selection                                 | Logic for the `where` clause and it's conversion to a java `Predicate` and Indexing          |
| org.openstreetmap.atlas.geography.atlas.dsl.mutant                                    | Deals with "mutations" as a result of an `update` or `delete` statement                      |
| org.openstreetmap.atlas.geography.atlas.dsl.schema.AtlasSchema                        | The abstraction layer that makes Atlas look like a DB Schema with tables.                    |
| org.openstreetmap.atlas.geography.atlas.dsl.query.explain.Explainer                   | The contract for the explain plan                                                            |
| org.openstreetmap.atlas.geography.atlas.dsl.query.optimizer.Optimizer                 | The entry point for the Query auto-optimizer                                                 |

## Understanding the components of an AQL query

When you write a query as follows,
```sql
mySelect = select edge.id, edge.startId, edge.endId, edge.tags, edge.start, edge.end from atlas.edge where edge.hasTag(source: 'yahoo') and edge.hasTag('access')
``` 

This is actually,

```groovy
mySelect = QueryBuilderFactory
                .select(AtlasDB.edge.id,
                        AtlasDB.edge.startId,
                        AtlasDB.edge.endId,
                        AtlasDB.edge.tags,
                        AtlasDB.edge.start,
                        AtlasDB.edge.end)
                .from(/*atlas here is an instance of AtlasSchema*/ atlas.edge)
                .where(AtlasDB.edge.hasTag(/*A java.util.Map of size 1*/source: 'yahoo'))
                .and(AtlasDB.edge.hasTag(/*A string*/'access'))
```

Which internally converts to a request to the Atlas API to fetch records.

You will see a similar patter for the `update` and `delete` statements.

Here is an update example,

```sql
myUpdate1 = update atlasSchema.node set node.addTag(abc: "xyz") where node.hasIds(307459622000000, 307446836000000)
```

is actually,

```groovy
myUpdate1 = QueryBuilderFactory
                .update( atlasSchema.node)
                .set( node.addTag(abc: "xyz"))
                .where(node.hasIds(307459622000000, 307446836000000))
```

Similarly for delete,

```sql
myDelete1 = delete atlasSchema.edge where edge.hasTag(highway: "footway") and not(edge.hasTag(foot: "yes"))
```
is internally,

```groovy
myDelete1 = QueryBuilderFactory
                            .delete(atlasSchema.edge)
                            .where(AtlasDB.edge.hasTag(highway: "footway"))
                            .and(
                                    QueryBuilderFactory.not(
                                            AtlasDB.edge.hasTag(foot: "yes")
                                    )
                            )
```

At the point of execution he QueryBuilder builds the query and executes it against the atlas, as seen
here - `org.openstreetmap.atlas.geography.atlas.dsl.query.QueryBuilderFactory.exec`. Commit works on similar
lines as exec - where it executes the query and works with the "affected rows".

## How is the code "actually executed"?

Since AQL is an "Internal DSL" of Groovy - it is technically valid Groovy code. All the code is 
compiled to bytecode and run on the JVM.
 