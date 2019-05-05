/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.rule.cwe;

import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.java.ast.ASTAssignmentOperator;
import net.sourceforge.pmd.lang.java.ast.ASTBlock;
import net.sourceforge.pmd.lang.java.ast.ASTBlockStatement;
import net.sourceforge.pmd.lang.java.ast.ASTBreakStatement;
import net.sourceforge.pmd.lang.java.ast.ASTEmptyStatement;
import net.sourceforge.pmd.lang.java.ast.ASTStatementExpression;
import net.sourceforge.pmd.lang.java.ast.ASTSwitchLabel;
import net.sourceforge.pmd.lang.java.ast.ASTSwitchStatement;
import net.sourceforge.pmd.lang.java.rule.AbstractJavaRule;

/**
 * Rule designed to detect CWE398_Poor Code Quality
 */
public class PoorCodeQualityRule extends AbstractJavaRule {

    @Override
    public Object visit(ASTBlock node, Object data) {
        if (node.jjtGetNumChildren() == 0) {
            addViolationWithMessage(data, node, "Empty block statement.");
        }
        return super.visit(node, data);
    }

    @Override
    public Object visit(ASTSwitchStatement node, Object data) {
        for (int i = 0; i < node.jjtGetNumChildren() - 1; i++) {
            Node child = node.jjtGetChild(i);

            if (child instanceof ASTSwitchLabel) {
                Node nextChild = node.jjtGetChild(i + 1);
                if (nextChild instanceof ASTBlockStatement) {
                    if (nextChild.jjtGetChild(0).jjtGetChild(0) instanceof ASTBreakStatement) {
                        addViolationWithMessage(data, node, "Empty switch case.");
                    }
                }
            }
        }
        return super.visit(node, data);
    }

    @Override
    public Object visit(ASTStatementExpression node, Object data) {

        if (node.jjtGetNumChildren() == 3) {
            try {
                if (node.jjtGetChild(1) instanceof ASTAssignmentOperator) {
                    String leftImage = node.jjtGetChild(0).jjtGetChild(0).jjtGetChild(0).getImage();
                    String rightImage = node.jjtGetChild(2).jjtGetChild(0).jjtGetChild(0).jjtGetChild(0).getImage();

                    if (leftImage.equals(rightImage)) {
                        addViolationWithMessage(data, node, "Setting variable equal to itself, statement has no effect.");
                    }
                }

            } catch (NullPointerException e) {
                return super.visit(node, data);
            }
        }
        return super.visit(node, data);
    }

    @Override
    public Object visit(ASTEmptyStatement node, Object data) {
        addViolationWithMessage(data, node, "Empty statement has no effect");
        return super.visit(node, data);
    }
}
