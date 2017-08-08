package org.openstreetmap.atlas.utilities.runtime.system.memory;

import java.util.Random;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * @author matthieun
 */
public class MemoryTest
{
    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Test
    public void testConversion()
    {
        for (int counter = 0; counter < 100; counter++)
        {
            long bytes = new Random().nextLong();
            if (bytes < 0)
            {
                bytes = -bytes;
            }
            runConversionTest(bytes);
        }
    }

    @Test
    public void testPrint()
    {
        Memory.printCurrentMemory();
    }

    private void assertConversion(final long bytes, final double kiloBytes, final double megaBytes,
            final double gigaBytes, final double teraBytes, final Memory memory)
    {
        Assert.assertEquals(0.0, Math.abs(bytes - memory.asBytes()) / bytes, 5);
        Assert.assertEquals(kiloBytes, memory.asKiloBytes(), 5);
        Assert.assertEquals(megaBytes, memory.asMegaBytes(), 5);
        Assert.assertEquals(gigaBytes, memory.asGigaBytes(), 5);
        Assert.assertEquals(teraBytes, memory.asTeraBytes(), 5);
    }

    private void runConversionTest(final long bytes)
    {
        final double kiloBytes = bytes / 1024.0;
        final double megaBytes = kiloBytes / 1024.0;
        final double gigaBytes = megaBytes / 1024.0;
        final double teraBytes = gigaBytes / 1024.0;

        Memory memory = Memory.bytes(bytes);
        assertConversion(bytes, kiloBytes, megaBytes, gigaBytes, teraBytes, memory);

        memory = Memory.kiloBytes(kiloBytes);
        assertConversion(bytes, kiloBytes, megaBytes, gigaBytes, teraBytes, memory);

        memory = Memory.megaBytes(megaBytes);
        assertConversion(bytes, kiloBytes, megaBytes, gigaBytes, teraBytes, memory);

        memory = Memory.gigaBytes(gigaBytes);
        assertConversion(bytes, kiloBytes, megaBytes, gigaBytes, teraBytes, memory);

        memory = Memory.teraBytes(teraBytes);
        assertConversion(bytes, kiloBytes, megaBytes, gigaBytes, teraBytes, memory);
    }
}
