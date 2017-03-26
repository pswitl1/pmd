/**
 *
 */
package net.sourceforge.pmd.lang.java.metrics;

import net.sourceforge.pmd.lang.java.ast.ASTMethodDeclaration;

/**
 * @author Clément Fournier (clement.fournier@insa-rennes.fr)
 *
 */
public interface MethodMetric extends Metric {
    public double computeFor(ASTMethodDeclaration node, DataHolder holder);
}
