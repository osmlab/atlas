package org.openstreetmap.atlas.tags.filters.matcher.parsing.tree;

/**
 * @author lcram
 */
public abstract class BinaryOperator implements ASTNode
{
    private final ASTNode left;
    private final ASTNode right;

    public BinaryOperator(final ASTNode left, final ASTNode right)
    {
        this.left = left;
        this.right = right;
    }

    @Override
    public void printTree()
    {

    }
}
