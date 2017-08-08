package org.openstreetmap.atlas.utilities.unicode;

import java.nio.file.Path;

import org.openstreetmap.atlas.streaming.resource.ClassResource;
import org.openstreetmap.atlas.streaming.resource.File;
import org.openstreetmap.atlas.streaming.resource.LineFilteredResource;
import org.openstreetmap.atlas.streaming.resource.Resource;
import org.openstreetmap.atlas.utilities.collections.Iterables;
import org.openstreetmap.atlas.utilities.collections.StringList;
import org.openstreetmap.atlas.utilities.unicode.Classification.CodeBlock;

/**
 * This will load both classifications and ignores from a CSV file in the format:
 * <p>
 * START HEX CODE, END HEX CODE, CLASSIFICATION, DESCRIPTION
 * <p>
 * Note: IGNORE is treated as a special CLASSIFICATION value that will add the value to the ignore
 * list
 *
 * @author cstaylor
 */
public class LoadingClassifier extends AbstractClassifier
{
    private static final int START_INDEX = 0;

    private static final int END_INDEX = 1;

    private static final int CLASSIFICATION_INDEX = 2;

    private static final int DESCRIPTION_INDEX = 3;

    private final Resource resource;

    public LoadingClassifier()
    {
        this("org/openstreetmap/atlas/utilities/unicode/unicode.defaults");
    }

    public LoadingClassifier(final Path path)
    {
        this(new File(path.toFile()));
    }

    public LoadingClassifier(final Resource resource)
    {
        this.resource = resource;
        initialize();
    }

    public LoadingClassifier(final String classResource)
    {
        this(new ClassResource(classResource));
    }

    @Override
    protected LoadingClassifier initialize()
    {
        if (this.resource != null)
        {
            /*
             * These are the codeblock mappings
             */
            Iterables
                    .stream(new LineFilteredResource(this.resource,
                            line -> !line.contains("IGNORE")).lines())
                    .map(line -> StringList.split(line, ",")).forEach(list ->
                    {
                        add(list.get(DESCRIPTION_INDEX), Integer.decode(list.get(START_INDEX)),
                                Integer.decode(list.get(END_INDEX)),
                                CodeBlock.valueOf(list.get(CLASSIFICATION_INDEX).toUpperCase()));
                    });

            /*
             * These are the ignores
             */
            Iterables
                    .stream(new LineFilteredResource(this.resource, line -> line.contains("IGNORE"))
                            .lines())
                    .map(line -> StringList.split(line, ",")).forEach(list ->
                    {
                        ignore(list.get(DESCRIPTION_INDEX), Integer.decode(list.get(START_INDEX)),
                                Integer.decode(list.get(END_INDEX)));
                    });
        }
        return this;
    }
}
