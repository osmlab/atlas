package org.openstreetmap.atlas.tags.filters.matcher.parsing;

import java.util.List;

import org.junit.Test;
import org.openstreetmap.atlas.tags.filters.matcher.parsing.tree.ASTNode;

/**
 * @author lcram
 */
public class ParserTest
{
    @Test
    public void test()
    {
        final List<Token> input = new Lexer().lex("foo = bar | (baz = bat & hello = world)");
        System.err.println(Lexer.debugString(input));
        final ASTNode node = new Parser(input).parse();
        System.err.println(node.printTree());

        // TODO actually do a test here
    }
}
