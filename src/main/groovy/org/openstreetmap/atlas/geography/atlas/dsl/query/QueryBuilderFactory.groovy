package org.openstreetmap.atlas.geography.atlas.dsl.query

import org.openstreetmap.atlas.geography.atlas.dsl.field.Field
import org.openstreetmap.atlas.geography.atlas.dsl.query.analyzer.domain.Analysis
import org.openstreetmap.atlas.geography.atlas.dsl.query.difference.Difference
import org.openstreetmap.atlas.geography.atlas.dsl.query.explain.Explanation
import org.openstreetmap.atlas.geography.atlas.dsl.query.result.Result
import org.openstreetmap.atlas.geography.atlas.dsl.schema.AtlasSchema
import org.openstreetmap.atlas.geography.atlas.dsl.schema.table.AtlasTable
import org.openstreetmap.atlas.geography.atlas.dsl.selection.constraints.Constraint
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity

/**
 * An entry point for AQL that offeres easy access to query objects with static imports.
 *
 * @author Yazad Khambata
 */
class QueryBuilderFactory {

    static QueryBuilder builder() {
        new QueryBuilder()
    }

    //Statements
    static final Closure<QueryBuilder> select = { final Field... fields -> QueryBuilderFactory.builder().select(fields) }
    static final Closure<QueryBuilder> update = { final AtlasTable<AtlasEntity> table -> QueryBuilderFactory.builder().update(table) }
    static final Closure<QueryBuilder> delete = { final AtlasTable<AtlasEntity> table -> QueryBuilderFactory.builder().delete(table) }

    //Special case of NOT clause.
    static final Closure<Constraint> not = { final Constraint constraint -> QueryBuilder.not(constraint) }

    //Commands returning AtlasSchema
    static final Closure<AtlasSchema> using = { final String uri -> QueryBuilderFactory.builder().using(uri) }
    static final Closure<AtlasSchema> commit = { final QueryBuilder... queryBuilders -> QueryBuilderFactory.builder().commit(queryBuilders) }
    static final Closure<Analysis> analyze = { final queryOrBuilder -> QueryBuilderFactory.builder().explain(queryOrBuilder) }
    static final Closure<Analysis> explain = QueryBuilderFactory.analyze

    //Commands returning Result
    static final Closure<Result> exec = { QueryBuilder qb -> qb.buildQuery().execute() }
    static final Closure<Result> execute = QueryBuilderFactory.exec

    //Commands returning Result
    static final Closure<Difference> diff = { final AtlasSchema atlas1, final AtlasSchema atlas2 -> QueryBuilderFactory.builder().difference(atlas1, atlas2) }
    static final Closure<Difference> difference = QueryBuilderFactory.diff
}
