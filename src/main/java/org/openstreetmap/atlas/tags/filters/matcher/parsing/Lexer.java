package org.openstreetmap.atlas.tags.filters.matcher.parsing;

import java.util.ArrayList;
import java.util.List;

import org.openstreetmap.atlas.exception.CoreException;

/**
 * @author lcram
 */
public class Lexer
{
    /**
     * @author lcram
     */
    private static class InputBuffer
    {
        static final int EOF = -1;

        String string;
        int position;

        InputBuffer(final String string)
        {
            this.string = string;
            this.position = 0;
        }

        int consumeCharacter()
        {
            if (this.position >= this.string.length())
            {
                return EOF;
            }
            return this.string.charAt(this.position++);
        }

        int peek()
        {
            if (this.position >= this.string.length())
            {
                return EOF;
            }
            return this.string.charAt(this.position);
        }

        void unconsume()
        {
            if (this.position > 0)
            {
                this.position--;
            }
        }
    }

    /**
     * @author lcram
     */
    private static class LexemeBuffer
    {
        List<Character> characters;

        LexemeBuffer()
        {
            this.characters = new ArrayList<>();
        }

        @Override
        public String toString()
        {
            final StringBuilder builder = new StringBuilder();
            for (final Character ch : this.characters)
            {
                builder.append(ch);
            }
            return builder.toString();
        }

        void addCharacter(final char ch)
        {
            this.characters.add(ch);
        }

        void clear()
        {
            this.characters.clear();
        }

        LexemeBuffer stripLeading()
        {
            this.characters.remove(0);
            return this;
        }

        LexemeBuffer stripTrailing()
        {
            this.characters.remove(this.characters.size() - 1);
            return this;
        }
    }

    private final LexemeBuffer lexemeBuffer;
    private final List<Token> lexedTokens;

    public Lexer()
    {
        this.lexemeBuffer = new LexemeBuffer();
        this.lexedTokens = new ArrayList<>();
    }

    public String debugString()
    {
        final StringBuilder builder = new StringBuilder();
        for (final Token token : this.lexedTokens)
        {
            builder.append(token.toString() + ", ");
        }
        return builder.toString();
    }

    public List<Token> getLexedTokens()
    {
        return this.lexedTokens;
    }

    /**
     * Lex a given input line.
     *
     * @param inputLine
     *            the input line
     */
    public void lex(final String inputLine)
    {
        final InputBuffer inputBuffer = new InputBuffer(inputLine);
        while (inputBuffer.peek() != InputBuffer.EOF)
        {
            if (isKeyValueCharacter(inputBuffer.peek()))
            {
                literal(inputBuffer, this.lexemeBuffer);
            }
            else if (isWhitespaceCharacter(inputBuffer.peek()))
            {
                whitespace(inputBuffer, this.lexemeBuffer);
            }
            else if (inputBuffer.peek() == Token.TokenType.EQUAL.getLiteralValue().charAt(0))
            {
                equal(inputBuffer, this.lexemeBuffer);
            }
            else if (inputBuffer.peek() == Token.TokenType.AND.getLiteralValue().charAt(0))
            {
                and(inputBuffer, this.lexemeBuffer);
            }
            else if (inputBuffer.peek() == Token.TokenType.OR.getLiteralValue().charAt(0))
            {
                or(inputBuffer, this.lexemeBuffer);
            }
            else if (inputBuffer.peek() == Token.TokenType.PAREN_OPEN.getLiteralValue().charAt(0))
            {
                parenOpen(inputBuffer, this.lexemeBuffer);
            }
            else if (inputBuffer.peek() == Token.TokenType.PAREN_CLOSE.getLiteralValue().charAt(0))
            {
                parenClose(inputBuffer, this.lexemeBuffer);
            }
            else if (inputBuffer.peek() == Token.TokenType.ESCAPE.getLiteralValue().charAt(0))
            {
                escape(inputBuffer, this.lexemeBuffer);
            }
            else if (inputBuffer.peek() == Token.TokenType.BANG.getLiteralValue().charAt(0))
            {
                bangOrBangEqual(inputBuffer, this.lexemeBuffer);
            }
            else if (inputBuffer.peek() == Token.TokenType.REGEX.getLiteralValue().charAt(0))
            {
                regex(inputBuffer, this.lexemeBuffer);
            }
            else if (inputBuffer.peek() == Token.TokenType.DOUBLE_QUOTE.getLiteralValue().charAt(0))
            {
                doubleQuote(inputBuffer, this.lexemeBuffer);
            }
            else
            {
                throw new CoreException("unknown char {}", (char) inputBuffer.peek());
            }

            this.lexemeBuffer.clear();
        }
    }

    private void and(final InputBuffer inputBuffer, final LexemeBuffer lexemeBuffer)
    {
        lexemeBuffer.addCharacter((char) inputBuffer.consumeCharacter());
        this.lexedTokens.add(new Token(Token.TokenType.AND, lexemeBuffer.toString()));
    }

    private void bangOrBangEqual(final InputBuffer inputBuffer, final LexemeBuffer lexemeBuffer)
    {
        lexemeBuffer.addCharacter((char) inputBuffer.consumeCharacter());
        if (inputBuffer.peek() == Token.TokenType.EQUAL.getLiteralValue().charAt(0))
        {
            lexemeBuffer.addCharacter((char) inputBuffer.consumeCharacter());
            this.lexedTokens.add(new Token(Token.TokenType.BANG_EQUAL, lexemeBuffer.toString()));
        }
        else
        {
            this.lexedTokens.add(new Token(Token.TokenType.BANG, lexemeBuffer.toString()));
        }
    }

    private void doubleQuote(final InputBuffer inputBuffer, final LexemeBuffer lexemeBuffer)
    {
        int ch;
        do
        {
            ch = inputBuffer.consumeCharacter();
            if (ch == InputBuffer.EOF)
            {
                throw new CoreException("Unexpected EOF after '\"' while lexing TaggableMatcher");
            }
            if (ch == Token.TokenType.ESCAPE.getLiteralValue().charAt(0))
            {
                final int escaped = inputBuffer.consumeCharacter();
                lexemeBuffer.addCharacter((char) escaped);
            }
            else
            {
                lexemeBuffer.addCharacter((char) ch);
            }
        }
        while (inputBuffer.peek() != Token.TokenType.DOUBLE_QUOTE.getLiteralValue().charAt(0));
        // consume the trailing '"'
        inputBuffer.consumeCharacter();

        // Strip leading " character
        final String lexeme = lexemeBuffer.stripLeading().toString();
        this.lexedTokens.add(new Token(Token.TokenType.DOUBLE_QUOTE, lexeme));
    }

    private void equal(final InputBuffer inputBuffer, final LexemeBuffer lexemeBuffer) // NOSONAR
    {
        lexemeBuffer.addCharacter((char) inputBuffer.consumeCharacter());
        this.lexedTokens.add(new Token(Token.TokenType.EQUAL, lexemeBuffer.toString()));
    }

    private void escape(final InputBuffer inputBuffer, final LexemeBuffer lexemeBuffer)
    {
        // Consume two characters, the '\' and the following character
        lexemeBuffer.addCharacter((char) inputBuffer.consumeCharacter());
        if (inputBuffer.peek() == InputBuffer.EOF)
        {
            throw new CoreException("Unexpected EOF after '\\' while lexing TaggableMatcher");
        }
        lexemeBuffer.addCharacter((char) inputBuffer.consumeCharacter());

        // Strip leading \ character
        final String lexeme = lexemeBuffer.stripLeading().toString();
        this.lexedTokens.add(new Token(Token.TokenType.ESCAPE, lexeme));
    }

    private boolean isKeyValueCharacter(final int ch)
    {
        // All these special chars do not count as key/value chars when they appear literally
        return ((char) ch) != Token.TokenType.AND.getLiteralValue().charAt(0)
                && ((char) ch) != Token.TokenType.OR.getLiteralValue().charAt(0)
                && ((char) ch) != Token.TokenType.EQUAL.getLiteralValue().charAt(0)
                && ((char) ch) != Token.TokenType.PAREN_OPEN.getLiteralValue().charAt(0)
                && ((char) ch) != Token.TokenType.PAREN_CLOSE.getLiteralValue().charAt(0)
                && ((char) ch) != Token.TokenType.REGEX.getLiteralValue().charAt(0)
                && ((char) ch) != Token.TokenType.ESCAPE.getLiteralValue().charAt(0)
                && ((char) ch) != Token.TokenType.BANG.getLiteralValue().charAt(0)
                && ((char) ch) != Token.TokenType.DOUBLE_QUOTE.getLiteralValue().charAt(0)
                && !isWhitespaceCharacter((char) ch);
    }

    private boolean isWhitespaceCharacter(final int ch)
    {
        return Character.isWhitespace((char) ch);
    }

    private void literal(final InputBuffer inputBuffer, final LexemeBuffer lexemeBuffer)
    {
        int ch;
        do
        {
            ch = inputBuffer.consumeCharacter();
            if (ch == InputBuffer.EOF)
            {
                break;
            }
            lexemeBuffer.addCharacter((char) ch);
        }
        while (isKeyValueCharacter(ch));
        if (ch != InputBuffer.EOF)
        {
            inputBuffer.unconsume();
            lexemeBuffer.stripTrailing();
        }

        this.lexedTokens.add(new Token(Token.TokenType.LITERAL, lexemeBuffer.toString()));
    }

    private void or(final InputBuffer inputBuffer, final LexemeBuffer lexemeBuffer)
    {
        lexemeBuffer.addCharacter((char) inputBuffer.consumeCharacter());
        this.lexedTokens.add(new Token(Token.TokenType.OR, lexemeBuffer.toString()));
    }

    private void parenClose(final InputBuffer inputBuffer, final LexemeBuffer lexemeBuffer)
    {
        lexemeBuffer.addCharacter((char) inputBuffer.consumeCharacter());
        this.lexedTokens.add(new Token(Token.TokenType.PAREN_CLOSE, lexemeBuffer.toString()));
    }

    private void parenOpen(final InputBuffer inputBuffer, final LexemeBuffer lexemeBuffer)
    {
        lexemeBuffer.addCharacter((char) inputBuffer.consumeCharacter());
        this.lexedTokens.add(new Token(Token.TokenType.PAREN_OPEN, lexemeBuffer.toString()));
    }

    private void regex(final InputBuffer inputBuffer, final LexemeBuffer lexemeBuffer)
    {
        int ch;
        do
        {
            ch = inputBuffer.consumeCharacter();
            if (ch == InputBuffer.EOF)
            {
                throw new CoreException("Unexpected EOF after '/' while lexing TaggableMatcher");
            }
            if (ch == Token.TokenType.ESCAPE.getLiteralValue().charAt(0))
            {
                /*
                 * If the user is specifically escaping a '/', we need to make sure that '\/' gets
                 * into the regex and the '/' will not be interpreted as an end to the regex.
                 */
                if (inputBuffer.peek() == Token.TokenType.REGEX.getLiteralValue().charAt(0))
                {
                    lexemeBuffer.addCharacter((char) ch);
                    final int escapedForwardSlash = inputBuffer.consumeCharacter();
                    lexemeBuffer.addCharacter((char) escapedForwardSlash);
                }
                // Otherwise, pass the \ forward into the regex normally
                else
                {
                    lexemeBuffer.addCharacter((char) ch);
                }
            }
            else
            {
                lexemeBuffer.addCharacter((char) ch);
            }
        }
        while (inputBuffer.peek() != Token.TokenType.REGEX.getLiteralValue().charAt(0));
        // consume the trailing '/'
        inputBuffer.consumeCharacter();

        // Strip leading and trailing / characters
        final String lexeme = lexemeBuffer.stripLeading().toString();
        this.lexedTokens.add(new Token(Token.TokenType.REGEX, lexeme));
    }

    private void whitespace(final InputBuffer inputBuffer, final LexemeBuffer lexemeBuffer)
    {
        lexemeBuffer.addCharacter((char) inputBuffer.consumeCharacter());
        this.lexedTokens.add(new Token(Token.TokenType.WHITESPACE, lexemeBuffer.toString()));
    }
}
