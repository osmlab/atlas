package org.openstreetmap.atlas.geography.geojson.parser.domain.properties.ext;

import org.openstreetmap.atlas.geography.geojson.parser.domain.annotation.Foreign;
import org.openstreetmap.atlas.geography.geojson.parser.domain.properties.Properties;

import java.util.Map;

/**
 * @author Yazad Khambata
 */
@Foreign
public class ExtendedProperties extends Properties {
    private String featureChangeType;

    private Description description;

    public ExtendedProperties(final Map<String, Object> valuesAsMap) {
        //TODO:

        super(valuesAsMap, null);
    }
}
