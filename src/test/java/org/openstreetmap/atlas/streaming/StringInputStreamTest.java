package org.openstreetmap.atlas.streaming;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author lcram
 */
public class StringInputStreamTest
{
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
    public void testBufferReadEdgeCases()
    {
        final String source = "Hello!";
        final StringInputStream stream1 = new StringInputStream(source);
        /*
         * Byte buffer must be exact length of input, or else the String comparison in the test will
         * fail. This happens because it appears that String#equals actually uses the underlying
         * byte buffer to check equality.
         */
        final byte[] buffer = new byte[source.length()];

        // Here we try to read a length longer than the String, this should work anyway
        final int bytesRead1 = stream1.read(buffer, 0, stream1.getSource().length() + 100);
        Assert.assertEquals(6, bytesRead1);
        Assert.assertEquals(source, new String(buffer, StandardCharsets.UTF_8));
    }

    @Test
    public void testByteReadSuccess() throws IOException
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
}
