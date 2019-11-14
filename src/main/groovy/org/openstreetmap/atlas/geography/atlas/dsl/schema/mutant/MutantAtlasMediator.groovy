package org.openstreetmap.atlas.geography.atlas.dsl.schema.mutant

import org.openstreetmap.atlas.geography.atlas.Atlas
import org.openstreetmap.atlas.geography.atlas.change.Change
import org.openstreetmap.atlas.geography.atlas.change.ChangeAtlas
import org.openstreetmap.atlas.geography.atlas.dsl.query.QueryBuilder
import org.openstreetmap.atlas.geography.atlas.dsl.query.commit.Commitable
import org.openstreetmap.atlas.geography.atlas.dsl.query.result.CommitableResult
import org.openstreetmap.atlas.geography.atlas.dsl.schema.AtlasMediator
import org.openstreetmap.atlas.geography.atlas.dsl.util.Valid
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.util.stream.Collectors

/**
 * An AtlasMediator over ChangeAtlas.
 *
 * @author Yazad Khambata
 */
class MutantAtlasMediator extends AtlasMediator {
    private static final Logger log = LoggerFactory.getLogger(MutantAtlasMediator.class);

    MutantAtlasMediator(final QueryBuilder[] queryBuilders) {
        super(commit(queryBuilders))
    }

    protected static AtlasMediator commit(final QueryBuilder[] queryBuilders) {
        Valid.notEmpty queryBuilders

        Valid.isTrue queryBuilders.size() >= 1

        final List<CommitableResult> commitableResults = Arrays.stream(queryBuilders)
                .map { queryBuilder -> queryBuilder.buildQuery() }
                .map { query -> Valid.isTrue query instanceof Commitable; query.execute() as CommitableResult }
                .collect(Collectors.toList())

        //Check how efficient atlas#hashCode() is.
        final long atlasCount = commitableResults.stream().map { it.atlasMediatorToCommitOn.atlas }.distinct().count()
        Valid.isTrue atlasCount == 1, "Illegal attempt to commit multiple Atlases in the same statement."

        final Atlas atlasToCommitOn = commitableResults[0].atlasMediatorToCommitOn.atlas
        final Change[] allChanges = commitableResults.stream().map { it.change }.toArray() as Change[]

        log.info "Applying changes to atlas..."
        final Atlas changeAtlas = new ChangeAtlas(atlasToCommitOn, allChanges)
        log.info "Changes applied to atlas!"

        new AtlasMediator(changeAtlas)
    }
}
