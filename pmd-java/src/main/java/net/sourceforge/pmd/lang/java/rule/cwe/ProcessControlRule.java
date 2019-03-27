/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.rule.cwe;

import net.sourceforge.pmd.lang.java.ast.ASTPrimaryExpression;
import net.sourceforge.pmd.lang.java.rule.AbstractJavaRule;

public class ProcessControlRule extends AbstractJavaRule {

    @Override
    public Object visit(ASTPrimaryExpression node, Object data) {
        String imageString = node.jjtGetChild(0).jjtGetChild(0).getImage();

        if (imageString != null && !imageString.isEmpty()) {
            String loadLibrary = "System.loadLibrary";
            if (loadLibrary.equals(imageString)) {
                addViolation(data, node);
            }
        }
        return super.visit(node, data);
    }
}
