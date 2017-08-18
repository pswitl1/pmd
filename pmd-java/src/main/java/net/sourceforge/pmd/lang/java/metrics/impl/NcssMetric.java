/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.metrics.impl;

import java.util.Set;

import org.apache.commons.lang3.mutable.MutableInt;

import net.sourceforge.pmd.lang.java.ast.ASTAnyTypeDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTMethodOrConstructorDeclaration;
import net.sourceforge.pmd.lang.java.ast.JavaParserDecoratedVisitor;
import net.sourceforge.pmd.lang.java.metrics.impl.visitors.NcssBaseVisitor;
import net.sourceforge.pmd.lang.java.metrics.impl.visitors.NcssCountImportsDecorator;
import net.sourceforge.pmd.lang.metrics.MetricOption;
import net.sourceforge.pmd.lang.metrics.MetricOptions;

/**
 * Non Commenting Source Statements. Similar to LOC but only counts statements, which is roughly equivalent to counting
 * the number of semicolons and opening braces in the program.
 *
 * <p>The standard version's precise rules for counting statements comply with <a href="http://www.kclee.de/clemens/java/javancss/">JavaNCSS
 * rules</a>. The only difference is that import and package statements are not counted.
 *
 * <p>Option {@link NcssOption#COUNT_IMPORTS}: Import and package statements are counted. Using that alone makes the
 * metric fully comply with JavaNcss rules.
 *
 * @author Clément Fournier
 * @see LocMetric
 * @since June 2017
 */
public final class NcssMetric {


    /** Variants of NCSS. */
    public enum NcssOption implements MetricOption {
        /** Counts import and package statement. This makes the metric JavaNCSS compliant. */
        COUNT_IMPORTS
    }

    public static final class NcssClassMetric extends AbstractJavaClassMetric {

        @Override
        public boolean supports(ASTAnyTypeDeclaration node) {
            return true;
        }


        @Override
        public double computeFor(ASTAnyTypeDeclaration node, MetricOptions version) {
            Set<MetricOption> options = version.getOptions();
            JavaParserDecoratedVisitor visitor = new JavaParserDecoratedVisitor(NcssBaseVisitor.INSTANCE);

            if (options.contains(NcssOption.COUNT_IMPORTS)) {
                visitor.decorateWith(new NcssCountImportsDecorator());
            }

            // decorate

            MutableInt ncss = (MutableInt) node.jjtAccept(visitor, new MutableInt(0));
            return (double) ncss.getValue();
        }

    }

    public static final class NcssOperationMetric extends AbstractJavaOperationMetric {

        @Override
        public boolean supports(ASTMethodOrConstructorDeclaration node) {
            return true;
        }


        @Override
        public double computeFor(ASTMethodOrConstructorDeclaration node, MetricOptions version) {
            Set<MetricOption> options = version.getOptions();
            JavaParserDecoratedVisitor visitor = new JavaParserDecoratedVisitor(NcssBaseVisitor.INSTANCE);

            if (options.contains(NcssOption.COUNT_IMPORTS)) {
                visitor.decorateWith(new NcssCountImportsDecorator());
            }

            MutableInt ncss = (MutableInt) node.jjtAccept(visitor, new MutableInt(0));
            return (double) ncss.getValue();
        }

    }

}
