package org.openstreetmap.atlas.tags.filters.matcher;

import java.util.ArrayList;
import java.util.List;

/**
 * Binary tree printer https://stackoverflow.com/a/29704252
 * 
 * @author MightyPork
 */
public class TODO_Remove_TreePrinterSampleTest
{
    public static class Node implements PrintableNode
    {
        PrintableNode left;
        PrintableNode center;
        PrintableNode right;
        String value;

        public Node(final PrintableNode left, final PrintableNode center, final PrintableNode right,
                final String value)
        {
            this.left = left;
            this.center = center;
            this.right = right;
            this.value = value;
        }

        @Override
        public PrintableNode getCenter()
        {
            return this.center;
        }

        @Override
        public PrintableNode getLeft()
        {
            return this.left;
        }

        @Override
        public PrintableNode getRight()
        {
            return this.right;
        }

        @Override
        public String getText()
        {
            return this.value;
        }
    }

    public static void main(final String[] args)
    {
        final Node foo = new Node(null, null, null, "foo");

        final Node fooRight = new Node(null, null, foo, "=");
        final Node fooRightRight = new Node(null, null, fooRight, "=");
        final Node fooRightRightRight = new Node(null, null, fooRightRight, "=");

        print(fooRightRightRight);
    }

    public static void main2(final String[] args)
    {
        final Node bar = new Node(null, null, null, "bar");
        final Node cat = new Node(null, null, null, "cat");
        final Node mat = new Node(null, null, null, "mat");

        final Node catOrMat = new Node(cat, null, mat, "|");
        final Node notCatOrMat = new Node(catOrMat, null, null, "!");
        final Node barEqNotCatOrMat = new Node(bar, null, notCatOrMat, "=");

        print(barEqNotCatOrMat);
    }

    /**
     * Print a tree
     *
     * @param root
     *            tree root node
     */
    public static void print(final PrintableNode root)
    {
        final List<List<String>> lines = new ArrayList<>();

        List<PrintableNode> nodesThisLevel = new ArrayList<>();
        List<PrintableNode> nodesNextLevel = new ArrayList<>();

        nodesThisLevel.add(root);
        int numberOfNodesRemaining = 1;
        int widest = 0;

        while (numberOfNodesRemaining != 0)
        {
            final List<String> line = new ArrayList<>();
            numberOfNodesRemaining = 0;
            for (final PrintableNode node : nodesThisLevel)
            {
                if (node == null)
                {
                    line.add(null);
                    nodesNextLevel.add(null);
                    nodesNextLevel.add(null);
                }
                else
                {
                    final String nodeText = node.getText();
                    line.add(nodeText);
                    if (nodeText.length() > widest)
                    {
                        widest = nodeText.length();
                    }

                    nodesNextLevel.add(node.getLeft());
                    nodesNextLevel.add(node.getRight());

                    if (node.getLeft() != null)
                    {
                        numberOfNodesRemaining++;
                    }
                    if (node.getRight() != null)
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

            final List<PrintableNode> tmp = nodesThisLevel;
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

    /** Node that can be printed */
    public interface PrintableNode
    {
        /** Get center child */
        PrintableNode getCenter();

        /** Get left child */
        PrintableNode getLeft();

        /** Get right child */
        PrintableNode getRight();

        /** Get text to be printed */
        String getText();
    }
}
