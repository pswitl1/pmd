/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.metrics.impl;

import net.sourceforge.pmd.lang.java.ast.ASTAnyTypeDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTAnyTypeDeclaration.TypeKind;
import net.sourceforge.pmd.lang.java.metrics.signature.JavaOperationSigMask;
import net.sourceforge.pmd.lang.java.metrics.signature.JavaOperationSignature.Role;
import net.sourceforge.pmd.lang.java.metrics.signature.JavaSignature.Visibility;
import net.sourceforge.pmd.lang.metrics.MetricOptions;

/**
 * Number of accessor methods.
 *
 * @author Clément Fournier
 * @since 6.0.0
 */
public class NoamMetric extends AbstractJavaClassMetric {

    @Override
    public boolean supports(ASTAnyTypeDeclaration node) {
        return node.getTypeKind() == TypeKind.CLASS;
    }


    @Override
    public double computeFor(ASTAnyTypeDeclaration node, MetricOptions options) {
        JavaOperationSigMask mask = new JavaOperationSigMask();
        mask.restrictRolesTo(Role.GETTER_OR_SETTER);
        mask.restrictVisibilitiesTo(Visibility.PUBLIC);


        return (double) countMatchingOpSigs(node, mask);
    }
}
