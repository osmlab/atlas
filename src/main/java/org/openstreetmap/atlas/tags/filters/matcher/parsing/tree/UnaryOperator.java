package org.openstreetmap.atlas.tags.filters.matcher.parsing.tree;

/**
 * @author lcram
 */
public abstract class UnaryOperator implements ASTNode
{
    private static int counter = 0;

    private final ASTNode child;
    private final int id;

    public UnaryOperator(final ASTNode child)
    {
        this.child = child;
        this.id = counter++;
    }

    @Override
    public String printTree()
    {
        final StringBuilder builder = new StringBuilder();
        builder.append(this.getName() + " id " + this.id + "\n");
        builder.append(this.getName() + " id " + this.id + " child\n");
        builder.append(this.child.printTree());
        return builder.toString();
    }
}
