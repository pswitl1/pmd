/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

/* Generated By:JJTree: Do not edit this line. ASTSelectStatement.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=true,VISITOR=true,TRACK_TOKENS=false,NODE_PREFIX=AST,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */

package net.sourceforge.pmd.lang.plsql.ast;

public class ASTSelectStatement extends AbstractSelectStatement {
    public ASTSelectStatement(int id) {
        super(id);
    }

    public ASTSelectStatement(PLSQLParser p, int id) {
        super(p, id);
    }

    /** Accept the visitor. **/
    @Override
    public Object jjtAccept(PLSQLParserVisitor visitor, Object data) {
        return visitor.visit(this, data);
    }
}
/* JavaCC - OriginalChecksum=6a27b24d958d9811f9cd9229a7d3a3ac (do not edit this line) */
