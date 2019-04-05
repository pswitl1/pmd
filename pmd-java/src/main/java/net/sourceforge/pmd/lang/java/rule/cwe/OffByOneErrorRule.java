/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.rule.cwe;

import java.util.List;

import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.java.ast.ASTDoStatement;
import net.sourceforge.pmd.lang.java.ast.ASTForStatement;
import net.sourceforge.pmd.lang.java.ast.ASTReferenceType;
import net.sourceforge.pmd.lang.java.ast.ASTRelationalExpression;
import net.sourceforge.pmd.lang.java.ast.ASTTypeDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTVariableDeclaratorId;
import net.sourceforge.pmd.lang.java.ast.ASTWhileStatement;
import net.sourceforge.pmd.lang.java.rule.AbstractJavaRule;

/**
 * Rule designed to detect CWE193_Off_by_One_Error
 */
public class OffByOneErrorRule extends AbstractJavaRule {

    /**
     * Visit a do statement and check for an unsafe loop condition, if so, add OffByOneErrorRule violation
     *
     * @param node:    primary expression node
     * @param data:    object data
     * @return Object: visit super
     */
    @Override
    public Object visit(ASTDoStatement node, Object data) {

        Node condition = node.jjtGetChild(1);
        if (unsafeLoopCondition(condition)) {
            addViolation(data, node);
        }

        return super.visit(node, data);
    }

    /**
     * Visit a for statement and check for an unsafe loop condition, if so, add OffByOneErrorRule violation
     *
     * @param node:    primary expression node
     * @param data:    object data
     * @return Object: visit super
     */
    @Override
    public Object visit(ASTForStatement node, Object data) {

        Node condition = node.jjtGetChild(1);
        if (unsafeLoopCondition(condition)) {
            addViolation(data, node);
        }

        return super.visit(node, data);
    }

    /**
     * Visit a while statement and check for an unsafe loop condition, if so, add OffByOneErrorRule violation
     *
     * @param node:    primary expression node
     * @param data:    object data
     * @return Object: visit super
     */
    @Override
    public Object visit(ASTWhileStatement node, Object data) {

        Node condition = node.jjtGetChild(0);
        if (unsafeLoopCondition(condition)) {
            addViolation(data, node);
        }

        return super.visit(node, data);
    }

    /**
     * Check if a loop condtion will cause an out of bounds access on an array
     * @param condition: node of the condtion
     * @return boolean: true if loop is unsafe, false otherwise
     */
    private static boolean unsafeLoopCondition(Node condition) {

        // if condition is not a relational expression, return safe
        Node relationalExpression = condition.jjtGetChild(0);
        if (!(relationalExpression instanceof ASTRelationalExpression)) {
            return false;
        }

        // if condition is not <=, return safe
        if (!relationalExpression.hasImageEqualTo("<=")) {
            return false;
        }

        // return if second operator is an arrays length
        Node secondOperator = relationalExpression.jjtGetChild(1).jjtGetChild(0).jjtGetChild(0);
        return isArrayLength(secondOperator);
    }

    /**
     * Check if a nodes image is an arrays length
     *
     * @param node: node to check
     * @return boolean: true if node is an arrays length, false otherwise
     */
    private static boolean isArrayLength(Node node) {
        String[] splitString = node.getImage().split("\\.");
        if (splitString.length < 2) {
            return false;
        }
        if (!splitString[1].equals("length")) {
            return false;
        }
        return isArray(node);
    }

    /**
     * Check if a nodes image is an array
     *
     * @param node: node to check
     * @return boolean: true if node is an array, false otherwise
     */
    private static boolean isArray(Node node) {
        String[] splitString = node.getImage().split("\\.");
        String possibleArrayImage = splitString[0];
        ASTTypeDeclaration cls = CweUtilities.getClass(node);

        List<ASTReferenceType> referenceTypeList = cls.findDescendantsOfType(ASTReferenceType.class);

        for (ASTReferenceType referenceType: referenceTypeList) {

            Node fieldDeclaration = referenceType.jjtGetParent().jjtGetParent();
            ASTVariableDeclaratorId variableId = fieldDeclaration.getFirstDescendantOfType(ASTVariableDeclaratorId.class);
            if (variableId.hasImageEqualTo(possibleArrayImage)) {
                return true;
            }
        }
        return false;
    }
}
