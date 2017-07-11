/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.oom.metrics;

import org.apache.commons.lang3.mutable.MutableInt;

import net.sourceforge.pmd.lang.java.ast.ASTAnyTypeDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTMethodOrConstructorDeclaration;
import net.sourceforge.pmd.lang.java.ast.JavaParserVisitor;
import net.sourceforge.pmd.lang.java.oom.AbstractMetric;
import net.sourceforge.pmd.lang.java.oom.api.ClassMetric;
import net.sourceforge.pmd.lang.java.oom.api.MetricVersion;
import net.sourceforge.pmd.lang.java.oom.api.OperationMetric;
import net.sourceforge.pmd.lang.java.oom.metrics.visitors.DefaultNcssVisitor;
import net.sourceforge.pmd.lang.java.oom.metrics.visitors.JavaNcssVisitor;

/**
 * Non Commenting Source Statements. Similar to LOC but only counts statements, which is roughly equivalent to counting
 * the number of semicolons and opening braces in the program.
 *
 * <p>The standard version's precise rules for counting statements comply with <a href="http://www.kclee.de/clemens/java/javancss/">JavaNCSS
 * rules</a>. The only difference is that import and package statements are not counted.
 *
 * <p>Version {@link Version#JAVANCSS}: Import and package statements are counted. This version fully complies with
 * JavaNcss rules.
 *
 * @author Clément Fournier
 * @see LocMetric
 * @since June 2017
 */
public final class NcssMetric extends AbstractMetric implements ClassMetric, OperationMetric {

    @Override
    public boolean supports(ASTAnyTypeDeclaration node) {
        return true;
    }


    @Override
    public boolean supports(ASTMethodOrConstructorDeclaration node) {
        return true;
    }


    @Override
    public double computeFor(ASTAnyTypeDeclaration node, MetricVersion version) {
        JavaParserVisitor visitor = (Version.JAVANCSS.equals(version))
                                    ? new JavaNcssVisitor()
                                    : new DefaultNcssVisitor();

        MutableInt ncss = (MutableInt) node.jjtAccept(visitor, new MutableInt(0));
        return (double) ncss.getValue();
    }


    @Override
    public double computeFor(ASTMethodOrConstructorDeclaration node, MetricVersion version) {
        JavaParserVisitor visitor = (Version.JAVANCSS.equals(version))
                                    ? new JavaNcssVisitor()
                                    : new DefaultNcssVisitor();

        MutableInt ncss = (MutableInt) node.jjtAccept(visitor, new MutableInt(0));
        return (double) ncss.getValue();
    }


    public enum Version implements MetricVersion {
        /** JavaNCSS compliant cyclo visitor. */
        JAVANCSS
    }

}
