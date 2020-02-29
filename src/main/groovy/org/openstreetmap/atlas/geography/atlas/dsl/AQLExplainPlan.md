# Explain Plan

The explain plan tries to explain and display the "execution plan of the query". Importantly the
explain plan also internally runs all the [optimizations](AQLOptimization.md) that would be run at the point of executing 
the query in question and explains how the query was improved (by usage of an index opr a better index). 

## Examples

### Example-1
```sql
select1 = select edge._ from atlas.edge where edge.hasId(478164185000001) or edge.hasId(528897519000001)

explain select1
```

Output,
```
==================================================
------------------------
Query Analysis
------------------------
[I] Was the query optimized?                             : true

---
[II] Original Query                                      :  
SELECT
    [ItselfField(_)]
FROM
    EdgeTable ( isZeroLength SelectOnlyField, inEdges SelectOnlyField, reversed SelectOnlyField, osmId StandardField, start SelectOnlyField, isWaySectioned SelectOnlyField, end SelectOnlyField, highwayTag SelectOnlyField, isMasterEdge SelectOnlyField, relations SelectOnlyField, _ ItselfField, id StandardField, connectedEdges SelectOnlyField, tags TagsField, osmIdentifier StandardField, outEdges SelectOnlyField, lastUserIdentifier StandardField, endId TerminalNodeIdField, lastEdit StandardField, rawGeometry SelectOnlyField, startId TerminalNodeIdField, type SelectOnlyField, connectedNodes SelectOnlyField, length SelectOnlyField, hasReverseEdge SelectOnlyField, bounds StandardField, osmTags SelectOnlyField, isClosed SelectOnlyField, lastUserName StandardField, identifier StandardField )
WHERE
	ConditionalConstruct: [WHERE Constraint(StandardField(identifier) eq 478164185000001; Potential Index: ID_UNIQUE_INDEX)]
	ConditionalConstruct: [OR Constraint(StandardField(identifier) eq 528897519000001; Potential Index: ID_UNIQUE_INDEX)]
LIMIT
    null
        

---
[III] Optimized Query                                    :
SELECT
    [ItselfField(_)]
FROM
    EdgeTable ( isZeroLength SelectOnlyField, inEdges SelectOnlyField, reversed SelectOnlyField, osmId StandardField, start SelectOnlyField, isWaySectioned SelectOnlyField, end SelectOnlyField, highwayTag SelectOnlyField, isMasterEdge SelectOnlyField, relations SelectOnlyField, _ ItselfField, id StandardField, connectedEdges SelectOnlyField, tags TagsField, osmIdentifier StandardField, outEdges SelectOnlyField, lastUserIdentifier StandardField, endId TerminalNodeIdField, lastEdit StandardField, rawGeometry SelectOnlyField, startId TerminalNodeIdField, type SelectOnlyField, connectedNodes SelectOnlyField, length SelectOnlyField, hasReverseEdge SelectOnlyField, bounds StandardField, osmTags SelectOnlyField, isClosed SelectOnlyField, lastUserName StandardField, identifier StandardField )
WHERE
	ConditionalConstruct: [WHERE Constraint(StandardField(identifier) inside [478164185000001, 528897519000001]; Potential Index: ID_UNIQUE_INDEX)]
LIMIT
    null
        

---
[IV] Index Usage Info (on ORIGINAL query)                :

Potential Index              : null.
Index Actually Used?         : false;
Reason for not using index?  : WHERE_HAS_OR_USED


---
[V] Index Usage Info (on OPTIMIZED query)                :

Potential Index              : ID_UNIQUE_INDEX.
Index Actually Used?         : true;
Reason for not using index?  : null


---
[VI] Optimization Trace (history of optimization)        :

 --> IdsInOptimization Applied,
SELECT
    [ItselfField(_)]
FROM
    EdgeTable ( isZeroLength SelectOnlyField, inEdges SelectOnlyField, reversed SelectOnlyField, osmId StandardField, start SelectOnlyField, isWaySectioned SelectOnlyField, end SelectOnlyField, highwayTag SelectOnlyField, isMasterEdge SelectOnlyField, relations SelectOnlyField, _ ItselfField, id StandardField, connectedEdges SelectOnlyField, tags TagsField, osmIdentifier StandardField, outEdges SelectOnlyField, lastUserIdentifier StandardField, endId TerminalNodeIdField, lastEdit StandardField, rawGeometry SelectOnlyField, startId TerminalNodeIdField, type SelectOnlyField, connectedNodes SelectOnlyField, length SelectOnlyField, hasReverseEdge SelectOnlyField, bounds StandardField, osmTags SelectOnlyField, isClosed SelectOnlyField, lastUserName StandardField, identifier StandardField )
WHERE
	ConditionalConstruct: [WHERE Constraint(StandardField(identifier) inside [478164185000001, 528897519000001]; Potential Index: ID_UNIQUE_INDEX)]
LIMIT
    null
        

==================================================
```

### Example-2

```sql
select1 = select edge._ from atlas.edge where edge.hasTag(/surface/) and edge.hasId(478164185000001)

explain select1
```

Output,

```
==================================================
------------------------
Query Analysis
------------------------
[I] Was the query optimized?                             : true

---
[II] Original Query                                      :  
SELECT
    [ItselfField(_)]
FROM
    EdgeTable ( isZeroLength SelectOnlyField, inEdges SelectOnlyField, reversed SelectOnlyField, osmId StandardField, start SelectOnlyField, isWaySectioned SelectOnlyField, end SelectOnlyField, highwayTag SelectOnlyField, isMasterEdge SelectOnlyField, relations SelectOnlyField, _ ItselfField, id StandardField, connectedEdges SelectOnlyField, tags TagsField, osmIdentifier StandardField, outEdges SelectOnlyField, lastUserIdentifier StandardField, endId TerminalNodeIdField, lastEdit StandardField, rawGeometry SelectOnlyField, startId TerminalNodeIdField, type SelectOnlyField, connectedNodes SelectOnlyField, length SelectOnlyField, hasReverseEdge SelectOnlyField, bounds StandardField, osmTags SelectOnlyField, isClosed SelectOnlyField, lastUserName StandardField, identifier StandardField )
WHERE
	ConditionalConstruct: [WHERE Constraint(ConstrainableFieldImpl(tags) tag surface; Potential Index: FULL)]
	ConditionalConstruct: [AND Constraint(StandardField(identifier) eq 478164185000001; Potential Index: ID_UNIQUE_INDEX)]
LIMIT
    null
        

---
[III] Optimized Query                                    :
SELECT
    [ItselfField(_)]
FROM
    EdgeTable ( isZeroLength SelectOnlyField, inEdges SelectOnlyField, reversed SelectOnlyField, osmId StandardField, start SelectOnlyField, isWaySectioned SelectOnlyField, end SelectOnlyField, highwayTag SelectOnlyField, isMasterEdge SelectOnlyField, relations SelectOnlyField, _ ItselfField, id StandardField, connectedEdges SelectOnlyField, tags TagsField, osmIdentifier StandardField, outEdges SelectOnlyField, lastUserIdentifier StandardField, endId TerminalNodeIdField, lastEdit StandardField, rawGeometry SelectOnlyField, startId TerminalNodeIdField, type SelectOnlyField, connectedNodes SelectOnlyField, length SelectOnlyField, hasReverseEdge SelectOnlyField, bounds StandardField, osmTags SelectOnlyField, isClosed SelectOnlyField, lastUserName StandardField, identifier StandardField )
WHERE
	ConditionalConstruct: [WHERE Constraint(StandardField(identifier) eq 478164185000001; Potential Index: ID_UNIQUE_INDEX)]
	ConditionalConstruct: [AND Constraint(ConstrainableFieldImpl(tags) tag surface; Potential Index: FULL)]
LIMIT
    null
        

---
[IV] Index Usage Info (on ORIGINAL query)                :

Potential Index              : null.
Index Actually Used?         : false;
Reason for not using index?  : FIRST_WHERE_CLAUSE_NEEDS_FULL_SCAN


---
[V] Index Usage Info (on OPTIMIZED query)                :

Potential Index              : ID_UNIQUE_INDEX.
Index Actually Used?         : true;
Reason for not using index?  : null


---
[VI] Optimization Trace (history of optimization)        :

 --> ReorderingOptimization Applied,
SELECT
    [ItselfField(_)]
FROM
    EdgeTable ( isZeroLength SelectOnlyField, inEdges SelectOnlyField, reversed SelectOnlyField, osmId StandardField, start SelectOnlyField, isWaySectioned SelectOnlyField, end SelectOnlyField, highwayTag SelectOnlyField, isMasterEdge SelectOnlyField, relations SelectOnlyField, _ ItselfField, id StandardField, connectedEdges SelectOnlyField, tags TagsField, osmIdentifier StandardField, outEdges SelectOnlyField, lastUserIdentifier StandardField, endId TerminalNodeIdField, lastEdit StandardField, rawGeometry SelectOnlyField, startId TerminalNodeIdField, type SelectOnlyField, connectedNodes SelectOnlyField, length SelectOnlyField, hasReverseEdge SelectOnlyField, bounds StandardField, osmTags SelectOnlyField, isClosed SelectOnlyField, lastUserName StandardField, identifier StandardField )
WHERE
	ConditionalConstruct: [WHERE Constraint(StandardField(identifier) eq 478164185000001; Potential Index: ID_UNIQUE_INDEX)]
	ConditionalConstruct: [AND Constraint(ConstrainableFieldImpl(tags) tag surface; Potential Index: FULL)]
LIMIT
    null
        

==================================================
```

### Example-3

```sql
select1 = select edge._ from atlas.edge where edge.isWithin(multiPolygon)
explain select1
```

Output,

```
==================================================
------------------------
Query Analysis
------------------------
[I] Was the query optimized?                             : true

---
[II] Original Query                                      :  
SELECT
    [ItselfField(_)]
FROM
    EdgeTable ( isZeroLength SelectOnlyField, inEdges SelectOnlyField, reversed SelectOnlyField, osmId StandardField, start SelectOnlyField, isWaySectioned SelectOnlyField, end SelectOnlyField, highwayTag SelectOnlyField, isMasterEdge SelectOnlyField, relations SelectOnlyField, _ ItselfField, id StandardField, connectedEdges SelectOnlyField, tags TagsField, osmIdentifier StandardField, outEdges SelectOnlyField, lastUserIdentifier StandardField, endId TerminalNodeIdField, lastEdit StandardField, rawGeometry SelectOnlyField, startId TerminalNodeIdField, type SelectOnlyField, connectedNodes SelectOnlyField, length SelectOnlyField, hasReverseEdge SelectOnlyField, bounds StandardField, osmTags SelectOnlyField, isClosed SelectOnlyField, lastUserName StandardField, identifier StandardField )
WHERE
	ConditionalConstruct: [WHERE Constraint(ItselfField(_) within [[[-122.4237920517, 37.8265958156], [-122.4251081994, 37.8270138686], [-122.425258081, 37.8266032148], [-122.4256749392, 37.8271988471], [-122.4259465995, 37.8276908875], [-122.4265742287, 37.8279942492], [-122.4259231805, 37.8283198066], [-122.4240871311, 37.8285232793], [-122.4229396001, 37.8282865111], [-122.4226070504, 37.8278943619], [-122.422499323, 37.8276057981], [-122.4232534147, 37.8270471648], [-122.4237920517, 37.8265958156]], [[-122.4219013779, 37.8242876135], [-122.4228347866, 37.8243299868], [-122.4237145512, 37.8246520228], [-122.4235858051, 37.8252367688], [-122.4231137364, 37.8254655812], [-122.4206139176, 37.8267028508], [-122.4191977112, 37.826940133], [-122.4192513554, 37.8256096479], [-122.4194337456, 37.8248977862], [-122.4219013779, 37.8242876135]], [[-70.24992942810059, 42.013016959237305], [-70.12899398803711, 42.013016959237305], [-70.12899398803711, 42.08420992526112], [-70.24992942810059, 42.08420992526112], [-70.24992942810059, 42.013016959237305]]]; Potential Index: SPATIAL_INDEX)]
LIMIT
    null
        

---
[III] Optimized Query                                    :
SELECT
    [ItselfField(_)]
FROM
    EdgeTable ( isZeroLength SelectOnlyField, inEdges SelectOnlyField, reversed SelectOnlyField, osmId StandardField, start SelectOnlyField, isWaySectioned SelectOnlyField, end SelectOnlyField, highwayTag SelectOnlyField, isMasterEdge SelectOnlyField, relations SelectOnlyField, _ ItselfField, id StandardField, connectedEdges SelectOnlyField, tags TagsField, osmIdentifier StandardField, outEdges SelectOnlyField, lastUserIdentifier StandardField, endId TerminalNodeIdField, lastEdit StandardField, rawGeometry SelectOnlyField, startId TerminalNodeIdField, type SelectOnlyField, connectedNodes SelectOnlyField, length SelectOnlyField, hasReverseEdge SelectOnlyField, bounds StandardField, osmTags SelectOnlyField, isClosed SelectOnlyField, lastUserName StandardField, identifier StandardField )
WHERE
	ConditionalConstruct: [WHERE Constraint(ItselfField(_) within MULTIPOLYGON (((-122.4219014 37.8242876, -122.4228348 37.82433, -122.4237146 37.824652, -122.4235858 37.8252368, -122.4231137 37.8254656, -122.4206139 37.8267029, -122.4191977 37.8269401, -122.4192514 37.8256096, -122.4194337 37.8248978, -122.4219014 37.8242876, -122.4219014 37.8242876)), ((-122.4237921 37.8265958, -122.4251082 37.8270139, -122.4252581 37.8266032, -122.4256749 37.8271988, -122.4259466 37.8276909, -122.4265742 37.8279942, -122.4259232 37.8283198, -122.4240871 37.8285233, -122.4229396 37.8282865, -122.4226071 37.8278944, -122.4224993 37.8276058, -122.4232534 37.8270472, -122.4237921 37.8265958, -122.4237921 37.8265958))); Potential Index: SPATIAL_INDEX)]
LIMIT
    null
        

---
[IV] Index Usage Info (on ORIGINAL query)                :

Potential Index              : SPATIAL_INDEX.
Index Actually Used?         : true;
Reason for not using index?  : null


---
[V] Index Usage Info (on OPTIMIZED query)                :

Potential Index              : SPATIAL_INDEX.
Index Actually Used?         : true;
Reason for not using index?  : null


---
[VI] Optimization Trace (history of optimization)        :

 --> GeometricSurfacesOverlapOptimization Applied,
SELECT
    [ItselfField(_)]
FROM
    EdgeTable ( isZeroLength SelectOnlyField, inEdges SelectOnlyField, reversed SelectOnlyField, osmId StandardField, start SelectOnlyField, isWaySectioned SelectOnlyField, end SelectOnlyField, highwayTag SelectOnlyField, isMasterEdge SelectOnlyField, relations SelectOnlyField, _ ItselfField, id StandardField, connectedEdges SelectOnlyField, tags TagsField, osmIdentifier StandardField, outEdges SelectOnlyField, lastUserIdentifier StandardField, endId TerminalNodeIdField, lastEdit StandardField, rawGeometry SelectOnlyField, startId TerminalNodeIdField, type SelectOnlyField, connectedNodes SelectOnlyField, length SelectOnlyField, hasReverseEdge SelectOnlyField, bounds StandardField, osmTags SelectOnlyField, isClosed SelectOnlyField, lastUserName StandardField, identifier StandardField )
WHERE
	ConditionalConstruct: [WHERE Constraint(ItselfField(_) within MULTIPOLYGON (((-122.4219014 37.8242876, -122.4228348 37.82433, -122.4237146 37.824652, -122.4235858 37.8252368, -122.4231137 37.8254656, -122.4206139 37.8267029, -122.4191977 37.8269401, -122.4192514 37.8256096, -122.4194337 37.8248978, -122.4219014 37.8242876, -122.4219014 37.8242876)), ((-122.4237921 37.8265958, -122.4251082 37.8270139, -122.4252581 37.8266032, -122.4256749 37.8271988, -122.4259466 37.8276909, -122.4265742 37.8279942, -122.4259232 37.8283198, -122.4240871 37.8285233, -122.4229396 37.8282865, -122.4226071 37.8278944, -122.4224993 37.8276058, -122.4232534 37.8270472, -122.4237921 37.8265958, -122.4237921 37.8265958))); Potential Index: SPATIAL_INDEX)]
LIMIT
    null
        

==================================================
```

### Example-4

```sql
select1 = select edge._ from atlas.edge where edge.isWithin(multiPolygon1) or edge.isWithin(multiPolygon2)
explain select1
```

Output,
```
==================================================
------------------------
Query Analysis
------------------------
[I] Was the query optimized?                             : true

---
[II] Original Query                                      :  
SELECT
    [ItselfField(_)]
FROM
    EdgeTable ( isZeroLength SelectOnlyField, inEdges SelectOnlyField, reversed SelectOnlyField, osmId StandardField, start SelectOnlyField, isWaySectioned SelectOnlyField, end SelectOnlyField, highwayTag SelectOnlyField, isMasterEdge SelectOnlyField, relations SelectOnlyField, _ ItselfField, id StandardField, connectedEdges SelectOnlyField, tags TagsField, osmIdentifier StandardField, outEdges SelectOnlyField, lastUserIdentifier StandardField, endId TerminalNodeIdField, lastEdit StandardField, rawGeometry SelectOnlyField, startId TerminalNodeIdField, type SelectOnlyField, connectedNodes SelectOnlyField, length SelectOnlyField, hasReverseEdge SelectOnlyField, bounds StandardField, osmTags SelectOnlyField, isClosed SelectOnlyField, lastUserName StandardField, identifier StandardField )
WHERE
	ConditionalConstruct: [WHERE Constraint(ItselfField(_) within [[[-122.4237920517, 37.8265958156], [-122.4251081994, 37.8270138686], [-122.425258081, 37.8266032148], [-122.4256749392, 37.8271988471], [-122.4259465995, 37.8276908875], [-122.4265742287, 37.8279942492], [-122.4259231805, 37.8283198066], [-122.4240871311, 37.8285232793], [-122.4229396001, 37.8282865111], [-122.4226070504, 37.8278943619], [-122.422499323, 37.8276057981], [-122.4232534147, 37.8270471648], [-122.4237920517, 37.8265958156]]]; Potential Index: SPATIAL_INDEX)]
	ConditionalConstruct: [OR Constraint(ItselfField(_) within [[[-122.4219013779, 37.8242876135], [-122.4228347866, 37.8243299868], [-122.4237145512, 37.8246520228], [-122.4235858051, 37.8252367688], [-122.4231137364, 37.8254655812], [-122.4206139176, 37.8267028508], [-122.4191977112, 37.826940133], [-122.4192513554, 37.8256096479], [-122.4194337456, 37.8248977862], [-122.4219013779, 37.8242876135]], [[-70.24992942810059, 42.013016959237305], [-70.12899398803711, 42.013016959237305], [-70.12899398803711, 42.08420992526112], [-70.24992942810059, 42.08420992526112], [-70.24992942810059, 42.013016959237305]]]; Potential Index: SPATIAL_INDEX)]
LIMIT
    null
        

---
[III] Optimized Query                                    :
SELECT
    [ItselfField(_)]
FROM
    EdgeTable ( isZeroLength SelectOnlyField, inEdges SelectOnlyField, reversed SelectOnlyField, osmId StandardField, start SelectOnlyField, isWaySectioned SelectOnlyField, end SelectOnlyField, highwayTag SelectOnlyField, isMasterEdge SelectOnlyField, relations SelectOnlyField, _ ItselfField, id StandardField, connectedEdges SelectOnlyField, tags TagsField, osmIdentifier StandardField, outEdges SelectOnlyField, lastUserIdentifier StandardField, endId TerminalNodeIdField, lastEdit StandardField, rawGeometry SelectOnlyField, startId TerminalNodeIdField, type SelectOnlyField, connectedNodes SelectOnlyField, length SelectOnlyField, hasReverseEdge SelectOnlyField, bounds StandardField, osmTags SelectOnlyField, isClosed SelectOnlyField, lastUserName StandardField, identifier StandardField )
WHERE
	ConditionalConstruct: [WHERE Constraint(ItselfField(_) inside MULTIPOLYGON (((-122.4219014 37.8242876, -122.4228348 37.82433, -122.4237146 37.824652, -122.4235858 37.8252368, -122.4231137 37.8254656, -122.4206139 37.8267029, -122.4191977 37.8269401, -122.4192514 37.8256096, -122.4194337 37.8248978, -122.4219014 37.8242876, -122.4219014 37.8242876)), ((-122.4237921 37.8265958, -122.4251082 37.8270139, -122.4252581 37.8266032, -122.4256749 37.8271988, -122.4259466 37.8276909, -122.4265742 37.8279942, -122.4259232 37.8283198, -122.4240871 37.8285233, -122.4229396 37.8282865, -122.4226071 37.8278944, -122.4224993 37.8276058, -122.4232534 37.8270472, -122.4237921 37.8265958, -122.4237921 37.8265958))); Potential Index: SPATIAL_INDEX)]
LIMIT
    null
        

---
[IV] Index Usage Info (on ORIGINAL query)                :

Potential Index              : null.
Index Actually Used?         : false;
Reason for not using index?  : WHERE_HAS_OR_USED


---
[V] Index Usage Info (on OPTIMIZED query)                :

Potential Index              : SPATIAL_INDEX.
Index Actually Used?         : true;
Reason for not using index?  : null


---
[VI] Optimization Trace (history of optimization)        :

 --> GeometricSurfacesOverlapOptimization Applied,
SELECT
    [ItselfField(_)]
FROM
    EdgeTable ( isZeroLength SelectOnlyField, inEdges SelectOnlyField, reversed SelectOnlyField, osmId StandardField, start SelectOnlyField, isWaySectioned SelectOnlyField, end SelectOnlyField, highwayTag SelectOnlyField, isMasterEdge SelectOnlyField, relations SelectOnlyField, _ ItselfField, id StandardField, connectedEdges SelectOnlyField, tags TagsField, osmIdentifier StandardField, outEdges SelectOnlyField, lastUserIdentifier StandardField, endId TerminalNodeIdField, lastEdit StandardField, rawGeometry SelectOnlyField, startId TerminalNodeIdField, type SelectOnlyField, connectedNodes SelectOnlyField, length SelectOnlyField, hasReverseEdge SelectOnlyField, bounds StandardField, osmTags SelectOnlyField, isClosed SelectOnlyField, lastUserName StandardField, identifier StandardField )
WHERE
	ConditionalConstruct: [WHERE Constraint(ItselfField(_) inside MULTIPOLYGON (((-122.4219014 37.8242876, -122.4228348 37.82433, -122.4237146 37.824652, -122.4235858 37.8252368, -122.4231137 37.8254656, -122.4206139 37.8267029, -122.4191977 37.8269401, -122.4192514 37.8256096, -122.4194337 37.8248978, -122.4219014 37.8242876, -122.4219014 37.8242876)), ((-122.4237921 37.8265958, -122.4251082 37.8270139, -122.4252581 37.8266032, -122.4256749 37.8271988, -122.4259466 37.8276909, -122.4265742 37.8279942, -122.4259232 37.8283198, -122.4240871 37.8285233, -122.4229396 37.8282865, -122.4226071 37.8278944, -122.4224993 37.8276058, -122.4232534 37.8270472, -122.4237921 37.8265958, -122.4237921 37.8265958))); Potential Index: SPATIAL_INDEX)]
LIMIT
    null
        

 --> GeometricSurfacesWithinOptimization Applied,
SELECT
    [ItselfField(_)]
FROM
    EdgeTable ( isZeroLength SelectOnlyField, inEdges SelectOnlyField, reversed SelectOnlyField, osmId StandardField, start SelectOnlyField, isWaySectioned SelectOnlyField, end SelectOnlyField, highwayTag SelectOnlyField, isMasterEdge SelectOnlyField, relations SelectOnlyField, _ ItselfField, id StandardField, connectedEdges SelectOnlyField, tags TagsField, osmIdentifier StandardField, outEdges SelectOnlyField, lastUserIdentifier StandardField, endId TerminalNodeIdField, lastEdit StandardField, rawGeometry SelectOnlyField, startId TerminalNodeIdField, type SelectOnlyField, connectedNodes SelectOnlyField, length SelectOnlyField, hasReverseEdge SelectOnlyField, bounds StandardField, osmTags SelectOnlyField, isClosed SelectOnlyField, lastUserName StandardField, identifier StandardField )
WHERE
	ConditionalConstruct: [WHERE Constraint(ItselfField(_) inside [[[-122.4237920517, 37.8265958156], [-122.4251081994, 37.8270138686], [-122.425258081, 37.8266032148], [-122.4256749392, 37.8271988471], [-122.4259465995, 37.8276908875], [-122.4265742287, 37.8279942492], [-122.4259231805, 37.8283198066], [-122.4240871311, 37.8285232793], [-122.4229396001, 37.8282865111], [-122.4226070504, 37.8278943619], [-122.422499323, 37.8276057981], [-122.4232534147, 37.8270471648], [-122.4237920517, 37.8265958156]], [[-122.4219013779, 37.8242876135], [-122.4228347866, 37.8243299868], [-122.4237145512, 37.8246520228], [-122.4235858051, 37.8252367688], [-122.4231137364, 37.8254655812], [-122.4206139176, 37.8267028508], [-122.4191977112, 37.826940133], [-122.4192513554, 37.8256096479], [-122.4194337456, 37.8248977862], [-122.4219013779, 37.8242876135]], [[-70.24992942810059, 42.013016959237305], [-70.12899398803711, 42.013016959237305], [-70.12899398803711, 42.08420992526112], [-70.24992942810059, 42.08420992526112], [-70.24992942810059, 42.013016959237305]]]; Potential Index: SPATIAL_INDEX)]
LIMIT
    null
        

==================================================
```