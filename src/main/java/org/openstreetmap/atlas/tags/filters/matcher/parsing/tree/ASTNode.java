package org.openstreetmap.atlas.tags.filters.matcher.parsing.tree;

import java.io.Serializable;
import java.util.List;

import org.openstreetmap.atlas.tags.filters.matcher.TaggableMatcher;

/**
 * A generic abstract syntax tree (AST) node. Any node must be able to print itself and its subtree,
 * as well as have some kind of name for debug purposes.
 *
 * @author lcram
 */
public abstract class ASTNode implements Serializable
{
    private static final long serialVersionUID = -2085619833468362629L;

    /**
     * Construct a debug printout of the entire tree. For a prettified version of this tree, try
     * {@link ASTNode#getPrettyPrintText()}.
     *
     * @return the debug printout
     */
    public abstract String debugPrintTree();

    /**
     * Get the center child of this {@link ASTNode}. {@link UnaryOperator}s are the only type of
     * node that have center children.
     *
     * @return the center child {@link ASTNode}
     */
    public abstract ASTNode getCenterChild();

    public abstract int getIdentifier();

    /**
     * Get the left child of this {@link ASTNode}. {@link BinaryOperator}s are the only type of node
     * that have left children.
     *
     * @return the left child {@link ASTNode}
     */
    public abstract ASTNode getLeftChild();

    public abstract String getName();

    /**
     * Get the representation of this node for the purposes of {@link TaggableMatcher}'s pretty tree
     * print functionality.
     *
     * @return the pretty tree
     */
    public abstract String getPrettyPrintText();

    /**
     * Get the right child of this {@link ASTNode}. {@link BinaryOperator}s are the only type of
     * node that have right children.
     *
     * @return the right child {@link ASTNode}
     */
    public abstract ASTNode getRightChild();

    public abstract boolean match(List<String> keys, List<String> values);
}
