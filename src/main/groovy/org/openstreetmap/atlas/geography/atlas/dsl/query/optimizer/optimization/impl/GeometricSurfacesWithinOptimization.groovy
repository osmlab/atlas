package org.openstreetmap.atlas.geography.atlas.dsl.query.optimizer.optimization.impl

import org.openstreetmap.atlas.geography.atlas.dsl.query.ConditionalConstruct
import org.openstreetmap.atlas.geography.atlas.dsl.query.ConditionalConstructList
import org.openstreetmap.atlas.geography.atlas.dsl.query.Query
import org.openstreetmap.atlas.geography.atlas.dsl.query.Statement
import org.openstreetmap.atlas.geography.atlas.dsl.query.optimizer.domain.OptimizationRequest
import org.openstreetmap.atlas.geography.atlas.dsl.schema.table.CommonFields
import org.openstreetmap.atlas.geography.atlas.dsl.selection.constraints.BasicConstraint
import org.openstreetmap.atlas.geography.atlas.dsl.selection.constraints.BinaryOperations
import org.openstreetmap.atlas.geography.atlas.dsl.selection.constraints.ScanType
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity
import org.openstreetmap.atlas.geography.atlas.items.Relation

import java.util.stream.Collectors

/**
 * Also called Optimization-3, combines multiple within checks to one to encourage use of the
 * spatial index.
 *
 * An optimization that can kick in,
 *
 * IFF,
 * 1. All constraints are based on SPATIAL_INDEX - isWithin(GeometricSurface)
 * 2. Only or clause is used.
 *
 * @author Yazad Khambata
 */
@Singleton
class GeometricSurfacesWithinOptimization<E extends AtlasEntity> extends AbstractQueryOptimizationTransform<E> {
    @Override
    boolean areAdditionalChecksMet(final OptimizationRequest<E> optimizationRequest) {
        //All ORs
        //Only id as field and clauses allowed =, in, inner query (at least 2) - i.e Scan Type ID only.
        onlyOrsUsed(optimizationRequest) && onlySpatialIndexScanTypeAvailable(optimizationRequest)
    }

    private boolean onlyOrsUsed(final OptimizationRequest<E> optimizationRequest) {
        final Set<Statement.Clause> andOrClausesInUse = andOrClausesInUse(optimizationRequest)
        andOrClausesInUse.size() == 1 && andOrClausesInUse.contains(Statement.Clause.OR)
    }

    boolean onlySpatialIndexScanTypeAvailable(final OptimizationRequest<E> optimizationRequest) {
        final Set<ScanType> scanTypes = scanTypeAvailable(optimizationRequest)
        scanTypes.size() == 1 && scanTypes.contains(ScanType.SPATIAL_INDEX)
    }


    @Override
    Query<E> applyTransformation(final OptimizationRequest<E> optimizationRequest) {
        final Query<E> query = optimizationRequest.getQuery()
        final ConditionalConstructList<Relation> conditionalConstructList = query.conditionalConstructList

        final List<List<BigDecimal>> combinedGeometricSurface = (conditionalConstructList.stream().map {
            it.constraint
        }.map { it.valueToCheck }
                .flatMap { List<List<BigDecimal>> geometricSurface -> geometricSurface.stream() }
                .collect(Collectors.toList()))

        query.shallowCopyWithConditionalConstructList(new ConditionalConstructList<E>([
                ConditionalConstruct.builder()
                        .constraint(
                                BasicConstraint.builder()
                                        .field(((CommonFields<AtlasEntity>) query.table)._)
                                        .operation(BinaryOperations.inside)
                                        .valueToCheck(combinedGeometricSurface)
                                        .bestCandidateScanType(ScanType.SPATIAL_INDEX)
                                        .atlasEntityClass(query.table.entityClass)
                                        .build()
                        )
                        .clause(Statement.Clause.WHERE)
                        .build()
        ]))
    }
}
