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

    public BinaryOperator(final ASTNode left, final ASTNode right)
    {
        this.left = left;
        this.right = right;
        this.id = counter++;
    }

    @Override
    public String printTree()
    {
        final StringBuilder builder = new StringBuilder();
        builder.append(this.getName() + " id " + this.id + "\n");
        builder.append(this.getName() + " id " + this.id + " left side\n");
        builder.append(this.left.printTree());
        builder.append(this.getName() + " id " + this.id + " right side\n");
        builder.append(this.right.printTree());
        return builder.toString();
    }
}
