package org.openstreetmap.atlas.geography.atlas.dsl.query.optimizer.optimization.impl

import org.openstreetmap.atlas.geography.atlas.dsl.query.*
import org.openstreetmap.atlas.geography.atlas.dsl.query.explain.Explanation
import org.openstreetmap.atlas.geography.atlas.dsl.query.optimizer.domain.OptimizationRequest
import org.openstreetmap.atlas.geography.atlas.dsl.schema.table.CommonFields
import org.openstreetmap.atlas.geography.atlas.dsl.selection.constraints.BasicConstraint
import org.openstreetmap.atlas.geography.atlas.dsl.selection.constraints.BinaryOperations
import org.openstreetmap.atlas.geography.atlas.dsl.selection.constraints.ScanType
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity
import org.openstreetmap.atlas.geography.atlas.items.Relation

import java.util.stream.Collectors

/**
 * Also called Optimization-2
 *
 * An optimization that can kick in,
 *
 * IFF,
 *  1. All constraints are based on ID_UNIQUE_INDEX - hasId(anId), hasIds(id1, id2, id3, ...), hasIds(innerSelect) is used
 *  2. Only or clause is used.
 *
 * @author Yazad Khambata
 */
@Singleton
class IdsInOptimization<E extends AtlasEntity> extends AbstractQueryOptimizationTransform<E> {
    @Override
    boolean areAdditionalChecksMet(final OptimizationRequest<E> optimizationRequest) {
        //All ORs
        //Only id as field and clauses allowed =, in, inner query (at least 2) - i.e Scan Type ID only.
        onlyOrsUsed(optimizationRequest) && onlyIdUniqueIndexScanTypeAvailable(optimizationRequest)
    }

    private boolean onlyOrsUsed(final OptimizationRequest<E> optimizationRequest) {
        final Set<Statement.Clause> andOrClausesInUse = andOrClausesInUse(optimizationRequest)
        andOrClausesInUse.size() == 1 && andOrClausesInUse.contains(Statement.Clause.OR)
    }

    boolean onlyIdUniqueIndexScanTypeAvailable(final OptimizationRequest<E> optimizationRequest) {
        final Set<ScanType> scanTypes = scanTypeAvailable(optimizationRequest)
        scanTypes.size() == 1 && scanTypes.contains(ScanType.ID_UNIQUE_INDEX)
    }


    @Override
    Query<E> applyTransformation(final OptimizationRequest<E> optimizationRequest) {
        final Query<E> query = optimizationRequest.getQuery()
        final ConditionalConstructList<Relation> conditionalConstructList = query.conditionalConstructList

        final List<Long> ids = (conditionalConstructList.stream().map { it.constraint }.map { it.valueToCheck }
                .flatMap {
                    if (it instanceof Number) {
                        return [it].stream()
                    }

                    if (it instanceof List) {
                        return it.stream()
                    }

                    if (it instanceof Long[]) {
                        return Arrays.stream(it)
                    }

                    if (it instanceof InnerSelectWrapper) {
                        return (it.identifiers as List).stream()
                    }

                    throw new IllegalArgumentException("${it} : ${it?.class}")
                }
                .collect(Collectors.toList()))

        query.shallowCopyWithConditionalConstructList(new ConditionalConstructList<E>([
                ConditionalConstruct.builder()
                        .constraint(
                                BasicConstraint.builder()
                                        .field(((CommonFields<AtlasEntity>)query.table).id)
                                        .operation(BinaryOperations.inside)
                                        .valueToCheck(ids as Long[])
                                        .bestCandidateScanType(ScanType.ID_UNIQUE_INDEX)
                                        .atlasEntityClass(query.table.entityClass)
                                        .build()
                        )
                        .clause(Statement.Clause.WHERE)
                        .build()
        ]))
    }
}
