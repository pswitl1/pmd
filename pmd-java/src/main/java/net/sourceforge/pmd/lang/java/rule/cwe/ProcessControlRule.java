/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.rule.cwe;

import net.sourceforge.pmd.lang.java.ast.ASTPrimaryExpression;
import net.sourceforge.pmd.lang.java.rule.AbstractJavaRule;

/**
 * Rule designed to detect CWE114_Process_Control
 */
public class ProcessControlRule extends AbstractJavaRule {

    /**
     * Visit a primary expression and determine if a violation can be added
     *
     * @param node:    primary expression node
     * @param data:    object data
     * @return Object: visit super
     */
    @Override
    public Object visit(ASTPrimaryExpression node, Object data) {
        String imageString = node.jjtGetChild(0).jjtGetChild(0).getImage();

        // if System.loadLibrary is found, add violation
        if (imageString != null && !imageString.isEmpty()) {
            String loadLibrary = "System.loadLibrary";
            if (loadLibrary.equals(imageString)) {
                addViolation(data, node);
            }
        }
        return super.visit(node, data);
    }
}
