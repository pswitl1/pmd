/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */
/* Generated By:JJTree: Do not edit this line. ASTMethodDeclaration.java */

package net.sourceforge.pmd.lang.java.ast;

import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.dfa.DFAGraphMethod;

public class ASTMethodDeclaration extends ASTMethodOrConstructorDeclaration implements DFAGraphMethod {


    public ASTMethodDeclaration(int id) {
        super(id);
    }

    public ASTMethodDeclaration(JavaParser p, int id) {
        super(p, id);
    }

    /**
     * Accept the visitor. *
     */
    @Override
    public Object jjtAccept(JavaParserVisitor visitor, Object data) {
        return visitor.visit(this, data);
    }

    /**
     * Gets the name of the method.
     *
     * @return a String representing the name of the method
     */
    public String getMethodName() {
        ASTMethodDeclarator md = getFirstChildOfType(ASTMethodDeclarator.class);
        if (md != null) {
            return md.getImage();
        }
        return null;
    }

    public String getName() {
        return getMethodName();
    }

    public boolean isSyntacticallyPublic() {
        return super.isPublic();
    }

    public boolean isSyntacticallyAbstract() {
        return super.isAbstract();
    }

    @Override
    public boolean isPublic() {
        // interface methods are public by default, but could be private since java9
        if (isInterfaceMember() && !isPrivate()) {
            return true;
        }
        return super.isPublic();
    }

    @Override
    public boolean isAbstract() {
        if (isInterfaceMember()) {
            return true;
        }
        return super.isAbstract();
    }


    public boolean isInterfaceMember() {
        // for a real class/interface the 3rd parent is a ClassOrInterfaceDeclaration,
        // for anonymous classes, the parent is e.g. a AllocationExpression
        Node potentialTypeDeclaration = getNthParent(3);

        if (potentialTypeDeclaration instanceof ASTClassOrInterfaceDeclaration) {
            return ((ASTClassOrInterfaceDeclaration) potentialTypeDeclaration).isInterface();
        }
        return false;
    }

    public boolean isVoid() {
        return getResultType().isVoid();
    }

    public ASTResultType getResultType() {
        return getFirstChildOfType(ASTResultType.class);
    }

    public ASTBlock getBlock() {
        for (int i = 0; i < jjtGetNumChildren(); i++) {
            Node n = jjtGetChild(i);
            if (n instanceof ASTBlock) {
                return (ASTBlock) n;
            }
        }
        return null;
    }

    public ASTNameList getThrows() {
        int declaratorIndex = -1;
        for (int i = 0; i < jjtGetNumChildren(); i++) {
            Node child = jjtGetChild(i);
            if (child instanceof ASTMethodDeclarator) {
                declaratorIndex = i;
                break;
            }
        }
        // the throws declaration is immediately followed by the
        // MethodDeclarator
        if (jjtGetNumChildren() > declaratorIndex + 1) {
            Node n = jjtGetChild(declaratorIndex + 1);
            if (n instanceof ASTNameList) {
                return (ASTNameList) n;
            }
        }
        return null;
    }


    @Override
    public MethodLikeKind getKind() {
        return MethodLikeKind.METHOD;
    }

}
