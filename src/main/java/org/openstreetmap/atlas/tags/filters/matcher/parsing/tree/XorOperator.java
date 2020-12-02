package org.openstreetmap.atlas.tags.filters.matcher.parsing.tree;

import java.util.List;

/**
 * @author lcram
 */
public class XorOperator extends BinaryOperator
{
    public XorOperator(final ASTNode left, final ASTNode right)
    {
        super(left, right);
    }

    @Override
    public String getName()
    {
        return "XOR_" + getId();
    }

    @Override
    public boolean match(final List<String> keys, final List<String> values)
    {
        final boolean left = getLeftSubTree().match(keys, values);
        final boolean right = getRightSubTree().match(keys, values);
        return (left || right) && !(left && right);
    }
}
