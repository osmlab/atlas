package org.openstreetmap.atlas.geography.atlas.dsl.query.optimizer.optimization.impl

import org.openstreetmap.atlas.geography.GeometricSurface
import org.openstreetmap.atlas.geography.MultiPolygon
import org.openstreetmap.atlas.geography.atlas.dsl.query.ConditionalConstructList
import org.openstreetmap.atlas.geography.atlas.dsl.query.Query
import org.openstreetmap.atlas.geography.atlas.dsl.query.optimizer.domain.OptimizationRequest
import org.openstreetmap.atlas.geography.atlas.dsl.selection.constraints.Constraint
import org.openstreetmap.atlas.geography.atlas.dsl.selection.constraints.ScanType
import org.openstreetmap.atlas.geography.atlas.dsl.selection.constraints.polygon.GeometricSurfaceSupport
import org.openstreetmap.atlas.geography.atlas.dsl.util.Valid
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity

import java.util.stream.Collectors

/**
 * Also called Optimization-4, which removes the polygons with a within check that are outside the
 * bounds of the atlas.
 *
 * @author Yazad Khambata
 */
@Singleton
class GeometricSurfacesOverlapOptimization<E extends AtlasEntity> extends AbstractQueryOptimizationTransform<E> {
    @Override
    boolean areAdditionalChecksMet(final OptimizationRequest<E> optimizationRequest) {
        optimizationRequest.query.conditionalConstructList.stream()
                .filter { conditionalConstruct ->
                    conditionalConstruct.constraint.bestCandidateScanType == ScanType.SPATIAL_INDEX
                }
                .findAny()
                .isPresent()
    }

    @Override
    Query<E> applyTransformation(final OptimizationRequest<E> optimizationRequest) {
        final Query<E> query = optimizationRequest.getQuery()
        final GeometricSurface bounds = query.bounds()

        final ConditionalConstructList<E> optimizedConditionalConstructList = query.conditionalConstructList.stream().map { conditionalConstruct ->
            def constraint = conditionalConstruct.constraint

            if (constraint.bestCandidateScanType != ScanType.SPATIAL_INDEX) {
                //NOP
                return conditionalConstruct
            }

            //Check if value needs to be changed and updated it.
            final Object valueToCheck = constraint.valueToCheck

            //Value to check must be List of List of List of BigDecimal or a MultiPolygon (GeometricSurface)
            Valid.isTrue valueToCheck instanceof List || valueToCheck instanceof MultiPolygon

            final Optional<GeometricSurface> optionalGeometricSurface = GeometricSurfaceSupport.instance.toGeometricSurface(valueToCheck, bounds)

            final Constraint<E> copiedConstraint = optionalGeometricSurface.map { geometricSurface ->
                constraint.deepCopyWithNewValueToCheck(geometricSurface)
            }.orElseThrow { -> new UnsupportedOperationException("") }

            def copiedConditionalConstruct = conditionalConstruct.shallowCopyWithConstraintOverride(copiedConstraint)
            copiedConditionalConstruct
        }.collect(
                Collectors.collectingAndThen(
                        Collectors.toList(),
                        { listOfConditionalConstructs -> new ConditionalConstructList<>(listOfConditionalConstructs) }
                )
        )

        final Query<E> optimizedQuery = query.shallowCopyWithConditionalConstructList(optimizedConditionalConstructList)

        optimizedQuery
    }
}
