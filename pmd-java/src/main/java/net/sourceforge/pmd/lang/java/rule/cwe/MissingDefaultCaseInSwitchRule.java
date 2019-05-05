/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.rule.cwe;

import java.util.List;

import net.sourceforge.pmd.lang.java.ast.ASTSwitchLabel;
import net.sourceforge.pmd.lang.java.ast.ASTSwitchStatement;
import net.sourceforge.pmd.lang.java.rule.AbstractJavaRule;

/**
 * Rule designed to detect CWE478_Missing_Default_Case_in_Switch
 */
public class MissingDefaultCaseInSwitchRule extends AbstractJavaRule {

    @Override
    public Object visit(ASTSwitchStatement node, Object data) {

        List<ASTSwitchLabel> switchLabelList = node.findChildrenOfType(ASTSwitchLabel.class);

        for (ASTSwitchLabel switchLabel: switchLabelList) {
            if (switchLabel.isDefault()) {
                return super.visit(node, data);
            }
        }

        // no default found
        addViolation(data, node);

        return super.visit(node, data);
    }
}
