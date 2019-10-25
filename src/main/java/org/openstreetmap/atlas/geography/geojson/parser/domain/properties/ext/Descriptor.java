package org.openstreetmap.atlas.geography.geojson.parser.domain.properties.ext;

import java.io.Serializable;

import org.openstreetmap.atlas.geography.geojson.parser.domain.annotation.Foreign;

/**
 * @author Yazad Khambata
 */
@Foreign
public class Descriptor implements Serializable
{
    private String name;
    private String type;
    private String key;
    private String value;
    private String originalValue;
    private String position;
    private String beforeView;
    private String afterView;

}
