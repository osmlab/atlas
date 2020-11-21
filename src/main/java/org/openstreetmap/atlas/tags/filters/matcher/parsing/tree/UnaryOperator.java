package org.openstreetmap.atlas.tags.filters.matcher.parsing.tree;

/**
 * @author lcram
 */
public abstract class UnaryOperator implements ASTNode
{
    private static int counter = 0;

    private final ASTNode child;
    private final int id;

    public static void clearIdCounter()
    {
        counter = 0;
    }

    public UnaryOperator(final ASTNode child)
    {
        this.child = child;
        this.id = counter++;
    }

    public ASTNode getChildSubTree()
    {
        return this.child;
    }

    @Override
    public int getId()
    {
        return this.id;
    }

    @Override
    public String printTree()
    {
        final StringBuilder builder = new StringBuilder();
        builder.append(this.getName() + "\n");
        builder.append(this.getName() + " child: " + this.child.getName() + "\n");
        builder.append(this.child.printTree());
        return builder.toString();
    }
}
