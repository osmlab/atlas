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

        ESCAPE("\\"),

        EOF(null),

        EQUAL("="),

        OR("|"),

        PAREN_OPEN("("),

        PAREN_CLOSE(")"),

        SLASH("/"),

        UNKNOWN(null),

        KEY_VALUE(null);

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

    public static final Token EPSILON_TOKEN = new Token(TokenType.EOF, null);

    private final TokenType type;
    private final String lexeme;

    public Token(final TokenType type, final String lexeme)
    {
        this.type = type;
        this.lexeme = lexeme;
    }

    public Token(final TokenType type)
    {
        this(type, null);
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
        return this.type == token.type && Objects.equals(this.getLexeme(), token.getLexeme());
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
        return Objects.hash(this.type, this.getLexeme());
    }

    @Override
    public String toString()
    {
        return "(" + this.type + ", " + this.lexeme + ")";
    }
}
