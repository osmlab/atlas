package org.openstreetmap.atlas.geography.geojson.parser.domain.properties.ext;

import org.openstreetmap.atlas.geography.geojson.parser.domain.annotation.Foreign;
import org.openstreetmap.atlas.geography.geojson.parser.domain.properties.Properties;

/**
 * @author Yazad Khambata
 */
@Foreign
public class ExtendedProperties extends Properties
{
    private String featureChangeType;

    private Metadata metadata;
}
