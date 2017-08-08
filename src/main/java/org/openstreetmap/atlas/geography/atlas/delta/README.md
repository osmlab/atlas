# Atlas Delta

## Use Cases

### All

* An Entity's tag is added
* An Entity's tag is removed
* An Entity's tag is changed

### Node

* A Node is added (There is a new identifier)
* A Node is deleted (The identifier has disappeared)
* A Node is moved (The Location is different for the same identifier)
* A Node's in-Edge is added
* A Node's in-Edge is removed
* A Node's out-Edge is added
* A Node's out-Edge is removed
* A Node is added to a Relation
* A Node is removed from a Relation
* A Node's order is changed inside a Relation

### Edge

* An Edge is added (There is a new identifier)
* An Edge is deleted (The identifier has disappeared)
* An Edge is moved (The PolyLine is different for the same identifier)
* An Edge's start Node is different
* An Edge's end Node is different
* An Edge is added to a Relation
* An Edge is removed from a Relation
* An Edge's order is changed inside a Relation

### Area

* An Area is added (There is a new identifier)
* An Area is deleted (The identifier has disappeared)
* An Area is moved (The Polygon is different for the same identifier)
* An Area is added to a Relation
* An Area is removed from a Relation
* An Area's order is changed inside a Relation

### Line

* A Line is added (There is a new identifier)
* A Line is deleted (The identifier has disappeared)
* A Line is moved (The PolyLine is different for the same identifier)
* A Line is added to a Relation
* A Line is removed from a Relation
* A Line's order is changed inside a Relation

### Point

* A Point is added (There is a new identifier)
* A Point is deleted (The identifier has disappeared)
* A Point is moved (The Location is different for the same identifier)
* A Point is added to a Relation
* A Point is removed from a Relation
* A Point's order is changed inside a Relation

### Relation

* A Relation is added (There is a new identifier)
* A Relation is deleted (The identifier has disappeared)
* A Relation is changed:
  * A Relation member is added (There is a new member identifier)
  * A Relation member is removed (A member identifier is changed)
  * A Relation member list order is different
  * A Relation member is changed:
    * The role changed (The role String is different)
* A Relation is added to a Relation
* A Relation is removed from a Relation
* A Relation's order is changed inside a Relation
