# BigNode

BigNode is a concept of "augmented" intersection. It should match single decision points for a driver. This is mostly useful when inferring turn restrictions.

## Structure

A BigNode is a group of Nodes, each one Edge away from at least one other, and close to each other.

For example, two dual carriageways, when intersecting each other, can be modeled by a BigNode made of 4 Nodes at the 4 intersection points. The 4 internal Edges are referred to as "Junction Edges", and the edges directly connected to one of the Nodes, but at one end only, are the "in-Edges" and "out-Edges" to the BigNode.

## Path

A BigNode can provide a set of Route objects called paths. Each path is the shortest way through the BigNode from one in-Edge to one out-Edge.

### RestrictedPath

A RestrictedPath is a BigNode path that is restricted because of some TurnRestriction. It can be direct with a "NO\_TURN" type restriction, or indirect with a "ONLY" restriction that the path is close to but not following (For example some BigNode has a right\_turn\_only restriction, then the path that goes straight and the one turning left will be restricted).

## Visualization

It is easy to visualize all the BigNodes and inferred RestrictedPaths by calling the following methods, provided an Atlas:

```
BigNodeFinder.findAndSaveBigNodesAsGeoJson(Atlas, WritableResource);
```

and

```
BigNodeFinder.findAndSaveRestrictedPathsAsGeoJson(Atlas, WritableResource);
```
