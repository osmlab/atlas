# Scalars Package

All the classes in here wrap scalar values, in a way that shields the developer from conversion errors. Each class stores a core primitive, as well as fixed constants to do the conversions, and offers construction and extraction methods that take care of the conversion.

## `Angle`

An `Angle` contains a degree of magnitude 7 (dm7, degree with 7 precision) angle value. It can read degrees, radians, dm7 and return any other.

## `Distance`

A `Distance` contains millimeters. It can read millimeters, meters, kilometers, feet and inches, among others, and return any other.

## `Duration`

A time duration. It contains milliseconds, and can read milliseconds, seconds, minutes and hours, and return any other.

## `Speed`

A `Speed` does not contain any primitive, but a combination of `Distance` and `Duration`. It can read miles per hour, kilometers per hour, meters per second among others, and return any other.

## `Surface`

A `Surface` is an `Angle` squared. It will read square dm7, and return the same, or using the earth's radius, square meters, square kilometers, etc.
