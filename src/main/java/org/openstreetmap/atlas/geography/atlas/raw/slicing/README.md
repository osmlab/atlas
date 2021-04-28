# `Raw Atlas Slicing`
## Overview
Slicing is the operation of taking an `Atlas` and tagging all of its entities with country codes, as well as ensuring that *only* entities from the loaded country code persist in the output `Atlas`. While this operation may sound straightforward, data complexities frequently result in corner cases and complicated calculations in order to make sure the entities in the output `Atlas` are sensible. The term "slicing" describes these more difficult cases because the geometry of an entity frequently spans multiple countries, and thus must be "sliced" into portions that either a) are contained entirely in only one country boundary polygon or b) exactly along the shared overlap of multiple country boundary exterior rings and thus have geometry shared by a set of countries.  

### Initialization
The slicer is constructed with a minimum input of `Atlas` and `AtlasLoadingOption`
* The `Atlas` should be raw, i.e. not previously sliced and consisting of only `Line`, `Point`, and `Relation` entities
* The `AtlasLoadingOption` will use the following paramters: 
    * `CountryBoundaryMap` to determine country boundary definitions
    * `countryCodes` to determine which entities to keep in the final Atlas-- any entity whose country code is *not* in the `countryCodes` set will be removed from the final tlas!
    * `relationSlicingFilter` to determine which `Relations` to expand the `DynamicAtlas` on and attempt to slice-- at a minimum, these relations must be `type->multipolygon` or `type->boundary`, but additional tagging critieria are acceptable here
    * `edgeFilter` to determine which `Line` entities should be considered future `Edge` entities
	    * This is important because closed `Line` entities will be sliced as two-dimenstional polygons but closed `Edge` entities (such as a traffic circle) will be sliced as linear features

If the constructor that takes in a `Sharding` object and `Atlas` fetcher function is used, then additionally the initial `Atlas` will be converted to a `DynamicAtlas` expanded on the `relationSlicingFilter` in the `AtlasLoadingOption`.
Once the constructor is called, three maps are initialized for `CompleteEntity` representations of all three `AtlasEntity` types. These objects will be used to track changes made during the slicing operation.

### Slicing Steps
The high level operations of slicing are executed in the `slice()` method, and can be summarized as follows:
1. Slice all `Line` entities in the `Atlas` following a basic logic fork:
	* If the `Line` entity is closed (i.e. a loop) and neither a future `Edge` or a multipolygon `Relation` member, then slice it as an `Area` (2d geometry)
	* Otherwise, slice it as a linear entity
2. Slice all multipolygon type `Relation` entities
3. Slice all `Point` entities
4. Filter any remaining `Relation` entities-- because we don't expand on all `Relations`, it's impossible to deterministically "slice" these `Relations`, so instead we just filter out any members that are outside the country code set being sliced and update the country tag for the `Relation` to be the sum of its remaining members.
5. Add all `CompleteEntities` in the staged entity maps as `FeatureChange.ADD`-- these are either existing features being updated or new entities being added, so `FeatureChange.ADD` is always appropriate
6. Build a new `ChangeAtlas` out of these changes, then cut out any entities that lay outside the shard bounds (frequently happens for data loaded in during the `DynamicAtlas`expansion for `Relations`)

### Geometry Slicing
Slicing of all entities follows the same general approach. 
1. The data for the entity is constructed into a relevant JTS geometry. 
    * For example, for a closed `Line` that meets the criteria for an `Area`, a JTS `Polygon` is created
2. This internal envelope for this geometry is checked against the spatial index of the boundary map to calculate which country boundary polygons it intersects
    * Should this geometry exclusively belong to one country, its geometry is left unaltered and the country code tag is updated to contain this country
3. Next, geometry is checked for validity-- it must be [valid geometry](http://www.ogc.org/docs/is/) and not empty
    * This is critical because if we filter out occasional invalid geomtery coming out of the slicing operation; inputting invalid geometry is a guarantee of junk output
    * If it's invalid, then we remove it from the Atlas
4. The JTS geometry is then divided into the portions intersected by each country boundary polygon, creating a map of country code to geometric pieces.
    * ALL of these pieces must meet the requirements for validity and significance, because incredibly small lines or polygons are likely junk data or irrelevant
    * Should the operation return a GeometryCollection, all geometries in the collection are separated and added to the results Set independently after being checked for validity and significance
5. The results are post-processed based on the type of AtlasEntity
    * These operations are largely similar but there are a few different expectations based on AtlasEntity type-- for example, a sliced linear `Line` will attempt to join all resulting `LineString` pieces for each country using `LineMerger` because occasionally a few of these `LineStrings` can be merged
    * Additionally, the map returned here will use `SortedMap` to guarantee deterministic consistency in `Line` slice creation regardless of what country code settings are used, etc.
6. On the small chance that the slicing operation returned an empty set or all significant geometry was in exclusively one country, the geometry will be unaltered and the country code tag will be updated to have either the country-missing value or the single country only, respectively
7. Additionally, should the number of slices be greater than the country identifier space (000-999), then the operation will fail out and the entity will be tagged with all country codes its geometry spanned
8. Finally, the slice geomtries will be converted sequentially based on their SortedMap ordering into new `AtlasEntities` with the same tags as their parent entity, but with the geomtery of the slice and the country code tag of the relevant country, and these are be added to the relevant staged `CompleteEntity` map
    * At the end of this process, the original parent entity will be removed from the relevant staged `CompleteEntity` map and a `FeatureChange.remove` is added to the `changes` `ChangeSet` to ensure it is removed from the final `Atlas`

### MultiPolygon Relation Slicing
This operation has some added complexity that is worth explaining in-depth. While the overall approach follows the approach described above, the changes are as follows:

1. The `Relation` is filtered of [invalid members](https://wiki.openstreetmap.org/wiki/Relation:multipolygon#Members)
2. The geometry is built using the *raw member line geometry*, not the sliced `Lines` in the staged `CompleteEntity` map
    * This choice ensures that the geometry will build if the raw `Relation` data builds a valid multipolygon, and avoids any possible small stitching errors resultant from data gaps introduced during `Line` slicing
3. After the multipolygon is sliced against the intersecting country boundary polygons and new sliced `Relations` are created, an additional step occurs: `createSyntheticRelationMembers()`
    * This method takes the sliced multipolygon for a country, subtracts out the existing sliced `Line` members for that country from that geometry, then generates new `Line` entities to cover the remaining geometry
    * This operation preserves the ability to build a valid multipolygon out of the sliced `Relation`-- without it, the sliced `Line` members would have major gaps and fail to build into geometry
	* Additionally, in rare circumstances a member that was previously tagged as an `inner` role will now overlap with the exterior ring of the sliced geometry
	    * In this case, that member will still be preserved but its role will be switched to an `outer`

## Synthetic Tags
There are synthetic tags generated by the country-slicing process:
1. `SyntheticRelationMemberAdded` - indicates a Relation that had an added member as a result of country-slicing. An example includes closing a water body MultiPolygon relation with a new `Line` member that runs along a country boundary
2. `SyntheticRelationRoleUpdated` - indicates a Relation member role update, any time that some combination of inner/outer relation members was merged
3. `SyntheticBoundaryNodeTag` -- indicates a Point/Node along a Line/Edge that has been created due to the slicing of Line/Edge geometry at a country boundary
4. `SytheticInvalidGeometryTag` -- indicates a geometry went through the slicing process, but could not be sliced due to not meeting OGC compliance for the geometry type
5. `SyntheticInvalidMultiPolygonRelationMembersRemovedTag` -- indicates any multipolygon Relation members that were removed during slicing due to not meeting the OSM requirements for members (i.e. not a Line with a role of "inner" or "outer")
6. `SyntheticSyntheticRelationMemberTag` -- indicates an entity is a synthetic addition to a multipolygon Relation added during slicing
7. `SyntheticGeometrySlicedTag` -- indicates an entity had its geometry changed during slicing
