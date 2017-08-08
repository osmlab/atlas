package org.openstreetmap.atlas.streaming.resource.http;

import java.net.URI;

import org.apache.http.client.methods.HttpPut;

/**
 * Same usage as {@link PostResource}
 *
 * @author cuthbertm
 */
public class PutResource extends PostResource
{
    public PutResource(final String uri)
    {
        this(URI.create(uri));
    }

    public PutResource(final URI uri)
    {
        super(uri);
        setRequest(new HttpPut(uri));
    }
}
