package org.openstreetmap.atlas.streaming.resource.http;

import java.net.URI;

import org.apache.http.client.methods.HttpDelete;

/**
 * Same usage as found in {@link HttpResource}
 *
 * @author cuthbertm
 */
public class DeleteResource extends HttpResource
{
    public DeleteResource(final String uri)
    {
        this(URI.create(uri));
    }

    public DeleteResource(final URI uri)
    {
        super(uri);
        setRequest(new HttpDelete(uri));
    }
}
