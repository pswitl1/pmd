/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.properties;

import java.util.Map;

import net.sourceforge.pmd.properties.PropertyBuilder.GenericListPropertyBuilder;
import net.sourceforge.pmd.properties.PropertyBuilder.GenericPropertyBuilder;


/**
 * Provides factory methods for common property types.
 * Note: from 7.0.0 on, this will be the only way to
 * build property descriptors.
 *
 * @author Clément Fournier
 * @since 6.10.0
 */
public final class PropertyFactory {

    private PropertyFactory() {

    }


    public static GenericPropertyBuilder<Integer> intProperty(String name) {
        return new GenericPropertyBuilder<>(name, ValueParserConstants.INTEGER_PARSER, Integer.class);
    }


    public static GenericListPropertyBuilder<Integer> intListProperty(String name) {
        return intProperty(name).toList();
    }


    public static GenericPropertyBuilder<Double> doubleProperty(String name) {
        return new GenericPropertyBuilder<>(name, ValueParserConstants.DOUBLE_PARSER, Double.class);
    }


    public static GenericListPropertyBuilder<Double> doubleListProperty(String name) {
        return doubleProperty(name).toList();
    }


    public static <T> GenericPropertyBuilder<T> enumProperty(String name, Map<String, T> nameToValue) {
        // TODO find solution to document the set of possible values
        // At best, map that requirement to a constraint (eg make parser return null if not found, and
        // add a non-null constraint with the right description. But if we disallow null values everywhere,
        // this will get caught early on.)
        return new GenericPropertyBuilder<>(name, ValueParserConstants.enumerationParser(nameToValue), (Class<T>) Object.class);
    }


    public static <T> GenericListPropertyBuilder<T> enumListProperty(String name, Map<String, T> nameToValue) {
        return enumProperty(name, nameToValue).toList();
    }


}
