# Tags

This package helps with OSM tag parsing and validation.

## Enum or Interface

Each recognized tag has a java instance named after itself. When the tag values are set in the OSM wiki, and well defined, we use enums (for example, [HighwayTag](HighwayTag.java)). When the values are more loosely defined, like using ranges, we use interfaces and validation annotations (for example, [LanesTag](LanesTag.java)).

## Annotations and Validation

The [annotations](annotations) and [validation](annotations/validation) packages define the annotation values used to identify the key and type of tags. For example, the [MaxSpeedTag](MaxSpeedTag.java) defines its Validation as a Speed, and annotates the tag's key as "maxspeed".

