# TaggableMatcher

#### Table of Contents
1. [Quick Intro and Examples](#quick-intro-and-examples)
   * [Some sample matchers with explanations](#some-sample-matchers-with-explanations)
2. [Basic Semantics](#basic-semantics)
3. [Syntax Rules](#syntax-rules)
   * [Table of Operators](#table-of-operators)
   * [Precedence](#precedence)
   * [Escaping and Whitespace](#escaping-and-whitespace)
   * [Regex](#regex)
   * [Tree Representation](#tree-representation)
4. TODO more sections

## Quick Intro and Examples
`TaggableMatcher` is an extension of `Predicate<Taggable>` that supports intuitive string definitions.
One can create a new `TaggableMatcher` like so:

```java
// Create a simple filter
String definition = "highway=primary";
Predicate<Taggable> filter = TaggableMatcher.from(definition);

// Extend the above filter
TaggableMatcher matcher = TaggableMatcher.from(definition + " & name=I280");
```

### Some sample matchers with explanations

Match any `Taggable` containing a "name" tag with value "John's Coffee Shop":
```
name="John's Coffee Shop"
```

Match any `Taggable` that is either a "natural=pond" or is a "natural=water" that is **not** named "Lake Michigan":
```
natural = water & name != Lake\ Michigan | natural = pond
```

Match any tertiary or residential highway whose "name" tag contains the word "street" or "Street"
```
(highway=tertiary | highway=residential) & name=/.*\b[s|S]treet\b.*/
```

Match all non-highway features, but also include primary and secondary highways.
```
!highway | highway=(primary | secondary)
```

And that's really all there is to it! Enough to get you started. Read on to get more details about
the syntax rules and various features.

## Basic Semantics
Consider the following `TaggableMatcher`:
```
foo = bar
```
This will match against any `Taggable` that contains the `key=value` pair "foo=bar". `TaggableMatchers`
are case sensitive. They are also *inclusive*, meaning that they automatically match anything containing *at least*
the specified constraint. So for the above `TaggableMatcher`, we would match both `Taggable(foo=bar)` as well
as `Taggable(baz=bat, foo=bar)`. If we wanted to exclude the `Taggable` containing the "baz=bat"
`key=value` pair, we would need to explicitly specify that in the constraint. One way to do this:
```
foo=bar & !baz
```

## Syntax Rules
`TaggableMatcher` syntax follows basic boolean expression syntax, with the standard boolean `==`/`!=`
operators replaced by `=`/`!=` to denote `key=value` pair constraints. Additionally, like boolean expressions,
chained `=`/`!=` operators are forbidden by the semantic checker, since these would be nonsense in
the context of tag matching (more on that in the `Tree Representation` section).

### Table of Operators
| Operator | Description |
| -------- | ----------- |
| `( .. )` | Group a subexpression |
| `!` | Negate a subexpression |
| `=` | Specify a `key=value` pair constraint that must be **included** in a given `Taggable` |
| `!=` | Specify `key=value` pair constraint that must be **excluded** from a given `Taggable` |
| `&` | Specify an AND relationship between `key=value` pair constaints or between specific keys/values within a pair constraint |
| `\|` | Specify an OR relationship between `key=value` pair constaints or between specific keys/values within a pair constraint |

### Precedence
`TaggableMatcher` operator precedence matches that of standard boolean expressions, but with the `=`/`!=` operators
taking the place of the standard boolean `==`/`!=` operators. The following snippet lists operators in descending order,
from highest precedence to lowest precedence.
```
( .. )
!
=, != (these have equivalent precedence)
&
|
```


### Escaping and Whitespace
For `TaggableMatcher` definitions, whitespace is not meaningful by default - the lexer will simply ignore it.
This means that, for example:
```
foo=bar
```
AND
```
foo = bar
```
are semantically equivalent.

In order to include significant whitespace in a tag constraint, you must either escape the whitespace or wrap
the whitespace-containing literal in `"`. For example, the following matcher will fail with a syntax error:
```
name = Lake Michigan

org.openstreetmap.atlas.exception.CoreException: syntax error: unexpected token LITERAL(Michigan)
name = Lake Michigan
~~~~~~~~~~~~^
```
Instead, you must do either:
```
name = Lake\ Michigan
```
OR
```
name = "Lake Michigan"
```

You may also use `\` and `"` to escape operator characters. So to match a tag that contains a literal "=" character, you
could do, for example:
```
math = 2+2\=4
```
OR
```
math="2+2=4"
```

Finally, note that, unlike many shell languages, a double quoted string constitutes a complete literal. The lexer will
**not** coalesce multiple consecutive literals together. So something like:
```
foo = bar" and baz"
```
will generate the following syntax error:
```
org.openstreetmap.atlas.exception.CoreException: syntax error: unexpected token LITERAL( and baz)
foo = bar" and baz"
~~~~~~~~~^
```

### Regex
`TaggableMatchers` support regex matching for keys and values with the following syntax:
```
name = /[l|L]ake.*/
```
Anything between the `/` symbols will be treated as a regex operand.

Regexes are evaluated using
Java's `String#matches(String)` method, which means that in order to get a match, the regex must match the
**entire** key or value string. So for example, the above regex would match `Taggable(name=lake michigan)`,
but it would **not** match `Taggable(name=Arrow Lake)`. In order to match this second `Taggable`, the
regex would need to be:
```
name = /.*[l|L]ake.*/
```

You can escape a closing `/` symbol using the escape symbol `\`, like so: `\/`. This will pass the `\/` directly
into the regex. For example:
```
name=/foo\/bar/
```
would result in a matcher regex `foo\/bar`, which would match the string "foo/bar".

https://regex101.com is a nice tool for testing regex and matching. Just note that unlike `TaggableMatcher` regex,
it will match substrings of the input.

### Tree Representation
It can be helpful to think about `TaggableMatchers` as syntax trees with the various operators as internal nodes.
For example, we could represent the following `TaggableMatcher` as this tree:
```
a = b & c = d | e != f
```
```
                |
              /   \
            /       \
           &        !=
         /   \      / \
        =     =    e   f
       / \   / \
      a   b c   d
```
The `TaggableMatcher` is evaluated by walking the tree in a depth-first, left-to-right fashion.

TODO add note about invalid nested "="/"!=" operators

