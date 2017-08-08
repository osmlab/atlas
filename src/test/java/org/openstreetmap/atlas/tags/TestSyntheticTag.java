package org.openstreetmap.atlas.tags;

import org.openstreetmap.atlas.tags.annotations.Tag;
import org.openstreetmap.atlas.tags.annotations.TagKey;

/**
 * Tag used for testing the synthetic attribute
 *
 * @author cstaylor
 */
@Tag(synthetic = true)
public interface TestSyntheticTag
{
    @TagKey
    String KEY = "replicant";
}
