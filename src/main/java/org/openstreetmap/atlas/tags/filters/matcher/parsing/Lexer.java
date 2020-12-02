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
            builder.append(token.toString());
            builder.append(", ");
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
            if (isKeyValueCharacter(inputBuffer.peek())
                    || inputBuffer.peek() == Token.TokenType.ESCAPE.getLiteralValue().charAt(0))
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
            else if (inputBuffer.peek() == Token.TokenType.XOR.getLiteralValue().charAt(0))
            {
                xor(inputBuffer, lexemeBuffer, lexedTokens);
            }
            else if (inputBuffer.peek() == Token.TokenType.PAREN_OPEN.getLiteralValue().charAt(0))
            {
                parenOpen(inputBuffer, lexemeBuffer, lexedTokens);
            }
            else if (inputBuffer.peek() == Token.TokenType.PAREN_CLOSE.getLiteralValue().charAt(0))
            {
                parenClose(inputBuffer, lexemeBuffer, lexedTokens);
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
                throwSyntaxError("EOF after '\"'", inputBuffer, inputBuffer.string);
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
        lexedTokens.add(new Token(Token.TokenType.DOUBLE_QUOTE, lexeme, inputBuffer.position));
    }

    private void equal(final InputBuffer inputBuffer, final LexemeBuffer lexemeBuffer, // NOSONAR
            final List<Token> lexedTokens)
    {
        lexemeBuffer.addCharacter((char) inputBuffer.consumeCharacter());
        lexedTokens.add(
                new Token(Token.TokenType.EQUAL, lexemeBuffer.toString(), inputBuffer.position));
    }

    private boolean isKeyValueCharacter(final int ch)
    {
        /*
         * Anything not in this list counts as a key/value character. To use characters on this list
         * in a key/value literal, users must use escapes '\' or double quotes '"'.
         */
        return ((char) ch) != Token.TokenType.AND.getLiteralValue().charAt(0)
                && ((char) ch) != Token.TokenType.OR.getLiteralValue().charAt(0)
                && ((char) ch) != Token.TokenType.XOR.getLiteralValue().charAt(0)
                && ((char) ch) != Token.TokenType.EQUAL.getLiteralValue().charAt(0)
                && ((char) ch) != Token.TokenType.PAREN_OPEN.getLiteralValue().charAt(0)
                && ((char) ch) != Token.TokenType.PAREN_CLOSE.getLiteralValue().charAt(0)
                && ((char) ch) != Token.TokenType.REGEX.getLiteralValue().charAt(0)
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
            if (ch == Token.TokenType.ESCAPE.getLiteralValue().charAt(0))
            {
                /*
                 * If we see an ESCAPE, consume the immediate next character and place it in the
                 * lexeme buffer. We throw the escape character '\' out. If the escape character
                 * comes just before the EOF, fail.
                 */
                final int escaped = inputBuffer.consumeCharacter();
                if (escaped == InputBuffer.EOF)
                {
                    throwSyntaxError("EOF after '\\'", inputBuffer, inputBuffer.string);
                }
                lexemeBuffer.addCharacter((char) escaped);
            }
            else
            {
                lexemeBuffer.addCharacter((char) ch);
            }
        }
        while (isKeyValueCharacter(ch));

        if (ch != InputBuffer.EOF)
        {
            /*
             * We reached the end of the putative LITERAL token, so give back the non-literal
             * character to the input buffer for the main loop to re-process.
             */
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
                throwSyntaxError("EOF after '/'", inputBuffer, inputBuffer.string);
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
                // Otherwise, pass the '\' forward into the regex normally
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

        // Strip leftover leading '/' character
        final String lexeme = lexemeBuffer.stripLeading().toString();
        lexedTokens.add(new Token(Token.TokenType.REGEX, lexeme, inputBuffer.position));
    }

    private void throwSyntaxError(final String unexpected, final InputBuffer inputBuffer,
            final String inputLine)
    {
        final String arrow = "~".repeat(Math.max(0, inputBuffer.position)) + "^";
        if (unexpected != null)
        {
            throw new CoreException("syntax error: unexpected {}\n{}\n{}", unexpected, inputLine,
                    arrow);
        }
        throw new CoreException("syntax error: unexpected input\n{}\n{}", inputLine, arrow);
    }

    private void whitespace(final InputBuffer inputBuffer, final LexemeBuffer lexemeBuffer,
            final List<Token> lexedTokens)
    {
        lexemeBuffer.addCharacter((char) inputBuffer.consumeCharacter());
        lexedTokens.add(new Token(Token.TokenType.WHITESPACE, lexemeBuffer.toString(),
                inputBuffer.position));
    }

    private void xor(final InputBuffer inputBuffer, final LexemeBuffer lexemeBuffer,
            final List<Token> lexedTokens)
    {
        lexemeBuffer.addCharacter((char) inputBuffer.consumeCharacter());
        lexedTokens
                .add(new Token(Token.TokenType.XOR, lexemeBuffer.toString(), inputBuffer.position));
    }
}
