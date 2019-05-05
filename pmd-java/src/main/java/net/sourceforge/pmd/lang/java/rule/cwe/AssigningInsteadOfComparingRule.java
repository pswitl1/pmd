/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.rule.cwe;

import java.util.List;

import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.java.ast.ASTAssignmentOperator;
import net.sourceforge.pmd.lang.java.ast.ASTIfStatement;
import net.sourceforge.pmd.lang.java.rule.AbstractJavaRule;

/**
 * Rule designed to detect CWE481_Assigning_Instead_of_Comparing
 */
public class AssigningInsteadOfComparingRule extends AbstractJavaRule {

    @Override
    public Object visit(ASTIfStatement node, Object data) {

        Node expression = node.jjtGetChild(0);

        List<ASTAssignmentOperator> assignmentOperatorList = expression.findDescendantsOfType(ASTAssignmentOperator.class);

        if (!assignmentOperatorList.isEmpty()) {
            addViolation(data, node);
        }

        return super.visit(node, data);
    }
}
