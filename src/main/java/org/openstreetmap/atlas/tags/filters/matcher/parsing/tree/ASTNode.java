package org.openstreetmap.atlas.tags.filters.matcher.parsing.tree;

/**
 * A generic abstract syntax tree (AST) node. Any node must be able to print itself and its subtree,
 * as well as have some kind of name for debug purposes.
 * 
 * @author lcram
 */
public interface ASTNode
{
    String getName();

    String printTree();
}
