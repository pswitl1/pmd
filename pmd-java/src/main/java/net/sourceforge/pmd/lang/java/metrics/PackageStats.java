/**
 *
 */
package net.sourceforge.pmd.lang.java.metrics;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Clément Fournier (clement.fournier@insa-rennes.fr)
 *
 */
public class PackageStats {
    
    private Map<String, PackageStats> m_subPackages = new HashMap<>();
    private Map<String, ClassStats>   m_classes     = new HashMap<>();
    
    public PackageStats getSubPackage(String[] qname, int index) {
        // ...
        // recursive navigation method
        return null;
    }

    public ClassStats getClassStats(String name) {
        return m_classes.get(name);
    }

    public boolean hasMatchingSig(String qname, OperationSigMask sigMask) {
        // navigate to the class in the tree
        // return true if the signature of the qualified name is covered by the
        // mask.
        return true;
    }
}
