/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.rule.cwe;

/**
 * Rule designed to detect CWE134_Uncontrolled_String_Format
 */
public class UncontrolledFormatStringRule extends AbstractUncontrolledStringRule {

    /**
     * Public constructor to set expressions to check for this string rule
     */
    public UncontrolledFormatStringRule() {

        // add expressions to check for
        expressionsToCheck.add("System.out.format");
        expressionsToCheck.add("System.out.printf");
    }
}
