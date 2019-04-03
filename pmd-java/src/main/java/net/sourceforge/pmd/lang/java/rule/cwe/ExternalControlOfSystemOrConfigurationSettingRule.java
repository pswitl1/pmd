/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.rule.cwe;

/**
 * Rule designed to detect CWE15_External_Control_of_System_or_Configuration_Setting
 */
public class ExternalControlOfSystemOrConfigurationSettingRule extends AbstractUncontrolledStringRule {

    /**
     * Public constructor to set expressions to check for this string rule
     */
    public ExternalControlOfSystemOrConfigurationSettingRule() {

        // add expressions to check for
        expressionsToCheck.add("dbConnection.setCatalog");
    }
}
