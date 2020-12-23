package org.openstreetmap.atlas.tags.filters.matcher.parsing;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.openstreetmap.atlas.exception.CoreException;

/**
 * @author lcram
 */
public class SemanticCheckerTest
{
    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Test
    public void test()
    {
        // These will never fail

        final String input = "foo=bar";
        new SemanticChecker().check(new Parser(new Lexer().lex(input), input).parse());

        final String input2 = "foo = bar | baz = bat";
        new SemanticChecker().check(new Parser(new Lexer().lex(input2), input2).parse());

        final String input3 = "((foo | bar) = baz | hello = world) & a=b";
        new SemanticChecker().check(new Parser(new Lexer().lex(input3), input3).parse());
    }

    @Test
    public void testFail1()
    {
        final String input = "!(foo = bar) = baz";
        this.expectedException.expect(CoreException.class);
        this.expectedException.expectMessage("semantic error: invalid nested equality operators");
        new SemanticChecker().check(new Parser(new Lexer().lex(input), input).parse());
    }

    @Test
    public void testFail2()
    {
        final String input = "foo = bar = baz = !(bat = cat = mat)";
        this.expectedException.expect(CoreException.class);
        this.expectedException.expectMessage("semantic error: invalid nested equality operators");
        new SemanticChecker().check(new Parser(new Lexer().lex(input), input).parse());
    }
}
