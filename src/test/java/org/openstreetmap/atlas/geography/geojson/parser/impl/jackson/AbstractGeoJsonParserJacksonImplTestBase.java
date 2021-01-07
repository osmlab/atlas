package org.openstreetmap.atlas.geography.geojson.parser.impl.jackson;

import java.nio.charset.Charset;

import org.apache.commons.io.IOUtils;
import org.openstreetmap.atlas.geography.geojson.parser.domain.base.GeoJsonItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Automatically loads the appropriate JSON file in the classpath under
 * {@link AbstractGeoJsonParserJacksonImplTestBase#BASE_CLASSPATH}. It looks for the file that
 * shares the same name as the test method.
 *
 * @author Yazad Khambata
 */
public class AbstractGeoJsonParserJacksonImplTestBase
{
    public static final String BASE_CLASSPATH = "org/openstreetmap/atlas/geography/geojson/parser/";
    public static final String EXTENSION = ".json";
    private static final Logger log = LoggerFactory
            .getLogger(AbstractGeoJsonParserJacksonImplTestBase.class);

    protected String extractJsonForExtension()
    {
        final String callingMethodName = Thread.currentThread().getStackTrace()[2].getMethodName();

        return extractJsonForExtension(callingMethodName);
    }

    protected GeoJsonItem toGeoJsonItem()
    {
        // Do not refactor into another method.
        final String callingMethodName = Thread.currentThread().getStackTrace()[2].getMethodName();

        log.info("calling method: {}.", callingMethodName);

        final String json = extractJsonForExtension(callingMethodName);

        log.info("{} json:: {}.", callingMethodName, json);

        final GeoJsonItem geoJsonItem = GeoJsonParserJacksonImpl.INSTANCE.deserialize(json);
        log.info("{} geoJsonItem:: {}.", callingMethodName, geoJsonItem);
        return geoJsonItem;
    }

    private String extractJson(final String filePath)
    {
        try
        {
            return IOUtils.toString(this.getClass().getClassLoader().getResourceAsStream(filePath),
                    Charset.defaultCharset());
        }
        catch (final Exception e)
        {
            throw new RuntimeException(filePath, e);
        }
    }

    private String extractJsonForExtension(final String callingMethodName)
    {
        final String filePath = toFilePath(callingMethodName);
        return extractJson(filePath);
    }

    private String toFilePath(final String callingMethodName)
    {
        return new StringBuilder(BASE_CLASSPATH).append(callingMethodName).append(EXTENSION)
                .toString();
    }

}
