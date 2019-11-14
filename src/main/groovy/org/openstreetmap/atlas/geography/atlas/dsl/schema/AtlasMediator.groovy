package org.openstreetmap.atlas.geography.atlas.dsl.schema

import groovy.transform.ToString
import org.apache.commons.lang3.builder.EqualsBuilder
import org.apache.commons.lang3.builder.HashCodeBuilder
import org.openstreetmap.atlas.geography.atlas.Atlas
import org.openstreetmap.atlas.geography.atlas.dsl.schema.uri.SchemeSupport

/**
 * The AtlasSchema and AtlasTables act as Colleagues.
 *
 * @author Yazad Khambata
 */
@ToString(includes = ["uri"])
class AtlasMediator {

    String uri

    Atlas atlas

    private AtlasMediator(final String uri, final Atlas atlas) {
        this.uri = uri
        this.atlas = atlas
    }

    AtlasMediator(String uri) {
        this(uri, loadAtlas(uri))
    }

    AtlasMediator(final Atlas atlas) {
        this(null, atlas)
    }

    AtlasMediator(final AtlasMediator atlasMediator) {
        this(atlasMediator.uri, atlasMediator.atlas)
    }

    protected static Atlas loadAtlas(String uri) {
        SchemeSupport.instance.load(uri)
    }

    @Override
    boolean equals(final Object that) {
        EqualsBuilder.reflectionEquals(this, that, "uri")
    }

    @Override
    int hashCode() {
        HashCodeBuilder.reflectionHashCode(this, "uri")
    }
}
