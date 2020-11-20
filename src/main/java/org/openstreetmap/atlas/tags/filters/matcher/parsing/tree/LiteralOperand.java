package org.openstreetmap.atlas.tags.filters.matcher.parsing.tree;

import org.openstreetmap.atlas.tags.filters.matcher.parsing.Token;

/**
 * @author lcram
 */
public class LiteralOperand extends Operand
{
    public LiteralOperand(final Token token)
    {
        super(token);
    }

    @Override
    public String getName()
    {
        return getToken().getLexeme();
    }

    @Override
    public void printTree()
    {

    }
}
