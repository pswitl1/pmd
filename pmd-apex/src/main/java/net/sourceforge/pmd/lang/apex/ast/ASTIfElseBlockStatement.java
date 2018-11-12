/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.apex.ast;

import apex.jorje.semantic.ast.statement.IfElseBlockStatement;

public class ASTIfElseBlockStatement extends AbstractApexNode<IfElseBlockStatement> {

    public ASTIfElseBlockStatement(IfElseBlockStatement ifElseBlockStatement) {
        super(ifElseBlockStatement);
    }

    @Override
    public Object jjtAccept(ApexParserVisitor visitor, Object data) {
        return visitor.visit(this, data);
    }
}
