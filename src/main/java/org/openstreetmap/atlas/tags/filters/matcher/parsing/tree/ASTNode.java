package org.openstreetmap.atlas.tags.filters.matcher.parsing.tree;

import java.util.List;

/**
 * A generic abstract syntax tree (AST) node. Any node must be able to print itself and its subtree,
 * as well as have some kind of name for debug purposes.
 *
 * @author lcram
 */
public interface ASTNode
{
    String debugPrintTree();

    ASTNode getCenterChild();

    int getId();

    ASTNode getLeftChild();

    String getName();

    String getPrettyPrintText();

    ASTNode getRightChild();

    boolean match(List<String> keys, List<String> values);
}
