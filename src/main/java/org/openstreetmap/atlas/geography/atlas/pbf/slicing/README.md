# Slicing

This package contains all the classes that deal with slicing OSM `Way`s and `Relation`s, either across country boundaries or at shape points.

## `CountrySlicingProcessor`

This class splits all `Way`s and `Relation`s that cross country boundaries. The high-level logic is to first slice all ways, then update any affected relations. After this is done, relations are sliced.

## `WaySectionProcessor`

This class splits `Way`s at intersections. Any time there is a shared shape point for two `Way`s, they will be split at the location and new identifiers will be generated for the them using the identifier package.
