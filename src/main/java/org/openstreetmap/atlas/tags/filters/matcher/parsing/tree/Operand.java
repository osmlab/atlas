package org.openstreetmap.atlas.tags.filters.matcher.parsing.tree;

import org.openstreetmap.atlas.tags.filters.matcher.parsing.Token;

/**
 * @author lcram
 */
public abstract class Operand implements ASTNode
{
    private static int counter = 0;

    private final Token token;
    private final int id;

    public static void clearIdCounter()
    {
        counter = 0;
    }

    public Operand(final Token token)
    {
        this.token = token;
        this.id = counter++;
    }

    @Override
    public int getId()
    {
        return this.id;
    }

    public Token getToken()
    {
        return this.token;
    }

    @Override
    public String printTree()
    {
        final StringBuilder builder = new StringBuilder();
        builder.append(this.getName() + "\n");
        return builder.toString();
    }
}
