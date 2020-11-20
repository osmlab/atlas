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
        /*
         * TODO "foo = (bar = baz)" is valid syntactically per the grammar, but not semantically. We
         * will need to catch this with some kind of type checker. Basically. we can walk the AST,
         * and if the left or right subtree of a "="/"!=" operator contains another "="/"!="
         * operator, then we fail since that is invalid semantically.
         */
        final List<Token> input = new Lexer()
                .lex("(foo = bar | baz = bat) & hello = \"world with a space\"");
        System.err.println(Lexer.debugString(input));
        new Parser(input).parse();
    }
}
