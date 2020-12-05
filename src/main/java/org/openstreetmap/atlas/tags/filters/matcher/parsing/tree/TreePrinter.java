package org.openstreetmap.atlas.tags.filters.matcher.parsing.tree;

import java.util.ArrayList;
import java.util.List;

import org.openstreetmap.atlas.tags.filters.matcher.TaggableMatcher;

/**
 * Inspired by MightyPork's algorithm here: https://stackoverflow.com/a/29704252
 *
 * @author lcram
 */
public final class TreePrinter
{
    /**
     * Print a {@link TaggableMatcher} tree.
     *
     * @param root
     *            the root {@link ASTNode} of the tree
     */
    public static String print(final ASTNode root)
    {
        final List<List<String>> lines = breadthFirstSearchNodesAndGetLines(root);

        // TODO implement

        return lines.toString();
    }

    private static List<List<String>> breadthFirstSearchNodesAndGetLines(final ASTNode root)
    {
        final List<List<String>> lines = new ArrayList<>();

        List<ASTNode> nodesThisLevel = new ArrayList<>();
        List<ASTNode> nodesNextLevel = new ArrayList<>();

        nodesThisLevel.add(root);
        int numberOfNodesRemaining = 1;
        int widest = 0;

        while (numberOfNodesRemaining != 0)
        {
            final List<String> line = new ArrayList<>();
            numberOfNodesRemaining = 0;
            for (final ASTNode node : nodesThisLevel)
            {
                if (node == null)
                {
                    line.add(null);
                    nodesNextLevel.add(null);
                    nodesNextLevel.add(null);
                }
                else
                {
                    final String nodeText = node.getPrettyPrintText();
                    line.add(nodeText);
                    if (nodeText.length() > widest)
                    {
                        widest = nodeText.length();
                    }

                    nodesNextLevel.add(node.getLeftChild());
                    nodesNextLevel.add(node.getRightChild());

                    if (node.getLeftChild() != null)
                    {
                        numberOfNodesRemaining++;
                    }
                    if (node.getRightChild() != null)
                    {
                        numberOfNodesRemaining++;
                    }
                }
            }

            if (widest % 2 == 1)
            {
                widest++;
            }

            lines.add(line);

            final List<ASTNode> tmp = nodesThisLevel;
            nodesThisLevel = nodesNextLevel;
            nodesNextLevel = tmp;
            nodesNextLevel.clear();
        }

        return lines;
    }

    private TreePrinter()
    {
    }
}
