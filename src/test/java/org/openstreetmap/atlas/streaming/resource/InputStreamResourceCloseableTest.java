package org.openstreetmap.atlas.streaming.resource;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;

import org.junit.jupiter.api.Test;
import org.openstreetmap.atlas.exception.CoreException;

/**
 * Test class for {@link InputStreamResourceCloseable}
 * 
 * @author Taylor Smock
 */
class InputStreamResourceCloseableTest
{
    /** An arbitrary string to use for input/output streams */
    private static final String HELLO_WORLD = "Hello World";

    /**
     * Test that the class properly closes a resource
     * 
     * @throws Exception
     *             If an untested exception is thrown
     */
    @Test
    void testClosingResource() throws Exception
    {
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStreamExceptional();
        outputStream.writeBytes(HELLO_WORLD.getBytes());
        try (InputStreamResourceCloseable inputStreamResource = new InputStreamResourceCloseable(
                () -> new ByteArrayInputStream(outputStream.toByteArray()), outputStream))
        {
            // Check twice for open/close of ByteArrayInputStream
            assertEquals(HELLO_WORLD, assertDoesNotThrow(() -> inputStreamResource.firstLine()));
            assertEquals(HELLO_WORLD, assertDoesNotThrow(() -> inputStreamResource.firstLine()));
            outputStream.close();
            final UncheckedIOException uncheckedIOException = assertThrows(
                    UncheckedIOException.class, () -> inputStreamResource.firstLine());
            assertTrue(uncheckedIOException.getCause() instanceof IOException);
        }
    }

    /**
     * Test that a thrown exception is passed back
     * 
     * @throws Exception
     *             If an untested exception is thrown
     */
    @Test
    void testExceptionOnClosing() throws Exception
    {
        final ByteArrayOutputStreamExceptional outputStream = new ByteArrayOutputStreamExceptional();
        outputStream.write(HELLO_WORLD.getBytes());
        final InputStreamResourceCloseable inputStreamResource = new InputStreamResourceCloseable(
                () -> new ByteArrayInputStream(outputStream.toByteArray()), outputStream);
        assertDoesNotThrow(() -> outputStream.close());
        outputStream.setThrowOnClose(true);

        assertThrows(CoreException.class, () -> inputStreamResource.close());
    }

    /**
     * Check that this class fails when improperly used
     * 
     * @throws Exception
     *             If an untested exception is thrown
     */
    @Test
    void testTryWithResources() throws Exception
    {
        final InputStreamResourceCloseable inputStreamResource;
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStreamExceptional())
        {
            outputStream.write(HELLO_WORLD.getBytes());
            inputStreamResource = new InputStreamResourceCloseable(
                    () -> new ByteArrayInputStream(outputStream.toByteArray()),
                    (AutoCloseable[]) null);
            assertEquals(HELLO_WORLD, assertDoesNotThrow(() -> inputStreamResource.firstLine()));
        }
        final UncheckedIOException uncheckedIOException = assertThrows(UncheckedIOException.class,
                () -> inputStreamResource.firstLine());
        assertTrue(uncheckedIOException.getCause() instanceof IOException);
        inputStreamResource.close();
    }
}
