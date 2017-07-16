/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.oom.metrics;

import net.sourceforge.pmd.lang.java.ast.ASTAnyTypeDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTMethodOrConstructorDeclaration;
import net.sourceforge.pmd.lang.java.oom.api.MetricVersion;

/**
 * Lines of Code. Equates the length in lines of code of the measured entity, counting everything including blank lines
 * and comments from the class declaration to the last closing brace. Import statements are not counted, for nested
 * classes to be comparable to outer ones.
 *
 * @author Clément Fournier
 * @see NcssMetric
 * @since June 2017
 */
public final class LocMetric {

    private LocMetric() {

    }


    public static final class OperationMetric extends AbstractOperationMetric {

        @Override
        public boolean supports(ASTMethodOrConstructorDeclaration node) {
            return true;
        }


        @Override
        public double computeFor(ASTMethodOrConstructorDeclaration node, MetricVersion version) {
            return 1 + node.getEndLine() - node.getBeginLine();
        }
    }

    public static final class ClassMetric extends AbstractClassMetric {

        @Override
        public boolean supports(ASTAnyTypeDeclaration node) {
            return true;
        }


        @Override
        public double computeFor(ASTAnyTypeDeclaration node, MetricVersion version) {
            return 1 + node.getEndLine() - node.getBeginLine();
        }


    }

}
