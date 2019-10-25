package org.openstreetmap.atlas.geography.geojson.parser.domain.base.type;

import java.util.Arrays;

import org.apache.commons.lang3.Validate;

import com.google.common.collect.Streams;

/**
 * @author Yazad Khambata
 */
public final class TypeUtil
{
    public static Type identifyStandardType(final String typeAsStr)
    {
        Validate.notEmpty(typeAsStr, "typeAsStr is EMPTY!");
        
        final Type identifiedType = Streams.concat(Arrays.stream(FeatureType.values()),
                Arrays.stream(GeometryType.values())).map(type -> (Type) type).filter(
                type -> type.getTypeValue().equals(typeAsStr)).findFirst().orElseThrow(
                () -> new IllegalArgumentException(typeAsStr));
        
        return identifiedType;
    }
    
    private TypeUtil()
    {
    }
}
