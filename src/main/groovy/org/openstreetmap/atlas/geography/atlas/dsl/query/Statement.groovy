package org.openstreetmap.atlas.geography.atlas.dsl.query

/**
 * An enum of statements and commands and the clauses allowed with them.
 *
 * @author Yazad Khambata
 */
enum Statement {
    USING,

    SELECT(Clause.FROM, Clause.WHERE, Clause.AND, Clause.OR, Clause.LIMIT),

    UPDATE(Clause.SET, Clause.WHERE, Clause.AND, Clause.OR),

    DELETE(Clause.WHERE, Clause.AND, Clause.OR),

    COMMIT,

    EXPLAIN,

    DIFFERENCE
    ;

    private List<Clause> allowedClauses

    Statement(Clause...allowedClauses) {
        this.allowedClauses = Arrays.asList(allowedClauses)
    }

    def from(final String statementAsStr) {
        Statement.valueOf(statementAsStr)
    }

    def isClauseAllowed(Clause clause) {
        clause == Clause.__IT__ || this.allowedClauses.contains(clause)
    }

    String closureName() {
        this.name().toLowerCase()
    }

    static enum Clause {
        /**
         * To represent the statement itself.
         */
        __IT__,

        FROM,

        WHERE,

        AND,

        OR,

        NOT,

        LIMIT,

        SET
        ;

        def from(final String clauseAsStr) {
            Clause.valueOf(clauseAsStr)
        }
    }
}
