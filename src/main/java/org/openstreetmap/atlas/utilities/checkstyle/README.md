# ArrangementCheck

This is a Checkstyle plugin that checks for member ordering in Java source files.

## Setup

The default setup takes the [default ordering definition](/src/main/resources/org/openstreetmap/atlas/utilities/checkstyle/arrangement.txt).

```xml
<module name="org.openstreetmap.atlas.utilities.checkstyle.ArrangementCheck"/>
```

or with a specified ordering definition:

```xml
<module name="org.openstreetmap.atlas.utilities.checkstyle.ArrangementCheck">
    <property name="arrangementDefinition" value="${config_loc}/arrangement.txt" />
</module>
```

To use this with gradle, the project needs this dependency:

```groovy

```

## Ordering definition

The ordering can be defined in a text file which has the following format:

```
type,visibility,static/non_static
```

with each line in order of importance.

All the combinations that are not matched in that file will be ignored during processing. For example, a file like this:

```
method,public,non_static
method,protected,non_static
```

will only check that non-static public methods come before non-static protected methods, and it will ignore everything else.

### Alphabetical order

By default all types that are comparable according to the ordering file will also have to be alphabetically ordered. This is mandatory, and not configurable yet.

## Examples

The following examples are based on the [default ordering definition](/src/main/resources/org/openstreetmap/atlas/utilities/checkstyle/arrangement.txt).

### Error: Field vs. Method

```java
public class MyClass
{
    public void method()
    {
    }

    private boolean field;
}
```

### Error: Visibility

```java
public class MyClass
{
    void methodA()
    {
    }

    public void methodB()
    {
    }
}
```

### Error: Name

```java
public class MyClass
{
    public void methodB()
    {
    }

    public void methodA()
    {
    }
}
```
