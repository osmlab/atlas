package org.openstreetmap.atlas.streaming.resource;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.streaming.readers.CsvReaderTest;
import org.openstreetmap.atlas.utilities.collections.Iterables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author matthieun
 */
public class ResourceTest
{
    private static final Logger logger = LoggerFactory.getLogger(ResourceTest.class);

    @Test
    public void testRead()
    {
        AbstractResource resource = new InputStreamResource(
                () -> CsvReaderTest.class.getResourceAsStream("data.csv"));
        resource.lines().forEach(logger::info);
        resource = new InputStreamResource(
                () -> CsvReaderTest.class.getResourceAsStream("data.csv"));
        Assert.assertEquals(3, Iterables.size(resource.lines()));
    }

    // Examples of how the HttpResource works
    /*
     * @Test public void testHttpResource() { try { final HttpResource get = new GetResource( new
     * URIBuilder("http://localhost:5000/").build()); Assert.assertEquals(200, get.getStatusCode());
     * // just check the first line Assert.assertEquals("<!DOCTYPE html>",
     * get.lines().iterator().next()); get.close(); // not authenticated final HttpResource delete =
     * new DeleteResource( new
     * URIBuilder("http://localhost:5000/api/admin/challenge/22263").build());
     * Assert.assertEquals(401, delete.getStatusCode()); delete.setAuth("testuser", "password");
     * delete.reExecute(); //Assert.assertEquals(200, delete.getStatusCode()); delete.close(); final
     * byte[] body =
     * "{\"title\":\"HttpResource22263\",\"description\":\"Testing out the HttpResource\",\"blurb\":\"Testing out the HttpResource\",\"help\":\"Testing out the HttpResource\",\"difficulty\":1,\"active\":true}"
     * .getBytes(); final HttpResource post = new PostResource(new
     * URIBuilder("http://localhost:5000/api/admin/challenge/22263").build(), body);
     * post.setHeader(HttpHeaders.CONTENT_TYPE, "application/json"); post.setAuth("testuser",
     * "password"); Assert.assertEquals(201, post.getStatusCode()); } catch (Exception e) { throw
     * new CoreException("failed", e); } }
     */
}
