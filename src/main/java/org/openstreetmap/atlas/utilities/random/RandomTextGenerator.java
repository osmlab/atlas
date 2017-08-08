package org.openstreetmap.atlas.utilities.random;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.openstreetmap.atlas.exception.CoreException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generate random text (all English words) for stress tests.
 *
 * @author matthieun
 */
public class RandomTextGenerator implements Serializable
{
    private static final long serialVersionUID = 1874838119269724332L;

    private static final int MAXIMUM_WORDS_PER_LINE = 10;

    private static final Logger logger = LoggerFactory.getLogger(RandomTextGenerator.class);

    private static final List<String> DICTIONARY;

    static
    {
        try
        {
            final BufferedReader reader = new BufferedReader(new InputStreamReader(
                    RandomTextGenerator.class.getResourceAsStream("dictionary.txt")));
            String line;
            final Set<String> dictionarySet = new HashSet<>();
            while ((line = reader.readLine()) != null)
            {
                dictionarySet.add(line);
            }
            DICTIONARY = new ArrayList<>(dictionarySet);
        }
        catch (final IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    private static final int WORD_REPORTING_FREQUENCY = 1_000_000;

    private final Random random;

    private long count = 0;

    public RandomTextGenerator()
    {
        this(new Random());
    }

    public RandomTextGenerator(final Random random)
    {
        this.random = random;
    }

    public String generate(final long sizeInBytes)
    {
        final StringBuilder builder = new StringBuilder();
        int counter = 0;
        while (builder.length() < sizeInBytes)
        {
            if (builder.length() > 0)
            {
                if (counter % MAXIMUM_WORDS_PER_LINE == 0)
                {
                    builder.append("\n");
                }
                else
                {
                    builder.append(" ");
                }
            }
            builder.append(newWord());
            counter++;
        }
        builder.append("\n");
        logger.info("Generated text with " + counter + " words.");
        return builder.toString();
    }

    public BufferedReader infiniteReader(final String separator)
    {
        return new BufferedReader(new Reader()
        {
            private boolean closed = false;
            private String currentWord = newWord();
            private int index = 0;

            @Override
            public void close() throws IOException
            {
                this.closed = true;
            }

            @Override
            public int read(final char[] cbuf, final int off, final int len) throws IOException
            {
                if (this.closed)
                {
                    throw new RuntimeException("Cannot read a closed stream.");
                }
                if (off + len > cbuf.length)
                {
                    throw new RuntimeException(
                            "Buffer offset + length are larger than buffer size.");
                }
                for (int i = off; i < len; i++)
                {
                    cbuf[i] = nextChar();
                }
                return len - off;
            }

            private char nextChar()
            {
                if (this.index >= this.currentWord.length())
                {
                    nextWord();
                }
                return this.currentWord.charAt(this.index++);
            }

            private void nextWord()
            {
                this.index = 0;
                this.currentWord = separator + newWord();
            }
        });
    }

    public InputStream infiniteStream()
    {
        return new InputStream()
        {
            private boolean closed = false;
            private int index = 0;
            private final BufferedReader reader = infiniteReader("\n");
            private String currentLine = newLine();

            @Override
            public void close() throws IOException
            {
                this.closed = true;
            }

            @Override
            public int read() throws IOException
            {
                if (this.closed)
                {
                    throw new RuntimeException("Cannot read a closed stream.");
                }
                if (this.index >= this.currentLine.length())
                {
                    this.index = 0;
                    this.currentLine = newLine();
                    return "\n".charAt(0);
                }
                return this.currentLine.charAt(this.index++);
            }

            private String newLine()
            {
                try
                {
                    return this.reader.readLine();
                }
                catch (final IOException e)
                {
                    throw new CoreException("Unable to get line.", e);
                }
            }
        };
    }

    public String newWord()
    {
        if (++this.count % WORD_REPORTING_FREQUENCY == 0)
        {
            logger.trace("Generated {} random words.", this.count);
        }
        return DICTIONARY.get(this.random.nextInt(DICTIONARY.size()));
    }
}
