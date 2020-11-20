package org.openstreetmap.atlas.tags.filters.matcher.parsing.tree;

/**
 * @author lcram
 */
public abstract class UnaryOperator implements ASTNode
{
    private final ASTNode child;

    public UnaryOperator(final ASTNode child)
    {
        this.child = child;
    }

    @Override
    public void printTree()
    {

    }
}
