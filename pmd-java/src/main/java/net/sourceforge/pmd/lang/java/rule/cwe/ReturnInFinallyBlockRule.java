/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.rule.cwe;

import net.sourceforge.pmd.lang.java.ast.ASTFinallyStatement;
import net.sourceforge.pmd.lang.java.ast.ASTReturnStatement;
import net.sourceforge.pmd.lang.java.rule.AbstractJavaRule;

/**
 * Rule designed to detect CWE584_Return_in_Finally_Block
 */
public class ReturnInFinallyBlockRule extends AbstractJavaRule {

    @Override
    public Object visit(ASTFinallyStatement node, Object data) {

        if (node.hasDescendantOfType(ASTReturnStatement.class)) {
            addViolation(data, node);
        }

        return super.visit(node, data);
    }
}
