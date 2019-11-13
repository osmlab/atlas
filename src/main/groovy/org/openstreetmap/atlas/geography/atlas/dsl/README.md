# Atlas Query Language (AQL)

# Introduction
Atlas Query Language or AQL is a new feature that allows developers to write queries directly against the Atlas files. This will allow rapid development where developers can write select statements against their Atlas or OSM files locally and then port these queries in their applications (spark or otherwise) to run the same queries at scale.

AQL currently supports using (as in use Atlas/OSM files), select, update, delete, explain (as in explain plan), commit and diff (as in difference between two atlas schemas).

The AQL statements (select, update, delete) support complex where clauses with geospatial and tag querying among other criterion. AQL also supports nested inner queries..

AQL leverages Atlas as the underlying framework and doesn't reinvent the wheels. AQL is efficient in the sense that queries are automatically optimized to benefit from index-ing which can be either atlas id based or geospatial based.

Atlas allows working against all 6 major entities, node, point, edge, line, relation, area, exposed as "tables" in an atlas schema.

# Examples and Usage

# Use(-ing) command

```sql
atlasDNK = using "/tmp/atlas/DNK/"
```

# Select data from "table"

```sql
select node.id, node.osmId, node.tags from atlasDNK.node where node.hasId(123000000) and node.hasOsmId(123) or node.hasTag("amenity": "college")
```

# Update records

```sql
polygon = [
        [
                [12.5210973, 55.662373], [12.521311, 55.6623777], [12.5212936, 55.6624042], [12.5227817, 55.6625331], [12.5210973, 55.662373]
        ]
]

update edge set edge.addTags(hello: "world"), edge.deleteTag("fixme") where edge.isWithin(polygon)
```

# Supported Tables

- node
- point
- line
- edge
- relationship
- area
