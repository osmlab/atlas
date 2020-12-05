package org.openstreetmap.atlas.tags.filters.matcher.parsing.tree;

import java.util.List;
import java.util.Objects;

import org.openstreetmap.atlas.exception.CoreException;
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
        return getToken().getLexeme() + "_" + getId();
    }

    @Override
    public String getPrettyPrintText()
    {
        return getToken().getLexeme();
    }

    @Override
    public boolean match(final List<String> keys, final List<String> values)
    {
        if (keys == null && values == null)
        {
            throw new CoreException("keys and values were null");
        }
        return Objects.requireNonNullElse(keys, values).contains(getToken().getLexeme());
    }
}
