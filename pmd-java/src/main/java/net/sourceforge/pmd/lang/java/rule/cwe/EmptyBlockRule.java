/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.rule.cwe;

import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.java.ast.ASTBlock;
import net.sourceforge.pmd.lang.java.ast.ASTBlockStatement;
import net.sourceforge.pmd.lang.java.rule.AbstractJavaRule;

//Has Blockstatement as first child for Block
public class EmptyBlockRule extends AbstractJavaRule {

    public Object visit(ASTBlock node, Object data) {
        Node firstStmt = node.jjtGetChild(1);
        if (!hasBlockStatementAsFirstChild(firstStmt)) {
            addViolation(data, node);
        }
        return super.visit(node,data);
    }

    private boolean hasBlockStatementAsFirstChild(Node node) {
        return (node.jjtGetNumChildren() != 0 && (node.jjtGetChild(0) instanceof ASTBlockStatement));
    }
}
