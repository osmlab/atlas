package org.openstreetmap.atlas.tags.filters.matcher.parsing;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author lcram
 */
public class LexerTest
{
    @Test
    public void test()
    {
        Lexer lexer = new Lexer();
        lexer.lex("(foo=bar|baz=bat)");
        Assert.assertEquals(
                "(PAREN_OPEN, (), (KEY_VALUE, foo), (EQUAL, =), (KEY_VALUE, bar), (OR, |), (KEY_VALUE, baz), (EQUAL, =), (KEY_VALUE, bat), (PAREN_CLOSE, )), ",
                lexer.debugString());

        lexer = new Lexer();
        lexer.lex("hello=world foo|a=b");
        Assert.assertEquals(
                "(KEY_VALUE, hello), (EQUAL, =), (KEY_VALUE, world foo), (OR, |), (KEY_VALUE, a), (EQUAL, =), (KEY_VALUE, b), ",
                lexer.debugString());

        lexer = new Lexer();
        lexer.lex("a=b\\=c");
        Assert.assertEquals(
                "(KEY_VALUE, a), (EQUAL, =), (KEY_VALUE, b), (ESCAPE, =), (KEY_VALUE, c), ",
                lexer.debugString());
    }
}
