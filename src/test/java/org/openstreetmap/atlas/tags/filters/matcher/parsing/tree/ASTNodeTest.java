package org.openstreetmap.atlas.tags.filters.matcher.parsing.tree;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.tags.filters.matcher.parsing.Token;

/**
 * @author lcram
 */
public class ASTNodeTest
{
    @Test
    public void testPrint()
    {
        BinaryOperator.clearIdentifierCounter();
        UnaryOperator.clearIdentifierCounter();
        Operand.clearIdentifierCounter();

        final ASTNode foo = new LiteralOperand(new Token(Token.TokenType.LITERAL, "foo", 0));
        final ASTNode bar = new LiteralOperand(new Token(Token.TokenType.LITERAL, "bar", 0));
        final ASTNode mat = new LiteralOperand(new Token(Token.TokenType.LITERAL, "mat", 0));
        final ASTNode baz = new LiteralOperand(new Token(Token.TokenType.LITERAL, "baz", 0));
        final ASTNode bat = new RegexOperand(new Token(Token.TokenType.LITERAL, "bat.*", 0));
        final ASTNode hat = new LiteralOperand(new Token(Token.TokenType.LITERAL, "hat", 0));

        final ASTNode not = new BangOperator(bar);
        final ASTNode equals = new EqualsOperator(foo, not, false);
        final ASTNode or1 = new OrOperator(baz, bat);
        final ASTNode bangEquals = new EqualsOperator(mat, or1, true);
        final ASTNode and = new AndOperator(equals, bangEquals);
        final ASTNode xor = new XorOperator(and, hat);

        final String expected = "XOR_4\n" + "XOR_4 left: AND_3\n" + "XOR_4 right: hat_5\n"
                + "AND_3\n" + "AND_3 left: EQ_0\n" + "AND_3 right: BANGEQ_2\n" + "EQ_0\n"
                + "EQ_0 left: foo_0\n" + "EQ_0 right: BANG_0\n" + "foo_0\n" + "BANG_0\n"
                + "BANG_0 child: bar_1\n" + "bar_1\n" + "BANGEQ_2\n" + "BANGEQ_2 left: mat_2\n"
                + "BANGEQ_2 right: OR_1\n" + "mat_2\n" + "OR_1\n" + "OR_1 left: baz_3\n"
                + "OR_1 right: bat.*_4\n" + "baz_3\n" + "bat.*_4\n" + "hat_5\n";
        Assert.assertEquals(expected, xor.debugPrintTree());
    }
}
