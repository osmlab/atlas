# Tag filtering

## TaggableFilter

`TaggableFilter` is an extension of `Predicate<Taggable>` that allows for easy string definitions. With a String containing a `TaggableFilter` definition, one can be created easily:

```java
String definition;
Predicate<Taggable> filter = TaggableFilter.forDefinition(definition);
```

## String definition

`TaggableFilter` is built with a decision tree that alternates the OR and AND operands.

```
                OR
              /    \
            /        \
         AND         Test
       /     \
     /         \
  Test         Test
```

Each leaf node is a "simple" filter, based on tags. The taggable filter is a collection of simple filters with AND and OR operands.

### Simple filters

#### One tag

```
water->pond
```

Only features with water=pond in their tags will pass this test.

#### Multiple tag values

```
water->pond,lake,canal
```

Only features with water=pond or water=lake or water=canal in their tags will pass this test.

#### Negation

```
water->!pond
```

Anything without water=pond

```
water->!pond,!lake
```

Anything without water=pond or water=lake

```
water->!
```

Anything without water tag.

#### Wildcard

```
water->*
```

Anything with a water tag.

### Tree logic

AND and OR operands are represented by `&` and `|` respectively. The same operands one level down are `&&` and `||`. Two levels down are `&&&` and `|||` and so on.

**All trees always start with OR!** This means that `a&b|c` translates to `(a AND b) OR c`. To achieve `a AND (b OR c)` one needs to write `a&b||c`

For example:

```
                      OR
                    /    \
                  /        \
               AND        highway=motorway
             /     \
           /         \
highway=service    service=parking_aisle
```

Is defined by:

```
highway->service&service->parking_aisle|highway->motorway
```

And

```
                      OR
                    /    \
                  /        \
               AND        highway=motorway
             /     \
           /         \
highway=service       OR
                    /    \
                  /        \
       cycleway=lane       AND
                         /     \
                       /         \
           cycleway:lane=*      No cycleway tag       
```

Is defined by:

```
highway->service&cycleway->lane||cycleway:lane->*&&cycleway->!|highway->motorway
```

#### Backwards compatibility

An older version of `TaggableFilter` had only two OR levels possible, and the lower OR level was represented by `^`. This is still allowed and is interpreted by the parser as `||`.

## ConfiguredTaggableFilter

`ConfiguredTaggableFilter` is a `TaggableFilter` that can be created from a JSON file that contains multiple of those above filters:

```javascript
{
    "filters": [
        "access->!no|motor_vehicle->yes|motorcar->yes|vehicle->yes",
        "oneway->!reversible",
        "route->ferry|man_made->pier|junction->roundabout|highway->motorway"
    ]
}
```

Each line in that filter must pass for the `TaggableFilter` to pass a `Taggable`. It could be translated to an AND of each filter.

Here it is equivalent to:

```javascript
{
    "filters": [
        "access->!no||motor_vehicle->yes||motorcar->yes||vehicle->yes&oneway->!reversible&route->ferry||man_made->pier||junction->roundabout||highway->motorway"
    ]
}
```
