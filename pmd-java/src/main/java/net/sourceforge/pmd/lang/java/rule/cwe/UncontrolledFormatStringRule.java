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

    @Override
    public Object visit(ASTPrimaryExpression node, Object data) {
        if (isStringFormatExpression(node)) {
            try {
                List<Node> children = node.findChildNodesWithXPath(
                        "./PrimarySuffix/Arguments/ArgumentList/Expression/PrimaryExpression/PrimaryPrefix");
                if (!children.isEmpty()) {
                    if (children.get(0).jjtGetNumChildren() != 0) {
                        Node potentialBadSink = children.get(0).jjtGetChild(0);
                        if (!(potentialBadSink instanceof ASTLiteral)) {
                            //System.out.println("Potentially bad sink found");
                            Node topBlock = findTopBlock(potentialBadSink);
                            String variableImage = potentialBadSink.getImage();

                            if (isLocalVariable(topBlock, variableImage)) {
                                //System.out.println("Potentially bad sink is a local variable");
                                if (!checkAssignments(topBlock, variableImage)) {
                                    addViolation(data, node);
                                }
                            } else {
                                Node methodDeclaration = topBlock.jjtGetParent();
                                String methodImage = methodDeclaration.findChildrenOfType(ASTMethodDeclarator.class).get(0).getImage();

                                Node thisClass = findTopTypeDeclaration(methodDeclaration);
                                String classImage = thisClass.jjtGetChild(0).getImage();

                                List<ASTTypeDeclaration> classes = methodDeclaration.getParentsOfType(ASTTypeDeclaration.class);
                                for (ASTTypeDeclaration cls: classes) {
                                    if (!checkMethodCalls(classImage, methodImage, cls)) {
                                        addViolation(data, node);
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (JaxenException je) {
                throw new RuntimeException(je);
            }
        }
        return super.visit(node, data);
    }

    private boolean checkMethodCalls(String classImage, String methodImage, Node parent) {
        List<ASTPrimaryPrefix> prefixes = parent.findDescendantsOfType(ASTPrimaryPrefix.class);
        for (ASTPrimaryPrefix prefix: prefixes) {
            String checkImage = prefix.jjtGetChild(0).getImage();
            if (checkImage != null && !checkImage.isEmpty()) {
                if (methodImage.equals(checkImage)) {
                    if (!checkArguments(prefix.jjtGetParent())) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private boolean checkArguments(Node expression) {
        //Node expression = prefix.jjtGetParent();
        try {
            List<? extends Node> args = expression.findChildNodesWithXPath(
                    "./PrimarySuffix/Arguments/ArgumentList/Expression/PrimaryExpression/PrimaryPrefix");

            for (Node arg: args) {
                String argImage = arg.jjtGetChild(0).getImage();
                Node topBlock = findTopBlock(expression);
                if (isLocalVariable(topBlock, argImage)) {
                    //System.out.println("Potentially bad sink is a local variable");
                    if (!checkAssignments(topBlock, argImage)) {
                        return false;
                    }
                }
            }
        } catch (JaxenException e) {
            e.printStackTrace();
        }
        return true;
    }

    private boolean isStringFormatExpression(ASTPrimaryExpression expression) {
        if (expression.jjtGetNumChildren() != 0) {
            if (expression.jjtGetChild(0) instanceof ASTPrimaryPrefix) {
                Node primaryPrefix = expression.jjtGetChild(0);
                if (primaryPrefix.jjtGetNumChildren() != 0) {
                    String imageString = primaryPrefix.jjtGetChild(0).getImage();
                    String systemOutFormatString = "System.out.format";
                    String systemOutPrintfString = "System.out.printf";
                    if (imageString != null && !imageString.isEmpty()) {
                        return systemOutFormatString.equals(imageString) || systemOutPrintfString.equals(imageString);
                    } else {
                        return false;
                    }
                }
            }
        }
        return false;
    }

    private Node findTopBlock(Node node) {
        Node parent = node.getFirstParentOfType(ASTBlock.class);

        if (parent == null) {
            return node;
        }
        return findTopBlock(parent);
    }

    private Node findTopTypeDeclaration(Node node) {
        Node parent = node.getFirstParentOfType(ASTTypeDeclaration.class);

        if (parent == null) {
            return node;
        }
        return findTopTypeDeclaration(parent);
    }

    private boolean isLocalVariable(Node parent, String variableImage) {
        List<ASTLocalVariableDeclaration> localVariableDeclarations = parent.findDescendantsOfType(ASTLocalVariableDeclaration.class);

        for (ASTLocalVariableDeclaration localVariableDeclaration: localVariableDeclarations) {
            //System.out.println("Local variable declaration found");
            Node node = localVariableDeclaration.getFirstDescendantOfType(ASTVariableDeclaratorId.class);
            if (node.hasImageEqualTo(variableImage)) {
                return true;
            }
        }
        return false;
    }

    private boolean checkAssignments(Node parentNode, String variableImage) {
        List<ASTAssignmentOperator> assignments = parentNode.findDescendantsOfType(ASTAssignmentOperator.class);

        for (ASTAssignmentOperator assignment: assignments) {
            //System.out.println("Assignment operator found");
            Node directParent = assignment.jjtGetParent();
            Node varPrefix = directParent.jjtGetChild(0).jjtGetChild(0).jjtGetChild(0);
            if (varPrefix.hasImageEqualTo(variableImage)) {
                //System.out.println("Assignment is assigning desired variable");
                Node afterAssignment = directParent.jjtGetChild(2);
                List<ASTPrimaryPrefix> prefixes = afterAssignment.findDescendantsOfType(ASTPrimaryPrefix.class);
                boolean unsafe = false;
                for (ASTPrimaryPrefix prefix: prefixes) {
                    //System.out.println("Found assignment prefix");
                    if (!(prefix.jjtGetChild(0) instanceof ASTLiteral)) {
                        unsafe = true; // Do something with unsafe assignments to further check safety
                    }
                }
                if (unsafe) {
                    // at this point, the var has been assigned to something besides a literal,
                    // the actual assignment should be checked to see if  what it's being assigned to is safe
                    // will use recursion
                    // TODO checkAssignmentOfAssignments()
                    return false;
                }
            }
        }
        return true;
    }
}
