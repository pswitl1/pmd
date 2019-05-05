/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.rule.cwe;

import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.java.ast.ASTPrimaryPrefix;
import net.sourceforge.pmd.lang.java.rule.AbstractJavaRule;

/**
 * Rule designed to detect CWE586_Explicit_Call_to_Finalize
 */
public class ExplicitCallToFinalizeRule extends AbstractJavaRule {

    @Override
    public Object visit(ASTPrimaryPrefix node, Object data) {

        Node child = node.jjtGetChild(0);
        if (child != null) {
            String childImage = child.getImage();
            if (childImage != null) {
                String[] splitString = childImage.split("\\.");
                if (splitString.length > 1) {
                    if (splitString[1].equals("finalize")) {
                        addViolation(data, node);
                    }
                }
            }
        }
        return super.visit(node, data);
    }
}
