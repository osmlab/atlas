package org.openstreetmap.atlas.geography.atlas.dsl.schema.table

import groovy.transform.EqualsAndHashCode
import org.openstreetmap.atlas.geography.atlas.Atlas
import org.openstreetmap.atlas.geography.atlas.dsl.field.Field
import org.openstreetmap.atlas.geography.atlas.dsl.schema.AtlasMediator
import org.openstreetmap.atlas.geography.atlas.dsl.schema.table.setting.TableSetting
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity

import java.util.function.Predicate

/**
 * Abstraction of an AQL table.
 *
 * @author Yazad Khambata
 */
@EqualsAndHashCode(includeFields = true, includes = ["atlasMediator", "atlasTableSettings"], callSuper = true)
abstract class BaseTable<E extends AtlasEntity> extends CommonFields<E> implements AtlasTable<E> {
    /**
     * This field is NULLABLE - in case of static references of AtlasTable which doesn't link to an AtlasSchema.
     */
    private AtlasMediator atlasMediator

    private TableSetting atlasTableSettings

    BaseTable(final AtlasMediator atlasMediator, final TableSetting tableSetting) {
        super(tableSetting.memberClass)

        this.atlasMediator = atlasMediator
        this.atlasTableSettings = tableSetting
    }

    AtlasMediator getAtlasMediator() {
        atlasMediator
    }

    protected Atlas getAtlas() {
        this.atlasMediator.atlas
    }

    @Override
    TableSetting getTableSetting() {
        atlasTableSettings
    }

    @Override
    Iterable<E> getAll() {
        atlasTableSettings.getAll(this.atlas)
    }

    @Override
    Iterable<E> getAllMatching(final Predicate<E> predicate) {
        atlasTableSettings.getAll(this.atlas, predicate)
    }

    @Override
    E getById(final long id) {
        atlasTableSettings.getById(this.atlas, id)
    }

    @Override
    Class<E> getEntityClass() {
        atlasTableSettings.memberClass
    }

    @Override
    Map<String, Field> getAllFields() {
        final Map<String, Field> fieldMap = super.getAllFields(this)
        fieldMap
    }
}
