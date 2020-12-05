package org.openstreetmap.atlas.tags.filters.matcher.parsing.tree;

import java.util.List;

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
    public String getPrettyPrintText()
    {
        return "!";
    }

    @Override
    public boolean match(final List<String> keys, final List<String> values)
    {
        return !getCenterChild().match(keys, values);
    }
}
