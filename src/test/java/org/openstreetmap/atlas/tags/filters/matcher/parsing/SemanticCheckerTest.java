package org.openstreetmap.atlas.tags.filters.matcher.parsing;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.tags.filters.matcher.parsing.tree.ASTNode;
import org.openstreetmap.atlas.tags.filters.matcher.parsing.tree.EqualsOperator;
import org.openstreetmap.atlas.tags.filters.matcher.parsing.tree.LiteralOperand;

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
        final ASTNode foo = new LiteralOperand(new Token(Token.TokenType.LITERAL, "foo", 0));
        final ASTNode bar = new LiteralOperand(new Token(Token.TokenType.LITERAL, "bar", 0));
        final ASTNode equals = new EqualsOperator(foo, bar);

        // this should not fail
        new SemanticChecker().check(equals);
    }

    @Test
    public void testFail()
    {
        final ASTNode foo = new LiteralOperand(new Token(Token.TokenType.LITERAL, "foo", 0));
        final ASTNode bar = new LiteralOperand(new Token(Token.TokenType.LITERAL, "bar", 0));
        final ASTNode baz = new LiteralOperand(new Token(Token.TokenType.LITERAL, "baz", 0));
        final ASTNode equals1 = new EqualsOperator(foo, bar);
        final ASTNode equals2 = new EqualsOperator(equals1, baz);

        this.expectedException.expect(CoreException.class);
        this.expectedException.expectMessage("semantic error: detected nested equality operators");
        new SemanticChecker().check(equals2);
    }
}
