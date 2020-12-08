package org.openstreetmap.atlas.tags.filters.matcher.parsing.tree;

/**
 * @author lcram
 */
public abstract class UnaryOperator implements ASTNode
{
    private static int counter = 0;

    private final ASTNode child;
    private final int identifier;

    public static void clearIdentifierCounter()
    {
        counter = 0;
    }

    public UnaryOperator(final ASTNode child)
    {
        this.child = child;
        this.identifier = counter++;
    }

    @Override
    public String debugPrintTree()
    {
        final StringBuilder builder = new StringBuilder();
        builder.append(this.getName() + "\n");
        builder.append(this.getName() + " child: " + this.child.getName() + "\n");
        builder.append(this.child.debugPrintTree());
        return builder.toString();
    }

    @Override
    public ASTNode getCenterChild()
    {
        return this.child;
    }

    @Override
    public int getIdentifier()
    {
        return this.identifier;
    }

    @Override
    public ASTNode getLeftChild()
    {
        return null;
    }

    @Override
    public ASTNode getRightChild()
    {
        return null;
    }
}
