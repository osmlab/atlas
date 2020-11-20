package org.openstreetmap.atlas.tags.filters.matcher.parsing;

import java.util.List;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.tags.Taggable;
import org.openstreetmap.atlas.tags.filters.matcher.TaggableMatcher;
import org.openstreetmap.atlas.tags.filters.matcher.parsing.tree.ASTNode;
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
        private int position;

        TokenBuffer(final List<Token> tokens)
        {
            this.tokens = tokens;
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
                return Token.EOF_TOKEN;
            }
            return this.tokens.get(this.position);
        }
    }

    private static final Logger logger = LoggerFactory.getLogger(Parser.class);

    private final TokenBuffer tokenBuffer;

    public Parser(final List<Token> tokens)
    {
        this.tokenBuffer = new TokenBuffer(tokens);
    }

    public ASTNode parse()
    {
        return exp();
    }

    private void accept(final Token.TokenType tokenType)
    {
        if (this.tokenBuffer.peek().getType() == tokenType)
        {
            logger.error("ACCEPT: accepted {}({})", this.tokenBuffer.peek().getType(),
                    this.tokenBuffer.peek().getLexeme());
            this.tokenBuffer.nextToken();
        }
        else
        {
            throw new CoreException("ACCEPT: expected {}, saw {}({})", tokenType,
                    this.tokenBuffer.peek().getType(), this.tokenBuffer.peek().getLexeme());
        }
    }

    private ASTNode exp()
    {
        ASTNode node;

        logger.error("EXP: peek: {}({})", this.tokenBuffer.peek().getType(),
                this.tokenBuffer.peek().getLexeme());
        if (this.tokenBuffer.peek().getType() == Token.TokenType.BANG
                || this.tokenBuffer.peek().getType() == Token.TokenType.LITERAL
                || this.tokenBuffer.peek().getType() == Token.TokenType.REGEX
                || this.tokenBuffer.peek().getType() == Token.TokenType.PAREN_OPEN)
        {
            term();
            expPrime();
        }
        else
        {
            // TODO better error message
            throw new CoreException("EXP: unexpected token {}({})",
                    this.tokenBuffer.peek().getType(), this.tokenBuffer.peek().getLexeme());
        }

        return null;
    }

    private ASTNode expPrime()
    {
        ASTNode node;

        logger.error("EXP_PRIME: peek: {}({})", this.tokenBuffer.peek().getType(),
                this.tokenBuffer.peek().getLexeme());
        if (this.tokenBuffer.peek().getType() == Token.TokenType.OR)
        {
            logger.error("EXP_PRIME: try accepting: {}", Token.TokenType.OR);
            accept(Token.TokenType.OR);
            term();
            expPrime();
        }
        else if (this.tokenBuffer.peek().getType() == Token.TokenType.EOF)
        {
            // TODO what to do here?
            // epsilon transition
            logger.error("EXP_PRIME: taking epsilon");
        }
        else if (this.tokenBuffer.peek().getType() == Token.TokenType.PAREN_CLOSE)
        {
            // TODO what to do here?
            logger.error("EXP_PRIME: taking epsilon due to FOLLOW )");
        }
        else
        {
            // TODO better error message
            throw new CoreException("EXP_PRIME: unexpected token {}({})",
                    this.tokenBuffer.peek().getType(), this.tokenBuffer.peek().getLexeme());
        }

        return null;
    }

    private ASTNode fact()
    {
        ASTNode node;

        logger.error("FACT: peek: {}({})", this.tokenBuffer.peek().getType(),
                this.tokenBuffer.peek().getLexeme());
        if (this.tokenBuffer.peek().getType() == Token.TokenType.BANG
                || this.tokenBuffer.peek().getType() == Token.TokenType.LITERAL
                || this.tokenBuffer.peek().getType() == Token.TokenType.REGEX
                || this.tokenBuffer.peek().getType() == Token.TokenType.PAREN_OPEN)
        {
            value();
            factPrime();
        }
        else
        {
            // TODO better error message
            throw new CoreException("FACT: unexpected token {}({})",
                    this.tokenBuffer.peek().getType(), this.tokenBuffer.peek().getLexeme());
        }

        return null;
    }

    private ASTNode factPrime()
    {
        ASTNode node;

        logger.error("FACT_PRIME: peek: {}({})", this.tokenBuffer.peek().getType(),
                this.tokenBuffer.peek().getLexeme());
        if (this.tokenBuffer.peek().getType() == Token.TokenType.EQUAL)
        {
            logger.error("FACT_PRIME: try accepting: {}", Token.TokenType.EQUAL);
            accept(Token.TokenType.EQUAL);
            value();
            factPrime();
        }
        else if (this.tokenBuffer.peek().getType() == Token.TokenType.BANG_EQUAL)
        {
            logger.error("FACT_PRIME: try accepting: {}", Token.TokenType.BANG_EQUAL);
            accept(Token.TokenType.BANG_EQUAL);
            value();
            factPrime();
        }
        else
        {
            // TODO what to do here?
            // epsilon transition
            logger.error("FACT_PRIME: taking epsilon");
        }

        return null;
    }

    private ASTNode term()
    {
        ASTNode node;

        logger.error("TERM: peek: {}({})", this.tokenBuffer.peek().getType(),
                this.tokenBuffer.peek().getLexeme());
        if (this.tokenBuffer.peek().getType() == Token.TokenType.BANG
                || this.tokenBuffer.peek().getType() == Token.TokenType.LITERAL
                || this.tokenBuffer.peek().getType() == Token.TokenType.REGEX
                || this.tokenBuffer.peek().getType() == Token.TokenType.PAREN_OPEN)
        {
            fact();
            termPrime();
        }
        else
        {
            // TODO better error message
            throw new CoreException("TERM: unexpected token {}({})",
                    this.tokenBuffer.peek().getType(), this.tokenBuffer.peek().getLexeme());
        }

        return null;
    }

    private ASTNode termPrime()
    {
        ASTNode node;

        logger.error("TERM_PRIME: peek: {}({})", this.tokenBuffer.peek().getType(),
                this.tokenBuffer.peek().getLexeme());
        if (this.tokenBuffer.peek().getType() == Token.TokenType.AND)
        {
            logger.error("TERM_PRIME: try accepting: {}", Token.TokenType.AND);
            accept(Token.TokenType.AND);
            fact();
            termPrime();
        }
        else
        {
            // TODO what to do here?
            // epsilon transition
            logger.error("TERM_PRIME: taking epsilon");
        }

        return null;
    }

    private ASTNode value()
    {
        ASTNode node;

        logger.error("VALUE: peek: {}({})", this.tokenBuffer.peek().getType(),
                this.tokenBuffer.peek().getLexeme());
        if (this.tokenBuffer.peek().getType() == Token.TokenType.PAREN_OPEN)
        {
            logger.error("VALUE: try accepting: {}", Token.TokenType.PAREN_OPEN);
            accept(Token.TokenType.PAREN_OPEN);
            exp();
            logger.error("VALUE: try accepting: {}", Token.TokenType.PAREN_CLOSE);
            accept(Token.TokenType.PAREN_CLOSE);
        }
        else if (this.tokenBuffer.peek().getType() == Token.TokenType.BANG)
        {
            logger.error("VALUE: try accepting: {}", Token.TokenType.BANG);
            accept(Token.TokenType.BANG);
            value();
        }
        else if (this.tokenBuffer.peek().getType() == Token.TokenType.LITERAL)
        {
            logger.error("VALUE: try accepting: {}", Token.TokenType.LITERAL);
            accept(Token.TokenType.LITERAL);
        }
        else if (this.tokenBuffer.peek().getType() == Token.TokenType.REGEX)
        {
            logger.error("VALUE: try accepting: {}", Token.TokenType.REGEX);
            accept(Token.TokenType.REGEX);
        }
        else
        {
            // TODO better error message
            throw new CoreException("VALUE: unexpected token {}({})",
                    this.tokenBuffer.peek().getType(), this.tokenBuffer.peek().getLexeme());
        }

        return null;
    }
}
