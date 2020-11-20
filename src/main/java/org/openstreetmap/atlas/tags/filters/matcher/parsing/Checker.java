package org.openstreetmap.atlas.tags.filters.matcher.parsing;

import org.openstreetmap.atlas.tags.filters.matcher.TaggableMatcher;
import org.openstreetmap.atlas.tags.filters.matcher.parsing.tree.ASTNode;

/**
 * Semantic checker for a {@link Parser} constructed {@link TaggableMatcher} abstract syntax tree
 * (AST). This checker will make sure the supplied matcher does not contain invalid "=" or "!="
 * semantics. For e.g. "foo = (bar != baz)" is valid syntactically per the {@link TaggableMatcher}
 * expression grammar, but not semantically. We will need to catch that with this checker.
 * Basically. we can walk the AST, and if the left or right subtree of a "="/"!=" operator contains
 * another "="/"!=" operator, then we fail since that is invalid semantically.
 * 
 * @author lcram
 */
public class Checker
{
    public void check(final ASTNode root)
    {

    }
}
