package org.openstreetmap.atlas.tags.filters.matcher;

import java.util.ArrayList;
import java.util.List;

import org.openstreetmap.atlas.tags.filters.matcher.parsing.tree.ASTNode;

/**
 * Binary tree printer https://stackoverflow.com/a/29704252
 * 
 * @author MightyPork
 */
public class TODO_Remove_TreePrinterSampleTest
{
    /**
     * Print a tree
     *
     * @param root
     *            tree root node
     */
    public static void print(final ASTNode root)
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
                /*
                 * Why add all these nulls? Because we want every possible place a node *could*
                 * appear to be "accounted for". Always thinking of the tree as full, but with empty
                 * places filled in by null, will make spacing and box-character drawing decisions
                 * in the next section much easier.
                 */
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

        /*
         * How are we calculating this? The last line returned by the breadth first search will be
         * the longest, since it will contain a lot of nulls for every dead branch in the tree. So
         * we use the last line as a baseline for line length. Then, we multiply by the widest width
         * of any node plus 4 (4 gives some nice padding for readability). As the loop walks down
         * the tree, this value will be continually halved, since each level there are approx. twice
         * as many tree pieces.
         */
        int lengthOfTreePiece = lines.get(lines.size() - 1).size() * (widestNodeWidth + 4);
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
                    System.out.print(boxCharacter);

                    /*
                     * Print whitespace above null line elements, since nothing is there.
                     */
                    if (line.get(lineElementIndex) == null)
                    {
                        for (int k = 0; k < lengthOfTreePiece - 1; k++)
                        {
                            System.out.print(" ");
                        }
                    }
                    /*
                     * Here we decide which box-drawing character to print above the nodes *BELOW*
                     * the current line.
                     */
                    else
                    {
                        for (int k = 0; k < nodeLeftRightPadding; k++)
                        {
                            System.out.print(isEven(lineElementIndex) ? " " : "─");
                        }
                        System.out.print(isEven(lineElementIndex) ? "┌" : "┐");
                        for (int k = 0; k < nodeLeftRightPadding; k++)
                        {
                            System.out.print(isEven(lineElementIndex) ? "─" : " ");
                        }
                    }
                }
                System.out.println();
            }

            /*
             * This section prints the actual line of elements.
             */
            for (String element : line)
            {
                if (element == null)
                {
                    element = "";
                }
                final double padding = (lengthOfTreePiece / 2f) - (element.length() / 2f);
                final int paddingLeft = (int) Math.ceil(padding);
                final int paddingRight = (int) Math.floor(padding);

                for (int k = 0; k < paddingLeft; k++)
                {
                    System.out.print(" ");
                }
                System.out.print(element);
                for (int k = 0; k < paddingRight; k++)
                {
                    System.out.print(" ");
                }
            }
            System.out.println();

            lengthOfTreePiece /= 2;
            firstIteration = false;
        }
    }

    private static boolean isEven(final int integer)
    {
        return integer % 2 == 0;
    }

    private static boolean isOdd(final int integer)
    {
        return integer % 2 != 0;
    }
}
