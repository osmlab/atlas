package org.openstreetmap.atlas.tags.filters.matcher.parsing;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.openstreetmap.atlas.exception.CoreException;

/**
 * This class can transform an input {@link String} into a sequence of {@link Token}s recognizable
 * by the {@link Parser}.
 * 
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

    public static String debugString(final List<Token> lexedTokens)
    {
        final StringBuilder builder = new StringBuilder();
        for (final Token token : lexedTokens)
        {
            builder.append(token.toString() + ", ");
        }
        return builder.toString();
    }

    /**
     * Lex a given input line.
     *
     * @param inputLine
     *            the input line
     * @return a {@link List} of the processed {@link Token}s
     */
    public List<Token> lex(final String inputLine)
    {
        final List<Token> lexedTokens = new ArrayList<>();
        final LexemeBuffer lexemeBuffer = new LexemeBuffer();
        final InputBuffer inputBuffer = new InputBuffer(inputLine);
        while (inputBuffer.peek() != InputBuffer.EOF)
        {
            if (isKeyValueCharacter(inputBuffer.peek()))
            {
                literal(inputBuffer, lexemeBuffer, lexedTokens);
            }
            else if (isWhitespaceCharacter(inputBuffer.peek()))
            {
                whitespace(inputBuffer, lexemeBuffer, lexedTokens);
            }
            else if (inputBuffer.peek() == Token.TokenType.EQUAL.getLiteralValue().charAt(0))
            {
                equal(inputBuffer, lexemeBuffer, lexedTokens);
            }
            else if (inputBuffer.peek() == Token.TokenType.AND.getLiteralValue().charAt(0))
            {
                and(inputBuffer, lexemeBuffer, lexedTokens);
            }
            else if (inputBuffer.peek() == Token.TokenType.OR.getLiteralValue().charAt(0))
            {
                or(inputBuffer, lexemeBuffer, lexedTokens);
            }
            else if (inputBuffer.peek() == Token.TokenType.PAREN_OPEN.getLiteralValue().charAt(0))
            {
                parenOpen(inputBuffer, lexemeBuffer, lexedTokens);
            }
            else if (inputBuffer.peek() == Token.TokenType.PAREN_CLOSE.getLiteralValue().charAt(0))
            {
                parenClose(inputBuffer, lexemeBuffer, lexedTokens);
            }
            else if (inputBuffer.peek() == Token.TokenType.ESCAPE.getLiteralValue().charAt(0))
            {
                escape(inputBuffer, lexemeBuffer, lexedTokens);
            }
            else if (inputBuffer.peek() == Token.TokenType.BANG.getLiteralValue().charAt(0))
            {
                bangOrBangEqual(inputBuffer, lexemeBuffer, lexedTokens);
            }
            else if (inputBuffer.peek() == Token.TokenType.REGEX.getLiteralValue().charAt(0))
            {
                regex(inputBuffer, lexemeBuffer, lexedTokens);
            }
            else if (inputBuffer.peek() == Token.TokenType.DOUBLE_QUOTE.getLiteralValue().charAt(0))
            {
                doubleQuote(inputBuffer, lexemeBuffer, lexedTokens);
            }
            else
            {
                throw new CoreException("unknown char {}", (char) inputBuffer.peek());
            }

            lexemeBuffer.clear();
        }

        // Remove all whitespace from token stream
        return lexedTokens.stream().filter(token -> token.getType() != Token.TokenType.WHITESPACE)
                .collect(Collectors.toList());
    }

    private void and(final InputBuffer inputBuffer, final LexemeBuffer lexemeBuffer,
            final List<Token> lexedTokens)
    {
        lexemeBuffer.addCharacter((char) inputBuffer.consumeCharacter());
        lexedTokens
                .add(new Token(Token.TokenType.AND, lexemeBuffer.toString(), inputBuffer.position));
    }

    private void bangOrBangEqual(final InputBuffer inputBuffer, final LexemeBuffer lexemeBuffer,
            final List<Token> lexedTokens)
    {
        lexemeBuffer.addCharacter((char) inputBuffer.consumeCharacter());
        if (inputBuffer.peek() == Token.TokenType.EQUAL.getLiteralValue().charAt(0))
        {
            lexemeBuffer.addCharacter((char) inputBuffer.consumeCharacter());
            lexedTokens.add(new Token(Token.TokenType.BANG_EQUAL, lexemeBuffer.toString(),
                    inputBuffer.position));
        }
        else
        {
            lexedTokens.add(
                    new Token(Token.TokenType.BANG, lexemeBuffer.toString(), inputBuffer.position));
        }
    }

    private void doubleQuote(final InputBuffer inputBuffer, final LexemeBuffer lexemeBuffer,
            final List<Token> lexedTokens)
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
        // Don't bother saving as DOUBLE_QUOTE type, since we will change it later anyway
        lexedTokens.add(new Token(Token.TokenType.LITERAL, lexeme, inputBuffer.position));
    }

    private void equal(final InputBuffer inputBuffer, final LexemeBuffer lexemeBuffer, // NOSONAR
            final List<Token> lexedTokens)
    {
        lexemeBuffer.addCharacter((char) inputBuffer.consumeCharacter());
        lexedTokens.add(
                new Token(Token.TokenType.EQUAL, lexemeBuffer.toString(), inputBuffer.position));
    }

    private void escape(final InputBuffer inputBuffer, final LexemeBuffer lexemeBuffer,
            final List<Token> lexedTokens)
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
        // Don't bother saving as ESCAPE type, since we will change it later anyway
        lexedTokens.add(new Token(Token.TokenType.LITERAL, lexeme, inputBuffer.position));
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

    private void literal(final InputBuffer inputBuffer, final LexemeBuffer lexemeBuffer,
            final List<Token> lexedTokens)
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

        lexedTokens.add(
                new Token(Token.TokenType.LITERAL, lexemeBuffer.toString(), inputBuffer.position));
    }

    private void or(final InputBuffer inputBuffer, final LexemeBuffer lexemeBuffer,
            final List<Token> lexedTokens)
    {
        lexemeBuffer.addCharacter((char) inputBuffer.consumeCharacter());
        lexedTokens
                .add(new Token(Token.TokenType.OR, lexemeBuffer.toString(), inputBuffer.position));
    }

    private void parenClose(final InputBuffer inputBuffer, final LexemeBuffer lexemeBuffer,
            final List<Token> lexedTokens)
    {
        lexemeBuffer.addCharacter((char) inputBuffer.consumeCharacter());
        lexedTokens.add(new Token(Token.TokenType.PAREN_CLOSE, lexemeBuffer.toString(),
                inputBuffer.position));
    }

    private void parenOpen(final InputBuffer inputBuffer, final LexemeBuffer lexemeBuffer,
            final List<Token> lexedTokens)
    {
        lexemeBuffer.addCharacter((char) inputBuffer.consumeCharacter());
        lexedTokens.add(new Token(Token.TokenType.PAREN_OPEN, lexemeBuffer.toString(),
                inputBuffer.position));
    }

    private void regex(final InputBuffer inputBuffer, final LexemeBuffer lexemeBuffer,
            final List<Token> lexedTokens)
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
        lexedTokens.add(new Token(Token.TokenType.REGEX, lexeme, inputBuffer.position));
    }

    private void whitespace(final InputBuffer inputBuffer, final LexemeBuffer lexemeBuffer,
            final List<Token> lexedTokens)
    {
        lexemeBuffer.addCharacter((char) inputBuffer.consumeCharacter());
        lexedTokens.add(new Token(Token.TokenType.WHITESPACE, lexemeBuffer.toString(),
                inputBuffer.position));
    }
}
