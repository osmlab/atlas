package org.openstreetmap.atlas.tags.filters.matcher.parsing.tree;

import java.util.List;

import org.openstreetmap.atlas.exception.CoreException;

/**
 * @author lcram
 */
public class BangEqualsOperator extends BinaryOperator
{
    public BangEqualsOperator(final ASTNode left, final ASTNode right)
    {
        super(left, right);
    }

    @Override
    public String getName()
    {
        return "BANGEQ_" + getId();
    }

    @Override
    public boolean match(final List<String> keys, final List<String> values)
    {
        throw new CoreException("TODO support");
    }
}
