/**
 *
 */
package net.sourceforge.pmd.lang.java.rule.metrics;

import net.sourceforge.pmd.lang.java.ast.ASTClassOrInterfaceDeclaration;
import net.sourceforge.pmd.lang.java.metrics.Metrics;
import net.sourceforge.pmd.lang.java.metrics.Metrics.ClassMetricKey;
import net.sourceforge.pmd.lang.java.rule.AbstractJavaRule;

/**
 * @author Clément Fournier (clement.fournier@insa-rennes.fr)
 *
 */
public class AtfdRule extends AbstractJavaRule {

    @Override
    public Object visit(ASTClassOrInterfaceDeclaration node, Object data) {
        double atfd = Metrics.get(ClassMetricKey.ATFD, node);
        if (atfd > .3) {
            addViolation(data, node);
        }
        return data;
    }
    
}
