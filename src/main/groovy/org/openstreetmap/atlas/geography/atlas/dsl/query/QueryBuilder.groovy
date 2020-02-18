package org.openstreetmap.atlas.geography.atlas.dsl.query

import groovy.transform.ToString
import org.openstreetmap.atlas.geography.atlas.dsl.query.analyzer.QueryAnalyzer
import org.openstreetmap.atlas.geography.atlas.dsl.query.analyzer.domain.Analysis
import org.openstreetmap.atlas.geography.atlas.dsl.query.analyzer.impl.QueryAnalyzerImpl
import org.openstreetmap.atlas.geography.atlas.dsl.query.difference.Difference
import org.openstreetmap.atlas.geography.atlas.dsl.query.difference.DifferenceGenerator
import org.openstreetmap.atlas.geography.atlas.dsl.query.explain.ExplainerImpl
import org.openstreetmap.atlas.geography.atlas.dsl.query.optimizer.RuleBasedOptimizerImpl
import org.openstreetmap.atlas.geography.atlas.dsl.selection.constraints.Constraint
import org.openstreetmap.atlas.geography.atlas.dsl.field.Field
import org.openstreetmap.atlas.geography.atlas.dsl.mutant.Mutant
import org.openstreetmap.atlas.geography.atlas.dsl.query.Statement.Clause
import org.openstreetmap.atlas.geography.atlas.dsl.schema.AtlasSchema
import org.openstreetmap.atlas.geography.atlas.dsl.schema.mutant.MutantAtlasMediator
import org.openstreetmap.atlas.geography.atlas.dsl.schema.table.AtlasTable
import org.openstreetmap.atlas.geography.atlas.dsl.selection.constraints.NotConstraint
import org.openstreetmap.atlas.geography.atlas.dsl.util.Valid
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity

/**
 * The actual query builder, accessed via the corresponding builder-factory.
 *
 * @author Yazad Khambata
 */
@ToString
class QueryBuilder {

    Statement base

    /**
     * Non-condition clauses
     */
    Map<Clause, Object> clauses = [:]

    List<ConditionalConstruct> conditionalConstructs = new ConditionalConstructList()

    private newQuerySetup(final Statement base) {
        Valid.isTrue this.base == null, "BUG! base is not NULL. ${this.base}, trying to set base to: ${base}."
        Valid.isTrue clauses.isEmpty(), "BUG! clauses must be empty. ${clauses}"
        Valid.isTrue conditionalConstructs.isEmpty(), "BUG! conditionalConstructs must be empty. ${conditionalConstructs}"

        this.base = base
    }

    Closure<AtlasSchema> using = { String uri ->
        newQuerySetup(Statement.USING)

        clauses[Clause.__IT__] = uri

        new AtlasSchema(uri)
    }

    def select = { Field... fields ->
        newQuerySetup(Statement.SELECT)

        this.clauses[Statement.Clause.__IT__] = Arrays.asList(fields)

        this
    }

    private onSchema = { Statement statement, Clause clause, AtlasTable<AtlasEntity> table ->
        Valid.isTrue statement == base, "Statement mismatch error."
        Valid.isTrue statement in [Statement.SELECT, Statement.UPDATE, Statement.DELETE], "${statement} unexpected"

        Valid.isTrue statement.isClauseAllowed(clause), "Unexpected clause ${clause} in ${base} statement."

        Valid.notEmpty table, "Table is NULL! statement: ${statement}; clause: ${clause}"

        clauses[clause] = table

        this
    }

    def from = { AtlasTable<AtlasEntity> table ->
        Valid.notEmpty this.base, "base statement is NULL."

        onSchema this.base, Clause.FROM, table
    }

    private condition = { Clause clause, Constraint constraint ->
        Valid.isTrue base in [Statement.SELECT, Statement.UPDATE, Statement.DELETE]

        final ConditionalConstruct conditionalConstruct =
                ConditionalConstruct.builder().clause(clause).constraint(constraint).build()

        conditionalConstructs.add(conditionalConstruct)

        this
    }


    def where = { Constraint constraint ->
        condition Clause.WHERE, constraint
    }

    def and = { Constraint constraint ->
        condition Clause.AND, constraint
    }

    def or = { Constraint constraint ->
        condition Clause.OR, constraint
    }

    public static not = { Constraint constraint ->
        NotConstraint.from(constraint)
    }

    def limit = { long to ->
        Valid.isTrue base == Statement.SELECT

        this.clauses[Statement.Clause.LIMIT] = to

        this
    }

    def update = { AtlasTable<AtlasEntity> table ->
        newQuerySetup(Statement.UPDATE)

        onSchema this.base, Clause.__IT__, table
    }

    def set = { Mutant... mutants ->
        Valid.isTrue base == Statement.UPDATE, "Set can only be used with ${Statement.UPDATE} statement, not ${base}."

        clauses[Clause.SET] = Arrays.asList(mutants)

        this
    }

    def delete = { AtlasTable<AtlasEntity> table ->
        newQuerySetup(Statement.DELETE)

        onSchema this.base, Clause.__IT__, table
    }

    Closure<AtlasSchema> commit = { final QueryBuilder...queryBuilders ->
        newQuerySetup(Statement.COMMIT)

        clauses[Clause.__IT__] = [queryBuilders: queryBuilders]

        new AtlasSchema(new MutantAtlasMediator(queryBuilders))
    }

    Closure<Analysis> explain = { queryOrBuilder ->
        newQuerySetup(Statement.EXPLAIN)
        clauses[Clause.__IT__] = [queryOrBuilder: queryOrBuilder]

        final QueryAnalyzer queryAnalyzer = new QueryAnalyzerImpl(ExplainerImpl.instance, RuleBasedOptimizerImpl.defaultOptimizer())

        final Analysis analysis = queryAnalyzer.analyze(queryOrBuilder)
        analysis.dump()

        analysis
    }

    Closure<Difference> difference = { final AtlasSchema atlas1, final AtlasSchema atlas2 ->
        newQuerySetup(Statement.DIFFERENCE)

        clauses[Clause.__IT__] = [atlas1: atlas1, atlas2: atlas2]

        DifferenceGenerator.getInstance().generateAndDumpDifference(atlas1, atlas2)
    }

    private Select buildSelectQuery() {
        Select.builder()
                .fieldsToSelect(clauses[Clause.__IT__])
                .table(clauses[Clause.FROM])
                .conditionalConstructList(conditionalConstructs)
                .limit(clauses[Clause.LIMIT])
                .build()
    }

    private Update buildUpdateQuery() {
        Update.builder()
                .table(clauses[Clause.__IT__])
                .mutants(clauses[Clause.SET])
                .conditionalConstructList(conditionalConstructs)
                .build()
    }

    private Delete buildDeleteQuery() {
        Delete.builder()
                .table(clauses[Clause.__IT__])
                .conditionalConstructList(conditionalConstructs)
                .build()
    }

    private Map<Statement, Closure<Query>> statementToQueryBuildMapping = [
            (Statement.SELECT): { return buildSelectQuery() },
            (Statement.UPDATE): { return buildUpdateQuery() },
            (Statement.DELETE): { return buildDeleteQuery() },
    ]

    Query buildQuery() {
        statementToQueryBuildMapping[base]()
    }

    @Override
    String toString() {
        return buildQuery().toPrettyString()
    }
}