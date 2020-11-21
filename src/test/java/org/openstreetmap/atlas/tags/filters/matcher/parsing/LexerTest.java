package org.openstreetmap.atlas.tags.filters.matcher.parsing;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.openstreetmap.atlas.exception.CoreException;

/**
 * @author lcram
 */
public class LexerTest
{
    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Test
    public void testFailEscape()
    {
        final Lexer lexer = new Lexer();

        this.expectedException.expect(CoreException.class);
        this.expectedException
                .expectMessage("Unexpected EOF after '\\' while lexing TaggableMatcher");
        lexer.lex("foo=bar\\");
    }

    @Test
    public void testFailQuote()
    {
        final Lexer lexer = new Lexer();

        this.expectedException.expect(CoreException.class);
        this.expectedException
                .expectMessage("Unexpected EOF after '\"' while lexing TaggableMatcher");
        lexer.lex("foo=\"bar\"baz\"");
    }

    @Test
    public void testFailRegex()
    {
        final Lexer lexer = new Lexer();

        this.expectedException.expect(CoreException.class);
        this.expectedException
                .expectMessage("Unexpected EOF after '/' while lexing TaggableMatcher");
        lexer.lex("foo=/bar");
    }

    @Test
    public void testSuccessfulLex()
    {
        final Lexer lexer = new Lexer();

        Assert.assertEquals(
                "(PAREN_OPEN, (), (LITERAL, foo), (EQUAL, =), (LITERAL, bar), (OR, |), (LITERAL, baz), (EQUAL, =), (LITERAL, bat), (PAREN_CLOSE, )), ",
                Lexer.debugString(lexer.lex("(foo=bar|baz=bat)")));

        /*
         * This one has a syntax error that would manifest at the parse step ("world" literal
         * followed by "foo" literal). It should lex OK.
         */
        Assert.assertEquals(
                "(LITERAL, hello), (EQUAL, =), (LITERAL, world), (LITERAL, foo), (OR, |), (LITERAL, a), (EQUAL, =), (BANG, !), (LITERAL, b), ",
                Lexer.debugString(lexer.lex("hello=world foo|a=!b")));

        Assert.assertEquals("(LITERAL, a), (EQUAL, =), (LITERAL, b=c), ",
                Lexer.debugString(lexer.lex("a=b\\=c")));

        Assert.assertEquals("(LITERAL, a), (EQUAL, =), (LITERAL, =b), ",
                Lexer.debugString(lexer.lex("a=\\=b")));

        Assert.assertEquals("(LITERAL, message), (EQUAL, =), (LITERAL, Hello World), ",
                Lexer.debugString(lexer.lex("message=Hello\\ World")));

        Assert.assertEquals(
                "(LITERAL, foo), (BANG_EQUAL, !=), (LITERAL, bar), (AND, &), (BANG, !), (LITERAL, baz), ",
                Lexer.debugString(lexer.lex("foo!=bar&!baz")));

        Assert.assertEquals("(LITERAL, foo), (EQUAL, =), (REGEX, bar), ",
                Lexer.debugString(lexer.lex("foo=/bar/")));

        Assert.assertEquals("(LITERAL, math), (EQUAL, =), (LITERAL, 2+2=4), ",
                Lexer.debugString(lexer.lex("math=\"2+2=4\"")));

        Assert.assertEquals("(LITERAL, foo), (EQUAL, =), (REGEX, bar\\/baz\\.), ",
                Lexer.debugString(lexer.lex("foo=/bar\\/baz\\./")));

        Assert.assertEquals("(LITERAL, foo), (EQUAL, =), (LITERAL, bar \"baz   bat), ",
                Lexer.debugString(lexer.lex("foo=\"bar \\\"baz   bat\"")));

        Assert.assertEquals(
                "(LITERAL, foo), (EQUAL, =), (LITERAL, bar), (AND, &), (PAREN_OPEN, (), (LITERAL, a), (EQUAL, =), (LITERAL, b), (OR, |), (LITERAL, c), (EQUAL, =), (LITERAL, d), (PAREN_CLOSE, )), ",
                Lexer.debugString(lexer.lex("    foo = bar & (   a=b | c = d)  ")));
    }

    @Test
    public void testTokenEquals()
    {
        final Token token1 = new Token(Token.TokenType.LITERAL, "foo", 0);
        final Token token2 = new Token(Token.TokenType.LITERAL, "foo", 0);
        final Token token3 = new Token(Token.TokenType.LITERAL, "bar", 4);

        Assert.assertEquals(token1, token2);
        Assert.assertNotEquals(token1, token3);
    }
}
