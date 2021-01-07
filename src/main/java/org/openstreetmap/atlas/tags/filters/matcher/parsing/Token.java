package org.openstreetmap.atlas.tags.filters.matcher.parsing;

import java.io.Serializable;
import java.util.Objects;

/**
 * @author lcram
 */
public class Token implements Serializable
{
    /**
     * @author lcram
     */
    public enum TokenType
    {
        AND("&"),

        BANG("!"),

        BANG_EQUAL("!="),

        DOUBLE_QUOTE("\""),

        ESCAPE("\\"),

        EOF(null),

        EQUAL("="),

        LITERAL(null),

        OR("|"),

        PAREN_OPEN("("),

        PAREN_CLOSE(")"),

        REGEX("/"),

        SINGLE_QUOTE("'"),

        WHITESPACE(null),

        XOR("^");

        private final String literalValue;

        TokenType(final String literalValue)
        {
            this.literalValue = literalValue;
        }

        public String getLiteralValue()
        {
            return this.literalValue;
        }
    }

    private static final long serialVersionUID = -8498419139066512731L;

    private final TokenType type;
    private final String lexeme;
    private final int indexInLine;

    public Token(final TokenType type, final String lexeme, final int indexInLine)
    {
        this.lexeme = lexeme;
        if (type == TokenType.DOUBLE_QUOTE || type == TokenType.SINGLE_QUOTE)
        {
            /*
             * Override DOUBLE_QUOTE/SINGLE_QUOTE with regular LITERAL, since after lexing no other
             * component cares about this distinction. Using LITERAL everywhere will simplify
             * following code.
             */
            this.type = TokenType.LITERAL;
            /*
             * We need to add 2 back to the lexeme length to account for the "/' characters we
             * removed.
             */
            final int addBack = 2;
            this.indexInLine = indexInLine - (lexeme != null ? lexeme.length() + addBack : 0);
        }
        else
        {
            this.type = type;
            this.indexInLine = indexInLine - (lexeme != null ? lexeme.length() : 0);
        }
    }

    @Override
    public boolean equals(final Object other)
    {
        if (this == other)
        {
            return true;
        }
        if (other == null || getClass() != other.getClass())
        {
            return false;
        }
        final Token token = (Token) other;
        return this.type == token.type && Objects.equals(this.getLexeme(), token.getLexeme())
                && this.indexInLine == token.indexInLine;
    }

    public int getIndexInLine()
    {
        return this.indexInLine;
    }

    public String getLexeme()
    {
        return this.lexeme;
    }

    public TokenType getType()
    {
        return this.type;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(this.type, this.getLexeme(), this.indexInLine);
    }

    @Override
    public String toString()
    {
        return "(" + this.type + ", " + this.lexeme + ")";
    }
}
