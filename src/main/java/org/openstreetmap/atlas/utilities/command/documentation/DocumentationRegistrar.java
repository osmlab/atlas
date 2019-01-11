package org.openstreetmap.atlas.utilities.command.documentation;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.streaming.resource.InputStreamResource;
import org.openstreetmap.atlas.streaming.resource.StringResource;
import org.openstreetmap.atlas.utilities.tuples.Tuple;

/**
 * @author lcram
 */
public class DocumentationRegistrar
{
    private static final String DESCRIPTION_HEADER = "DESCRIPTION";

    private final Map<String, List<Tuple<DocumentationFormatType, String>>> sections;

    public DocumentationRegistrar()
    {
        this.sections = new LinkedHashMap<>();
    }

    public void addCodeLineToSection(final String section, final String codeLine)
    {
        final String capsSection = section.toUpperCase();
        if (!this.sections.containsKey(capsSection))
        {
            throw new CoreException("Section {} has not been added", capsSection);
        }
        final List<Tuple<DocumentationFormatType, String>> list = this.sections.get(capsSection);
        list.add(new Tuple<>(DocumentationFormatType.CODE, codeLine));
    }

    public void addManualPageSection(final String section)
    {
        final String capsSection = section.toUpperCase();
        if (this.sections.containsKey(capsSection))
        {
            throw new CoreException("Manpage section {} was already added", capsSection);
        }
        this.sections.put(capsSection, new ArrayList<>());
    }

    public void addManualPageSection(final String section,
            final InputStream sectionResourceFileStream)
    {
        final String capsSection = section.toUpperCase();
        if (this.sections.containsKey(capsSection))
        {
            throw new CoreException("Manpage section {} was already added", capsSection);
        }
        final StringResource resource = new StringResource();
        resource.copyFrom(new InputStreamResource(() -> sectionResourceFileStream));
        final String rawText = resource.all();
        final List<Tuple<DocumentationFormatType, String>> sectionContents = new ArrayList<>();

        StringBuilder paragraphBuilder = new StringBuilder();
        final String[] split = rawText.split(System.getProperty("line.separator"));
        for (final String line : split)
        {
            if (line.isEmpty())
            {
                sectionContents.add(new Tuple<>(DocumentationFormatType.PARAGRAPH,
                        paragraphBuilder.toString()));
                paragraphBuilder = new StringBuilder();
            }
            else if (line.startsWith("#"))
            {
                /*
                 * Close and add any in-progress paragraph. If the user was not in the middle of
                 * creating a paragraph, this is fine. Empty blocks will be filtered by downstream
                 * code.
                 */
                sectionContents.add(new Tuple<>(DocumentationFormatType.PARAGRAPH,
                        paragraphBuilder.toString()));
                paragraphBuilder = new StringBuilder();

                // scrub the '#' off the line
                final String scrubbedLine = line.substring(1);
                sectionContents.add(new Tuple<>(DocumentationFormatType.CODE, scrubbedLine));
            }
            else
            {
                paragraphBuilder.append(line + " ");
            }
        }
        // add last paragraph if one is left over
        sectionContents
                .add(new Tuple<>(DocumentationFormatType.PARAGRAPH, paragraphBuilder.toString()));

        this.sections.put(capsSection, sectionContents);
    }

    public void addParagraphToSection(final String section, final String paragraph)
    {
        final String capsSection = section.toUpperCase();
        if (!this.sections.containsKey(capsSection))
        {
            throw new CoreException("Section {} has not been added", capsSection);
        }
        final List<Tuple<DocumentationFormatType, String>> list = this.sections.get(capsSection);
        list.add(new Tuple<>(DocumentationFormatType.PARAGRAPH, paragraph));
    }

    public String getDescriptionHeader()
    {
        return DESCRIPTION_HEADER;
    }

    public List<Tuple<DocumentationFormatType, String>> getSectionContents(final String section)
    {
        return this.sections.get(section);
    }

    public Set<String> getSections()
    {
        return this.sections.keySet();
    }

    public boolean hasDescriptionSection()
    {
        return this.sections.keySet().contains(DESCRIPTION_HEADER);
    }
}
