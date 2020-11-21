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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class can transform a sequence of {@link Token}s into a syntactically valid abstract syntax
 * tree (AST) that can then be checked by the {@link Checker}. Finally, a {@link TaggableMatcher}
 * may walk this AST to determine if a {@link Taggable}'s tag map corresponds to the matcher. This
 * {@link Parser} implements an LL(1) {@link TaggableMatcher} expression grammar using recursive
 * descent. The grammar can be found below in comment form.
 *
 * @author lcram
 */
public class Parser
{
    /*
     * The Grammar. Operator precedence is handled in the standard way. '=' and '!=' are treated as
     * extremely "sticky" (i.e. high precedence) operators.
     */
    // EXP -> TERM EXP'
    // EXP' -> | TERM EXP'
    // EXP' -> ''
    // TERM -> FACT TERM'
    // TERM' -> & FACT TERM'
    // TERM' -> ''
    // FACT -> VALUE FACT'
    // FACT' -> = VALUE FACT'
    // FACT' -> != VALUE FACT'
    // FACT' -> ''
    // VALUE -> ( EXP )
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
        return exp();
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

    private ASTNode exp()
    {
        ASTNode node = null;

        logger.debug("EXP: peek: {}({})", this.tokenBuffer.peek().getType(),
                this.tokenBuffer.peek().getLexeme());
        if (this.tokenBuffer.peek().getType() == Token.TokenType.BANG
                || this.tokenBuffer.peek().getType() == Token.TokenType.LITERAL
                || this.tokenBuffer.peek().getType() == Token.TokenType.REGEX
                || this.tokenBuffer.peek().getType() == Token.TokenType.PAREN_OPEN)
        {
            node = term();
            final ASTNode rightResult = expPrime();
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

    private ASTNode expPrime()
    {
        ASTNode node = null;

        logger.debug("EXP_PRIME: peek: {}({})", this.tokenBuffer.peek().getType(),
                this.tokenBuffer.peek().getLexeme());
        if (this.tokenBuffer.peek().getType() == Token.TokenType.OR)
        {
            logger.debug("EXP_PRIME: try accepting: {}", Token.TokenType.OR);
            accept(Token.TokenType.OR);
            node = term();
            final ASTNode rightResult = expPrime();
            if (rightResult != null)
            {
                node = new OrOperator(node, rightResult);
            }
        }
        else if (this.tokenBuffer.peek().getType() == Token.TokenType.EOF)
        {
            // epsilon transition
            logger.debug("EXP_PRIME: taking epsilon");
            return null;
        }
        else if (this.tokenBuffer.peek().getType() == Token.TokenType.PAREN_CLOSE)
        {
            // epsilon transition
            logger.debug("EXP_PRIME: taking epsilon due to FOLLOW )");
            return null;
        }
        else
        {
            throwSyntaxError(null, this.tokenBuffer.peek(), this.inputLine);
        }

        return node;
    }

    private ASTNode fact()
    {
        ASTNode node = null;

        logger.debug("FACT: peek: {}({})", this.tokenBuffer.peek().getType(),
                this.tokenBuffer.peek().getLexeme());
        if (this.tokenBuffer.peek().getType() == Token.TokenType.BANG
                || this.tokenBuffer.peek().getType() == Token.TokenType.LITERAL
                || this.tokenBuffer.peek().getType() == Token.TokenType.REGEX
                || this.tokenBuffer.peek().getType() == Token.TokenType.PAREN_OPEN)
        {
            node = value();
            if (this.tokenBuffer.peek().getType() == Token.TokenType.EQUAL)
            {
                final ASTNode rightResult = factPrime();
                if (rightResult != null)
                {
                    node = new EqualsOperator(node, rightResult);
                }
            }
            else if (this.tokenBuffer.peek().getType() == Token.TokenType.BANG_EQUAL)
            {
                final ASTNode rightResult = factPrime();
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

    private ASTNode factPrime()
    {
        ASTNode node;

        logger.debug("FACT_PRIME: peek: {}({})", this.tokenBuffer.peek().getType(),
                this.tokenBuffer.peek().getLexeme());
        if (this.tokenBuffer.peek().getType() == Token.TokenType.EQUAL)
        {
            logger.debug("FACT_PRIME: try accepting: {}", Token.TokenType.EQUAL);
            accept(Token.TokenType.EQUAL);
            node = value();
            if (this.tokenBuffer.peek().getType() == Token.TokenType.EQUAL)
            {
                final ASTNode rightResult = factPrime();
                if (rightResult != null)
                {
                    node = new EqualsOperator(node, rightResult);
                }
            }
            else if (this.tokenBuffer.peek().getType() == Token.TokenType.BANG_EQUAL)
            {
                final ASTNode rightResult = factPrime();
                if (rightResult != null)
                {
                    node = new BangEqualsOperator(node, rightResult);
                }
            }
        }
        else if (this.tokenBuffer.peek().getType() == Token.TokenType.BANG_EQUAL)
        {
            logger.debug("FACT_PRIME: try accepting: {}", Token.TokenType.BANG_EQUAL);
            accept(Token.TokenType.BANG_EQUAL);
            node = value();
            if (this.tokenBuffer.peek().getType() == Token.TokenType.EQUAL)
            {
                final ASTNode rightResult = factPrime();
                if (rightResult != null)
                {
                    node = new EqualsOperator(node, rightResult);
                }
            }
            else if (this.tokenBuffer.peek().getType() == Token.TokenType.BANG_EQUAL)
            {
                final ASTNode rightResult = factPrime();
                if (rightResult != null)
                {
                    node = new BangEqualsOperator(node, rightResult);
                }
            }
        }
        else
        {
            // epsilon transition
            logger.error("FACT_PRIME: taking epsilon");
            return null;
        }

        return node;
    }

    private ASTNode term()
    {
        ASTNode node = null;

        logger.debug("TERM: peek: {}({})", this.tokenBuffer.peek().getType(),
                this.tokenBuffer.peek().getLexeme());
        if (this.tokenBuffer.peek().getType() == Token.TokenType.BANG
                || this.tokenBuffer.peek().getType() == Token.TokenType.LITERAL
                || this.tokenBuffer.peek().getType() == Token.TokenType.REGEX
                || this.tokenBuffer.peek().getType() == Token.TokenType.PAREN_OPEN)
        {
            node = fact();
            final ASTNode rightResult = termPrime();
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

    private ASTNode termPrime()
    {
        ASTNode node;

        logger.debug("TERM_PRIME: peek: {}({})", this.tokenBuffer.peek().getType(),
                this.tokenBuffer.peek().getLexeme());
        if (this.tokenBuffer.peek().getType() == Token.TokenType.AND)
        {
            logger.debug("TERM_PRIME: try accepting: {}", Token.TokenType.AND);
            accept(Token.TokenType.AND);
            node = fact();
            final ASTNode rightResult = termPrime();
            if (rightResult != null)
            {
                node = new AndOperator(node, rightResult);
            }
        }
        else
        {
            // epsilon transition
            logger.debug("TERM_PRIME: taking epsilon");
            return null;
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

    private ASTNode value()
    {
        ASTNode node = null;

        logger.debug("VALUE: peek: {}({})", this.tokenBuffer.peek().getType(),
                this.tokenBuffer.peek().getLexeme());
        if (this.tokenBuffer.peek().getType() == Token.TokenType.PAREN_OPEN)
        {
            logger.debug("VALUE: try accepting: {}", Token.TokenType.PAREN_OPEN);
            accept(Token.TokenType.PAREN_OPEN);
            node = exp();
            logger.debug("VALUE: try accepting: {}", Token.TokenType.PAREN_CLOSE);
            accept(Token.TokenType.PAREN_CLOSE);
        }
        else if (this.tokenBuffer.peek().getType() == Token.TokenType.BANG)
        {
            logger.debug("VALUE: try accepting: {}", Token.TokenType.BANG);
            accept(Token.TokenType.BANG);
            node = new BangOperator(value());
        }
        else if (this.tokenBuffer.peek().getType() == Token.TokenType.LITERAL)
        {
            logger.debug("VALUE: try accepting: {}", Token.TokenType.LITERAL);
            node = new LiteralOperand(this.tokenBuffer.peek());
            accept(Token.TokenType.LITERAL);
        }
        else if (this.tokenBuffer.peek().getType() == Token.TokenType.REGEX)
        {
            logger.debug("VALUE: try accepting: {}", Token.TokenType.REGEX);
            node = new RegexOperand(this.tokenBuffer.peek());
            accept(Token.TokenType.REGEX);
        }
        else
        {
            throwSyntaxError(null, this.tokenBuffer.peek(), this.inputLine);
        }

        return node;
    }
}
