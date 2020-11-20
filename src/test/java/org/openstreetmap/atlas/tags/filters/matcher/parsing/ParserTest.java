package org.openstreetmap.atlas.tags.filters.matcher.parsing;

import java.util.List;

import org.junit.Test;

/**
 * @author lcram
 */
public class ParserTest
{
    @Test
    public void test()
    {
        final List<Token> input = new Lexer()
                .lex("(foo = bar | baz = bat) & hello = \"world with a space\"");
        System.err.println(Lexer.debugString(input));
        new Parser(input).parse();

        // TODO actually do a test here
    }
}
