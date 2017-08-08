package org.openstreetmap.atlas.utilities.runtime.system.memory;

import org.openstreetmap.atlas.utilities.scalars.Ratio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class holding an amount of memory
 *
 * @author matthieun
 */
public final class Memory
{
    private static final Logger logger = LoggerFactory.getLogger(Memory.class);
    private static final long BYTES_PER_KILO_BYTE = 1024;
    private static final long BYTES_PER_MEGA_BYTE = BYTES_PER_KILO_BYTE * BYTES_PER_KILO_BYTE;
    private static final long BYTES_PER_GIGA_BYTE = BYTES_PER_KILO_BYTE * BYTES_PER_MEGA_BYTE;
    private static final long BYTES_PER_TERA_BYTE = BYTES_PER_KILO_BYTE * BYTES_PER_GIGA_BYTE;
    public static final Memory ZERO = Memory.bytes(0);

    private final long bytes;

    /**
     * @param bytes
     *            Some amount of memory in bytes
     * @return The equivalent memory object
     */
    public static Memory bytes(final long bytes)
    {
        return new Memory(bytes);
    }

    /**
     * @return The free memory, as reported by Runtime.getRuntime().freeMemory()
     */
    public static Memory free()
    {
        return Memory.bytes(Runtime.getRuntime().freeMemory());
    }

    /**
     * @param gigaBytes
     *            Some amount of memory in gigaBytes
     * @return The equivalent memory object
     */
    public static Memory gigaBytes(final double gigaBytes)
    {
        return bytes(Math.round(gigaBytes * BYTES_PER_GIGA_BYTE));
    }

    /**
     * @param kiloBytes
     *            Some amount of memory in kiloBytes
     * @return The equivalent memory object
     */
    public static Memory kiloBytes(final double kiloBytes)
    {
        return bytes(Math.round(kiloBytes * BYTES_PER_KILO_BYTE));
    }

    /**
     * @return The maximum memory, as reported by Runtime.getRuntime().maxMemory()
     */
    public static Memory maximum()
    {
        return Memory.bytes(Runtime.getRuntime().maxMemory());
    }

    /**
     * @param megaBytes
     *            Some amount of memory in megaBytes
     * @return The equivalent memory object
     */
    public static Memory megaBytes(final double megaBytes)
    {
        return bytes(Math.round(megaBytes * BYTES_PER_MEGA_BYTE));
    }

    /**
     * Print a summary of the JVM's current memory usage, using Runtime.getRuntime()
     */
    public static void printCurrentMemory()
    {
        logger.info("########## Memory utilization statistics ##########");
        logger.info("Ratio Used / Maximum: {}",
                Ratio.ratio(Memory.used().asKiloBytes() / Memory.maximum().asKiloBytes()));
        logger.info("Used Memory: {}", Memory.used());
        logger.info("Free Memory: {}", Memory.free());
        logger.info("Total Memory: {}", Memory.total());
        logger.info("Maximum Memory: {}", Memory.maximum());
        logger.info("###################################################");
    }

    /**
     * @param teraBytes
     *            Some amount of memory in teraBytes
     * @return The equivalent memory object
     */
    public static Memory teraBytes(final double teraBytes)
    {
        return bytes(Math.round(teraBytes * BYTES_PER_TERA_BYTE));
    }

    /**
     * @return The total memory, as reported by Runtime.getRuntime().totalMemory()
     */
    public static Memory total()
    {
        return Memory.bytes(Runtime.getRuntime().totalMemory());
    }

    /**
     * @return The used memory, as reported by Runtime.getRuntime().totalMemory() minus
     *         Runtime.getRuntime().freeMemory()
     */
    public static Memory used()
    {
        final Runtime runtime = Runtime.getRuntime();
        final long bytes = runtime.totalMemory() - runtime.freeMemory();
        if (bytes < 0)
        {
            return ZERO;
        }
        return Memory.bytes(bytes);
    }

    /**
     * construct a memory object
     *
     * @param bytes
     *            The amount of bytes
     */
    private Memory(final long bytes)
    {
        this.bytes = bytes;
    }

    /**
     * @return This memory amount in bytes
     */
    public long asBytes()
    {
        return this.bytes;
    }

    /**
     * @return This memory amount in giga bytes
     */
    public double asGigaBytes()
    {
        return (double) this.asBytes() / BYTES_PER_GIGA_BYTE;
    }

    /**
     * @return This memory amount in kilo bytes
     */
    public double asKiloBytes()
    {
        return (double) this.asBytes() / BYTES_PER_KILO_BYTE;
    }

    /**
     * @return This memory amount in mega bytes
     */
    public double asMegaBytes()
    {
        return (double) this.asBytes() / BYTES_PER_MEGA_BYTE;
    }

    /**
     * @return This memory amount in tera bytes
     */
    public double asTeraBytes()
    {
        return (double) this.asBytes() / BYTES_PER_TERA_BYTE;
    }

    @Override
    public String toString()
    {
        if (this.bytes < BYTES_PER_KILO_BYTE)
        {
            return this.asBytes() + " bytes";
        }
        else if (this.bytes < BYTES_PER_MEGA_BYTE)
        {
            return format(this.asKiloBytes()) + " Kb";
        }
        else if (this.bytes < BYTES_PER_GIGA_BYTE)
        {
            return format(this.asMegaBytes()) + " Mb";
        }
        else if (this.bytes < BYTES_PER_TERA_BYTE)
        {
            return format(this.asGigaBytes()) + " Gb";
        }
        else
        {
            return format(this.asTeraBytes()) + " Tb";
        }
    }

    private String format(final double value)
    {
        return String.format("%.2f", value);
    }
}
