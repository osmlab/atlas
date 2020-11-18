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
                "(PAREN_OPEN, (), (LITERAL, foo), (EQUAL, =), (LITERAL, bar), (OR, |), (LITERAL, baz), (EQUAL, =), (LITERAL, bat), (PAREN_CLOSE, )), ",
                lexer.debugString());

        lexer = new Lexer();
        lexer.lex("hello=world foo|a=!b");
        Assert.assertEquals(
                "(LITERAL, hello), (EQUAL, =), (LITERAL, world), (WHITESPACE,  ), (LITERAL, foo), (OR, |), (LITERAL, a), (EQUAL, =), (BANG, !), (LITERAL, b), ",
                lexer.debugString());

        lexer = new Lexer();
        lexer.lex("a=b\\=c");
        Assert.assertEquals("(LITERAL, a), (EQUAL, =), (LITERAL, b), (ESCAPE, =), (LITERAL, c), ",
                lexer.debugString());

        lexer = new Lexer();
        lexer.lex("foo!=bar&!baz");
        Assert.assertEquals(
                "(LITERAL, foo), (BANG_EQUAL, !=), (LITERAL, bar), (AND, &), (BANG, !), (LITERAL, baz), ",
                lexer.debugString());

        lexer = new Lexer();
        lexer.lex("foo=/bar/");
        Assert.assertEquals("(LITERAL, foo), (EQUAL, =), (REGEX, bar), ", lexer.debugString());

        lexer = new Lexer();
        lexer.lex("foo=/bar\\/baz\\./");
        Assert.assertEquals("(LITERAL, foo), (EQUAL, =), (REGEX, bar\\/baz\\.), ",
                lexer.debugString());

        lexer = new Lexer();
        lexer.lex("foo=\"bar \\\"baz\"bat");
        Assert.assertEquals(
                "(LITERAL, foo), (EQUAL, =), (DOUBLE_QUOTE, bar \"baz), (LITERAL, bat), ",
                lexer.debugString());

        lexer = new Lexer();
        lexer.lex("foo = bar & (a=b | c = d)");
        Assert.assertEquals(
                "(LITERAL, foo), (WHITESPACE,  ), (EQUAL, =), (WHITESPACE,  ), (LITERAL, bar), (WHITESPACE,  ), (AND, &), (WHITESPACE,  ), (PAREN_OPEN, (), (LITERAL, a), (EQUAL, =), (LITERAL, b), (WHITESPACE,  ), (OR, |), (WHITESPACE,  ), (LITERAL, c), (WHITESPACE,  ), (EQUAL, =), (WHITESPACE,  ), (LITERAL, d), (PAREN_CLOSE, )), ",
                lexer.debugString());
    }
}
