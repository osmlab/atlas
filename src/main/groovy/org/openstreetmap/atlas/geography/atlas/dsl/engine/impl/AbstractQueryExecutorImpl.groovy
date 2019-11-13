package org.openstreetmap.atlas.geography.atlas.dsl.engine.impl

import org.apache.commons.io.IOUtils
import org.apache.commons.lang3.StringUtils
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.ImportCustomizer
import org.openstreetmap.atlas.geography.atlas.Atlas
import org.openstreetmap.atlas.geography.atlas.dsl.console.ConsoleWriter
import org.openstreetmap.atlas.geography.atlas.dsl.console.impl.StandardOutputConsoleWriter
import org.openstreetmap.atlas.geography.atlas.dsl.engine.QueryExecutor
import org.openstreetmap.atlas.geography.atlas.dsl.query.QueryBuilder
import org.openstreetmap.atlas.geography.atlas.dsl.query.QueryBuilderFactory
import org.openstreetmap.atlas.geography.atlas.dsl.query.Statement
import org.openstreetmap.atlas.geography.atlas.dsl.query.result.Result
import org.openstreetmap.atlas.geography.atlas.dsl.schema.AtlasDB
import org.openstreetmap.atlas.geography.atlas.dsl.schema.AtlasMediator
import org.openstreetmap.atlas.geography.atlas.dsl.schema.AtlasSchema
import org.openstreetmap.atlas.geography.atlas.dsl.util.Valid

/**
 * Generalisation of query execution irrespective of security of actual implementation.
 *
 * @author Yazad Khambata
 */
abstract class AbstractQueryExecutorImpl implements QueryExecutor {
    private String key

    public static final String SYSTEM_PARAM_KEY = "aqlKey"

    private ConsoleWriter consoleWriter

    AbstractQueryExecutorImpl() {
        this(getKeyFromSystemParam())
    }

    AbstractQueryExecutorImpl(final String key) {
        this(key, StandardOutputConsoleWriter.getInstance())
    }

    AbstractQueryExecutorImpl(final ConsoleWriter consoleWriter) {
        this(getKeyFromSystemParam(), consoleWriter)
    }

    AbstractQueryExecutorImpl(final String key, final ConsoleWriter consoleWriter) {
        this.key = key
        this.consoleWriter = consoleWriter
    }

    protected static final String getKeyFromSystemParam() {
        final String key = System.getProperty(SYSTEM_PARAM_KEY)
        Valid.notEmpty key, "Have you set the system parameter ${SYSTEM_PARAM_KEY}?"

        key
    }

    @Override
    final Result exec(final Atlas atlas, final String queryAsString, final String signature) {
        validateSignature(queryAsString, signature)

        Valid.notEmpty atlas

        final AtlasSchema atlasSchema = toAtlasSchema(atlas)

        validate(queryAsString)

        evalQueryString(atlasSchema, queryAsString)
    }

    @Override
    final Result exec(final Atlas atlas, final Reader queryAsReader, final String signature) {
        exec(atlas, IOUtils.toString(queryAsReader), signature)
    }

    private AtlasSchema toAtlasSchema(Atlas atlas) {
        final AtlasMediator atlasMediator = toAtlasMediator(atlas)

        final AtlasSchema atlasSchema = new AtlasSchema(atlasMediator)
        atlasSchema
    }

    abstract void validateSignature(final String queryAsString, final String signature)

    private AtlasMediator toAtlasMediator(Atlas atlas) {
        new AtlasMediator(atlas)
    }

    private final void validate(String queryAsString) {
        Valid.notEmpty queryAsString, "Query is EMPTY!"

        final List<Statement> allowedStatements = [Statement.SELECT, Statement.UPDATE, Statement.DELETE]

        Valid.isTrue allowedStatements.stream()
                .filter { statement -> queryAsString.startsWith(statement.closureName()) }
                .count() == 1,
                "Invalid Query input, only ${allowedStatements} supported."
        Valid.isTrue Arrays.stream(queryAsString.split(/\r\n|\r|\n/))
                .filter { line -> StringUtils.isNotEmpty(line) }
                .count() == 1,
                "Only one statement permitted."
    }

    private final Result evalQueryString(final AtlasSchema atlasSchema, final String queryAsString) {
        final Binding binding = new Binding()
        binding.setVariable("atlas", atlasSchema)

        final ImportCustomizer importCustomizer = new ImportCustomizer()
        importCustomizer.addStaticStars(QueryBuilderFactory.class.getName(), AtlasDB.class.getName())

        final CompilerConfiguration compilerConfiguration = new CompilerConfiguration()
        compilerConfiguration.addCompilationCustomizers(importCustomizer)

        final GroovyShell groovyShell = new GroovyShell(binding, compilerConfiguration)

        final QueryBuilder queryBuilder = groovyShell.evaluate(queryAsString)

        queryBuilder.buildQuery().execute(consoleWriter)
    }
}
