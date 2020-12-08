package org.openstreetmap.atlas.tags.filters.matcher.parsing.tree;

import java.util.Collections;
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
        return "BANGEQ_" + getIdentifier();
    }

    @Override
    public String getPrettyPrintText()
    {
        return "!=";
    }

    @Override
    public boolean match(final List<String> keys, final List<String> values)
    {
        if (keys.size() != values.size())
        {
            throw new CoreException("`keys' and `values' sizes did not match, {} vs {}",
                    keys.size(), values.size());
        }

        for (int i = 0; i < keys.size(); i++)
        {
            final boolean leftSide = getLeftChild().match(Collections.singletonList(keys.get(i)),
                    null);
            final boolean rightSide = getRightChild().match(null,
                    Collections.singletonList(values.get(i)));

            if (leftSide && !rightSide)
            {
                return true;
            }
        }

        return false;
    }
}
