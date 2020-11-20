package org.openstreetmap.atlas.tags.filters.matcher.parsing.tree;

import org.openstreetmap.atlas.tags.filters.matcher.parsing.Token;

/**
 * @author lcram
 */
public abstract class Operand implements ASTNode
{
    private final Token token;

    public Operand(final Token token)
    {
        this.token = token;
    }

    public Token getToken()
    {
        return this.token;
    }
}
