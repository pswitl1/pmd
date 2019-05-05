/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.rule.cwe;

import java.util.List;

import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.java.ast.ASTIfStatement;
import net.sourceforge.pmd.lang.java.ast.ASTPrimaryExpression;
import net.sourceforge.pmd.lang.java.ast.ASTPrimarySuffix;
import net.sourceforge.pmd.lang.java.ast.ASTRelationalExpression;

/**
 * Rule designed to detect CWE129_Improper_Validation_Of_Array_Index
 */
public class ImproperValidationOfArrayIndex extends AbstractUncontrolledStringRule {

    @Override
    public Object visit(ASTPrimarySuffix node, Object data) {
        if (node.isArrayDereference()) {
            String arrayImage = node.jjtGetParent().jjtGetChild(0).jjtGetChild(0).getImage();
            List<ASTPrimaryExpression> primaryExpressionList = node.findDescendantsOfType(ASTPrimaryExpression.class);
            for (ASTPrimaryExpression primaryExpression: primaryExpressionList) {
                Node indexer = primaryExpression.jjtGetChild(0).jjtGetChild(0);
                if (unsafe(primaryExpression, indexer)) {
                    if (boundsNotChecked(arrayImage, indexer)) {
                        addViolation(data, node);
                    }
                }
            }
        }
        return super.visit(node, data);
    }

    private boolean boundsNotChecked(String arrayImage, Node indexer) {

        boolean minChecked = false;
        boolean maxChecked = false;

        List<ASTIfStatement> ifStatementList = indexer.getParentsOfType(ASTIfStatement.class);
        for (ASTIfStatement ifStatement: ifStatementList) {
            List<ASTRelationalExpression> relationalExpressionList = ifStatement.jjtGetChild(0).findDescendantsOfType(ASTRelationalExpression.class);
            for (ASTRelationalExpression relationalExpression: relationalExpressionList) {
                if (expressionChecksArrayMin(relationalExpression, indexer.getImage())) {
                    minChecked = true;
                }
                if (expressionChecksArrayMax(relationalExpression, arrayImage, indexer.getImage())) {
                    maxChecked = true;
                }
            }
        }

        return !minChecked || !maxChecked;
    }

    private boolean expressionChecksArrayMin(ASTRelationalExpression relation, String indexerImage) {
        Node first = relation.jjtGetChild(0).jjtGetChild(0).jjtGetChild(0);
        Node second = relation.jjtGetChild(1).jjtGetChild(0).jjtGetChild(0);

        if (first.hasImageEqualTo("0")) {
            if (second.hasImageEqualTo(indexerImage)) {
                if (relation.hasImageEqualTo("<=")) {
                    return true;
                }
            }
        }

        if (second.hasImageEqualTo("0")) {
            if (first.hasImageEqualTo(indexerImage)) {
                return relation.hasImageEqualTo(">=");
            }
        }
        return false;
    }

    private boolean expressionChecksArrayMax(ASTRelationalExpression relation, String arrayImage, String indexerImage) {
        Node first = relation.jjtGetChild(0).jjtGetChild(0).jjtGetChild(0);
        Node second = relation.jjtGetChild(1).jjtGetChild(0).jjtGetChild(0);

        String[] splitString = first.getImage().split("\\.");
        if (splitString.length > 1) {
            if (splitString[0].equals(arrayImage) && splitString[1].equals("length")) {
                if (second.hasImageEqualTo(indexerImage)) {
                    if (relation.hasImageEqualTo(">")) {
                        return true;
                    }
                }
            }
        }

        splitString = second.getImage().split("\\.");
        if (splitString.length > 1) {
            if (splitString[0].equals(arrayImage) && splitString[1].equals("length")) {
                if (first.hasImageEqualTo(indexerImage)) {
                    return relation.hasImageEqualTo("<");
                }
            }
        }
        return false;
    }
}
