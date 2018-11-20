package org.openstreetmap.atlas.utilities.command.documentation.multistring;

import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.TypeElement;

/**
 * Process the {@link MultiString} annotation. This inserts the value of a Javadoc comment into the
 * attached {@link String} at compile time.
 *
 * @author lcram
 */
@SupportedAnnotationTypes({
        "org.openstreetmap.atlas.utilities.command.documentation.multistring.MultiString" })
public class MultiStringProcessor extends AbstractProcessor
{
    @Override
    public void init(final ProcessingEnvironment processingEnvironment)
    {
        super.init(processingEnvironment);
    }

    @Override
    public boolean process(final Set<? extends TypeElement> annotations,
            final RoundEnvironment roundEnvironment)
    {
        // TODO Auto-generated method stub
        return false;
    }
}
