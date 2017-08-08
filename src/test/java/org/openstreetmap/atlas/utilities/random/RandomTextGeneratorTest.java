package org.openstreetmap.atlas.utilities.random;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

import org.junit.Test;

/**
 * @author matthieun
 */
public class RandomTextGeneratorTest
{
    @Test
    public void testReader() throws IOException
    {
        final RandomTextGenerator generator = new RandomTextGenerator();
        final BufferedReader reader = generator.infiniteReader("\n");
        String line = "";
        int counter = 0;
        while ((line = reader.readLine()) != null && counter < 10)
        {
            counter++;
            System.out.println(line);
        }
        reader.close();
        try
        {
            reader.readLine();
        }
        catch (final IOException e)
        {
            // Test passes :)
            return;
        }
        throw new RuntimeException("Reader was closed but kept reading.");
    }

    @Test
    public void testStream() throws IOException
    {
        final RandomTextGenerator generator = new RandomTextGenerator();
        final BufferedReader reader = new BufferedReader(
                new InputStreamReader(generator.infiniteStream(), Charset.forName("UTF-8")));
        String line = "";
        int counter = 0;
        while ((line = reader.readLine()) != null && counter < 10)
        {
            counter++;
            System.out.println(line);
        }
        reader.close();
        try
        {
            reader.readLine();
        }
        catch (final IOException e)
        {
            // Test passes :)
            return;
        }
        throw new RuntimeException("Reader was closed but kept reading.");
    }
}
