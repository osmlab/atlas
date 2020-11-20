package org.openstreetmap.atlas.tags.filters.matcher.parsing.tree;

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
}
