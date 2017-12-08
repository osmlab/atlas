package org.openstreetmap.atlas.streaming.resource.http;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Optional;

import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.streaming.StringInputStream;
import org.openstreetmap.atlas.streaming.resource.AbstractResource;

/**
 * Base Http resource object that will handle most of the http request information. Sub classes
 * generally will set the type of request and possibly a couple of request-specific parameters. For
 * instance POST will require to post body data in the request. Example Usage: URI uri = new
 * URIBuilder("http://localhost:2020/path/to/location").build(); HttpResource post = new GetResource
 * // get t(uri, body); //read the response post.lines().foreach(System.out.println(x)); //get
 * status code int code = post.getStatusCode();
 *
 * @author cuthbertm
 */
public abstract class HttpResource extends AbstractResource
{
    private HttpRequestBase request;
    private final URI uri;
    private CloseableHttpResponse response = null;
    private Optional<UsernamePasswordCredentials> creds = Optional.empty();
    private Optional<HttpHost> proxy = Optional.empty();

    private static HttpClientContext createBasicAuthCache(final HttpHost target,
            final HttpClientContext context)
    {
        // Create AuthCache instance
        final AuthCache authCache = new BasicAuthCache();
        // Generate BASIC scheme object and add it to the local
        // auth cache
        final BasicScheme basicAuth = new BasicScheme();
        authCache.put(target, basicAuth);
        // Add AuthCache to the execution context
        context.setAuthCache(authCache);
        return context;
    }

    public HttpResource(final String uri)
    {
        this(URI.create(uri));
    }

    public HttpResource(final URI uri)
    {
        this.uri = uri;
    }

    public void close()
    {
        HttpClientUtils.closeQuietly(this.response);
    }

    /**
     * If you want to execute the request, call this. All other attempts in an HttpResource will
     * first check to see if the response object has been retrieved. This will null out the response
     * object and execute it again.
     */
    public void execute()
    {
        this.response = null;
        onRead();
    }

    public Header[] getHeader(final String headerKey)
    {
        // make sure that a connection attempt has been made
        onRead();
        return this.response.getHeaders(headerKey);
    }

    public HttpRequestBase getRequest()
    {
        return this.request;
    }

    public String getRequestBodyAsString()
    {
        // make sure that a connection attempt has been made
        final StringBuilder builder = new StringBuilder();
        lines().forEach(x -> builder.append(x));
        return builder.toString();
    }
    // ------------------------------------//

    public CloseableHttpResponse getResponse()
    {
        // make sure that a connection attempt has been made
        onRead();
        return this.response;
    }

    // ---- HTTP Helper Functions ---------//
    public int getStatusCode()
    {
        // make sure that a connection attempt has been made
        onRead();
        return this.response.getStatusLine().getStatusCode();
    }

    public URI getURI()
    {
        return this.uri;
    }

    public void setAuth(final String user, final String pass)
    {
        this.creds = Optional.of(new UsernamePasswordCredentials(user, pass));
    }

    public void setHeader(final String name, final String value)
    {
        this.request.setHeader(name, value);
    }

    public void setProxy(final HttpHost proxy)
    {
        this.proxy = Optional.ofNullable(proxy);
    }

    public void setRequest(final HttpRequestBase request)
    {
        this.request = request;
    }

    @Override
    protected InputStream onRead()
    {
        try
        {
            if (this.response == null)
            {
                final HttpHost target = new HttpHost(this.uri.getHost(), this.uri.getPort(),
                        this.uri.getScheme());
                HttpClientContext context = HttpClientContext.create();
                HttpClientBuilder clientBuilder = HttpClients.custom();
                if (this.creds.isPresent())
                {
                    final CredentialsProvider credsProvider = new BasicCredentialsProvider();
                    credsProvider.setCredentials(
                            new AuthScope(target.getHostName(), target.getPort()),
                            this.creds.get());
                    clientBuilder = clientBuilder.setDefaultCredentialsProvider(credsProvider);
                }
                if (this.proxy.isPresent())
                {
                    clientBuilder = clientBuilder.setProxy(this.proxy.get());
                }
                final CloseableHttpClient client = clientBuilder.build();
                context = createBasicAuthCache(target, context);
                this.response = client.execute(target, this.request, context);
            }
            if (this.response.getEntity() == null)
            {
                return new StringInputStream("");
            }
            return this.response.getEntity().getContent();
        }
        catch (final IOException ioe)
        {
            throw new CoreException(ioe.getMessage(), ioe);
        }
    }
}
