/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.metrics.internal;

import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.metrics.MetricMemoizer;
import net.sourceforge.pmd.lang.metrics.ParameterizedMetricKey;


/**
 * Memoizes nothing.
 *
 * @author Clément Fournier
 * @since 7.0.0
 */
public class DummyMetricMemoizer<N extends Node> implements MetricMemoizer<N> {

    private static final DummyMetricMemoizer<Node> INSTANCE = new DummyMetricMemoizer<>();


    private DummyMetricMemoizer() {

    }


    @Override
    public Double getMemo(ParameterizedMetricKey<N> key) {
        return null;
    }


    @Override
    public void memoize(ParameterizedMetricKey<N> key, double value) {

    }


    @SuppressWarnings("unchecked")
    public static <N extends Node> DummyMetricMemoizer<N> getInstance() {
        return (DummyMetricMemoizer<N>) INSTANCE;
    }
}
