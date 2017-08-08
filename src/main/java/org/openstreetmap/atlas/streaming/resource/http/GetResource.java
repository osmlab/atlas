package org.openstreetmap.atlas.streaming.resource.http;

import java.net.URI;

import org.apache.http.client.methods.HttpGet;

/**
 * Same usage as found in {@link HttpResource}
 *
 * @author cuthbertm
 */
public class GetResource extends HttpResource
{
    public GetResource(final String uri)
    {
        this(URI.create(uri));
    }

    public GetResource(final URI uri)
    {
        super(uri);
        setRequest(new HttpGet(uri));
    }
}
