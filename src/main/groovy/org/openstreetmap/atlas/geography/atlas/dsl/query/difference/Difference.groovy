package org.openstreetmap.atlas.geography.atlas.dsl.query.difference

import groovy.transform.builder.Builder
import org.openstreetmap.atlas.geography.atlas.change.Change
import org.openstreetmap.atlas.geography.atlas.dsl.schema.AtlasSchema

import java.util.stream.Collectors

/**
 * Domain representing the difference.
 *
 * @author Yazad Khambata
 */
@Builder
class Difference {
    AtlasSchema atlasSchema1
    AtlasSchema atlasSchema2

    Change change

    @Override
    String toString() {
        """
================================================================================================
Atlas 1                   : ${atlasSchema1}
Atlas 2                   : ${atlasSchema2}

Has Difference            : ${change != null}

Differences               :
${change.changes().map {featureChange -> "\t${featureChange}" }.collect(Collectors.joining("\n")) }
================================================================================================
"""
    }
}
