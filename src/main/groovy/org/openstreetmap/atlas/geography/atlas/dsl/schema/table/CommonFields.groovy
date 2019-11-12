package org.openstreetmap.atlas.geography.atlas.dsl.schema.table

import groovy.transform.EqualsAndHashCode
import org.apache.commons.lang3.tuple.Pair
import org.openstreetmap.atlas.geography.GeometricSurface
import org.openstreetmap.atlas.geography.atlas.complete.CompleteEntity
import org.openstreetmap.atlas.geography.atlas.dsl.selection.constraints.Constraint
import org.openstreetmap.atlas.geography.atlas.dsl.selection.constraints.ScanType
import org.openstreetmap.atlas.geography.atlas.dsl.field.Field
import org.openstreetmap.atlas.geography.atlas.dsl.field.ItselfField
import org.openstreetmap.atlas.geography.atlas.dsl.field.SelectOnlyField
import org.openstreetmap.atlas.geography.atlas.dsl.field.StandardField
import org.openstreetmap.atlas.geography.atlas.dsl.field.TagsField
import org.openstreetmap.atlas.geography.atlas.dsl.mutant.Mutant
import org.openstreetmap.atlas.geography.atlas.dsl.query.InnerSelectWrapper
import org.openstreetmap.atlas.geography.atlas.dsl.query.QueryBuilder
import org.openstreetmap.atlas.geography.atlas.dsl.util.Valid
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity

import java.util.stream.Collectors
import java.util.stream.Stream

/**
 * Composition of common fields in ALL Atlas Tables.
 *
 * @author Yazad Khambata
 */
@EqualsAndHashCode(includeFields = true, includes = ["atlasEntityClass"])
class CommonFields<E extends AtlasEntity> {

    private Class<E> atlasEntityClass

    CommonFields(final Class<E> atlasEntityClass) {
        this.atlasEntityClass = atlasEntityClass
    }

    ItselfField _ = new ItselfField()

    private ItselfField itself = _

    StandardField identifier = new StandardField("identifier")
    StandardField id = identifier
    StandardField osmIdentifier = new StandardField("osmIdentifier")
    StandardField osmId = osmIdentifier
    TagsField<CompleteEntity> tags = new TagsField<>("tags")
    SelectOnlyField osmTags = null
    SelectOnlyField rawGeometry = null

    SelectOnlyField type = null
    StandardField lastEdit = null
    StandardField lastUserIdentifier = null

    StandardField lastUserName = null
    SelectOnlyField relations = null
    StandardField bounds = null

    Constraint hasId(final Long id) {
        identifier.has(eq: id, ScanType.ID_UNIQUE_INDEX, atlasEntityClass)
    }

    Constraint hasOsmId(final Long osmId) {
        osmIdentifier.has(eq: osmId, atlasEntityClass)
    }

    Constraint hasIds(final Long...ids) {
        identifier.has(in: ids, ScanType.ID_UNIQUE_INDEX, atlasEntityClass)
    }

    Constraint hasOsmIds(final Long...osmIds) {
        osmIdentifier.has(in: osmIds, atlasEntityClass)
    }

    def <E extends AtlasEntity> Constraint hasIds(final QueryBuilder selectInnerQuery) {
        final InnerSelectWrapper innerSelectWrapper = InnerSelectWrapper.from(selectInnerQuery)

        //Needless to say that the Operation.inner_query will be used only if not indexed.
        identifier.has(inner_query: innerSelectWrapper, ScanType.ID_UNIQUE_INDEX, atlasEntityClass)
    }

    Constraint hasTag(final Map<String, Object> params) {
        return tags.has(tag: params, atlasEntityClass)
    }

    Constraint hasTag(final String key) {
        tags.has(tag: key, atlasEntityClass)
    }

    Constraint hasTagLike(final Map paramsExactKeyAndRegexValue) {
        tags.has(tag_like: paramsExactKeyAndRegexValue, atlasEntityClass)
    }

    Constraint hasTagLike(final String keyRegex) {
        tags.has(tag_like: keyRegex, atlasEntityClass)
    }

    Constraint hasLastUserName(final String userName) {
        lastUserName.has(eq: userName, atlasEntityClass)
    }

    Constraint hasLastUserNameLike(final String userNameRegex) {
        lastUserName.has(like: userNameRegex, atlasEntityClass)
    }

    /**
     * @param geometricSurface - expects [ [ [x, y], [a, b] ] ]
     * @return
     */
    Constraint isWithin(final List<List<BigDecimal>> geometricSurface) {
        this.itself.was(within: geometricSurface, ScanType.SPATIAL_INDEX, atlasEntityClass)
    }

    Constraint isWithin(final GeometricSurface geometricSurface) {
        this.itself.was(within: geometricSurface, ScanType.SPATIAL_INDEX, atlasEntityClass)
    }

    Mutant addTag(final Map<String, String> tagToAdd) {
        Valid.isTrue tagToAdd.size() == 1, "add accepts one tag key/value at a time, invoke addTag multiple times to add more tags."

        this.tags.add(tagToAdd)
    }

    Mutant deleteTag(final String key) {
        this.tags.remove(key)
    }

    /**
     * Alias for deleting tags.
     */
    def removeTag = this.&deleteTag

    /**
     * Automatically initialize the fields in the table.
     *
     * @param atlasTable - the table whose fields need to be auto initialized.
     */
    void autoSetFields(final AtlasTable atlasTable) {
        final Map<String, Class<? extends Field>> filedNamesToAutoSet = streamFields(atlasTable)
                .filter { prop -> prop.getProperty(atlasTable) == null }
                .map { prop -> Pair.of(prop.name, prop.getType()) }
                .collect(Collectors.toMap({ pair -> pair.key}, { pair -> pair.value}))

        filedNamesToAutoSet.entrySet().stream()
                .forEach { pair ->
            final String fieldName = pair.key
            final Class type = pair.value

            def instance = type.newInstance(fieldName)
            atlasTable[fieldName] = instance
        }
    }

    private Stream<Field> streamFields(final AtlasTable atlasTable) {
        atlasTable.getMetaClass().getProperties().stream()
                .filter { prop -> prop.getName() != "class" }
                .filter { prop -> prop.getType() in [StandardField, SelectOnlyField] }
    }

    @Override
    String toString() {
        def fieldsAsStr = this.getMetaClass().getProperties().stream()
                .filter({ prop -> Field.isAssignableFrom(prop.type) })
                .map({ prop -> "${prop.name} ${prop.type.simpleName}" })
                .collect(Collectors.joining(", "))

        "${this.class.simpleName} ( ${fieldsAsStr} )"
    }

    Map<String, Field> getAllFields(final AtlasTable atlasTable) {
        streamFields(atlasTable)
                .map { prop -> prop.getProperty(atlasTable) }
                .distinct() //needed to remove alias fields like identifier and osmIdentifier.
                .map { prop -> new Tuple2<>(prop.name, prop) }
                .collect(Collectors.toMap({ t2 -> t2.first}, { t2 -> t2.second }))
    }
}
