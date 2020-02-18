package org.openstreetmap.atlas.geography.atlas.dsl.query.analyzer.domain

import groovy.transform.ToString
import groovy.transform.builder.Builder
import org.openstreetmap.atlas.geography.atlas.dsl.console.ConsoleWriter
import org.openstreetmap.atlas.geography.atlas.dsl.console.impl.StandardOutputConsoleWriter
import org.openstreetmap.atlas.geography.atlas.dsl.query.Query
import org.openstreetmap.atlas.geography.atlas.dsl.query.optimizer.optimization.QueryOptimizationTransformer
import org.openstreetmap.atlas.geography.atlas.dsl.selection.indexing.scanning.strategy.ScanStrategizer
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity

import java.util.stream.Collectors

/**
 * Domain representing the analysis.
 *
 * @author Yazad Khambata
 */
@Builder
@ToString
class Analysis<E extends AtlasEntity> {
    Query<E> originalQuery
    Query<E> optimizedQuery
    Map<Class<? extends QueryOptimizationTransformer<E>>, Query<E>> optimizationTrace

    boolean checkIfOptimized() {
        !optimizedQuery?.is(originalQuery)
    }

    void dump() {
        dump(StandardOutputConsoleWriter.getInstance())
    }

    void dump(final ConsoleWriter consoleWriter) {
        consoleWriter.echo(this.toPrettyString())
    }

    String toPrettyString() {
        """
==================================================
------------------------
Query Analysis
------------------------
[I] Was the query optimized?                            : ${checkIfOptimized()}

---
[II] Original Query                                      :  
${originalQuery.toPrettyString()}

---
[III] Optimized Query                                    :
${optimizedQuery.toPrettyString()}

---
[IV] Index Usage Info (on ORIGINAL query)                :
${ScanStrategizer.instance.strategize(originalQuery.conditionalConstructList).indexUsageInfo.toPrettyString()}

---
[V] Index Usage Info (on OPTIMIZED query)                :
${ScanStrategizer.instance.strategize(optimizedQuery.conditionalConstructList).indexUsageInfo.toPrettyString()}

---
[VI] Optimization Trace (history of optimization)        :

${optimizationTrace.entrySet().stream().map { entry -> " --> " + entry.key.getSimpleName() + " Applied,\n" + entry.value.toPrettyString() }.collect(Collectors.joining("\n\n"))}

==================================================
"""
    }
}
