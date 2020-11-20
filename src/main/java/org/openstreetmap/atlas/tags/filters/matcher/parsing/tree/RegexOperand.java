package org.openstreetmap.atlas.tags.filters.matcher.parsing.tree;

import org.openstreetmap.atlas.tags.filters.matcher.parsing.Token;

/**
 * @author lcram
 */
public class RegexOperand extends Operand
{
    public RegexOperand(final Token token)
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
