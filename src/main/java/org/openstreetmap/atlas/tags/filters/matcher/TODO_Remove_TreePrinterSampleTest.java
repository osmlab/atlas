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

        int perpiece = lines.get(lines.size() - 1).size() * (widest + 4);
        for (int i = 0; i < lines.size(); i++)
        {
            final List<String> line = lines.get(i);
            final int heightPerWidth = (int) Math.floor(perpiece / 2f) - 1;

            /*
             * This section prints the tree pieces between each line of actual elements.
             */
            if (i > 0)
            {
                for (int j = 0; j < line.size(); j++)
                {
                    // split node
                    char c = ' ';
                    if (j % 2 == 1)
                    {
                        if (line.get(j - 1) != null)
                        {
                            c = (line.get(j) != null) ? '┴' : '┘';
                        }
                        else
                        {
                            if (j < line.size() && line.get(j) != null)
                                c = '└';
                        }
                    }
                    System.out.print(c);

                    // lines and spaces
                    if (line.get(j) == null)
                    {
                        for (int k = 0; k < perpiece - 1; k++)
                        {
                            System.out.print(" ");
                        }
                    }
                    else
                    {
                        for (int k = 0; k < heightPerWidth; k++)
                        {
                            System.out.print(j % 2 == 0 ? " " : "─");
                        }
                        System.out.print(j % 2 == 0 ? "┌" : "┐");
                        for (int k = 0; k < heightPerWidth; k++)
                        {
                            System.out.print(j % 2 == 0 ? "─" : " ");
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
                final double gapExact = (perpiece / 2f) - (element.length() / 2f);
                final int gapLeft = (int) Math.ceil(gapExact);
                final int gapRight = (int) Math.floor(gapExact);

                // a number
                for (int k = 0; k < gapLeft; k++)
                {
                    System.out.print(" ");
                }
                System.out.print(element);
                for (int k = 0; k < gapRight; k++)
                {
                    System.out.print(" ");
                }
            }
            System.out.println();

            perpiece /= 2;
        }
    }
}
