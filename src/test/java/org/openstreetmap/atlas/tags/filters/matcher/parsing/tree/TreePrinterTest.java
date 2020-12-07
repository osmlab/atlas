package org.openstreetmap.atlas.tags.filters.matcher.parsing.tree;

import org.junit.Test;
import org.openstreetmap.atlas.tags.filters.matcher.TODO_Remove_TreePrinterSampleTest;
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
        final String input3 = "foo=bar | baz=bat";
        final ASTNode root = new Parser(new Lexer().lex(input3), input3).parse();
        TODO_Remove_TreePrinterSampleTest.print(root);
        System.err.println(TreePrinter.print(root));
    }
}
