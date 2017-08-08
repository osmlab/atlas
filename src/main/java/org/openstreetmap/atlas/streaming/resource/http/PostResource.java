package org.openstreetmap.atlas.streaming.resource.http;

import java.io.UnsupportedEncodingException;
import java.net.URI;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;

/**
 * Usage: byte[] body = "{\"test\":\"test\"}".getBytes(); URI uri = new
 * URIBuilder("http://localhost:2020/path/to/location").build(); HttpResource post = new
 * PostResource(uri, body); //read the response post.lines().foreach(System.out.println(x)); // get
 * the status code int code = post.getStatusCode();
 *
 * @author cuthbertm
 */
public class PostResource extends HttpResource
{
    public PostResource(final String uri)
    {
        this(URI.create(uri));
    }

    public PostResource(final URI uri)
    {
        super(uri);
        setRequest(new HttpPost(uri));
    }

    public void setEntity(final HttpEntity entity)
    {
        ((HttpEntityEnclosingRequestBase) getRequest()).setEntity(entity);
    }

    public void setStringBody(final String body, final ContentType contentType)
            throws UnsupportedEncodingException
    {
        // using HttpEntityEnclosingRequestBase, so that resources like Put and Path can
        // extend from it.
        final HttpEntityEnclosingRequestBase base = (HttpEntityEnclosingRequestBase) getRequest();
        base.addHeader(HttpHeaders.CONTENT_TYPE, contentType.getMimeType());
        final StringEntity entity = new StringEntity(body, contentType);
        base.setEntity(entity);
    }
}
