package org.openstreetmap.atlas.tags.filters.matcher.parsing.tree;

import java.util.ArrayList;
import java.util.List;

import org.openstreetmap.atlas.tags.filters.matcher.TaggableMatcher;
import org.openstreetmap.atlas.utilities.tuples.Tuple;

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
        final Tuple<List<List<String>>, Integer> tuple = discoverAllTreeNodes(root);
        final List<List<String>> lines = tuple.getFirst();
        final int widestNodeWidth = tuple.getSecond();

        final int lengthPerPrintedLine = lines.get(lines.size() - 1).size() * (widestNodeWidth + 4);
        System.err.println("widestNodeWidth: " + widestNodeWidth);
        System.err.println("lengthPerPrintedLine: " + lengthPerPrintedLine);

        // TODO implement

        return lines.toString();
    }

    private static Tuple<List<List<String>>, Integer> discoverAllTreeNodes(final ASTNode root)
    {
        final List<List<String>> lines = new ArrayList<>();

        List<ASTNode> nodesThisLevel = new ArrayList<>();
        List<ASTNode> nodesNextLevel = new ArrayList<>();

        nodesThisLevel.add(root);
        int numberOfNodesRemaining = 1;
        int widestNodeWidth = 0;

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
                    if (nodeText.length() > widestNodeWidth)
                    {
                        widestNodeWidth = nodeText.length();
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

            if (isOdd(widestNodeWidth))
            {
                widestNodeWidth++;
            }

            lines.add(line);

            final List<ASTNode> tmp = nodesThisLevel;
            nodesThisLevel = nodesNextLevel;
            nodesNextLevel = tmp;
            nodesNextLevel.clear();
        }

        return new Tuple<>(lines, widestNodeWidth);
    }

    private static boolean isOdd(final int integer)
    {
        return integer % 2 != 0;
    }

    private TreePrinter()
    {
    }
}
