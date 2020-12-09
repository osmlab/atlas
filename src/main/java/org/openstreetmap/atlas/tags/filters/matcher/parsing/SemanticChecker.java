package org.openstreetmap.atlas.tags.filters.matcher.parsing;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.tags.filters.matcher.TaggableMatcher;
import org.openstreetmap.atlas.tags.filters.matcher.parsing.tree.ASTNode;
import org.openstreetmap.atlas.tags.filters.matcher.parsing.tree.EqualsOperator;

/**
 * Semantic checker for a {@link TaggableMatcher} abstract syntax tree (AST). This checker will make
 * sure the supplied matcher does not contain invalid "=" or "!=" semantics. For e.g. "foo = (bar !=
 * baz)" is valid syntactically per the {@link TaggableMatcher} expression grammar, but not
 * semantically since it makes no sense for the purposes of tag matching. We will need to catch that
 * with this checker. Basically, we can walk the AST, and if the left or right subtree of a "="/"!="
 * operator contains another "="/"!=" operator, then we fail.
 *
 * @author lcram
 */
public class SemanticChecker
{
    public void check(final ASTNode root)
    {
        if (root == null)
        {
            return;
        }

        if ((root instanceof EqualsOperator) && subtreeContainsEquals(root))
        {
            throw new CoreException("semantic error: invalid nested equality operators");
        }
        check(root.getLeftChild());
        check(root.getRightChild());
        check(root.getCenterChild());
    }

    private boolean subtreeContainsEquals(final ASTNode root)
    {
        if (root == null)
        {
            return false;
        }

        final ASTNode leftRoot = root.getLeftChild();
        final ASTNode rightRoot = root.getRightChild();
        final ASTNode centerRoot = root.getCenterChild();
        if (leftRoot instanceof EqualsOperator || rightRoot instanceof EqualsOperator
                || centerRoot instanceof EqualsOperator)
        {
            return true;
        }
        return subtreeContainsEquals(leftRoot) || subtreeContainsEquals(rightRoot)
                || subtreeContainsEquals(centerRoot);
    }
}
