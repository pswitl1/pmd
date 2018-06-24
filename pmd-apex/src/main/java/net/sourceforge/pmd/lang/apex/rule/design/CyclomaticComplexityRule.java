/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.apex.rule.design;


import java.util.Stack;

import net.sourceforge.pmd.lang.apex.ast.ASTMethod;
import net.sourceforge.pmd.lang.apex.ast.ASTUserClass;
import net.sourceforge.pmd.lang.apex.ast.ASTUserTrigger;
import net.sourceforge.pmd.lang.apex.metrics.ApexMetrics;
import net.sourceforge.pmd.lang.apex.metrics.api.ApexClassMetricKey;
import net.sourceforge.pmd.lang.apex.metrics.api.ApexOperationMetricKey;
import net.sourceforge.pmd.lang.apex.rule.AbstractApexRule;
import net.sourceforge.pmd.lang.metrics.ResultOption;
import net.sourceforge.pmd.properties.IntegerProperty;


/**
 * Cyclomatic complexity rule using metrics. Uses Wmc to report classes.
 * 
 * @author Clément Fournier
 */
public class CyclomaticComplexityRule extends AbstractApexRule {

    private static final IntegerProperty CLASS_LEVEL_DESCRIPTOR
        = IntegerProperty.named("classReportLevel")
                         .desc("Total class complexity reporting threshold")
                         .range(1, 200)
                         .defaultValue(40)
                         .uiOrder(1.0f).build();

    private static final IntegerProperty METHOD_LEVEL_DESCRIPTOR 
        = IntegerProperty.named("methodReportLevel")
                         .desc("Cyclomatic complexity reporting threshold")
                         .range(1, 30)
                         .defaultValue(10)
                         .uiOrder(2.0f).build();

    private Stack<String> classNames = new Stack<>();
    private boolean inTrigger;


    public CyclomaticComplexityRule() {
        definePropertyDescriptor(CLASS_LEVEL_DESCRIPTOR);
        definePropertyDescriptor(METHOD_LEVEL_DESCRIPTOR);
    }


    @Override
    public Object visit(ASTUserTrigger node, Object data) {
        inTrigger = true;
        super.visit(node, data);
        inTrigger = false;
        return data;
    }


    @Override
    public Object visit(ASTUserClass node, Object data) {

        classNames.push(node.getImage());
        super.visit(node, data);
        classNames.pop();

        if (ApexClassMetricKey.WMC.supports(node)) {
            int classWmc = (int) ApexMetrics.get(ApexClassMetricKey.WMC, node);

            if (classWmc >= getProperty(CLASS_LEVEL_DESCRIPTOR)) {
                int classHighest = (int) ApexMetrics.get(ApexOperationMetricKey.CYCLO, node, ResultOption.HIGHEST);

                String[] messageParams = {"class",
                                          node.getImage(),
                                          " total",
                                          classWmc + " (highest " + classHighest + ")", };

                addViolation(data, node, messageParams);
            }
        }
        return data;
    }


    @Override
    public final Object visit(ASTMethod node, Object data) {

        int cyclo = (int) ApexMetrics.get(ApexOperationMetricKey.CYCLO, node);
        if (cyclo >= getProperty(METHOD_LEVEL_DESCRIPTOR)) {
            String opType = inTrigger ? "trigger"
                                      : node.getImage().equals(classNames.peek()) ? "constructor"
                                                                                  : "method";

            addViolation(data, node, new String[]{opType,
                                                  node.getQualifiedName().getOperation(),
                                                  "",
                                                  "" + cyclo, });
        }

        return data;
    }

}
