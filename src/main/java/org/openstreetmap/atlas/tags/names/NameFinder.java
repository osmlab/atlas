package org.openstreetmap.atlas.tags.names;

import static org.openstreetmap.atlas.geography.atlas.items.Relation.RELATION_ID_COMPARATOR;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.geography.atlas.items.Relation;
import org.openstreetmap.atlas.locale.IsoLanguage;
import org.openstreetmap.atlas.tags.Taggable;
import org.openstreetmap.atlas.tags.Taggable.TagSearchOption;
import org.openstreetmap.atlas.tags.annotations.validation.Validators;
import org.openstreetmap.atlas.utilities.collections.EnhancedCollectors;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

/**
 * Responsible for finding names in AtlasEntities
 *
 * @author cstaylor
 * @author Sid
 */
public class NameFinder implements Serializable
{
    /**
     * Standard set of name tags excluding the reference tags
     */
    public static final ImmutableList<Class<?>> STANDARD_TAGS_NON_REFERENCE = new ImmutableList.Builder<Class<?>>()
            .add(NameTag.class, InternationallyKnownAsTag.class, NationallyKnownAsTag.class,
                    RegionallyKnownAsTag.class, LocallyKnownAsTag.class,
                    HistoricallyKnownAsTag.class, AlternativeNameTag.class, ShortNameTag.class,
                    OfficialNameTag.class)
            .build();

    /**
     * Standard set of reference tags
     */
    public static final ImmutableList<Class<?>> STANDARD_TAGS_REFERENCE = new ImmutableList.Builder<Class<?>>()
            .add(ReferenceTag.class, InternationallyReferencedAsTag.class,
                    NationallyReferencedAsTag.class, RegionallyReferencedAsTag.class,
                    LocallyReferencedAsTag.class, HistoricallyReferencedAsTag.class)
            .build();

    /**
     * Standard set of name tags in order of priority per mcuthbert's NameTag class
     */
    public static final ImmutableList<Class<?>> STANDARD_TAGS = new ImmutableList.Builder<Class<?>>()
            .addAll(STANDARD_TAGS_NON_REFERENCE).addAll(STANDARD_TAGS_REFERENCE).build();

    public static final ImmutableList<String> STANDARD_TAG_KEYS;
    private static final long serialVersionUID = -7268140468931884651L;
    static
    {
        STANDARD_TAG_KEYS = STANDARD_TAGS.stream().map(Validators::findTagNameIn)
                .collect(EnhancedCollectors.toImmutableList());
    }

    private transient IsoLanguage language;

    private final LinkedHashSet<Class<?>> priorityOrderOfTagNames;

    private TagSearchOption searchOption = TagSearchOption.DEFAULT;

    /**
     * Returns a new NameFinder initialized with the following tags in priority order:
     * <ol>
     * <li>NameTag</li>
     * <li>InternationallyKnownAsTag</li>
     * <li>NationallyKnownAsTag</li>
     * <li>RegionallyKnownAsTag</li>
     * <li>LocallyKnownAsTag</li>
     * <li>HistoricallyKnownAsTag</li>
     * <li>AlternativeNameTag</li>
     * <li>ShortNameTag</li>
     * <li>OfficialNameTag</li>
     * <li>ReferenceTag</li>
     * <li>InternationallyReferencedAsTag</li>
     * <li>NationallyReferencedAsTag</li>
     * <li>RegionallyReferencedAsTag</li>
     * <li>LocallyReferencedAsTag</li>
     * <li>HistoricallyReferencedAsTag</li>
     * </ol>
     * <p>
     * Note: This order was originally written by mcuthbert for his NameTag class.
     *
     * @param language
     *            the language we should use for localizable tags when finding their values
     * @return the initialized NameFinder instance
     */
    public static NameFinder createStandardSet(final IsoLanguage language)
    {
        return new NameFinder().withTags(STANDARD_TAGS).inLanguage(language);
    }

    private static List<Taggable> children(final AtlasEntity entity)
    {
        final List<Taggable> taggables = new ArrayList<>();
        taggables.add(entity);
        final List<Relation> relations = new ArrayList<>(entity.relations());
        relations.sort(RELATION_ID_COMPARATOR);
        Iterables.addAll(taggables, relations);
        return taggables;
    }

    public NameFinder()
    {
        this.priorityOrderOfTagNames = new LinkedHashSet<>();
        this.language = null;
    }

    public Map<Class<?>, String> all(final Taggable taggable)
    {
        final Map<Class<?>, String> returnValue = new HashMap<>();
        for (final Class<?> tagClass : this.priorityOrderOfTagNames)
        {
            taggable.getTag(tagClass, Optional.ofNullable(this.language), this.searchOption)
                    .ifPresent(tagValue ->
                    {
                        returnValue.put(tagClass, tagValue);
                    });
        }
        return returnValue;
    }

    public Optional<String> best(final AtlasEntity entity)
    {
        return children(entity).stream().map(this::best).filter(Optional::isPresent)
                .map(Optional::get).findFirst();
    }

    public Optional<String> best(final Taggable taggable)
    {
        return this.priorityOrderOfTagNames.stream()
                .map(tagClass -> taggable.getTag(tagClass, Optional.ofNullable(this.language),
                        this.searchOption))
                .filter(Optional::isPresent).map(Optional::get).findFirst();
    }

    public NameFinder forceLocalized()
    {
        this.searchOption = TagSearchOption.FORCE_ALL_LOCALIZED_ONLY;
        return this;
    }

    public ImmutableCollection<Class<?>> getTagNames()
    {
        return ImmutableList.<Class<?>> builder().addAll(this.priorityOrderOfTagNames).build();
    }

    public NameFinder inLanguage(final IsoLanguage language)
    {
        this.language = language;
        return this;
    }

    public NameFinder localizedOnly()
    {
        this.searchOption = TagSearchOption.LOCALIZED_ONLY;
        return this;
    }

    public NameFinder withTags(final Class<?>... tagClasses)
    {
        for (final Class<?> tagClass : tagClasses)
        {
            this.priorityOrderOfTagNames.add(tagClass);
        }
        return this;
    }

    public NameFinder withTags(final Iterable<Class<?>> tagClasses)
    {
        for (final Class<?> tagClass : tagClasses)
        {
            this.priorityOrderOfTagNames.add(tagClass);
        }
        return this;
    }

    private void readObject(final ObjectInputStream stream)
            throws IOException, ClassNotFoundException
    {
        stream.defaultReadObject();
        final String iso2 = (String) stream.readObject();
        if (iso2 != null)
        {
            this.language = IsoLanguage.forLanguageCode(iso2).orElse(null);
        }
    }

    private void writeObject(final ObjectOutputStream stream) throws IOException
    {
        stream.defaultWriteObject();
        final String iso2 = this.language == null ? null : this.language.getLanguageCode();
        stream.writeObject(iso2);
    }
}
