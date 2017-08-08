package org.openstreetmap.atlas.utilities.unicode;

import java.util.List;
import java.util.Optional;

import org.openstreetmap.atlas.utilities.unicode.Classification.CodeBlock;

/**
 * Responsible for reducing a CharSequence into a Classification object and transforming a
 * CharSequence into an equivalent number of Optional CodeBlock objects. If an Optional is not
 * present, that character could not be classified
 *
 * @author cstaylor
 */
public interface Classifier
{
    Classification classify(CharSequence sequence);

    List<Optional<CodeBlock>> transform(CharSequence sequence);
}
