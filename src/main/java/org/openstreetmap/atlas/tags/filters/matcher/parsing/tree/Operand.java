package org.openstreetmap.atlas.tags.filters.matcher.parsing.tree;

import org.openstreetmap.atlas.tags.filters.matcher.parsing.Token;

/**
 * @author lcram
 */
public abstract class Operand implements ASTNode
{
    private static int counter = 0;

    private final Token token;
    private final int identifier;

    public static void clearIdentifierCounter()
    {
        counter = 0;
    }

    public Operand(final Token token)
    {
        this.token = token;
        this.identifier = counter++;
    }

    @Override
    public String debugPrintTree()
    {
        final StringBuilder builder = new StringBuilder();
        builder.append(this.getName() + "\n");
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
        return null;
    }

    @Override
    public ASTNode getRightChild()
    {
        return null;
    }

    public Token getToken()
    {
        return this.token;
    }
}
