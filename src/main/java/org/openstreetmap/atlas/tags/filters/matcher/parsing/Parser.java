package org.openstreetmap.atlas.tags.filters.matcher.parsing;

import java.util.List;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.tags.Taggable;
import org.openstreetmap.atlas.tags.filters.matcher.TaggableMatcher;
import org.openstreetmap.atlas.tags.filters.matcher.parsing.tree.ASTNode;
import org.openstreetmap.atlas.tags.filters.matcher.parsing.tree.AndOperator;
import org.openstreetmap.atlas.tags.filters.matcher.parsing.tree.BangEqualsOperator;
import org.openstreetmap.atlas.tags.filters.matcher.parsing.tree.BangOperator;
import org.openstreetmap.atlas.tags.filters.matcher.parsing.tree.BinaryOperator;
import org.openstreetmap.atlas.tags.filters.matcher.parsing.tree.EqualsOperator;
import org.openstreetmap.atlas.tags.filters.matcher.parsing.tree.LiteralOperand;
import org.openstreetmap.atlas.tags.filters.matcher.parsing.tree.Operand;
import org.openstreetmap.atlas.tags.filters.matcher.parsing.tree.OrOperator;
import org.openstreetmap.atlas.tags.filters.matcher.parsing.tree.RegexOperand;
import org.openstreetmap.atlas.tags.filters.matcher.parsing.tree.UnaryOperator;
import org.openstreetmap.atlas.tags.filters.matcher.parsing.tree.XorOperator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class can transform a sequence of {@link Token}s into a syntactically valid abstract syntax
 * tree (AST) that can then be checked by the {@link SemanticChecker}. Finally, a
 * {@link TaggableMatcher} may walk this AST to determine if a {@link Taggable}'s tag map
 * corresponds to the matcher. This {@link Parser} implements an LL(1) {@link TaggableMatcher}
 * expression grammar using recursive descent. The grammar can be found below in comment form.
 *
 * @author lcram
 */
public class Parser
{
    /*
     * The Grammar. Operator precedence is handled in the standard way. '=' and '!=' are treated as
     * extremely "sticky" (i.e. high precedence) operators. The grammar is not capable of detecting
     * "nested" '=' and '!=' operators (e.g. foo=(bar=baz)), which are syntactically valid but
     * semantically invalid. Syntax trees containing nested equality operators must be dealt with at
     * a later stage.
     */

    // OR -> XOR OR'
    // OR' -> | XOR OR'
    // OR' -> ''
    // XOR -> AND XOR'
    // XOR' -> | AND XOR'
    // XOR' -> ''
    // AND -> EQ AND'
    // AND' -> & EQ AND'
    // AND' -> ''
    // EQ -> VALUE EQ'
    // EQ' -> = VALUE EQ'
    // EQ' -> != VALUE EQ'
    // EQ' -> ''
    // VALUE -> ( OR )
    // VALUE -> ! VALUE
    // VALUE -> literal
    // VALUE -> /regex/

    /**
     * @author lcram
     */
    private static class TokenBuffer
    {
        private final List<Token> tokens;
        private final String inputLine;
        private int position;

        TokenBuffer(final List<Token> tokens, final String inputLine)
        {
            this.tokens = tokens;
            this.inputLine = inputLine;
            this.position = 0;
        }

        void nextToken()
        {
            if (this.position < this.tokens.size())
            {
                this.position++;
            }
        }

        Token peek()
        {
            if (this.position >= this.tokens.size())
            {
                return new Token(Token.TokenType.EOF, null, this.inputLine.length());
            }
            return this.tokens.get(this.position);
        }
    }

    private static final Logger logger = LoggerFactory.getLogger(Parser.class);

    private final TokenBuffer tokenBuffer;
    private final String inputLine;

    public Parser(final List<Token> tokens, final String inputLine)
    {
        this.tokenBuffer = new TokenBuffer(tokens, inputLine);
        this.inputLine = inputLine;
    }

    public ASTNode parse()
    {
        BinaryOperator.clearIdCounter();
        UnaryOperator.clearIdCounter();
        Operand.clearIdCounter();
        return or();
    }

    private void accept(final Token.TokenType tokenType)
    {
        if (this.tokenBuffer.peek().getType() == tokenType)
        {
            logger.debug("ACCEPT: accepted {}({})", this.tokenBuffer.peek().getType(),
                    this.tokenBuffer.peek().getLexeme());
            this.tokenBuffer.nextToken();
        }
        else
        {
            throwSyntaxError(tokenType, this.tokenBuffer.peek(), this.inputLine);
        }
    }

    // AND -> EQ AND'
    private ASTNode and()
    {
        ASTNode node = null;

        logger.debug("AND: peek: {}({})", this.tokenBuffer.peek().getType(),
                this.tokenBuffer.peek().getLexeme());
        if (this.tokenBuffer.peek().getType() == Token.TokenType.BANG
                || this.tokenBuffer.peek().getType() == Token.TokenType.LITERAL
                || this.tokenBuffer.peek().getType() == Token.TokenType.REGEX
                || this.tokenBuffer.peek().getType() == Token.TokenType.PAREN_OPEN)
        {
            node = eq();
            final ASTNode rightResult = andPrime();
            if (rightResult != null)
            {
                node = new AndOperator(node, rightResult);
            }
        }
        else
        {
            throwSyntaxError(null, this.tokenBuffer.peek(), this.inputLine);
        }

        return node;
    }

    // AND' -> & EQ AND'
    // AND' -> ''
    private ASTNode andPrime()
    {
        ASTNode node;

        logger.debug("AND_PRIME: peek: {}({})", this.tokenBuffer.peek().getType(),
                this.tokenBuffer.peek().getLexeme());
        if (this.tokenBuffer.peek().getType() == Token.TokenType.AND)
        {
            logger.debug("AND_PRIME: try accepting: {}", Token.TokenType.AND);
            accept(Token.TokenType.AND);
            node = eq();
            final ASTNode rightResult = andPrime();
            if (rightResult != null)
            {
                node = new AndOperator(node, rightResult);
            }
        }
        else
        {
            // epsilon transition
            logger.debug("AND_PRIME: taking epsilon");
            return null;
        }

        return node;
    }

    // EQ -> VALUE EQ'
    private ASTNode eq()
    {
        ASTNode node = null;

        logger.debug("EQ: peek: {}({})", this.tokenBuffer.peek().getType(),
                this.tokenBuffer.peek().getLexeme());
        if (this.tokenBuffer.peek().getType() == Token.TokenType.BANG
                || this.tokenBuffer.peek().getType() == Token.TokenType.LITERAL
                || this.tokenBuffer.peek().getType() == Token.TokenType.REGEX
                || this.tokenBuffer.peek().getType() == Token.TokenType.PAREN_OPEN)
        {
            node = value();
            if (this.tokenBuffer.peek().getType() == Token.TokenType.EQUAL)
            {
                final ASTNode rightResult = eqPrime();
                if (rightResult != null)
                {
                    node = new EqualsOperator(node, rightResult);
                }
            }
            else if (this.tokenBuffer.peek().getType() == Token.TokenType.BANG_EQUAL)
            {
                final ASTNode rightResult = eqPrime();
                if (rightResult != null)
                {
                    node = new BangEqualsOperator(node, rightResult);
                }
            }
        }
        else
        {
            throwSyntaxError(null, this.tokenBuffer.peek(), this.inputLine);
        }

        return node;
    }

    // EQ' -> = VALUE EQ'
    // EQ' -> != VALUE EQ'
    // EQ' -> ''
    private ASTNode eqPrime()
    {
        ASTNode node;

        logger.debug("EQ_PRIME: peek: {}({})", this.tokenBuffer.peek().getType(),
                this.tokenBuffer.peek().getLexeme());
        if (this.tokenBuffer.peek().getType() == Token.TokenType.EQUAL)
        {
            logger.debug("EQ_PRIME: try accepting: {}", Token.TokenType.EQUAL);
            accept(Token.TokenType.EQUAL);
            node = value();
            if (this.tokenBuffer.peek().getType() == Token.TokenType.EQUAL)
            {
                final ASTNode rightResult = eqPrime();
                if (rightResult != null)
                {
                    node = new EqualsOperator(node, rightResult);
                }
            }
            else if (this.tokenBuffer.peek().getType() == Token.TokenType.BANG_EQUAL)
            {
                final ASTNode rightResult = eqPrime();
                if (rightResult != null)
                {
                    node = new BangEqualsOperator(node, rightResult);
                }
            }
        }
        else if (this.tokenBuffer.peek().getType() == Token.TokenType.BANG_EQUAL)
        {
            logger.debug("EQ_PRIME: try accepting: {}", Token.TokenType.BANG_EQUAL);
            accept(Token.TokenType.BANG_EQUAL);
            node = value();
            if (this.tokenBuffer.peek().getType() == Token.TokenType.EQUAL)
            {
                final ASTNode rightResult = eqPrime();
                if (rightResult != null)
                {
                    node = new EqualsOperator(node, rightResult);
                }
            }
            else if (this.tokenBuffer.peek().getType() == Token.TokenType.BANG_EQUAL)
            {
                final ASTNode rightResult = eqPrime();
                if (rightResult != null)
                {
                    node = new BangEqualsOperator(node, rightResult);
                }
            }
        }
        else
        {
            // epsilon transition
            logger.error("EQ_PRIME: taking epsilon");
            return null;
        }

        return node;
    }

    // OR -> XOR OR'
    private ASTNode or()
    {
        ASTNode node = null;

        logger.debug("OR: peek: {}({})", this.tokenBuffer.peek().getType(),
                this.tokenBuffer.peek().getLexeme());
        if (this.tokenBuffer.peek().getType() == Token.TokenType.BANG
                || this.tokenBuffer.peek().getType() == Token.TokenType.LITERAL
                || this.tokenBuffer.peek().getType() == Token.TokenType.REGEX
                || this.tokenBuffer.peek().getType() == Token.TokenType.PAREN_OPEN)
        {
            node = xor();
            final ASTNode rightResult = orPrime();
            if (rightResult != null)
            {
                node = new OrOperator(node, rightResult);
            }
        }
        else
        {
            throwSyntaxError(null, this.tokenBuffer.peek(), this.inputLine);
        }

        return node;
    }

    // OR' -> | XOR OR'
    // OR' -> ''
    private ASTNode orPrime()
    {
        ASTNode node = null;

        logger.debug("OR_PRIME: peek: {}({})", this.tokenBuffer.peek().getType(),
                this.tokenBuffer.peek().getLexeme());
        if (this.tokenBuffer.peek().getType() == Token.TokenType.OR)
        {
            logger.debug("OR_PRIME: try accepting: {}", Token.TokenType.OR);
            accept(Token.TokenType.OR);
            node = xor();
            final ASTNode rightResult = orPrime();
            if (rightResult != null)
            {
                node = new OrOperator(node, rightResult);
            }
        }
        else if (this.tokenBuffer.peek().getType() == Token.TokenType.EOF)
        {
            // epsilon transition
            logger.debug("OR_PRIME: taking epsilon");
            return null;
        }
        else if (this.tokenBuffer.peek().getType() == Token.TokenType.PAREN_CLOSE)
        {
            // epsilon transition
            logger.debug("OR_PRIME: taking epsilon due to FOLLOW )");
            return null;
        }
        else
        {
            throwSyntaxError(null, this.tokenBuffer.peek(), this.inputLine);
        }

        return node;
    }

    private void throwSyntaxError(final Token.TokenType expectedTokenType, final Token currentToken,
            final String inputLine)
    {
        final String arrow = "~".repeat(Math.max(0, currentToken.getIndexInLine())) + "^";
        if (expectedTokenType == null)
        {
            throw new CoreException("syntax error: unexpected token {}({})\n{}\n{}",
                    currentToken.getType(), currentToken.getLexeme(), inputLine, arrow);
        }
        throw new CoreException("syntax error: expected {}, but saw {}({})\n{}\n{}",
                expectedTokenType, currentToken.getType(), currentToken.getLexeme(), inputLine,
                arrow);
    }

    // VALUE -> ( EXP )
    // VALUE -> ! VALUE
    // VALUE -> literal
    // VALUE -> /regex/
    private ASTNode value()
    {
        ASTNode node = null;

        logger.debug("VALUE: peek: {}({})", this.tokenBuffer.peek().getType(),
                this.tokenBuffer.peek().getLexeme());
        if (this.tokenBuffer.peek().getType() == Token.TokenType.PAREN_OPEN)
        {
            logger.debug("VALUE: try accepting: {}", Token.TokenType.PAREN_OPEN);
            accept(Token.TokenType.PAREN_OPEN);
            node = or();
            logger.debug("VALUE: try accepting: {}", Token.TokenType.PAREN_CLOSE);
            accept(Token.TokenType.PAREN_CLOSE);
        }
        else if (this.tokenBuffer.peek().getType() == Token.TokenType.BANG)
        {
            logger.debug("VALUE: try accepting: {}", Token.TokenType.BANG);
            // accept the BANG first, and then parse the remaining token buffer
            accept(Token.TokenType.BANG);
            node = new BangOperator(value());
        }
        else if (this.tokenBuffer.peek().getType() == Token.TokenType.LITERAL)
        {
            logger.debug("VALUE: try accepting: {}", Token.TokenType.LITERAL);
            // Create the AST node first, since accepting will advance the token buffer
            node = new LiteralOperand(this.tokenBuffer.peek());
            accept(Token.TokenType.LITERAL);
        }
        else if (this.tokenBuffer.peek().getType() == Token.TokenType.REGEX)
        {
            logger.debug("VALUE: try accepting: {}", Token.TokenType.REGEX);
            // Create the AST node first, since accepting will advance the token buffer
            node = new RegexOperand(this.tokenBuffer.peek());
            accept(Token.TokenType.REGEX);
        }
        else
        {
            throwSyntaxError(null, this.tokenBuffer.peek(), this.inputLine);
        }

        return node;
    }

    // XOR -> AND XOR'
    private ASTNode xor()
    {
        ASTNode node = null;

        logger.debug("XOR: peek: {}({})", this.tokenBuffer.peek().getType(),
                this.tokenBuffer.peek().getLexeme());
        if (this.tokenBuffer.peek().getType() == Token.TokenType.BANG
                || this.tokenBuffer.peek().getType() == Token.TokenType.LITERAL
                || this.tokenBuffer.peek().getType() == Token.TokenType.REGEX
                || this.tokenBuffer.peek().getType() == Token.TokenType.PAREN_OPEN)
        {
            node = and();
            final ASTNode rightResult = xorPrime();
            if (rightResult != null)
            {
                node = new XorOperator(node, rightResult);
            }
        }
        else
        {
            throwSyntaxError(null, this.tokenBuffer.peek(), this.inputLine);
        }

        return node;
    }

    // XOR' -> | AND XOR'
    // XOR' -> ''
    private ASTNode xorPrime()
    {
        ASTNode node;

        if (this.tokenBuffer.peek().getType() == Token.TokenType.XOR)
        {
            logger.debug("XOR_PRIME: try accepting: {}", Token.TokenType.XOR);
            accept(Token.TokenType.XOR);
            node = and();
            final ASTNode rightResult = xorPrime();
            if (rightResult != null)
            {
                node = new XorOperator(node, rightResult);
            }
        }
        else
        {
            // epsilon transition
            logger.debug("XOR_PRIME: taking epsilon");
            return null;
        }

        return node;
    }
}
