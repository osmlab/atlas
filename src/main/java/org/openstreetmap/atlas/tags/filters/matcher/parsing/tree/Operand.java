package org.openstreetmap.atlas.tags.filters.matcher.parsing.tree;

import java.io.Serializable;

import org.openstreetmap.atlas.tags.filters.matcher.parsing.Token;

/**
 * @author lcram
 */
public abstract class Operand implements ASTNode, Serializable
{
    private static int counter = 0;
    private static final long serialVersionUID = 4045177960157269200L;

    private final Token token;
    private final int identifier;

    public static void clearIdentifierCounter()
    {
        counter = 0;
    }

    protected Operand(final Token token)
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
