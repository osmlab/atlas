package org.openstreetmap.atlas.utilities.conversion;

import java.nio.charset.StandardCharsets;

/**
 * Inspired from <a href="https://stackoverflow.com/a/140861/1558687"/> and
 * <a href="https://stackoverflow.com/a/9855338/1558687" />
 * 
 * @author matthieun
 */
public class HexStringByteArrayConverter implements TwoWayConverter<String, byte[]>
{
    private static final int SHIFT_4 = 4;
    private static final int SHIFT_16 = 16;
    private static final int SHIFT_FF = 0xFF;
    private static final int SHIFT_0F = 0x0F;
    private static final byte[] HEX_ARRAY = "0123456789ABCDEF".getBytes();

    @Override
    public String backwardConvert(final byte[] bytes)
    {
        final byte[] hexChars = new byte[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++)
        {
            final int value = bytes[j] & SHIFT_FF;
            hexChars[j * 2] = HEX_ARRAY[value >>> SHIFT_4];
            hexChars[j * 2 + 1] = HEX_ARRAY[value & SHIFT_0F];
        }
        return new String(hexChars, StandardCharsets.UTF_8);
    }

    @Override
    public byte[] convert(final String value)
    {
        final int length = value.length();
        final byte[] result = new byte[length / 2];
        for (int i = 0; i < length; i += 2)
        {
            result[i / 2] = (byte) ((Character.digit(value.charAt(i), SHIFT_16) << SHIFT_4)
                    + Character.digit(value.charAt(i + 1), SHIFT_16));
        }
        return result;
    }
}
