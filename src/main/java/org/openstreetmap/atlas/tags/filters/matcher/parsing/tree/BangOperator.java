package org.openstreetmap.atlas.tags.filters.matcher.parsing.tree;

import java.util.List;

import org.openstreetmap.atlas.exception.CoreException;

/**
 * @author lcram
 */
public class BangOperator extends UnaryOperator
{
    public BangOperator(final ASTNode child)
    {
        super(child);
    }

    @Override
    public String getName()
    {
        return "BANG_" + getId();
    }

    @Override
    public boolean match(final List<String> keys, final List<String> values)
    {
        throw new CoreException("TODO support");
    }
}
