package org.openstreetmap.atlas.tags.filters.matcher.parsing.tree;

import java.util.List;

/**
 * @author lcram
 */
public class AndOperator extends BinaryOperator
{
    public AndOperator(final ASTNode left, final ASTNode right)
    {
        super(left, right);
    }

    @Override
    public String getName()
    {
        return "AND_" + getIdentifier();
    }

    @Override
    public String getPrettyPrintText()
    {
        return "&";
    }

    @Override
    public boolean match(final List<String> keys, final List<String> values)
    {
        return getLeftChild().match(keys, values) && getRightChild().match(keys, values);
    }
}
