/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.properties.newframework;

import java.util.List;
import java.util.Set;
import java.util.function.Function;


/**
 * @author Clément Fournier
 * @since 6.7.0
 */
final class SingleValuePropertyDescriptor<T> extends AbstractPropertyDescriptor<T> {


    private final T defaultValue;
    private final Function<String, T> parser;


    SingleValuePropertyDescriptor(String name, String description, float uiOrder,
                                  T defaultValue,
                                  Set<PropertyValidator<T>> validators,
                                  Function<String, T> parser,
                                  Class<T> type) {
        super(name, description, uiOrder, validators, type);
        this.defaultValue = defaultValue;
        this.parser = parser;
    }


    @Override
    public boolean isMultiValue() {
        return false;
    }


    @Override
    public T getDefaultValue() {
        return defaultValue;
    }


    @Override
    public T valueFrom(List<String> valuesList) throws IllegalArgumentException {
        if (valuesList.size() != 1) {
            throw new IllegalArgumentException("This property can only handle a single value, but " + valuesList.size() + " was supplied");
        }

        return parser.apply(valuesList.get(0));
    }
}
