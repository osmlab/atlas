package org.openstreetmap.atlas.utilities.http.rest;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.utilities.tuples.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * This class handles all responses from an HTTP server, and the caller can configure a behavior
 * based on the HTTP status code. There are five kinds of actions that can be taken:
 * <ul>
 * <li>log - just write the response code and body contents to SLF4J</li>
 * <li>ignore - don't do anything at all with the response</li>
 * <li>throw - wrap up the status code and body into a DislikedResponseCodeException and throw it
 * </li>
 * <li>actOn - given a consumer for a tuple of response code and String, wrap up the status code and
 * body into a Tuple and pass it to the consumer</li>
 * <li>parseJson - given a Java class the represents the incoming JSON data and a consumer, using
 * Jackson's ObjectMapper and convert it, then pass a Tuple containing the response code and the
 * converted Java object to the consumer</li>
 * </ul>
 * Note: All of these methods expect smallish bodies since these are really API calls into JSON
 * using a REST interface
 *
 * @author cstaylor
 */
public class HttpResultHandler
{
    private static final Logger logger = LoggerFactory.getLogger(HttpResultHandler.class);

    private static final Consumer<Tuple<Integer, String>> LOGGING_HANDLER = tuple ->
    {
        logger.debug("[{}] -> {}", tuple.getFirst(), tuple.getSecond());
    };

    private static final Consumer<Tuple<Integer, String>> IGNORE_HANDLER = tuple ->
    {
        // Does nothing
    };

    private static final Consumer<Tuple<Integer, String>> THROW_HANDLER = tuple ->
    {
        throw new DislikedResponseCodeException(tuple.getFirst(), tuple.getSecond());
    };

    private final Map<Integer, Consumer<Tuple<Integer, String>>> responseHandlers = new HashMap<>();

    private Consumer<Tuple<Integer, String>> defaultHandler = IGNORE_HANDLER;

    public HttpResultHandler abort()
    {
        this.defaultHandler = THROW_HANDLER;
        return this;
    }

    public HttpResultHandler abort(final int statusCode)
    {
        return actOn(statusCode, THROW_HANDLER);
    }

    public HttpResultHandler actOn(final int statusCode,
            final Consumer<Tuple<Integer, String>> data)
    {
        this.responseHandlers.put(statusCode, data);
        return this;
    }

    public HttpResultHandler ignore()
    {
        this.defaultHandler = IGNORE_HANDLER;
        return this;
    }

    public HttpResultHandler ignore(final int statusCode)
    {
        return actOn(statusCode, IGNORE_HANDLER);
    }

    public HttpResultHandler log()
    {
        this.defaultHandler = LOGGING_HANDLER;
        return this;
    }

    public HttpResultHandler log(final int statusCode)
    {
        return actOn(statusCode, LOGGING_HANDLER);
    }

    public void parse(final CloseableHttpResponse response)
    {
        try (StringWriter stringWriter = new StringWriter())
        {
            IOUtils.copy(response.getEntity().getContent(), stringWriter, StandardCharsets.UTF_8);
            final int statusCode = response.getStatusLine().getStatusCode();
            stringWriter.flush();
            this.responseHandlers.getOrDefault(statusCode, this.defaultHandler)
                    .accept(new Tuple<>(statusCode, stringWriter.toString()));
        }
        catch (final IOException oops)
        {
            throw new CoreException("Error when parsing HTTP response body", oops);
        }
    }

    public <T> HttpResultHandler parseJSON(final int statusCode, final Class<T> conversionClass,
            final Consumer<Tuple<Integer, T>> consumer)
    {
        return actOn(statusCode, item ->
        {
            try
            {
                final JsonParser parser = new JsonFactory().createParser(item.getSecond());
                parser.setCodec(new ObjectMapper());
                consumer.accept(new Tuple<>(statusCode, parser.readValueAs(conversionClass)));
            }
            catch (final Exception oops)
            {
                throw new CoreException("Error when parsing JSON", oops);
            }
        });
    }
}
