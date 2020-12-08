package org.openstreetmap.atlas.tags.filters.matcher.parsing.tree;

/**
 * @author lcram
 */
public abstract class BinaryOperator implements ASTNode
{
    private static int counter = 0;

    private final ASTNode left;
    private final ASTNode right;
    private final int identifier;

    public static void clearIdentifierCounter()
    {
        counter = 0;
    }

    public BinaryOperator(final ASTNode left, final ASTNode right)
    {
        this.left = left;
        this.right = right;
        this.identifier = counter++;
    }

    @Override
    public String debugPrintTree()
    {
        final StringBuilder builder = new StringBuilder();
        builder.append(this.getName() + "\n");
        builder.append(this.getName() + " left: " + this.left.getName() + "\n");
        builder.append(this.getName() + " right: " + this.right.getName() + "\n");
        builder.append(this.left.debugPrintTree());
        builder.append(this.right.debugPrintTree());
        return builder.toString();
    }

    @Override
    public ASTNode getCenterChild()
    {
        return null;
    }

    @Override
    public int getIdentifier()
    {
        return this.identifier;
    }

    @Override
    public ASTNode getLeftChild()
    {
        return this.left;
    }

    @Override
    public ASTNode getRightChild()
    {
        return this.right;
    }
}
