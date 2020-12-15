# TaggableMatcher

#### Table of Contents
1. [Quick Intro and Examples](#quick-intro-and-examples)
   * [Some sample matchers with explanations](#some-sample-matchers-with-explanations)
2. [Basic Semantics](#basic-semantics)
3. [Syntax Rules](#syntax-rules)
   * [Table of Operators](#table-of-operators)
   * [Precedence](#precedence)
   * [Escaping and Whitespace](#escaping-and-whitespace)
   * [More On Quoting](#more-on-quoting)
   * [Regex](#regex)
   * [Tree Representation](#tree-representation)
4. [Converting Your Old TaggableFilters](#converting-your-old-taggableFilters)

## Quick Intro and Examples
`TaggableMatcher` is an extension of `Predicate<Taggable>` that supports intuitive string definitions.
You can create a new `TaggableMatcher` like:

```java
// Create a simple predicate:
String definition = "highway=primary";
Predicate<Taggable> predicate = TaggableMatcher.from(definition);

// You could also do this to access the extra TaggableMatcher methods:
TaggableMatcher matcher = TaggableMatcher.from(definition + " & name=I280");
```

### Some sample matchers with explanations

Match any `Taggable` containing a "name" tag with value "John's Coffee Shop":
```
name="John's Coffee Shop"
```

Match any `Taggable` that is a "water=pond" or is a "water=lake" that is *not* named "Lake Michigan":
```
water = pond | water = lake & name != Lake\ Michigan
```

Match any tertiary or residential highway whose "name" tag contains the word "street" or "Street":
```
(highway=tertiary | highway=residential) & name=/.*\b[s|S]treet\b.*/
```

Match all non-highway features, but also include primary and secondary highways:
```
!highway | highway=(primary | secondary)
```

Match any `Taggable` that is a "natural=lake" or "water=lake":
```
(natural | water) = lake
```

And that's really all there is to it! Enough to get you started. Read on to get more details about
the syntax rules and various features.

## Basic Semantics
Consider the following definition:
```
foo = bar
```
This will create a `TaggableMatcher` with a single `key=value` pair constraint, "foo=bar". `TaggableMatcher`
constraints are case sensitive, but they are also *inclusive*, meaning that they automatically match anything
containing *at least* the specified constraint. So the above `TaggableMatcher` would match both
`Taggable(foo=bar)` as well as `Taggable(baz=bat, foo=bar)`. If we wanted to exclude the `Taggable`
containing the "baz=bat" `key=value` pair, we would need to explicitly specify that in a compound constraint.
One way to do this is by combining the original `key=value` constraint and a negated `key-only` constraint with an
`&` (AND) operator, like:
```
foo=bar & !baz
```
In the above example, `!baz` is the `key-only` constraint. Any constraint that does not include an `=` or
`!=` operator will become a `key-only` constraint and will match against the *key only*, as the name suggests.
So something like:
```
water & !name
```
is equivalent to the old `TaggableFilter` syntax:
```
water->*&name->!
```
which will match any `Taggable` that both *has* a "water" key and does *not have* a "name" key, with no constraint on
the associated values.

## Syntax Rules
`TaggableMatcher` syntax follows basic boolean expression syntax, with the standard boolean `==`/`!=`
operators replaced by `=`/`!=` to denote `key=value` pair constraints. Additionally, like boolean expressions,
chained `=`/`!=` operators are forbidden by the semantic checker since these would be nonsense in
the context of tag matching (more on that in the `Tree Representation` section). For example,
this `TaggableMatcher` would generate the following error:
```
foo = (bar = baz)
org.openstreetmap.atlas.exception.CoreException: semantic error: invalid nested equality operators
```

### Table of Operators
| Operator | Description |
| -------- | ----------- |
| `( .. )` | Group a subexpression to increase its precedence |
| `!` | Negate a subexpression |
| `=` | Specify a `key=value` pair constraint that must be **included** in a given `Taggable` |
| `!=` | Specify `key=value` pair constraint that must be **excluded** from a given `Taggable` |
| `&` | Specify an AND relationship between constraints or between specific keys/values within a constraint |
| `^` | Specify an XOR relationship between constraints or between specific keys/values within a constraint |
| `\|` | Specify an OR relationship between constraints or between specific keys/values within a constraint |

### Precedence
`TaggableMatcher` operator precedence follows that of standard boolean expressions, but again with the `=`/`!=` operators
taking the place of the standard boolean `==`/`!=` operators. The following snippet lists operators in descending order,
from highest precedence to lowest precedence. `TaggableMatcher` will evaluate higher precedence operators first and will
fall back on left-to-right evaluation.
```
( .. )
!
=, != (these have equivalent precedence)
&
^
|
```

### Escaping and Whitespace
For `TaggableMatcher` definitions, whitespace is not meaningful by default - the lexer will simply ignore it.
This means that, for example:
```
foo=bar|baz=bat
```
and
```
foo = bar | baz = bat
```
are semantically equivalent.

In order to include significant whitespace in a constraint, you must either escape the whitespace or wrap
the whitespace-containing literal in quotes (`TaggableMatcher` supports single ' or double " quoted literals). For example,
the following matcher will fail with a syntax error:
```
name = Lake Michigan & water = lake

org.openstreetmap.atlas.exception.CoreException: syntax error: unexpected token LITERAL(Michigan)
name = Lake Michigan & water = lake
~~~~~~~~~~~~^
```
Instead, you must do either:
```
name = Lake\ Michigan & water = lake
```
or
```
name = "Lake Michigan" & water = lake
```
or
```
name = 'Lake Michigan' & water = lake
```

You may also use `\`, `"`, `'` to escape operator characters. For example, to match a tag that contains
a literal "=" character you could do:
```
math = 2+2\=4
```
or
```
math="2+2=4"
```

### More On Quoting
As shown above, `TaggableMatcher` supports both single and double quoting. There are not many differences
between the two, other than their escaping rules. Specifically, a single quoted string may contain
unescaped double quote characters but must escape all inner single quote characters. And vice versa
for double quoted strings. A few examples of this:
```
// The ' does not need escaping, but the " do
name="John's \"Coffee\" Shop"
```

```
// Here we must escape the ', but we can skip escaping the "
name='John\'s "Coffee" Shop'
```

Finally note that unlike many shell languages, a quoted string constitutes a complete literal, and the lexer will
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
`TaggableMatcher` supports regex matching for keys and values with the following syntax:
```
name = /[l|L]ake.*/
```
Anything between the `/` symbols will be treated as a regex operand.

Regexes are evaluated using
Java's `String#matches(String)` method, which means that in order to get a match, the regex must match the
**entire** key or value string. So for example, the above regex would match `Taggable(name=lake michigan)`,
but it would **not** match `Taggable(name=Arrow Lake)`. In order to match this second `Taggable` as well as
the first, the regex would need to be:
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
You can print out pretty Unicode trees for your `TaggableMatchers` using the `TaggableMatcherPrinterCommand`, which
you can call from the command line as `print-matcher` using [Atlas Shell Tools](https://github.com/osmlab/atlas/tree/dev/atlas-shell-tools).

For example, we could print the following `TaggableMatcher` as this tree:
```
a = b & c = d | e != f
```
```
                        |
            ┌───────────┴───────────┐
            &                      !=
      ┌─────┴─────┐           ┌─────┴─────┐
      =           =           e           f
   ┌──┴──┐     ┌──┴──┐
   a     b     c     d
```
The `TaggableMatcher` is evaluated by walking the tree in post-order (LRN).

As mentioned in an earlier section, chained "="/"!=" operators are forbidden since expressions
containing them are nonsensical in the context of tag matching. Expressions with chained equality
operators become trees in which an equality operator is present in the subtree of another equality
operator. For example, the matcher definition:
```
a = b = c
```
becomes this tree:
```
            =
      ┌─────┴─────┐
      a           =
               ┌──┴──┐
               b     c
```
The `TaggableMatcher` semantic checker is able to detect subtrees like this and reject the matcher
definition as invalid. In fact, the `TaggableMatcherPrinterCommand` will not even allow you to print
this tree since it fails the semantic check.

## Converting Your Old TaggableFilters
The `TaggableMatcherPrinterCommand` mentioned earlier can also help you convert old `TaggableFilter`
definitions into the new `TaggableMatcher` syntax. All you need to do is specify the `--reverse` option
at the command line, and then pass as many `TaggableFilter` definitions as you would like to convert.
For example:
```
$ atlas print-matcher --reverse 'foo->bar|baz->bat' 'cat->mat&hat->zat,hello'
foo=bar | baz=bat
(cat=mat & hat=(zat | hello))
```
Note that occasionally, `print-matcher` will include unnecessary parentheses or other strange
conversion artifacts in its generated `TaggableMatcher` definitions. You can safely remove those.

Also note that `print-matcher` will fail to convert certain kinds of valid `TaggableFilters`,
specifically those that make use of ambiguous combinations of operators. For example:
```
$ atlas print-matcher --reverse 'foo->bar,!,bat'
print-matcher: error: Cannot transpile `foo->bar,!,bat' since composite value `bar,!,bat' contains a lone `!' operator.
Expression `foo->bar,!,bat' is ambiguous and order dependent, please rewrite your TaggableFilter to remove it.
```

Finally, as mentioned above, you can obtain `print-matcher` by installing [Atlas Shell Tools](https://github.com/osmlab/atlas/tree/dev/atlas-shell-tools).
