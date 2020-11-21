package org.openstreetmap.atlas.tags.filters.matcher.parsing;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.tags.filters.matcher.TaggableMatcher;
import org.openstreetmap.atlas.tags.filters.matcher.parsing.tree.ASTNode;
import org.openstreetmap.atlas.tags.filters.matcher.parsing.tree.BangEqualsOperator;
import org.openstreetmap.atlas.tags.filters.matcher.parsing.tree.BinaryOperator;
import org.openstreetmap.atlas.tags.filters.matcher.parsing.tree.EqualsOperator;
import org.openstreetmap.atlas.tags.filters.matcher.parsing.tree.Operand;
import org.openstreetmap.atlas.tags.filters.matcher.parsing.tree.UnaryOperator;

/**
 * Semantic checker for a {@link TaggableMatcher} abstract syntax tree (AST). This checker will make
 * sure the supplied matcher does not contain invalid "=" or "!=" semantics. For e.g. "foo = (bar !=
 * baz)" is valid syntactically per the {@link TaggableMatcher} expression grammar, but not
 * semantically since it makes no sense for the purposes of tag matching. We will need to catch that
 * with this checker. Basically. we can walk the AST, and if the left or right subtree of a "="/"!="
 * operator contains another "="/"!=" operator, then we fail.
 * 
 * @author lcram
 */
public class SemanticChecker
{
    public void check(final ASTNode root)
    {
        if (root instanceof Operand || root instanceof UnaryOperator)
        {
            return; // NOSONAR
        }
        else if (root instanceof BinaryOperator)
        {
            if ((root instanceof EqualsOperator || root instanceof BangEqualsOperator)
                    && subtreeContainsEquals(root))
            {
                // TODO fix error message
                throw new CoreException("semantic error: detected nested equality operators");
            }
            check(((BinaryOperator) root).getLeftSubTree());
            check(((BinaryOperator) root).getRightSubTree());
        }
        else
        {
            throw new CoreException("Unknown Node type {}", root.getClass().getName());
        }
    }

    private boolean subtreeContainsEquals(final ASTNode root)
    {
        if (root instanceof BinaryOperator)
        {
            final BinaryOperator rootOp = (BinaryOperator) root;
            final ASTNode leftRoot = rootOp.getLeftSubTree();
            final ASTNode rightRoot = rootOp.getRightSubTree();
            if (leftRoot instanceof EqualsOperator || leftRoot instanceof BangEqualsOperator
                    || rightRoot instanceof EqualsOperator
                    || rightRoot instanceof BangEqualsOperator)
            {
                return true;
            }
            return subtreeContainsEquals(leftRoot) || subtreeContainsEquals(rightRoot);
        }
        return false;
    }
}
