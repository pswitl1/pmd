/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */
/* Generated By:JJTree: Do not edit this line. ASTEnumDeclaration.java */

package net.sourceforge.pmd.lang.java.ast;

public class ASTEnumDeclaration extends AbstractJavaAccessTypeNode implements ASTAnyTypeDeclaration {

    private QualifiedName qualifiedName;

    public ASTEnumDeclaration(int id) {
        super(id);
    }

    public ASTEnumDeclaration(JavaParser p, int id) {
        super(p, id);
    }

    public Object jjtAccept(JavaParserVisitor visitor, Object data) {
        return visitor.visit(this, data);
    }

    public boolean isNested() {
        return jjtGetParent() instanceof ASTClassOrInterfaceBodyDeclaration
            || jjtGetParent() instanceof ASTAnnotationTypeMemberDeclaration;
    }

    @Override
    public QualifiedName getQualifiedName() {
        if (qualifiedName == null) {
            if (isNested()) {
                ASTAnyTypeDeclaration parent = this.getFirstParentOfType(ASTAnyTypeDeclaration.class);
                QualifiedName parentQN = parent.getQualifiedName();
                qualifiedName = QualifiedName.makeNestedClassOf(parentQN, this.getImage());
                return qualifiedName;
            }

            qualifiedName = QualifiedName.makeOuterClassOf(this);
        }

        return qualifiedName;
    }

    @Override
    public TypeKind getTypeKind(){
        return TypeKind.ENUM;
    }
}
