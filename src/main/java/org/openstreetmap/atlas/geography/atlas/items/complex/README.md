# Complex Entities

Complex entities are higher level features made of relationships between Atlas features. Those complex entities are never stored in the Atlas directly, but rather built on demand using Finders.

## Some examples

### ComplexTurnRestriction

One relation with type=restriction and from, via and to members. This is aggregated to build the restricted route and fails to build if the relation is malformed.

### ComplexBuilding

One building with multiple parts, or a hole. The Finder reads the relations with the right tags and members, and provides a ComplexBuilding that can return the built-out `MultiPolygon` object.
