package org.openstreetmap.atlas.tags.filters.matcher.parsing.tree;

import java.util.List;

import org.openstreetmap.atlas.exception.CoreException;
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
        return getToken().getLexeme() + "_" + getId();
    }

    @Override
    public boolean match(final List<String> keys, final List<String> values)
    {
        throw new CoreException("TODO support");
    }
}
