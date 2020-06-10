package org.openstreetmap.atlas.streaming;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * @author lcram
 */
public class StringInputStreamTest
{
    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Test
    public void testBasicBufferRead()
    {
        final String source = "Hello!";
        final StringInputStream stream1 = new StringInputStream(source);
        /*
         * Byte buffer must be exact length of input, or else the String comparison in the test will
         * fail. This happens because it appears that String#equals actually uses the underlying
         * byte buffer to check equality.
         */
        final byte[] buffer = new byte[source.length()];
        final int bytesRead1 = stream1.read(buffer, 0, stream1.getSource().length());
        Assert.assertEquals(6, bytesRead1);
        Assert.assertEquals(source, new String(buffer, StandardCharsets.UTF_8));

        // Subsequent read should return -1 since String has been exhausted
        final int bytesRead2 = stream1.read(buffer, 0, stream1.getSource().length());
        Assert.assertEquals(-1, bytesRead2);
    }

    @Test
    public void testBufferReadEdgeCaseLengthInvalid()
    {
        final String source = "Hello!";
        final StringInputStream stream1 = new StringInputStream(source);

        final byte[] buffer1 = new byte[source.length()];

        // Here we try to read an invalid number of bytes (i.e. we set requested length to -1)
        this.expectedException.expect(IndexOutOfBoundsException.class);
        this.expectedException.expectMessage("Range [0, 0 + -1) out of bounds for length 6");
        final int bytesRead1 = stream1.read(buffer1, 0, -1);
        Assert.assertEquals(0, bytesRead1);
    }

    @Test
    public void testBufferReadEdgeCaseLengthTooLong()
    {
        final String source = "Hello!";
        final StringInputStream stream1 = new StringInputStream(source);
        /*
         * Byte buffer must be exact length of input, or else the String comparison in the test will
         * fail. This happens because it appears that String#equals actually uses the underlying
         * byte buffer to check equality.
         */
        final byte[] buffer1 = new byte[source.length()];

        // Here we try to read a length longer than the String
        this.expectedException.expect(IndexOutOfBoundsException.class);
        this.expectedException.expectMessage("Range [0, 0 + 7) out of bounds for length 6");
        final int read = stream1.read(buffer1, 0, stream1.getSource().length() + 1);
    }

    @Test
    public void testByteReadSuccess()
    {
        final String source = "Hello!";
        final StringInputStream stream1 = new StringInputStream(source);

        final int byteRead1 = stream1.read();
        Assert.assertEquals(72, byteRead1);

        final int byteRead2 = stream1.read();
        Assert.assertEquals(101, byteRead2);

        final int byteRead3 = stream1.read();
        Assert.assertEquals(108, byteRead3);

        final int byteRead4 = stream1.read();
        Assert.assertEquals(108, byteRead4);

        final int byteRead5 = stream1.read();
        Assert.assertEquals(111, byteRead5);

        final int byteRead6 = stream1.read();
        Assert.assertEquals(33, byteRead6);

        // All subsequent reads should return -1 now that the String has been exhausted
        final int byteRead7 = stream1.read();
        Assert.assertEquals(-1, byteRead7);
        final int byteRead8 = stream1.read();
        Assert.assertEquals(-1, byteRead8);
    }

    @Test
    public void testMixReads()
    {
        final String source = "Hello!";
        final StringInputStream stream1 = new StringInputStream(source);

        final int byteRead1 = stream1.read();
        Assert.assertEquals(72, byteRead1);

        final int byteRead2 = stream1.read();
        Assert.assertEquals(101, byteRead2);

        final byte[] buffer1 = new byte["llo!".length()];
        final int bytesRead1 = stream1.read(buffer1, 0, buffer1.length);
        Assert.assertEquals(4, bytesRead1);
        Assert.assertEquals("llo!", new String(buffer1, StandardCharsets.UTF_8));
    }

    @Test
    public void testMultiBufferRead()
    {
        final String source = "Hello world!";
        final StringInputStream stream1 = new StringInputStream(source);

        final byte[] buffer1 = new byte["Hello".length()];
        final byte[] buffer2 = new byte[" world!".length()];

        final int bytesRead1 = stream1.read(buffer1, 0, buffer1.length);
        Assert.assertEquals(5, bytesRead1);
        Assert.assertEquals("Hello", new String(buffer1, StandardCharsets.UTF_8));

        final int bytesRead2 = stream1.read(buffer2, 0, buffer2.length);
        Assert.assertEquals(7, bytesRead2);
        Assert.assertEquals(" world!", new String(buffer2, StandardCharsets.UTF_8));
    }

    @Test
    public void testOffsetBufferRead()
    {
        final String source = "Hello world!";
        final StringInputStream stream1 = new StringInputStream(source);

        final byte[] buffer1 = new byte["Hello world!".length()];

        final int initialChunkSize = 5;
        final int bytesRead1 = stream1.read(buffer1, 0, initialChunkSize);
        Assert.assertEquals(5, bytesRead1);
        final int bytesRead2 = stream1.read(buffer1, 5, buffer1.length - initialChunkSize);
        Assert.assertEquals(7, bytesRead2);

        Assert.assertEquals("Hello world!", new String(buffer1, StandardCharsets.UTF_8));
    }

    @Test
    public void testReadOnAlreadyReadStream()
    {
        final String source = "Hello world!";
        final StringInputStream stream1 = new StringInputStream(source);

        /*
         * Read 5 bytes of the input into a buffer. This means our buffer will not be entirely
         * filled.
         */
        final byte[] buffer1 = new byte["Hello world!".length()];
        final int readChunkSize = 5;
        final int bytesRead1 = stream1.read(buffer1, 0, readChunkSize);
        Assert.assertEquals(readChunkSize, bytesRead1);
        Assert.assertEquals("Hello\0\0\0\0\0\0\0", new String(buffer1, StandardCharsets.UTF_8));

        /*
         * Now we try to read with a length larger than the remaining stream. Again, our buffer will
         * not be entirely filled.
         */
        final byte[] buffer2 = new byte["Hello world!".length()];
        final int bytesRead2 = stream1.read(buffer2, 0, buffer2.length);
        Assert.assertEquals(7, bytesRead2);
        Assert.assertEquals(" world!\0\0\0\0\0", new String(buffer2, StandardCharsets.UTF_8));
    }

    @Test
    public void testReadZeroLength()
    {
        final String source = "Hello world!";
        final StringInputStream stream1 = new StringInputStream(source);

        final byte[] buffer1 = new byte["Hello world!".length()];
        final int bytesRead1 = stream1.read(buffer1, 0, 0);
        Assert.assertEquals(0, bytesRead1);
    }

    @Test
    public void testWithScanner()
    {
        final String source = "line1\nline2\nline3\nline4\n";
        final StringInputStream stream1 = new StringInputStream(source);

        final List<String> lines = new ArrayList<>();
        final Scanner scanner = new Scanner(stream1);
        while (scanner.hasNextLine())
        {
            lines.add(scanner.nextLine());
        }

        Assert.assertEquals(Arrays.asList("line1", "line2", "line3", "line4"), lines);
    }
}
