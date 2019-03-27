/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.rule.cwe;

import java.util.List;

import org.jaxen.JaxenException;

import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.java.ast.ASTAssignmentOperator;
import net.sourceforge.pmd.lang.java.ast.ASTBlock;
import net.sourceforge.pmd.lang.java.ast.ASTLiteral;
import net.sourceforge.pmd.lang.java.ast.ASTLocalVariableDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTMethodDeclarator;
import net.sourceforge.pmd.lang.java.ast.ASTPrimaryExpression;
import net.sourceforge.pmd.lang.java.ast.ASTPrimaryPrefix;
import net.sourceforge.pmd.lang.java.ast.ASTTypeDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTVariableDeclaratorId;
import net.sourceforge.pmd.lang.java.rule.AbstractJavaRule;

public class UncontrolledFormatStringRule extends AbstractJavaRule {

    /**
     * Visit a primary expression and determine if an UncontrolledFormatStringRule violation can be added
     *
     * @param node:    primary expression node
     * @param data:    object data
     * @return Object: visit super
     */
    @Override
    public Object visit(ASTPrimaryExpression node, Object data) {

        // if primary expression isnt a string format/printf, return
        if (!isStringFormatOrPrintfExpression(node)) {
            return super.visit(node, data);
        }

        // get first arg, if no arg, return
        Node firstArg = getFirstArg(node);
        if (firstArg == null) {
            return super.visit(node, data);
        }

        // if literal, return
        if (firstArg instanceof ASTLiteral) {
            return super.visit(node, data);
        }

        // get this method
        ASTBlock thisMethod = getMethod(node);

        // if variable is local, check assignments
        if (isVariableLocal(thisMethod, firstArg.getImage())) {
            if (!validAssignments(thisMethod, firstArg.getImage())) {
                addViolation(data, node);
            }
        } else {

            // else check class for calls to this method
            ASTTypeDeclaration thisClass = getClass(node);
            Node methodDeclaration = thisMethod.jjtGetParent();
            String methodImage = methodDeclaration.findChildrenOfType(ASTMethodDeclarator.class).get(0).getImage();
            if (!validMethodCalls(thisClass, methodImage)) {
                addViolation(data, node);
            }
        }
        return super.visit(node, data);
    }

    /**
     * Determine if a primary expression is a format or printf function.
     *
     * @param expression: primary expression to check
     * @return boolean:   true if expression is a System.out.format or System.out.printf expression, false otherwise
     */
    private static boolean isStringFormatOrPrintfExpression(ASTPrimaryExpression expression) {

        // check that children exist to enable a string format/printf
        if (expression.jjtGetNumChildren() != 0) {
            Node primaryPrefix = expression.jjtGetChild(0);
            if (primaryPrefix.jjtGetNumChildren() != 0) {

                // check if image is a system format/printf
                String prefixImage = primaryPrefix.jjtGetChild(0).getImage();
                String systemOutFormatImage = "System.out.format";
                String systemOutPrintfImage = "System.out.printf";
                if (prefixImage != null && !prefixImage.isEmpty()) {
                    return systemOutFormatImage.equals(prefixImage) || systemOutPrintfImage.equals(prefixImage);
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
    private static Node getFirstArg(ASTPrimaryExpression expression) {
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
    private static ASTBlock getMethod(Node node) {
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
    private static boolean isVariableLocal(ASTBlock method, String variableName) {

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
     * Check if variable assignments are safe
     *
     * @param method:       method to look for variable in
     * @param variableName: name of variable to check assignments of
     * @return boolean:     true if assignments are safe, false otherwise
     */
    private static boolean validAssignments(ASTBlock method, String variableName) {

        // safe assignments boolean
        boolean safe = true;

        // find all assignments
        List<ASTAssignmentOperator> assignments = method.findDescendantsOfType(ASTAssignmentOperator.class);

        // loop over assignments
        for (ASTAssignmentOperator assignment: assignments) {

            // check if assignment is assigning desired variable
            Node expression = assignment.jjtGetParent();
            Node assignmentVariable = expression.jjtGetChild(0).jjtGetChild(0).jjtGetChild(0);
            if (assignmentVariable.hasImageEqualTo(variableName)) {

                // check if variable is assigned to something unsafe
                Node assignedTo = expression.jjtGetChild(2);
                List<ASTPrimaryPrefix> prefixes = assignedTo.findDescendantsOfType(ASTPrimaryPrefix.class);
                for (ASTPrimaryPrefix prefix: prefixes) {
                    if (!(prefix.jjtGetChild(0) instanceof ASTLiteral)) {
                        safe = false;
                        // TODO more extensive check to see if prefix child is safe
                    }
                }
            }
        }
        return safe;
    }

    /**
     * Return the enclosing class of node
     *
     * @param node:      node to get class of
     * @return ASTTypeDeclaration: class declaration
     */
    private static ASTTypeDeclaration getClass(Node node) {
        ASTTypeDeclaration parent = node.getFirstParentOfType(ASTTypeDeclaration.class);
        if (parent == null) {
            return (ASTTypeDeclaration) node;
        }
        return getClass(parent);
    }


    /**
     * Determine if method calls are safe
     * @param cls:        class to look for method calls in
     * @param methodName: method to look for calls too
     * @return boolean:   true if safe calls only, false otherwise
     */
    private boolean validMethodCalls(ASTTypeDeclaration cls, String methodName) {
        List<ASTPrimaryPrefix> prefixes = cls.findDescendantsOfType(ASTPrimaryPrefix.class);
        for (ASTPrimaryPrefix prefix: prefixes) {
            String methodCall = prefix.jjtGetChild(0).getImage();
            if (methodCall != null && !methodCall.isEmpty()) {
                if (methodName.equals(methodCall)) {
                    if (!validArguments(prefix.jjtGetParent())) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * Determine if arguments in an expression are safe
     * TODO need to only look for correct argument
     *
     * @param expression: expression to look at
     * @return boolean: true if arguments are safe, false otherwise
     */
    private boolean validArguments(Node expression) {
        try {
            List<? extends Node> args = expression.findChildNodesWithXPath(
                    "./PrimarySuffix/Arguments/ArgumentList/Expression/PrimaryExpression/PrimaryPrefix");
            for (Node arg: args) {
                String argName = arg.jjtGetChild(0).getImage();
                ASTBlock method = getMethod(expression);
                if (isVariableLocal(method, argName)) {
                    if (!validAssignments(method, argName)) {
                        return false;
                    }
                }
            }
        } catch (JaxenException e) {
            e.printStackTrace();
        }
        return true;
    }
}
