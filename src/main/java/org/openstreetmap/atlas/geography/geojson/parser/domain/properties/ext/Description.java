package org.openstreetmap.atlas.geography.geojson.parser.domain.properties.ext;

import org.openstreetmap.atlas.geography.geojson.parser.domain.annotation.Foreign;

import java.io.Serializable;
import java.util.List;

/**
 * @author Yazad Khambata
 */
@Foreign
public class Description implements Serializable {
    private String type;
    private List<Descriptor> descriptors;
}
