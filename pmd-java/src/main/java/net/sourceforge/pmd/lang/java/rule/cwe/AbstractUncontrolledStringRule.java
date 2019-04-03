/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.rule.cwe;

import java.util.ArrayList;
import java.util.List;

import org.jaxen.JaxenException;

import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.java.ast.ASTAssignmentOperator;
import net.sourceforge.pmd.lang.java.ast.ASTBlock;
import net.sourceforge.pmd.lang.java.ast.ASTFieldDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTLiteral;
import net.sourceforge.pmd.lang.java.ast.ASTLocalVariableDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTMethodDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTMethodDeclarator;
import net.sourceforge.pmd.lang.java.ast.ASTPrimaryExpression;
import net.sourceforge.pmd.lang.java.ast.ASTPrimaryPrefix;
import net.sourceforge.pmd.lang.java.ast.ASTReturnStatement;
import net.sourceforge.pmd.lang.java.ast.ASTTypeDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTVariableDeclarator;
import net.sourceforge.pmd.lang.java.ast.ASTVariableDeclaratorId;
import net.sourceforge.pmd.lang.java.rule.AbstractJavaRule;

/**
 * Abstract rule class used to add a violation if a an uncontrolled string is used in certain expressions
 * The subclasses must add any desired expressions to the "expressionsToCheck" class member
 *     see UncontrolledFormatStringRule or ExternalControlOfSystemOrConfigurationSettingRule for examples
 */
public class AbstractUncontrolledStringRule extends AbstractJavaRule {

    private List<String> currentlyCheckingVar;
    private List<String> currentlyCheckingVarHolder;
    List<String> expressionsToCheck;

    /**
     * Package private constructor to initialize lists
     */
    AbstractUncontrolledStringRule() {
        currentlyCheckingVar = new ArrayList<>();
        currentlyCheckingVarHolder = new ArrayList<>();
        expressionsToCheck = new ArrayList<>();
    }

    /**
     * Visit a primary expression and determine if a violation can be added
     *
     * @param node:    primary expression node
     * @param data:    object data
     * @return Object: visit super
     */
    @Override
    public Object visit(ASTPrimaryExpression node, Object data) {

        // make sure ASTPrimaryExpression is an expression in expressionsToCheck
        boolean checkForViolation = false;
        for (String expressionToCheck: expressionsToCheck) {
            if (isExpression(node, expressionToCheck)) {
                checkForViolation = true;
            }
        }

        // if expression is of desired type, check if unsafe
        if (checkForViolation && unsafe(node)) {
            addViolation(data, node);
        }

        return super.visit(node, data);
    }

    /**
     * Determine if a primary expression is of type expressionToCheck
     *
     * @param expression:              primary expression to check
     * @param expressionStringToCheck: string of expression to check against expression
     * @return boolean:                true if expression of type expressionStringToCheck, false otherwise
     */
    private static boolean isExpression(ASTPrimaryExpression expression, String expressionStringToCheck) {

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
     * Check if expression is unsafe
     *
     * @param node:  expression to check
     * @return true: if expression is unsafe, false otherwise
     */
    private boolean unsafe(ASTPrimaryExpression node) {

        // get first arg, if no arg, return
        Node firstArg = getFirstArg(node);
        if (firstArg == null) {
            return false;
        }

        // if literal, return
        if (firstArg instanceof ASTLiteral) {
            return false;
        }

        // get this method
        ASTBlock thisMethod = getMethod(node);

        // if variable is local, check assignments
        if (isVariableLocal(thisMethod, firstArg.getImage())) {
            Node methodDeclaration = thisMethod.jjtGetParent();
            String methodImage = methodDeclaration.findChildrenOfType(ASTMethodDeclarator.class).get(0).getImage();
            return unsafeAssignments(thisMethod, methodImage, firstArg.getImage());
        } else {

            // else check class for calls to this method
            ASTTypeDeclaration thisClass = getClass(node);
            Node methodDeclaration = thisMethod.jjtGetParent();
            String methodImage = methodDeclaration.findChildrenOfType(ASTMethodDeclarator.class).get(0).getImage();
            return unsafeMethodCalls(thisClass, methodImage);
        }
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
     * Return the enclosing class of node
     *
     * @param node:                node to get class of
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
     * Check if all variable assignments are safe
     *
     * @param method:       method to look for variable in
     * @param variableName: name of variable to check assignments of
     * @return boolean:     true if at least one assignment is unsafe, false otherwise
     */
    private boolean unsafeAssignments(ASTBlock method, String variableHolder, String variableName) {

        // add to currently checking

        currentlyCheckingVar.add(variableName);
        currentlyCheckingVarHolder.add(variableHolder);

        // check all assignments
        List<ASTAssignmentOperator> assignments = method.findDescendantsOfType(ASTAssignmentOperator.class);
        for (ASTAssignmentOperator assignment: assignments) {
            Node expression = assignment.jjtGetParent();
            Node assignmentVariable = expression.jjtGetChild(0).jjtGetChild(0).jjtGetChild(0);
            if (assignmentVariable.hasImageEqualTo(variableName)) {
                if (unsafeExpression(expression.jjtGetChild(2))) {
                    return true;
                }
            }
        }

        // check all variable initializer assignments
        List<ASTVariableDeclarator> variables = method.findDescendantsOfType(ASTVariableDeclarator.class);
        for (ASTVariableDeclarator variable: variables) {
            Node variableId = variable.jjtGetChild(0);
            if (variableId.hasImageEqualTo(variableName)) {
                if (variable.jjtGetNumChildren() > 1) {
                    if (unsafeExpression(variable.jjtGetChild(1).jjtGetChild(0))) {
                        return true;
                    }
                }
            }
        }

        currentlyCheckingVar.remove(variableName);
        currentlyCheckingVarHolder.remove(variableHolder);
        return false;
    }

    /**
     * Determine if method calls are safe
     *
     * @param cls:        class to look for method calls in
     * @param methodName: method to look for calls too
     * @return boolean:   true if at least one call is unsafe, false otherwise
     */
    private boolean unsafeMethodCalls(ASTTypeDeclaration cls, String methodName) {
        List<ASTPrimaryPrefix> prefixes = cls.findDescendantsOfType(ASTPrimaryPrefix.class);
        for (ASTPrimaryPrefix prefix: prefixes) {
            String methodCall = prefix.jjtGetChild(0).getImage();
            if (methodCall != null && !methodCall.isEmpty()) {
                if (methodName.equals(methodCall)) {
                    if (unsafeArguments(prefix.jjtGetParent())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Determine if an individual assignment is safe.
     *
     * @param expression: assignment expression
     * @return boolean:   true if at least one prefix from expression is unsafe, false otherwise
     */
    private boolean unsafeExpression(Node expression) {

        // iterate over each prefix of assignment
        List<ASTPrimaryPrefix> prefixes = expression.findDescendantsOfType(ASTPrimaryPrefix.class);
        for (ASTPrimaryPrefix prefix: prefixes) {
            Node assignment = prefix.jjtGetChild(0);

            // if not literal, check further
            if (!(assignment instanceof ASTLiteral)) {
                ASTBlock thisMethod = getMethod(expression);
                String assignmentImage = assignment.getImage();

                // make sure we aren't already checking the assignment image
                Node methodDeclaration = thisMethod.jjtGetParent();
                String methodImage = methodDeclaration.findChildrenOfType(ASTMethodDeclarator.class).get(0).getImage();
                int i = 0;
                for (String var: currentlyCheckingVar) {
                    if (var.equals(assignmentImage)) {
                        if (methodImage.equals(currentlyCheckingVarHolder.get(i))) {
                            return false;
                        }
                    }
                    i++;
                }

                // is local variable, do recursive check on that variable
                if (isVariableLocal(thisMethod, assignmentImage)) {

                    if (unsafeAssignments(thisMethod, methodImage, assignmentImage)) {
                        return true;
                    }
                } else {

                    // if method call, check method return value
                    Node method = findMethod(getClass(expression), assignmentImage);

                    // check for class variable
                    String classVar = findClassVariable(getClass(expression), assignmentImage);

                    if (method != null) {
                        if (unsafeMethodReturnStatements(method)) {
                            return true;
                        }
                    } else if (classVar != null) {
                        if (unsafeClassVariableAssignments(getClass(expression), assignmentImage)) {
                            return true;
                        }
                    } else {
                        // if not a literal, local variable, method call, or class variable, assume unsafe
                        // Add another if statement if any other type of assignment could be safe
                        System.out.println("Cannot find unsafe expression, assuming it is unsafe");
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Try to find a method in a class
     *
     * @param thisClass:  class to look in
     * @param methodName: method name to look for
     * @return Node:      the MethodDeclaration if found, if not null
     */
    private static Node findMethod(ASTTypeDeclaration thisClass, String methodName) {
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
    private static String findClassVariable(ASTTypeDeclaration thisClass, String varName) {
        List<ASTFieldDeclaration> fields = thisClass.findDescendantsOfType(ASTFieldDeclaration.class);

        for (ASTFieldDeclaration field: fields) {
            Node fieldId = field.jjtGetChild(1).jjtGetChild(0);
            if (fieldId.hasImageEqualTo(varName)) {
                return varName;
            }
        }

        return null;
    }

    /**
     * Check if a methods return statements are safe
     *
     * @param method:   method to check
     * @return boolean: true if at least one return statement is unsafe, false otherwise
     */
    private boolean unsafeMethodReturnStatements(Node method) {
        List<ASTReturnStatement> returns = method.findDescendantsOfType(ASTReturnStatement.class);
        for (ASTReturnStatement ret: returns) {
            if (unsafeExpression(ret.jjtGetChild(0))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check for unsafe class variable assignments
     *
     * @param thisClass: class of class variable
     * @param classVar:  class variable name
     * @return boolean:  true if at least one assignment is unsafe, false otherwise
     */
    private boolean unsafeClassVariableAssignments(Node thisClass, String classVar) {
        List<ASTMethodDeclaration> methods = thisClass.findDescendantsOfType(ASTMethodDeclaration.class);

        String classImage = thisClass.jjtGetChild(0).getImage();
        for (ASTMethodDeclaration method: methods) {
            if (unsafeAssignments(method.getFirstChildOfType(ASTBlock.class), classImage, classVar)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Determine if arguments in an expression are safe
     * TODO need to only look for correct argument
     *
     * @param expression: expression to look at
     * @return boolean:   true if at least one argument is unsafe, false otherwise
     */
    private boolean unsafeArguments(Node expression) {
        try {
            List<? extends Node> args = expression.findChildNodesWithXPath(
                    "./PrimarySuffix/Arguments/ArgumentList/Expression/PrimaryExpression/PrimaryPrefix");
            for (Node arg: args) {
                String argName = arg.jjtGetChild(0).getImage();
                ASTBlock method = getMethod(expression);
                if (isVariableLocal(method, argName)) {
                    Node methodDeclaration = method.jjtGetParent();
                    String methodImage = methodDeclaration.findChildrenOfType(ASTMethodDeclarator.class).get(0).getImage();
                    if (unsafeAssignments(method, methodImage, argName)) {
                        return true;
                    }
                }
            }
        } catch (JaxenException e) {
            e.printStackTrace();
        }
        return false;
    }
}
