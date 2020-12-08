package org.openstreetmap.atlas.tags.filters.matcher.parsing.tree;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.tags.filters.matcher.TaggableMatcher;
import org.openstreetmap.atlas.utilities.tuples.Tuple;

/**
 * Inspired by MightyPork's algorithm here: https://stackoverflow.com/a/29704252
 *
 * @author lcram
 */
public final class TreePrinter
{
    public static long lengthOfLongestLineForTree(final ASTNode root)
    {
        return Arrays.stream(print(root).split("\n")).mapToLong(String::length).max()
                .orElseThrow(() -> new CoreException("Failed to caluclate longest line length!"));
    }

    /**
     * Get a {@link TaggableMatcher} tree as a string.
     *
     * @param root
     *            the root {@link ASTNode} of the tree
     * @return the string tree
     */
    public static String print(final ASTNode root) // NOSONAR
    {
        final StringBuilder treeString = new StringBuilder();
        final Tuple<List<List<String>>, Integer> tuple = discoverAllTreeNodes(root);
        final List<List<String>> lines = tuple.getFirst();
        final int widestNodeWidth = tuple.getSecond();

        /*
         * How are we calculating this? The last line returned by the breadth first search will be
         * the longest, since it will contain a lot of nulls for every dead branch in the tree. So
         * we use the last line as a baseline for line length. Then, we multiply by the widest width
         * of any node plus 4 (4 gives some nice padding for readability). As the loop walks down
         * the tree, this value will be continually halved, since each level there are approx. twice
         * as many tree pieces.
         */
        final int widthPadding = 4;
        int lengthOfTreePiece = lines.get(lines.size() - 1).size()
                * (widestNodeWidth + widthPadding);
        boolean firstIteration = true;
        for (final List<String> line : lines)
        {
            final int nodeLeftRightPadding = (int) Math.floor(lengthOfTreePiece / 2f) - 1;

            /*
             * This section prints the Unicode box-drawing characters above each line of actual
             * elements. It does not run on the first iteration of the loop, since there is no line
             * containing Unicode box-drawing characters to print above the root node.
             */
            if (!firstIteration)
            {
                for (int lineElementIndex = 0; lineElementIndex < line.size(); lineElementIndex++)
                {
                    /*
                     * Decide which Unicode box-drawing character to print below the nodes *ABOVE*
                     * the current line. Only print for odd elements within the line. Since the tree
                     * is binary, there is only one node "between" each of the nodes in the current
                     * line.
                     */
                    char boxCharacter = ' ';
                    if (isOdd(lineElementIndex))
                    {
                        if (line.get(lineElementIndex - 1) != null)
                        {
                            boxCharacter = (line.get(lineElementIndex) != null) ? '┴' : '┘';
                        }
                        else if (line.get(lineElementIndex) != null)
                        {
                            boxCharacter = '└';
                        }
                    }
                    treeString.append(boxCharacter);

                    /*
                     * Print whitespace above null line elements, since nothing is there.
                     */
                    if (line.get(lineElementIndex) == null)
                    {
                        treeString.append(" ".repeat(Math.max(0, lengthOfTreePiece - 1)));
                    }
                    /*
                     * Here we decide which box-drawing character to print above the nodes *BELOW*
                     * the current line.
                     */
                    else
                    {
                        treeString.append((isEven(lineElementIndex) ? " " : "─")
                                .repeat(Math.max(0, nodeLeftRightPadding)));
                        treeString.append(isEven(lineElementIndex) ? "┌" : "┐");
                        treeString.append((isEven(lineElementIndex) ? "─" : " ")
                                .repeat(Math.max(0, nodeLeftRightPadding)));
                    }
                }
                treeString.append("\n");
            }

            /*
             * This section prints the actual line of elements.
             */
            for (final String element : line)
            {
                String element2 = element;
                if (element2 == null)
                {
                    element2 = "";
                }
                final double padding = (lengthOfTreePiece / 2f) - (element2.length() / 2f);
                final int paddingLeft = (int) Math.ceil(padding);
                final int paddingRight = (int) Math.floor(padding);

                treeString.append(" ".repeat(Math.max(0, paddingLeft)));
                treeString.append(element2);
                treeString.append(" ".repeat(Math.max(0, paddingRight)));
            }
            treeString.append("\n");

            lengthOfTreePiece /= 2;
            firstIteration = false;
        }

        return treeString.toString();
    }

    private static Tuple<List<List<String>>, Integer> discoverAllTreeNodes(final ASTNode root) // NOSONAR
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

                    if (node.getCenterChild() != null)
                    {
                        nodesNextLevel.add(node.getCenterChild());
                    }
                    else
                    {
                        nodesNextLevel.add(node.getLeftChild());
                    }
                    nodesNextLevel.add(node.getRightChild());

                    if (node.getCenterChild() != null)
                    {
                        numberOfNodesRemaining++;
                    }
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

    private static boolean isEven(final int integer)
    {
        return integer % 2 == 0;
    }

    private static boolean isOdd(final int integer)
    {
        return integer % 2 != 0;
    }

    private TreePrinter()
    {
    }
}
