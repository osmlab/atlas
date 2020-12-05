package org.openstreetmap.atlas.tags.filters.matcher.parsing.tree;

/**
 * @author lcram
 */
public abstract class BinaryOperator implements ASTNode
{
    private static int counter = 0;

    private final ASTNode left;
    private final ASTNode right;
    private final int id;

    public static void clearIdCounter()
    {
        counter = 0;
    }

    public BinaryOperator(final ASTNode left, final ASTNode right)
    {
        this.left = left;
        this.right = right;
        this.id = counter++;
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

    @Override public ASTNode getCenterChild()
    {
        return null;
    }

    @Override
    public int getId()
    {
        return this.id;
    }

    @Override public ASTNode getLeftChild()
    {
        return this.left;
    }

    @Override public ASTNode getRightChild()
    {
        return this.right;
    }
}
