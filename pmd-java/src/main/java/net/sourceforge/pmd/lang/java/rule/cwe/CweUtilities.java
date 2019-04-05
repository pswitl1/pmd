/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.rule.cwe;

import java.util.List;

import org.jaxen.JaxenException;

import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.java.ast.ASTBlock;
import net.sourceforge.pmd.lang.java.ast.ASTFieldDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTLocalVariableDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTMethodDeclarator;
import net.sourceforge.pmd.lang.java.ast.ASTPrimaryExpression;
import net.sourceforge.pmd.lang.java.ast.ASTTypeDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTVariableDeclaratorId;

abstract class CweUtilities {

    /**
     * Determine if a primary expression is of type expressionToCheck
     *
     * @param expression:              primary expression to check
     * @param expressionStringToCheck: string of expression to check against expression
     * @return boolean:                true if expression of type expressionStringToCheck, false otherwise
     */
    static boolean isExpression(ASTPrimaryExpression expression, String expressionStringToCheck) {

        // check that children exist to enable a string format/printf
        if (expression.jjtGetNumChildren() != 0) {
            Node primaryPrefix = expression.jjtGetChild(0);
            if (primaryPrefix.jjtGetNumChildren() != 0) {

                // check if image is a system format/printf
                String prefixImage = primaryPrefix.jjtGetChild(0).getImage();

                if (prefixImage != null && !prefixImage.isEmpty()) {
                    return expressionStringToCheck.equals(prefixImage);
                }
            }
        }
        return false;
    }

    /**
     * Get the first argument in a primary expression
     *
     * @param expression: desired expression
     * @return Node:      first argument of expression
     */
    static Node getFirstArg(ASTPrimaryExpression expression) {
        try {
            List<Node> args = expression.findChildNodesWithXPath("./PrimarySuffix/Arguments/ArgumentList/Expression/PrimaryExpression/PrimaryPrefix");
            if (args.isEmpty()) {
                return null;
            }
            return args.get(0).jjtGetChild(0);

        } catch (JaxenException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Return the enclosing method of node
     *
     * @param node:      node to get method of
     * @return ASTBlock: method block
     */
    static ASTBlock getMethod(Node node) {
        ASTBlock parent = node.getFirstParentOfType(ASTBlock.class);
        if (parent == null) {
            return (ASTBlock) node;
        }
        return getMethod(parent);
    }

    /**
     * Determine if a variable is local
     *
     * @param method:       method to look for variable in
     * @param variableName: name of variable to look for
     * @return boolean:     true if variable is local, false otherwise
     */
    static boolean isVariableLocal(ASTBlock method, String variableName) {

        // find all variable declarations in method
        List<ASTLocalVariableDeclaration> variables = method.findDescendantsOfType(ASTLocalVariableDeclaration.class);

        // search for variable
        for (ASTLocalVariableDeclaration variable: variables) {
            ASTVariableDeclaratorId variableId = variable.getFirstDescendantOfType(ASTVariableDeclaratorId.class);
            if (variableId.hasImageEqualTo(variableName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Return the enclosing class of node
     *
     * @param node:                node to get class of
     * @return ASTTypeDeclaration: class declaration
     */
    static ASTTypeDeclaration getClass(Node node) {
        ASTTypeDeclaration parent = node.getFirstParentOfType(ASTTypeDeclaration.class);
        if (parent == null) {
            return (ASTTypeDeclaration) node;
        }
        return getClass(parent);
    }

    /**
     * Try to find a method in a class
     *
     * @param thisClass:  class to look in
     * @param methodName: method name to look for
     * @return Node:      the MethodDeclaration if found, if not null
     */
    static Node findMethod(ASTTypeDeclaration thisClass, String methodName) {
        List<ASTMethodDeclarator> methodsInClass = thisClass.findDescendantsOfType(ASTMethodDeclarator.class);

        for (ASTMethodDeclarator method: methodsInClass) {
            if (method.hasImageEqualTo(methodName)) {
                return method.jjtGetParent();
            }
        }
        return null;
    }

    /**
     * Try to find a class variable in a class
     *
     * @param thisClass: class to look in
     * @param varName:   class variable name to look for
     * @return String:   class variable string if found, if not null
     */
    static String findClassVariable(ASTTypeDeclaration thisClass, String varName) {
        List<ASTFieldDeclaration> fields = thisClass.findDescendantsOfType(ASTFieldDeclaration.class);

        for (ASTFieldDeclaration field: fields) {
            Node fieldId = field.jjtGetChild(1).jjtGetChild(0);
            if (fieldId.hasImageEqualTo(varName)) {
                return varName;
            }
        }

        return null;
    }
}
