/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.rule.cwe;

import java.util.ArrayList;
import java.util.List;

import org.jaxen.JaxenException;

import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.java.ast.ASTArgumentList;
import net.sourceforge.pmd.lang.java.ast.ASTAssignmentOperator;
import net.sourceforge.pmd.lang.java.ast.ASTBlock;
import net.sourceforge.pmd.lang.java.ast.ASTLiteral;
import net.sourceforge.pmd.lang.java.ast.ASTMethodDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTMethodDeclarator;
import net.sourceforge.pmd.lang.java.ast.ASTPrimaryExpression;
import net.sourceforge.pmd.lang.java.ast.ASTPrimaryPrefix;
import net.sourceforge.pmd.lang.java.ast.ASTReturnStatement;
import net.sourceforge.pmd.lang.java.ast.ASTTypeDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTVariableDeclarator;
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
            if (CweUtilities.isExpression(node, expressionToCheck)) {
                checkForViolation = true;
            }
        }

        // if expression is of desired type, check if unsafe
        if (checkForViolation && unsafe(node, null)) {
            addViolation(data, node);
        }

        return super.visit(node, data);
    }

    /**
     * Check if expression is unsafe
     *
     * @param node:  expression to check
     * @param firstArg: optionally override call to getFirstArg, can be left null
     * @return true: if expression is unsafe, false otherwise
     */
    boolean unsafe(ASTPrimaryExpression node, Node firstArg) {

        // get first arg, if no arg, return
        if (firstArg == null) {
            firstArg = CweUtilities.getFirstArg(node);
            if (firstArg == null) {
                return false;
            }
        }

        // if literal, return
        if (firstArg instanceof ASTLiteral) {
            return false;
        }

        // get this method
        ASTBlock thisMethod = CweUtilities.getMethod(node);

        // if variable is local, check assignments
        if (CweUtilities.isVariableLocal(thisMethod, firstArg.getImage())) {
            Node methodDeclaration = thisMethod.jjtGetParent();
            String methodImage = methodDeclaration.findChildrenOfType(ASTMethodDeclarator.class).get(0).getImage();
            return unsafeAssignments(thisMethod, methodImage, firstArg.getImage());
        } else {

            // else check class for calls to this method
            ASTTypeDeclaration thisClass = CweUtilities.getClass(node);
            Node methodDeclaration = thisMethod.jjtGetParent();
            String methodImage = methodDeclaration.findChildrenOfType(ASTMethodDeclarator.class).get(0).getImage();
            return unsafeMethodCalls(thisClass, methodImage);
        }
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

            if (assignment.getFirstParentOfType(ASTArgumentList.class) == null) {
                // if not literal, check further
                if (!(assignment instanceof ASTLiteral)) {
                    ASTBlock thisMethod = CweUtilities.getMethod(expression);
                    String assignmentImage = assignment.getImage();

                    // make sure we aren't already checking the assignment image
                    Node methodDeclaration = thisMethod.jjtGetParent();
                    String methodImage = methodDeclaration.findChildrenOfType(ASTMethodDeclarator.class).get(0).getImage();
                    int i = 0;
                    for (String var : currentlyCheckingVar) {
                        if (var.equals(assignmentImage)) {
                            if (methodImage.equals(currentlyCheckingVarHolder.get(i))) {
                                return false;
                            }
                        }
                        i++;
                    }

                    // is local variable, do recursive check on that variable
                    if (CweUtilities.isVariableLocal(thisMethod, assignmentImage)) {

                        if (unsafeAssignments(thisMethod, methodImage, assignmentImage)) {
                            return true;
                        }
                    } else {

                        // if method call, check method return value
                        Node method = CweUtilities.findMethod(CweUtilities.getClass(expression), assignmentImage);

                        // check for class variable
                        String classVar = CweUtilities.findClassVariable(CweUtilities.getClass(expression), assignmentImage);

                        if (method != null) {
                            if (unsafeMethodReturnStatements(method)) {
                                return true;
                            }
                        } else if (classVar != null) {
                            if (unsafeClassVariableAssignments(CweUtilities.getClass(expression), assignmentImage)) {
                                return true;
                            }
                        } else {
                            // if not a literal, local variable, method call, or class variable, assume unsafe
                            // Add another if statement if any other type of assignment could be safe
                            // System.out.println("Cannot find unsafe expression, assuming it is unsafe");
                            return true;
                        }
                    }
                }
            }
        }
        return false;
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
                ASTBlock method = CweUtilities.getMethod(expression);
                if (CweUtilities.isVariableLocal(method, argName)) {
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
