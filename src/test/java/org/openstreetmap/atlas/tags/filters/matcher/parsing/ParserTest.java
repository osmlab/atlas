package org.openstreetmap.atlas.tags.filters.matcher.parsing;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.openstreetmap.atlas.exception.CoreException;

/**
 * @author lcram
 */
public class ParserTest
{
    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Test
    public void testBasic()
    {
        final String input1 = "!name | name=/.*[s|S]treet/";
        final String expected1 = "OR_1\n" + "OR_1 left: BANG_0\n" + "OR_1 right: EQ_0\n"
                + "BANG_0\n" + "BANG_0 child: name_0\n" + "name_0\n" + "EQ_0\n"
                + "EQ_0 left: name_1\n" + "EQ_0 right: .*[s|S]treet_2\n" + "name_1\n"
                + ".*[s|S]treet_2\n";
        Assert.assertEquals(expected1,
                new Parser(new Lexer().lex(input1), input1).parse().printTree());

        final String input2 = "foo | bar | baz";
        final String expected2 = "OR_1\n" + "OR_1 left: foo_0\n" + "OR_1 right: OR_0\n" + "foo_0\n"
                + "OR_0\n" + "OR_0 left: bar_1\n" + "OR_0 right: baz_2\n" + "bar_1\n" + "baz_2\n";
        Assert.assertEquals(expected2,
                new Parser(new Lexer().lex(input2), input2).parse().printTree());

        final String input3 = "foo = bar = baz != bat";
        final String expected3 = "EQ_2\n" + "EQ_2 left: foo_0\n" + "EQ_2 right: EQ_1\n" + "foo_0\n"
                + "EQ_1\n" + "EQ_1 left: bar_1\n" + "EQ_1 right: BANGEQ_0\n" + "bar_1\n"
                + "BANGEQ_0\n" + "BANGEQ_0 left: baz_2\n" + "BANGEQ_0 right: bat_3\n" + "baz_2\n"
                + "bat_3\n";
        Assert.assertEquals(expected3,
                new Parser(new Lexer().lex(input3), input3).parse().printTree());

        final String input4 = "foo != bar = baz != bat != hello";
        final String expected4 = "BANGEQ_3\n" + "BANGEQ_3 left: foo_0\n" + "BANGEQ_3 right: EQ_2\n"
                + "foo_0\n" + "EQ_2\n" + "EQ_2 left: bar_1\n" + "EQ_2 right: BANGEQ_1\n" + "bar_1\n"
                + "BANGEQ_1\n" + "BANGEQ_1 left: baz_2\n" + "BANGEQ_1 right: BANGEQ_0\n" + "baz_2\n"
                + "BANGEQ_0\n" + "BANGEQ_0 left: bat_3\n" + "BANGEQ_0 right: hello_4\n" + "bat_3\n"
                + "hello_4\n";
        Assert.assertEquals(expected4,
                new Parser(new Lexer().lex(input4), input4).parse().printTree());

        final String input5 = "foo = bar";
        final String expected5 = "EQ_0\n" + "EQ_0 left: foo_0\n" + "EQ_0 right: bar_1\n" + "foo_0\n"
                + "bar_1\n";
        Assert.assertEquals(expected5,
                new Parser(new Lexer().lex(input5), input5).parse().printTree());

        final String input6 = "foo";
        final String expected6 = "foo_0\n";
        Assert.assertEquals(expected6,
                new Parser(new Lexer().lex(input6), input6).parse().printTree());

        final String input7 = "foo & bar & baz";
        final String expected7 = "AND_1\n" + "AND_1 left: foo_0\n" + "AND_1 right: AND_0\n"
                + "foo_0\n" + "AND_0\n" + "AND_0 left: bar_1\n" + "AND_0 right: baz_2\n" + "bar_1\n"
                + "baz_2\n";
        Assert.assertEquals(expected7,
                new Parser(new Lexer().lex(input7), input7).parse().printTree());
    }

    @Test
    public void testExceptionDoubleAnd()
    {
        final String input = "(foo = bar & baz = bat) && (hello = city | world)";

        this.expectedException.expect(CoreException.class);
        this.expectedException.expectMessage("syntax error: unexpected token AND(&)\n"
                + "(foo = bar & baz = bat) && (hello = city | world)\n"
                + "~~~~~~~~~~~~~~~~~~~~~~~~~^");
        new Parser(new Lexer().lex(input), input).parse().printTree();
    }

    @Test
    public void testExceptionDoubleEqual()
    {
        final String input = "foo == bar";

        this.expectedException.expect(CoreException.class);
        this.expectedException.expectMessage(
                "syntax error: unexpected token EQUAL(=)\n" + "foo == bar\n" + "~~~~~^");
        new Parser(new Lexer().lex(input), input).parse().printTree();
    }

    @Test
    public void testExceptionDoubleLiteral()
    {
        final String input = "foo baz = bar";

        this.expectedException.expect(CoreException.class);
        this.expectedException.expectMessage(
                "syntax error: unexpected token LITERAL(baz)\n" + "foo baz = bar\n" + "~~~~^");
        new Parser(new Lexer().lex(input), input).parse().printTree();
    }

    @Test
    public void testExceptionExpectedParenthesis()
    {
        final String input = "(foo = bar";

        this.expectedException.expect(CoreException.class);
        this.expectedException
                .expectMessage("syntax error: expected PAREN_CLOSE, but saw EOF(null)\n"
                        + "(foo = bar\n" + "~~~~~~~~~~^");
        new Parser(new Lexer().lex(input), input).parse().printTree();
    }

    @Test
    public void testExceptionLeadingOp()
    {
        final String input = "& foo = bar";

        this.expectedException.expect(CoreException.class);
        this.expectedException
                .expectMessage("syntax error: unexpected token AND(&)\n" + "& foo = bar\n" + "^");
        new Parser(new Lexer().lex(input), input).parse().printTree();
    }

    @Test
    public void testParentheticalChanges()
    {
        final String input1 = "foo = bar | (baz = bat & hello = world)";
        final String expected1 = "OR_4\n" + "OR_4 left: EQ_0\n" + "OR_4 right: AND_3\n" + "EQ_0\n"
                + "EQ_0 left: foo_0\n" + "EQ_0 right: bar_1\n" + "foo_0\n" + "bar_1\n" + "AND_3\n"
                + "AND_3 left: EQ_1\n" + "AND_3 right: EQ_2\n" + "EQ_1\n" + "EQ_1 left: baz_2\n"
                + "EQ_1 right: bat_3\n" + "baz_2\n" + "bat_3\n" + "EQ_2\n" + "EQ_2 left: hello_4\n"
                + "EQ_2 right: world_5\n" + "hello_4\n" + "world_5\n";
        Assert.assertEquals(expected1,
                new Parser(new Lexer().lex(input1), input1).parse().printTree());

        final String input2 = "(foo = bar | baz = bat) & hello = world";
        final String expected2 = "AND_4\n" + "AND_4 left: OR_2\n" + "AND_4 right: EQ_3\n" + "OR_2\n"
                + "OR_2 left: EQ_0\n" + "OR_2 right: EQ_1\n" + "EQ_0\n" + "EQ_0 left: foo_0\n"
                + "EQ_0 right: bar_1\n" + "foo_0\n" + "bar_1\n" + "EQ_1\n" + "EQ_1 left: baz_2\n"
                + "EQ_1 right: bat_3\n" + "baz_2\n" + "bat_3\n" + "EQ_3\n" + "EQ_3 left: hello_4\n"
                + "EQ_3 right: world_5\n" + "hello_4\n" + "world_5\n";
        Assert.assertEquals(expected2,
                new Parser(new Lexer().lex(input2), input2).parse().printTree());

        final String inputA = "foo = bar | (baz = bat & hello = world)";
        final String inputB = "foo = bar | baz = bat & hello = world";
        Assert.assertEquals(new Parser(new Lexer().lex(inputA), inputA).parse().printTree(),
                new Parser(new Lexer().lex(inputB), inputB).parse().printTree());
    }
}
