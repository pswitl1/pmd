/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.metrics.impl;

import java.util.Map;

import net.sourceforge.pmd.lang.java.metrics.api.JavaClassMetricKey;
import net.sourceforge.pmd.lang.java.metrics.api.JavaOperationMetricKey;
import net.sourceforge.pmd.lang.java.metrics.impl.CycloMetric.CycloOptions;
import net.sourceforge.pmd.lang.metrics.MetricVersion;

/**
 * Tests standard cyclo.
 *
 * @author Clément Fournier
 */
public class CycloTestRule extends AbstractMetricTestRule {

    @Override
    protected JavaClassMetricKey getClassKey() {
        return null;
    }


    @Override
    protected JavaOperationMetricKey getOpKey() {
        return JavaOperationMetricKey.CYCLO;
    }


    @Override
    protected Map<String, MetricVersion> optionMappings() {
        Map<String, MetricVersion> mappings = super.optionMappings();
        mappings.put("ignoreBooleanPaths", CycloOptions.IGNORE_BOOLEAN_PATHS);
        return mappings;
    }
}
