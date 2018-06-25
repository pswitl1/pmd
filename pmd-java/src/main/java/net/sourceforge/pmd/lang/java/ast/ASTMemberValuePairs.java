/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */
/* Generated By:JJTree: Do not edit this line. ASTMemberValuePairs.java */

package net.sourceforge.pmd.lang.java.ast;

import java.util.Iterator;


/**
 * Represents a list of member values in an {@linkplain ASTNormalAnnotation annotation}.
 *
 * <pre>
 *
 *  MemberValuePairs ::= {@linkplain ASTMemberValuePair MemberValuePair} ( "," {@linkplain ASTMemberValuePair MemberValuePair} )*
 *
 * </pre>
 */
public class ASTMemberValuePairs extends AbstractJavaNode implements Iterable<ASTMemberValuePair> {
    public ASTMemberValuePairs(int id) {
        super(id);
    }


    public ASTMemberValuePairs(JavaParser p, int id) {
        super(p, id);
    }


    @Override
    public Object jjtAccept(JavaParserVisitor visitor, Object data) {
        return visitor.visit(this, data);
    }


    @Override
    public ASTMemberValuePair jjtGetChild(int index) {
        return (ASTMemberValuePair) super.jjtGetChild(index);
    }


    @Override
    public ASTNormalAnnotation jjtGetParent() {
        return (ASTNormalAnnotation) super.jjtGetParent();
    }


    @Override
    public Iterator<ASTMemberValuePair> iterator() {
        return new NodeChildrenIterator<>(this, ASTMemberValuePair.class);
    }
}
