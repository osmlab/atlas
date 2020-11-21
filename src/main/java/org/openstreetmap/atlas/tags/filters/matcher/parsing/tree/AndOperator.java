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
        return "AND_" + getId();
    }

    @Override
    public boolean match(final List<String> keys, final List<String> values)
    {
        return getLeftSubTree().match(keys, values) && getRightSubTree().match(keys, values);
    }
}
