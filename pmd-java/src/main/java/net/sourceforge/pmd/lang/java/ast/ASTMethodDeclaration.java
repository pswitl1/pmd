/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */
/* Generated By:JJTree: Do not edit this line. ASTMethodDeclaration.java */

package net.sourceforge.pmd.lang.java.ast;

import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.dfa.DFAGraphMethod;

public class ASTMethodDeclaration extends AbstractJavaAccessNode implements DFAGraphMethod, ASTMethodOrConstructorDeclaration {
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
        if (isInterfaceMember()) {
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
        ASTClassOrInterfaceDeclaration clz = getFirstParentOfType(ASTClassOrInterfaceDeclaration.class);
        return clz != null && clz.isInterface();
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
    public String getQualifiedName() {
        StringBuilder qname = new StringBuilder();
        qname.append(this.getFirstChildOfType(ASTClassOrInterfaceDeclaration.class).getQualifiedName());
        qname.append(METHOD_DELIMITER);
        qname.append(getMethodName());

        qname.append(LEFT_PARAM_DELIMITER);
        ASTFormalParameters params = this.getFirstChildOfType(ASTFormalParameters.class);
        int lastParam = params.jjtGetNumChildren() - 1;
        for (int i = 0; i < lastParam; i++) {
            qname.append(params.jjtGetChild(i).getFirstDescendantOfType(ASTType.class).getTypeImage());
            qname.append(PARAMLIST_DELIMITER);

        }

        qname.append(params.jjtGetChild(lastParam).getFirstDescendantOfType(ASTType.class).getTypeImage());
        qname.append(RIGHT_PARAM_DELIMITER);

        return qname.toString();
    }
}
