/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */
/* Generated By:JJTree: Do not edit this line. ASTFieldDeclaration.java */

package net.sourceforge.pmd.lang.java.ast;

import net.sourceforge.pmd.lang.ast.SignedNode;
import net.sourceforge.pmd.lang.java.metrics.signature.JavaFieldSignature;

public class ASTFieldDeclaration extends AbstractJavaAccessTypeNode implements Dimensionable, SignedNode<ASTFieldDeclaration> {


    public ASTFieldDeclaration(int id) {
        super(id);
    }

    public ASTFieldDeclaration(JavaParser p, int id) {
        super(p, id);
    }

    /**
     * Accept the visitor. *
     */
    @Override
    public Object jjtAccept(JavaParserVisitor visitor, Object data) {
        return visitor.visit(this, data);
    }

    public boolean isSyntacticallyPublic() {
        return super.isPublic();
    }

    @Override
    public boolean isPublic() {
        if (isAnnotationMember() || isInterfaceMember()) {
            return true;
        }
        return super.isPublic();
    }

    public boolean isSyntacticallyStatic() {
        return super.isStatic();
    }

    @Override
    public boolean isStatic() {
        if (isAnnotationMember() || isInterfaceMember()) {
            return true;
        }
        return super.isStatic();
    }

    public boolean isSyntacticallyFinal() {
        return super.isFinal();
    }

    @Override
    public boolean isFinal() {
        if (isAnnotationMember() || isInterfaceMember()) {
            return true;
        }
        return super.isFinal();
    }

    @Override
    public boolean isPrivate() {
        if (isAnnotationMember() || isInterfaceMember()) {
            return false;
        }
        return super.isPrivate();
    }

    @Override
    public boolean isPackagePrivate() {
        if (isAnnotationMember() || isInterfaceMember()) {
            return false;
        }
        return super.isPackagePrivate();
    }

    @Override
    public boolean isProtected() {
        if (isAnnotationMember() || isInterfaceMember()) {
            return false;
        }
        return super.isProtected();
    }

    public boolean isAnnotationMember() {
        if (jjtGetParent().jjtGetParent() instanceof ASTAnnotationTypeBody) {
            return true;
        }
        return false;
    }

    public boolean isInterfaceMember() {
        if (jjtGetParent().jjtGetParent() instanceof ASTEnumBody) {
            return false;
        }
        ASTClassOrInterfaceDeclaration n = getFirstParentOfType(ASTClassOrInterfaceDeclaration.class);
        return n != null && n.isInterface();
    }

    public boolean isArray() {
        return checkType() + checkDecl() > 0;
    }

    public int getArrayDepth() {
        if (!isArray()) {
            return 0;
        }
        return checkType() + checkDecl();
    }

    private int checkType() {
        if (jjtGetNumChildren() == 0 || !(jjtGetChild(0) instanceof ASTType)) {
            return 0;
        }
        return ((ASTType) jjtGetChild(0)).getArrayDepth();
    }

    private int checkDecl() {
        if (jjtGetNumChildren() < 2 || !(jjtGetChild(1) instanceof ASTVariableDeclarator)) {
            return 0;
        }
        return ((ASTVariableDeclaratorId) jjtGetChild(1).jjtGetChild(0)).getArrayDepth();
    }

    /**
     * Gets the variable name of this field. This method searches the first
     * VariableDeclartorId node and returns its image or <code>null</code> if
     * the child node is not found.
     *
     * @return a String representing the name of the variable
     */
    public String getVariableName() {
        ASTVariableDeclaratorId decl = getFirstDescendantOfType(ASTVariableDeclaratorId.class);
        if (decl != null) {
            return decl.getImage();
        }
        return null;
    }


    @Override
    public JavaFieldSignature getSignature() {
        return JavaFieldSignature.buildFor(this);
    }
}
