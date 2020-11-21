package org.openstreetmap.atlas.tags.filters.matcher.parsing;

import java.util.Objects;

/**
 * @author lcram
 */
public class Token
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

        WHITESPACE(null);

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

    private final TokenType type;
    private final String lexeme;
    private final int indexInLine;

    public Token(final TokenType type, final String lexeme, final int indexInLine)
    {
        this.type = type;
        this.lexeme = lexeme;
        this.indexInLine = indexInLine - (lexeme != null ? lexeme.length() : 0);
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
