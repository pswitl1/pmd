/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.rule.cwe;

import java.util.List;

import org.jaxen.JaxenException;

import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.java.ast.ASTLiteral;
import net.sourceforge.pmd.lang.java.ast.ASTPrimaryExpression;
import net.sourceforge.pmd.lang.java.ast.ASTPrimaryPrefix;
import net.sourceforge.pmd.lang.java.rule.AbstractJavaRule;


public class UncontrolledFormatStringRule extends AbstractJavaRule {

    @Override
    public Object visit(ASTPrimaryExpression node, Object data) {
        if (isStringFormatExpression(node)) {
            System.out.println("String literal found");
            try {
                List<Node> children = node.findChildNodesWithXPath(
                        "./PrimarySuffix/Arguments/ArgumentList/Expression/PrimaryExpression/PrimaryPrefix");
                if (!children.isEmpty()) {
                    System.out.println("Found Arguments");
                    if (children.get(0).jjtGetNumChildren() != 0) {
                        if (!(children.get(0).jjtGetChild(0) instanceof ASTLiteral)) {
                            System.out.println("First child is not a literal");
                            addViolation(data, node);
                        }
                    }
                }
            } catch (JaxenException je) {
                throw new RuntimeException(je);
            }
        }
        return super.visit(node, data);
    }

    private boolean isStringFormatExpression(ASTPrimaryExpression expression) {
        if (expression.jjtGetNumChildren() != 0) {
            if (expression.jjtGetChild(0) instanceof ASTPrimaryPrefix) {
                Node primaryPrefix = expression.jjtGetChild(0);
                if (primaryPrefix.jjtGetNumChildren() != 0) {
                    String imageString = primaryPrefix.jjtGetChild(0).getImage();
                    String systemOutFormatString = "System.out.format";
                    if (imageString != null && !imageString.isEmpty()) {
                        return systemOutFormatString.equals(imageString);
                    } else {
                        return false;
                    }
                }
            }
        }
        return false;
    }
}
