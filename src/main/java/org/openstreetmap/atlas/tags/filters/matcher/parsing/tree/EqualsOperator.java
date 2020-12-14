package org.openstreetmap.atlas.tags.filters.matcher.parsing.tree;

import java.util.Collections;
import java.util.List;

import org.openstreetmap.atlas.exception.CoreException;

/**
 * @author lcram
 */
public class EqualsOperator extends BinaryOperator
{
    private static final long serialVersionUID = 4853555543879394794L;
    private final boolean bang;

    public EqualsOperator(final ASTNode left, final ASTNode right, final boolean bang)
    {
        super(left, right);
        this.bang = bang;
    }

    @Override
    public String getName()
    {
        if (this.bang)
        {
            return "BANGEQ_" + getIdentifier();
        }
        else
        {
            return "EQ_" + getIdentifier();
        }
    }

    @Override
    public String getPrettyPrintText()
    {
        if (this.bang)
        {
            return "!=";
        }
        else
        {
            return "=";
        }
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
            boolean rightSide = getRightChild().match(null,
                    Collections.singletonList(values.get(i)));

            /*
             * For BANG_EQUALS, flip the boolean value of the right side to mimic the logic of `!='.
             */
            if (this.bang)
            {
                rightSide = !rightSide;
            }

            if (leftSide && rightSide)
            {
                return true;
            }
        }

        return false;
    }
}
