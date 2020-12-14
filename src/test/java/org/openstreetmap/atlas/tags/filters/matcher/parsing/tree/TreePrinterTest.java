package org.openstreetmap.atlas.tags.filters.matcher.parsing.tree;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.tags.filters.matcher.parsing.Lexer;
import org.openstreetmap.atlas.tags.filters.matcher.parsing.Parser;

/**
 * @author lcram
 */
public class TreePrinterTest
{
    @Test
    public void test()
    {
        final String input1 = "foo=bar | !(baz=bat)";
        ASTNode root = new Parser(new Lexer().lex(input1), input1).parse();
        final String tree1 = TreePrinter.print(root);
        Assert.assertEquals(
                "                                |                               \n"
                        + "                ┌───────────────┴───────────────┐               \n"
                        + "                =                               !               \n"
                        + "        ┌───────┴───────┐               ┌───────┘               \n"
                        + "       foo             bar              =                       \n"
                        + "                                    ┌───┴───┐                   \n"
                        + "                                   baz     bat                  \n",
                tree1);

        final String input2 = "foo=bar | baz=bat";
        root = new Parser(new Lexer().lex(input2), input2).parse();
        final String tree2 = TreePrinter.print(root);
        Assert.assertEquals("                |               \n"
                + "        ┌───────┴───────┐       \n" + "        =               =       \n"
                + "    ┌───┴───┐       ┌───┴───┐   \n" + "   foo     bar     baz     bat  \n",
                tree2);

        final String input3 = "foo=bar | baz=bat & cat=mat";
        root = new Parser(new Lexer().lex(input3), input3).parse();
        final String tree3 = TreePrinter.print(root);
        Assert.assertEquals(
                "                                |                               \n"
                        + "                ┌───────────────┴───────────────┐               \n"
                        + "                =                               &               \n"
                        + "        ┌───────┴───────┐               ┌───────┴───────┐       \n"
                        + "       foo             bar              =               =       \n"
                        + "                                    ┌───┴───┐       ┌───┴───┐   \n"
                        + "                                   baz     bat     cat     mat  \n",
                tree3);

        final String input4 = "(foo=bar | baz=bat) & cat=mat";
        root = new Parser(new Lexer().lex(input4), input4).parse();
        final String tree4 = TreePrinter.print(root);
        Assert.assertEquals(
                "                                &                               \n"
                        + "                ┌───────────────┴───────────────┐               \n"
                        + "                |                               =               \n"
                        + "        ┌───────┴───────┐               ┌───────┴───────┐       \n"
                        + "        =               =              cat             mat      \n"
                        + "    ┌───┴───┐       ┌───┴───┐                                   \n"
                        + "   foo     bar     baz     bat                                  \n",
                tree4);
    }
}
