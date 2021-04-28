package org.openstreetmap.atlas.streaming.resource;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.OutputStream;
import java.io.UncheckedIOException;

import org.junit.jupiter.api.Test;
import org.openstreetmap.atlas.exception.CoreException;

/**
 * Test class for {@link OutputStreamWritableResourceCloseable}
 * 
 * @author Taylor Smock
 */
class OutputStreamWritableResourceCloseableTest
{
    private static final String HELLO_WORLD = "Hello World";
    private static final byte[] HELLO_WORLD_BYTES = HELLO_WORLD.getBytes();

    @Test
    void testClosingResource() throws Exception
    {
        final ByteArrayOutputStreamExceptional outputStream = new ByteArrayOutputStreamExceptional();
        final OutputStreamWritableResourceCloseable resource = new OutputStreamWritableResourceCloseable(
                outputStream, (AutoCloseable[]) null);
        assertDoesNotThrow(() -> resource.write().write(HELLO_WORLD_BYTES));
        assertEquals(HELLO_WORLD, new String(outputStream.toByteArray()));

        outputStream.close();
        assertTrue(outputStream.isClosed());
        final OutputStream writer = resource.write();
        assertThrows(UncheckedIOException.class, () -> writer.write(HELLO_WORLD_BYTES));
        resource.close();
    }

    @Test
    void testCorrectImplementation() throws Exception
    {
        final ByteArrayOutputStreamExceptional outputStream = new ByteArrayOutputStreamExceptional();
        final OutputStreamWritableResourceCloseable resource = new OutputStreamWritableResourceCloseable(
                outputStream, outputStream);
        assertDoesNotThrow(() -> resource.write().write(HELLO_WORLD_BYTES));
        assertEquals(HELLO_WORLD, new String(outputStream.toByteArray()));

        resource.close();
        assertTrue(outputStream.isClosed());
    }

    @Test
    void testTryWithResources() throws Exception
    {
        final ByteArrayOutputStreamExceptional outputStream = new ByteArrayOutputStreamExceptional();
        final OutputStreamWritableResourceCloseable resource = new OutputStreamWritableResourceCloseable(
                outputStream, outputStream);

        try (ByteArrayOutputStreamExceptional temp = outputStream)
        {
            assertDoesNotThrow(() -> resource.write().write(HELLO_WORLD_BYTES));
            assertEquals(HELLO_WORLD, new String(outputStream.toByteArray()));
        }

        final OutputStream writer = resource.write();
        assertThrows(UncheckedIOException.class, () -> writer.write(HELLO_WORLD_BYTES));
        outputStream.setThrowOnClose(true);
        assertThrows(CoreException.class, () -> resource.close());
    }

}
