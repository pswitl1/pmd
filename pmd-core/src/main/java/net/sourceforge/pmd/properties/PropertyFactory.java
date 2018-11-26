/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.properties;

import java.util.List;
import java.util.Map;

import net.sourceforge.pmd.properties.PropertyBuilder.GenericCollectionPropertyBuilder;
import net.sourceforge.pmd.properties.PropertyBuilder.GenericPropertyBuilder;
import net.sourceforge.pmd.properties.constraints.NumericConstraints;
import net.sourceforge.pmd.properties.constraints.PropertyConstraint;


/**
 * Provides factory methods for common property types.
 * Note: from 7.0.0 on, this will be the only way to
 * build property descriptors.
 *
 * TODO next PR add doc
 *
 * @author Clément Fournier
 * @since 6.10.0
 */
public final class PropertyFactory {

    private PropertyFactory() {

    }


    /**
     * Returns a builder for an integer property. The property descriptor
     * will by default accept any value conforming to the format specified
     * by {@link Integer#parseInt(String)}, e.g. {@code 1234} or {@code -123}.
     * Acceptable values may be further refined by {@linkplain PropertyBuilder#require(PropertyConstraint) adding constraints}.
     * The class {@link NumericConstraints} provides some useful ready-made constraints
     * for that purpose.
     *
     * @param name Name of the property to build
     *
     * @return A new builder
     *
     * @see NumericConstraints
     */
    public static GenericPropertyBuilder<Integer> intProperty(String name) {
        return new GenericPropertyBuilder<>(name, ValueParserConstants.INTEGER_PARSER, Integer.class);
    }


    public static GenericCollectionPropertyBuilder<Integer, List<Integer>> intListProperty(String name) {
        return intProperty(name).toList().delim(MultiValuePropertyDescriptor.DEFAULT_NUMERIC_DELIMITER);
    }


    public static GenericPropertyBuilder<Double> doubleProperty(String name) {
        return new GenericPropertyBuilder<>(name, ValueParserConstants.DOUBLE_PARSER, Double.class);
    }


    public static GenericCollectionPropertyBuilder<Double, List<Double>> doubleListProperty(String name) {
        return doubleProperty(name).toList().delim(MultiValuePropertyDescriptor.DEFAULT_NUMERIC_DELIMITER);
    }


    public static GenericPropertyBuilder<String> stringProperty(String name) {
        return new GenericPropertyBuilder<>(name, ValueParserConstants.STRING_PARSER, String.class);
    }


    public static GenericCollectionPropertyBuilder<String, List<String>> stringListProperty(String name) {
        return stringProperty(name).toList().delim(MultiValuePropertyDescriptor.DEFAULT_DELIMITER);
    }


    public static <T> GenericPropertyBuilder<T> enumProperty(String name, Map<String, T> nameToValue) {
        // TODO find solution to document the set of possible values
        // At best, map that requirement to a constraint (eg make parser return null if not found, and
        // add a non-null constraint with the right description.)
        return new GenericPropertyBuilder<>(name, ValueParserConstants.enumerationParser(nameToValue), (Class<T>) Object.class);
    }


    public static <T> GenericCollectionPropertyBuilder<T, List<T>> enumListProperty(String name, Map<String, T> nameToValue) {
        return enumProperty(name, nameToValue).toList();
    }


}
